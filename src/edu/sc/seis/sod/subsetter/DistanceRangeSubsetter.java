package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/**
 * DistanceRange.java
 *
 *
 * Created: Mon Apr  8 16:09:49 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DistanceRangeSubsetter implements SodElement{
    public DistanceRangeSubsetter (Element config) throws ConfigurationException{
        processConfig(config);
    }

    public void processConfig(Element config) throws ConfigurationException{
        unitRange = SodUtil.loadUnitRange(config);
    }

    public edu.iris.Fissures.UnitRange  getDistanceRange() { return unitRange; }

    public QuantityImpl getMinDistance() {
        return new QuantityImpl(getDistanceRange().min_value,
                                getDistanceRange().the_units);
    }

    public QuantityImpl getMaxDistance() {
        return new QuantityImpl(getDistanceRange().max_value,
                                getDistanceRange().the_units);
    }

    private edu.iris.Fissures.UnitRange unitRange;
}// DistanceRange
