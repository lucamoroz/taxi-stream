package bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import utils.Logger;

import java.util.Map;

public class ThroughputBolt extends BaseRichBolt {
    OutputCollector _collector;
    Logger logger;
    long lastThroughputMeasurementNs = 0;
    long nProcessedTuplesAccumulator = 0;
    double averageThroughput = 0;
    long nThroughputSamples = 0;

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
            // Note: with 1 second cadence nProcessedTuplesAccumulator == current throughput
            double numerator = ((double) nProcessedTuplesAccumulator) - averageThroughput;
            double denominator = (double) (nThroughputSamples+1);
            averageThroughput = averageThroughput + (numerator / denominator);
            logger.log(String.format("Current throughput: %d, Average Throughput: %.3f", nProcessedTuplesAccumulator, averageThroughput));

            nProcessedTuplesAccumulator = 0;
            nThroughputSamples++;
            lastThroughputMeasurementNs = System.nanoTime();
        } else {
            nProcessedTuplesAccumulator += nProcessedTuples;
        }
        _collector.ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) { }
}
