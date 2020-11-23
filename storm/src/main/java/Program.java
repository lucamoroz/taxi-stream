import bolts.NotifyLeavingAreaBolt;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import spouts.NotifyLeavingAreaSpout;


public class Program {
    public static void main(String[] args) {


        LocalCluster cluster = null;

        JedisPoolConfig poolConfig = new JedisPoolConfig.Builder()
                .setHost("redis").setPort(6379).build();

        TopologyBuilder topoBuilder = new TopologyBuilder();

        KafkaSpoutConfig.Builder<String, String> kafkaSpoutBuilder = KafkaSpoutConfig.builder("kafka:" + "9092", "test");
        kafkaSpoutBuilder.setProp(ConsumerConfig.GROUP_ID_CONFIG, "test");
        kafkaSpoutBuilder.setProp(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        topoBuilder.setSpout("kafkaSpout", new KafkaSpout<>(kafkaSpoutBuilder.build()), 1);

        topoBuilder.setBolt("consoleBolt", new ConsoleBolt())
                .shuffleGrouping("kafkaSpout");
        topoBuilder.setBolt("calculateSpeedBolt", new CalculateSpeedBolt())
                .fieldsGrouping("dataProvider", new Fields("id"));
        topoBuilder.setBolt("averageSpeedBolt", new AverageSpeedBolt())
                .fieldsGrouping("calculateSpeedBolt", new Fields("id"));

        topoBuilder.setBolt("calculateDistanceBolt", new CalculateDistanceBolt())
                .fieldsGrouping("dataProvider", new Fields("id"));

        topoBuilder.setBolt("updateLocationBolt", new UpdateLocationBolt(poolConfig))
                .fieldsGrouping("dataProvider", new Fields("id"));

        topoBuilder.setSpout("notifyLeavingAreaSpout", new NotifyLeavingAreaSpout());
        topoBuilder.setBolt("notifyLeavingAreaBolt", new NotifyLeavingAreaBolt())
                .fieldsGrouping("notifyLeavingAreaSpout", new Fields("id"));
        //TODO: add notify Speeding Bolt from "Calculate Speed" bolt

        try {
            cluster = new LocalCluster();

            Config config = new Config();
            config.setDebug(false);
            config.setMaxSpoutPending(5000);

            cluster.submitTopology("Program", config, topoBuilder.createTopology());
            Thread.sleep(2000000000);
            cluster.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
