package bolts;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import org.apache.storm.tuple.Values;
import utils.*;

import static utils.Numbers.*;

public class NotifyLeavingAreaBolt extends BaseRichBolt {

    private OutputCollector outputCollector;
    private Map<Integer, TaxiLog> lastLogs = new HashMap<>();
    private Logger logger;

    private TaxiLog centerBeijingLocation;
    
    private WebsocketClientEndpoint clientEndPoint;

    long lastThroughputMeasurementNs = 0;
    long nProcessedTuples = 0;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext,
    OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        lastLogs = new HashMap<>();

        centerBeijingLocation = new TaxiLog(0, LONG_BEIJING, LAT_BEIJING);
        this.logger = new Logger("bolts.NotifyLeavingAreaBolt");

        try {
            // open websocket
            this.clientEndPoint = new WebsocketClientEndpoint(new URI("ws://dashboard-backend:8082"));

            // add listener
            clientEndPoint.addMessageHandler(message -> logger.log(message));

        } catch (URISyntaxException ex) {
            this.logger.log("URISyntaxException exception: " + ex.getMessage());
        }
    }

    @Override
    public void execute(Tuple tuple) {

        int taxiId = tuple.getIntegerByField("taxi_id");
        Double longitude = tuple.getDoubleByField("longitude");
        Double latitude = tuple.getDoubleByField("latitude");
        long timestamp = tuple.getLongByField("timestamp");

        TaxiLog currentLog = new TaxiLog(timestamp, latitude, longitude);
        double distanceToBeijingCenterMeter = CoordinateHelper.calculateDistance(currentLog, centerBeijingLocation);

        if (!lastLogs.containsKey(taxiId)) {
            
            if (distanceToBeijingCenterMeter > MAX_DISTANCE_TO_CENTER) {

                this.logger.log("Taxi " + taxiId + " is leaving a predefined area!");

                this.lastLogs.put(taxiId, currentLog);
                
                sendLeavingAreaMessageToDashboard(true, taxiId);

            } 
        }else {
                
                TaxiLog existingLog = this.lastLogs.get(taxiId);

                if (distanceToBeijingCenterMeter <= MAX_DISTANCE_TO_CENTER &&
                    existingLog.getTimestamp() <= currentLog.getTimestamp()) {

                    this.logger.log("Taxi " + taxiId + " is inside the predefined area again");

                    this.lastLogs.remove(taxiId);

                    sendLeavingAreaMessageToDashboard(false, taxiId);
                }
            }

            if ((System.nanoTime() - lastThroughputMeasurementNs) > Numbers.THROUGHPUT_CADENCE_NS) {
                this.outputCollector.emit("performance", new Values(nProcessedTuples));
                nProcessedTuples = 0;
                lastThroughputMeasurementNs = System.nanoTime();
            } else {
                nProcessedTuples++;
            }

        }
    
        
    

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //there is only output to the frontend
        outputFieldsDeclarer.declareStream("performance", new Fields("throughput"));
    }

    private void sendLeavingAreaMessageToDashboard( Boolean leavingArea, Integer taxiId){
        
        clientEndPoint.sendMessage("{\"taxi\":\"" + taxiId + "\",\"leavingArea\":"+ leavingArea.toString() + "}");
    }

}
