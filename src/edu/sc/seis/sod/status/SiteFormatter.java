/**
 * SiteFormatter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;


import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.Time;
import java.util.Iterator;
import org.w3c.dom.Element;



public class SiteFormatter extends Template implements SiteTemplate {
    SiteGroupTemplate sgt;
    
    public SiteFormatter(Element el){
        this(el, null);
    }
    
    public SiteFormatter(Element el, SiteGroupTemplate sgt){
        this.sgt = sgt;
        parse(el);
    }
    
    /**
     * Method getResult
     *
     * @param    site                a  Site
     *
     * @return   a String
     *
     */
    public String getResult(Site site) {
        StringBuffer buf = new StringBuffer();
        Iterator it = templates.iterator();
        while (it.hasNext()){
            SiteTemplate cur = (SiteTemplate)it.next();
            buf.append(cur.getResult(site));
        }
        return buf.toString();
    }
    
    /**
     *returns an object of the template type that this class uses, and returns
     * the passed in text when the getResult method of that template type is
     * called
     */
    protected Object textTemplate(final String text) {
        return new SiteTemplate(){
            public String getResult(Site site){
                return text;
            }
        };
    }
    
    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, final Element el) {
    
        if (tag.equals("siteCode")){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return site.get_id().site_code;
                }
            };
        }
        else if (tag.equals("stationCode")){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return site.get_id().station_code;
                }
            };
        }
        else if (tag.equals("networkCode")){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return site.get_id().network_id.network_code;
                }
            };
        }
        else if (tag.equals("beginTime")){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return btt.getResult(site.get_id().begin_time);
                }
                    TimeTemplate btt = new TimeTemplate(el);
            };
        }
        else if (tag.equals("endTime")){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return btt.getResult(site.effective_time.end_time);
                }
                TimeTemplate btt = new TimeTemplate(el);
            };
        }
        else if (tag.equals("status") && sgt != null){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return sgt.siteMap.get(site).toString();
                }
            };
        }
        else if (tag.equals("comment")){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return site.comment;
                }
            };
        }
        else if (tag.equals("depth")){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return Double.toString(site.my_location.depth.value);
                }
            };
        }
        else if (tag.equals("elevation")){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return Double.toString(site.my_location.elevation.value);
                }
            };
        }
        else if (tag.equals("lat")){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return Float.toString(site.my_location.latitude);
                }
            };
        }
        else if (tag.equals("lon")){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return Float.toString(site.my_location.longitude);
                }
            };
        }
        else if (tag.equals("beginTimeUnformatted")){
            return new SiteTemplate(){
                public String getResult(Site site){
                    return site.get_id().begin_time.date_time;
                }
            };
        }
        return super.getTemplate(tag, el);
    }
    
    public static String formatSiteCode(String siteCode){
        if (siteCode == null || siteCode.equals("")){
            return "  ";
        }
        else return siteCode;
    }
    
}

