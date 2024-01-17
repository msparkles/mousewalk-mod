package lgbt.mouse.blocks

import eu.pb4.polymer.core.api.block.SimplePolymerBlock
import eu.pb4.polymer.core.api.item.PolymerBlockItem
import lgbt.mouse.Mousewalk
import lgbt.mouse.blocks.flag.CableConnectable
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import team.reborn.energy.api.base.SimpleEnergyStorage

object FuelGenerator : SimplePolymerBlock(
    FabricBlockSettings.create().sounds(BlockSoundGroup.COPPER).strength(0.5f),
    Blocks.FURNACE
), BlockEntityProvider, CableConnectable {
    const val FUEL_GENERATOR_NAME = "fuel_generator"

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?,
    ): BlockEntityTicker<T>? {
        if (type == Mousewalk.FUEL_GENERATOR_ENTITY_TYPE) {
            return BlockEntityTicker { world1, pos, state1, be ->
                (be as FuelGeneratorEntity).tick(
                    world1,
                    pos,
                    state1,
                )
            }
        }
        return null
    }

    class FuelGeneratorEntity(pos: BlockPos, state: BlockState) :
        EntityWithEnergyGeneration(Mousewalk.FUEL_GENERATOR_ENTITY_TYPE, pos, state) {

        companion object {
            private const val FUEL_TIME_KEY = "fuel_time"
            private const val FUEL_SLOT_KEY = "fuel_slot"
        }

        var fuelSlot: ItemStack? = null

        val fuelStorage = object : SingleStackStorage() {
            override fun getStack() = fuelSlot ?: ItemStack.EMPTY

            override fun setStack(stack: ItemStack?) {
                fuelSlot = stack
            }

            override fun canExtract(itemVariant: ItemVariant?): Boolean {
                return false
            }

            override fun canInsert(itemVariant: ItemVariant): Boolean {
                return FuelRegistry.INSTANCE[itemVariant.item] > 0
            }
        }

        private var fuelTime = 0

        override val energyStorage = object : SimpleEnergyStorage(40000, 0, 100) {
            override fun onFinalCommit() {
                markDirty()
            }
        }

        override fun readNbt(nbt: NbtCompound) {
            this.fuelTime = nbt.getInt(FUEL_TIME_KEY)
            if (nbt.contains(FUEL_SLOT_KEY)) {
                this.fuelSlot = ItemStack.fromNbt(nbt.getCompound(FUEL_SLOT_KEY))
            }
            super.readNbt(nbt)
        }

        override fun writeNbt(nbt: NbtCompound) {
            nbt.putInt(FUEL_TIME_KEY, this.fuelTime)
            this.fuelSlot?.let {
                nbt.put(FUEL_SLOT_KEY, it.writeNbt(NbtCompound()))
            }
            super.writeNbt(nbt)
        }

        override fun tick(world: World, pos: BlockPos, state: BlockState) {
            if (fuelTime <= 0) {
                fuelSlot?.let {
                    if (!it.isEmpty) {
                        fuelTime += FuelRegistry.INSTANCE[it.item]
                        it.decrement(1)
                    }
                    if (it.isEmpty) {
                        fuelSlot = null
                    }
                }
            }
            if (fuelTime > 0 && energyStorage.amount < energyStorage.capacity) {
                fuelTime -= 10
                energyStorage.amount += 25
            }
            super.tick(world, pos, state)
        }
    }

    val ITEM = object : PolymerBlockItem(FuelGenerator, FabricItemSettings(), Items.FURNACE) {
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = FuelGeneratorEntity(pos, state)
}