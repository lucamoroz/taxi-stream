package bolts;

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

import java.util.HashMap;
import java.util.Map;

public class CalculateSpeedBolt extends BaseRichBolt {
    OutputCollector _collector;
    Map<Integer, TaxiLog> lastLogs = new HashMap<>();
    Logger logger;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
        logger = new Logger("bolts.CalculateSpeedBolt");
    }

    @Override
    public void execute(Tuple input) {
        int taxiId = input.getIntegerByField("taxi_id");
        double latitude = input.getDoubleByField("latitude");
        double longitude = input.getDoubleByField("longitude");
        long timestamp = input.getLongByField("timestamp");

        TaxiLog currentLog = new TaxiLog(timestamp, latitude, longitude);

        if (lastLogs.containsKey(taxiId)) {
            TaxiLog lastLog = lastLogs.get(taxiId);

            double distanceKm = Math.abs(CoordinateHelper.calculateDistance(lastLog, currentLog)) / 1000d;

            double timeDiffHours = Math.abs(currentLog.getTimestamp() - lastLog.getTimestamp()) / 3600d;

            // Ignore logs with the same timestamp
            if (timeDiffHours != 0) {
                // speed as km/h
                double speed = distanceKm/timeDiffHours;

                _collector.emit(new Values(taxiId, speed, currentLog.getTimestamp()));
                logger.log(String.format("speed of taxi %d: %.2f km/h ", taxiId, speed));
            }
        }
        lastLogs.put(taxiId, currentLog);
        _collector.ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "speed", "timestamp"));
    }
}
