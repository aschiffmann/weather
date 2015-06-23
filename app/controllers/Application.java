package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public Result index() {

        return ok(index.render("Nothing to say"));
    }


    public Result doshit() {

        return ok(index.render("shit done"));
    }
}
