package controllers;

import play.mvc.*;
import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Nothing to say", "no data"));
    }

    public static Result getNews() {
        String message = Communication.process("News", new News());

        return ok(index.render("here's your news", message));
    }

    public static Result getForecast() {
        String message = Communication.process("WF", new Weather());

        return ok(index.render("here's your forecast", message));
    }


}
