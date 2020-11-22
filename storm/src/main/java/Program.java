import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.*;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;


public class Program {
    public static void main(String[] args) {

        String zookeeperIP = "zookeeper:2181";
        BrokerHosts zkHosts = new ZkHosts(zookeeperIP);

        SpoutConfig kafkaConfig = new SpoutConfig(zkHosts, "test", "", "storm");
        kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        kafkaConfig.startOffsetTime = kafka.api.OffsetRequest.EarliestTime();

        KafkaSpout kafkaSpout = new KafkaSpout(kafkaConfig);
        LocalCluster cluster = null;

        JedisPoolConfig poolConfig = new JedisPoolConfig.Builder()
                .setHost("redis").setPort(6379).build();

        TopologyBuilder topoBuilder = new TopologyBuilder();
//        topoBuilder.setSpout("dataProvider", new DataProvider());
        topoBuilder.setSpout("kafkaSpout", kafkaSpout);

        topoBuilder.setBolt("consoleBolt", new ConsoleBolt())
                .shuffleGrouping("kafkaSpout");
//        topoBuilder.setBolt("calculateSpeedBolt", new CalculateSpeedBolt())
//                .fieldsGrouping("dataProvider", new Fields("id"));
//        topoBuilder.setBolt("averageSpeedBolt", new AverageSpeedBolt())
//                .fieldsGrouping("calculateSpeedBolt", new Fields("id"));
//
//        topoBuilder.setBolt("calculateDistanceBolt", new CalculateDistanceBolt())
//                .fieldsGrouping("dataProvider", new Fields("id"));
//
//        topoBuilder.setBolt("updateLocationBolt", new UpdateLocationBolt(poolConfig))
//                .fieldsGrouping("dataProvider", new Fields("id"));
//
//        topoBuilder.setSpout("notifyLeavingAreaSpout", new NotifyLeavingAreaSpout());
//        topoBuilder.setBolt("notifyLeavingAreaBolt", new NotifyLeavingAreaBolt())
//                .fieldsGrouping("notifyLeavingAreaSpout", new Fields("id"));
        //TODO: add notify Speeding Bolt from "Calculate Speed" bolt

        try {
            cluster = new LocalCluster();

            Config config = new Config();
            config.setDebug(false);
            config.setMaxSpoutPending(5000);

            cluster.submitTopology("Program", config, topoBuilder.createTopology());
            Thread.sleep(20000);
            cluster.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
