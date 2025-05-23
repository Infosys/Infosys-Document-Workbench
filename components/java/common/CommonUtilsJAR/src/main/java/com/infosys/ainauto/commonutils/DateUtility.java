/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class DateUtility {

	private DateUtility() {
		// private constructor to avoid instantiation
	}
	
    public static String toString(Timestamp ts, String format) {
        if (ts == null)
            return "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String string = dateFormat.format(ts);
        return string;
    }

    public static String toString(Date ts, String format) {
        if (ts == null)
            return "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String string = dateFormat.format(ts);
        return string;
    }
    
	public static Date toTimestamp(String date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date dateWithTimestamp=null;
		try {
			dateWithTimestamp = sdf.parse(date);
			} catch (ParseException e) {
		}
		return dateWithTimestamp;
	}
    public static String getDuration(Timestamp tsOld) {
        if (tsOld ==null) return "";
        Timestamp tsNew = new Timestamp((new Date()).getTime());
        return getDuration(tsOld, tsNew);
    }
    
    public static String getDuration(Timestamp tsOld, Timestamp tsNew) {
        // Duration.between(tsNew.getTime(), tsOld.getTime());
        //Period pd = Period.between(tsOld.toLocalDateTime().toLocalDate(), tsNew.toLocalDateTime().toLocalDate());
        //return pd.getYears() + "-" + pd.getMonths() + "-" + pd.getDays() ;
        if (tsOld ==null || tsNew ==null) return "";
        Duration duration = Duration.between(tsOld.toLocalDateTime(), tsNew.toLocalDateTime());
        if (duration.toDays()>1) {
            return duration.toDays() + " days ago";
        }
        if (duration.toDays()==1) {
            return duration.toDays() + " day ago";
        }
        if (duration.toHours()>1) {
            return duration.toHours() + " hours ago";
        }
        if (duration.toHours()==1) {
            return duration.toHours() + " hour ago";
        }
        if (duration.toMinutes()>1) {
            return duration.toMinutes() + " mins ago";
        }
        if (duration.toMinutes()==1) {
            return duration.toMinutes() + " min ago";
        }
        return "Few secs ago";
    }

    public static Date getLocalDate(Timestamp createDt, int offsetToGetLT) {

        int localOffSetMin = (offsetToGetLT) * (-1);

        Calendar now = Calendar.getInstance();
        // get current TimeZone using getTimeZone method of Calendar class
        TimeZone timeZone = now.getTimeZone();
        int serverOffset = timeZone.getRawOffset();
        int serverOffSetMinutes = serverOffset / 60000;

        int offSets = Math.abs(serverOffSetMinutes - localOffSetMin);

        now.setTime(createDt); // sets calendar time/date
        now.add(Calendar.MINUTE, offSets); // adds offset
        Date localDt = now.getTime();

        return localDt;
    }
    
    public static Date addDate(Date date, int daysToAdd) {
    	//SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(date); 
    	cal.add(Calendar.DATE, daysToAdd); 
    	return cal.getTime();
    }

}
