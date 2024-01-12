package lgbt.mouse.items

import eu.pb4.polymer.core.api.item.SimplePolymerItem
import lgbt.mouse.effects.DoubleJumpStatusEffect
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.FoodComponent
import net.minecraft.item.Items
import net.minecraft.util.Rarity

object Estrogen : SimplePolymerItem(
    FabricItemSettings().rarity(Rarity.EPIC)
        .food(
            FoodComponent.Builder()
                .alwaysEdible()
                .snack()
                .hunger(0)
                .saturationModifier(0.0F)
                .statusEffect(StatusEffectInstance(DoubleJumpStatusEffect, 1200), 100.0F)
                .build()
        ),
    Items.SWEET_BERRIES
)