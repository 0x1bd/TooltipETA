package org.kvxd.tooltipeta

import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import java.util.Locale

object ETAUtils {

    private fun getEnchantmentLevel(
        player: Player,
        enchantment: ResourceKey<Enchantment>,
        stack: ItemStack
    ): Int {
        val enchantments = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
        return EnchantmentHelper.getItemEnchantmentLevel(
            enchantments.getOrThrow(enchantment),
            stack
        )
    }

    fun calculateElytraTime(stack: ItemStack, player: Player): Int {
        val durability = stack.maxDamage - stack.damageValue
        if (durability <= 0) return 0

        val unbreakingLevel = getEnchantmentLevel(player, Enchantments.UNBREAKING, stack)
        val averageMultiplier = unbreakingLevel + 1

        val effectiveDurability = durability * averageMultiplier
        return effectiveDurability
    }

    fun calculateToolUses(stack: ItemStack, player: Player): Int {
        val durability = stack.maxDamage - stack.damageValue
        if (durability <= 0) return 0

        val unbreakingLevel = getEnchantmentLevel(player, Enchantments.UNBREAKING, stack)
        val averageMultiplier = unbreakingLevel + 1

        return durability * averageMultiplier
    }

    fun calculateArmorUses(stack: ItemStack, player: Player): Int {
        val durability = stack.maxDamage - stack.damageValue
        if (durability <= 0) return 0

        val unbreakingLevel = getEnchantmentLevel(player, Enchantments.UNBREAKING, stack)
        val durabilityChance = 0.6 + (0.4 / (unbreakingLevel + 1))
        val averageMultiplier = 1.0 / durabilityChance

        return (durability * averageMultiplier).toInt()
    }

    fun getEstimateColor(remainingRatio: Double, config: TooltipETAConfig): ChatFormatting {
        return when {
            remainingRatio <= config.thresholds.criticalThresholdPercent -> ChatFormatting.RED
            remainingRatio <= config.thresholds.warningThresholdPercent -> ChatFormatting.YELLOW
            else -> ChatFormatting.GREEN
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

        return armorTags.any { tag -> stack.`is`(tag) }
    }

    fun getRemainingDurability(stack: ItemStack): Int {
        return (stack.maxDamage - stack.damageValue).coerceAtLeast(0)
    }

    fun getRemainingRatio(stack: ItemStack): Double {
        if (stack.maxDamage <= 0) return 0.0
        return getRemainingDurability(stack).toDouble() / stack.maxDamage
    }

    fun formatPercent(ratio: Double): String {
        return String.format(Locale.ROOT, "%.1f%%", ratio.coerceIn(0.0, 1.0) * 100.0)
    }
}