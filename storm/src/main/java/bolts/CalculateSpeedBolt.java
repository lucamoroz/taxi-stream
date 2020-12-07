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

import java.util.Date;
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
        System.out.println(input.toString());
        int taxiId = input.getIntegerByField("taxi_id");
        double latitude = input.getDoubleByField("latitude");
        double longitude = input.getDoubleByField("longitude");

        TaxiLog currentLog = new TaxiLog(new Date(), latitude, longitude);

        if (lastLogs.containsKey(taxiId)) {
            TaxiLog lastLog = lastLogs.get(taxiId);

            double distance = CoordinateHelper.calculateDistance(lastLog, currentLog);

            long timeDiff = currentLog.getTimestamp().getTime() - lastLog.getTimestamp().getTime();

            // Ignore logs with the same timestamp
            if (timeDiff != 0) {
                double speed = distance/timeDiff;

                _collector.emit(new Values(taxiId, speed));
                logger.log(String.format("speed of taxi %d: %.2f ", taxiId, speed));
            }
        }
        lastLogs.put(taxiId, currentLog);
        _collector.ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "speed"));
    }
}
