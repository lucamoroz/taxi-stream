package main;

import static kafka.SampleConsumer.runConsumer;

import bolts.MultiplierBolt;
import kafka.SampleConsumer;
import kafka.SampleProducer;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.LocalDRPC;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.tuple.Fields;
import spouts.IntegerSpout;
import trident.complexTrident;
import trident.tri1;

public class MainTopology {

    public static void main(String[] args) {

        //Trident try out

        LocalDRPC drpc = new LocalDRPC();//distributed remote procedure call, kind of client

        TridentTopology topology = new TridentTopology();

        topology.newDRPCStream("simple", drpc)
            .each(new Fields("args"),//always args
                new tri1(),//called Trident function
                new Fields("processed_word") //output fields
            );

        topology.newDRPCStream("complex", drpc).each(
            new Fields("args"),
            new complexTrident(),
            new Fields("returnVal")
        );

        Config config = new Config();
        config.setDebug(true);//emit results to console/log

        try {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("trident-topology", config, topology.build());

            for (String word : new String[]{"word 1", "word 2", "word 3"}) {
                System.out.println("Result for " + word + ": " + drpc.execute("simple", word));
                System.out.println("Result for " + word + ": " + drpc.execute("complex", word));
            }

            cluster.shutdown();
        }
        catch (Exception e){
            e.printStackTrace();
        }

/*
//Kafka try out
        Config config = new Config();
        config.setDebug(true);

        SampleProducer sampleProducer = new SampleProducer();

        try {
            runConsumer();
        }
        catch(Exception e){
            e.printStackTrace();
        }


 */
/*
//Apache Storm Spout/Bolt try out

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("IntegerSpout", new IntegerSpout());
        builder.setBolt("MultiplierBolt", new MultiplierBolt()).shuffleGrouping("IntegerSpout");

        LocalCluster cluster = null;

        try {
            cluster = new LocalCluster();
            cluster.submitTopology("HelloTopology", config, builder.createTopology());
            Thread.sleep(10000);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            cluster.shutdown();
        }

 */
    }


}
