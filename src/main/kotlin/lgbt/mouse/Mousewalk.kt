package lgbt.mouse

import eu.pb4.polymer.core.api.block.PolymerBlockUtils
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils
import lgbt.mouse.blocks.CopperCable
import lgbt.mouse.blocks.CopperCable.Companion.COPPER_CABLE_NAME
import lgbt.mouse.effects.DoubleJumpStatusEffect
import lgbt.mouse.items.BuildersWand
import lgbt.mouse.items.Dolly
import lgbt.mouse.items.Estrogen
import lgbt.mouse.items.IndustrialDrill
import lgbt.mouse.utils.BlockHighlighter
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import org.slf4j.LoggerFactory
import team.reborn.energy.api.EnergyStorage


const val MOD_ID = "mousewalk"

object Mousewalk : ModInitializer {
    val LOGGER = LoggerFactory.getLogger(MOD_ID)!!

    val COPPER_CABLE_ENTITY_TYPE: BlockEntityType<CopperCable.Companion.CopperCableEntity> =
        Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(MOD_ID, "${COPPER_CABLE_NAME}_entity"),
            FabricBlockEntityTypeBuilder.create(
                { pos, state -> CopperCable.Companion.CopperCableEntity(pos, state) },
                *CopperCable.ALL_COLORS.values.map { it.first }.toTypedArray()
            ).build()
        )

    override fun onInitialize() {
        PolymerResourcePackUtils.markAsRequired()

        BlockHighlighter.register()

        Registry.register(Registries.ITEM, Identifier(MOD_ID, "industrial_drill"), IndustrialDrill)
        Registry.register(Registries.ITEM, Identifier(MOD_ID, "builders_wand"), BuildersWand)
        Registry.register(Registries.ITEM, Identifier(MOD_ID, "estrogen"), Estrogen)
        Registry.register(Registries.ITEM, Identifier(MOD_ID, "dolly"), Dolly)

        CopperCable.ALL_COLORS.forEach { (_, v) ->
            val (cable, name) = v

            Registry.register(Registries.BLOCK, Identifier(MOD_ID, name), cable)
        }

        Registry.register(Registries.ITEM, Identifier(MOD_ID, COPPER_CABLE_NAME), CopperCable.ITEM)

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
                    entries.add(CopperCable.ITEM)
                }
                .build()
        )

        PolymerBlockUtils.registerBlockEntity(COPPER_CABLE_ENTITY_TYPE)

        EnergyStorage.SIDED.registerForBlockEntity(
            { entity: CopperCable.Companion.CopperCableEntity, direction: Direction? ->
                entity.world?.let { world ->
                    direction?.let { direction ->
                        if (
                            (entity.cachedState.block as CopperCable).getConnected(
                                entity.pos,
                                entity.cachedState,
                                world
                            ).any { direction.unitVector == it.subtract(entity.pos) }
                        ) {
                            entity.energyStorage
                        } else {
                            null
                        }
                    }
                }
            },
            COPPER_CABLE_ENTITY_TYPE
        )

        DoubleJumpStatusEffect.register()
        BuildersWand.register()
        IndustrialDrill.register()
    }
}