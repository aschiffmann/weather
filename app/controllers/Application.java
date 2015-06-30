package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.*;

import views.html.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

public class Application extends Controller {

    public Result index() {
        return ok(index.render("Nothing to say", "no data"));
    }


    public Result getForecast() {
        String message;
        try {
            JsonNode data = getJsonData();
            message = getMessageFromData(data);
            send(message);
        } catch (Exception e) {
            message = "Error. Sorry. " + e.getClass().toString();
            Logger.error("Error!", e);
        }

        return ok(index.render("here's your forecast", message));
    }

    private void send(String message) throws IOException {
        String msisdn = "01234";
        int tlength = 157 - message.length();
        String body = "msisdn=" + URLEncoder.encode(msisdn, "UTF-8") + "&" +
                "from=" + URLEncoder.encode("WFC", "UTF-8") + "&" +
                "message=" + URLEncoder.encode(message, "UTF-8") + "&" +
                "tlength=" + URLEncoder.encode(tlength + "", "UTF-8");

        URL url = new URL( "https://sms.thuraya.com/sms.php" );
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", String.valueOf(body.length()));

        OutputStreamWriter writer = new OutputStreamWriter( connection.getOutputStream() );
        writer.write(body);
        writer.flush();

        String response = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        for ( String line; (line = reader.readLine()) != null; )
        {
            response += line;
        }
        writer.close();
        reader.close();

        Logger.debug("sent message. response:\n" + response);
        if(response.contains("message was sent")) {
            Logger.debug("Sending successfull!");
        } else {
            Logger.error("Something went wrong while sending!");
        }
    }

    private String getMessageFromData(JsonNode data) {
        String result = "" ;

        ArrayList<Integer> desiredHours = getDesiredHours();

        Iterator<JsonNode> hourlyData = data.get("hourly_forecast").elements();
        for(JsonNode currData = hourlyData.next(); hourlyData.hasNext() && !desiredHours.isEmpty(); currData = hourlyData.next()) {
            if(desiredHours.get(0) == currData.get("FCTTIME").get("hour").asInt()) {
                result += desiredHours.remove(0) + currData.get("FCTTIME").get("ampm").asText(); // 2am
                result += " ";
                result += currData.get("temp").get("metric").asText() + "C"; // 15C
                result += " ";
                result += currData.get("pop").asText() + "%"; // 0%
                result += " ";
                result += currData.get("qpf").get("metric").asText() + "mm"; // 2mm
                result += " ";
                result += currData.get("wspd").get("metric").asText() + currData.get("wdir").get("dir").asText(); // 10NNE
                result += "\n";
            }
        }
        Logger.debug("Created message:\n" + result);
        return result.substring(0, Math.min(result.length(), 157));
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

    private JsonNode getJsonData() {
        String iskanderkul = "http://api.wunderground.com/api/2d20da4a2a78196d/hourly10day/q/zmw:00000.1.38718.json";
        String weiden = "http://api.wunderground.com/api/2d20da4a2a78196d/hourly10day/q/zmw:00000.1.10688.json";
        String location = weiden;

        F.Promise<JsonNode> jsonPromise = WS.url(location).get().map(
                new F.Function<WSResponse, JsonNode>() {
                    public JsonNode apply(WSResponse response) {
                        return response.asJson();
                    }
                }
        );

        return jsonPromise.get(100000);
    }


    private JsonNode getTomorrowText(JsonNode jsonNode) {
        return jsonNode.get("forecast").get("txt_forecast").get("forecastday").elements().next();
    }
    private JsonNode getTomorrowNrs(JsonNode jsonNode) {
        return jsonNode.get("forecast").get("simpleforecast").get("forecastday").elements().next();
    }
}
