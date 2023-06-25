package net.pineclone.simplecmd.cmds.tpa.status;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;

public class TPARequest {

    private TPARequestStatus status;
    private final PlayerEntity sender;
    private final PlayerEntity receiver;
    private boolean isClosed;
    private final ReentrantLock lock = new ReentrantLock();

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
        lock.lock();
        try {
            if (isClosed) return;
            //to avoid a same task being activated over once.
            if (this.status == null) {
                status = new Nascent(this);
                status.execute();
            }
        } finally {
            lock.unlock();
        }
    }

    public void update(TPARequestStatus status) {
        lock.lock();
        try {
            if (isClosed) return;
            this.status.halt();
            this.status = status;
            status.execute();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Only the three status below will the tpa manager remove the request from the map.
     */
    public void timeout() {
        lock.lock();
        try {
            if (isClosed) return;
            status.halt();
            status = new Timeout(this);
            status.execute();
            TPAManager.removeRequest(this);
            isClosed = true;
        } finally {
            lock.unlock();
        }
    }

    public void cancel() {
        lock.lock();
        try {
            if (isClosed) return;
            status.halt();
            status = new Cancel(this);
            status.execute();
            TPAManager.removeRequest(this);
            isClosed = true;
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        lock.lock();
        try {
            if (isClosed) return;
            status.halt();
            status = new Close(this);
            status.execute();
            TPAManager.removeRequest(this);
            isClosed = true;
        } finally {
            lock.unlock();
        }
    }

    public void cancel(String reason) {
        lock.lock();
        try {
            if (isClosed) return;
            status.halt();
            status = new Cancel(this, reason);
            status.execute();
            TPAManager.removeRequest(this);
            isClosed = true;
        } finally {
            lock.unlock();
        }
    }
}

