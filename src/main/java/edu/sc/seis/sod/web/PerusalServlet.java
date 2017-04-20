package edu.sc.seis.sod.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;
import edu.sc.seis.sod.web.jsonapi.PerusalJson;

public class PerusalServlet extends HttpServlet {

    private static final String PREV_ESP = "prevESP";

    public static final String CURR_ESP = "currESP";

    public PerusalServlet() {
        this(WebAdmin.getApiBaseUrl());
    }

    public PerusalServlet(String baseUrl) {
        this.baseUrl = baseUrl;
        this.perusalDir = new File("perusals");
        if (!perusalDir.exists()) {
            perusalDir.mkdirs();
            logger.info("Create perusal Dir: " + perusalDir.getAbsolutePath());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String URL = req.getRequestURL().toString();
            System.out.println("GET: " + URL);
            logger.info("GET: " + URL);
            WebAdmin.setJsonHeader(req, resp);
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            Matcher matcher = allPattern.matcher(URL);
            if (matcher.matches()) {
                // all perusal ids
                List<String> perusalIds = getPerusalIds();
                List<JsonApiData> jsonList = new ArrayList<JsonApiData>();
                for (String p : perusalIds) {
                    jsonList.add(new PerusalJson(p, baseUrl));
                }
                JsonApi.encodeJsonWithoutInclude(out, jsonList);
            } else {
                matcher = idPattern.matcher(URL);
                if (matcher.matches()) {
                    String pId = matcher.group(1);
                    JSONObject p = loadPerusal(pId);
                    writer.print(p);
                } else {
                    logger.warn("Bad URL for servlet: " + URL);
                    JsonApi.encodeError(out, "bad url for servlet: " + URL);
                    writer.close();
                    resp.sendError(500);
                }
            }
            writer.close();
        } catch(JSONException e) {
            throw new ServletException(e);
        } catch(NumberFormatException e) {
            throw new ServletException(e);
        } finally {
            NetworkDB.rollback();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebAdmin.setJsonHeader(req, resp);
        JSONObject inJson = loadFromReader(req.getReader());
        System.out.println("POST: " + inJson.toString(2));
        JSONObject dataObj = inJson.getJSONObject(JsonApi.DATA);
        if (dataObj != null) {
            String type = dataObj.getString(JsonApi.TYPE);
            String id = dataObj.optString(JsonApi.ID);
            if (type.equals(PerusalJson.PERUSAL)) {
                if (id.length() == 0) {
                    // empty id, so new, create
                    id = java.util.UUID.randomUUID().toString();
                    dataObj.put(JsonApi.ID, id);
                }
                // security, limit to simple filename
                Matcher m = filenamePattern.matcher(id);
                if (!m.matches()) {
                    resp.sendError(400, "Bad id: " + id);
                    return;
                }
                updateNext(inJson);
                savePerusal(id, inJson);
                PrintWriter w = resp.getWriter();
                w.print(inJson.toString(2));
                w.close();
            } else {
                resp.sendError(400, "type  wrong/missing: " + type);
                return;
            }
        } else {
            resp.sendError(400, "Unable to parse JSON");
            return;
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPut(req, resp);
    }

    // @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebAdmin.setJsonHeader(req, resp);
        String URL = req.getRequestURL().toString();
        System.out.println("doPatch " + URL);
        Matcher matcher = idPattern.matcher(URL);
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        try {
            if (matcher.matches()) {
                String id = matcher.group(1);
                JSONObject p = loadPerusal(id);
                JSONObject inJson = loadFromReader(req.getReader());

                JSONObject pRelated = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.RELATIONSHIPS);
                JSONObject pAttr = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.ATTRIBUTES);
                JSONObject inRelated = inJson.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.RELATIONSHIPS);
                JSONObject inAttr = inJson.getJSONObject(JsonApi.DATA).optJSONObject(JsonApi.ATTRIBUTES);
                if (inAttr != null) {
                    if (pAttr == null) {
                        pAttr = new JSONObject();
                        p.getJSONObject(JsonApi.DATA).put(JsonApi.ATTRIBUTES, pAttr);
                    }
                    Iterator<String> keyIt = inAttr.keys();
                    while(keyIt.hasNext()) {
                        String key = keyIt.next();
                        pAttr.put(key, inAttr.get(key));
                    }
                }
                if(inRelated != null) {
                    if (pRelated == null) {
                        pRelated = new JSONObject();
                        p.getJSONObject(JsonApi.DATA).put(JsonApi.RELATIONSHIPS, pRelated);
                    }
                    Iterator<String> keyIt = inRelated.keys();
                    while(keyIt.hasNext()) {
                        String key = keyIt.next();
                        pRelated.put(key, inRelated.get(key));
                    }
                }
                updateNext(p);
                savePerusal(id, p);
                writer.print(p);
            } else {
                logger.warn("Bad URL for servlet: " + URL);
                JsonApi.encodeError(out, "bad url for servlet: " + URL);
                writer.close();
                resp.sendError(500);
            }
        } finally {
            writer.close();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("DELETE: " + URL);
        logger.info("DELETE: " + URL);
        Matcher matcher = idPattern.matcher(URL);
        if (matcher.matches()) {
            String pId = matcher.group(1);
            File f = new File(perusalDir, pId);
            f.delete();
        }
        resp.getWriter().close();// empty content
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equals("PATCH"))
            doPatch(req, resp);
        else
            super.service(req, resp);
    }

    protected List<String> getPerusalIds() {
        return Arrays.asList(perusalDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                Matcher matcher = filenamePattern.matcher(name);
                return matcher.matches();
            }
        }));
    }

    private void savePerusal(String id, JSONObject inJson) throws IOException {
        BufferedWriter out = null;
        try {
            File pFile = new File(perusalDir, id);
            if (pFile.exists()) {
                Files.move(pFile.toPath(), new File(perusalDir, id+".old").toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            out = new BufferedWriter(new FileWriter(pFile));
            out.write(inJson.toString(2));
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected JSONObject loadPerusal(String id) throws IOException {
        // security, limit to simple filename
        Matcher m = filenamePattern.matcher(id);
        if (m.matches()) {
            BufferedReader in = null;
            try {
                File f = new File(perusalDir, id);
                in = new BufferedReader(new FileReader(f));
                return loadFromReader(in);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch(IOException e) {
                        // oh well
                    }
                }
            }
        } else {
            throw new RuntimeException("perusal id does not match pattern: " + id);
        }
    }

    protected JSONObject loadFromReader(BufferedReader in) throws IOException {
        StringBuffer json = new StringBuffer();
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = in.read(buf)) != -1) {
            json.append(String.valueOf(buf, 0, numRead));
        }
        JSONObject out = new JSONObject(json.toString());
        return out;
    }

    protected void updateNext(JSONObject p) {
        JSONObject related = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.RELATIONSHIPS);
        JSONObject attr = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.ATTRIBUTES);
        JSONObject curr = related.optJSONObject(CURR_ESP);
        JSONObject prev = related.optJSONObject(PREV_ESP);
        if (curr == null || curr.getJSONObject(JsonApi.DATA).getString(JsonApi.ID).length() == 0) {
            // no next, so find
            int prevDbId = 0;
            EventStationPair next = null;
            if (prev == null || prev.getJSONObject(JsonApi.DATA).getString(JsonApi.ID).length() == 0) {
                // no prev, so first time
                if (attr.getString(KEY_PRIMARY_SORT).equalsIgnoreCase(KEY_SORT_BY_EVENT)) {
                    next = getNextPrimaryEvent(null);
                    // no more left
                } else {
                    next = getNextPrimaryStation(null);
                    // no more left
                }
            } else {
                prevDbId = Integer.parseInt(prev.getJSONObject(JsonApi.DATA).getString(JsonApi.ID));
                String q = "from EventStationPair where dbid = " + prevDbId;
                Query query = SodDB.getSession().createQuery(q);
                EventStationPair esp = (EventStationPair)query.uniqueResult();
                List<EventStationPair> nextESPList;
                StatefulEvent currEvent = esp.getEvent();
                StationImpl currStation = esp.getStation();
                if (attr.getString(KEY_PRIMARY_SORT).equalsIgnoreCase(KEY_SORT_BY_EVENT)) {
                    nextESPList = SodDB.getSingleton().getSuccessfulESPForStation(currStation);
                } else {
                    nextESPList = SodDB.getSingleton().getSuccessfulESPForEvent(currEvent);
                }
                Iterator<EventStationPair> iterator = nextESPList.iterator();
                while (iterator.hasNext()) {
                    EventStationPair eventStationPair = iterator.next();
                    if (eventStationPair.getDbid() == prevDbId) {
                        // found it
                        break;
                    }
                }
                if (iterator.hasNext()) {
                    next = iterator.next();
                } else {
                    // no next in sub-sort, go to next in primary
                    if (attr.getString(KEY_PRIMARY_SORT).equalsIgnoreCase(KEY_SORT_BY_EVENT)) {
                        next = getNextPrimaryEvent(currEvent);
                        // no more left
                    } else {
                        next = getNextPrimaryStation(currStation);
                        // no more left
                    }
                }
            }
            if (next != null) {
                EventStationJson esJson = new EventStationJson(next, baseUrl);
                JSONObject nextJSON = new JSONObject();
                JSONObject dataJSON = new JSONObject();
                dataJSON.put(JsonApi.TYPE, esJson.getType());
                dataJSON.put(JsonApi.ID, esJson.getId());
                nextJSON.put(JsonApi.DATA, dataJSON);
                related.put(CURR_ESP, nextJSON);
            }
        }
    }

    private EventStationPair getNextPrimaryStation(StationImpl currStation) {
        List<StationImpl> allStationList = Arrays.asList(NetworkDB.getSingleton().getAllStations());
        // should sort here
        Iterator<StationImpl> staIt = allStationList.iterator();
        while (currStation != null && staIt.hasNext()) {
            StationImpl s = staIt.next();
            if (s.getDbid() == currStation.getDbid()) {
                // found it
                break;
            }
        }
        while (staIt.hasNext()) {
            StationImpl s = staIt.next();
            List<EventStationPair> nextESPList = SodDB.getSingleton().getSuccessfulESPForStation(s);
            if (nextESPList.size() > 0) {
                return nextESPList.get(0);
            }
        }
        return null;
    }

    private EventStationPair getNextPrimaryEvent(StatefulEvent currEvent) {
        SuccessfulEventCache cache = WebAdmin.getSuccessfulEventCache();
        List<StatefulEvent> events = cache.getEventWithSuccessful();
        // should sort here...
        Iterator<StatefulEvent> eventIt = events.iterator();
        while (currEvent != null && eventIt.hasNext()) {
            StatefulEvent e = eventIt.next();
            if (e.getDbid() == currEvent.getDbid()) {
                break;
            }
        }
        while (eventIt.hasNext()) {
            StatefulEvent e = eventIt.next();
            List<EventStationPair> nextESPList = SodDB.getSingleton().getSuccessfulESPForEvent(e);
            if (nextESPList.size() > 0) {
                return nextESPList.get(0);
            }
        }
        return null;
    }

    Pattern allPattern = Pattern.compile(".*/perusals");

    Pattern idPattern = Pattern.compile(".*/perusals/([-_a-zA-Z0-9]+)");

    Pattern filenamePattern = Pattern.compile("[-_a-zA-Z0-9]+");

    private String baseUrl;

    private File perusalDir;

    private static final JSONObject EMPTY_JSON = new JSONObject();
    
    public static final String KEY_PRIMARY_SORT = "primary-sort";

    public static final String KEY_STATION_SORT = "station-sort";

    public static final String KEY_EVENT_SORT = "event-sort";

    public static final String KEY_SORT_BY_EVENT = "event";

    public static final String KEY_SORT_BY_STATION = "station";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PerusalServlet.class);
}
