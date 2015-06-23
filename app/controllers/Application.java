package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public Result index() {

        return ok(index.render("Nothing to say"));
    }


    public Result doshit() {
        String weiden = "http://api.wunderground.com/api/2d20da4a2a78196d/forecast10day/lang:DL/q/zmw:00000.1.10688.json";

        F.Promise<JsonNode> jsonPromise = WS.url(weiden).get().map(
                new F.Function<WSResponse, JsonNode>() {
                    public JsonNode apply(WSResponse response) {
                        return response.asJson();
                    }
                }
        );

        String result;
        try {
            JsonNode jsonNode = jsonPromise.get(100000);
            result = getTomorrowText(jsonNode).get("title").asText() + "<br>"
                    + getTomorrowText(jsonNode).get("fcttext_metric").asText()
                    + getTomorrowNrs(jsonNode).get("low").get("celsius").asText() + "-"+ getTomorrowNrs(jsonNode).get("high").get("celsius").asText() + "|"
                    + getTomorrowNrs(jsonNode).get("pop").asText() + "%"  ;
        } catch(Exception e) {
            result = "fail<br>"+e.getStackTrace();
        }
        return ok(index.render(result));
    }


    private JsonNode getTomorrowText(JsonNode jsonNode) {
        return jsonNode.get("forecast").get("txt_forecast").get("forecastday").elements().next();
    }
    private JsonNode getTomorrowNrs(JsonNode jsonNode) {
        return jsonNode.get("forecast").get("simpleforecast").get("forecastday").elements().next();
    }
}
