package net.pineclone.simplecmd.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

public interface PlayerChangeDimensionCallBack {
    Event<PlayerChangeDimensionCallBack> EVENT = EventFactory.createArrayBacked(PlayerChangeDimensionCallBack.class,
            (listeners) -> (player , origin) -> {
                for (PlayerChangeDimensionCallBack event : listeners) {
                    ActionResult result = event.interact(player , origin);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }
    );

    ActionResult interact(ServerPlayerEntity player , ServerWorld origin);


}
