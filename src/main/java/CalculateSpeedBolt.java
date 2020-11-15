import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CalculateSpeedBolt extends BaseRichBolt {
    OutputCollector _collector;
    Map<Integer, TaxiLog> lastLogs = new HashMap<Integer, TaxiLog>();

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        int id = input.getInteger(0);
        TaxiLog currentLog = new TaxiLog(new Date(), input.getDouble(1), input.getDouble(2));

        if (lastLogs.containsKey(id)) {
            TaxiLog lastLog = lastLogs.get(id);

            double distance = CoordinateHelper.calculateDistance(lastLog, currentLog);

            long timeDiff = currentLog.getTimestamp().getTime() - lastLog.getTimestamp().getTime();

            double speed = distance/timeDiff;

            _collector.emit(new Values(id, speed));
            System.out.println("SPEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEED: " + speed);
        }

        lastLogs.put(id, currentLog);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "speed"));
    }
}
