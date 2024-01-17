package lgbt.mouse.blocks.transfer

import lgbt.mouse.blocks.EntityWithEnergy.Companion.ENERGY_KEY
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.EnergyStorageUtil

abstract class CableEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(type, pos, state) {
    abstract val sidedEnergy: CableSidedEnergyContainer

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putLong(ENERGY_KEY, sidedEnergy.amount)
        super.writeNbt(nbt)
    }

    override fun readNbt(nbt: NbtCompound) {
        sidedEnergy.amount = nbt.getLong(ENERGY_KEY)
        super.readNbt(nbt)
    }

    open fun tick(world: World, pos: BlockPos, state: BlockState) {
        trySendEnergy(world, pos, this.sidedEnergy)
    }

    companion object {
        fun sendTo(
            world: World,
            pos: BlockPos,
            direction: Direction,
            storage: EnergyStorage,
            target: EnergyStorage,
            amount: Long,
        ) {
            if (storage.supportsExtraction()) {
                val result = EnergyStorageUtil.move(
                    storage,
                    target,
                    amount,
                    null
                )
                if (result > 0) {
                    (world.getBlockEntity(pos.offset(direction)) as? CableEntity)?.let {
                        it.sidedEnergy.insertSides += direction.opposite
                    }
                }
            }
        }

        fun trySendEnergy(world: World, pos: BlockPos, sidedEnergy: CableSidedEnergyContainer) {
            Direction.entries
                .filterNot { sidedEnergy.insertSides.contains(it) }
                .mapNotNull { dir ->
                    EnergyStorage.SIDED.find(world, pos.offset(dir), dir.opposite)?.let {
                        it to dir
                    }
                }
                .filter { (target) -> target.supportsInsertion() }
                .forEach { (target, dir) ->
                    sendTo(world, pos, dir, sidedEnergy.getSideStorage(dir), target, sidedEnergy.amount)
                }
        }
    }
}