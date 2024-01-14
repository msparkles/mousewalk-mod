package lgbt.mouse.blocks

import eu.pb4.polymer.core.api.block.SimplePolymerBlock
import eu.pb4.polymer.core.api.item.PolymerBlockItem
import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement
import lgbt.mouse.MOD_ID
import lgbt.mouse.utils.Cardinal.CARDINALS
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

val COPPER_CABLE_ID = Identifier(MOD_ID, "copper_cable")

object CopperCable :
    SimplePolymerBlock(
        FabricBlockSettings.create().sounds(BlockSoundGroup.COPPER).strength(0.2f).nonOpaque(),
        Blocks.BARRIER
    ) {
    object Item : PolymerBlockItem(CopperCable, FabricItemSettings(), Items.LIGHTNING_ROD)

    private const val SCALE_SCALAR = 0.3333333333333333
    private const val OFFSET_SCALAR = 0.5 - SCALE_SCALAR / 2.0
    private val SCALE = Vec3d(SCALE_SCALAR, SCALE_SCALAR, SCALE_SCALAR)
    private val OFFSET = Vec3d(OFFSET_SCALAR, OFFSET_SCALAR, OFFSET_SCALAR)

    private val ELEMENT_HOLDERS = mutableMapOf<BlockPos, ElementHolder>()

    private fun remove(pos: BlockPos) {
        ELEMENT_HOLDERS[pos]?.destroy()
        ELEMENT_HOLDERS.remove(pos)
    }

    private fun newHolder(pos: BlockPos, world: ServerWorld) {
        remove(pos)

        val holder = ElementHolder()

        val center = BlockDisplayElement(Blocks.COPPER_BLOCK.defaultState)
        center.scale = SCALE.toVector3f()
        center.offset = OFFSET
        holder.addElement(center)

        CARDINALS
            .forEach {
                if (world.getBlockState(pos.add(it)).block is CopperCable) {
                    val e = BlockDisplayElement(Blocks.COPPER_BLOCK.defaultState)
                    e.scale = SCALE.toVector3f()
                    e.offset = Vec3d.of(it).multiply(SCALE).add(OFFSET)
                    holder.addElement(e)
                }
            }

        holder.attachment = BlockBoundAttachment.of(holder, world, Vec3d.of(pos))

        ELEMENT_HOLDERS[pos] = holder

        holder.tick()
    }

    override fun onBlockAdded(
        state: BlockState?,
        world: World?,
        pos: BlockPos,
        oldState: BlockState?,
        notify: Boolean,
    ) {
        (world as? ServerWorld)?.let {
            newHolder(pos, it)
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
        state: BlockState?,
        world: World?,
        pos: BlockPos,
        sourceBlock: Block?,
        sourcePos: BlockPos?,
        notify: Boolean,
    ) {
        (world as? ServerWorld)?.let {
            newHolder(pos, it)
        }

        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify)
    }

    override fun onPolymerBlockSend(blockState: BlockState?, pos: BlockPos.Mutable, player: ServerPlayerEntity) {
        newHolder(pos.toImmutable(), player.serverWorld)

        super.onPolymerBlockSend(blockState, pos, player)
    }
}