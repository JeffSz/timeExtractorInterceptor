package jeff.flume.interceptor.timeExtractInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.SimpleEvent;
import org.junit.Test;

import junit.framework.TestCase;

public class TestTimeExtractInterceptor extends TestCase {

	@Test
	public void testExtract(){
		TimeExtractInterceptor.Builder builder = new TimeExtractInterceptor.Builder();
		GregorianCalendar time = new GregorianCalendar(2017, 9, 19, 14, 3, 6);
		String log = "client.go:292: [info] test line info";
		Map<String, String> params = new HashMap<String, String>();
		
		// 
		String format = "yyyy/MM/dd HH:mm:ss";
		params.put("TimeFormat", format);
		builder.configure(new Context(params));
		TimeExtractInterceptor interceptor = new TimeExtractInterceptor(builder);
		
		Event e = new SimpleEvent();
		e.setBody((new SimpleDateFormat(format).format(time.getTime()) + " " + log).getBytes());
		e = interceptor.intercept(e);
		assertNotNull(e);
		assertNotNull(e.getHeaders().get("timestamp"));
		assertEquals(time.getTime().getTime(), Long.parseLong(e.getHeaders().get("timestamp")));
		
		// Back slash
		format = "yyyy\\MM\\dd HH:m:ss";
		params.put("TimeFormat", format);
		builder.configure(new Context(params));
		interceptor = new TimeExtractInterceptor(builder);
		
		e = new SimpleEvent();
		e.setBody((new SimpleDateFormat(format).format(time.getTime()) + " " + log).getBytes());
		e = interceptor.intercept(e);
		assertNotNull(e);
		assertNotNull(e.getHeaders().get("timestamp"));
		assertEquals(time.getTime().getTime(), Long.parseLong(e.getHeaders().get("timestamp")));
		
		// MiliSecond
		time.add(Calendar.MILLISECOND, 2);
		format = "yyyy/MM/dd HH:mm:ss SSS";
		params.put("TimeFormat", format);
		builder.configure(new Context(params));
		interceptor = new TimeExtractInterceptor(builder);
		
		e = new SimpleEvent();
		e.setBody((new SimpleDateFormat(format).format(time.getTime()) + " " + log).getBytes());
		e = interceptor.intercept(e);
		assertNotNull(e);
		assertNotNull(e.getHeaders().get("timestamp"));
		assertEquals(time.getTime().getTime(), Long.parseLong(e.getHeaders().get("timestamp")));
		
		// a
		format = "yyyy/MMM/dd hh:mm:ss SSS a";
		params.put("TimeFormat", format);
		builder.configure(new Context(params));
		interceptor = new TimeExtractInterceptor(builder);
		
		e = new SimpleEvent();
		e.setBody((new SimpleDateFormat(format).format(time.getTime()) + " " + log).getBytes());
		e = interceptor.intercept(e);
		assertNotNull(e);
		assertNotNull(e.getHeaders().get("timestamp"));
		assertEquals(time.getTime().getTime(), Long.parseLong(e.getHeaders().get("timestamp")));
	}
	
	@Test
	public void testMultiComponentConfig(){
		TimeExtractInterceptor.Builder builder = new TimeExtractInterceptor.Builder();
		GregorianCalendar time = new GregorianCalendar(2017, 9, 19, 14, 3, 6);
		String log = "client.go:292: [info]: test line info";
		Map<String, String> params = new HashMap<String, String>();
		String format = "yyyy/MMM/dd HH:mm:ss SSS a";
		params.put("TimeFormat", format);
		builder.configure(new Context(params));
		TimeExtractInterceptor interceptor = new TimeExtractInterceptor(builder);
		
		Event e = new SimpleEvent();
		e.setBody((new SimpleDateFormat(format).format(time.getTime()) + " " + log).getBytes());
		interceptor.intercept(e);
	}
	
	@Test(timeout=2000)
	public void testExtractMulti(){
		TimeExtractInterceptor.Builder builder = new TimeExtractInterceptor.Builder();
		String log = "[2017/10/10 14:23:02 123] client.go:292: [info] test line info";
		Map<String, String> params = new HashMap<String, String>();
		
		String format = "yyyy/MM/dd HH:mm:ss SSS";
		params.put("TimeFormat", format);
		builder.configure(new Context(params));
		TimeExtractInterceptor interceptor = new TimeExtractInterceptor(builder);
		
		List<Event> list = new ArrayList<Event>();
		for(int i = 0; i< 100000; i++){
			Event e = new SimpleEvent();
			e.setBody((log + i).getBytes());
			list.add(e);
		}
		interceptor.intercept(list);
	}

}
