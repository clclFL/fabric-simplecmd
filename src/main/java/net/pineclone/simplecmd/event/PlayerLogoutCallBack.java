package net.pineclone.simplecmd.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerLogoutCallBack {
    Event<PlayerLogoutCallBack> EVENT = EventFactory.createArrayBacked(PlayerLogoutCallBack.class,
            (listeners) -> (player) -> {
                for (PlayerLogoutCallBack event : listeners) {
                    ActionResult result = event.interact(player);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            }
    );

    ActionResult interact(ServerPlayerEntity player);

}
