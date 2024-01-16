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
import team.reborn.energy.api.base.SimpleSidedEnergyContainer

abstract class CableEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(type, pos, state) {
    abstract val sidedEnergy: SimpleSidedEnergyContainer

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putLong(ENERGY_KEY, sidedEnergy.amount)
        super.writeNbt(nbt)
    }

    override fun readNbt(nbt: NbtCompound) {
        sidedEnergy.amount = nbt.getLong(ENERGY_KEY)
        super.readNbt(nbt)
    }

    open fun tick(world: World, pos: BlockPos, state: BlockState) {
        this.trySendEnergy(world, pos, this.sidedEnergy)
    }

    fun trySendEnergy(world: World, pos: BlockPos, sidedEnergy: SimpleSidedEnergyContainer) {
        Direction.entries.mapNotNull { dir ->
            EnergyStorage.SIDED.find(world, pos.add(dir.vector), dir.opposite)?.let {
                it to dir
            }
        }.forEach { (target, dir) ->
            if (target.supportsInsertion()) {
                val storage = sidedEnergy.getSideStorage(dir)
                if (storage.supportsExtraction()) {
                    EnergyStorageUtil.move(
                        storage,
                        target,
                        Long.MAX_VALUE,
                        null
                    )
                }
            }
        }
    }
}