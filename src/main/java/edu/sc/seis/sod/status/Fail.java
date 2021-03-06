package edu.sc.seis.sod.status;

public class Fail extends StringTreeLeaf {

    public Fail(Object actor) {
        this(actor, "");
    }

    public Fail(Object actor, String reason) {
        super(actor, false, reason);
    }

    public Fail(Object actor, String reason, Throwable t) {
        super(actor, false, reason, t);
    }
}
