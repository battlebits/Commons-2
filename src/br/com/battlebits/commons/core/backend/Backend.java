package br.com.battlebits.commons.core.backend;

public interface Backend {

	public void startConnection() throws Exception;

	public void closeConnection() throws Exception;

	public boolean isConnected() throws Exception;

	public void recallConnection() throws Exception;

}
