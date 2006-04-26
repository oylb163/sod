package edu.sc.seis.sod.process.waveform;

import java.io.File;
import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.ContextWrangler;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public abstract class AbstractSeismogramWriter implements WaveformProcess {

    private String fileTemplate;

    public AbstractSeismogramWriter(String fileTemplate) {
        this.fileTemplate = fileTemplate;
    }

    public void removeExisting(String base) {
        File baseFile = new File(base);
        int count = 1;
        while(baseFile.exists()) {
            baseFile.delete();
            baseFile = new File(generateFile(base, count++));
        }
    }

    public String[] generateLocations(String base, int length) {
        String[] locs = new String[length];
        for(int i = 0; i < locs.length; i++) {
            locs[i] = generateFile(base, i);
        }
        return locs;
    }

    private String generateFile(String base, int position) {
        if(position == 0) {
            return base;
        }
        return base + "." + position;
    }

    public String generateBase(EventAccessOperations event,
                               Channel channel,
                               LocalSeismogramImpl representativeSeismogram) {
        VelocityContext ctx = ContextWrangler.createContext(event);
        ContextWrangler.insertIntoContext(representativeSeismogram,
                                          channel,
                                          ctx);
        return FissuresFormatter.filize(velocitizer.evaluate(fileTemplate,
                                                                            ctx));
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        if(seismograms.length > 0) {
            String base = generateBase(event, channel, seismograms[0]);
            removeExisting(base);
            String[] locs = generateLocations(base, seismograms.length);
            for(int i = 0; i < locs.length; i++) {
                File parent = new File(locs[i]).getParentFile();
                if(!parent.exists() && !parent.mkdirs()) {
                    StringTreeLeaf reason = new StringTreeLeaf(this,
                                                               false,
                                                               "Unable to create directory "
                                                                       + parent);
                    return new WaveformResult(seismograms, reason);
                }
                write(locs[i], seismograms[i], channel, event);
            }
        }
        return new WaveformResult(true, seismograms, this);
    }

    public abstract void write(String loc,
                               LocalSeismogramImpl seis,
                               Channel chan,
                               EventAccessOperations ev) throws Exception;

    private SimpleVelocitizer velocitizer = new SimpleVelocitizer();

    protected static String extractFileTemplate(Element el, String def) {
        return DOMHelper.extractText(el, "location", def);
    }
}
