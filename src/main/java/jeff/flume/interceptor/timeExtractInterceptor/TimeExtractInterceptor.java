package jeff.flume.interceptor.timeExtractInterceptor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.apache.log4j.Logger;

/**
 * Extract time info from Event content, and set the timestamp into the event header.
 * Thread safe.
 * 
 * @author Jeff
 *
 */
public class TimeExtractInterceptor implements Interceptor {
	private static Logger logger = Logger.getLogger(TimeExtractInterceptor.class);

	private Builder 							builder;
	private ThreadLocal<SimpleDateFormat> 		format;

	public TimeExtractInterceptor(Builder builder) {
		this.builder = builder;
		this.initialize();
	}

	/*
	 * Get format tool.
	 * User ThreadLocal to share object and keep thread safe.
	 */
	private SimpleDateFormat getFormat() {
		SimpleDateFormat fmt = format.get();
		if (null == fmt) {
			fmt = new SimpleDateFormat(this.builder.getTimeFormat());
			format.set(fmt);
		}
		return fmt;
	}

	/***
	 * Process single event.
	 * Event will be filtered if there is no time pattern found or time string is not valid.
	 * @param {@link org.apache.flume.Event}
	 * @return {@link org.apache.flume.Event}
	 */
	@Override
	public Event intercept(Event event) {
		String content = new String(event.getBody());
		Matcher matcher = this.builder.getTimePattern().matcher(content);
		if (matcher.find()) {
			try {
				event.getHeaders().put("timestamp", getFormat().parse(matcher.group(0)).getTime() + "");
			} catch (ParseException e) {
				logger.debug("abandon: " + content);
				return null;
			}
			return event;
		} else {
			logger.debug("abandon: " + content);
			return null;
		}
	}

	/***
	 * Process a list of events.
	 * @see #intercept(Event)
	 */
	@Override
	public List<Event> intercept(List<Event> events) {
		List<Event> results = new ArrayList<Event>();
		for (Event event : events) {
			Event e = intercept(event);
			if (null != e) {
				results.add(event);
			}
		}
		return results;
	}
	
	@Override
	public void close() {
		format.remove();
	}

	@Override
	public void initialize() {
		format = new ThreadLocal<SimpleDateFormat>();
	}

	/***
	 * Builder of {@link #TimeExtractInterceptor()}
	 * @author Jeff
	 *
	 */
	public static class Builder implements Interceptor.Builder {
		/***
		 * Time String format {@link #configure(Context)}
		 */
		private String TimeFormat;
		
		/***
		 * Regex pattern of TimeFormat
		 */
		private Pattern TimePattern;

		public Builder() {
			this.setTimeFormat("yyyy-MM-dd HH:mm:ss");
		}

		/***
		 * @param timeFormat {@link #configure(Context)}
		 */
		public Builder(String timeFormat) {
			this.setTimeFormat(timeFormat);
		}
		
		/*
		 * Make pattern from format configure.
		 */
		private String makePattern(String format){
			char[] chars = this.TimeFormat.toCharArray();
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < chars.length;) {
				char ch = chars[i++];
				switch (ch) {
				case '\\':
				case '.':
				case '*':
				case '?':
				case '[':
				case ']':
				case '(':
				case ')':
				case '{':
				case '}':
				case '<':
				case '>':
				case '$':
				case '^':
					sb.append("\\" + ch);
					break;
				case 'y':
					// yyyy | yyy | yy | y
					if (i < chars.length && chars[i] == ch) {
						i++;
						if (i < chars.length && chars[i] == ch) {
							i++;
							if (i < chars.length && chars[i] == ch) {
								i++;
								sb.append("([0-9]{4})");
							} else
								sb.append("([0-9]{4})");
						} else
							sb.append("([0-9]{2})");
					} else
						sb.append("([0-9]{4})");
					break;
				case 'M':
					// MMM | MM | M
					if (i < chars.length && chars[i] == ch) {
						i++;
						if (i < chars.length && chars[i] == ch) {
							i++;
							sb.append("((Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))");
						} else
							sb.append("((0|[1-9])|(1[0-2]))");
					} else
						sb.append("(([1-9])|(1[0-2]))");
					break;
				case 'd':
					// dd | d
					if (i < chars.length && chars[i] == ch) {
						i++;
						sb.append("((0[1-9])|([1-2][0-9])|(3[0-1]))");
					} else
						sb.append("([1-9]|([1-2][0-9])|(3[0-1]))");
					break;
				case 'H':
					// HH | H
					if (i < chars.length && chars[i] == ch) {
						i++;
						sb.append("(([0-1][0-9])|(2[0-3]))");
					} else
						sb.append("(([0-9])|(1[0-9])|(2[0-3]))");
					break;
				case 'h':
					// hh | h
					if (i < chars.length && chars[i] == ch) {
						i++;
						sb.append("((0[1-9])|(1[0-2]))");
					} else
						sb.append("(([1-9])|(1[0-2]))");
					break;
				case 'm':
					// mm | m
				case 's':
					// ss | s
					if (i < chars.length && chars[i] == ch) {
						i++;
						sb.append("([0-5][0-9])");
					} else
						sb.append("([0-9]|([1-5][0-9]))");
					break;
				case 'S':
					// SSS | S
					if (i + 1 < chars.length && chars[i] == ch && chars[i + 1] == ch) {
						i += 2;
						sb.append("([0-9]{3})");
					} else
						sb.append("([0-9]|([1-9][0-9])|([1-9][0-9]{2}))");
					break;
				case 'a':
					// a
					sb.append("(AM|PM)");
					break;
				default:
					sb.append(ch);
				}
			}
			
			return sb.toString();
		}

		private void setTimeFormat(String timeFormat) {
			this.TimeFormat = timeFormat;
			
			try {
				this.TimePattern = Pattern.compile(makePattern(timeFormat));
			} catch (PatternSyntaxException pse) {
				ConfigException th = new ConfigException(pse.getMessage());
				// th.addSuppressed(pse);
				throw th;
			}
		}
		
		public Pattern getTimePattern(){
			return this.TimePattern;
		}
		
		public String getTimeFormat(){
			return this.TimeFormat;
		}

		/***
		 * Configure TimeExtractorInterceptor Builder
		 * Valid item name is:
		 * 		TimeFormat
		 * 			Valid parts are:
		 * 				yyyy 	-- year, 	e.g: 2018 	(0-9999)
		 * 				yy		-- year, 	e.g: 18		(0-99)
		 *  			MMM		-- month, 	e.g: Jan	(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)
		 *  			MM		-- month, 	e.g: 03		(01-12)
		 *  			M		-- month,	e.g: 3		(1-12)
		 * 				dd		-- day,		e.g: 08		(01-31)
		 *  			d		-- day,		e.g: 8		(1-31)
		 *  			HH		-- hour,	e.g: 14|08	(00-23)
		 *  			H		-- hour, 	e.g: 14|8	(0-23)
		 *  			hh		-- hour,	e.g: 02|08	(0-12)
		 *  			h		-- hour,	e.g: 2|8	(0-12)
		 *  			mm		-- minute,	e.g: 05		(00-59)
		 *  			m		-- minute,	e.g: 5		(0-59)
		 *  			ss		-- second,	e.g: 05		(00-59)
		 *  			s		-- second,	e.g: 5		(0-59)
		 *  			SSS		-- millisecond,	e.g: 005	(000-999)
		 *  			S		-- millisecond,	e.g: 5	(0-999)
		 *  
		 *  @param flume context.
		 * 			
		 */
		public void configure(Context context) {
			String timeFormat = context.getString("TimeFormat", this.TimeFormat);
			logger.info("read TimeFormat: " + this.TimeFormat);
			this.setTimeFormat(timeFormat);
		}

		public Interceptor build() {
			return new TimeExtractInterceptor(this);
		}

	}
}
