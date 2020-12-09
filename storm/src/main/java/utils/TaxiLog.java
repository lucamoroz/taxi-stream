package utils;

public class TaxiLog {
    private long timestamp;
    private double longitude;
    private double latitude;

    public TaxiLog(long timestamp, double longitude, double latitude) {
        this.timestamp = timestamp;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}