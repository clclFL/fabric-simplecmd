package net.pineclone.simplecmd.cmds.tpa.status;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class Teleporting extends TPARequestStatus {

    public Teleporting(TPARequest request) {
        super(request);
    }

    @Override
    public void execute() {
        if (!(request.getReceiver() instanceof ServerPlayerEntity receiver)
                || !(request.getSender() instanceof ServerPlayerEntity sender)) {
            request.cancel("cmd.tpa.unknown_exception_occure");
            return;
        }

        Vec3d des = receiver.getPos();
        sender.teleport(des.x, des.y, des.z, true);
        sender.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        request.close();
    }

    @Override
    public void halt() {
        //void
    }
}
