package net.pineclone.simplecmd.cmds.tpa.status;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.pineclone.simplecmd.utils.TomlUtils;

public class Close extends TPARequestStatus {

    public Close(TPARequest request) {
        super(request);
    }

    @Override
    public void execute() {
        request.getSender().sendMessage(Text.translatable("cmd.tpaaccept.start_teleporting", request.getReceiver().getDisplayName().getString())
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        request.getReceiver().sendMessage(Text.translatable("cmd.tpaaccept.done_teleporting", request.getSender().getDisplayName().getString())
                .formatted(Formatting.GOLD , Formatting.BOLD) , false);

        long coldDown = MathHelper.clamp(TomlUtils.modToml.getLong("cmd.tpa.max_cooled_time"), 0, 3600);
        if (coldDown != 0) {
            TPAManager.setCooledTime(request.getSender() , coldDown);
        }
    }

    @Override
    public void halt() {
        //void
    }
}
