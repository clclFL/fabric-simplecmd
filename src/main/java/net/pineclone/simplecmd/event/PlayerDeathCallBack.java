package net.pineclone.simplecmd.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerDeathCallBack {
    Event<PlayerDeathCallBack> EVENT = EventFactory.createArrayBacked(PlayerDeathCallBack.class,
            (listeners) -> (player , damageSource) -> {
                for (PlayerDeathCallBack event : listeners) {
                    ActionResult result = event.interact(player, damageSource);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }
    );

    ActionResult interact(PlayerEntity player , DamageSource damageSource);

}
