package bolts;

import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.streams.Pair;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import redis.clients.jedis.JedisCommands;
import utils.Logger;

import java.util.HashMap;
import java.util.Map;

public class AverageSpeedBolt extends AbstractRedisBolt {
    // Pair contains the last average speed and the total number of samples
    Map<Integer, Pair<Double, Integer>> lastDataMap = new HashMap<>();
    Logger logger;

    public AverageSpeedBolt(JedisPoolConfig config) {
        super(config);
    }

    public AverageSpeedBolt(JedisClusterConfig config) {
        super(config);
    }

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector collector) {
        super.prepare(map, topologyContext, collector);
        this.logger = new Logger("bolts.AverageSpeedBolt");
    }

    @Override
    protected void process(Tuple input) {
        int taxiId = input.getIntegerByField("id");
        double speed = input.getDoubleByField("speed");



        Pair<Double, Integer> lastData = lastDataMap.getOrDefault(taxiId, Pair.of(0., 0));

        final double newAvgSpeed = lastData.value1 + ((speed - lastData.value1) / (double)(lastData.value2 + 1));
        Pair<Double, Integer> newData = Pair.of(newAvgSpeed, lastData.value2 + 1);
        lastDataMap.put(taxiId, newData);

        JedisCommands jedisCommands = null;
        try {
            jedisCommands = getInstance();
            jedisCommands.hset(String.valueOf(taxiId), "average_speed", String.format("%.6f", newAvgSpeed));
            logger.log(String.format("average speed of taxi %d: %.2f km/h", taxiId, newAvgSpeed));
        } finally {
            if (jedisCommands != null) {
                returnInstance(jedisCommands);
            }
            this.collector.ack(input);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) { }
}
