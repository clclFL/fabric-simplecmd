package net.pineclone.simplecmd.cmds.tpa.status;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Cancel extends TPARequestStatus {

    private String reason;

    public Cancel(TPARequest request) {
        super(request);
    }

    public Cancel(TPARequest request, String reason) {
        super(request);
        this.reason = reason;
    }

    @Override
    public void execute() {
        if (reason == null) {
            request.getSender().sendMessage(Text.translatable("cmd.tpa.cancel")
                    .formatted(Formatting.RED), false);
            return;
        }
        request.getSender().sendMessage(Text.translatable(reason).formatted(Formatting.RED));
    }

    @Override
    public void halt() {

    }
}
