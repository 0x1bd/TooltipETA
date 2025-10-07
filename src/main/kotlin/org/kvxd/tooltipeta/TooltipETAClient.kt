package org.kvxd.tooltipeta

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import org.slf4j.LoggerFactory

class TooltipETAClient : ClientModInitializer {

    override fun onInitializeClient() {
        ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
            TooltipHandler.appendTooltip(stack, lines)
        }
    }

    companion object {

        const val ID = "tooltipeta"

        val LOGGER = LoggerFactory.getLogger(ID)
    }
}