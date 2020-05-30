package com.itdevcloud.japp.se.common.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class DateUtils {
	public static final String DEFAULT_DATE_FORMAT = "yyyyMMddHHmmss";
	public static final String DDEFAULT_DISPLAY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z";

    private static DatatypeFactory datatypeFactory = null;
    static {
        try {
        	datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException dce) {
            throw new IllegalStateException(
                "Exception while obtaining DatatypeFactory instance", dce);
        }
    }  
	public static String currentDate() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DDEFAULT_DISPLAY_DATE_FORMAT);
		return sdf.format(cal.getTime());
	}

	public static String dateToString(Date date) {
		return dateToString(date, DDEFAULT_DISPLAY_DATE_FORMAT);
	}

	public static String dateToString(Date date, String pattern) {
		if (date == null)
			return null;

		return new SimpleDateFormat(pattern).format(date);
	}

	public static Date stringToDate(String strDate) throws ParseException {
		return stringToDate(strDate, DDEFAULT_DISPLAY_DATE_FORMAT);
	}

	public static Date stringToDate(String strDate, String pattern) {
		if (strDate == null) {
			return null;
		}
		DateFormat df = new SimpleDateFormat(pattern);
		Date date;
		try {
			date = df.parse(strDate);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return date;
	}

	public static Timestamp stringToTimestamp(String strDate, String pattern) {
		Date date = stringToDate(strDate, pattern);
		if (date == null) {
			return null;
		}
		long time = date.getTime();
		return new Timestamp(time);
	}
	
	public static Timestamp stringToTimestamp(String strDate) {
		return stringToTimestamp(strDate, null);
	}
	
	
	public static Calendar dateStringToCalendar(String s) throws ParseException {
		return dateStringToCalendar(s, DEFAULT_DATE_FORMAT);
	}

	public static Calendar dateStringToCalendar(String s, String pattern)
			throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(pattern);
		Date d1 = df.parse(s);

		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		return cal;
	}

	public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) {
		return stringToXMLGregorianCalendar(dateToString(date));
	}

	public static XMLGregorianCalendar stringToXMLGregorianCalendar(
			String strDate) {

		int idx = strDate.indexOf("-");

		if (idx != -1) {
			strDate = strDate.replaceAll("-", "");
		}
		//format yyyyMMddHHmmss
		DatatypeFactory dFactory;
		try {
			XMLGregorianCalendar xmlCal = datatypeFactory.newXMLGregorianCalendar();
			
			xmlCal.setYear(Integer.parseInt(strDate.substring(0, 4)));
			xmlCal.setMonth(Integer.parseInt(strDate.substring(4, 6)));
			xmlCal.setDay(Integer.parseInt(strDate.substring(6, 8)));
			xmlCal.setHour(Integer.parseInt(strDate.substring(8, 10)));
			xmlCal.setMinute(Integer.parseInt(strDate.substring(10, 12)));
			xmlCal.setSecond(Integer.parseInt(strDate.substring(12, 14)));

			return xmlCal;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static Timestamp xMLGregorianCalendarToTimestamp(
			XMLGregorianCalendar xmlCal) throws DatatypeConfigurationException {
		long time = xmlCal.toGregorianCalendar().getTimeInMillis();
		return new Timestamp(time);

	}
	public static Date xMLGregorianCalendarToDate(
			XMLGregorianCalendar xmlCal) throws DatatypeConfigurationException {
		long time = xmlCal.toGregorianCalendar().getTimeInMillis();
		return new Date(time);

	}
    
    public static XMLGregorianCalendar dateToSimpleXMLGregorianCalendar(java.util.Date date) {
        if (date == null) {
            return null;
        } else {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis(date.getTime());
            return datatypeFactory.newXMLGregorianCalendarDate(
                	gc.get(Calendar.YEAR), 
                	gc.get(Calendar.MONTH)+1, 
                	gc.get(Calendar.DAY_OF_MONTH), 
                	DatatypeConstants.FIELD_UNDEFINED);
        }
    }   

    public static XMLGregorianCalendar toSimpleXMLGregorianCalendar(XMLGregorianCalendar xmlGregorianCalendar) {
    	return dateToSimpleXMLGregorianCalendar(XMLGregorianCalendarToDate(xmlGregorianCalendar));
    }   

    public static java.util.Date XMLGregorianCalendarToDate(XMLGregorianCalendar xmlGregorianCalendar) {
        if (xmlGregorianCalendar == null) {
            return null;
        } else {
            return xmlGregorianCalendar.toGregorianCalendar().getTime();
        }
    }
}
