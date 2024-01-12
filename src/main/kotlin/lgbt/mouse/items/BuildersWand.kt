package lgbt.mouse.items

import eu.pb4.polymer.core.api.item.SimplePolymerItem
import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.ManualAttachment
import eu.pb4.polymer.virtualentity.api.elements.EntityElement
import lgbt.mouse.MousewalkItems
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class BuildersWand : SimplePolymerItem(FabricItemSettings().maxCount(1).rarity(Rarity.RARE).fireproof(), Items.STICK) {
    companion object {
        private const val MAX_RANGE = 50

        private val CARDINAL = listOf(
            BlockPos(0, -1, 0),
            BlockPos(0, 1, 0),
            BlockPos(0, 0, -1),
            BlockPos(0, 0, 1),
            BlockPos(-1, 0, 0),
            BlockPos(1, 0, 0),
        )
        private val X_CARDINAL = listOf(
            CARDINAL[0],
            CARDINAL[1],
            CARDINAL[2],
            CARDINAL[3],
        )
        private val Y_CARDINAL = listOf(
            CARDINAL[2],
            CARDINAL[3],
            CARDINAL[4],
            CARDINAL[5],
        )
        private val Z_CARDINAL = listOf(
            CARDINAL[0],
            CARDINAL[1],
            CARDINAL[4],
            CARDINAL[5],
        )

        private fun findBlocks(
            startingPos: List<BlockPos>,
            block: BlockState,
            world: World,
            side: Direction,
            count: Int = 0,
            seenPos: MutableSet<BlockPos> = mutableSetOf(),
        ): List<BlockPos> {
            val dirs = when (side.axis!!) {
                Axis.X -> X_CARDINAL
                Axis.Y -> Y_CARDINAL
                Axis.Z -> Z_CARDINAL
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
                            && world.testBlockState(it.add(side.vector)) { other -> other.isAir }
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

        private val HIGHLIGHTING_HOLDER = mutableMapOf<PlayerEntity, ElementHolder>()

        fun register() {
            ServerTickEvents.END_SERVER_TICK.register { server ->
                if (server.ticks % 5 == 0) {
                    HIGHLIGHTING_HOLDER.values.forEach { it.destroy() }
                    HIGHLIGHTING_HOLDER.clear()

                    server.playerManager.playerList.filter { player -> player.handItems.any { it.isOf(MousewalkItems.BUILDERS_WAND) } }
                        .forEach { player ->
                            val cast =
                                player.raycast(PlayerEntity.getReachDistance(player.isCreative).toDouble(), 0F, false)

                            (cast as? BlockHitResult)?.let { blockHit ->
                                val block = player.world.getBlockState(blockHit.blockPos)
                                if (!block.isAir) {
                                    val highlights = findBlocks(
                                        listOf(blockHit.blockPos),
                                        block,
                                        player.world,
                                        blockHit.side
                                    )
                                    HIGHLIGHTING_HOLDER[player] = ElementHolder().apply {
                                        this.attachment =
                                            ManualAttachment(this, player.serverWorld) { Vec3d.of(blockHit.blockPos) }
                                        this.startWatching(player)

                                        highlights.forEach {
                                            val e = EntityElement(EntityType.SHULKER, player.serverWorld)
                                            e.entity().isInvisible = true
                                            e.entity().isGlowing = true
                                            e.offset = Vec3d.of(it.subtract(blockHit.blockPos))
                                            this.addElement(e)
                                        }
                                    }
                                }
                            }
                        }
                }

                server.playerManager.playerList.forEach { player ->
                    HIGHLIGHTING_HOLDER[player]?.tick()
                }
            }
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
                context.world.setBlockState(it.add(context.side.vector), block)
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