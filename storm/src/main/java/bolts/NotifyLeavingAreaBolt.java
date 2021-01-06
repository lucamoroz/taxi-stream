package bolts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

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

    private Integer maxDistanceToBeijingCenterKiloMeter = 10;

    //TCP
    Socket tcpSocket;
    PrintWriter out;
    BufferedReader in;

    //UDP
    InetAddress address;
    DatagramSocket udpSocket;
    byte[] buff;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext,
                        OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        lastLogs = new HashMap<>();

        //TCP
        try {
            tcpSocket = new Socket("dashboard-backend", 8082);
            out = new PrintWriter(tcpSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Cant build the socket!");
            e.printStackTrace();
        }

        //UDP
//        try {
//            udpSocket = new DatagramSocket();
//            address = InetAddress.getByName("dashboard-backend");
//        } catch (SocketException | UnknownHostException e) {
//            System.out.println("Either socket or unknown host exception!");
//            e.printStackTrace();
//        }
        lastLogs = new HashMap<>();

        centerBeijingLocation = new TaxiLog(0, longitudeBeijing, latitudeBeijing);
        this.logger = new Logger("bolts.NotifyLeavingAreaBolt");
    }

    @Override
    public void execute(Tuple tuple) {

        int taxiId = tuple.getIntegerByField("taxi_id");
        Double longitude = tuple.getDoubleByField("longitude");
        Double latitude = tuple.getDoubleByField("latitude");
        long timestamp = tuple.getLongByField("timestamp");

        this.logger.log("Before going to SendViaTCP");
        sendViaTCP();

        TaxiLog currentLog = new TaxiLog(timestamp, latitude, longitude);
        Double distanceToBeijingCenterMeter = 0.;
        distanceToBeijingCenterMeter = CoordinateHelper.calculateDistance(currentLog, centerBeijingLocation);


        //TODO: include timestamp check
        if (!lastLogs.containsKey(taxiId)) {
            if (distanceToBeijingCenterMeter > maxDistanceToBeijingCenterKiloMeter) {
                this.logger.log("Taxi " + taxiId + " is leaving a predefined area!");

                this.lastLogs.put(taxiId, currentLog);


            } else {
                TaxiLog existingLog = this.lastLogs.get(taxiId);

                if (distanceToBeijingCenterMeter <= maxDistanceToBeijingCenterKiloMeter &&
                        existingLog.getTimestamp() <= currentLog.getTimestamp()) {

                    this.logger.log("Taxi " + taxiId + " is inside the predefined area again");

                    this.lastLogs.remove(taxiId);
                    //TCP


                    //UDP
//                String str = "Car is leaving the area!";
//                buff = str.getBytes();
//                sendViaUDP();
                }
            }

        }
    }
        
    

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //there is only output to the frontend
    }

    private void sendViaTCP(){
        try {
            
            this.logger.log("Inside SendViaTCP");

            out.println("Car is leaving the area!");

            this.logger.log("Inside SendViaTCP2");

            //this.logger.log(in.readLine());

            this.logger.log("Inside SendViaTCP3");
            tcpSocket.close();
            out.close();
            in.close();
            
            this.logger.log("Inside SendViaTCP after close");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendViaUDP(){
        try {
            DatagramPacket packet
                    = new DatagramPacket(buff, buff.length, address, 8082);
            udpSocket.send(packet);
            packet = new DatagramPacket(buff, buff.length);
            udpSocket.receive(packet);
            String received = new String(
                    packet.getData(), 0, packet.getLength());
            System.out.println(received);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


}
