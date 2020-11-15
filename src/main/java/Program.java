import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;


public class Program {
    public static void main(String[] args) {
        LocalCluster cluster = null;

        JedisPoolConfig poolConfig = new JedisPoolConfig.Builder()
                .setHost("redis").setPort(6379).build();

        TopologyBuilder topoBuilder = new TopologyBuilder();
        topoBuilder.setSpout("dataProvider", new DataProvider());
        topoBuilder.setBolt("calculateSpeedBolt", new CalculateSpeedBolt())
                .fieldsGrouping("dataProvider", new Fields("id"));
        topoBuilder.setBolt("distanceBolt", new DistanceBolt())
                .fieldsGrouping("dataProvider", new Fields("id"));
        topoBuilder.setBolt("updateLocationBolt", new UpdateLocationBolt(poolConfig))
                .fieldsGrouping("dataProvider", new Fields("id"));
        topoBuilder.setBolt("consoleBolt", new ConsoleBolt())
                .shuffleGrouping("distanceBolt");

        try {
            cluster = new LocalCluster();

            Config config = new Config();
            config.setDebug(false);

            cluster.submitTopology("Program", config, topoBuilder.createTopology());
            Thread.sleep(20000);
            cluster.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
