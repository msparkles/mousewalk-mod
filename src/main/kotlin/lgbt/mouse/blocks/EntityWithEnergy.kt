package lgbt.mouse.blocks

import lgbt.mouse.blocks.transfer.CableEntity
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.base.SimpleEnergyStorage

abstract class EntityWithEnergy(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(type, pos, state) {
    companion object {
        const val ENERGY_KEY = "energy"
    }

    abstract val energyStorage: SimpleEnergyStorage

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putLong(ENERGY_KEY, energyStorage.amount)
        super.writeNbt(nbt)
    }

    override fun readNbt(nbt: NbtCompound) {
        energyStorage.amount = nbt.getLong(ENERGY_KEY)
        super.readNbt(nbt)
    }

    open fun tick(world: World, pos: BlockPos, state: BlockState) {
        if (this.energyStorage.supportsExtraction()) {
            Direction.entries
                .mapNotNull { dir ->
                    EnergyStorage.SIDED.find(world, pos.offset(dir), dir.opposite)?.let {
                        it to dir
                    }
                }
                .filter { (target) -> target.supportsInsertion() }
                .forEach { (target, dir) ->
                    CableEntity.sendTo(world, pos, dir, this.energyStorage, target, this.energyStorage.amount)
                }
        }
    }
}