import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.ConfigurableTopology;
import org.apache.storm.topology.TopologyBuilder;

public class Program extends ConfigurableTopology{
    public static void main(String[] args) {
        ConfigurableTopology.start(new Program(), args);
    }

    @Override
    protected int run(String[] args) throws Exception {
        TopologyBuilder topoBuilder = new TopologyBuilder();
        topoBuilder.setSpout("dataProvider", new DataProvider());
        topoBuilder.setBolt("consoleBolt", new ConsoleBolt())
                .shuffleGrouping("dataProvider");

        conf.setDebug(true);

        String topologyName = "word-count";

        conf.setNumWorkers(3);

        return submit(topologyName,conf, topoBuilder);
    }
}
