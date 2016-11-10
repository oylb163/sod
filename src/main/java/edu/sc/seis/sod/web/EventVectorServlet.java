package edu.sc.seis.sod.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.json.JSONWriter;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.hibernate.AbstractHibernateDB;
import edu.sc.seis.fissuresUtil.hibernate.EventSeismogramFileReference;
import edu.sc.seis.fissuresUtil.hibernate.SeismogramFileRefDB;
import edu.sc.seis.fissuresUtil.hibernate.SeismogramFileReference;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.fissuresUtil.xml.UnsupportedFileTypeException;
import edu.sc.seis.sod.AbstractEventChannelPair;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.EventVectorPair;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.EventVectorJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class EventVectorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("GET: " + URL);
        Matcher m = mseedPattern.matcher(URL);
        if (m.matches()) {
            // raw miniseed
            AbstractEventChannelPair ecp = getECP(m.group(1));
            ChannelImpl[] chans;
            if (ecp instanceof EventVectorPair) {
                chans = ((EventVectorPair)ecp).getChannelGroup().getChannels();
            } else {
                chans = new ChannelImpl[] {((EventChannelPair)ecp).getChannel()};
            }
            List<EventSeismogramFileReference> seisRefList = new ArrayList<EventSeismogramFileReference>();
            for (int j = 0; j < chans.length; j++) {
                seisRefList.addAll(SeismogramFileRefDB.getSingleton()
                        .getSeismogramsForEventForChannel(ecp.getEvent(), chans[j].getId()));
            }
            resp.setContentType("application/vnd.fdsn.mseed");
            OutputStream outBinary = resp.getOutputStream();
            for (EventSeismogramFileReference ref : seisRefList) {
                try {
                    if (SeismogramFileTypes.fromInt(ref.getFileType()).equals(SeismogramFileTypes.MSEED)) {
                        BufferedInputStream bufIn = new BufferedInputStream(ref.getFilePathAsURL().openStream());
                        byte[] buf = new byte[1024];
                        int bufNum = 0;
                        while ((bufNum = bufIn.read(buf)) != -1) {
                            outBinary.write(buf, 0, bufNum);
                        }
                        bufIn.close();
                    }
                } catch(UnsupportedFileTypeException e) {
                    throw new RuntimeException("Should never happen", e);
                }
            }
            outBinary.flush();
        } else {
            if (req.getHeader("accept") != null && req.getHeader("accept").contains("application/vnd.api+json")) {
                resp.setContentType("application/vnd.api+json");
                System.out.println("      contentType: application/vnd.api+json");
            } else {
                resp.setContentType("application/json");
                System.out.println("      contentType: application/json");
            }
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            m = eventStationPattern.matcher(URL);
            if (m.matches()) {
                AbstractEventChannelPair ecp = getECP(m.group(1));
                EventVectorJson jsonData = new EventVectorJson(ecp, WebAdmin.getBaseUrl());
                JsonApi.encodeJson(out, jsonData);
            } else {
                JsonApi.encodeError(out, "url does not match " + eventStationPattern.pattern());
            }
            writer.close();
        }
        AbstractHibernateDB.rollback();
    }

    AbstractEventChannelPair getECP(String dbid) {
        Query q = AbstractHibernateDB.getSession().createQuery("from " + SodDB.getSingleton().getEcpClass().getName()
                + " where dbid = " + dbid);
        AbstractEventChannelPair esp = (AbstractEventChannelPair)q.uniqueResult();
        return esp;
    }

    Pattern eventStationPattern = Pattern.compile(".*/quake-vectors/([0-9]+)");

    Pattern mseedPattern = Pattern.compile(".*/quake-vectors/([0-9]+)/mseed");
}