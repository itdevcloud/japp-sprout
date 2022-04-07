package com.itdevcloud.japp.se.common.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
public class DateUtil {
	public static final String DEFAULT_DATE_FORMAT = "yyyyMMddHHmmss";
	public static final String DEFAULT_DISPLAY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z";

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
        LocalDateTime now = LocalDateTime.now();
		return now.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}
	
	public static ZoneOffset getSystemDefaultOffset() {
		return OffsetDateTime.now().getOffset();
	}
	public static ZoneId getSystemDefaultZoneId() {
		return ZoneId.systemDefault();
	}
	
	public static ZonedDateTime getZonedDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
		if(localDateTime == null) {
			return null;
		}
		if(zoneId == null) {
			zoneId = ZoneId.systemDefault();
		}
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
		return zonedDateTime;
	}
	public static OffsetDateTime getOffsetDateTimee(LocalDateTime localDateTime, ZoneOffset offset) {
		if(localDateTime == null) {
			return null;
		}
		if(offset == null) {
			offset = getSystemDefaultOffset();
		}
		return OffsetDateTime.of(localDateTime, offset);
	}
	
	public static Long LocalDateTimeToEpochSecond(LocalDateTime localDateTime, ZoneOffset offset) {
		if(localDateTime == null) {
			return null;
		}
		if(offset == null) {
			offset = getSystemDefaultOffset();
		}
		return localDateTime.toEpochSecond(offset);
		
	}
	public static Long LocalDateTimeToEpochMilliSecond(LocalDateTime localDateTime, ZoneOffset offset) {
		if(localDateTime == null) {
			return null;
		}
		if(offset == null) {
			offset = getSystemDefaultOffset();
		}
		Instant instant = localDateTime.toInstant(offset);	
		return instant.toEpochMilli(); 	
	}
	
	public static LocalDateTime epochSecondToLocalDateTime(Long timeInSeconds, ZoneOffset offset) {
		if(timeInSeconds == null) {
			return null;
		}
		if(offset == null) {
			offset = getSystemDefaultOffset();
		}
		return LocalDateTime.ofEpochSecond(timeInSeconds, 0, offset);
	}	
	
	public static LocalDateTime epochMilliSecondToLocalDateTime(Long timeInMilliSeconds, ZoneId zoneId) {
		if(timeInMilliSeconds == null) {
			return null;
		}
		if(zoneId == null) {
			zoneId = ZoneId.systemDefault();
		}
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeInMilliSeconds), zoneId); 
	}	
	
//	public static long dataToJsonNumericDate(Date date) {
//		if (date == null) {
//			return 0;
//		}
//		long numericDate = date.getTime()/1000;
//		return numericDate;
//	}
//	//json numericDate is second based RFC 7519
//	public static Date jsonNumericDateToDate(long numericDate) {
//		if (numericDate <= 0) {
//			return null;
//		}
//		Date date = new Date(numericDate*1000);
//		return date;
//	}
	
	public static LocalDate convertToLocalDate(Date dateToConvert, ZoneId zoneId) {
		if(zoneId == null) {
			zoneId = getSystemDefaultZoneId();
		}
	    return LocalDate.ofInstant(
	      dateToConvert.toInstant(), zoneId);
	}

	public static LocalDateTime convertToLocalDateTime(Date dateToConvert, ZoneId zoneId) {
		if(zoneId == null) {
			zoneId = getSystemDefaultZoneId();
		}
	    return LocalDateTime.ofInstant(
	      dateToConvert.toInstant(), zoneId);
	}
	public static Date convertLocalDateToDate(LocalDate dateToConvert, ZoneId zoneId) {
		if(zoneId == null) {
			zoneId = getSystemDefaultZoneId();
		}
	    return java.util.Date.from(dateToConvert.atStartOfDay()
	      .atZone(zoneId)
	      .toInstant());
	}
	public static Date convertLocalDateTimeToDate(LocalDateTime dateToConvert, ZoneId zoneId) {
		if(zoneId == null) {
			zoneId = getSystemDefaultZoneId();
		}
	    return java.util.Date
	      .from(dateToConvert.atZone(zoneId)
	      .toInstant());
	}
	
	public static List<String> getTimeZoneList() {
		 
		Set<String> availableZoneIds = ZoneId.getAvailableZoneIds();

        LocalDateTime now = LocalDateTime.now();
        
        class ZoneComparator implements Comparator<ZoneId> {

    	    @Override
    	    public int compare(ZoneId zoneId1, ZoneId zoneId2) {
    	        LocalDateTime now = LocalDateTime.now();
    	        ZoneOffset offset1 = now.atZone(zoneId1).getOffset();
    	        ZoneOffset offset2 = now.atZone(zoneId2).getOffset();

    	        return offset1.compareTo(offset2);
    	    }
    	}
        
        return availableZoneIds
                .stream()
                .map(ZoneId::of)
                .sorted(new ZoneComparator())
                .map(id -> String.format("%s - %s", getOffset(now, id), id.getId()))
                .collect(Collectors.toList());

	}
    public static String getOffset(LocalDateTime dateTime, ZoneId id) {
        return dateTime
          .atZone(id)
          .getOffset()
          .getId()
          .replace("Z", "+00:00");
    }	
	public static String dateToString(Date date) {
		return dateToString(date, DEFAULT_DISPLAY_DATE_FORMAT);
	}

	public static String dateToString(Date date, String pattern) {
		if (date == null) {
			return null;
		}

		return new SimpleDateFormat(pattern).format(date);
	}

	public static Date stringToDate(String strDate) throws ParseException {
		return stringToDate(strDate, DEFAULT_DISPLAY_DATE_FORMAT);
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
