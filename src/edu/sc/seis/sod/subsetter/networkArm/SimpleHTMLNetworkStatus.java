/**
 * SimpleHTMLStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.NetworkStatus;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.SimpleHTMLPage;
import java.io.File;
import org.w3c.dom.Element;

public class SimpleHTMLNetworkStatus implements NetworkStatus{
    public SimpleHTMLNetworkStatus(Element config) throws ConfigurationException{
        File htmlDir = SodUtil.makeOutputDirectory(config);
        pages[0] = statusPage = new SimpleHTMLPage("Network Arm Status", "status.html", htmlDir);
        pages[1] = sitePage = new SimpleHTMLPage("Sites", "sites.html", htmlDir);
        pages[2] = networkPage = new SimpleHTMLPage("Networks", "networks.html", htmlDir);
        pages[3] = stationPage = new SimpleHTMLPage("Stations", "stations.html", htmlDir);
        pages[4] = channelPage = new SimpleHTMLPage("Channels", "channel.html", htmlDir);
        statusPage.addLinks(pages);
        sitePage.addLinks(pages);
        networkPage.addLinks(pages);
        stationPage.addLinks(pages);
        channelPage.addLinks(pages);
    }
    
    public void setArmStatus(String status) {
        statusPage.clear("Status");
        statusPage.append("Status", "<b>Status: </b> " + status);
        for (int i = 0; i < pages.length; i++) {
            pages[i].write();
        }
    }
    
    public void change(NetworkAccess network, RunStatus status) {
        appendToPage(channelPage, status,
                     NetworkIdUtil.toString(network.get_attributes().get_id()));
    }
    
    public void change(Station station, RunStatus status) {
        appendToPage(channelPage, status,
                     StationIdUtil.toString(station.get_id()));
    }
    
    public void change(Site site, RunStatus status) {
        appendToPage(channelPage, status,
                     SiteIdUtil.toString(site.get_id()));
    }
    
    public void change(Channel channel, RunStatus status) {
        appendToPage(channelPage, status,
                     ChannelIdUtil.toString(channel.get_id()));
    }
    
    private void appendToPage(SimpleHTMLPage page, RunStatus status, String text){
        if(status == RunStatus.FAILED){
            page.append("Failures", text);
        }else if(status == RunStatus.PASSED){
            page.append("Successes", text);
        }
    }
    
    private SimpleHTMLPage channelPage, sitePage, networkPage, stationPage, statusPage;
    
    private SimpleHTMLPage[] pages = new SimpleHTMLPage[5];
}
