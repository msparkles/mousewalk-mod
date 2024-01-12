package lgbt.mouse.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.util.ActionResult


fun interface ClientCommandEvent {
    fun onClientCommand(packet: ClientCommandC2SPacket, handler: ServerPlayNetworkHandler): ActionResult

    companion object {
        val INPUT_EVENT: Event<ClientCommandEvent> = EventFactory.createArrayBacked(
            ClientCommandEvent::class.java
        ) { listeners ->
            ClientCommandEvent { packet, handler ->
                for (listener in listeners) {
                    val result = listener.onClientCommand(packet, handler)
                    if (result != ActionResult.PASS) {
                        return@ClientCommandEvent result
                    }
                }
                ActionResult.PASS
            }
        }
    }
}