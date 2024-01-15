package lgbt.mouse.mixin;

import lgbt.mouse.effects.DoubleJumpStatusEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "setOnGround(Z)V", at = @At("TAIL"))
    public void mousewalk$setOnGround(boolean onGround, CallbackInfo ci) {
        if (onGround && ((Entity) (Object) this) instanceof ServerPlayerEntity playerEntity) {
            DoubleJumpStatusEffect.INSTANCE.getHasJumped().remove(playerEntity);
        }
    }

    @Inject(method = "setOnGround(ZLnet/minecraft/util/math/Vec3d;)V", at = @At("TAIL"))
    public void mousewalk$setOnGround(boolean onGround, Vec3d movement, CallbackInfo ci) {
        if (onGround && ((Entity) (Object) this) instanceof ServerPlayerEntity playerEntity) {
            DoubleJumpStatusEffect.INSTANCE.getHasJumped().remove(playerEntity);
        }
    }
}
