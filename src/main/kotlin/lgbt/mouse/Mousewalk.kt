package lgbt.mouse

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils
import lgbt.mouse.effects.DoubleJumpStatusEffect
import lgbt.mouse.items.BuildersWand
import lgbt.mouse.items.Estrogen
import lgbt.mouse.items.IndustrialDrill
import lgbt.mouse.utils.BlockHighlighter
import net.fabricmc.api.ModInitializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

const val MOD_ID = "mousewalk"

object Mousewalk : ModInitializer {
    val LOGGER = LoggerFactory.getLogger(MOD_ID)!!

    override fun onInitialize() {
        PolymerResourcePackUtils.addModAssets(MOD_ID)
        PolymerResourcePackUtils.markAsRequired()

        BlockHighlighter.register()

        Registry.register(Registries.ITEM, Identifier(MOD_ID, "builders_wand"), BuildersWand)
        Registry.register(Registries.ITEM, Identifier(MOD_ID, "estrogen"), Estrogen)
        Registry.register(Registries.ITEM, Identifier(MOD_ID, "industrial_drill"), IndustrialDrill)
        Registry.register(
            Registries.STATUS_EFFECT,
            Identifier(MOD_ID, "double_jump"),
            DoubleJumpStatusEffect
        )

        DoubleJumpStatusEffect.register()
        BuildersWand.register()
        IndustrialDrill.register()
    }
}