package net.pineclone.simplecmd.cmds.tpa.status;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.pineclone.simplecmd.event.*;
import net.pineclone.simplecmd.utils.IEntityDataSaver;
import net.pineclone.simplecmd.utils.TomlUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class TPAManager {

    private static final ConcurrentHashMap<PlayerEntity, Optional<TPARequest>> SENDERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<PlayerEntity, RequestList> RECEIVERS = new ConcurrentHashMap<>();
    public static final String TPA_COOLED_TIME = "tpa_cooled_time";

    public static void register() {
        PlayerDyingCallBack.EVENT.register((player, damageSource) -> {
            SENDERS.get(player).ifPresent(deadManRequest -> {
                deadManRequest.cancel("cmd.tpa.cancel_due_to_death");
                resetCooledTime(player);
            });
            return ActionResult.PASS;
        });

        PlayerHurtCallBack.EVENT.register((player, damageSource, amount) -> {
            if (!TomlUtils.modToml.getBoolean("cmd.tpa.hurt_break_tpa")) return ActionResult.PASS;
            SENDERS.get(player).ifPresent(hurtManRequest -> {
                if (hurtManRequest.getStatus() instanceof PreTeleport)
                    hurtManRequest.cancel("cmd.tpa.cancel_due_to_hurt");
            });
            return ActionResult.PASS;
        });

        PlayerChangeDimensionCallBack.EVENT.register((player, origin) -> {
            SENDERS.get(player).ifPresent(changeDimensionRequest -> changeDimensionRequest
                    .cancel("cmd.tpa.cancel_due_to_change_dimension"));
            return ActionResult.PASS;
        });

        PlayerLogoutCallBack.EVENT.register(player -> {
            Optional<TPARequest> its = SENDERS.get(player);
            if (its == null) return ActionResult.PASS;
            its.ifPresent(TPARequest::cancel);
            return ActionResult.PASS;
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 != 0) return;
            server.getPlayerManager().getPlayerList().forEach(player -> {
                NbtCompound nbt = ((IEntityDataSaver) player).getPersistentData();
                long time = nbt.getLong(TPA_COOLED_TIME);
                if (time == 0) return;
                if (time == 1) player.sendMessage(Text.translatable("cmd.tpa.cooled_down_reset")
                        .formatted(Formatting.GREEN), false);
                removeCooledTime((IEntityDataSaver) player, 1);
            });
        });

    }

    /**
     * Create a new tpa request, this method will help create one single tpa request from one player point to another player.
     *
     * @param sender   The player who input the command [tpa] in the game.
     * @param receiver The target in the [tpa] command.
     * @see TPARequest
     */
    public static void createNewRequest(PlayerEntity sender,
                                        PlayerEntity receiver) {
        TPARequest newRequest = new TPARequest(sender, receiver);
        //check if the sender and receiver are already in the map, if sender or receiver are not created in the map yet,
        //then create new entry contains that.
        SENDERS.computeIfAbsent(sender, k -> Optional.empty());
        RECEIVERS.computeIfAbsent(receiver, k -> new RequestList());

        //then check if the sender entry's value is null or not, if null, means that this sender has just been created.
        //if not, means that the sender is already existed in the map.
        SENDERS.get(sender).ifPresent(TPARequest::cancel);

        SENDERS.put(sender, Optional.of(newRequest));
        RECEIVERS.get(receiver).push(newRequest);
        //then execute the request.
        newRequest.activate();
    }

    protected static Optional<TPARequest> removeRequest(TPARequest request) {
        PlayerEntity sender = request.getSender();
        return removeRequest(sender);
    }

    protected static Optional<TPARequest> removeRequest(PlayerEntity sender) {
        if (!SENDERS.containsKey(sender)) {
            SENDERS.put(sender, Optional.empty());
            return Optional.empty();
        }

        if (SENDERS.get(sender).isEmpty()) {
            return Optional.empty();
        }

        TPARequest currentRequest = SENDERS.get(sender).get();
        SENDERS.put(sender, Optional.empty());
        RECEIVERS.get(currentRequest.getReceiver()).remove(currentRequest);
        return Optional.of(currentRequest);
    }

    public static RequestList getRequestList(ServerPlayerEntity receiver) {
        RECEIVERS.computeIfAbsent(receiver, k -> new RequestList());
        return RECEIVERS.get(receiver);
    }

    public static long getRemainingCooledTime(PlayerEntity player) {
        NbtCompound nbt = ((IEntityDataSaver) player).getPersistentData();
        return nbt.getLong(TPA_COOLED_TIME);
    }

    public static boolean isCoolingDown(PlayerEntity player) {
        NbtCompound nbt = ((IEntityDataSaver) player).getPersistentData();
        long time = nbt.getLong(TPA_COOLED_TIME);
        return (time > 0);
    }

    public static void resetCooledTime(PlayerEntity player) {
        NbtCompound nbt = ((IEntityDataSaver) player).getPersistentData();
        nbt.putLong(TPA_COOLED_TIME, 0);
    }

    public static void setCooledTime(PlayerEntity player, long value) {
        NbtCompound nbt = ((IEntityDataSaver) player).getPersistentData();
        nbt.putLong(TPA_COOLED_TIME, value);
    }

    public static long addCooledTime(IEntityDataSaver player, long value) {
        NbtCompound nbt = player.getPersistentData();
        long currentValue = nbt.getLong(TPA_COOLED_TIME);
        long maxValue = TomlUtils.modToml.getLong("cmd.tpa.max_cooled_time");

        currentValue = MathHelper.clamp(currentValue + value, 0, maxValue);
        nbt.putLong(TPA_COOLED_TIME, currentValue);
        return currentValue;
    }

    public static long removeCooledTime(IEntityDataSaver player, long value) {
        NbtCompound nbt = player.getPersistentData();
        long currentValue = nbt.getLong(TPA_COOLED_TIME);
        long maxValue = TomlUtils.modToml.getLong("cmd.tpa.max_cooled_time");

        currentValue = MathHelper.clamp(currentValue - value, 0, maxValue);
        nbt.putLong(TPA_COOLED_TIME, currentValue);
        return currentValue;
    }

    public static class RequestList {
        private final LinkedList<TPARequest> requests = new LinkedList<>();

        private RequestList() {
        }

        public Optional<TPARequest> pop() {
            if (requests.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(requests.removeLast());
        }

        private void push(TPARequest request) {
            requests.addLast(request);
        }

        public Optional<TPARequest> peek() {
            if (requests.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(requests.getLast());
        }

        private boolean remove(TPARequest request) {
            return requests.remove(request);
        }
    }

}
