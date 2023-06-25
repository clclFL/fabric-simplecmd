package net.pineclone.simplecmd.cmds.tpa.status;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.pineclone.simplecmd.utils.TomlUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Teleporting extends TPARequestStatus {

    private static final ConcurrentHashMap<PlayerEntity, TPARequest> PRE_TELE_PLAYER = new ConcurrentHashMap<>();
    private static final long DELAY_BEFORE = MathHelper.clamp(TomlUtils.modToml.getLong("cmd.tpaaccept.time_before_teleporting"), 0, 20) * 20;
    private static final boolean moveBreak = TomlUtils.modToml.getBoolean("cmd.tpa.move_break_tpa");

    private Vec3d pos;
    private long when;
    private boolean hasInit;
    private boolean hasTeleport;
    private final ReentrantLock lock = new ReentrantLock();

    public Teleporting(TPARequest request) {
        super(request);
    }

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (PRE_TELE_PLAYER.isEmpty()) return;
            PRE_TELE_PLAYER.forEach((p, r) -> {
                if (!(r.getStatus() instanceof Teleporting pre)) {
                    PRE_TELE_PLAYER.remove(p);
                    return;
                }

                if (moveBreak) {
                    if (!pre.getPos().equals(p.getPos())) {
                        r.cancel("cmd.tpaaccept.cancel_due_to_move");
                        return;
                    }
                }

                pre.check(server.getTicks());
            });
        });
    }

    @Override
    public void execute() {
        PlayerEntity sender = request.getSender();
        PlayerEntity receiver = request.getReceiver();
        if (DELAY_BEFORE == 0) {
            lock.lock();
            try {
                if (hasTeleport) return;
                Vec3d des = receiver.getPos();
                sender.teleport(des.x, des.y, des.z, true);
                sender.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                hasTeleport = true;
                request.close();
                return;
            } finally {
                lock.unlock();
            }
        }
        pos = sender.getPos();
        sender.sendMessage(Text.translatable("cmd.tpaaccpet.sender_has_been_accepted", DELAY_BEFORE / 20).formatted(Formatting.BLUE));
        receiver.sendMessage(Text.translatable("cmd.tpaaccept.receiver_has_accepted", DELAY_BEFORE / 20).formatted(Formatting.BLUE));
        PRE_TELE_PLAYER.put(sender, request);
    }

    public Vec3d getPos() {
        return pos;
    }

    public void check(long ticks) {
        if (!hasInit) {
            when = ticks;
            hasInit = true;
        }

        if (ticks > DELAY_BEFORE + when) {
            lock.lock();
            try {
                if (hasTeleport) return;
                PlayerEntity sender = request.getSender();
                PlayerEntity receiver = request.getReceiver();

                Vec3d des = receiver.getPos();
                sender.teleport(des.x, des.y, des.z, true);
                sender.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                hasTeleport = true;
                request.close();
            } finally {
                lock.unlock();
            }
        }

    }

    @Override
    public void halt() {
        PRE_TELE_PLAYER.remove(request.getSender());
    }
}
