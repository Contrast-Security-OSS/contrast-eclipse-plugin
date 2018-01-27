package com.contrastsecurity.ide.eclipse.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.contrastsecurity.ide.eclipse.core.internal.preferences.ConnectionConfig;

public class MapUtil {
	
	private final static String ELEMENT_DELIMITER = "|@|@|";
	private final static String PROPERTY_DELIMITER = "|&|&|";
	private final static String KEY_SEPERATOR = "|";
	
	public static String convertFromMap(Map<String, ConnectionConfig> configs) {
		StringBuffer buffer = new StringBuffer();
		
		for(Entry<String, ConnectionConfig> entry : configs.entrySet()) {
			buffer.append(entry.getKey());
			buffer.append(PROPERTY_DELIMITER);
			buffer.append(convertConfigToString(entry.getValue()));
			buffer.append(ELEMENT_DELIMITER);
		}
		
		return buffer.toString();
	}
	
	public static Map<String, ConnectionConfig> convertToMap(String configString) {
		Map<String, ConnectionConfig> map = new HashMap<>();
		String[] configs = StringUtils.splitByWholeSeparator(configString, ELEMENT_DELIMITER);
		
		for(String config : configs) {
			if(StringUtils.isBlank(config))
				continue;
			
			String[] props = StringUtils.splitByWholeSeparator(config, PROPERTY_DELIMITER);
			ConnectionConfig conf = new ConnectionConfig();
			conf.setUsername(props[1]);
			conf.setTsUrl(props[2]);
			conf.setServiceKey(props[3]);
			conf.setApiKey(props[4]);
			conf.setOrgName(props[5]);
			conf.setOrgId(props[6]);
			
			map.put(props[0], conf);
		}
		
		return map;
	}
	
	public static String convertConfigToString(ConnectionConfig config) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(config.getUsername());
		buffer.append(PROPERTY_DELIMITER);
		buffer.append(config.getTsUrl());
		buffer.append(PROPERTY_DELIMITER);
		buffer.append(config.getServiceKey());
		buffer.append(PROPERTY_DELIMITER);
		buffer.append(config.getApiKey());
		buffer.append(PROPERTY_DELIMITER);
		buffer.append(config.getOrgName());
		buffer.append(PROPERTY_DELIMITER);
		buffer.append(config.getOrgId());
		
		return buffer.toString();
	}
	
	public static String generateConfigurationKey(ConnectionConfig config) {
		return config.getOrgId() + KEY_SEPERATOR + config.getServiceKey();
	}

}
