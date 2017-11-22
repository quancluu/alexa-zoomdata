package zoomdata.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by qcluu on 11/19/17.
 */
public class MiscUtil {

    static public final String GetDataFromFileName(final String fileName) {
       return new MiscUtil().getDataFromFile(fileName);
    }

    private String getDataFromFile(final String fileName) {
        try {
            final InputStream inputStream = getClass().getResourceAsStream(fileName);
            final String data = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Date GetDateFromSql(String sqlDate) throws Exception {
        if (StringUtils.isEmpty(sqlDate) == true) return null;
        sqlDate = sqlDate.trim();
        if (sqlDate.contains(" ") == false) {
            if (sqlDate.contains("-") == true) {
            } else {
                sqlDate += " 00:00:00";
            }
        }

        final String formats[] = {
                "dd/MMM/yyyy:HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "EEE MMM dd HH:mm:ss z yyyy",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy",
        };

        Date dateString = null;
        for (int i = 0; i < formats.length; i++) {
            final String format = formats[i];
            try {
                // TimeZone UTC = TimeZone.getTimeZone("UTC");
                final SimpleDateFormat df = new SimpleDateFormat(format);
                // df.setTimeZone(UTC);

                dateString = df.parse(sqlDate);
            } catch (Exception e) {
                // swallow! Keep trying.
            }
        } // end for.

        if (dateString == null) {
            throw new Exception("*** Error ***: unable to parse sqlDate " + sqlDate);
        }

        return dateString;
    }

    public static String GetSqlZdDateFromDate(final Date date) throws Exception {
        if (date == null) return "";
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return df.format(date);
    }
}
