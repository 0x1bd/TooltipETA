package org.kvxd.tooltipeta

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object TooltipHandler {

    fun appendTooltip(stack: ItemStack, lines: MutableList<Component>) {
        val player = Minecraft.getInstance().player ?: return
        val config = TooltipETAConfigManager.config
        if (!config.general.enabled || !stack.isDamageableItem || stack.maxDamage <= 0) return
        if (!isWhitelisted(stack, config)) return

        when {
            stack.item == Items.ELYTRA && config.features.showElytra -> appendElytraTooltip(stack, player, lines, config)
            ETAUtils.isArmor(stack) && config.features.showArmor -> appendArmorTooltip(stack, player, lines, config)
            config.features.showTools -> appendToolTooltip(stack, player, lines, config)
        }
    }

    private fun appendElytraTooltip(
        stack: ItemStack,
        player: Player,
        lines: MutableList<Component>,
        config: TooltipETAConfig
    ) {
        val seconds = ETAUtils.calculateElytraTime(stack, player)
        if (seconds > 0) {
            val remainingRatio = ETAUtils.getRemainingRatio(stack)
            val formattedTime = ETAUtils.formatTime(seconds)
            val color = ETAUtils.getEstimateColor(remainingRatio, config)
            when (config.general.outputMode) {
                TooltipETAConfig.OutputMode.DETAILED -> {
                    appendBaseHeader(lines, config, "tooltip.tooltipeta.flight_time")
                    lines.add(Component.literal(" $formattedTime").withStyle(color))
                    appendExtraInfoLines(stack, lines, config)
                }

                TooltipETAConfig.OutputMode.COMPACT -> {
                    appendBaseHeader(lines, config, "tooltip.tooltipeta.flight_time")
                    lines.add(Component.translatable("tooltip.tooltipeta.compact_time", formattedTime).withStyle(color))
                }
            }
        }
    }

    private fun appendToolTooltip(
        stack: ItemStack,
        player: Player,
        lines: MutableList<Component>,
        config: TooltipETAConfig
    ) {
        val uses = ETAUtils.calculateToolUses(stack, player)
        if (uses > 0) {
            val remainingRatio = ETAUtils.getRemainingRatio(stack)
            val formatted = ETAUtils.formatNumber(uses, config.display.compactNumberFormatting)
            val color = ETAUtils.getEstimateColor(remainingRatio, config)
            when (config.general.outputMode) {
                TooltipETAConfig.OutputMode.DETAILED -> {
                    appendBaseHeader(lines, config, "tooltip.tooltipeta.estimated_uses")
                    lines.add(Component.translatable("tooltip.tooltipeta.value_uses", formatted).withStyle(color))
                    appendExtraInfoLines(stack, lines, config)
                }

                TooltipETAConfig.OutputMode.COMPACT -> {
                    appendBaseHeader(lines, config, "tooltip.tooltipeta.estimated_uses")
                    lines.add(Component.translatable("tooltip.tooltipeta.compact_uses", formatted).withStyle(color))
                }
            }
        }
    }

    private fun appendArmorTooltip(
        stack: ItemStack,
        player: Player,
        lines: MutableList<Component>,
        config: TooltipETAConfig
    ) {
        val uses = ETAUtils.calculateArmorUses(stack, player)
        if (uses > 0) {
            val remainingRatio = ETAUtils.getRemainingRatio(stack)
            val formatted = ETAUtils.formatNumber(uses, config.display.compactNumberFormatting)
            val color = ETAUtils.getEstimateColor(remainingRatio, config)
            when (config.general.outputMode) {
                TooltipETAConfig.OutputMode.DETAILED -> {
                    appendBaseHeader(lines, config, "tooltip.tooltipeta.estimated_uses")
                    lines.add(Component.translatable("tooltip.tooltipeta.value_uses", formatted).withStyle(color))
                    appendExtraInfoLines(stack, lines, config)
                }

                TooltipETAConfig.OutputMode.COMPACT -> {
                    appendBaseHeader(lines, config, "tooltip.tooltipeta.estimated_uses")
                    lines.add(Component.translatable("tooltip.tooltipeta.compact_uses", formatted).withStyle(color))
                }
            }
        }
    }

    private fun appendBaseHeader(lines: MutableList<Component>, config: TooltipETAConfig, key: String) {
        if (config.display.showBlankSeparator) {
            lines.add(Component.literal(""))
        }

        if (config.general.outputMode == TooltipETAConfig.OutputMode.DETAILED) {
            lines.add(Component.translatable(key).withStyle(ChatFormatting.GRAY))
        }
    }

    private fun appendExtraInfoLines(stack: ItemStack, lines: MutableList<Component>, config: TooltipETAConfig) {
        val remainingDurability = ETAUtils.getRemainingDurability(stack)

        if (config.display.showDurabilityLine) {
            lines.add(
                Component.translatable(
                    "tooltip.tooltipeta.remaining_durability",
                    ETAUtils.formatNumber(remainingDurability, false),
                    ETAUtils.formatNumber(stack.maxDamage, false)
                ).withStyle(ChatFormatting.DARK_GRAY)
            )
        }

        if (config.display.showPercentLine) {
            lines.add(
                Component.translatable(
                    "tooltip.tooltipeta.remaining_percent",
                    ETAUtils.formatPercent(ETAUtils.getRemainingRatio(stack))
                ).withStyle(ChatFormatting.DARK_GRAY)
            )
        }
    }

    private fun isWhitelisted(stack: ItemStack, config: TooltipETAConfig): Boolean {
        if (!config.whitelist.enabled) return true
        return config.whitelist.itemIds.contains(stack.item)
    }
}