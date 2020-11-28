package utils;

public class TransferKafkaObject {

    Integer taxi_id;
    String longitude;
    String latitude;
    Float datetime;

    public TransferKafkaObject (Integer taxi_id, String longitude, String latitude, Float datetime){
        this.taxi_id = taxi_id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.datetime = datetime;
    }

    public Integer getTaxi_id(){
        return this.taxi_id;
    }

    public String getLongitude(){
        return this.longitude;

    }

    public String getLatitude(){
        return this.latitude;
    }

    public Float getDatetime(){
        return this.datetime;
    }

    public String toString(){
        return "taxi_id: " + taxi_id +
            " longitude: " + longitude +
            " latitude: " +  latitude +
            " datetime: " + datetime;
    }
}