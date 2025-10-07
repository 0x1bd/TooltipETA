package org.kvxd.tooltipeta

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Formatting

object ETAUtils {

    private fun getEnchantmentLevel(
        player: PlayerEntity,
        enchantment: RegistryKey<Enchantment>,
        stack: ItemStack
    ): Int {
        return EnchantmentHelper.getLevel(
            player.world.registryManager
                .getOrThrow(RegistryKeys.ENCHANTMENT)
                .getEntry(enchantment.value).orElseThrow(),
            stack
        )
    }

    fun calculateElytraTime(stack: ItemStack, player: PlayerEntity): Int {
        val durability = stack.maxDamage - stack.damage
        if (durability <= 0) return 0

        val unbreakingLevel = getEnchantmentLevel(player, Enchantments.UNBREAKING, stack)
        val averageMultiplier = unbreakingLevel + 1

        val effectiveDurability = durability * averageMultiplier
        return effectiveDurability
    }

    fun calculateToolUses(stack: ItemStack, player: PlayerEntity): Int {
        val durability = stack.maxDamage - stack.damage
        if (durability <= 0) return 0

        val unbreakingLevel = getEnchantmentLevel(player, Enchantments.UNBREAKING, stack)
        val averageMultiplier = unbreakingLevel + 1

        return durability * averageMultiplier
    }

    fun calculateArmorUses(stack: ItemStack, player: PlayerEntity): Int {
        val durability = stack.maxDamage - stack.damage
        if (durability <= 0) return 0

        val unbreakingLevel = getEnchantmentLevel(player, Enchantments.UNBREAKING, stack)
        val durabilityChance = 0.6 + (0.4 / (unbreakingLevel + 1))
        val averageMultiplier = 1.0 / durabilityChance

        return (durability * averageMultiplier).toInt()
    }

    fun getEstimateColor(uses: Int, stack: ItemStack): Formatting {
        val percentage = if (stack.maxDamage > 0) {
            (stack.maxDamage - stack.damage).toDouble() / stack.maxDamage
        } else {
            if (uses > 1000) 1.0 else if (uses > 500) 0.5 else 0.2
        }

        return when {
            percentage > 0.66 -> Formatting.GREEN
            percentage > 0.33 -> Formatting.YELLOW
            else -> Formatting.RED
        }
    }

    fun formatNumber(num: Int): String {
        return when {
            num >= 1_000_000 -> "%.1fM".format(num / 1_000_000.0)
            num >= 10_000 -> "%.1fK".format(num / 1_000.0)
            num >= 1_000 -> "%,d".format(num)
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
}