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
    Map<Integer, Integer[]> test;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
        test = new HashMap<Integer, Integer[]>();
    }

    public void execute(Tuple input) {
        int id = input.getInteger(0);
        int x = input.getInteger(1);
        int y = input.getInteger(2);

        int distance = 0;

        if (test.containsKey(id)) {
            int lastX = test.get(id)[0];
            int lastY = test.get(id)[1];

            distance = test.get(id)[2] + Math.abs(lastX - x) + Math.abs(lastY - y);
        }

        test.put(id, new Integer[]{x, y, distance});

        _collector.emit(new Values(id, distance));
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "distance"));
    }
}
