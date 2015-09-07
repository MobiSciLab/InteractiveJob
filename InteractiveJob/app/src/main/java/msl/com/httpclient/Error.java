package msl.com.httpclient;

public enum Error 
{
	NONE(0),
	UNEXPECTED(600),
	NETWORK_CONNECTION(602),
	SERVER_COMMUNICATION(603),
	CLIENT_SUSPENDED(604),
	DISABLED_SOLUTION(605),
	INVALID_LICENSE_FORMAT(606),
	CLIENT_LICENSE_UNAUTHORIZED(607),
	LICENSE_EXPIRED(608),
	INVALID_CUSTOM_FIELDS(609),
	REQUEST_CANCELED(611);
	
	private final int code;
	private Error(int c) 
	{
		this.code = c;
	}
	
	public final int getCode() 
	{
		return code;
	}
}
