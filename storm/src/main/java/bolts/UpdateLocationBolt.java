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
import java.util.Map;


public class UpdateLocationBolt extends BaseRichBolt {

    private OutputCollector outputCollector;
    private Logger logger;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector collector) {
        this.outputCollector = collector;
        this.logger = new Logger("bolts.UpdateLocationBolt");
    }

    @Override
    public void execute(Tuple input) {
        Instant startTime = Instant.now().truncatedTo(ChronoUnit.NANOS);

        int taxiId = input.getIntegerByField("taxi_id");
        double latitude = input.getDoubleByField("latitude");
        double longitude = input.getDoubleByField("longitude");

        outputCollector.emit(new Values(taxiId, "location", String.format("%.6f, %.6f", latitude, longitude)));

        Instant endTime = Instant.now().truncatedTo(ChronoUnit.NANOS);
        logger.log("Time of execution in nanoseconds: " + endTime.minusNanos(startTime.getNano()));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("id", "type", "value"));
    }
}
