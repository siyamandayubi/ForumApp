package drupal.forumapp.domain;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by serva on 10/21/2017.
 */

public class DateHelper {
    private static final String GENERAL_DATE_FORMAT = "yy MMM dd";
    private static final String TODATE_DATE_FORMAT = "hh a";
    private static final String THIS_YEAR_FORMAT = "MMM dd";

    public static String format(long dateInSeconds) {
        Calendar givenDate = Calendar.getInstance();
        givenDate.setTimeInMillis(dateInSeconds * 1000);
        Calendar today = Calendar.getInstance();
        //here your time in miliseconds
        String format = givenDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) ?
                givenDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        givenDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) ?
                        TODATE_DATE_FORMAT : THIS_YEAR_FORMAT : GENERAL_DATE_FORMAT;

        return android.text.format.DateFormat.format(format, dateInSeconds * 1000).toString();
    }
}
