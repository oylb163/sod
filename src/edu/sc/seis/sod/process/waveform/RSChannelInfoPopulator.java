package edu.sc.seis.sod.process.waveform;

import java.awt.Dimension;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.fissuresUtil.display.registrar.CustomLayOutConfig;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.DataSetToXML;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.database.waveform.JDBCRecordSectionChannel;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.ConfigurationException;

/**
 * @author danala Created on Mar 30, 2005
 */
public class RSChannelInfoPopulator implements WaveformProcess {

    public RSChannelInfoPopulator(Element config) throws Exception {
        initConfig(config);
        saveSeisToFile = getSaveSeismogramToFile();
        recordSectionChannel = new JDBCRecordSectionChannel();
        eventAccess = new JDBCEventAccess();
        channel = new JDBCChannel();
    }

    private void initConfig(Element config) throws NoSuchFieldException {
        id = SodUtil.getText(SodUtil.getElement(config, "id"));
        saveSeisId = DOMHelper.extractText(config, "saveSeisId", id);
        if(DOMHelper.hasElement(config, "percentSeisHeight")) {
            percentSeisHeight = new Double(SodUtil.getText(SodUtil.getElement(config,
                                                                              "percentSeisHeight"))).doubleValue();
        }
        int idealNumberOfSeismograms = 10;
        if(DOMHelper.hasElement(config, "idealNumberOfSeismograms")) {
            String idealNumText = SodUtil.getText(SodUtil.getElement(config,
                                                                     "idealNumberOfSeismograms"));
            idealNumberOfSeismograms = new Integer(idealNumText).intValue();
        }
        int maxNumberOfSeismograms = idealNumberOfSeismograms + 5;
        if(DOMHelper.hasElement(config, "maxNumberOfSeismograms")) {
            String maxSeisText = SodUtil.getText(SodUtil.getElement(config,
                                                                    "maximumSeismogramsPerRecordSection"));
            maxNumberOfSeismograms = new Integer(maxSeisText).intValue();
        }
        if(DOMHelper.hasElement(config, "distanceRange")) {
            distRange = new DistanceRange(SodUtil.getElement(config,
                                                             "distanceRange"));
        }
        if(DOMHelper.hasElement(config, "recordSectionSize")) {
            int width = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                          "recordSectionSize"),
                                                                       "width"))).intValue();
            int height = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                           "recordSectionSize"),
                                                                        "height"))).intValue();
            recSecDim = new Dimension(width, height);
        }
        spacer = new RecordSectionSpacer(distRange,
                                         idealNumberOfSeismograms,
                                         maxNumberOfSeismograms);
        if(DOMHelper.hasElement(config, "displayConfig")) {
            displayCreator = SeismogramDisplayConfiguration.create(DOMHelper.getElement(config,
                                                                                        "displayConfig"));
        }
    }

    public Dimension getRecSecDimension() {
        return recSecDim;
    }

    public SaveSeismogramToFile getSaveSeismogramToFile() throws Exception {
        return getSaveSeismogramToFile(saveSeisId);
    }

    public SaveSeismogramToFile getSaveSeismogramToFile(String id)
            throws Exception {
        Element docElement = Start.getDocument().getDocumentElement();
        Element saveSeisConf = (Element)XPathAPI.selectSingleNode(docElement,
                                                                  "//saveSeismogramToFile[id/text() = \""
                                                                          + id
                                                                          + "\"]");
        if(saveSeisConf != null) {
            return new SaveSeismogramToFile(saveSeisConf);
        } else {
            throw new ConfigurationException("No SaveSeismogramToFile element with id "
                    + id + " found");
        }
    }

    public MemoryDataSetSeismogram[] wrap(DataSetSeismogram[] dss)
            throws Exception {
        MemoryDataSetSeismogram[] memDss = new MemoryDataSetSeismogram[dss.length];
        for(int i = 0; i < memDss.length; i++) {
            memDss[i] = new MemoryDataSetSeismogram(((URLDataSetSeismogram)dss[i]).getSeismograms(),
                                                    dss[i].getDataSet(),
                                                    dss[i].getName());
        }
        return memDss;
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel chan,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        acceptableChannels.add(ChannelIdUtil.toString(chan.get_id()));
        int eq_dbid = eventAccess.getDBId(event);
        DataSetSeismogram[] dss = extractSeismograms(event);
        ArrayList acceptableSeis = new ArrayList();
        for(int i = 0; i < dss.length; i++) {
            if(acceptableChannels.contains(ChannelIdUtil.toString(dss[i].getChannelId()))) {
                acceptableSeis.add(dss[i]);
            }
        }
        dss = (DataSetSeismogram[])acceptableSeis.toArray(new DataSetSeismogram[0]);
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.add(wrap(dss));
        File temp = File.createTempFile("tempRecSec", "png");
        rsDisplay.outputToPNG(temp);
        temp.delete();
        HashMap pixelMap = rsDisplay.getPixelMap();
        int[] channelIds = getChannelDBIds(dss);
        for(int j = 0; j < channelIds.length; j++) {
            if(!recordSectionChannel.channelExists(id, eq_dbid, channelIds[j])) {
                recordSectionChannel.insert(id,
                                            eq_dbid,
                                            channelIds[j],
                                            (double[])pixelMap.get(dss[j].getRequestFilter().channel_id),
                                            0);
            }
        }
        DataSetSeismogram[] bestSeismos = spacer.spaceOut(dss);
        recordSectionChannel.updateChannels(id,
                                            eq_dbid,
                                            getChannelDBIds(bestSeismos));
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public int[] getChannelDBIds(DataSetSeismogram[] dss) throws SQLException,
            NotFound {
        int[] channelIds = new int[dss.length];
        for(int j = 0; j < dss.length; j++) {
            ChannelId channel_id = dss[j].getRequestFilter().channel_id;
            channelIds[j] = channel.getDBId(channel_id);
        }
        return channelIds;
    }

    public DataSetSeismogram[] extractSeismograms(EventAccessOperations eao)
            throws Exception {
        DataSet ds = DataSetToXML.load(saveSeisToFile.getDSMLFile(eao)
                .toURI()
                .toURL());
        String[] dataSeisNames = ds.getDataSetSeismogramNames();
        DataSetSeismogram[] dss = new DataSetSeismogram[dataSeisNames.length];
        for(int i = 0; i < dataSeisNames.length; i++) {
            dss[i] = ds.getDataSetSeismogram(dataSeisNames[i]);
        }
        return dss;
    }

    public RecordSectionDisplay getConfiguredRSDisplay() {
        RecordSectionDisplay rsDisplay = (RecordSectionDisplay)displayCreator.createDisplay();
        CustomLayOutConfig custConfig = new CustomLayOutConfig(distRange.getMinDistance(),
                                                               distRange.getMaxDistance(),
                                                               percentSeisHeight);
        custConfig.setSwapAxes(rsDisplay.getSwapAxes());
        rsDisplay.setLayout(custConfig);
        return rsDisplay;
    }

    private SaveSeismogramToFile saveSeisToFile;

    private String id, saveSeisId;

    private DistanceRange distRange = new DistanceRange(0, 180);

    private JDBCRecordSectionChannel recordSectionChannel;

    private JDBCEventAccess eventAccess;

    private JDBCChannel channel;

    private double percentSeisHeight = 10;

    private Dimension recSecDim = new Dimension(500, 500);

    protected RecordSectionSpacer spacer;

    private SeismogramDisplayConfiguration displayCreator;

    private Set acceptableChannels = new HashSet();
}