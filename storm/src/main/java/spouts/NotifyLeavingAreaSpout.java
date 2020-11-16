//package spouts;
//
//import java.util.Map;
//import org.apache.storm.spout.SpoutOutputCollector;
//import org.apache.storm.task.TopologyContext;
//import org.apache.storm.topology.OutputFieldsDeclarer;
//import org.apache.storm.topology.base.BaseRichSpout;
//import org.apache.storm.tuple.Fields;
//
//public class NotifyLeavingAreaSpout extends BaseRichSpout {
//
//    SpoutOutputCollector spoutOutputCollector;
//
//    @Override
//    public void open(Map<String, Object> map, TopologyContext topologyContext,
//        SpoutOutputCollector spoutOutputCollector) {
//
//        this.spoutOutputCollector = spoutOutputCollector;
//    }
//
//    @Override
//    public void nextTuple() {
//        //Include Kafka Collector and emit the result
//    }
//
//    @Override
//    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
//        outputFieldsDeclarer.declare(new Fields("idNotification", "idTaxi"));
//    }
//}
