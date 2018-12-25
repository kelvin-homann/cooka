package app.cooka.cookapp.utils;

import android.content.Context;
import android.content.res.Resources;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import app.cooka.cookapp.R;

public class TimeUtils {

    public static long getDateDifferenceInDays(Date laterDate, Date earlierDate) {

        long diff = laterDate.getTime() - earlierDate.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public static long getDateDifferenceInHours(Date laterDate, Date earlierDate) {

        long diff = laterDate.getTime() - earlierDate.getTime();
        return TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public static long getDateDifferenceInMinutes(Date laterDate, Date earlierDate) {

        long diff = laterDate.getTime() - earlierDate.getTime();
        return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets a Android localized string in the form "[n] [time] ago" where [time] is either minutes,
     *      hours, days, weeks or years and [n] the corresponding value; or "[n] [time]" is equal
     *      to "some seconds ago" if the time is less than a minute.
     * @param context the application or activity context.
     * @param laterDate the later date (usually the current date).
     * @param earlierDate the earlier date that is before laterDate.
     * @return an Android localized string in the form "[n] [time] ago".
     */
    public static String getTimeAgoString(Context context, Date laterDate, Date earlierDate) {

        if(context == null || laterDate == null || earlierDate == null)
            return null;

        Resources resources = context.getResources();
        if(resources == null)
            return null;

        long diff = laterDate.getTime() - earlierDate.getTime();
        int seconds = (int)(diff / 1000);

        // notice: getQuantityString() doesn't work correctly; it does not return the "one"-value
        // for the "R.plurals.XXX_ago_plural" resources

        if(seconds < 60) {
            return resources.getString(R.string.some_seconds_ago);
        }
        else if(seconds < 60 * 60) {
            int minutes = seconds / 60;
            return minutes == 1 ? resources.getString(R.string.one_minute_ago) : resources.getString(R.string.minutes_ago, minutes);
            //return resources.getQuantityString(R.plurals.minutes_ago_plural, minutes, minutes);
        }
        else if(seconds < 24 * 60 * 60) {
            int hours = seconds / 60 / 60;
            return hours == 1 ? resources.getString(R.string.one_hour_ago) : resources.getString(R.string.hours_ago, hours);
            //return resources.getQuantityString(R.plurals.hours_ago_plural, hours, hours);
        }
        else if(seconds < 21 * 24 * 60 * 60) {
            int days = seconds / 24 / 60 / 60;
            return days == 1 ? resources.getString(R.string.one_day_ago) : resources.getString(R.string.days_ago, days);
            //return resources.getQuantityString(R.plurals.days_ago_plural, days, days);
        }
        else if(seconds < 365 * 24 * 60 * 60) {
            int weeks = seconds / 7 / 24 / 60 / 60;
            return weeks == 1 ? resources.getString(R.string.one_week_ago) : resources.getString(R.string.weeks_ago, weeks);
            //return resources.getQuantityString(R.plurals.weeks_ago_plural, weeks, weeks);
        }
        else {
            int years = seconds / 365 / 24 / 60 / 60;
            return years == 1 ? resources.getString(R.string.one_year_ago) : resources.getString(R.string.years_ago, years);
            //return resources.getQuantityString(R.plurals.years_ago_plural, years, years);
        }
    }

    /**
     * Gets a Android localized string in the form "[n] [time] ago" where [time] is either minutes,
     *      hours, days, weeks or years and [n] the corresponding value; or "[n] [time]" is equal
     *      to "a couple of seconds ago" if the time is less than a minute.
     * @param context the application or activity context.
     * @param sinceDate the past date that is "time" ago from the current date (now).
     * @return an Android localized string in the form "[n] [time] ago".
     */
    public static String getTimeAgoString(Context context, Date sinceDate) {

        return getTimeAgoString(context, new Date(), sinceDate);
    }
}
