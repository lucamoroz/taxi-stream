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
import utils.TransferKafkaObject;

import java.util.Map;

import com.google.gson.Gson;


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
        Gson g = new Gson();
        TransferKafkaObject p = g.fromJson(input.getValue(4).toString(), TransferKafkaObject.class);
        int taxiId = p.getTaxi_id();
        double latitude = Double.parseDouble(p.getLatitude());
        double longitude = Double.parseDouble(p.getLongitude());

        JedisCommands jedisCommands = null;
        try {
            jedisCommands = getInstance();
            // todo define location format
            jedisCommands.hset(String.valueOf(taxiId), "location", String.format("%.6f, %.6f", latitude, longitude));
            logger.log(String.format("updated location of taxy %d to %.6f, %.6f", taxiId, latitude, longitude));

        } finally {
            if (jedisCommands != null) {
                returnInstance(jedisCommands);
            }
            this.collector.ack(input);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
