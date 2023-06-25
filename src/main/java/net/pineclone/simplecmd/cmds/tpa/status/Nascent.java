package net.pineclone.simplecmd.cmds.tpa.status;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.pineclone.simplecmd.utils.Ticker;
import net.pineclone.simplecmd.utils.TickerTask;
import net.pineclone.simplecmd.utils.TomlUtils;

public class Nascent extends TPARequestStatus {

    private TickerTask.DelayTask task;

    public Nascent(TPARequest request) {
        super(request);
    }

    @Override
    public void execute() {
        PlayerEntity sender = request.getSender();
        PlayerEntity receiver = request.getReceiver();
        //send successfully create tpa request message to the sender.
        sender.sendMessage(Text.translatable("cmd.tpa.create", receiver.getDisplayName().getString())
                .formatted(Formatting.GOLD), false);

        //send receive tpa request from player to the receiver.
        receiver.sendMessage(Text.translatable("cmd.tpa.player.popup.msg.for.request.1",
                sender.getDisplayName().getString()).formatted(Formatting.GOLD), false);
        receiver.sendMessage(Text.translatable("cmd.tpa.player.popup.msg.for.request.2").formatted(Formatting.GOLD), false);
        receiver.sendMessage(Text.translatable("cmd.tpa.player.popup.msg.for.request.3").formatted(Formatting.GOLD), false);
        receiver.sendMessage(Text.translatable("cmd.tpa.player.popup.msg.for.request.4", TomlUtils.modToml.getLong("cmd.tpa.max_wait_time"))
                .formatted(Formatting.GOLD), false);

        //if the task wait too long, then cancel this request.
        long wait = MathHelper.clamp(TomlUtils.modToml.getLong("cmd.tpa.max_wait_time"), 10, 1000) * 20;
        task = TickerTask.delay(request::timeout, wait);
        Ticker.schedule(task);
    }

    @Override
    public void halt() {
        if (task != null)
            task.cancel();
    }

}
