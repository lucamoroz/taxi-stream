package spouts;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import utils.Logger;

import java.util.Map;
import java.util.Random;

public class FakeDataSpout extends BaseRichSpout {
    SpoutOutputCollector _collector;
    private Logger logger;

    @Override
    public void open(Map<String, Object> conf, TopologyContext context, SpoutOutputCollector collector) {
        _collector = collector;
        this.logger = new Logger("spouts.DataProvider");
    }

    @Override
    public void nextTuple() {
        Utils.sleep(1000);
        Random rand = new Random();

        int taxiId = 1;
        double latitude = -180 + 360 * rand.nextDouble();
        double longitude = -90 + 180 * rand.nextDouble();

        _collector.emit(new Values(taxiId, latitude, longitude));
        logger.log(String.format("emitted: %d, %.6f, %.6f", taxiId, latitude, longitude));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "latitude", "longitude"));
    }
}
