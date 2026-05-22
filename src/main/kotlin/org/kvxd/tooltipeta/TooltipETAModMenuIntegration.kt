package org.kvxd.tooltipeta

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.ListOption
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.api.controller.ControllerBuilder
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder
import dev.isxander.yacl3.api.controller.EnumControllerBuilder
import dev.isxander.yacl3.api.controller.ItemControllerBuilder
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.item.Item
import net.minecraft.item.Items
import java.util.Locale

class TooltipETAModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory<Screen> { parent ->
            createConfigScreen(parent)
        }
    }

    private fun createConfigScreen(parent: Screen): Screen {
        val config = TooltipETAConfigManager.config
        val defaults = TooltipETAConfig()

        return YetAnotherConfigLib.createBuilder()
            .title(text("title"))
            .category(settingsCategory(config, defaults))
            .save(TooltipETAConfigManager::save)
            .build()
            .generateScreen(parent)
    }

    private fun settingsCategory(config: TooltipETAConfig, defaults: TooltipETAConfig): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(text("category.settings"))
            .group(generalGroup(config, defaults))
            .group(featuresGroup(config, defaults))
            .group(displayGroup(config, defaults))
            .group(thresholdsGroup(config, defaults))
            .group(whitelistToggleGroup(config, defaults))
            .group(whitelistItemsGroup(config, defaults))
            .build()
    }

    private fun generalGroup(config: TooltipETAConfig, defaults: TooltipETAConfig): OptionGroup {
        return OptionGroup.createBuilder()
            .name(text("group.general"))
            .option(
                booleanOption(
                    "option.general.enabled",
                    defaults.general.enabled,
                    { config.general.enabled },
                    { config.general.enabled = it }
                )
            )
            .option(
                option(
                    "option.general.outputMode",
                    defaults.general.outputMode,
                    { config.general.outputMode },
                    { config.general.outputMode = it }
                ) { option ->
                    EnumControllerBuilder.create(option)
                        .enumClass(TooltipETAConfig.OutputMode::class.java)
                        .formatValue { value ->
                            text("option.general.outputMode.${value.name.lowercase(Locale.ROOT)}")
                        }
                }
            )
            .build()
    }

    private fun featuresGroup(config: TooltipETAConfig, defaults: TooltipETAConfig): OptionGroup {
        return OptionGroup.createBuilder()
            .name(text("group.features"))
            .option(
                booleanOption(
                    "option.features.showTools",
                    defaults.features.showTools,
                    { config.features.showTools },
                    { config.features.showTools = it }
                )
            )
            .option(
                booleanOption(
                    "option.features.showArmor",
                    defaults.features.showArmor,
                    { config.features.showArmor },
                    { config.features.showArmor = it }
                )
            )
            .option(
                booleanOption(
                    "option.features.showElytra",
                    defaults.features.showElytra,
                    { config.features.showElytra },
                    { config.features.showElytra = it }
                )
            )
            .option(
                booleanOption(
                    "option.features.showLoyaltyTridentReturnEta",
                    defaults.features.showLoyaltyTridentReturnEta,
                    { config.features.showLoyaltyTridentReturnEta },
                    { config.features.showLoyaltyTridentReturnEta = it }
                )
            )
            .build()
    }

    private fun displayGroup(config: TooltipETAConfig, defaults: TooltipETAConfig): OptionGroup {
        return OptionGroup.createBuilder()
            .name(text("group.display"))
            .option(
                booleanOption(
                    "option.display.showDurabilityLine",
                    defaults.display.showDurabilityLine,
                    { config.display.showDurabilityLine },
                    { config.display.showDurabilityLine = it }
                )
            )
            .option(
                booleanOption(
                    "option.display.showPercentLine",
                    defaults.display.showPercentLine,
                    { config.display.showPercentLine },
                    { config.display.showPercentLine = it }
                )
            )
            .option(
                booleanOption(
                    "option.display.showBlankSeparator",
                    defaults.display.showBlankSeparator,
                    { config.display.showBlankSeparator },
                    { config.display.showBlankSeparator = it }
                )
            )
            .option(
                booleanOption(
                    "option.display.compactNumberFormatting",
                    defaults.display.compactNumberFormatting,
                    { config.display.compactNumberFormatting },
                    { config.display.compactNumberFormatting = it }
                )
            )
            .build()
    }

    private fun thresholdsGroup(config: TooltipETAConfig, defaults: TooltipETAConfig): OptionGroup {
        return OptionGroup.createBuilder()
            .name(text("group.thresholds"))
            .option(
                percentOption(
                    "option.thresholds.warningThresholdPercent",
                    defaults.thresholds.warningThresholdPercent,
                    0.01,
                    0.95,
                    { config.thresholds.warningThresholdPercent },
                    { config.thresholds.warningThresholdPercent = it }
                )
            )
            .option(
                percentOption(
                    "option.thresholds.criticalThresholdPercent",
                    defaults.thresholds.criticalThresholdPercent,
                    0.0,
                    0.94,
                    { config.thresholds.criticalThresholdPercent },
                    { config.thresholds.criticalThresholdPercent = it }
                )
            )
            .build()
    }

    private fun whitelistToggleGroup(config: TooltipETAConfig, defaults: TooltipETAConfig): OptionGroup {
        return OptionGroup.createBuilder()
            .name(text("group.whitelist"))
            .option(
                booleanOption(
                    "option.whitelist.enabled",
                    defaults.whitelist.enabled,
                    { config.whitelist.enabled },
                    { config.whitelist.enabled = it }
                )
            )
            .build()
    }

    private fun whitelistItemsGroup(config: TooltipETAConfig, defaults: TooltipETAConfig): ListOption<Item> {
        return ListOption.createBuilder<Item>()
            .name(text("option.whitelist.itemIds"))
            .description(description("option.whitelist.itemIds"))
            .binding(
                defaults.whitelist.itemIds,
                { config.whitelist.itemIds.toList() },
                { config.whitelist.itemIds = it.toMutableList() }
            )
            .initial(Items.TRIDENT)
            .controller { option -> ItemControllerBuilder.create(option) }
            .collapsed(false)
            .build()
    }

    private fun booleanOption(
        translationPath: String,
        defaultValue: Boolean,
        getter: () -> Boolean,
        setter: (Boolean) -> Unit
    ): Option<Boolean> {
        return option(translationPath, defaultValue, getter, setter) { option ->
            BooleanControllerBuilder.create(option).onOffFormatter()
        }
    }

    private fun percentOption(
        translationPath: String,
        defaultValue: Double,
        minimum: Double,
        maximum: Double,
        getter: () -> Double,
        setter: (Double) -> Unit
    ): Option<Double> {
        return option(translationPath, defaultValue, getter, setter) { option ->
            DoubleSliderControllerBuilder.create(option)
                .range(minimum, maximum)
                .step(0.01)
                .formatValue { value -> Text.literal(ETAUtils.formatPercent(value)) }
        }
    }

    private fun <T : Any> option(
        translationPath: String,
        defaultValue: T,
        getter: () -> T,
        setter: (T) -> Unit,
        controller: (Option<T>) -> ControllerBuilder<T>
    ): Option<T> {
        return Option.createBuilder<T>()
            .name(text(translationPath))
            .description(description(translationPath))
            .binding(defaultValue, getter, setter)
            .controller(controller)
            .build()
    }

    private fun text(path: String): Text {
        return Text.translatable("config.${TooltipETAClient.ID}.$path")
    }

    private fun description(path: String): OptionDescription {
        return OptionDescription.of(text("$path.tooltip"))
    }
}
