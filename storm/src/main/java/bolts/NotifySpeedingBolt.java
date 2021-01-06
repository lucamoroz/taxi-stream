package bolts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

public class NotifySpeedingBolt extends BaseRichBolt {

    OutputCollector outputCollector;
    Map<Integer, Integer> idNotificationMap;
    Integer speedLimit; //eg. 50 km/h
    Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext,
        OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        idNotificationMap = new HashMap<>();
        speedLimit = 50;

        try {
            socket = new Socket("dashboard-backend", 8082);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void execute(Tuple tuple) {
        //0 ... id of the notification
        //1 ... id of the taxi
        //2 ... speed of the taxi
        int idNotification = tuple.getInteger(0);

       // this.sendMessage("Example message from backend!");

        if(!idNotificationMap.containsKey(idNotification)){

            int idTaxi = tuple.getInteger(1);
            int speed = tuple.getInteger(2);

            idNotificationMap.put(idNotification, idTaxi);

            if(speed > speedLimit) {
                System.out.println("Taxi " + idTaxi + " is speeding, implement notification!");
                //TODO: implement frontend notification


            }
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //there is only output to the frontend
    }

    public String sendMessage(String msg) {

        String resp = null;
        try {
            resp = in.readLine();
            out.println(msg);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;

    }

    public void stopConnection() {

        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
