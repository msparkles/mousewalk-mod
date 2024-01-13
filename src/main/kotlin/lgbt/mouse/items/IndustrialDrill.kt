package lgbt.mouse.items

import eu.pb4.polymer.core.api.item.PolymerItem
import lgbt.mouse.utils.BlockHighlighter
import lgbt.mouse.utils.Cardinal
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.StackReference
import net.minecraft.item.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.registry.tag.BlockTags
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ClickType
import net.minecraft.util.Colors
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object IndustrialDrill :
    MiningToolItem(
        1.0f,
        -2.8f,
        ToolMaterials.DIAMOND,
        BlockTags.PICKAXE_MINEABLE,
        FabricItemSettings()
            .maxCount(1)
            .rarity(Rarity.RARE)
            .fireproof()
    ),
    PolymerItem {
    fun findBlocks(player: PlayerEntity, blockPos: BlockPos, world: World, side: Direction): List<BlockPos> {
        if (player.isSneaking) return emptyList()

        val dirs = when (side.axis!!) {
            Direction.Axis.X -> Cardinal.X_CARDINALS_AND_CORNERS
            Direction.Axis.Y -> Cardinal.Y_CARDINALS_AND_CORNERS
            Direction.Axis.Z -> Cardinal.Z_CARDINALS_AND_CORNERS
        }

        return dirs.map { blockPos.add(it) }.filter { world.testBlockState(it) { state -> !state.isAir } }
    }

    fun register() {
        BlockHighlighter.REGISTERED_HIGHLIGHTER[this] = Colors.BLACK to { player, blockPos, _, world, side ->
            this.findBlocks(player, blockPos, world, side) + blockPos
        }
    }

    override fun getPolymerItem(itemStack: ItemStack?, player: ServerPlayerEntity?): Item? {
        return Items.BUNDLE
    }

    private const val FUEL_KEY = "Items"
    private const val FUEL_BUFFER_KEY = "FuelBuffer"
    private const val FUEL_SIZE = 64

    private fun removeFuel(stack: ItemStack): ItemStack? {
        val nbt = stack.getOrCreateNbt()

        return if (!nbt.contains(FUEL_KEY)) {
            null
        } else {
            val items = nbt.getList(FUEL_KEY, NbtCompound.COMPOUND_TYPE.toInt())

            if (items.isEmpty()) {
                return null
            } else {
                return ItemStack.fromNbt(items.removeAt(0)!! as NbtCompound)
            }
        }
    }

    private fun getFuelBuffer(stack: ItemStack): Int {
        val nbt = stack.getOrCreateNbt()

        return nbt.getInt(FUEL_BUFFER_KEY)
    }

    private fun setFuelBuffer(stack: ItemStack, buffer: Int) {
        val nbt = stack.getOrCreateNbt()

        return nbt.putInt(FUEL_BUFFER_KEY, buffer.coerceAtLeast(0))
    }


    private fun getFuel(stack: ItemStack): ItemStack? {
        val nbt = stack.getOrCreateNbt()

        return if (!nbt.contains(FUEL_KEY)) {
            null
        } else {
            val items = nbt.getList(FUEL_KEY, NbtCompound.COMPOUND_TYPE.toInt())

            if (items.isEmpty()) {
                return null
            } else {
                return ItemStack.fromNbt(items.getCompound(0)!!)
            }
        }
    }

    private fun addFuel(drill: ItemStack, stack: ItemStack): Int {
        if (stack.isEmpty || !stack.item.canBeNested()) {
            return 0
        }
        if (FuelRegistry.INSTANCE[stack.item] == null) {
            return 0
        }

        val nbt = drill.getOrCreateNbt()
        val fuel = this.getFuel(drill) ?: ItemStack.EMPTY

        if (!fuel.isEmpty && fuel.item != stack.item) {
            return 0
        }

        val missing = FUEL_SIZE - fuel.count
        val toInsert = missing.coerceAtMost(stack.count)
        if (toInsert <= 0) {
            return 0
        }

        val items = NbtList()
        items.add(ItemStack(stack.item, fuel.count + toInsert).writeNbt(NbtCompound()))
        nbt.put(FUEL_KEY, items)

        stack.decrement(toInsert)

        return toInsert
    }

    private fun takeOneFuel(drill: ItemStack): Item? {
        val nbt = drill.getOrCreateNbt()
        val fuel = this.getFuel(drill) ?: return null
        if (fuel.isEmpty) return null

        val items = NbtList()
        items.add(ItemStack(fuel.item, fuel.count - 1).writeNbt(NbtCompound()))
        nbt.put(FUEL_KEY, items)

        return fuel.item
    }

    override fun onClicked(
        stack: ItemStack,
        otherStack: ItemStack,
        slot: Slot,
        clickType: ClickType,
        player: PlayerEntity,
        cursorStackReference: StackReference,
    ): Boolean {
        if (clickType != ClickType.RIGHT || !slot.canTakePartial(player)) {
            return false
        }

        if (otherStack.isEmpty) {
            removeFuel(stack)?.let {
                cursorStackReference.set(it)
            }
        } else {
            this.addFuel(stack, otherStack)
        }

        return true
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text?>,
        context: TooltipContext?,
    ) {
        tooltip.add(
            Text.translatable(
                "item.mousewalk.industrial_drill.fuel", this.calculateRemainingFuel(stack)
            )
                .formatted(Formatting.GOLD)
        )
    }

    override fun isItemBarVisible(stack: ItemStack) = true

    fun calculateRemainingFuel(stack: ItemStack): Int {
        val fuelBuffer = this.getFuelBuffer(stack)

        val fuel = this.getFuel(stack) ?: return fuelBuffer
        val fuelValue = FuelRegistry.INSTANCE[fuel.item] ?: return fuelBuffer

        return fuel.count * fuelValue + fuelBuffer
    }

    override fun postMine(
        stack: ItemStack,
        world: World?,
        state: BlockState?,
        pos: BlockPos?,
        miner: LivingEntity?,
    ): Boolean {
        val buffer = getFuelBuffer(stack)
        if (buffer <= 0) {
            takeOneFuel(stack)?.let {
                this.setFuelBuffer(stack, FuelRegistry.INSTANCE[it])
            }
        }
        this.setFuelBuffer(stack, this.getFuelBuffer(stack) - 20)

        return true
    }

    override fun isSuitableFor(stack: ItemStack, state: BlockState?): Boolean {
        return this.calculateRemainingFuel(stack) > 0 && super.isSuitableFor(stack, state)
    }

    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState): Float {
        return if (this.calculateRemainingFuel(stack) > 0) {
            if (state.isIn(BlockTags.PICKAXE_MINEABLE) || state.isIn(BlockTags.SHOVEL_MINEABLE)) {
                return this.miningSpeed
            } else {
                return 1.0f
            }
        } else 0.0f
    }
}