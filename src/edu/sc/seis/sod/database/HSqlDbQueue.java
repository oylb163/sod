package edu.sc.seis.sod.database;

import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessHelper;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.Start;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * HSqlDbQueue.java
 *
 *
 * Created: Tue Sep 17 13:54:22 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class HSqlDbQueue implements Queue {
    public HSqlDbQueue (){
        Properties props = new Properties();
        this.props = props;
        eventDatabase = DatabaseManager.getDatabaseManager(props, "hsqldb").getEventDatabase();
        //  eventDatabase.updateStatus(Status.PROCESSING, Status.NEW);
        //  delete(Status.COMPLETE_SUCCESS);
    }

    public HSqlDbQueue(Properties props) {
        this.props = props;
        if(getDatabaseType(props) == 0) {
            eventDatabase = DatabaseManager.getDatabaseManager(props, "hsqldb").getEventDatabase();
        }else {
            eventDatabase =  DatabaseManager.getDatabaseManager(props, "postgres").getEventDatabase();
        }
        if(Start.RE_OPEN_EVENTS) {
            eventDatabase.reOpenEvents();
        }
        if(Start.GET_NEW_EVENTS) {
            deleteTimeConfig();
        }
    }

    public void clean() {
        if(Start.REMOVE_DATABASE == true) {
            eventDatabase.clean();
        }
        deleteTimeConfig();
    }


    public void deleteTimeConfig() {
        //if(Start.GET_NEW_EVENTS == true)
        {
            eventDatabase.deleteTimeConfig();
        }

    }

    public void updateEventDatabase() {
        if(getPersistanceType(props) == 0) {
            eventDatabase.updateStatus(Status.PROCESSING, Status.NEW);
            eventDatabase.updateStatus(Status.AWAITING_FINAL_STATUS, Status.NEW);
            //delete(Status.COMPLETE_SUCCESS);
        } else {
            eventDatabase.updateStatus(Status.PROCESSING, Status.COMPLETE_REJECT);
            eventDatabase.updateStatus(Status.AWAITING_FINAL_STATUS, Status.COMPLETE_REJECT);
        }
    }

    private int getDatabaseType(Properties props) {
        String value = props.getProperty("edu.sc.seis.sod.databasetype");
        if(value == null) value = "hsqldb";
        if(value.equalsIgnoreCase("POSTGRES")) return 1;
        else return 0;
    }

    private int getPersistanceType(Properties props) {

        String value = props.getProperty("edu.sc.seis.sod.persistencetype");
        if(value == null) value = "ATLEASTONCE";
        if(value.equalsIgnoreCase("ATMOSTONCE")) return 1;
        else return 0;
    }

    /**
     * inserts the obj at the end of the queue.
     *
     */
    public synchronized void push(String serverDNS,
                                  String serverName,
                                  edu.iris.Fissures.IfEvent.EventAccess obj,
                                  edu.iris.Fissures.IfEvent.Origin originObj) {
        org.omg.CORBA.ORB orb = null;
        try {
            orb = CommonAccess.getCommonAccess().getORB();
        } catch(edu.sc.seis.sod.ConfigurationException cfe) {
            cfe.printStackTrace();
        }
        EventAccess eventAccess = obj;//(EventAccess) ((CacheEvent)obj).getEventAccess();
        Origin origin = originObj;
        String name = eventAccess.get_attributes().name;
        float lat = origin.my_location.latitude;
        float lon = origin.my_location.longitude;
        float depth = (float)origin.my_location.depth.value;

        String eventAccessIOR = null;
        try {
            eventAccessIOR = orb.object_to_string(eventAccess);
        } catch(Exception e) {
            e.printStackTrace();
        }
        edu.iris.Fissures.Time origin_time = origin.origin_time;
        int dbid = eventDatabase.put(serverName,
                                     serverDNS,
                                     name,
                                     lat,
                                     lon,
                                     depth,
                                     origin_time,
                                     eventAccessIOR);
        notifyAll();


    }

    public synchronized void setFinalStatus(EventAccessOperations eventAccess, Status status) {

        int dbid = eventDatabase.get(eventAccess);
        Status st = eventDatabase.getStatus(dbid);
        if(!(st.getId() == Status.AWAITING_FINAL_STATUS.getId() ||
                 st.getId() == Status.PROCESSING.getId())) {
            return;
        }

        eventDatabase.updateStatus(dbid, status);

        notifyAll();

        getLength();


    }

    private void open() {

    }

    /**
     * pops the first element of the queue.
     * @return a <code>java.lang.Object</code> value
     */
    public synchronized int pop() {
        int dbId = eventDatabase.getFirst(Status.NEW);

        logger.debug("DBId: " + dbId + " SourceAlive: " + sourceAlive);
        while(dbId == -1 && sourceAlive){
            try {
                wait();
                dbId = eventDatabase.getFirst(Status.NEW);
            } catch(InterruptedException ie) { }

        }
        if(dbId != -1){
            eventDatabase.updateStatus(dbId, Status.PROCESSING);
        }
        return dbId;
    }

    private org.omg.CORBA.Object reValidate(int dbid, org.omg.CORBA.Object obj) {

        if(obj._non_existent()) {
            //here query from the server again.
            EventDC eventDC = getEventDC(dbid);
            edu.iris.Fissures.IfEvent.EventFinder finder = eventDC.a_finder();
            EventAccess[] eventAccess = finder.get_by_name(eventDatabase.getEventName(dbid));
            for(int counter = 0; counter < eventAccess.length; counter++) {
                int checkdbid = -1;
                if((checkdbid = eventDatabase.get(eventAccess[counter])) != -1) {
                    if(checkdbid == dbid) {
                        //update the ior corresponding to the dbid and return the object.
                        org.omg.CORBA.ORB orb = null;
                        try {
                            orb = CommonAccess.getCommonAccess().getORB();
                        } catch(edu.sc.seis.sod.ConfigurationException cfe) {
                            cfe.printStackTrace();
                        }
                        String newIOR = orb.object_to_string(eventAccess[counter]);
                        eventDatabase.updateIOR(dbid, newIOR);
                        return eventAccess[counter];
                    }
                }
            }
            return null;
        } else {
            return obj;
        }

    }

    public int getEventId(EventAccessOperations eventAccess) {
        return eventDatabase.get(eventAccess);
    }


    /**
     * returns the length of the queue.
     *
     * @return an <code>int</code> value
     */
    public synchronized int getLength() {
        int numNew = eventDatabase.getCount(Status.NEW);
        int numAwait = eventDatabase.getCount(Status.AWAITING_FINAL_STATUS);
        int numProcessing = eventDatabase.getCount(Status.PROCESSING);
        int reopen = eventDatabase.getCount(Status.RE_OPEN);
        int reopenProcessing = eventDatabase.getCount(Status.RE_OPEN_PROCESSING);
        return (numNew + numAwait + numProcessing + reopen + reopenProcessing);
    }

    private int getNew() {
        int numNew = eventDatabase.getCount(Status.NEW);
        return numNew;
    }
    /**
     * sets if the source i.e., the thread which pushes objects into the queue
     * is alive
     *
     * @param value a <code>boolean</code> value
     */
    public synchronized void setSourceAlive(boolean value) {
        this.sourceAlive = value;
        notifyAll();
    }

    /**
     * returns true if the source i.e., the thread which pushes objects into the queue
     * is alive, else returns false.
     *
     * @return a <code>boolean</code> value
     */
    public boolean getSourceAlive() {
        return sourceAlive;
    }


    public  synchronized void waitForProcessing() {
        if(getLength() > 4) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
    }

    public synchronized void delete(Status status) {
        getLength();
        int numSuccessful = eventDatabase.getCount(status);
        if(numSuccessful > 0) {
            eventDatabase.delete(status);
            notifyAll();
        }

    }

    private edu.iris.Fissures.IfEvent.EventDC getEventDC(int dbid) {
        try {


            FissuresNamingService fissuresNamingService =
                new FissuresNamingService(CommonAccess.getCommonAccess().getORB());
            EventDC eventDC = fissuresNamingService.getEventDC(eventDatabase.getServerDNS(dbid),
                                                                   eventDatabase.getServerName(dbid));
            return eventDC;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public EventAccessOperations getEventAccess(int dbid) {
        CacheEventHolder holder;
        if ((holder = (CacheEventHolder)cacheEventMap.get(""+dbid)) != null &&
            holder.createTime.after(ClockUtil.now().subtract(cacheTime))) {
            return holder.event;
        }

        org.omg.CORBA.ORB orb = null;
        try {
            orb = CommonAccess.getCommonAccess().getORB();
        } catch(edu.sc.seis.sod.ConfigurationException cfe) {
            cfe.printStackTrace();
        }
        String ior = eventDatabase.getObject(dbid);
        if(ior == null)  {
            return null;
        }
        org.omg.CORBA.Object obj = orb.string_to_object(ior);
        obj = reValidate(dbid, obj);
        EventAccess eventAccess = EventAccessHelper.narrow(obj);
        holder = new CacheEventHolder(dbid,
                                      new CacheEvent(eventAccess));
        cacheEventMap.put(""+dbid, holder);
        return holder.event;
    }

    HashMap cacheEventMap = new HashMap();
    TimeInterval cacheTime = new TimeInterval(10, UnitImpl.MINUTE);
    class CacheEventHolder {
        CacheEventHolder(int dbid, CacheEvent event) {
            this.dbid = dbid;
            this.event = event;
        }
        int dbid = -1;
        CacheEvent event = null;
        MicroSecondDate createTime = ClockUtil.now();
    }

    public Status getStatus(int eventid) {
        return eventDatabase.getStatus(eventid);
    }

    public void closeDatabase() {
        if(getDatabaseType(props) == 0) DatabaseManager.getDatabaseManager(props, "hsqldb").close();
        else DatabaseManager.getDatabaseManager(props, "postgres").close();
    }

    public void setTime(String serverName,
                        String serverDNS,
                        edu.iris.Fissures.Time time) {
        eventDatabase.setTime(serverName,
                              serverDNS,
                              time);
    }

    public edu.iris.Fissures.Time getTime(String serverName,
                                          String serverDNS) {
        return eventDatabase.getTime(serverName,
                                     serverDNS);
    }

    public void incrementTime(String serverName,
                              String serverDNS,
                              int days) {
        eventDatabase.incrementTime(serverName,
                                    serverDNS,
                                    days);
    }

    Properties props;

    private boolean sourceAlive = true;

    EventDatabase eventDatabase;

    private boolean waitFlag = false;

    private PreparedStatement putStmt;

    private PreparedStatement getStmt;

    private static Logger logger = Logger.getLogger(HSqlDbQueue.class);

    private PreparedStatement getMaxIdStmt;

}// HSqlDbQueue

