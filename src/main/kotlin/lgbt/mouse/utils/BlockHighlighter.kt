package lgbt.mouse.utils

import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.ManualAttachment
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.joml.Vector3f

object BlockHighlighter {

    private val HIGHLIGHTING_HOLDER = mutableMapOf<PlayerEntity, ElementHolder>()
    private const val SCALE = 0.999
    private const val OFFSET = (1.0 - SCALE) / 2.0

    val REGISTERED_HIGHLIGHTER =
        mutableMapOf<Item, Pair<Int, (PlayerEntity, BlockPos, BlockState, World, Direction) -> List<BlockPos>>>()

    fun register() {
        ServerTickEvents.END_SERVER_TICK.register { server ->
            if (server.ticks % 4 == 0) {
                HIGHLIGHTING_HOLDER.values.forEach { it.destroy() }
                HIGHLIGHTING_HOLDER.clear()

                server.playerManager.playerList
                    .firstNotNullOfOrNull { player ->
                        player.handItems
                            .firstNotNullOfOrNull {
                                REGISTERED_HIGHLIGHTER[it.item]
                            }?.let {
                                player to it
                            }
                    }?.let { (player, v) ->
                        val (color, highlighter) = v
                        val cast =
                            player.raycast(PlayerEntity.getReachDistance(player.isCreative).toDouble(), 0F, false)

                        (cast as? BlockHitResult)?.let { blockHit ->
                            val block = player.world.getBlockState(blockHit.blockPos)
                            if (!block.isAir) {
                                val highlights = highlighter(
                                    player,
                                    blockHit.blockPos,
                                    block,
                                    player.world,
                                    blockHit.side
                                )
                                HIGHLIGHTING_HOLDER[player] = ElementHolder().apply {
                                    this.attachment =
                                        ManualAttachment(this, player.serverWorld) { Vec3d.of(blockHit.blockPos) }
                                    this.startWatching(player)

                                    highlights.forEach {
                                        val e = BlockDisplayElement(player.world.getBlockState(it))
                                        e.isGlowing = true
                                        e.glowColorOverride = color
                                        e.scale = Vector3f(SCALE.toFloat(), SCALE.toFloat(), SCALE.toFloat())
                                        e.offset = Vec3d.of(it.subtract(blockHit.blockPos))
                                            .add(Vec3d(OFFSET, OFFSET, OFFSET))
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