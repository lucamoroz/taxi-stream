import bolts.*;
import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import spouts.NotifyLeavingAreaSpout;
import utils.TransferKafkaObject;


public class Program {
    public static void main(String[] args) {

        LocalCluster cluster;

        JedisPoolConfig poolConfig = new JedisPoolConfig.Builder()
                .setHost("redis").setPort(6379).build();

        TopologyBuilder topoBuilder = new TopologyBuilder();

        topoBuilder.setSpout("kafkaSpout", getKafkaSpout(), 1);

        topoBuilder.setBolt("calculateSpeedBolt", new CalculateSpeedBolt())
                .fieldsGrouping("kafkaSpout", new Fields("taxi_id"));
        topoBuilder.setBolt("averageSpeedBolt", new AverageSpeedBolt(poolConfig))
                 .fieldsGrouping("calculateSpeedBolt", new Fields("id"));

        topoBuilder.setBolt("calculateDistanceBolt", new CalculateDistanceBolt(poolConfig))
                .fieldsGrouping("kafkaSpout", new Fields("taxi_id"));

        topoBuilder.setBolt("updateLocationBolt", new UpdateLocationBolt(poolConfig))
                .fieldsGrouping("kafkaSpout", new Fields("taxi_id"));

        topoBuilder.setBolt("notifyLeavingAreaBolt", new NotifyLeavingAreaBolt())
                .fieldsGrouping("kafkaSpout", new Fields("taxi_id"));

        topoBuilder.setBolt("notifySpeedingBolt", new NotifySpeedingBolt())
            .fieldsGrouping("calculateSpeedBolt", new Fields("id"));


        //TODO: add notify Speeding Bolt from "Calculate Speed" bolt

        try {
            cluster = new LocalCluster();

            Config config = new Config();
            config.setDebug(false);
            config.setMaxSpoutPending(5000);

            cluster.submitTopology("Program", config, topoBuilder.createTopology());
            Thread.sleep( 2000000000);
            cluster.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static KafkaSpout<String, String> getKafkaSpout() {
        KafkaSpoutConfig.Builder<String, String> kafkaSpoutBuilder = KafkaSpoutConfig.builder("kafka:" + "9092", "test");
        kafkaSpoutBuilder.setProp(ConsumerConfig.GROUP_ID_CONFIG, "test");
        kafkaSpoutBuilder.setProp(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        kafkaSpoutBuilder.setProcessingGuarantee(KafkaSpoutConfig.ProcessingGuarantee.AT_MOST_ONCE);
        kafkaSpoutBuilder.setRecordTranslator((ConsumerRecord<String, String> record) -> {
            Gson g = new Gson();
            TransferKafkaObject p = g.fromJson(record.value(), TransferKafkaObject.class);
            return new Values(p.getTaxi_id(), p.getDatetime(), Double.parseDouble(p.getLatitude()), Double.parseDouble(p.getLongitude()));
        }, new Fields("taxi_id", "timestamp", "latitude", "longitude"));

        return new KafkaSpout<>(kafkaSpoutBuilder.build());
    }
}
