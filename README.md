# TimeExtractInterceptor of Flume
Unlike TimestampInterceptor that get timestamp of current time clock, TimeExtractInterceptor extract time information from Event content, matched the interceptor pattern configured in Flume properties file. TimeExtractInterceptor may get the real time that data was produced and ignore event unmatched. 

# Usage:
If the text line in the log is like: 2017/10/10 00:00:02 client.go:292: [info] test line

Flume configure
```
agent.source1.interceptors = e1
agent.source1.interceptors.e1.type = jeff.flume.interceptor.TimeExtractInterceptor.$Builder
agent.source1.interceptors.e1.TimeFormat = yyyy/MM/dd HH:mm:ss
```

TimeFormat parts
```
* 		Valid parts are:
* 			yyyy 	-- year, 	e.g: 2018 	(0-9999)
* 			yy		-- year, 	e.g: 18		(0-99)
*  			MMM		-- month, 	e.g: Jan	(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)
*  			MM		-- month, 	e.g: 03		(01-12)
*  			M		-- month,	e.g: 3		(1-12)
* 			dd		-- day,		e.g: 08		(01-31)
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
```