package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.h2.util.StringUtils;
import play.Logger;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Communication {

    public static String process(String from, IMessageSource source) {
        String message;
        if(StringUtils.isNullOrEmpty(from)) {
            from = "Default";
        }

        try {
            JsonNode data = source.getJsonData();
            message = source.getMessageFromData(data);
            from = source.getSenderFromData(data);
        } catch (Exception e) {
            message = "Error. Sorry. " + e.getClass().toString();
            Logger.error("Error!", e);
        }

        if(false) {
            try {
                sendSMS(from, message);
            } catch (IOException e) {
                Logger.error("error while sending!", e);
            }
        }

        return message;
    }

    public static void sendSMS(String from, String message) throws IOException {
        message = URLEncoder.encode(message, "UTF-8");
        from = URLEncoder.encode(from, "UTF-8");
        message = message.substring(0, Math.min(message.length(), 160 - from.length()));
        String msisdn = "21275049";
        int tlength = 160 - from.length() - message.length();
        String body = "msisdn=" + URLEncoder.encode(msisdn, "UTF-8") + "&" +
                "from=" + from + "&" +
                "message=" + message + "&" +
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

        Logger.debug("sent message. response: " + response);
        if(response.contains("message was sent")) {
            Logger.debug("Sending successfull!");
        } else {
            Logger.error("Something went wrong while sending!");
        }
    }

    public static JsonNode getJson(String url) {
        F.Promise<JsonNode> jsonPromise = WS.url(url).get().map(
                new F.Function<WSResponse, JsonNode>() {
                    public JsonNode apply(WSResponse response) {
                        return response.asJson();
                    }
                }
        );

        return jsonPromise.get(100000);
    }
}
