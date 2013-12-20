package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.*;
import org.joda.time.*;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import java.util.*;
import org.junit.Before;
import org.junit.Test;

public class TimeUtilsTest {
	
	private List<DateTime> datetimes = new ArrayList<DateTime>();
	private static long MILLIS_PER_SECOND = 1000L;
    private static long MILLIS_PER_MINUTE = 60L * MILLIS_PER_SECOND;
    private static long MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE;
    private static long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;
    private long period;
    
	@Before
	public void setUp() throws Exception {
		DateTime start =new DateTime(2000,1,1,0,0);
		for(int i=0; i<10; i++)
			datetimes.add(start.plusDays(i));
		period = 1L *MILLIS_PER_DAY;
	}

	@Test
	public void testgetPeriodString(){
		long second_period = 44 *MILLIS_PER_SECOND;
		assertEquals("PT44S", TimeUtils.getPeriodString(second_period) );
		long minute_period = 15*MILLIS_PER_MINUTE +second_period;
		assertEquals("PT15M44S", TimeUtils.getPeriodString(minute_period) );
		long hour_period =20* MILLIS_PER_HOUR +minute_period;
		assertEquals("PT20H15M44S", TimeUtils.getPeriodString(hour_period) );
		long day_period = 3 *MILLIS_PER_DAY+ hour_period;
		assertEquals("P3DT20H15M44S", TimeUtils.getPeriodString(day_period) );
	}
	
	@Test
	public void test() {
		String date_period = TimeUtils.getPeriodString(period);
		int datetime_number =datetimes.size();
		String expected =TimeUtils.dateTimeToISO8601(datetimes.get(0)) +"/"+TimeUtils.dateTimeToISO8601(datetimes.get(datetime_number-1))
				+"/"+ date_period;
		assertEquals(expected, TimeUtils.getTimeStringForCapabilities(datetimes) );
		
	}

}
