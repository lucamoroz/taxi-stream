import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import redis.clients.jedis.JedisCommands;


public class UpdateLocationBolt extends AbstractRedisBolt {

    public UpdateLocationBolt(JedisPoolConfig config) {
        super(config);
    }

    public UpdateLocationBolt(JedisClusterConfig config) {
        super(config);
    }

    @Override
    protected void process(Tuple input) {
        System.out.println("UpdateLocationBolt received: " + input.toString());
        JedisCommands jedisCommands = null;
        try {
            jedisCommands = getInstance();

            jedisCommands.hset("taxiid", "currentLocation", "location");

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
