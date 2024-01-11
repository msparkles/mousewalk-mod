package lgbt.mouse

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils
import net.fabricmc.api.ModInitializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

const val MOD_ID = "mousewalk"

object Mousewalk : ModInitializer {
    private val logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        PolymerResourcePackUtils.addModAssets(MOD_ID)
        PolymerResourcePackUtils.markAsRequired()

        Registry.register(Registries.ITEM, Identifier(MOD_ID, "builders_wand"), MousewalkItems.BUILDERS_WAND)
    }
}