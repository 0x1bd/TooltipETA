package org.kvxd.tooltipeta

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.projectile.arrow.ThrownTrident
import net.minecraft.world.phys.Vec3
import org.kvxd.tooltipeta.mixin.ThrownTridentAccessor
import java.util.Locale
import kotlin.math.ceil

object LoyaltyTridentReturnTracker {
    private const val MAX_SIMULATED_TICKS = 200
    private const val TICKS_PER_SECOND = 20.0
    private const val RETURN_VERTICAL_CATCHUP = 0.015
    private const val RETURN_VELOCITY_DAMPING = 0.95
    private const val RETURN_ACCELERATION_PER_LEVEL = 0.05
    private const val POST_MOVE_INERTIA = 0.99

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            tick(client)
        }
    }

    private fun tick(client: Minecraft) {
        val config = TooltipETAConfigManager.config
        if (!config.general.enabled || !config.features.showLoyaltyTridentReturnEta) return

        val player = client.player ?: return
        val level = client.level ?: return

        val estimate = level.entitiesForRendering()
            .asSequence()
            .filterIsInstance<ThrownTrident>()
            .mapNotNull { trident -> estimateReturn(trident, client) }
            .minByOrNull { it.ticks }
            ?: return

        player.sendOverlayMessage(
            Component.translatable(
                "tooltip.tooltipeta.trident_return_eta",
                loyaltyName(estimate.loyaltyLevel),
                Component.literal(formatSeconds(estimate.ticks)).withStyle(etaColor(estimate.ticks)),
                Component.translatable(
                    "tooltip.tooltipeta.trident_return_distance",
                    formatDistance(estimate.distance)
                ).withStyle(distanceColor(estimate.distance))
            ).withStyle(ChatFormatting.GRAY)
        )
    }

    private fun estimateReturn(trident: ThrownTrident, client: Minecraft): ReturnEstimate? {
        val player = client.player ?: return null
        val owner = trident.owner ?: return null
        if (owner.uuid != player.uuid) return null
        if (!owner.isAlive || player.isSpectator) return null
        if (!trident.isNoPhysics && trident.clientSideReturnTridentTickCount <= 0) return null

        val loyaltyLevel = getLoyaltyLevel(trident)
        if (loyaltyLevel <= 0) return null

        val ticks = simulateReturnTicks(
            startPosition = trident.position(),
            startVelocity = trident.deltaMovement,
            targetPosition = player.eyePosition,
            targetVelocity = player.deltaMovement,
            pickupDistance = player.bbWidth + 1.0,
            loyaltyLevel = loyaltyLevel
        ) ?: return null

        return ReturnEstimate(
            ticks = ticks,
            loyaltyLevel = loyaltyLevel,
            distance = trident.position().distanceTo(player.eyePosition)
        )
    }

    private fun getLoyaltyLevel(trident: ThrownTrident): Int {
        return trident.entityData.get(ThrownTridentAccessor.getLoyaltyDataAccessor()).toInt()
    }

    private fun simulateReturnTicks(
        startPosition: Vec3,
        startVelocity: Vec3,
        targetPosition: Vec3,
        targetVelocity: Vec3,
        pickupDistance: Double,
        loyaltyLevel: Int
    ): Int? {
        var position = startPosition
        var velocity = startVelocity
        var target = targetPosition
        val pickupDistanceSqr = pickupDistance * pickupDistance

        if (target.subtract(position).lengthSqr() <= pickupDistanceSqr) {
            return 1
        }

        for (tick in 1..MAX_SIMULATED_TICKS) {
            val toTarget = target.subtract(position)
            if (toTarget.lengthSqr() <= pickupDistanceSqr) {
                return tick
            }

            position = Vec3(
                position.x,
                position.y + toTarget.y * RETURN_VERTICAL_CATCHUP * loyaltyLevel,
                position.z
            )
            velocity = velocity
                .scale(RETURN_VELOCITY_DAMPING)
                .add(toTarget.normalize().scale(RETURN_ACCELERATION_PER_LEVEL * loyaltyLevel))
            position = position.add(velocity)
            velocity = velocity.scale(POST_MOVE_INERTIA)
            target = target.add(targetVelocity)
        }

        return null
    }

    private fun formatSeconds(ticks: Int): String {
        val seconds = ticks / TICKS_PER_SECOND
        return if (seconds < 10.0) {
            String.format(Locale.ROOT, "%.1fs", seconds)
        } else {
            "${ceil(seconds).toInt()}s"
        }
    }

    private fun formatDistance(distance: Double): String {
        return if (distance < 10.0) {
            String.format(Locale.ROOT, "%.1f", distance)
        } else {
            ceil(distance).toInt().toString()
        }
    }

    private fun etaColor(ticks: Int): ChatFormatting {
        return when {
            ticks <= 40 -> ChatFormatting.GREEN
            ticks <= 80 -> ChatFormatting.YELLOW
            else -> ChatFormatting.RED
        }
    }

    private fun distanceColor(distance: Double): ChatFormatting {
        return when {
            distance <= 10.0 -> ChatFormatting.GREEN
            distance <= 25.0 -> ChatFormatting.YELLOW
            else -> ChatFormatting.RED
        }
    }

    private fun loyaltyName(level: Int): Component {
        return Component.translatable("enchantment.minecraft.loyalty")
            .withStyle(ChatFormatting.AQUA)
            .append(" ")
            .append(
                when (level) {
                    1 -> "I"
                    2 -> "II"
                    3 -> "III"
                    else -> level.toString()
                }
            )
    }

    private data class ReturnEstimate(
        val ticks: Int,
        val loyaltyLevel: Int,
        val distance: Double
    )
}
