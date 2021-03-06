package edu.sc.seis.sod.velocity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;
import edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC.MockSeismogram;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.network.VelocityNetwork;
import edu.sc.seis.sod.velocity.network.VelocityStation;
import edu.sc.seis.sod.velocity.seismogram.VelocitySeismogram;

/**
 * Handles getting stuff in the context and directing output to System.out or a
 * file for the printlineprocess classes
 * 
 * @author groves
 * 
 * Created on May 30, 2005
 */
public class PrintlineVelocitizer {

    static VelocityContext mockContext = new VelocityContext();
    
    static {
        VelocityEvent event = new VelocityEvent(MockEventAccessOperations.createEvent());
        ChannelImpl chan = MockChannel.createChannel();
        mockContext.put("event", event);
                mockContext.put("channel", new VelocityChannel(chan));
        mockContext.put("station", new VelocityStation(MockStation.createStation()));
        mockContext.put("net", new VelocityNetwork((NetworkAttrImpl)MockChannel.createChannel().getNetworkAttr()));
        List<LocalSeismogramImpl> seisList = new ArrayList<LocalSeismogramImpl>();
        seisList.add(new VelocitySeismogram(MockSeismogram.createSpike(chan.getId()), chan));
        mockContext.put("seismograms", seisList);
        mockContext.put("index", new Integer(1));
    }
    
    /**
     * Evaluates the templates such that errors might be discovered
     */
    public PrintlineVelocitizer(String[] strings) throws ConfigurationException {
        if ( System.getProperty("printlinevelocitizer.check") == null 
                || ! System.getProperty("printlinevelocitizer.check").equalsIgnoreCase("false")) {
        for(int i = 0; i < strings.length; i++) {
            try {
                StringWriter stringWriter = new StringWriter();
                Velocity.evaluate(mockContext,
                                  stringWriter,
                                  "PrintlineTest",
                                  strings[i]);
                logger.debug("PrintlineVelocitizer: "+strings[i]+" Result: "+stringWriter.toString());
            } catch(ParseErrorException e) {
                throw new UserConfigurationException("Malformed Velocity '"
                        + strings[i] + "'.  " + e.getMessage());
            } catch(Exception e) {
                throw new ConfigurationException("Exception caused by testing Velocity",
                                                 e);
            }
        }
        }
    }

    public String evaluate(String fileTemplate,
                           String template,
                           NetworkAttr attr) throws IOException {
        return evalulate(fileTemplate,
                         template,
                         ContextWrangler.createContext(attr));
    }

    public String evaluate(String fileTemplate, String template, Channel chan)
            throws IOException {
        return evalulate(fileTemplate,
                         template,
                         ContextWrangler.createContext(chan));
    }

    public String evaluate(String fileTemplate, String template, StationImpl sta)
            throws IOException {
        return evalulate(fileTemplate,
                         template,
                         ContextWrangler.createContext(sta));
    }

    public String evaluate(String fileTemplate, String template, 
                           EventAccessOperations event, StationImpl sta, CookieJar cookieJar)
            throws IOException {
        VelocityContext cntxt = ContextWrangler.createContext(sta);
        ContextWrangler.insertIntoContext(event, cntxt);
        cntxt.put("cookieJar", cookieJar);
        return evalulate(fileTemplate,
                         template,
                         cntxt);
    }

    public String evaluate(String filename,
                           String template,
                           EventAccessOperations event,
                           ChannelImpl channel,
                           RequestFilter[] request,
                           CookieJar cookieJar) throws IOException {
        return evaluate(filename,
                        template,
                        event,
                        channel,
                        request,
                        new RequestFilter[0],
                        cookieJar);
    }

    public String evaluate(String filename,
                           String template,
                           EventAccessOperations event,
                           ChannelImpl channel,
                           RequestFilter[] original,
                           RequestFilter[] available,
                           CookieJar cookieJar) throws IOException {
        return evaluate(filename,
                        template,
                        event,
                        channel,
                        original,
                        available,
                        new LocalSeismogramImpl[0],
                        cookieJar);
    }

    public String evaluate(String fileTemplate,
                           String template,
                           EventAccessOperations event,
                           ChannelImpl channel,
                           RequestFilter[] original,
                           RequestFilter[] available,
                           LocalSeismogramImpl[] seismograms,
                           CookieJar cookieJar) throws IOException {
        return evalulate(fileTemplate,
                         template,
                         ContextWrangler.createContext(event,
                                                       channel,
                                                       original,
                                                       available,
                                                       seismograms,
                                                       cookieJar));
    }

    public String evaluate(String fileTemplate,
                           String template,
                           EventAccessOperations event) throws IOException {
        return evalulate(fileTemplate,
                         template,
                         ContextWrangler.createContext(event));
    }

    public String evalulate(String fileTemplate,
                            String template,
                            VelocityContext ctx) throws IOException {
        String result = simple.evaluate(template, ctx);
        if(fileTemplate.equals("")) {
            System.out.println(result);
        } else {
            appendToFile(fileTemplate, result, ctx);
        }
        return result;
    }

    private void appendToFile(String fileTemplate,
                              String toAppend,
                              VelocityContext ctx) throws IOException {
        String filename = FissuresFormatter.filize(simple.evaluate(fileTemplate,
                                                                   ctx));
        File file = new File(filename);
        file.getAbsoluteFile().getParentFile().mkdirs();
        FileWriter fwriter = new FileWriter(file, true);
        BufferedWriter bwriter = null;
        try {
            bwriter = new BufferedWriter(fwriter);
            bwriter.write(toAppend);
            bwriter.newLine();
        } finally {
            if(bwriter != null) {
                bwriter.close();
            }
        }
    }
    
    private SimpleVelocitizer simple = new SimpleVelocitizer();

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PrintlineVelocitizer.class);
}
