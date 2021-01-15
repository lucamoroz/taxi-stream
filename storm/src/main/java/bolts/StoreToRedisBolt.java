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
import utils.WriteToCSV;

import java.util.Map;

public class StoreToRedisBolt extends AbstractRedisBolt {

    private Logger logger;
    private WriteToCSV writeToCSV;

    public StoreToRedisBolt(JedisPoolConfig config) {
        super(config);
    }

    public StoreToRedisBolt(JedisClusterConfig config) {
        super(config);
    }

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector collector) {
        super.prepare(map, topologyContext, collector);
        this.logger = new Logger("bolts.StoreToRedisBolt");
        this.writeToCSV = WriteToCSV.createWriteToCSV();
    }

    @Override
    protected void process(Tuple tuple) {

        JedisCommands jedisCommands = null;
        try {
            jedisCommands = getInstance();

            jedisCommands.hset(String.valueOf(tuple.getIntegerByField("id")),
                    tuple.getStringByField("type"),
                    tuple.getStringByField("value"));
            logger.log("Taxi id: " + tuple.getIntegerByField("id") + "; Type : " + tuple.getStringByField("type") +
                    "; Value: " + tuple.getStringByField("value") + " has been added to redis.");
        } finally {
            if (jedisCommands != null) {
                returnInstance(jedisCommands);
            }
            this.collector.ack(tuple);
        }

        long endTime = System.currentTimeMillis();
        try{
            String id = String.valueOf(tuple.getIntegerByField("id"));
            String time = String.valueOf(endTime - tuple.getLongByField("startTime"));
            this.writeToCSV.writeToFile(id, tuple.getStringByField("type") + " in StoredRedisBolt", time);
        } catch (Exception ex){
            this.logger.log("Error while writing to CSV: " + ex.toString());
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
