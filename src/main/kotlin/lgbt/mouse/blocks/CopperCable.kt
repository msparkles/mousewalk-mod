package lgbt.mouse.blocks

import eu.pb4.polymer.core.api.item.PolymerBlockItem
import lgbt.mouse.Mousewalk
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.Items
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.base.SimpleEnergyStorage


class CopperCable(
    color: DyeColor?,
) : Cable<CopperCable>(
    color,
    COPPER_CABLE_NAME,
    FabricBlockSettings.create()
        .sounds(BlockSoundGroup.COPPER)
        .strength(0.2f),
), BlockEntityProvider {
    companion object {
        class CopperCableEntity(pos: BlockPos, state: BlockState) : CableEntity<CopperCable>(
            Mousewalk.COPPER_CABLE_ENTITY_TYPE, pos, state
        ) {
            override val energyStorage: EnergyStorage = object : SimpleEnergyStorage(12000, 300, 300) {
                override fun onFinalCommit() {
                    markDirty()
                }
            }
        }

        const val COPPER_CABLE_NAME = "copper_cable"

        val COLORLESS = CopperCable(null)
        val ALL_COLORS = makeAllColors(COLORLESS, COPPER_CABLE_NAME) { CopperCable(it) }

        val ITEM = object : PolymerBlockItem(COLORLESS, FabricItemSettings(), Items.LIGHTNING_ROD) {
        }
    }

    override val colorless = lazy { COLORLESS }
    override val allColors = lazy { ALL_COLORS }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return CopperCableEntity(pos, state)
    }

    override fun isSameBlock(other: Block): Boolean {
        return other is CopperCable
    }
}