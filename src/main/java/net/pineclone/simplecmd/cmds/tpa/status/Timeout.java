package net.pineclone.simplecmd.cmds.tpa.status;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Timeout extends TPARequestStatus {

    public Timeout(TPARequest request) {
        super(request);
    }

    @Override
    public void execute() {
        request.getSender().sendMessage(Text.translatable("cmd.tpa.wait_timeout")
                .formatted(Formatting.RED), false);
    }

    @Override
    public void halt() {

    }
}
