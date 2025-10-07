package org.kvxd.tooltipeta

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object TooltipHandler {

    fun appendTooltip(stack: ItemStack, lines: MutableList<Text>) {
        val player = MinecraftClient.getInstance().player ?: return

        when {
            stack.item == Items.ELYTRA -> appendElytraTooltip(stack, player, lines)
            ETAUtils.isArmor(stack) -> appendArmorTooltip(stack, player, lines)
            stack.isDamageable && stack.maxDamage > 0 -> appendToolTooltip(stack, player, lines)
        }
    }

    private fun appendElytraTooltip(stack: ItemStack, player: PlayerEntity, lines: MutableList<Text>) {
        val seconds = ETAUtils.calculateElytraTime(stack, player)
        if (seconds > 0) {
            lines.add(Text.literal(""))
            lines.add(Text.translatable("tooltip.tooltipeta.flight_time").formatted(Formatting.GRAY))

            val formattedTime = ETAUtils.formatTime(seconds)
            val color = ETAUtils.getEstimateColor(seconds, stack)
            lines.add(Text.literal(" $formattedTime").formatted(color))
        }
    }

    private fun appendToolTooltip(stack: ItemStack, player: PlayerEntity, lines: MutableList<Text>) {
        val uses = ETAUtils.calculateToolUses(stack, player)
        if (uses > 0) {
            lines.add(Text.literal(""))
            lines.add(Text.translatable("tooltip.tooltipeta.estimated_uses").formatted(Formatting.GRAY))

            val formatted = ETAUtils.formatNumber(uses)
            val color = ETAUtils.getEstimateColor(uses, stack)
            lines.add(Text.literal(" $formatted uses").formatted(color))
        }
    }

    private fun appendArmorTooltip(stack: ItemStack, player: PlayerEntity, lines: MutableList<Text>) {
        val uses = ETAUtils.calculateArmorUses(stack, player)
        if (uses > 0) {
            lines.add(Text.literal(""))
            lines.add(Text.translatable("tooltip.tooltipeta.estimated_uses").formatted(Formatting.GRAY))

            val formatted = ETAUtils.formatNumber(uses)
            val color = ETAUtils.getEstimateColor(uses, stack)
            lines.add(Text.literal(" $formatted uses").formatted(color))
        }
    }
}