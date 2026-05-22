package org.kvxd.tooltipeta

import net.minecraft.util.Formatting
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

object TooltipHandler {

    fun appendTooltip(stack: ItemStack, lines: MutableList<Text>) {
        val player = MinecraftClient.getInstance().player ?: return
        val config = TooltipETAConfigManager.config
        if (!config.general.enabled || !stack.isDamageable || stack.getMaxDamage() <= 0) return
        if (!isWhitelisted(stack, config)) return

        when {
            stack.item == Items.ELYTRA && config.features.showElytra -> appendElytraTooltip(stack, player, lines, config)
            ETAUtils.isArmor(stack) && config.features.showArmor -> appendArmorTooltip(stack, player, lines, config)
            config.features.showTools -> appendToolTooltip(stack, player, lines, config)
        }
    }

    private fun appendElytraTooltip(
        stack: ItemStack,
        player: PlayerEntity,
        lines: MutableList<Text>,
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
                    lines.add(Text.literal(" $formattedTime").formatted(color))
                    appendExtraInfoLines(stack, lines, config)
                }

                TooltipETAConfig.OutputMode.COMPACT -> {
                    appendBaseHeader(lines, config, "tooltip.tooltipeta.flight_time")
                    lines.add(Text.translatable("tooltip.tooltipeta.compact_time", formattedTime).formatted(color))
                }
            }
        }
    }

    private fun appendToolTooltip(
        stack: ItemStack,
        player: PlayerEntity,
        lines: MutableList<Text>,
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
                    lines.add(Text.translatable("tooltip.tooltipeta.value_uses", formatted).formatted(color))
                    appendExtraInfoLines(stack, lines, config)
                }

                TooltipETAConfig.OutputMode.COMPACT -> {
                    appendBaseHeader(lines, config, "tooltip.tooltipeta.estimated_uses")
                    lines.add(Text.translatable("tooltip.tooltipeta.compact_uses", formatted).formatted(color))
                }
            }
        }
    }

    private fun appendArmorTooltip(
        stack: ItemStack,
        player: PlayerEntity,
        lines: MutableList<Text>,
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
                    lines.add(Text.translatable("tooltip.tooltipeta.value_uses", formatted).formatted(color))
                    appendExtraInfoLines(stack, lines, config)
                }

                TooltipETAConfig.OutputMode.COMPACT -> {
                    appendBaseHeader(lines, config, "tooltip.tooltipeta.estimated_uses")
                    lines.add(Text.translatable("tooltip.tooltipeta.compact_uses", formatted).formatted(color))
                }
            }
        }
    }

    private fun appendBaseHeader(lines: MutableList<Text>, config: TooltipETAConfig, key: String) {
        if (config.display.showBlankSeparator) {
            lines.add(Text.literal(""))
        }

        if (config.general.outputMode == TooltipETAConfig.OutputMode.DETAILED) {
            lines.add(Text.translatable(key).formatted(Formatting.GRAY))
        }
    }

    private fun appendExtraInfoLines(stack: ItemStack, lines: MutableList<Text>, config: TooltipETAConfig) {
        val remainingDurability = ETAUtils.getRemainingDurability(stack)

        if (config.display.showDurabilityLine) {
            lines.add(
                Text.translatable(
                    "tooltip.tooltipeta.remaining_durability",
                    ETAUtils.formatNumber(remainingDurability, false),
                    ETAUtils.formatNumber(stack.getMaxDamage(), false)
                ).formatted(Formatting.DARK_GRAY)
            )
        }

        if (config.display.showPercentLine) {
            lines.add(
                Text.translatable(
                    "tooltip.tooltipeta.remaining_percent",
                    ETAUtils.formatPercent(ETAUtils.getRemainingRatio(stack))
                ).formatted(Formatting.DARK_GRAY)
            )
        }
    }

    private fun isWhitelisted(stack: ItemStack, config: TooltipETAConfig): Boolean {
        if (!config.whitelist.enabled) return true
        return config.whitelist.itemIds.contains(stack.item)
    }
}
