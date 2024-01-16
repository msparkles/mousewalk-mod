package lgbt.mouse.blocks.transfer

import eu.pb4.polymer.core.api.block.SimplePolymerBlock
import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement
import lgbt.mouse.MOD_ID
import lgbt.mouse.blocks.flag.CableConnectable
import lgbt.mouse.utils.OpaqueBlockBoundAttachment
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.DyeItem
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

abstract class Cable<T : Cable<T>>(
    private val color: DyeColor?,
    name: String,
    settings: FabricBlockSettings,
) : SimplePolymerBlock(
    run {
        var base = settings
            .drops(Identifier(MOD_ID, "blocks/${name}"))
            .nonOpaque()

        color?.let {
            base = base.mapColor(it)
        }

        base
    },
    null
), BlockEntityProvider {
    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?,
    ): BlockEntityTicker<T> {
        return BlockEntityTicker { world1, pos, state1, be ->
            (be as? CableEntity)?.tick(
                world1,
                pos,
                state1
            )
        }
    }

    companion object {
        private const val SCALE_SCALAR = 0.3333333333333333
        private const val OFFSET_SCALAR = 0.5 - SCALE_SCALAR / 2.0
        private val SCALE = Vec3d(SCALE_SCALAR, SCALE_SCALAR, SCALE_SCALAR)
        private val OFFSET = Vec3d(OFFSET_SCALAR, OFFSET_SCALAR, OFFSET_SCALAR)

        private val ELEMENT_HOLDERS = mutableMapOf<BlockPos, ElementHolder>()

        fun <T : Cable<T>> makeAllColors(
            colorless: T,
            name: String,
            supplier: (DyeColor) -> T,
        ): Map<DyeColor?, Pair<T, String>> {
            return mapOf(null to (colorless to name)) + DyeColor.entries.associateWith { (supplier(it) to "${it.getName()}_${name}") }
        }
    }

    abstract val colorless: Lazy<T>
    abstract val allColors: Lazy<Map<DyeColor?, Pair<T, String>>>

    open fun isSameBlock(other: Block): Boolean {
        return other is Cable<*>
    }

    override fun getPolymerBlock(state: BlockState): Block {
        return color?.let {
            when (it) {
                DyeColor.WHITE -> Blocks.WHITE_STAINED_GLASS
                DyeColor.ORANGE -> Blocks.ORANGE_STAINED_GLASS
                DyeColor.MAGENTA -> Blocks.MAGENTA_STAINED_GLASS
                DyeColor.LIGHT_BLUE -> Blocks.LIGHT_BLUE_STAINED_GLASS
                DyeColor.YELLOW -> Blocks.YELLOW_STAINED_GLASS
                DyeColor.LIME -> Blocks.LIME_STAINED_GLASS
                DyeColor.PINK -> Blocks.PINK_STAINED_GLASS
                DyeColor.GRAY -> Blocks.GRAY_STAINED_GLASS
                DyeColor.LIGHT_GRAY -> Blocks.LIGHT_GRAY_STAINED_GLASS
                DyeColor.CYAN -> Blocks.CYAN_STAINED_GLASS
                DyeColor.PURPLE -> Blocks.PURPLE_STAINED_GLASS
                DyeColor.BLUE -> Blocks.BLUE_STAINED_GLASS
                DyeColor.BROWN -> Blocks.BROWN_STAINED_GLASS
                DyeColor.GREEN -> Blocks.GREEN_STAINED_GLASS
                DyeColor.RED -> Blocks.RED_STAINED_GLASS
                DyeColor.BLACK -> Blocks.BLACK_STAINED_GLASS
            }
        } ?: Blocks.GLASS
    }

    override fun getCameraCollisionShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext?,
    ): VoxelShape {
        return VoxelShapes.empty()
    }

    private fun remove(pos: BlockPos) {
        ELEMENT_HOLDERS[pos]?.destroy()
        ELEMENT_HOLDERS.remove(pos)
    }

    fun canConnectCable(otherBlock: Block): Boolean {
        if (this == otherBlock) {
            return true
        }

        if (this.isSameBlock(otherBlock)) {
            return (this == this.colorless.value || otherBlock == this.colorless.value)
        }

        return otherBlock is CableConnectable
    }

    private fun getConnected(pos: BlockPos, world: World) = Direction.entries.mapNotNull {
        val otherState = world.getBlockState(pos.add(it.vector))

        if (canConnectCable(otherState.block)) {
            it
        } else {
            null
        }
    }


    private fun getOffsets(pos: BlockPos, world: World) =
        listOf(Vec3d.ZERO) + getConnected(pos, world).map {
            Vec3d.of(it.vector).multiply(SCALE)
        }

    private fun newHolder(pos: BlockPos, state: BlockState, world: ServerWorld) {
        remove(pos)

        val holder = ElementHolder()

        getOffsets(pos, world).forEach { o ->
            val e = BlockDisplayElement(Blocks.COPPER_BLOCK.defaultState)
            e.scale = SCALE.toVector3f()
            e.offset = o.add(OFFSET)
            holder.addElementWithoutUpdates(e)
        }

        holder.attachment = OpaqueBlockBoundAttachment.of(holder, world, pos, state, true)
        holder.tick()

        ELEMENT_HOLDERS[pos] = holder
    }

    override fun onBlockAdded(
        state: BlockState,
        world: World?,
        pos: BlockPos,
        oldState: BlockState?,
        notify: Boolean,
    ) {
        (world as? ServerWorld)?.let {
            newHolder(pos, state, it)
        }

        super.onBlockAdded(state, world, pos, oldState, notify)
    }


    override fun onStateReplaced(
        state: BlockState?,
        world: World?,
        pos: BlockPos,
        newState: BlockState?,
        moved: Boolean,
    ) {
        remove(pos)

        super.onStateReplaced(state, world, pos, newState, moved)
    }


    override fun neighborUpdate(
        state: BlockState,
        world: World?,
        pos: BlockPos,
        sourceBlock: Block?,
        sourcePos: BlockPos?,
        notify: Boolean,
    ) {
        (world as? ServerWorld)?.let {
            newHolder(pos, state, it)
        }

        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult,
    ): ActionResult {
        (player.getStackInHand(hand).item as? DyeItem)?.let {
            this.allColors.value[it.color]?.let { (block) ->
                world.setBlockState(pos, block.getStateWithProperties(state))
                return ActionResult.SUCCESS
            }
        }

        if (player.getStackInHand(hand).item == Items.CLAY_BALL) {
            this.allColors.value[null]?.let { (block) ->
                world.setBlockState(pos, block.getStateWithProperties(state))
                return ActionResult.SUCCESS
            }
        }

        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun onPolymerBlockSend(blockState: BlockState, pos: BlockPos.Mutable, player: ServerPlayerEntity) {
        newHolder(pos.toImmutable(), blockState, player.serverWorld)

        super.onPolymerBlockSend(blockState, pos, player)
    }
}