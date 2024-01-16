package lgbt.mouse.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos

abstract class EntityWithEnergyGeneration(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
) : EntityWithEnergy(type, pos, state) {
}