package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.subsetter.waveFormArm.LocalSeismogramArm;
import org.apache.log4j.Logger;

public class WaveFormArmProcessor extends SodExceptionSource implements Runnable{
    public WaveFormArmProcessor (EventDbObject eventAccess,
                                 EventStationSubsetter eventStationSubsetter,
                                 LocalSeismogramArm localSeismogramArm,
                                 NetworkDbObject networkAccess,
                                 ChannelDbObject[] successfulChannels,
                                 WaveFormArm parent,
                                 SodExceptionListener sodExceptionListener){
        this.eventDbObject = eventAccess;
        this.networkAccess = networkAccess;
        this.eventStationSubsetter = eventStationSubsetter;
        this.localSeismogramArm = localSeismogramArm;
        this.successfulChannels = successfulChannels;
        this.parent = parent;
        addSodExceptionListener(sodExceptionListener);
    }

    public void run() {
        try {
            EventAccessOperations eventAccess = eventDbObject.getEventAccess();
            for(int counter = 0; counter < successfulChannels.length; counter++) {
                EventChannelPair cur = new EventChannelPair(networkAccess,
                                                            eventDbObject,
                                                            successfulChannels[counter],
                                                            parent);
                cur.update("Processing Started", Status.PROCESSING);
                boolean passedESS;
                synchronized(eventStationSubsetter) {
                    try {
                        passedESS = eventStationSubsetter.accept(eventAccess,
                                                                 networkAccess.getNetworkAccess(),
                                                                 successfulChannels[counter].getChannel().my_site.my_station,
                                                                 null);
                        if(!passedESS) {
                            cur.update("Event Station Subsetter Failed",
                                       Status.COMPLETE_REJECT);
                        }else{
                            cur.update("Event Station Subsetter Succeeded",
                                       Status.PROCESSING);
                            try {
                                localSeismogramArm.processLocalSeismogramArm(cur);
                            } catch (Exception e) {
                                cur.update(e,
                                           "System failure in the local seismogram arm processor",
                                           Status.SOD_FAILURE);
                            }
                        }//end of if
                    } catch (Exception e) {
                        cur.update(e,
                                   "System failure in event station subsetter",
                                   Status.SOD_FAILURE);
                    }

                }
            }//end of for
        } catch(Throwable ce) {
            CommonAccess.handleException(ce,
                                         "Waveform processing thread dies unexpectantly.");
        }

    }

    private EventDbObject eventDbObject;

    private NetworkDbObject networkAccess;

    private EventStationSubsetter eventStationSubsetter;

    private LocalSeismogramArm localSeismogramArm;

    private NetworkArm networkArm;

    private ChannelDbObject[] successfulChannels;

    private WaveFormArm parent;

    private static Logger logger = Logger.getLogger(WaveFormArmProcessor.class);
}// WaveFormArmThread
