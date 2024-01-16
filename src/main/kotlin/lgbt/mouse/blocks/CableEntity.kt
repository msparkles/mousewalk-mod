package lgbt.mouse.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import team.reborn.energy.api.EnergyStorage

abstract class CableEntity<T : Cable<T>>(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(type, pos, state) {
    abstract val energyStorage: EnergyStorage
}