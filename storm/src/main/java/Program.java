import bolts.NotifyLeavingAreaBolt;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.ConfigurableTopology;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.HashMap;
import spouts.NotifyLeavingAreaSpout;

public class Program {
    public static void main(String[] args) {
        LocalCluster cluster = null;

        try {
            cluster = new LocalCluster();
            TopologyBuilder topoBuilder = new TopologyBuilder();
            topoBuilder.setSpout("dataProvider", new DataProvider());
            topoBuilder.setBolt("distanceBolt", new DistanceBolt())
                    .fieldsGrouping("dataProvider", new Fields("id"));
            topoBuilder.setBolt("consoleBolt", new ConsoleBolt())
                    .shuffleGrouping("distanceBolt");


            topoBuilder.setSpout("notifyLeavingAreaSpout", new NotifyLeavingAreaSpout());
            topoBuilder.setBolt("notifyLeavingAreaBolt", new NotifyLeavingAreaBolt())
                .fieldsGrouping("notifyLeavingAreaSpout", new Fields("id"));
            //TODO: add notify Speeding Bolt from "Calculate Speed" bolt

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
