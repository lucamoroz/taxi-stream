package bolts;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import utils.CoordinateHelper;
import utils.Logger;
import utils.TaxiLog;
import utils.WebsocketClientEndpoint;

public class NotifyLeavingAreaBolt extends BaseRichBolt {

    private OutputCollector outputCollector;
    private Map<Integer, TaxiLog> lastLogs = new HashMap<>();
    private Logger logger;


    private Double latitudeBeijing = 39.916668;
    private Double longitudeBeijing = 116.383331;

    private TaxiLog centerBeijingLocation;

    private Integer maxDistanceToBeijingCenterMeter = 10000;

    
    private WebsocketClientEndpoint clientEndPoint;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext,
    OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        lastLogs = new HashMap<>();

        centerBeijingLocation = new TaxiLog(0, longitudeBeijing, latitudeBeijing);
        this.logger = new Logger("bolts.NotifyLeavingAreaBolt");

        try {
            // open websocket
            this.clientEndPoint = new WebsocketClientEndpoint(new URI("ws://dashboard-backend:8082"));

            // add listener
            clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    System.out.println(message);
                }
            });

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
        Double distanceToBeijingCenterMeter = 0.;
        distanceToBeijingCenterMeter = CoordinateHelper.calculateDistance(currentLog, centerBeijingLocation);

        if (!lastLogs.containsKey(taxiId)) {
            
            if (distanceToBeijingCenterMeter > maxDistanceToBeijingCenterMeter) {

                this.logger.log("Taxi " + taxiId + " is leaving a predefined area!");

                this.lastLogs.put(taxiId, currentLog);
                
                sendLeavingAreaMessageToDashboard(true, taxiId);

            } 
        }else {
                
                TaxiLog existingLog = this.lastLogs.get(taxiId);

                if (distanceToBeijingCenterMeter <= maxDistanceToBeijingCenterMeter &&
                    existingLog.getTimestamp() <= currentLog.getTimestamp()) {

                    this.logger.log("Taxi " + taxiId + " is inside the predefined area again");

                    this.lastLogs.remove(taxiId);

                    sendLeavingAreaMessageToDashboard(false, taxiId);
                }
            }

        }
    
        
    

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //there is only output to the frontend
    }

    private void sendLeavingAreaMessageToDashboard( Boolean leavingArea, Integer taxiId){
        
        clientEndPoint.sendMessage("{\"taxi\":\"" + taxiId + "\",\"leavingArea\":"+ leavingArea.toString() + "}");
    }

}
