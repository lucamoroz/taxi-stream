package bolts;

import java.util.Date;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.HashMap;
import java.util.Map;
import utils.CoordinateHelper;
import utils.Logger;
import utils.TaxiLog;

public class NotifyLeavingAreaBolt extends BaseRichBolt {

    private OutputCollector outputCollector;
    private Map<Integer, TaxiLog> lastLogs = new HashMap<>();
    private Logger logger;


    private Double latitudeBeijing = 39.9075;
    private Double longitudeBeijing = 116.39723;

    private TaxiLog centerBeijingLocation;

    private Integer maxDistanceToBeijingCenterMeter = 10000;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext,
        OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        lastLogs = new HashMap<>();

        centerBeijingLocation = new TaxiLog(new Date(), longitudeBeijing, latitudeBeijing);
    }

    @Override
    public void execute(Tuple tuple) {

        int taxiId = tuple.getIntegerByField("taxi_id");
        Double longitude = tuple.getDoubleByField("longitude");
        Double latitude = tuple.getDoubleByField("latitude");

        TaxiLog currentLog = new TaxiLog(new Date(), latitude, longitude);

        Double distanceToBeijingCenterMeter = CoordinateHelper.calculateDistance(currentLog, centerBeijingLocation);

        //TODO: include timestamp check
        if(!lastLogs.containsKey(taxiId) && distanceToBeijingCenterMeter > maxDistanceToBeijingCenterMeter){

            this.logger.log("Taxi " + taxiId + " is leaving a predefined area!");

            this.lastLogs.put(taxiId, currentLog);
            //TODO: implement frontend notification


        } else {
            TaxiLog existingLog = this.lastLogs.get(taxiId);

            if (distanceToBeijingCenterMeter <= maxDistanceToBeijingCenterMeter &&
                existingLog.getTimestamp().before(currentLog.getTimestamp())){

                this.logger.log("Taxi " + taxiId + " is inside the predefined area again");

                this.lastLogs.remove(taxiId);

            }
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //there is only output to the frontend
    }


}
