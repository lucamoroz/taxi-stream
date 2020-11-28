import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import utils.Logger;
import java.util.Map;

public class ConsoleBolt extends BaseRichBolt {
    OutputCollector _collector;
    Logger logger;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        _collector = outputCollector;
        logger = new Logger("Console Bolt");
    }

    @Override
    public void execute(Tuple input) {
        System.out.println("Id: " + input.getIntegerByField("taxi_id"));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
