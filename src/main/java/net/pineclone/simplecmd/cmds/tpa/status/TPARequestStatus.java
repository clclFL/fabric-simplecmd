package net.pineclone.simplecmd.cmds.tpa.status;

public abstract class TPARequestStatus {

    protected final TPARequest request;

    public TPARequestStatus(TPARequest request) {
        this.request = request;
    }

    public abstract void execute();

    public abstract void halt();

}
