package lgbt.mouse.blocks.transfer

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import team.reborn.energy.api.base.SimpleSidedEnergyContainer

abstract class CableSidedEnergyContainer(
    private val pos: BlockPos,
    private val state: BlockState,
    private val entity: CableEntity,
    private val capacity: Long,
    private val maxExtract: Long,
) :
    SimpleSidedEnergyContainer() {
    val insertSides = mutableSetOf<Direction>()

    override fun getCapacity() = capacity

    override fun getMaxInsert(side: Direction?): Long {
        val dir = side ?: return capacity

        return if ((state.block as Cable<*>).canConnectCable(entity.world!!.getBlockState(pos.offset(dir)).block)) {
            capacity
        } else {
            0L
        }
    }

    override fun getMaxExtract(side: Direction?): Long {
        side?.let {
            if (insertSides.contains(it)) {
                return 0L
            }
        }

        return maxExtract
    }
}