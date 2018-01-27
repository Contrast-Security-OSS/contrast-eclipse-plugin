package com.contrastsecurity.ide.eclipse.core.internal.preferences;

public class ConnectionConfig {

	private String tsUrl;
	private String username;
	private String serviceKey;
	private String apiKey;
	private String orgName;
	private String orgId;
	
	public String getTsUrl() {
		return tsUrl;
	}
	
	public void setTsUrl(String tsUrl) {
		this.tsUrl = tsUrl;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getServiceKey() {
		return serviceKey;
	}
	
	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}
	
	public String getApiKey() {
		return apiKey;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public String getOrgName() {
		return orgName;
	}
	
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	
	public String getOrgId() {
		return orgId;
	}
	
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	
}
