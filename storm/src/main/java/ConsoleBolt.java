import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import utils.Logger;
import utils.TransferKafkaObject;
import java.util.Map;
import com.google.gson.Gson;

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
        Gson g = new Gson();
        TransferKafkaObject p = g.fromJson(input.getValue(4).toString(), TransferKafkaObject.class);
        System.out.println("Input: " + p.toString());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
