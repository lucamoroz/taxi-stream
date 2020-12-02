package bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.HashMap;
import java.util.Map;

public class NotifyLeavingAreaBolt extends BaseRichBolt {

    OutputCollector outputCollector;
    Map<Integer, Integer> idNotificationMap;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext,
        OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        idNotificationMap = new HashMap<>();
    }

    @Override
    public void execute(Tuple tuple) {
        //0 ... id of the notification
        //1 ... id of the taxi
        int idNotification = tuple.getInteger(0);

        if(!idNotificationMap.containsKey(idNotification)){
            int idTaxi = tuple.getInteger(1);
            //TODO: calcualte distance
            Integer distanceToBeijingCenter = 0; //set the distance to Beijing center in km


            idNotificationMap.put(idNotification, idTaxi);

            if (distanceToBeijingCenter > 10){
                //Inform the frontend
                System.out.println("Taxi " + idTaxi + " is leaving a predefined area, implement http notification!");
                //TODO: implement frontend notification

            }
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //there is only output to the frontend
    }
}
