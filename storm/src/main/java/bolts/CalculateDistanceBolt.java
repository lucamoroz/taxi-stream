package bolts;

import com.codahale.metrics.MetricRegistryListener;
import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import utils.CoordinateHelper;
import utils.Logger;
import utils.TaxiLog;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class CalculateDistanceBolt extends BaseRichBolt {

    private OutputCollector outputCollector;
    Map<Integer, Object[]> overallDistances = new HashMap<>();
    Logger logger;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector collector) {
        this.outputCollector = collector;
        this.logger = new Logger("bolts.CalculateDistanceBolt");
    }

    @Override
    public void execute(Tuple input) {
        Instant startTime = Instant.now().truncatedTo(ChronoUnit.NANOS);
        int taxiId = input.getIntegerByField("taxi_id");
        double latitude = input.getDoubleByField("latitude");
        double longitude = input.getDoubleByField("longitude");
        long timestamp = input.getLongByField("timestamp");

        TaxiLog currentLog = new TaxiLog(timestamp, latitude, longitude);

        double currentOverallDistance = 0;

        if (overallDistances.containsKey(taxiId)) {
            currentOverallDistance = (double) overallDistances.get(taxiId)[0];
            TaxiLog lastLog = (TaxiLog) overallDistances.get(taxiId)[1];
            currentOverallDistance += CoordinateHelper.calculateDistance(lastLog, currentLog) / 1000d;
        }

        overallDistances.put(taxiId, new Object[]{currentOverallDistance, currentLog});

        outputCollector.emit(new Values(taxiId, "overall_distance", String.format("%.6f", currentOverallDistance)));
        outputCollector.ack(input);

        Instant endTime = Instant.now().truncatedTo(ChronoUnit.NANOS);
        logger.log("Time of execution in nanoseconds: " + endTime.minusNanos(startTime.getNano()));

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "type", "value"));
    }
}