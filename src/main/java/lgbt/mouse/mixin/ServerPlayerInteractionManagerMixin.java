package lgbt.mouse.mixin;

import lgbt.mouse.items.IndustrialDrill;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Shadow
    public abstract boolean tryBreakBlock(BlockPos pos);

    @Inject(method = "finishMining", at = @At("HEAD"))
    public void $mousewalk_finishMining(BlockPos pos, int sequence, String reason, CallbackInfo ci) {
        if (this.player.getMainHandStack().getItem() == IndustrialDrill.INSTANCE) {
            HitResult cast = player.raycast(PlayerEntity.getReachDistance(player.isCreative()), 0F, false);
            if (cast instanceof BlockHitResult result) {
                for (BlockPos n : IndustrialDrill.INSTANCE.findBlocks(pos, result.getSide())) {
                    this.tryBreakBlock(n);
                }
            }
        }
    }
}