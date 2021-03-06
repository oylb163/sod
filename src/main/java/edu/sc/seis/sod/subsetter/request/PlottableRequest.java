package edu.sc.seis.sod.subsetter.request;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.Dimension;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.NSPlottableDC;
import edu.sc.seis.fissuresUtil.cache.ProxyPlottableDC;
import edu.sc.seis.fissuresUtil.cache.RetryPlottableDC;
import edu.sc.seis.fissuresUtil.display.PlottableDisplay;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.AbstractSource;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class PlottableRequest extends AbstractSource  implements RequestSubsetter {

    public PlottableRequest(Element config) throws Exception {
        super(config, "DelilahCache");
        dns = SodUtil.loadText(config, "dns", "edu/iris/dmc");
        NodeList dims = config.getElementsByTagName("pixelsPerDay");
        pixelsPerDay = new int[dims.getLength()];
        for(int i=0;i<dims.getLength();i++) {
            pixelsPerDay[i] = Integer.parseInt(XMLUtil.getText((Element)dims.item(i)));
        }
        plottableCache = new RetryPlottableDC(new NSPlottableDC(getDNS(), getName(), CommonAccess.getNameService()), 2);
    }
    
    public StringTree accept(CacheEvent event, ChannelImpl channel, RequestFilter[] request, CookieJar cookieJar)
            throws Exception {
        Plottable[] plottables = new Plottable[0];
        for (int i = 0; i < request.length; i++) {
            for (int k = 0; k < pixelsPerDay.length; k++) {
                Dimension dimension = new Dimension(pixelsPerDay[k], PlottableDisplay.OFFSET);
                plottables = plottableCache.get_plottable(request[i], dimension);
            }
        }
        System.out.println("Got "+plottables.length+" plottable for "+ChannelIdUtil.toStringNoDates(channel)+" "+request[0].start_time.date_time+" to "+request[0].end_time.date_time);
        return new Pass(this);
    }

    public String getDNS() {
        return dns;
    }
    
    private String dns = "";
    
    private ProxyPlottableDC plottableCache;

    private int[] pixelsPerDay;
}
