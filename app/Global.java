import akka.actor.Cancellable;
import controllers.News;
import controllers.Weather;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.FiniteDuration;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Global extends GlobalSettings {
    private static Cancellable weatherJob;
    private static Cancellable newsJob;

    @Override
    public void onStart(Application app) {
        startWeatherJob();
        startNewsJob();
    }

    private void startWeatherJob() {
        Logger.info("starting Weather-Job");

        try {
            FiniteDuration nextRun = calculateDelayToTime("16:40");

            weatherJob = Akka.system().scheduler().scheduleOnce(nextRun, new Runnable() {
                 @Override
                 public void run() {
                     new Weather().run();
                     // run itself to calculate next run time
                     startWeatherJob();
                 }
             }, Akka.system().dispatcher());

            Logger.debug("Next weather-report will be sent in " + nextRun.toString());
        } catch (Exception e) {
            Logger.error("Error while executing weather-job", e);
        }
    }

    private void startNewsJob() {
        Logger.info("starting News-Job");

        try {
            FiniteDuration nextRun = calculateDelayToTime("03:00");

            newsJob = Akka.system().scheduler().scheduleOnce(nextRun, new Runnable() {
                @Override
                public void run() {
                    new News().run();
                    // run itself to calculate next run time
                    startNewsJob();
                }
            }, Akka.system().dispatcher());

            Logger.debug("Next news-report will be sent in " + nextRun.toString());
        } catch (Exception e) {
            Logger.error("Error while executing news-job", e);
        }
    }

    @Override
    public void onStop(Application app) {
        if (weatherJob != null && !weatherJob.isCancelled()) {
            weatherJob.cancel();
            Logger.info("Weather-Job cancelled");
        }

        if (newsJob != null && !newsJob.isCancelled()) {
            newsJob.cancel();
            Logger.info("News-Job cancelled");
        }
    }

    private static FiniteDuration calculateDelayToTime(String dayTime) {
        DateTime dayTimeDate = DateTimeFormat.forPattern("HH:mm").parseDateTime(dayTime);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, dayTimeDate.getHourOfDay());
        calendar.set(Calendar.MINUTE, dayTimeDate.getMinuteOfHour());
        calendar.set(Calendar.SECOND, dayTimeDate.getSecondOfMinute());

        Date plannedStart = calendar.getTime();
        Date now = new Date();
        Date nextRun;
        if(now.after(plannedStart)) {
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            nextRun = calendar.getTime();
        } else {
            nextRun = calendar.getTime();
        }
        Long delay = (nextRun.getTime() - now.getTime());

        return FiniteDuration.create(delay, TimeUnit.MILLISECONDS);
    }
}