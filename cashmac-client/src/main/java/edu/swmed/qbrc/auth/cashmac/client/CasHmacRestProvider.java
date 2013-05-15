package edu.swmed.qbrc.auth.cashmac.client;

import com.google.inject.Provider;

public interface CasHmacRestProvider<T> extends Provider<T> {
	public String getClientId();
	public String getSecret();
}
