/**
 * NetworkTemplateGenerator.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.status.networkArm;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.NetworkFormatter;
import edu.sc.seis.sod.status.StationFormatter;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.status.networkArm.ChannelsInStationTemplate;
import edu.sc.seis.sod.status.networkArm.NetworkArmMonitor;
import edu.sc.seis.sod.status.networkArm.StationsInNetworkTemplate;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.sc.seis.sod.status.FileWritingTemplate;

public class NetworkInfoTemplateGenerator implements NetworkArmMonitor {
    private String fileDir, netsOutputFileName, stasOutputFileName, sitesOutputFileName, chansOutputFileName;
    private NetworkFormatter netFormatter;
    private StationFormatter staFormatter;
    private NetworkStatusTemplate netTemplate;
    private HashMap stationTemplates = new HashMap(); //station templates by network id string
    private HashMap channelTemplates = new HashMap(); //channel templates by station id string
    private Logger logger = Logger.getLogger(NetworkInfoTemplateGenerator.class);
    private Element netConfig, staConfig, siteConfig, chanConfig;

    public NetworkInfoTemplateGenerator(Element el) throws Exception{
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("fileDir")){
                fileDir = n.getFirstChild().getNodeValue();
            }
            else if (n.getNodeName().equals("netConfig")){
                netConfig = TemplateFileLoader.getTemplate((Element)n);

                Node tmpEl = SodUtil.getElement(netConfig, "filename");
                netsOutputFileName = tmpEl.getFirstChild().getNodeValue();
                logger.debug("netsOutputFileName = " + netsOutputFileName);
                netConfig.removeChild(tmpEl);
            }
            else if (n.getNodeName().equals("stationConfig")){
                staConfig = TemplateFileLoader.getTemplate((Element)n);

                Node tmpEl = SodUtil.getElement(staConfig, "outputLocation");
                netFormatter = new NetworkFormatter((Element)tmpEl);
                staConfig.removeChild(tmpEl);

                tmpEl = SodUtil.getElement(staConfig, "filename");
                stasOutputFileName = tmpEl.getFirstChild().getNodeValue();
                logger.debug("stasOutputFileName = " + stasOutputFileName);
                staConfig.removeChild(tmpEl);
            }
            else if (n.getNodeName().equals("channelConfig")){
                chanConfig = TemplateFileLoader.getTemplate((Element)n);

                Node tmpEl = SodUtil.getElement(chanConfig, "outputLocation");
                staFormatter = new StationFormatter((Element)tmpEl);
                chanConfig.removeChild(tmpEl);

                tmpEl = SodUtil.getElement(chanConfig, "filename");
                chansOutputFileName = tmpEl.getFirstChild().getNodeValue();
                logger.debug("chansOutputFileName = " + chansOutputFileName);
                chanConfig.removeChild(tmpEl);
            }
        }
        if (fileDir == null){
            fileDir = FileWritingTemplate.getBaseDirectoryName();
        }
        if (fileDir == null || netFormatter == null || staFormatter == null || netConfig == null
            || staConfig == null || chanConfig == null || netsOutputFileName == null
            || stasOutputFileName == null || chansOutputFileName == null){
            throw new IllegalArgumentException(
                                               "The configuration element must contain a fileDir, netConfig, stationConfig, and chanConfig.");
        }

        netTemplate = new NetworkStatusTemplate(netConfig, fileDir ,netsOutputFileName);
    }

    public void change(NetworkAccess net, Status status){
        netTemplate.change(net, status);
        getStationsInNetworkTemplate(net);
    }

    public void change(Station station, Status status){
        StationsInNetworkTemplate snt = getStationsInNetworkTemplate(station);
        snt.change(station, status);
        getChannelsInStationTemplate(station);
    }

    public void change(Channel channel, Status status) {
        ChannelsInStationTemplate cst = getChannelsInStationTemplate(channel);
        cst.change(channel, status);
    }

    public NetworkStatusTemplate getNetworkStatusTemplate(){
        return netTemplate;
    }

    public StationsInNetworkTemplate getStationsInNetworkTemplate(NetworkAccess net){
        if (!contains(net)){
            try {
                stationTemplates.put(getIDString(net),
                                     new StationsInNetworkTemplate(staConfig,
                                                                   fileDir,
                                                                   netFormatter.getResult(net)
                                                                       + '/'
                                                                       + stasOutputFileName,
                                                                   net));
            } catch (IOException e) {
                CommonAccess.handleException(e, "trouble creating StationsInNetworkTemplate");
            }
        }
        StationsInNetworkTemplate snt = (StationsInNetworkTemplate)stationTemplates.get(getIDString(net));
        return snt;
    }

    public StationsInNetworkTemplate getStationsInNetworkTemplate(Station station){
        return getStationsInNetworkTemplate(getNetworkFromStation(station));
    }

    public ChannelsInStationTemplate getChannelsInStationTemplate(Station station){
        if (!contains(station)){
            try {
                channelTemplates.put(getIDString(station),
                                     new ChannelsInStationTemplate(chanConfig,
                                                                   fileDir,
                                                                   netFormatter.getResult(getNetworkFromStation(station))
                                                                       + '/'
                                                                       + staFormatter.getResult(station)
                                                                       + '/'
                                                                       + chansOutputFileName,
                                                                   station));
            } catch (IOException e) {
                CommonAccess.handleException(e, "trouble creating ChannelsInSiteTemplate");
            }
        }
        ChannelsInStationTemplate cst = (ChannelsInStationTemplate)channelTemplates.get(getIDString(station));
        return cst;
    }

    public ChannelsInStationTemplate getChannelsInStationTemplate(Channel chan){
        return getChannelsInStationTemplate(chan.my_site.my_station);
    }

    public NetworkAccess getNetworkFromStation(Station station){
        NetworkAccess staNet = null;
        Iterator it = stationTemplates.keySet().iterator();
        while (it.hasNext() && staNet == null){
            String cur = (String)it.next();
            if (cur.equals(getIDString(station.my_network))){
                staNet = ((StationsInNetworkTemplate)stationTemplates.get(cur)).getNetwork();
            }
        }
        return staNet;
    }

    public boolean contains(NetworkAccess net){
        return stationTemplates.containsKey(getIDString(net));
    }

    public boolean contains(Station sta){
        return channelTemplates.containsKey(getIDString(sta));
    }

    private String getIDString(NetworkAccess net){
        return getIDString(net.get_attributes());
    }

    private String getIDString(NetworkAttr netAttr){
        return NetworkIdUtil.toString(netAttr.get_id());
    }

    private String getIDString(Station sta){
        return StationIdUtil.toString(sta.get_id());
    }

    public void setArmStatus(String status)  throws IOException {
        netTemplate.setArmStatus(status);
    }

    public void change(Site site, Status status){
        // noImpl
    }
}




