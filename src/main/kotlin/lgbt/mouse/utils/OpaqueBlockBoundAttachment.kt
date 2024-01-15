package lgbt.mouse.utils

import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.chunk.WorldChunk


class OpaqueBlockBoundAttachment(
    holder: ElementHolder,
    chunk: WorldChunk?,
    private var blockState: BlockState,
    private val blockPos: BlockPos,
    position: Vec3d,
    autoTick: Boolean,
) : ChunkAttachment(holder, chunk, position, autoTick), BlockAwareAttachment {
    companion object {
        fun of(
            holder: ElementHolder,
            serverWorld: ServerWorld,
            blockPos: BlockPos,
            state: BlockState,
            autoTick: Boolean = false,
        ): OpaqueBlockBoundAttachment? {
            return serverWorld.getWorldChunk(blockPos)?.let { chunk ->
                OpaqueBlockBoundAttachment(
                    holder,
                    chunk,
                    state,
                    blockPos,
                    Vec3d.of(blockPos),
                    autoTick,
                )
            }
        }
    }

    override fun getBlockPos(): BlockPos {
        return this.blockPos
    }

    override fun getBlockState(): BlockState {
        return this.blockState
    }

    override fun isPartOfTheWorld(): Boolean {
        return true
    }

    override fun tick() {
        super<ChunkAttachment>.tick()

        if (world.server.ticks % 20 == 0) {
            for (player in world.server.playerManager.playerList) {
                val playerPos = player.getCameraPosVec(0.0f)
                val blockPos = this.blockPos.toCenterPos()

                val canSee = playerPos.squaredDistanceTo(blockPos) < 128.0 && this.world.raycast(
                    RaycastContext(
                        playerPos,
                        blockPos,
                        RaycastContext.ShapeType.VISUAL,
                        RaycastContext.FluidHandling.NONE,
                        ShapeContext.of(player)
                    )
                )?.blockPos == this.blockPos

                if (canSee) {
                    VirtualEntityUtils.wrapCallWithContext(this.world) { this.startWatching(player) }
                } else {
                    VirtualEntityUtils.wrapCallWithContext(this.world) { this.stopWatching(player) }
                }
            }
        }
    }
}
