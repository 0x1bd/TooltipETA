package org.kvxd.tooltipeta

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class TooltipETAConfig {
    var general: General = General()

    var features: Features = Features()

    var display: Display = Display()

    var thresholds: Thresholds = Thresholds()

    var whitelist: Whitelist = Whitelist()

    enum class OutputMode {
        DETAILED,
        COMPACT
    }

    class General {
        var enabled: Boolean = true

        var outputMode: OutputMode = OutputMode.DETAILED
    }

    class Features {
        var showTools: Boolean = true
        var showArmor: Boolean = true
        var showElytra: Boolean = true
        var showLoyaltyTridentReturnEta: Boolean = true
    }

    class Display {
        var showDurabilityLine: Boolean = true
        var showPercentLine: Boolean = true
        var showBlankSeparator: Boolean = true
        var compactNumberFormatting: Boolean = true
    }

    class Thresholds {
        var warningThresholdPercent: Double = 0.33
        var criticalThresholdPercent: Double = 0.10
    }

    class Whitelist {
        var enabled: Boolean = false
        var itemIds: MutableList<Item> = defaultWhitelistItems().toMutableList()
    }

    companion object {
        fun defaultWhitelistItems(): List<Item> {
            return listOf(
                Items.IRON_SWORD,
                Items.IRON_PICKAXE,
                Items.IRON_AXE,
                Items.IRON_SHOVEL,
                Items.IRON_HOE,
                Items.DIAMOND_SWORD,
                Items.DIAMOND_PICKAXE,
                Items.DIAMOND_AXE,
                Items.DIAMOND_SHOVEL,
                Items.DIAMOND_HOE,
                Items.NETHERITE_SWORD,
                Items.NETHERITE_PICKAXE,
                Items.NETHERITE_AXE,
                Items.NETHERITE_SHOVEL,
                Items.NETHERITE_HOE,
                Items.ELYTRA,
                Items.TRIDENT
            )
        }
    }
}

object TooltipETAConfigManager {
    val config: TooltipETAConfig
        get() = loadedConfig

    private var loadedConfig = TooltipETAConfig()

    private val configPath: Path = FabricLoader.getInstance()
        .configDir
        .resolve("${TooltipETAClient.ID}.json")

    private val gson = GsonBuilder()
        .registerTypeHierarchyAdapter(Item::class.java, object : TypeAdapter<Item>() {
            override fun write(out: JsonWriter, value: Item?) {
                if (value == null || value == Items.AIR) {
                    out.nullValue()
                    return
                }

                out.value(BuiltInRegistries.ITEM.getKey(value).toString())
            }

            override fun read(`in`: JsonReader): Item {
                if (`in`.peek() == JsonToken.NULL) {
                    `in`.nextNull()
                    return Items.AIR
                }

                val id = Identifier.tryParse(`in`.nextString().trim()) ?: return Items.AIR
                return BuiltInRegistries.ITEM.get(id).map { it.value() }.orElse(Items.AIR)
            }
        })
        .setPrettyPrinting()
        .create()

    fun load() {
        loadedConfig = readConfig()
        normalizeAndSave()
    }

    fun save() {
        normalize()
        try {
            Files.createDirectories(configPath.parent)
            Files.newBufferedWriter(configPath).use { writer ->
                gson.toJson(loadedConfig, writer)
            }
        } catch (exception: IOException) {
            TooltipETAClient.LOGGER.error("Failed to save TooltipETA config", exception)
        }
    }

    private fun readConfig(): TooltipETAConfig {
        if (!Files.isRegularFile(configPath)) {
            return TooltipETAConfig()
        }

        return try {
            Files.newBufferedReader(configPath).use { reader ->
                val json = JsonParser.parseReader(reader).asJsonObject
                val config = gson.fromJson(json, TooltipETAConfig::class.java) ?: TooltipETAConfig()
                applyFlatConfigMigration(json, config)
                config
            }
        } catch (exception: JsonSyntaxException) {
            TooltipETAClient.LOGGER.warn("Failed to parse TooltipETA config; using defaults", exception)
            TooltipETAConfig()
        } catch (exception: IllegalStateException) {
            TooltipETAClient.LOGGER.warn("Failed to parse TooltipETA config; using defaults", exception)
            TooltipETAConfig()
        } catch (exception: IOException) {
            TooltipETAClient.LOGGER.warn("Failed to read TooltipETA config; using defaults", exception)
            TooltipETAConfig()
        }
    }

    private fun applyFlatConfigMigration(json: JsonObject, config: TooltipETAConfig) {
        json.booleanValue("enabled")?.let { config.general.enabled = it }
        json.stringValue("outputMode")
            ?.let { runCatching { TooltipETAConfig.OutputMode.valueOf(it) }.getOrNull() }
            ?.let { config.general.outputMode = it }

        json.booleanValue("showTools")?.let { config.features.showTools = it }
        json.booleanValue("showArmor")?.let { config.features.showArmor = it }
        json.booleanValue("showElytra")?.let { config.features.showElytra = it }
        json.booleanValue("showLoyaltyTridentReturnEta")?.let { config.features.showLoyaltyTridentReturnEta = it }

        json.booleanValue("showDurabilityLine")?.let { config.display.showDurabilityLine = it }
        json.booleanValue("showPercentLine")?.let { config.display.showPercentLine = it }
        json.booleanValue("showBlankSeparator")?.let { config.display.showBlankSeparator = it }
        json.booleanValue("compactNumberFormatting")?.let { config.display.compactNumberFormatting = it }

        json.doubleValue("warningThresholdPercent")?.let { config.thresholds.warningThresholdPercent = it }
        json.doubleValue("criticalThresholdPercent")?.let { config.thresholds.criticalThresholdPercent = it }
    }

    private fun normalizeAndSave() {
        normalize()
        save()
    }

    private fun normalize() {
        val cfg = loadedConfig
        cfg.thresholds.warningThresholdPercent = cfg.thresholds.warningThresholdPercent.coerceIn(0.01, 0.95)
        cfg.thresholds.criticalThresholdPercent = cfg.thresholds.criticalThresholdPercent.coerceIn(0.0, cfg.thresholds.warningThresholdPercent - 0.01)
        cfg.whitelist.itemIds = cfg.whitelist.itemIds
            .asSequence()
            .filter { it != Items.AIR }
            .distinct()
            .toMutableList()
    }

    private fun JsonObject.booleanValue(name: String): Boolean? {
        return runCatching { get(name)?.takeUnless { it.isJsonNull }?.asBoolean }.getOrNull()
    }

    private fun JsonObject.doubleValue(name: String): Double? {
        return runCatching { get(name)?.takeUnless { it.isJsonNull }?.asDouble }.getOrNull()
    }

    private fun JsonObject.stringValue(name: String): String? {
        return runCatching { get(name)?.takeUnless { it.isJsonNull }?.asString }.getOrNull()
    }
}
