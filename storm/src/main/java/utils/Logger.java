package utils;

public class Logger {
    private final String name;

    public Logger(String name) {
        this.name = name;
    }

    public void log(String log){
        System.out.println("LOG|"+this.name+"|"+log);
    }
}
