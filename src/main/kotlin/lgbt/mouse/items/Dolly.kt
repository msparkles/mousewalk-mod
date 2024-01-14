package lgbt.mouse.items

import eu.pb4.polymer.core.api.item.SimplePolymerItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.nbt.NbtOps
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.world.World
import kotlin.jvm.optionals.getOrNull

object Dolly : SimplePolymerItem(FabricItemSettings().maxCount(1), null) {
    private const val BLOCK_KEY = "Block"
    private const val BLOCK_ENTITY_KEY = "BlockEntity"

    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayerEntity?): Item {
        return if (itemStack.orCreateNbt.contains(BLOCK_KEY) && itemStack.orCreateNbt.contains(BLOCK_ENTITY_KEY)) {
            Items.OAK_CHEST_BOAT
        } else {
            Items.OAK_BOAT
        }
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text?>,
        context: TooltipContext?,
    ) {
        super.appendTooltip(stack, world, tooltip, context)

        tooltip.add(Text.translatable("item.mousewalk.dolly.usage1").formatted(Formatting.GRAY))
        tooltip.add(Text.translatable("item.mousewalk.dolly.usage2").formatted(Formatting.GRAY))
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val stack = context.stack
        val nbt = stack.orCreateNbt
        val pos = context.blockPos.add(context.side.vector)

        if (nbt.contains(BLOCK_KEY) && nbt.contains(BLOCK_ENTITY_KEY)) {
            val blockData = nbt.getCompound(BLOCK_KEY)
            val blockEntityData = nbt.getCompound(BLOCK_ENTITY_KEY)

            nbt.remove(BLOCK_KEY)
            nbt.remove(BLOCK_ENTITY_KEY)

            val block =
                BlockState.CODEC.decode(NbtOps.INSTANCE, blockData).result().getOrNull()?.first
                    ?: return ActionResult.FAIL
            context.world.setBlockState(pos, block)

            val blockEntity = BlockEntity.createFromNbt(pos, block, blockEntityData) ?: return ActionResult.FAIL
            context.world.addBlockEntity(blockEntity)
        } else {
            val block = context.world.getBlockState(context.blockPos)
            if (block.isAir) return ActionResult.FAIL
            val blockEntity = context.world.getBlockEntity(context.blockPos) ?: return ActionResult.FAIL
            val blockData =
                BlockState.CODEC.encodeStart(NbtOps.INSTANCE, block).result().getOrNull() ?: return ActionResult.FAIL
            val blockEntityData = blockEntity.createNbtWithId() ?: return ActionResult.FAIL

            nbt.put(BLOCK_KEY, blockData)
            nbt.put(BLOCK_ENTITY_KEY, blockEntityData)

            context.world.removeBlockEntity(context.blockPos)
            context.world.removeBlock(context.blockPos, false)
        }

        return ActionResult.SUCCESS
    }
}