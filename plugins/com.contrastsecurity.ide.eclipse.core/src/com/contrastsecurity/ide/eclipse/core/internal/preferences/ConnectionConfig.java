package com.contrastsecurity.ide.eclipse.core.internal.preferences;

public class ConnectionConfig {

	private String tsUrl;
	private String username;
	private String serviceKey;
	private String apiKey;
	private String orgName;
	private String orgId;
	
	public ConnectionConfig() {
		//Default
	}
	
	public ConnectionConfig(String tsUrl, String username, String serviceKey, String apiKey, String orgName, String orgId) {
		this.tsUrl = tsUrl;
		this.username = username;
		this.serviceKey = serviceKey;
		this.apiKey = apiKey;
		this.orgName = orgName;
		this.orgId = orgId;
	}
	
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
	
	@Override
	public boolean equals(Object object) {
		if(object == null || !(object instanceof ConnectionConfig))
			return false;
		
		ConnectionConfig config = (ConnectionConfig) object;
		
		if(!tsUrl.equals(config.getTsUrl()))
			return false;
		if(!username.equals(config.getUsername()))
			return false;
		if(!serviceKey.equals(config.getServiceKey()))
			return false;
		if(!apiKey.equals(config.getApiKey()))
			return false;
		if(!orgName.equals(config.getOrgName()))
			return false;
		if(!orgId.equals(config.getOrgId()))
			return false;
		
		return true;
	}
	
}
