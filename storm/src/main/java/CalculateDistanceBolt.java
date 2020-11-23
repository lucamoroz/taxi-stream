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

public class CalculateDistanceBolt extends BaseRichBolt {
    OutputCollector _collector;
    Map<Integer, Object[]> overallDistances = new HashMap<Integer, Object[]>();
    Logger logger;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
        logger = new Logger("CalculateDistanceBolt");
    }

    @Override
    public void execute(Tuple input) {
        int id = input.getInteger(0);
        TaxiLog currentLog = new TaxiLog(new Date(), input.getDouble(1), input.getDouble(2));

        double currentOverallDistance = 0;

        if (overallDistances.containsKey(id)) {
            currentOverallDistance = (double) overallDistances.get(id)[0];
            TaxiLog lastLog = (TaxiLog) overallDistances.get(id)[1];

            currentOverallDistance += CoordinateHelper.calculateDistance(lastLog, currentLog);
        }

        overallDistances.put(id, new Object[]{currentOverallDistance, currentLog});
        _collector.emit(new Values(id, currentOverallDistance));
        logger.log("dist: " + currentOverallDistance);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "distance"));
    }
}