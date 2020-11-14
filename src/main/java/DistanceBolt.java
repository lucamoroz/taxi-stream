import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;

public class DistanceBolt extends BaseRichBolt {
    OutputCollector _collector;
    Map<Integer, Double[]> test;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
        test = new HashMap<Integer, Double[]>();
    }

    public void execute(Tuple input) {
        int id = input.getInteger(0);
        double x = input.getDouble(1);
        double y = input.getDouble(2);

        double distance = 0;

        if (test.containsKey(id)) {
            double lastX = test.get(id)[0];
            double lastY = test.get(id)[1];

            distance = test.get(id)[2] + Math.abs(lastX - x) + Math.abs(lastY - y);
        }

        test.put(id, new Double[]{x, y, distance});

        _collector.emit(new Values(id, distance));
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "distance"));
    }
}
