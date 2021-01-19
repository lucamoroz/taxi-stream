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
import utils.Logger;
import utils.Numbers;
import utils.WebsocketClientEndpoint;

import static utils.Numbers.SPEED_LIMIT;

public class NotifySpeedingBolt extends BaseRichBolt {

    private OutputCollector outputCollector;

    private Map<Integer, Long> lastLogs = new HashMap<>();
    private Logger logger;

    private WebsocketClientEndpoint clientEndPoint;

    long lastThroughputMeasurementNs = 0;
    long nProcessedTuples = 0;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext,
                        OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        lastLogs = new HashMap<>();
        this.logger = new Logger("bolts.NotifySpeedingBolt");

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

                this.logger.log("Taxi " + taxiId + " is speeding, implement notification!");
                sendSpeedingMessageToDashboard(true, taxiId);
            }
        } else {
            if( speed.compareTo(SPEED_LIMIT) <= 0 &&
                    lastLogs.get(taxiId) < timestamp){
                lastLogs.remove(taxiId);

                sendSpeedingMessageToDashboard(false, taxiId);
            }
        }

        sendThroughputLog();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //there is only output to the frontend
        outputFieldsDeclarer.declareStream("performance", new Fields("throughput"));
    }

    private void sendSpeedingMessageToDashboard( Boolean speeding, Integer taxiId){

        clientEndPoint.sendMessage("{\"taxi\":\"" + taxiId + "\",\"speeding\":"+ speeding.toString() + "}");
    }

    private void sendThroughputLog() {
        if (!System.getenv("MODE").equals("DEBUG"))
            return;
        if ((System.nanoTime() - lastThroughputMeasurementNs) > Numbers.THROUGHPUT_CADENCE_NS) {
            this.outputCollector.emit("performance", new Values(nProcessedTuples));
            nProcessedTuples = 0;
            lastThroughputMeasurementNs = System.nanoTime();
        } else {
            nProcessedTuples++;
        }
    }
}
