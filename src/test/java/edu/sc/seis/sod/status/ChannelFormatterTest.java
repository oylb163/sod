package edu.sc.seis.sod.status;



import java.text.SimpleDateFormat;
import java.util.TimeZone;

import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.XMLConfigUtil;

public class ChannelFormatterTest extends TestCase{
    public ChannelFormatterTest(String name){ super(name); }

    public void setUp(){
        chan = MockChannel.createChannel();
    }

    public void testStationCode(){
        assertEquals(chan.get_id().station_code, create("<stationCode/>").getResult(chan));
    }

    public void testNetworkCode(){
        assertEquals(chan.get_id().network_id.network_code,
                     create("<networkCode/>").getResult(chan));
    }

    public void testSiteCode(){
        assertEquals(chan.get_id().site_code, create("<siteCode/>").getResult(chan));
    }

    public void testBeginTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(ISOTime.UTC);
        ChannelFormatter cf = create("<beginTime>HH:mm</beginTime>");
        assertEquals(sdf.format(new MicroSecondDate(chan.get_id().begin_time)),
                     cf.getResult(chan));
    }

    public void testName(){
        assertEquals("Vertical Channel", create("<name/>").getResult(chan));
    }

    private ChannelFormatter create(String config){
        try {
            return new ChannelFormatter(XMLConfigUtil.parse(open + config + close));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String open = "<channelFormat>";

    private String close = "</channelFormat>";

    private Channel chan;
}
