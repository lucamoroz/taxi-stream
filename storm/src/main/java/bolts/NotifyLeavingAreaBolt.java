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

public class NotifyLeavingAreaBolt extends BaseRichBolt {

    OutputCollector outputCollector;
    Map<Integer, Integer> idNotificationMap;

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
        idNotificationMap = new HashMap<>();

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

            if (distanceToBeijingCenter > 10) {
                //Inform the frontend
                System.out.println("Taxi " + idTaxi + " is leaving a predefined area, implement http notification!");
                //TODO: implement frontend notification
                //TCP
                sendViaTCP();

                //UDP
//                String str = "Car is leaving the area!";
//                buff = str.getBytes();
//                sendViaUDP();
            }
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //there is only output to the frontend
    }

    private void sendViaTCP(){
        try {
            out.println("Car is leaving the area!");
            System.out.println(in.readLine());
            tcpSocket.close();
            out.close();
            in.close();
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
