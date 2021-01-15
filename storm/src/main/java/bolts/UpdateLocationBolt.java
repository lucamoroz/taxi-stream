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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;



public class UpdateLocationBolt extends AbstractRedisBolt {

    private Logger logger;

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
        Instant startTime = Instant.now().truncatedTo(ChronoUnit.NANOS);
        int taxiId = input.getIntegerByField("taxi_id");
        double latitude = input.getDoubleByField("latitude");
        double longitude = input.getDoubleByField("longitude");

        JedisCommands jedisCommands = null;
        try {
            jedisCommands = getInstance();

            jedisCommands.hset(String.valueOf(taxiId), "location", String.format("%.6f, %.6f", latitude, longitude));
            logger.log(String.format("updated location of taxy %d to %.6f, %.6f", taxiId, latitude, longitude));

        } finally {
            if (jedisCommands != null) {
                returnInstance(jedisCommands);
            }
            this.collector.ack(input);
        }
        Instant endTime = Instant.now().truncatedTo(ChronoUnit.NANOS);
        logger.log("Time of execution in nanoseconds: " + endTime.minusNanos(startTime.getNano()));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
