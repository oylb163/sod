package edu.sc.seis.sod.subsetter;


import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.XMLConfigUtil;
import edu.sc.seis.sod.subsetter.MockFissures;
import java.text.SimpleDateFormat;
import junit.framework.TestCase;

public class ChannelFormatterTest extends TestCase{
    public ChannelFormatterTest(String name){ super(name); }
    
    public void setUp(){
        chan = MockFissures.createChannel();
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
        ChannelFormatter cf = create("<beginTime>HH:mm</beginTime>");
        assertEquals(sdf.format(new MicroSecondDate(chan.get_id().begin_time)),
                     cf.getResult(chan));
    }
    
    public void testOrientation(){
        assertEquals(chan.an_orientation.azimuth + " az " + chan.an_orientation.dip + " dip",
                     create("<orientation/>").getResult(chan));
    }
    
    public void testName(){
        assertEquals("Test Channel", create("<name/>").getResult(chan));
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
