package edu.sc.seis.sod.velocity;

import java.io.StringWriter;
import java.util.Properties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.RuntimeConstants;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.CookieJar;

/**
 * @author groves
 * 
 * Created on May 25, 2005
 */
public class SimpleVelocitizer {

    public String evaluate(String template, Channel chan) {
        return evaluate(template, ContextWrangler.createContext(chan));
    }

    public String evaluate(String template, EventAccessOperations event) {
        return evaluate(template, ContextWrangler.createContext(event));
    }

    public String evaluate(String template,
                           EventAccessOperations event,
                           Channel channel,
                           RequestFilter[] original,
                           RequestFilter[] available,
                           LocalSeismogramImpl[] seismograms,
                           CookieJar cookieJar) {
        return evaluate(template, ContextWrangler.createContext(event,
                                                                channel,
                                                                original,
                                                                available,
                                                                seismograms,
                                                                cookieJar));
    }

    public String evaluate(String template, VelocityContext ctx) {
        StringWriter writer = new StringWriter();
        try {
            try {
                Velocity.evaluate(ctx, writer, "SimpleVelocitizer", template);
            } catch(ParseErrorException parseError) {
                return parseError.getMessage();
            }
            return writer.toString();
        } catch(Exception e) {
            GlobalExceptionHandler.handle(e);
            return "Unable to evaluate " + template;
        }
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SimpleVelocitizer.class);
    static {
        try {
            Properties props = new Properties();
            props.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                              "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            props.setProperty("runtime.log.logsystem.log4j.category",
                              logger.getName());
            props.setProperty("velocimacro.library", "");
            Velocity.init(props);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble initializing velocity", e);
        }
    }
}
