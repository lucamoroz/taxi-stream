import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.Map;


public class ConsoleBolt extends BaseRichBolt {
    OutputCollector _collector;
    Logger logger;

//    @Override
//    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
//
//    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        _collector = outputCollector;
        logger = new Logger("Console Bolt");
    }

    @Override
    public void execute(Tuple input) {
        System.out.println("Id: " + input.getString(0));
        logger.log("Id: " + input.getString(0));
        System.out.println("Total Distance: " + input.getString(1));
        logger.log("Total Distance: " + input.getString(1));

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
