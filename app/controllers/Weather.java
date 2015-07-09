package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import java.util.ArrayList;
import java.util.Iterator;

public class Weather implements IMessageSource, Runnable {

    public JsonNode getJsonData() {
        String iskanderkul = "http://api.wunderground.com/api/2d20da4a2a78196d/hourly10day/q/zmw:00000.1.38718.json";
        String weiden = "http://api.wunderground.com/api/2d20da4a2a78196d/hourly10day/q/zmw:00000.1.10688.json";
        String location = iskanderkul;

        return Communication.getJson(location);
    }

    public String getMessageFromData(JsonNode data) {
        String result = "" ;

        ArrayList<Integer> desiredHours = getDesiredHours();

        Iterator<JsonNode> hourlyData = data.get("hourly_forecast").elements();
        for(JsonNode currData = hourlyData.next(); hourlyData.hasNext() && !desiredHours.isEmpty(); currData = hourlyData.next()) {
            if(desiredHours.get(0) == currData.get("FCTTIME").get("hour").asInt()) {
                result += desiredHours.remove(0);//2 + currData.get("FCTTIME").get("ampm").asText(); // 2am
                result += " ";
                result += currData.get("temp").get("metric").asText() + "C"; // 15C
                result += " ";
                result += currData.get("pop").asText() + "%"; // 0%
                result += " ";
                result += currData.get("qpf").get("metric").asText() + "mm"; // 2mm
                result += " ";
                result += currData.get("wspd").get("metric").asText() + currData.get("wdir").get("dir").asText(); // 10NNE
                result += " ";
                result += currData.get("humidity").asText() + "%"; // 78%
                result += "\n";
            }
        }
        Logger.debug("Created message:\n" + result);
        return result;
    }

    public String getSenderFromData(JsonNode data) {
        String result = "" ;

        ArrayList<Integer> desiredHours = getDesiredHours();
        Iterator<JsonNode> hourlyData = data.get("hourly_forecast").elements();
        for(JsonNode currData = hourlyData.next(); hourlyData.hasNext() && !desiredHours.isEmpty(); currData = hourlyData.next()) {
            if(desiredHours.get(0) == currData.get("FCTTIME").get("hour").asInt()) {
                result = currData.get("FCTTIME").get("mday").asText(); // 11
                break;
            }
        }

        Logger.debug("Day: " + result);
        return result;
    }

    private ArrayList<Integer> getDesiredHours() {
        ArrayList<Integer> desiredHours = new ArrayList<>();
        desiredHours.add(6);
        desiredHours.add(12);
        desiredHours.add(18);
        desiredHours.add(0);
        desiredHours.add(6);
        desiredHours.add(12);
        desiredHours.add(18);
        return desiredHours;
    }

    @Override
    public void run() {
        Logger.info("###weather tick");
        Communication.process("WF", this);
    }
}
