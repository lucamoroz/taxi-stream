package bolts;

import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import redis.clients.jedis.JedisCommands;
import utils.WriteToCSV;
import utils.CoordinateHelper;
import utils.Logger;
import utils.TaxiLog;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class CalculateDistanceBolt extends AbstractRedisBolt {
    Map<Integer, Object[]> overallDistances = new HashMap<>();
    Logger logger;
    WriteToCSV writeToCSV;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector collector) {
        super.prepare(map, topologyContext, collector);
        this.logger = new Logger("bolts.CalculateDistanceBolt");
        this.writeToCSV = WriteToCSV.createWriteToCSV();
    }

    public CalculateDistanceBolt(JedisPoolConfig config) {
        super(config);
    }

    public CalculateDistanceBolt(JedisClusterConfig config) {
        super(config);
    }

    @Override
    protected void process(Tuple input) {
        int taxiId = input.getIntegerByField("taxi_id");
        double latitude = input.getDoubleByField("latitude");
        double longitude = input.getDoubleByField("longitude");
        long timestamp = input.getLongByField("timestamp");

        TaxiLog currentLog = new TaxiLog(timestamp, latitude, longitude);

        double currentOverallDistance = 0;

        if (overallDistances.containsKey(taxiId)) {
            currentOverallDistance = (double) overallDistances.get(taxiId)[0];
            TaxiLog lastLog = (TaxiLog) overallDistances.get(taxiId)[1];
            currentOverallDistance += CoordinateHelper.calculateDistance(lastLog, currentLog) / 1000d;
        }

        overallDistances.put(taxiId, new Object[]{currentOverallDistance, currentLog});

        JedisCommands jedisCommands = null;
        try {
            jedisCommands = getInstance();
            jedisCommands.hset(String.valueOf(taxiId), "overall_distance", String.format("%.6f", currentOverallDistance));
            logger.log(String.format("overall distance of taxi %d: %.6f km", taxiId, currentOverallDistance));

        } finally {
            if (jedisCommands != null) {
                returnInstance(jedisCommands);
            }
            this.collector.ack(input);
        }
        
        long endTime = System.currentTimeMillis();
        try{
            String id = String.valueOf(taxiId);
            String time = String.valueOf(endTime - input.getLongByField("startTime"));
            this.writeToCSV.writeToFile(id, "CalculateDistanceBolt", time);
        } catch (Exception ex){
            this.logger.log("Error while writing to CSV: " + ex.toString());
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) { }
}