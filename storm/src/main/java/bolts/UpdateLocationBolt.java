package bolts;

import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import redis.clients.jedis.JedisCommands;
import utils.Logger;
import utils.Numbers;

import java.util.Map;



public class UpdateLocationBolt extends AbstractRedisBolt {

    private Logger logger;
    long lastThroughputMeasurementNs = 0;
    long nProcessedTuples = 0;

    public UpdateLocationBolt(JedisPoolConfig config) {
        super(config);
    }

    public UpdateLocationBolt(JedisClusterConfig config) {
        super(config);
    }

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector collector) {
        super.prepare(map, topologyContext, collector);
        this.logger = new Logger("bolts.UpdateLocationBolt");
    }

    @Override
    protected void process(Tuple input) {        
        int taxiId = input.getIntegerByField("taxi_id");
        double latitude = input.getDoubleByField("latitude");
        double longitude = input.getDoubleByField("longitude");

        JedisCommands jedisCommands = null;
        try {
            jedisCommands = getInstance();

            jedisCommands.hset(String.valueOf(taxiId), "location", String.format("%.6f, %.6f", latitude, longitude));
            logger.log(String.format("updated location of taxi %d to %.6f, %.6f", taxiId, latitude, longitude));

        } finally {
            if (jedisCommands != null) {
                returnInstance(jedisCommands);
            }
            this.collector.ack(input);
        }

        sendThroughputLog();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream("performance", new Fields("throughput"));
    }

    private void sendThroughputLog() {
        if (!System.getenv("MODE").equals("DEBUG"))
            return;
        if ((System.nanoTime() - lastThroughputMeasurementNs) > Numbers.THROUGHPUT_CADENCE_NS) {
            this.collector.emit("performance", new Values(nProcessedTuples));
            nProcessedTuples = 0;
            lastThroughputMeasurementNs = System.nanoTime();
        } else {
            nProcessedTuples++;
        }
    }
}
