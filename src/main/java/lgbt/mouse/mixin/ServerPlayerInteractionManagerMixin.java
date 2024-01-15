package lgbt.mouse.mixin;

import lgbt.mouse.items.IndustrialDrill;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Shadow
    public abstract boolean tryBreakBlock(BlockPos pos);

    @Shadow
    protected ServerWorld world;

    @Inject(method = "finishMining", at = @At("HEAD"))
    public void mousewalk$finishMining(BlockPos pos, int sequence, String reason, CallbackInfo ci) {
        if (player.getMainHandStack().getItem() == IndustrialDrill.INSTANCE) {
            HitResult cast = player.raycast(PlayerEntity.getReachDistance(player.isCreative()), 0F, false);

            if (cast instanceof BlockHitResult result) {
                for (BlockPos n : IndustrialDrill.INSTANCE.findBlocks(player, pos, world, result.getSide())) {
                    BlockState state = world.getBlockState(n);
                    if (state != null && state.calcBlockBreakingDelta(player, world, n) > 0.0) {
                        this.tryBreakBlock(n);
                    }
                }
            }
        }
    }

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    public void $mousewalk_tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (player.getMainHandStack().getItem() == IndustrialDrill.INSTANCE
                && IndustrialDrill.INSTANCE.calculateRemainingFuel(player.getMainHandStack()) <= 0) {
            cir.setReturnValue(false);
        }
    }
}
