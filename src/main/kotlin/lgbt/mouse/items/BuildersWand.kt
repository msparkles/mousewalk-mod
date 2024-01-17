package lgbt.mouse.items

import eu.pb4.polymer.core.api.item.SimplePolymerItem
import lgbt.mouse.utils.BlockHighlighter
import lgbt.mouse.utils.Cardinal.X_CARDINALS
import lgbt.mouse.utils.Cardinal.Y_CARDINALS
import lgbt.mouse.utils.Cardinal.Z_CARDINALS
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.BlockState
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
import net.minecraft.util.Colors
import net.minecraft.util.Rarity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis
import net.minecraft.world.World

object BuildersWand : SimplePolymerItem(FabricItemSettings().maxCount(1).rarity(Rarity.RARE).fireproof(), Items.STICK) {

    private const val MAX_RANGE = 50

    private fun findBlocks(
        startingPos: List<BlockPos>,
        block: BlockState,
        world: World,
        side: Direction,
        count: Int = 0,
        seenPos: MutableSet<BlockPos> = mutableSetOf(),
    ): List<BlockPos> {
        val dirs = when (side.axis!!) {
            Axis.X -> X_CARDINALS
            Axis.Y -> Y_CARDINALS
            Axis.Z -> Z_CARDINALS
        }

        if (count >= MAX_RANGE) {
            return emptyList()
        }

        val set = mutableSetOf<BlockPos>()
        startingPos.forEach { pos ->
            (dirs.map { pos.add(it) } + pos)
                .filter { !seenPos.contains(it) }
                .forEach {
                    if (
                        world.testBlockState(it) { other -> other == block }
                        && world.testBlockState(it.offset(side)) { other -> other.isAir }
                    ) {
                        set += it
                    }
                }
        }

        val results = set.take(MAX_RANGE - count)
        if (results.isEmpty()) {
            return emptyList()
        }

        seenPos += results

        return results + findBlocks(results, block, world, side, count + results.size, seenPos)
    }

    fun register() {
        BlockHighlighter.REGISTERED_HIGHLIGHTER[this] = Colors.WHITE to { _, blockPos, blockState, world, side ->
            this.findBlocks(listOf(blockPos), blockState, world, side)
        }
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val block = context.world.getBlockState(context.blockPos)
        val player = context.player ?: return ActionResult.FAIL

        val canBePlaced = findBlocks(listOf(context.blockPos), block, context.world, context.side)

        if (canBePlaced.isNotEmpty()) {
            if (!player.isCreative) {
                val removed =
                    player.inventory.remove({ it.isOf(block.block.asItem()) }, canBePlaced.size, SimpleInventory(0))

                canBePlaced.take(removed)
            } else {
                canBePlaced
            }.forEach {
                context.world.setBlockState(it.offset(context.side), block)
            }

            player.playSound(
                block.soundGroup.placeSound,
                SoundCategory.BLOCKS,
                1.0F,
                1.0F
            )

            return ActionResult.SUCCESS
        }

        return super.useOnBlock(context)
    }
}