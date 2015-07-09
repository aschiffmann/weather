package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import java.util.Iterator;

public class News implements IMessageSource, Runnable {

    @Override
    public JsonNode getJsonData() {
        String url = "http://ajax.googleapis.com/ajax/services/search/news?v=1.0&q=spiegel+online+schlagzeilen";
        return Communication.getJson(url);
    }

    @Override
    public String getMessageFromData(JsonNode data) {

        String message = "";
        for(Iterator<JsonNode> news = data.get("responseData").get("results").elements(); news.hasNext(); ) {
            message += news.next().get("titleNoFormatting").asText().replace(" - SPIEGEL ONLINE", "");
            message += "\n";
        }
        Logger.debug("Created message:\n" + message);
        return message;
    }

    @Override
    public String getSenderFromData(JsonNode data) {
        return "News";
    }

    @Override
    public void run() {
        Logger.info("###News tick");
        Communication.process("N", this);
    }
}
