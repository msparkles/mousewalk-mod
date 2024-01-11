package lgbt.mouse.items

import eu.pb4.polymer.core.api.item.SimplePolymerItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class BuildersWand : SimplePolymerItem(FabricItemSettings().maxCount(1).rarity(Rarity.RARE).fireproof(), Items.STICK) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val block = context.world.getBlockState(context.blockPos)
        val player = context.player ?: return ActionResult.FAIL

        val positions = mutableListOf<Pair<BlockPos, BlockPos>>()
        for (i in -2..2) {
            for (j in -2..2) {
                positions += when (context.side!!) {
                    Direction.DOWN -> context.blockPos.add(BlockPos(i, 0, j)) to BlockPos(0, -1, 0)
                    Direction.UP -> context.blockPos.add(BlockPos(i, 0, j)) to BlockPos(0, 1, 0)
                    Direction.NORTH -> context.blockPos.add(BlockPos(i, j, 0)) to BlockPos(0, 0, -1)
                    Direction.SOUTH -> context.blockPos.add(BlockPos(i, j, 0)) to BlockPos(0, 0, 1)
                    Direction.WEST -> context.blockPos.add(BlockPos(0, i, j)) to BlockPos(-1, 0, 0)
                    Direction.EAST -> context.blockPos.add(BlockPos(0, i, j)) to BlockPos(1, 0, 0)
                }
            }
        }
        val canBePlaced = positions
            .filter { context.world.testBlockState(it.first) { other -> other == block } }
            .filter { context.world.testBlockState(it.first.add(it.second)) { other -> other.isAir } }

        if (canBePlaced.isNotEmpty()) {
            if (!player.isCreative) {
                val removed =
                    player.inventory.remove({ it.isOf(block.block.asItem()) }, canBePlaced.size, SimpleInventory(0))

                canBePlaced.take(removed)
            } else {
                canBePlaced
            }.forEach {
                context.world.setBlockState(it.first.add(it.second), block)
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