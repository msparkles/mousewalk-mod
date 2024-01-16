package lgbt.mouse.blocks

import eu.pb4.polymer.core.api.block.SimplePolymerBlock
import eu.pb4.polymer.core.api.item.PolymerBlockItem
import eu.pb4.sgui.api.gui.SimpleGuiBuilder
import eu.pb4.sgui.virtual.SguiScreenHandlerFactory
import lgbt.mouse.Mousewalk
import lgbt.mouse.blocks.flag.CableConnectable
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.RecipeType
import net.minecraft.screen.FurnaceScreenHandler
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import team.reborn.energy.api.base.SimpleEnergyStorage
import kotlin.jvm.optionals.getOrNull


object ElectricFurnace : SimplePolymerBlock(
    FabricBlockSettings.create().sounds(BlockSoundGroup.COPPER).strength(0.5f),
    Blocks.FURNACE
), BlockEntityProvider, CableConnectable {
    const val ELECTRIC_FURNACE_NAME = "electric_furnace"

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?,
    ): BlockEntityTicker<T>? {
        if (type == Mousewalk.ELECTRIC_FURNACE_ENTITY_TYPE) {
            return BlockEntityTicker { world1, pos, state1, be ->
                (be as ElectricFurnaceEntity).tick(
                    world1,
                    pos,
                    state1
                )
            }
        }
        return null
    }

    class ElectricFurnaceEntity(pos: BlockPos, state: BlockState) :
        EntityWithEnergy(Mousewalk.ELECTRIC_FURNACE_ENTITY_TYPE, pos, state) {

        companion object {
            private const val COOK_TIME_KEY = "CookTime"
            private const val COOK_TIME_TOTAL_KEY = "CookTimeTotal"
            private const val INPUT_SLOT_KEY = "input_slot"
            private const val OUTPUT_SLOT_KEY = "output_slot"
            private const val TICKS_NEEDED: Short = 80
        }

        private var cookTime: Short = 0

        val propertyDelegate = object : PropertyDelegate {
            override fun get(index: Int): Int {
                return when (index) {
                    2 -> cookTime.toInt()
                    3 -> TICKS_NEEDED.toInt()
                    else -> 0
                }
            }

            override fun set(index: Int, value: Int) {
            }

            override fun size(): Int {
                return 4
            }
        };

        val inventory = SimpleInventory(3)

        private var inputSlot: Slot = Slot(inventory, 0, 0, 0)
        private var outputSlot: Slot = Slot(inventory, 2, 0, 0)

        val builder: SimpleGuiBuilder = run {
            val builder = SimpleGuiBuilder(ScreenHandlerType.FURNACE, false)

            builder.title = Text.translatable(state.block.getTranslationKey())
            builder.setSlotRedirect(0, this.inputSlot)
            builder.setSlotRedirect(2, this.outputSlot)

            builder
        }

        val inputStorage = object : SingleStackStorage() {
            override fun getStack() = inputSlot.stack

            override fun setStack(stack: ItemStack?) {
                inputSlot.stack = stack
            }
        }


        override val energyStorage = object : SimpleEnergyStorage(4000, 100, 0) {
            override fun onFinalCommit() {
                markDirty()
            }
        }

        override fun readNbt(nbt: NbtCompound) {
            this.cookTime = nbt.getShort(COOK_TIME_KEY)
            if (nbt.contains(INPUT_SLOT_KEY)) {
                this.inputSlot.stack = ItemStack.fromNbt(nbt.getCompound(INPUT_SLOT_KEY))
            }
            if (nbt.contains(OUTPUT_SLOT_KEY)) {
                this.outputSlot.stack = ItemStack.fromNbt(nbt.getCompound(OUTPUT_SLOT_KEY))
            }
            super.readNbt(nbt)
        }

        override fun writeNbt(nbt: NbtCompound) {
            nbt.putShort(COOK_TIME_TOTAL_KEY, TICKS_NEEDED)
            nbt.putShort(COOK_TIME_KEY, this.cookTime)
            this.inputSlot.stack.let {
                nbt.put(INPUT_SLOT_KEY, it.writeNbt(NbtCompound()))
            }
            this.outputSlot.stack.let {
                nbt.put(OUTPUT_SLOT_KEY, it.writeNbt(NbtCompound()))
            }
            super.writeNbt(nbt)
        }

        override fun tick(world: World, pos: BlockPos, state: BlockState) {
            if (cookTime < TICKS_NEEDED && energyStorage.amount >= 5) {
                cookTime++
                energyStorage.amount -= 5
            }
            if (cookTime >= TICKS_NEEDED) {
                if (!inputSlot.stack.isEmpty) {
                    val recipe =
                        world.recipeManager.getFirstMatch(RecipeType.SMELTING, inventory, world).getOrNull()
                            ?: return

                    val result = recipe.value.craft(inventory, world.registryManager)

                    if (outputSlot.stack.isEmpty) {
                        outputSlot.stack = result
                    } else {
                        if (outputSlot.stack.item == result.item) {
                            outputSlot.stack.increment(result.count)
                        } else {
                            return
                        }
                    }

                    inputSlot.stack.decrement(1)
                    cookTime = 0
                }
            }

            super.tick(world, pos, state)
        }
    }

    val ITEM = object : PolymerBlockItem(ElectricFurnace, FabricItemSettings(), Items.FURNACE) {
    }


    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand?,
        hit: BlockHitResult?,
    ): ActionResult {
        (player as? ServerPlayerEntity)?.let { serverPlayer ->
            (world.getBlockEntity(pos) as? ElectricFurnaceEntity)?.let {
                serverPlayer.openHandledScreen(SguiScreenHandlerFactory(it.builder.build(player)) { syncId, playerInventory, _ ->
                    FurnaceScreenHandler(syncId, playerInventory, it.inventory, it.propertyDelegate)
                })
            }
        }
        return ActionResult.SUCCESS
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = ElectricFurnaceEntity(pos, state)
}