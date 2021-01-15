package bolts;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import utils.WriteToCSV;
import utils.Logger;
import utils.WebsocketClientEndpoint;

import static utils.Numbers.SPEED_LIMIT;

public class NotifySpeedingBolt extends BaseRichBolt {

    private OutputCollector outputCollector;

    private Map<Integer, Long> lastLogs = new HashMap<>();
    private Logger logger;
    private WriteToCSV writeToCSV;

    private WebsocketClientEndpoint clientEndPoint;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext,
                        OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        lastLogs = new HashMap<>();
        this.logger = new Logger("bolts.NotifySpeedingBolt");
        this.writeToCSV = WriteToCSV.createWriteToCSV();

        try {
            // open websocket
            this.clientEndPoint = new WebsocketClientEndpoint(new URI("ws://dashboard-backend:8083"));

            // add listener
            clientEndPoint.addMessageHandler(message -> logger.log(message));

        } catch (URISyntaxException ex) {
            this.logger.log("URISyntaxException exception: " + ex.getMessage());
        }
    }

    @Override
    public void execute(Tuple tuple) {

        int taxiId = tuple.getIntegerByField("id");

        Double speed = tuple.getDoubleByField("speed");

        long timestamp = tuple.getLongByField("timestamp");

        if(!lastLogs.containsKey(taxiId)){
            if (speed.compareTo(SPEED_LIMIT) > 0) {
                lastLogs.put(taxiId, timestamp);

                this.logger.log("Taxi " + taxiId + " is speeding!");
                sendSpeedingMessageToDashboard(true, taxiId);
            }
        } else {
            if( speed.compareTo(SPEED_LIMIT) <= 0 &&
                    lastLogs.get(taxiId) < timestamp){
                lastLogs.remove(taxiId);

                sendSpeedingMessageToDashboard(false, taxiId);
            }
        }
        
        long endTime = System.currentTimeMillis();
        try{
            String id = String.valueOf(taxiId);
            String time = String.valueOf(endTime - tuple.getLongByField("startTime"));
            this.writeToCSV.writeToFile(id, "NotifySpeedingBolt", time);
        } catch (Exception ex){
            this.logger.log("Error while writing to CSV: " + ex.toString());
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //there is only output to the frontend
    }

    private void sendSpeedingMessageToDashboard( Boolean speeding, Integer taxiId){

        clientEndPoint.sendMessage("{\"taxi\":\"" + taxiId + "\",\"speeding\":"+ speeding.toString() + "}");
    }
}
