//import org.apache.storm.redis.bolt.AbstractRedisBolt;
//import org.apache.storm.redis.common.config.JedisClusterConfig;
//import org.apache.storm.redis.common.config.JedisPoolConfig;
//import org.apache.storm.topology.OutputFieldsDeclarer;
//import org.apache.storm.tuple.Tuple;
//import redis.clients.jedis.JedisCommands;
//
//
//public class UpdateLocationBolt extends AbstractRedisBolt {
//
//    public UpdateLocationBolt(JedisPoolConfig config) {
//        super(config);
//    }
//
//    public UpdateLocationBolt(JedisClusterConfig config) {
//        super(config);
//    }
//
//    @Override
//    protected void process(Tuple input) {
//        System.out.println("UpdateLocationBolt received: " + input.toString());
//
//        int taxiId = input.getInteger(0);
//        double latitude = input.getDouble(1);
//        double longitude = input.getDouble(2);
//
//        JedisCommands jedisCommands = null;
//        try {
//            jedisCommands = getInstance();
//            // todo define location format
//            jedisCommands.hset(String.valueOf(taxiId), "location", String.format("%.6f, %.6f", latitude, longitude));
//
//        } finally {
//            if (jedisCommands != null) {
//                returnInstance(jedisCommands);
//            }
//            this.collector.ack(input);
//        }
//    }
//
//    @Override
//    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
//
//    }
//}
