package net.pineclone.simplecmd.cmds.tpa.status;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.pineclone.simplecmd.event.*;
import net.pineclone.simplecmd.utils.Ticker;
import net.pineclone.simplecmd.utils.TickerTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class TPAManager {

    private static final ConcurrentHashMap<PlayerEntity, Optional<TPARequest>> SENDERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<PlayerEntity, RequestList> RECEIVERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<PlayerEntity, TickerTask.DelayTask> COOLED_DOWN = new ConcurrentHashMap<>();

    public static void register() {
        PlayerDeathCallBack.EVENT.register((player, damageSource) -> {
            SENDERS.get(player).ifPresent(deadManRequest -> {
                deadManRequest.cancel("cmd.tpa.cancel_due_to_death");
                resetCooledTime(player);
            });
            return ActionResult.PASS;
        });

        PlayerHurtCallBack.EVENT.register((player, damageSource, amount) -> {
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
            if (COOLED_DOWN.containsKey(player)) {
                System.out.println("contains");
                COOLED_DOWN.get(player).pause();
            }
            SENDERS.get(player).ifPresent(TPARequest::cancel);
            return ActionResult.PASS;
        });

        PlayerSpawnCallBack.EVENT.register(player -> {
            if (COOLED_DOWN.containsKey(player)) {
                COOLED_DOWN.get(player).resume();
            }
            return ActionResult.PASS;
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

    public static void setCooledTime(PlayerEntity player, long cooledDown) {
        TickerTask.DelayTask task = TickerTask.delay(() -> {
            COOLED_DOWN.remove(player);
            player.sendMessage(Text.translatable("cmd.tpa.cooled_down_reset").formatted(Formatting.GREEN), false);
        }, cooledDown);
        COOLED_DOWN.put(player, task);
        Ticker.schedule(task);
    }

    public static long getRemainingCooledTime(PlayerEntity player) {
        if (!COOLED_DOWN.containsKey(player)) return 0;
        return COOLED_DOWN.get(player).getRemainingTicks() / 20;
    }

    public static boolean isCooledDown(PlayerEntity player) {
        return COOLED_DOWN.containsKey(player);
    }

    public static boolean resetCooledTime(PlayerEntity player) {
        if (!COOLED_DOWN.containsKey(player)) return false;
        COOLED_DOWN.get(player).cancel();
        COOLED_DOWN.remove(player);
        return true;
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
