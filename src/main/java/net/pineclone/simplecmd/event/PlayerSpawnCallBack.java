package net.pineclone.simplecmd.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerSpawnCallBack {
    Event<PlayerSpawnCallBack> EVENT = EventFactory.createArrayBacked(PlayerSpawnCallBack.class,
            (listeners) -> (player) -> {
                for (PlayerSpawnCallBack event : listeners) {
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
