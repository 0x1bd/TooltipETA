package org.kvxd.tooltipeta

import net.minecraft.util.Formatting
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.tag.ItemTags
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import java.util.Locale

object ETAUtils {

    private fun getEnchantmentLevel(
        player: PlayerEntity,
        enchantment: RegistryKey<Enchantment>,
        stack: ItemStack
    ): Int {
        val enchantments = player.entityWorld.registryManager.getOrThrow(RegistryKeys.ENCHANTMENT)
        return EnchantmentHelper.getLevel(
            enchantments.getOrThrow(enchantment),
            stack
        )
    }

    fun calculateElytraTime(stack: ItemStack, player: PlayerEntity): Int {
        val durability = stack.getMaxDamage() - stack.getDamage()
        if (durability <= 0) return 0

        val unbreakingLevel = getEnchantmentLevel(player, Enchantments.UNBREAKING, stack)
        val averageMultiplier = unbreakingLevel + 1

        val effectiveDurability = durability * averageMultiplier
        return effectiveDurability
    }

    fun calculateToolUses(stack: ItemStack, player: PlayerEntity): Int {
        val durability = stack.getMaxDamage() - stack.getDamage()
        if (durability <= 0) return 0

        val unbreakingLevel = getEnchantmentLevel(player, Enchantments.UNBREAKING, stack)
        val averageMultiplier = unbreakingLevel + 1

        return durability * averageMultiplier
    }

    fun calculateArmorUses(stack: ItemStack, player: PlayerEntity): Int {
        val durability = stack.getMaxDamage() - stack.getDamage()
        if (durability <= 0) return 0

        val unbreakingLevel = getEnchantmentLevel(player, Enchantments.UNBREAKING, stack)
        val durabilityChance = 0.6 + (0.4 / (unbreakingLevel + 1))
        val averageMultiplier = 1.0 / durabilityChance

        return (durability * averageMultiplier).toInt()
    }

    fun getEstimateColor(remainingRatio: Double, config: TooltipETAConfig): Formatting {
        return when {
            remainingRatio <= config.thresholds.criticalThresholdPercent -> Formatting.RED
            remainingRatio <= config.thresholds.warningThresholdPercent -> Formatting.YELLOW
            else -> Formatting.GREEN
        }
    }

    fun formatNumber(num: Int, compact: Boolean): String {
        if (!compact) {
            return String.format(Locale.ROOT, "%,d", num)
        }

        return when {
            num >= 1_000_000 -> String.format(Locale.ROOT, "%.1fM", num / 1_000_000.0)
            num >= 10_000 -> String.format(Locale.ROOT, "%.1fK", num / 1_000.0)
            num >= 1_000 -> String.format(Locale.ROOT, "%,d", num)
            else -> num.toString()
        }
    }

    fun formatTime(seconds: Int): String {
        return when {
            seconds >= 3600 -> {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                "${hours}h ${minutes}m"
            }

            seconds >= 60 -> {
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                "${minutes}m ${remainingSeconds}s"
            }

            else -> "${seconds}s"
        }
    }

    fun isArmor(stack: ItemStack): Boolean {
        val armorTags = listOf(
            ItemTags.HEAD_ARMOR,
            ItemTags.CHEST_ARMOR,
            ItemTags.LEG_ARMOR,
            ItemTags.FOOT_ARMOR
        )

        return armorTags.any { tag -> stack.isIn(tag) }
    }

    fun getRemainingDurability(stack: ItemStack): Int {
        return (stack.getMaxDamage() - stack.getDamage()).coerceAtLeast(0)
    }

    fun getRemainingRatio(stack: ItemStack): Double {
        if (stack.getMaxDamage() <= 0) return 0.0
        return getRemainingDurability(stack).toDouble() / stack.getMaxDamage()
    }

    fun formatPercent(ratio: Double): String {
        return String.format(Locale.ROOT, "%.1f%%", ratio.coerceIn(0.0, 1.0) * 100.0)
    }
}
