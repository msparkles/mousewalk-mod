package lgbt.mouse.effects

import eu.pb4.polymer.core.api.other.PolymerStatusEffect
import lgbt.mouse.events.ClientCommandEvent
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Colors
import net.minecraft.util.math.Vec3d

object DoubleJumpStatusEffect : PolymerStatusEffect, StatusEffect(StatusEffectCategory.BENEFICIAL, Colors.YELLOW) {
    val hasJumped = mutableMapOf<ServerPlayerEntity, Int>()
    private const val MAX_JUMP = 1

    override fun getPolymerReplacement(player: ServerPlayerEntity): StatusEffect = StatusEffects.SPEED

    fun register() {
        ClientCommandEvent.INPUT_EVENT.register { packet, handler ->
            if (handler.player.hasStatusEffect(this)
                && packet.mode == ClientCommandC2SPacket.Mode.START_SPRINTING
                && !handler.player.groundCollision
            ) {
                val new = 1 + hasJumped.getOrDefault(handler.player, 0)
                if (new <= MAX_JUMP) {
                    hasJumped[handler.player] = new

                    handler.player.velocity = Vec3d.fromPolar(handler.player.pitch, handler.player.yaw)
                    handler.player.velocityModified = true
                    handler.player.addStatusEffect(StatusEffectInstance(StatusEffects.SLOW_FALLING, 10))
                }
            }

            ActionResult.PASS
        }
    }
}