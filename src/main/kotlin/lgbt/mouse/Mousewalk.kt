package lgbt.mouse

import eu.pb4.polymer.core.api.block.PolymerBlockUtils
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils
import lgbt.mouse.blocks.ElectricFurnace
import lgbt.mouse.blocks.ElectricFurnace.ELECTRIC_FURNACE_NAME
import lgbt.mouse.blocks.FuelGenerator
import lgbt.mouse.blocks.FuelGenerator.FUEL_GENERATOR_NAME
import lgbt.mouse.blocks.transfer.CopperCable
import lgbt.mouse.blocks.transfer.CopperCable.Companion.COPPER_CABLE_NAME
import lgbt.mouse.effects.DoubleJumpStatusEffect
import lgbt.mouse.items.BuildersWand
import lgbt.mouse.items.Dolly
import lgbt.mouse.items.Estrogen
import lgbt.mouse.items.IndustrialDrill
import lgbt.mouse.utils.BlockHighlighter
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
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

    val COPPER_CABLE_ENTITY_TYPE =
        Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(MOD_ID, "${COPPER_CABLE_NAME}_entity"),
            FabricBlockEntityTypeBuilder.create(
                { pos, state -> CopperCable.Companion.CopperCableEntity(pos, state) },
                *CopperCable.ALL_COLORS.values.map { it.first }.toTypedArray()
            ).build()
        )!!

    val FUEL_GENERATOR_ENTITY_TYPE =
        Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(MOD_ID, "${FUEL_GENERATOR_NAME}_entity"),
            FabricBlockEntityTypeBuilder.create(
                { pos, state -> FuelGenerator.FuelGeneratorEntity(pos, state) },
                FuelGenerator
            ).build()
        )!!

    val ELECTRIC_FURNACE_ENTITY_TYPE =
        Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(MOD_ID, "${ELECTRIC_FURNACE_NAME}_entity"),
            FabricBlockEntityTypeBuilder.create(
                { pos, state -> ElectricFurnace.ElectricFurnaceEntity(pos, state) },
                ElectricFurnace
            ).build()
        )!!

    override fun onInitialize() {
        PolymerResourcePackUtils.markAsRequired()
        PolymerResourcePackUtils.addModAssets(MOD_ID)

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

        Registry.register(Registries.BLOCK, Identifier(MOD_ID, FUEL_GENERATOR_NAME), FuelGenerator)
        Registry.register(Registries.ITEM, Identifier(MOD_ID, FUEL_GENERATOR_NAME), FuelGenerator.ITEM)

        Registry.register(Registries.BLOCK, Identifier(MOD_ID, ELECTRIC_FURNACE_NAME), ElectricFurnace)
        Registry.register(Registries.ITEM, Identifier(MOD_ID, ELECTRIC_FURNACE_NAME), ElectricFurnace.ITEM)

        Registry.register(
            Registries.STATUS_EFFECT,
            Identifier(MOD_ID, "double_jump"),
            DoubleJumpStatusEffect
        )

        PolymerItemGroupUtils.registerPolymerItemGroup(
            Identifier(MOD_ID, "main"),
            FabricItemGroup
                .builder()
                .icon { FuelGenerator.ITEM.defaultStack }
                .displayName(Text.translatable("itemGroup.mousewalk.main"))
                .entries { _, entries ->
                    entries.add(BuildersWand)
                    entries.add(Estrogen)
                    entries.add(IndustrialDrill)
                    entries.add(Dolly)
                    entries.add(CopperCable.ITEM)
                    entries.add(FuelGenerator.ITEM)
                    entries.add(ElectricFurnace.ITEM)
                }
                .build()
        )

        PolymerBlockUtils.registerBlockEntity(
            COPPER_CABLE_ENTITY_TYPE,
            FUEL_GENERATOR_ENTITY_TYPE,
            ELECTRIC_FURNACE_ENTITY_TYPE
        )

        EnergyStorage.SIDED.registerForBlockEntity(
            { entity: CopperCable.Companion.CopperCableEntity, direction: Direction? ->
                entity.sidedEnergy.getSideStorage(direction)
            },
            COPPER_CABLE_ENTITY_TYPE
        )

        EnergyStorage.SIDED.registerForBlockEntity(
            { entity: FuelGenerator.FuelGeneratorEntity, _: Direction? -> entity.energyStorage },
            FUEL_GENERATOR_ENTITY_TYPE
        )
        ItemStorage.SIDED.registerForBlockEntity(
            { entity: FuelGenerator.FuelGeneratorEntity, _: Direction? -> entity.fuelStorage },
            FUEL_GENERATOR_ENTITY_TYPE,
        )

        EnergyStorage.SIDED.registerForBlockEntity(
            { entity: ElectricFurnace.ElectricFurnaceEntity, _: Direction? -> entity.energyStorage },
            ELECTRIC_FURNACE_ENTITY_TYPE
        )
        ItemStorage.SIDED.registerForBlockEntity(
            { entity: ElectricFurnace.ElectricFurnaceEntity, _: Direction? -> entity.storage },
            ELECTRIC_FURNACE_ENTITY_TYPE,
        )

        DoubleJumpStatusEffect.register()
        BuildersWand.register()
        IndustrialDrill.register()
    }
}