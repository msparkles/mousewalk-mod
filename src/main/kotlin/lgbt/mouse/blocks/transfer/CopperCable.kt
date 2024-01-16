package lgbt.mouse.blocks.transfer

import eu.pb4.polymer.core.api.item.PolymerBlockItem
import lgbt.mouse.Mousewalk
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.Items
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import team.reborn.energy.api.base.SimpleSidedEnergyContainer


class CopperCable(
    color: DyeColor?,
) : Cable<CopperCable>(
    color,
    COPPER_CABLE_NAME,
    FabricBlockSettings.create()
        .sounds(BlockSoundGroup.COPPER)
        .strength(0.2f),
) {
    companion object {
        class CopperCableEntity(pos: BlockPos, state: BlockState) : CableEntity(
            Mousewalk.COPPER_CABLE_ENTITY_TYPE, pos, state
        ) {
            override val sidedEnergy = object : SimpleSidedEnergyContainer() {
                override fun onFinalCommit() {
                    markDirty()
                }

                override fun getCapacity() = 2000L

                override fun getMaxInsert(side: Direction?): Long {
                    return side?.let {
                        if ((state.block as Cable<*>).canConnectCable(
                                this@CopperCableEntity.world!!.getBlockState(pos.add(it.vector)).block
                            )
                        ) {
                            300L
                        } else {
                            0L
                        }
                    } ?: 300L
                }

                override fun getMaxExtract(side: Direction?): Long {
                    return 300L
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

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = CopperCableEntity(pos, state)

    override fun isSameBlock(other: Block): Boolean {
        return other is CopperCable
    }
}