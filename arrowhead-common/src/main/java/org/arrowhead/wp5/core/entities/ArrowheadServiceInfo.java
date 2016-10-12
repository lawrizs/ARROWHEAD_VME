package org.arrowhead.wp5.core.entities;

public class ArrowheadServiceInfo {
	private String hostname;
	private int port;
	private String resource;
	private String service;
	private String aggId;

	public ArrowheadServiceInfo(String aggId, String hostname, int port, String resource, String service) {
		this.aggId = aggId;
		this.hostname = hostname;
		this.port = port;
		this.resource = resource;
		this.service = service;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public String getResource() {
		return resource;
	}

	public String getService() {
		return service;
	}

	public String getAggId() {
		return aggId;
	}
}
