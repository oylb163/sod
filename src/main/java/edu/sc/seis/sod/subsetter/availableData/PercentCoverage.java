package edu.sc.seis.sod.subsetter.availableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.MicroSecondTimeRange;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.time.CoverageTool;
import edu.sc.seis.sod.util.time.ReduceTool;

public class PercentCoverage implements AvailableDataSubsetter {

    private double percentage;

    public PercentCoverage(Element content) {
        DOMHelper.extractDouble(content, ".", 100);
    }

    public PercentCoverage(double percentage) {
        this.percentage = percentage;
    }

    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             CookieJar cookieJar) {
        return new StringTreeLeaf(this, accept(request, available));
    }

    public boolean accept(RequestFilter[] original, RequestFilter[] available) {
        return percentCovered(original, available) >= percentage;
    }

    public double percentCovered(RequestFilter[] request,
                                 RequestFilter[] available) {
        RequestFilter[] uncovered = CoverageTool.notCovered(request, available);
        TimeInterval totalOriginalTime = sum(toMSTR(request));
        TimeInterval totalUncoveredTime = sum(toMSTR(uncovered));
        return (1 - totalUncoveredTime.divideBy(totalOriginalTime).getValue()) * 100;
    }

    private TimeInterval sum(List microSecondTimeRanges) {
        TimeInterval total = new TimeInterval(0, UnitImpl.SECOND);
        for(Iterator iter = microSecondTimeRanges.iterator(); iter.hasNext();) {
            MicroSecondTimeRange time = (MicroSecondTimeRange)iter.next();
            total = total.add(time.getInterval());
        }
        return total;
    }

    private List<MicroSecondTimeRange> toMSTR(RequestFilter[] filters) {
        // Ensure that there are no overlaps in the request filters
        filters = ReduceTool.merge(filters);
        List<MicroSecondTimeRange> mstrs = new ArrayList<MicroSecondTimeRange>(filters.length);
        for(int i = 0; i < filters.length; i++) {
            mstrs.add(new MicroSecondTimeRange(filters[i]));
        }
        return mstrs;
    }
}
