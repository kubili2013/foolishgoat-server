package com.foolishgoat.server.validator;

public interface Validator {
	public boolean validate(String key,String value, String... rule);
	public String getMsg();
}
