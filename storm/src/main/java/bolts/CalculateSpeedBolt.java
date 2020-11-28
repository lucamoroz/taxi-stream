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
import utils.TransferKafkaObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class CalculateSpeedBolt extends BaseRichBolt {
    OutputCollector _collector;
    Map<Integer, TaxiLog> lastLogs = new HashMap<Integer, TaxiLog>();
    Logger logger;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
        logger = new Logger("bolts.CalculateSpeedBolt");
    }

    @Override
    public void execute(Tuple input) {
        Gson g = new Gson();
        TransferKafkaObject p = g.fromJson(input.getValue(4).toString(), TransferKafkaObject.class);
        int id = p.getTaxi_id();
        double latitude = Double.parseDouble(p.getLatitude());
        double longitude = Double.parseDouble(p.getLongitude());

        TaxiLog currentLog = new TaxiLog(new Date(), latitude, longitude);

        if (lastLogs.containsKey(id)) {
            TaxiLog lastLog = lastLogs.get(id);

            double distance = CoordinateHelper.calculateDistance(lastLog, currentLog);

            long timeDiff = currentLog.getTimestamp().getTime() - lastLog.getTimestamp().getTime();

            double speed = distance/timeDiff;

            _collector.emit(new Values(id, speed));
            logger.log("speed: " + speed);
        }
        lastLogs.put(id, currentLog);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "speed"));
    }
}
