package bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import utils.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AverageSpeedBolt extends BaseRichBolt {

    private OutputCollector outputCollector;
    private Map<Integer, List<Double>> lastSpeeds = new HashMap<>();
    private Logger logger;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector collector) {
        this.outputCollector = collector;
        this.logger = new Logger("bolts.AverageSpeedBolt");
    }

    @Override
    public void execute(Tuple input) {
        Instant startTime = Instant.now().truncatedTo(ChronoUnit.NANOS);
        int taxiId = input.getIntegerByField("id");
        double speed = input.getDoubleByField("speed");

        // todo avg speed can be computed with constant memory usage, see:
        //  https://math.stackexchange.com/questions/106700/incremental-averageing
        List<Double> speeds;

        if (lastSpeeds.containsKey(taxiId)) {
            speeds = lastSpeeds.get(taxiId);
        }else{
            speeds = new ArrayList();
            lastSpeeds.put(taxiId, speeds);
        }

        speeds.add(speed);
        double averageSpeed = speeds.stream().reduce(0d, Double::sum) / speeds.size();

        outputCollector.emit(input, new Values(taxiId, "average_speed", String.format("%.6f", averageSpeed)));
        outputCollector.ack(input);

        Instant endTime = Instant.now().truncatedTo(ChronoUnit.NANOS);
        logger.log("Time of execution in nanoseconds: " + endTime.minusNanos(startTime.getNano()));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "type", "value"));
    }
}
