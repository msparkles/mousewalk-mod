package lgbt.mouse.mixin;

import lgbt.mouse.events.ClientCommandEvent;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Inject(method = "onClientCommand", at = @At("TAIL"))
    public void $mousewalk_onPlayerMove(ClientCommandC2SPacket packet, CallbackInfo ci) {
        ClientCommandEvent.Companion.getINPUT_EVENT().invoker().onClientCommand(packet, (ServerPlayNetworkHandler) (Object) this);
    }
}
