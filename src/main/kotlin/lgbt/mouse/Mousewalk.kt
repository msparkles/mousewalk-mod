package lgbt.mouse

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils
import lgbt.mouse.blocks.COPPER_CABLE_ID
import lgbt.mouse.blocks.CopperCable
import lgbt.mouse.effects.DoubleJumpStatusEffect
import lgbt.mouse.items.BuildersWand
import lgbt.mouse.items.Dolly
import lgbt.mouse.items.Estrogen
import lgbt.mouse.items.IndustrialDrill
import lgbt.mouse.utils.BlockHighlighter
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

const val MOD_ID = "mousewalk"

object Mousewalk : ModInitializer {
    val LOGGER = LoggerFactory.getLogger(MOD_ID)!!

    override fun onInitialize() {
        PolymerResourcePackUtils.markAsRequired()

        BlockHighlighter.register()

        Registry.register(Registries.ITEM, Identifier(MOD_ID, "industrial_drill"), IndustrialDrill)
        Registry.register(Registries.ITEM, Identifier(MOD_ID, "builders_wand"), BuildersWand)
        Registry.register(Registries.ITEM, Identifier(MOD_ID, "estrogen"), Estrogen)
        Registry.register(Registries.ITEM, Identifier(MOD_ID, "dolly"), Dolly)

        Registry.register(Registries.BLOCK, COPPER_CABLE_ID, CopperCable)
        Registry.register(Registries.ITEM, COPPER_CABLE_ID, CopperCable.Item)

        Registry.register(
            Registries.STATUS_EFFECT,
            Identifier(MOD_ID, "double_jump"),
            DoubleJumpStatusEffect
        )

        PolymerItemGroupUtils.registerPolymerItemGroup(
            Identifier(MOD_ID, "main"),
            FabricItemGroup
                .builder()
                .icon { ItemStack(IndustrialDrill) }
                .displayName(Text.translatable("itemGroup.mousewalk.main"))
                .entries { _, entries ->
                    entries.add(BuildersWand)
                    entries.add(Estrogen)
                    entries.add(IndustrialDrill)
                    entries.add(Dolly)
                    entries.add(CopperCable.Item)
                }
                .build()
        )

        DoubleJumpStatusEffect.register()
        BuildersWand.register()
        IndustrialDrill.register()
    }
}