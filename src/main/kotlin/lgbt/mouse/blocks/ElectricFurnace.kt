package lgbt.mouse.blocks

import eu.pb4.polymer.core.api.block.SimplePolymerBlock
import eu.pb4.polymer.core.api.item.PolymerBlockItem
import eu.pb4.sgui.api.ScreenProperty
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import lgbt.mouse.Mousewalk
import lgbt.mouse.blocks.flag.CableConnectable
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.RecipeType
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import team.reborn.energy.api.base.SimpleEnergyStorage
import kotlin.jvm.optionals.getOrNull


object ElectricFurnace : SimplePolymerBlock(
    FabricBlockSettings.create().sounds(BlockSoundGroup.COPPER).strength(0.5f),
    Blocks.FURNACE
), BlockEntityProvider, CableConnectable {
    init {
        this.defaultState = defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH);
    }

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

            const val TICKS_NEEDED: Short = 80

            class FurnaceGui(state: BlockState, val entity: ElectricFurnaceEntity, player: ServerPlayerEntity) :
                SimpleGui(ScreenHandlerType.FURNACE, player, false) {
                override fun onTick() {
                    this.sendProperty(ScreenProperty.MAX_PROGRESS, TICKS_NEEDED.toInt())
                    this.sendProperty(ScreenProperty.CURRENT_PROGRESS, entity.cookTime.toInt())
                }

                init {
                    this.title = Text.translatable(state.block.getTranslationKey())
                    this.addSlotRedirect(entity.inputSlot)
                    this.addSlot(GuiElementBuilder().setItem(Items.BARRIER).setName(Text.empty()))
                    this.addSlotRedirect(entity.outputSlot)
                }
            }
        }

        var cookTime: Short = 0
            private set

        val inputSlot = Slot(SimpleInventory(1), 0, 0, 0)
        val outputSlot = Slot(SimpleInventory(1), 0, 0, 0)

        val storage = CombinedStorage(listOf(
            object : SingleStackStorage() {
                override fun getStack() = inputSlot.stack

                override fun setStack(stack: ItemStack?) {
                    inputSlot.stack = stack ?: ItemStack.EMPTY
                }

                override fun canExtract(itemVariant: ItemVariant?) = false
                override fun supportsExtraction() = false
            },
            object : SingleStackStorage() {
                override fun getStack() = outputSlot.stack

                override fun setStack(stack: ItemStack?) {
                    outputSlot.stack = stack ?: ItemStack.EMPTY
                }

                override fun canInsert(itemVariant: ItemVariant?) = false
                override fun supportsInsertion() = false
            }
        ))

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
            nbt.put(INPUT_SLOT_KEY, this.inputSlot.stack.writeNbt(NbtCompound()))
            nbt.put(OUTPUT_SLOT_KEY, this.outputSlot.stack.writeNbt(NbtCompound()))
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
                        world.recipeManager.getFirstMatch(RecipeType.SMELTING, inputSlot.inventory, world).getOrNull()
                            ?: return

                    val result = recipe.value.craft(inputSlot.inventory, world.registryManager)

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


    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return super.getPlacementState(ctx)!!
            .with(Properties.HORIZONTAL_FACING, ctx.horizontalPlayerFacing.opposite)
    }

    override fun getPolymerBlockState(state: BlockState): BlockState {
        return this.getPolymerBlock(state).defaultState.with(
            Properties.HORIZONTAL_FACING,
            state[Properties.HORIZONTAL_FACING]
        )
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand?,
        hit: BlockHitResult?,
    ): ActionResult {
        if (world.isClient) return ActionResult.PASS

        (player as? ServerPlayerEntity)?.let {
            val entity = (world.getBlockEntity(pos) as? ElectricFurnaceEntity) ?: return ActionResult.FAIL
            val gui = ElectricFurnaceEntity.Companion.FurnaceGui(state, entity, player)

            gui.open()
        }
        return ActionResult.SUCCESS
    }

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean,
    ) {
        if (state.block != newState.block) {
            (world.getBlockEntity(pos) as? ElectricFurnaceEntity)?.let {
                ItemScatterer.spawn(world, pos, it.inputSlot.inventory)
                ItemScatterer.spawn(world, pos, it.outputSlot.inventory)
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = ElectricFurnaceEntity(pos, state)
}