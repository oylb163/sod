package edu.sc.seis.sod.status;

import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils;

public class StringTreeBranch extends StringTree {

    public StringTreeBranch(Object actor, boolean boo, StringTree branch) {
        this(actor, boo, new StringTree[] { branch });
    }

    public StringTreeBranch(Object actor, boolean boo, StringTree[] branches) {
        this(ExceptionReporterUtils.getClassName(actor), boo, branches);
    }
    
    public StringTreeBranch(String actorName, boolean status, StringTree[] branches){
        super(actorName, status);
        this.branches = branches;
    }

    public String toString() {
        String s = super.toString()+" (";
        for (int i = 0; i < branches.length; i++) {
            if (branches[i] != null) {
                s += branches[i].toString();
            } else {
                s += "null";
            }
            if (i != branches.length-1) {
                s += ", ";
            }
        }
        return s += ")";
    }

    protected StringTree[] branches;
}


