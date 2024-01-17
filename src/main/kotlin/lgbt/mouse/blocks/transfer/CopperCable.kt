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
            override val sidedEnergy = object : CableSidedEnergyContainer(pos, state, this, 3000L, 300L) {
                override fun onFinalCommit() {
                    markDirty()
                    super.onFinalCommit()
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

    override fun isSameClass(other: Block): Boolean {
        return other is CopperCable
    }
}