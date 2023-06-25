package net.pineclone.simplecmd.cmds.tpa.status;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.pineclone.simplecmd.utils.Ticker;
import net.pineclone.simplecmd.utils.TickerTask;
import net.pineclone.simplecmd.utils.TomlUtils;

public class PreTeleport extends TPARequestStatus {

    private TickerTask.TickableTask task;
    private Vec3d pos;

    public PreTeleport(TPARequest request) {
        super(request);
    }

    @Override
    public void execute() {
        long wait = MathHelper.clamp(TomlUtils.modToml.getLong("cmd.tpaaccept.time_before_teleporting"), 0, 20);
        PlayerEntity sender = request.getSender();
        PlayerEntity receiver = request.getReceiver();
        if (wait == 0) {
            request.update(new Teleporting(request));
        }

        sender.sendMessage(Text.translatable("cmd.tpaaccpet.sender_has_been_accepted", wait).formatted(Formatting.BLUE));
        receiver.sendMessage(Text.translatable("cmd.tpaaccept.receiver_has_accepted", wait).formatted(Formatting.BLUE));


        Boolean moveBreak = TomlUtils.modToml.getBoolean("cmd.tpa.move_break_tpa");
        if (moveBreak) {
            pos = sender.getPos();
            task = TickerTask.polling(() -> {
                if (!sender.getPos().equals(pos) && request.getStatus() instanceof PreTeleport)
                    request.cancel("cmd.tpaaccept.cancel_due_to_move");
            }, wait * 20, 10).onCanceled(() -> request.update(new Teleporting(request)));
            Ticker.schedule(task);
            return;
        }

        task = TickerTask.delay(() -> request.update(new Teleporting(request)), wait * 20);
        Ticker.schedule(task);

    }

    @Override
    public void halt() {
        if (task != null)
            task.cancel();
    }
}
