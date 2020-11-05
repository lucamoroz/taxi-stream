import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.ConfigurableTopology;
import org.apache.storm.topology.TopologyBuilder;

import java.util.HashMap;

public class Program {
    public static void main(String[] args) {
        try (LocalCluster cluster = new LocalCluster()) {
            TopologyBuilder topoBuilder = new TopologyBuilder();
            topoBuilder.setSpout("dataProvider", new DataProvider());
            topoBuilder.setBolt("consoleBolt", new ConsoleBolt())
                    .shuffleGrouping("dataProvider");

            Config config = new Config();
            config.setDebug(true);

            String topologyName = "geilomatiko";

            cluster.submitTopology("Program", config, topoBuilder.createTopology());
            Thread.sleep(10000);
            cluster.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
