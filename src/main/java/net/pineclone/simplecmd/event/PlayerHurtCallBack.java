package net.pineclone.simplecmd.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerHurtCallBack {
    Event<PlayerHurtCallBack> EVENT = EventFactory.createArrayBacked(PlayerHurtCallBack.class,
            (listeners) -> (player , damageSource , amount) -> {
                for (PlayerHurtCallBack event : listeners) {
                    ActionResult result = event.interact(player, damageSource , amount);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }
    );

    ActionResult interact(PlayerEntity player , DamageSource damageSource, float amount);

}
