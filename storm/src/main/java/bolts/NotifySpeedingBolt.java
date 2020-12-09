package bolts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import utils.Logger;

public class NotifySpeedingBolt extends BaseRichBolt {

    OutputCollector outputCollector;

    Map<Integer, Long> lastLogs = new HashMap<>();
    Logger logger;

    Double speedLimitMPerSecond;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext,
        OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        lastLogs = new HashMap<>();
        speedLimitMPerSecond = 13.89;
    }

    //TODO: check, whether there are race conditions (include timestamp)
    @Override
    public void execute(Tuple tuple) {

        int taxiId = tuple.getIntegerByField("id");

        Double speed = tuple.getDoubleByField("speed");

        long timestamp = tuple.getLongByField("timestamp");

        if(!lastLogs.containsKey(taxiId) && speed > speedLimitMPerSecond){

            //TODO: add date
            lastLogs.put(taxiId, timestamp);

            System.out.println("Taxi " + taxiId + " is speeding, implement notification!");
            //TODO: implement frontend notification


        } else {
            if( speed <= speedLimitMPerSecond &&
            lastLogs.get(taxiId) < timestamp){
                lastLogs.remove(taxiId);
            }
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //there is only output to the frontend
    }
}
