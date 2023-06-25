package net.pineclone.simplecmd.utils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.ConcurrentLinkedDeque;

public class Ticker {

    public static final ConcurrentLinkedDeque<TickerTask.TickableTask> TASKS = new ConcurrentLinkedDeque<>();
    public static MinecraftServer server = null;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> Ticker.server = server);
        ServerTickEvents.END_WORLD_TICK.register(server -> {
            if (TASKS.isEmpty()) return;
            int ticks = Ticker.server.getTicks();
            TASKS.forEach(t -> t.exec(ticks));

        });
    }

    public static void schedule(TickerTask.TickableTask task) {
        TASKS.offer(task);
    }

}
