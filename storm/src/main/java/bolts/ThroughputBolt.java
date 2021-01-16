package bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import utils.Logger;
import utils.Numbers;

import java.util.Map;

public class ThroughputBolt extends BaseRichBolt {
    OutputCollector _collector;
    Logger logger;
    long lastThroughputMeasurementNs = 0;
    long nProcessedTuplesAccumulator = 0;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
        logger = new Logger("bolts.ThroughputBolt");
    }

    @Override
    public void execute(Tuple input) {
        long nProcessedTuples = input.getLongByField("throughput");

        // show throughput every 1 second
        if ((System.nanoTime() - lastThroughputMeasurementNs) > 1000000000) {
            logger.log(String.format("Throughput: %d", nProcessedTuples));
            nProcessedTuplesAccumulator = 0;
            lastThroughputMeasurementNs = System.nanoTime();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) { }
}
