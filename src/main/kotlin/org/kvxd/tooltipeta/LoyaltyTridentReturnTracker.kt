package org.kvxd.tooltipeta

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.util.Formatting
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.entity.projectile.TridentEntity
import net.minecraft.util.math.Vec3d
import org.kvxd.tooltipeta.mixin.TridentEntityAccessor
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

    private fun tick(client: MinecraftClient) {
        val config = TooltipETAConfigManager.config
        if (!config.general.enabled || !config.features.showLoyaltyTridentReturnEta) return

        val player = client.player ?: return
        val level = client.world ?: return

        val estimate = level.entities
            .asSequence()
            .filterIsInstance<TridentEntity>()
            .mapNotNull { trident -> estimateReturn(trident, client) }
            .minByOrNull { it.ticks }
            ?: return

        player.sendMessage(
            Text.translatable(
                "tooltip.tooltipeta.trident_return_eta",
                loyaltyName(estimate.loyaltyLevel),
                Text.literal(formatSeconds(estimate.ticks)).formatted(etaColor(estimate.ticks)),
                Text.translatable(
                    "tooltip.tooltipeta.trident_return_distance",
                    formatDistance(estimate.distance)
                ).formatted(distanceColor(estimate.distance))
            ).formatted(Formatting.GRAY),
            true
        )
    }

    private fun estimateReturn(trident: TridentEntity, client: MinecraftClient): ReturnEstimate? {
        val player = client.player ?: return null
        val owner = trident.owner ?: return null
        if (owner.uuid != player.uuid) return null
        if (!owner.isAlive || player.isSpectator) return null
        if (!trident.noClip && trident.returnTimer <= 0) return null

        val loyaltyLevel = getLoyaltyLevel(trident)
        if (loyaltyLevel <= 0) return null

        val ticks = simulateReturnTicks(
            startPosition = trident.entityPos,
            startVelocity = trident.velocity,
            targetPosition = player.eyePos,
            targetVelocity = player.velocity,
            pickupDistance = player.width + 1.0,
            loyaltyLevel = loyaltyLevel
        ) ?: return null

        return ReturnEstimate(
            ticks = ticks,
            loyaltyLevel = loyaltyLevel,
            distance = trident.entityPos.distanceTo(player.eyePos)
        )
    }

    private fun getLoyaltyLevel(trident: TridentEntity): Int {
        return trident.dataTracker.get(TridentEntityAccessor.getLoyaltyDataAccessor()).toInt()
    }

    private fun simulateReturnTicks(
        startPosition: Vec3d,
        startVelocity: Vec3d,
        targetPosition: Vec3d,
        targetVelocity: Vec3d,
        pickupDistance: Double,
        loyaltyLevel: Int
    ): Int? {
        var position = startPosition
        var velocity = startVelocity
        var target = targetPosition
        val pickupDistanceSqr = pickupDistance * pickupDistance

        if (target.subtract(position).lengthSquared() <= pickupDistanceSqr) {
            return 1
        }

        for (tick in 1..MAX_SIMULATED_TICKS) {
            val toTarget = target.subtract(position)
            if (toTarget.lengthSquared() <= pickupDistanceSqr) {
                return tick
            }

            position = Vec3d(
                position.x,
                position.y + toTarget.y * RETURN_VERTICAL_CATCHUP * loyaltyLevel,
                position.z
            )
            velocity = velocity
                .multiply(RETURN_VELOCITY_DAMPING)
                .add(toTarget.normalize().multiply(RETURN_ACCELERATION_PER_LEVEL * loyaltyLevel))
            position = position.add(velocity)
            velocity = velocity.multiply(POST_MOVE_INERTIA)
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

    private fun etaColor(ticks: Int): Formatting {
        return when {
            ticks <= 40 -> Formatting.GREEN
            ticks <= 80 -> Formatting.YELLOW
            else -> Formatting.RED
        }
    }

    private fun distanceColor(distance: Double): Formatting {
        return when {
            distance <= 10.0 -> Formatting.GREEN
            distance <= 25.0 -> Formatting.YELLOW
            else -> Formatting.RED
        }
    }

    private fun loyaltyName(level: Int): Text {
        return Text.translatable("enchantment.minecraft.loyalty")
            .formatted(Formatting.AQUA)
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
