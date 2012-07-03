package edu.sc.seis.sod.source.network;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.InstrumentationImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.fissuresUtil.stationxml.ChannelSensitivityBundle;
import edu.sc.seis.fissuresUtil.stationxml.StationChannelBundle;
import edu.sc.seis.fissuresUtil.stationxml.StationXMLToFissures;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.seisFile.stationxml.Channel;
import edu.sc.seis.seisFile.stationxml.Epoch;
import edu.sc.seis.seisFile.stationxml.Network;
import edu.sc.seis.seisFile.stationxml.NetworkIterator;
import edu.sc.seis.seisFile.stationxml.Response;
import edu.sc.seis.seisFile.stationxml.StaMessage;
import edu.sc.seis.seisFile.stationxml.Station;
import edu.sc.seis.seisFile.stationxml.StationEpoch;
import edu.sc.seis.seisFile.stationxml.StationIterator;
import edu.sc.seis.seisFile.stationxml.StationXMLException;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;


public class StationXML implements NetworkSource {
    
    public StationXML(Element config) throws ConfigurationException {
        if (DOMHelper.hasElement(config, URL_ELEMENT)) {
            url = SodUtil.getNestedText(SodUtil.getElement(config, URL_ELEMENT));
            try {
                parsedURL = new URI(url);
                List<String> split = new ArrayList<String>();
                String[] splitArray = parsedURL.getQuery().split("&");
                for (String s : splitArray) {
                    String[] nvSplit = s.split("=");
                    if ( ! nvSplit[0].equals("level")) {
                        // zap level as we do that ourselves
                        split.add(s);
                    }
                }
                String newQuery = "";
                boolean first = true;
                for (String s : split) {
                    if (!first) {
                        newQuery += "&";
                    }
                    newQuery += s;
                    first = false;
                }
                parsedURL = new URI(parsedURL.getScheme(), parsedURL.getUserInfo(), parsedURL.getHost(), parsedURL.getPort(), parsedURL.getPath(), newQuery, parsedURL.getFragment());
                url = parsedURL.toURL().toString();
            } catch(URISyntaxException e) {
                throw new ConfigurationException("Invalid <url> element found.", e);
            } catch(MalformedURLException e) {
                throw new ConfigurationException("Bad URL", e);
            }
        } else {
            throw new ConfigurationException("No <url> element found");
        }
        if(DOMHelper.hasElement(config, AbstractNetworkSource.REFRESH_ELEMENT)) {
            refreshInterval = SodUtil.loadTimeInterval(SodUtil.getElement(config, AbstractNetworkSource.REFRESH_ELEMENT));
        } else {
            refreshInterval = new TimeInterval(1, UnitImpl.FORTNIGHT);
        }
    }

    
    public String getDNS() {
        return url;
    }

    public String getName() {
        return this.getClass().getName();
    }

    public TimeInterval getRefreshInterval() {
        return refreshInterval;
    }

    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        return new CacheNetworkAccess(null, attr);
    }

    public List<? extends CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound {
        throw new NetworkNotFound();
    }

    public List<? extends NetworkAttrImpl> getNetworks() {
        checkNetsLoaded();
        return Collections.unmodifiableList(networks);
    }

    public List<? extends StationImpl> getStations(NetworkId net) {
        checkChansLoaded(NetworkIdUtil.toStringNoDates(net));
        List<StationChannelBundle> bundles = staChanMap.get(NetworkIdUtil.toStringNoDates(net));
        List<StationImpl> out = new ArrayList<StationImpl>();
        for (StationChannelBundle b : bundles) {
            out.add(b.getStation());
        }
        return out;
    }

    public List<? extends ChannelImpl> getChannels(StationImpl station) {
        checkChansLoaded(NetworkIdUtil.toStringNoDates(station.getNetworkAttr()));
        List<StationChannelBundle> bundles = staChanMap.get(NetworkIdUtil.toStringNoDates(station.getNetworkAttr()));
        for (StationChannelBundle b : bundles) {
            if (StationIdUtil.areEqual(station, b.getStation())) {
                List<ChannelImpl> out = new ArrayList<ChannelImpl>();
                for (ChannelSensitivityBundle chanSens : b.getChanList()) {
                    out.add(chanSens.getChan());
                }
                return out;
            }
        }
        return new ArrayList<ChannelImpl>();
    }

    public QuantityImpl getSensitivity(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        checkChansLoaded(NetworkIdUtil.toStringNoDates(chanId.network_id));
        List<StationChannelBundle> bundles = staChanMap.get(NetworkIdUtil.toStringNoDates(chanId.network_id));
        for (StationChannelBundle b : bundles) {
            if (chanId.station_code.equals( b.getStation().get_code())) {
                for (ChannelSensitivityBundle chanSens : b.getChanList()) {
                    if (ChannelIdUtil.areEqual(chanId, chanSens.getChan().get_id()) && chanSens.getSensitivity() != null) {
                        return chanSens.getSensitivity();
                    }
                }
            }
        }
        throw new ChannelNotFound();
    }

    public Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        MicroSecondDate chanBegin = new MicroSecondDate(chanId.begin_time);
        String newQuery = "net="+chanId.network_id.network_code+
                "&sta="+chanId.station_code+
                "&loc="+chanId.site_code+
                "&chan="+chanId.channel_code+
                "&timewindow="+toDateString(chanBegin)+
                ","+toDateString(chanBegin.add(ONE_DAY));
        try {
            URI chanUri = new URI(parsedURL.getScheme(),
                                  parsedURL.getUserInfo(),
                                  parsedURL.getHost(),
                                  parsedURL.getPort(),
                                  parsedURL.getPath(),
                                  newQuery,
                                  parsedURL.getFragment());
            StaMessage sm = retrieveXML(chanUri, "resp");
            NetworkIterator netIt = sm.getNetworks();
            while (netIt.hasNext()) {
                Network n = netIt.next();
                StationIterator staIt = n.getStations();
                while (staIt.hasNext()) {
                    Station s = staIt.next();
                    for (StationEpoch se : s.getStationEpochs()) {
                        for (Channel c : se.getChannelList()) {
                            for (Epoch e : c.getChanEpochList()) {
                                List<Response> r = e.getResponseList();
                                InstrumentationImpl inst = StationXMLToFissures.convertInstrumentation(e);
                                if (RangeTool.areOverlapping(new MicroSecondTimeRange(inst.effective_time),
                                                             new MicroSecondTimeRange(chanBegin.add(ONE_SECOND), chanBegin.add(ONE_DAY)))) {
                                    return inst;
                                }
                                logger.debug("Skipping as wrong start time "+ChannelIdUtil.toString(chanId)+" "+inst.effective_time.start_time.date_time+" "+inst.effective_time.end_time.date_time);
                            }
                        }
                    }
                    
                }
            }
            
        } catch(URISyntaxException e) {
            throw new InvalidResponse("StationXML URL is not valid, should not happen but it did.", e);
        } catch(XMLStreamException e) {
            throw new InvalidResponse("Problem getting response via stationxml.", e);
        } catch(StationXMLException e) {
            throw new InvalidResponse("Problem getting response via stationxml.", e);
        } catch(IOException e) {
            throw new InvalidResponse("Problem getting response via stationxml.", e);
        }
        
        throw new ChannelNotFound();
    }
    

    synchronized void checkNetsLoaded() {
        if (networks == null) {
            try {
                parseNets();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    synchronized void checkChansLoaded(String netCode) {
        if (staChanMap.get(netCode) == null) {
            checkNetsLoaded();
            try {
                parse();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    static StaMessage retrieveXML(URI u, String level) throws XMLStreamException, StationXMLException, IOException, URISyntaxException  {
        URI chanUri = new URI(u.getScheme(),
                              u.getUserInfo(),
                              u.getHost(),
                              u.getPort(),
                              u.getPath(),
                              u.getQuery()+"&level="+level,
                              u.getFragment());

        logger.info("Retrieve from "+chanUri);
        InputStream in  = new BufferedInputStream(chanUri.toURL().openStream());

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader r = factory.createXMLEventReader(in);
        XMLEvent e = r.peek();
        while(! e.isStartElement()) {
            e = r.nextEvent(); // eat this one
            e = r.peek();  // peek at the next
        }
        return new StaMessage(r);
    }
    
    synchronized void parseNets() throws XMLStreamException, StationXMLException, IOException, URISyntaxException {
        networks = new ArrayList<NetworkAttrImpl>();
        logger.info("Parsing networks from "+parsedURL);
        StaMessage staMessage = retrieveXML(parsedURL, "net");
        
        NetworkIterator netIt = staMessage.getNetworks();
        while (netIt.hasNext()) {
            Network net = netIt.next();
            networks.add(StationXMLToFissures.convert(net));
            StationIterator it = net.getStations(); // should be empty, but just make sure
            while(it.hasNext()) {
                Station s = it.next();
            }
        }
        staMessage.closeReader();
        logger.info("found "+networks.size()+" networks after parse");
    }
    
    synchronized void parse() throws XMLStreamException, StationXMLException, IOException, URISyntaxException {
        staChanMap.clear();
        int numChannels = 0;
        logger.info("Parsing channels from "+parsedURL);
        StaMessage staMessage = retrieveXML(parsedURL, "chan");
        lastLoadDate = staMessage.getSentDate();
        NetworkIterator netIt = staMessage.getNetworks();
        while (netIt.hasNext()) {
            Network net = netIt.next();
            String key = NetworkIdUtil.toStringNoDates(StationXMLToFissures.convert(net).getId());
            if ( ! staChanMap.containsKey(key)) {
                staChanMap.put(key, new ArrayList<StationChannelBundle>());
            }
            StationIterator it = net.getStations();
            while(it.hasNext()) {
                Station s = it.next();
                try {
                    numChannels += processStation(networks, s);
                } catch (StationXMLException ee) {
                    logger.error("Skipping "+s.getNetCode()+"."+s.getStaCode()+" "+ ee.getMessage());
                }
            }
        }
        logger.info("found "+ numChannels+" channels in "+networks.size()+" networks after parse ");
    }
    
    int processStation(List<NetworkAttrImpl> netList, Station s) throws StationXMLException {
        int numChannels = 0;
        for (String ignore : ignoreNets) {
            if (s.getNetCode().equals(ignore)) {
            // not sure what AB network is, skip it for now
            return 0;
            }
        }
        List<StationChannelBundle> bundles = StationXMLToFissures.convert(s, netList, true);
        for (StationChannelBundle b : bundles) {
            String staKey = NetworkIdUtil.toStringNoDates(b.getStation().getNetworkAttr());
            if ( ! staChanMap.containsKey(staKey)) {
                staChanMap.put(staKey, new ArrayList<StationChannelBundle>());
            }
            staChanMap.get(staKey).add(b);
            numChannels += b.getChanList().size();
        }
        return numChannels;
    }
    
    public static String toDateString(MicroSecondDate msd) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(msd);
    }
    
    static String[] ignoreNets = new String[] {"AB", "AI", "BN"};
    
    List<NetworkAttrImpl> knownNetworks = new ArrayList<NetworkAttrImpl>();;
    
    List<NetworkAttrImpl> networks;
    
    Map<String, List<StationChannelBundle>> staChanMap = new HashMap<String, List<StationChannelBundle>>();
    
    String url;
    
    URI parsedURL;
    
    TimeInterval refreshInterval;
    
    String lastLoadDate;

    public static final TimeInterval ONE_SECOND = new TimeInterval(1, UnitImpl.SECOND);
    
    public static final TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StationXML.class);
    
    public static final String URL_ELEMENT = "url";
}
