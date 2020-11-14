import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

public class ConsoleBolt extends BaseRichBolt {
    OutputCollector _collector;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        System.out.println("Id: " + input.getInteger(0));
        System.out.println("Total Distance: " + input.getDouble(1));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
