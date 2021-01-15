package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class WriteToCSV{

    private static WriteToCSV singleton;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;
    private Logger logger;
    private String fileName = "output.csv";
    private PrintWriter pw;
    

    private WriteToCSV(){

        this.logger = new Logger("WriteToCSV");

        try{
            this.fileWriter = new FileWriter(this.fileName, true);
            this.bufferedWriter = new BufferedWriter(this.fileWriter);
            
            this.pw = new PrintWriter(this.bufferedWriter);
        } catch(Exception ex){
            this.logger.log(ex.toString());
        }
        
    }

    public static WriteToCSV createWriteToCSV(){

        if(singleton == null){
            WriteToCSV.singleton = new WriteToCSV();
        }

        return WriteToCSV.singleton;

    }

    public void writeToFile(String id, String bolt,  String time) throws IOException {

        this.logger.log("Writing to file: " + id + time);

        try{
            pw.println(id + "," + bolt + "," + time);
            pw.flush();
        }catch (Exception ex){
            this.logger.log(ex.toString());
        }
        
    }

}