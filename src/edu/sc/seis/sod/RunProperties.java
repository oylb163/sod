/**
 * RunProperties.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import org.w3c.dom.Element;

public class RunProperties{
    public RunProperties(Element el) throws ConfigurationException{
        Element runNameChild = SodUtil.getElement(el, "runName");
        if(runNameChild != null ){
            runName = SodUtil.getText(runNameChild);
        }

        Element statusBaseChild = SodUtil.getElement(el, "statusBase");
        if(statusBaseChild != null){
            statusDir = SodUtil.getText(statusBaseChild);
        }

        Element taupModelChild = SodUtil.getElement(el, "TauPModel");
        if(taupModelChild != null){
            tauPModel = SodUtil.getText(taupModelChild);
        }

        Element eventQueryChild = SodUtil.getElement(el, "eventQueryIncrement");
        if(eventQueryChild != null){
            eventQueryIncrement = SodUtil.loadTimeInterval(el);
        }

        Element eventLagChild = SodUtil.getElement(el, "eventLag");
        if(eventLagChild != null){
            eventLag = SodUtil.loadTimeInterval(el);
        }

        Element eventRefreshChild = SodUtil.getElement(el, "eventRefreshInterval");
        if(eventRefreshChild != null){
            eventRefresh = SodUtil.loadTimeInterval(el);
        }

        Element maxRetryChild = SodUtil.getElement(el, "maxRetryDelay");
        if(maxRetryChild != null){
            maxRetry = SodUtil.loadTimeInterval(maxRetryChild);
        }

        Element numWorkersChild = SodUtil.getElement(el, "waveformWorkerThreads");
        if(numWorkersChild != null){
            numWorkers = Integer.parseInt(SodUtil.getText(numWorkersChild));
        }

        if(SodUtil.getElement(el, "reopenEvents") != null){
            reopenEvents = true;
        }

        if(SodUtil.getElement(el, "removeDatabase") != null){
            removeDatabase = true;
        }
    }

    public TimeInterval getMaxRetryDelay() { return maxRetry; }

    public TimeInterval getEventQueryIncrement() { return eventQueryIncrement; }

    public TimeInterval getEventLag() { return eventLag; }

    public TimeInterval getEventRefreshInterval() { return eventRefresh; }

    public String getRunName(){ return runName; }

    public String getStatusBaseDir(){ return statusDir; }

    public String getTauPModel(){ return tauPModel; }

    public int getNumWaveformWorkerThreads(){ return numWorkers; }

    public boolean reopenEvents(){ return reopenEvents; }

    public boolean removeDatabase(){ return removeDatabase; }

    private static final TimeInterval ONE_WEEK = new TimeInterval(7, UnitImpl.DAY);
    private static final TimeInterval TEN_MIN = new TimeInterval(10, UnitImpl.MINUTE);
    private static final TimeInterval DAYS_180 = new TimeInterval(180, UnitImpl.DAY);

    private TimeInterval eventQueryIncrement = ONE_WEEK;
    private TimeInterval eventLag = ONE_WEEK;
    private TimeInterval eventRefresh = TEN_MIN;
    private TimeInterval maxRetry = DAYS_180;

    private String runName = "Your Sod";
    private String statusDir = "status";
    private String tauPModel = "prem";

    private int numWorkers = 1;

    private boolean reopenEvents = false;
    private boolean removeDatabase = false;
}
