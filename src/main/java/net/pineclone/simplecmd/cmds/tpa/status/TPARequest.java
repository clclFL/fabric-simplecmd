package net.pineclone.simplecmd.cmds.tpa.status;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

public class TPARequest {

    private TPARequestStatus status;
    private final PlayerEntity sender;
    private final PlayerEntity receiver;

    protected TPARequest(@NotNull PlayerEntity sender,
                      @NotNull PlayerEntity receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public PlayerEntity getSender() {
        return sender;
    }

    public PlayerEntity getReceiver() {
        return receiver;
    }

    public TPARequestStatus getStatus() {
        return status;
    }

    public void activate() {
        //to avoid a same task being activated over once.
        if (this.status == null) {
            status = new Nascent(this);
            status.execute();
        }
    }

    public void update(TPARequestStatus status) {
        this.status.halt();
        this.status = status;
        status.execute();
    }

    /**
     * Only the three status below will the tpa manager remove the request from the map.
     */
    public void timeout() {
        status.halt();
        status = new Timeout(this);
        status.execute();
        TPAManager.removeRequest(this);
    }

    public void cancel() {
        status.halt();
        status = new Cancel(this);
        status.execute();
        TPAManager.removeRequest(this);
    }

    public void close() {
        status.halt();
        status = new Close(this);
        status.execute();
        TPAManager.removeRequest(this);
    }

    public void cancel(String reason) {
        status.halt();
        status = new Cancel(this, reason);
        status.execute();
        TPAManager.removeRequest(this);
    }
}

