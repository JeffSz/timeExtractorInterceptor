package jeff.flume.interceptor.timeExtractInterceptor;

public class ConfigException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;
	private String message;

	public ConfigException(String msg) {
		super();
		this.message = msg;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}
