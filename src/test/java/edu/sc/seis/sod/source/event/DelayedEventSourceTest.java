package edu.sc.seis.sod.source.event;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;

public class DelayedEventSourceTest {

    public static final TimeInterval LONG_AGO = new TimeInterval(1, UnitImpl.YEAR);

    public static final TimeInterval SHORT_AGO = new TimeInterval(.1, UnitImpl.SECOND);

    public static final TimeInterval MED_SHORT_AGO = new TimeInterval(.2, UnitImpl.SECOND);

    @Test
    public void testNext() throws InterruptedException, NoPreferredOrigin {
        final List<CacheEvent> events = new ArrayList<CacheEvent>();
        events.add(MockEventAccessOperations.createEvent(ClockUtil.now().subtract(LONG_AGO), 0f, 0));
        events.add(MockEventAccessOperations.createEvent(ClockUtil.now().subtract(LONG_AGO), 1f, 0));
        events.add(MockEventAccessOperations.createEvent(ClockUtil.now().subtract(SHORT_AGO), 2f, 0));
        EventSource es = new TestSimpleEventSource(events);
        DelayedEventSource delayedES = new DelayedEventSource(MED_SHORT_AGO, es);
        CacheEvent[] firstEvents = delayedES.next();
        assertEquals("first get", 2, firstEvents.length);
        Thread.sleep((long)MED_SHORT_AGO.getValue(UnitImpl.MILLISECOND));
        CacheEvent[] secondEvents = delayedES.next();
        assertEquals("second get", 1, secondEvents.length);
        assertEquals("lat", 2, secondEvents[0].get_preferred_origin().getLocation().latitude, 0.000001);
    }

    @Test
    public void testGetWaitForNext() {
        final List<CacheEvent> events = new ArrayList<CacheEvent>();
        events.add(MockEventAccessOperations.createEvent(ClockUtil.now().subtract(LONG_AGO), 0f, 0));
        events.add(MockEventAccessOperations.createEvent(ClockUtil.now().subtract(LONG_AGO), 1f, 0));
        events.add(MockEventAccessOperations.createEvent(ClockUtil.now().subtract(SHORT_AGO), 2f, 0));
        EventSource es = new TestSimpleEventSource(events);
        DelayedEventSource delayedES = new DelayedEventSource(MED_SHORT_AGO, es);
        CacheEvent[] firstEvents = delayedES.next();
        TimeInterval wait = delayedES.getWaitBeforeNext();
        assertTrue("wait less than MED "+wait, wait.lessThan(MED_SHORT_AGO));
    }
}

class TestSimpleEventSource implements EventSource {

    boolean first = true;

    List<CacheEvent> events;

    public TestSimpleEventSource(List<CacheEvent> events) {
        this.events = events;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void appendToName(String suffix) {}

    @Override
    public boolean hasNext() {
        return first;
    }

    @Override
    public CacheEvent[] next() {
        if (first) {
            return events.toArray(new CacheEvent[0]);
        }
        return null;
    }

    @Override
    public TimeInterval getWaitBeforeNext() {
        return DelayedEventSourceTest.MED_SHORT_AGO;
    }

    @Override
    public MicroSecondTimeRange getEventTimeRange() {
        return null;
    }

    @Override
    public String getDescription() {
        return "fake es";
    }

    @Override
    public int getRetries() {
        // TODO Auto-generated method stub
        return 0;
    }
};