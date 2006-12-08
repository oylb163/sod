package edu.sc.seis.sod.subsetter.site;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;

public final class SiteOR extends SiteLogicalSubsetter implements SiteSubsetter {

    public SiteOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean shouldContinue(StringTree result) {
        return !result.isSuccess();
    }

    public boolean isSuccess(StringTree[] reasons) {
        for(int i = 0; i < reasons.length; i++) {
            if(reasons[i].isSuccess()){
                return true;
            }
        }
        return false;
    }
}// SiteOR
