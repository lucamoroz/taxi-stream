import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AverageSpeedBolt extends BaseRichBolt {
    OutputCollector _collector;
    Map<Integer, List<Double>> lastSpeeds = new HashMap<Integer, List<Double>>();
    Logger logger;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
        logger = new Logger("AverageSpeedBolt");
    }

    @Override
    public void execute(Tuple input) {
        int id = input.getInteger(0);
        double speed = input.getDouble(1);

        // todo avg speed can be computed with constant memory usage, see:
        //  https://math.stackexchange.com/questions/106700/incremental-averageing
        List<Double> speeds;

        if (lastSpeeds.containsKey(id)) {
            speeds = lastSpeeds.get(id);
        }else{
            speeds = new ArrayList();
            lastSpeeds.put(id, speeds);
        }

        speeds.add(speed);
        double averageSpeed = speeds.stream().reduce(0d, (total, element) -> total + element) / speeds.size();
        logger.log("avg: " + averageSpeed);
        _collector.emit(new Values(id, averageSpeed));

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "average_speed"));
    }
}
