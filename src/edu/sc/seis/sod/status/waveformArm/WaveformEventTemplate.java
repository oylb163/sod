package edu.sc.seis.sod.status.waveFormArm;

import edu.sc.seis.sod.status.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.waveFormArm.WaveFormStatus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class WaveformEventTemplate extends FileWritingTemplate implements WaveFormStatus{
    public WaveformEventTemplate(Element el, EventAccessOperations event) throws Exception {
        this(TemplateFileLoader.getTemplate(el),
             el.getAttribute("outputLocation"),
             event);
    }

    public WaveformEventTemplate(Element el, String outputLocation, EventAccessOperations event) throws IOException {
        super(outputLocation);
        this.event = event;
        parse(el);
        if(Start.getWaveformArm() != null) Start.getWaveformArm().addStatusMonitor(this);
        write();
    }

    public void update(EventChannelPair ecp) throws Exception {
        if(ecp.getEvent().equals(event)){
            Iterator it = waveformStatusListeners.iterator();
            while(it.hasNext()) ((WaveFormStatus)it.next()).update(ecp);
            it = channelListeners.iterator();
            while(it.hasNext()) ((ChannelGroupTemplate)it.next()).change(ecp.getChannel(),
                                                                         RunStatus.translate(ecp.getStatus()));
            write();
        }
    }

    protected Object getTemplate(String tag, Element el) {
        if(tag.equals("channels")) {
            ChannelGroupTemplate cgt = new ChannelGroupTemplate(el);
            channelListeners.add(cgt);
            return cgt;
        }
        if(tag.equals("map")){
            MapWaveFormStatus map = new MapWaveFormStatus(getOutputDirectory() + "/map.png", pool);
            map.add(event);
            waveformStatusListeners.add(map);
            map.write();
            return new GenericTemplate(){
                public String getResult() { return "map.png";}
            };
        }
        if(tag.equals("event")){  return new EventTemplate(el); }
        return super.getTemplate(tag, el);
    }

    private class EventTemplate implements GenericTemplate{
        public EventTemplate(Element el){ formatter = new EventFormatter(el); }

        public String getResult() { return formatter.getResult(event); }

        private EventFormatter formatter;
    }

    private EventAccessOperations event;

    private static MapPool pool = new MapPool(2);

    private List waveformStatusListeners = new ArrayList();

    private List channelListeners = new ArrayList();
}
