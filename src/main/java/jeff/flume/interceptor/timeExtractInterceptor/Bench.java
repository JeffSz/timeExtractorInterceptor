package jeff.flume.interceptor.timeExtractInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.SimpleEvent;

public class Bench {

	public static void main(String[] args) {
		TimeExtractInterceptor.Builder builder = new TimeExtractInterceptor.Builder();
		String log = "[2017/10/10 14:23:02 123] client.go:292: [info] track_agent1 stat_info:{\"Dtype\":\"streamlist\",\"Dtime\":\"2017-10-10T00:00:01.803+08:00\",\"Dtags\":{\"app\":\"remix\",\"ch\":\"mjygbh_vivo\",\"pt\":\"android\"},\"Dfields\":{\"did\":\"EE2009938A92EE1D50267BC4A216192\",\"userid\":\"138291715084657751\"}}";
		Map<String, String> params = new HashMap<String, String>();
		
		String format = "yyyy/MM/dd HH:mm:ss SSS";
		params.put("TimeFormat", format);
		builder.configure(new Context(params));
		TimeExtractInterceptor interceptor = new TimeExtractInterceptor(builder);
		
		while(true){
			List<Event> list = new ArrayList<Event>();
			for(int i = 0; i< 100000; i++){
				Event e = new SimpleEvent();
				e.setBody((log + i).getBytes());
				list.add(e);
			}
			interceptor.intercept(list);
		}
	}

}
