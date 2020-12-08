package bolts;

import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import redis.clients.jedis.JedisCommands;
import utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AverageSpeedBolt extends AbstractRedisBolt {
    Map<Integer, List<Double>> lastSpeeds = new HashMap<>();
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

        // todo avg speed can be computed with constant memory usage, see:
        //  https://math.stackexchange.com/questions/106700/incremental-averageing
        List<Double> speeds;

        if (lastSpeeds.containsKey(taxiId)) {
            speeds = lastSpeeds.get(taxiId);
        }else{
            speeds = new ArrayList();
            lastSpeeds.put(taxiId, speeds);
        }

        speeds.add(speed);
        double averageSpeed = speeds.stream().reduce(0d, Double::sum) / speeds.size();


        JedisCommands jedisCommands = null;
        try {
            jedisCommands = getInstance();
            jedisCommands.hset(String.valueOf(taxiId), "average_speed", String.format("%.6f", averageSpeed));
            logger.log(String.format("average speed of taxi %d: %.2f km/h", taxiId, averageSpeed));
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
