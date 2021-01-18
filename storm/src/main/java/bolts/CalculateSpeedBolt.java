package bolts;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import utils.CoordinateHelper;
import utils.Logger;
import utils.TaxiLog;
import utils.WriteToCSV;

public class CalculateSpeedBolt extends BaseRichBolt {
    OutputCollector _collector;
    Map<Integer, TaxiLog> lastLogs = new HashMap<>();
    Logger logger;
    WriteToCSV writeToCSV;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
        logger = new Logger("bolts.CalculateSpeedBolt");
        this.writeToCSV = WriteToCSV.createWriteToCSV();
    }

    @Override
    public void execute(Tuple input) {
        int taxiId = input.getIntegerByField("taxi_id");
        double latitude = input.getDoubleByField("latitude");
        double longitude = input.getDoubleByField("longitude");
        long timestamp = input.getLongByField("timestamp");

        TaxiLog currentLog = new TaxiLog(timestamp, latitude, longitude);

        if (lastLogs.containsKey(taxiId)) {
            TaxiLog lastLog = lastLogs.get(taxiId);

            double distanceKm = Math.abs(CoordinateHelper.calculateDistance(lastLog, currentLog)) / 1000d;

            double timeDiffHours = Math.abs(currentLog.getTimestamp() - lastLog.getTimestamp()) / 3600d;

            // Ignore logs with the same timestamp
            if (timeDiffHours != 0) {
                // speed as km/h
                double speed = distanceKm/timeDiffHours;

                _collector.emit(new Values(taxiId, speed, currentLog.getTimestamp(), input.getLongByField("startTime")));
                logger.log(String.format("speed of taxi %d: %.2f km/h ", taxiId, speed));
            }
        }
        lastLogs.put(taxiId, currentLog);
        _collector.ack(input);

/*
        long endTime = System.currentTimeMillis();
        try{
            String id = String.valueOf(taxiId);
            String time = String.valueOf(endTime - input.getLongByField("startTime"));
            this.writeToCSV.writeToFile(id, "CalculateSpeedBolt", time);
        } catch (Exception ex){
            this.logger.log("Error while writing to CSV: " + ex.toString());
        }
        */
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("id", "speed", "timestamp", "startTime"));
    }
}
