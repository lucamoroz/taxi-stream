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


    private Double latitudeBeijing = 39.9075;
    private Double longitudeBeijing = 116.39723;

    private TaxiLog centerBeijingLocation;

    private Integer maxDistanceToBeijingCenterKiloMeter = 10;

    
    private WebsocketClientEndpoint clientEndPoint;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext,
    OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        lastLogs = new HashMap<>();

        

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
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }


        lastLogs = new HashMap<>();

        centerBeijingLocation = new TaxiLog(0, longitudeBeijing, latitudeBeijing);
        this.logger = new Logger("bolts.NotifyLeavingAreaBolt");
    }

    @Override
    public void execute(Tuple tuple) {

        int taxiId = tuple.getIntegerByField("taxi_id");
        Double longitude = tuple.getDoubleByField("longitude");
        Double latitude = tuple.getDoubleByField("latitude");
        long timestamp = tuple.getLongByField("timestamp");

        this.logger.log("Before going to SendViaTCP");
        

        clientEndPoint.sendMessage("{'event':'addChannel','channel':'ok_btccny_ticker'}");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        TaxiLog currentLog = new TaxiLog(timestamp, latitude, longitude);
        Double distanceToBeijingCenterMeter = 0.;
        distanceToBeijingCenterMeter = CoordinateHelper.calculateDistance(currentLog, centerBeijingLocation);


        //TODO: include timestamp check
        if (!lastLogs.containsKey(taxiId)) {
            if (distanceToBeijingCenterMeter > maxDistanceToBeijingCenterKiloMeter) {
                this.logger.log("Taxi " + taxiId + " is leaving a predefined area!");

                this.lastLogs.put(taxiId, currentLog);


            } else {
                TaxiLog existingLog = this.lastLogs.get(taxiId);

                if (distanceToBeijingCenterMeter <= maxDistanceToBeijingCenterKiloMeter &&
                        existingLog.getTimestamp() <= currentLog.getTimestamp()) {

                    this.logger.log("Taxi " + taxiId + " is inside the predefined area again");

                    this.lastLogs.remove(taxiId);
                    
                }
            }

        }
    }
        
    

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //there is only output to the frontend
    }

}
