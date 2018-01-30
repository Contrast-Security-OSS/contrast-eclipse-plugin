package com.contrastsecurity.ide.eclipse.core.unit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.internal.preferences.ConnectionConfig;
import com.contrastsecurity.ide.eclipse.core.util.ConnectionConfigUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry; 

public class ContrastCoreActivatorTest {
	
	private final static String API_KEY_ONE = "myDummyApiKey98745";
	private final static String ORG_NAME_ONE = "org1";
	private final static String ORG_ID_ONE = "orgIdOne";
	private final static String API_KEY_TWO = "myDummyApiKey12345";
	private final static String ORG_NAME_TWO = "org2";
	private final static String ORG_ID_TWO = "orgIdTwo";
	
	private final static String SERVICE_KEY = "thisIsAServiceKey";
	private final static String USERNAME = "someUser";
	private final static String TEAM_SERVER_URL = "http://somewhere.com/api";
	
	private Map<String, ConnectionConfig> configs;
	private Map<String, ConnectionConfig> savedConfigs;
	private static ConnectionConfig config1;
	private static ConnectionConfig config2;
	
	private String addConfig(Map<String, ConnectionConfig> configs, ConnectionConfig config) {
		String key = ConnectionConfigUtil.generateConfigurationKey(config);
		configs.put(key, config);
		return key;
	}
	
	@BeforeClass
	public static void initConfigs() {
		config1 = new ConnectionConfig(TEAM_SERVER_URL, USERNAME, SERVICE_KEY, API_KEY_ONE, ORG_NAME_ONE, ORG_ID_ONE);
		config2 = new ConnectionConfig(TEAM_SERVER_URL, USERNAME, SERVICE_KEY, API_KEY_TWO, ORG_NAME_TWO, ORG_ID_TWO);
	}
	
	@Before
	public void initTest() {
		configs = new HashMap<>();
		savedConfigs = null;
	}
	
	@Test
	public void saveAndGetConfigurationsTest() {
		String key1 = addConfig(configs, config1);
		String key2 = addConfig(configs, config2);
		
		ContrastCoreActivator.saveConfigurations(configs);
		savedConfigs = ContrastCoreActivator.getConfigurations();
		
		assertEquals(API_KEY_ONE, savedConfigs.get(key1).getApiKey());
		assertEquals(ORG_ID_ONE, savedConfigs.get(key1).getOrgId());
		
		assertEquals(API_KEY_TWO, savedConfigs.get(key2).getApiKey());
		assertEquals(ORG_ID_TWO, savedConfigs.get(key2).getOrgId());
		
		for(Entry<String, ConnectionConfig> entry : savedConfigs.entrySet()) {
			assertEquals(SERVICE_KEY, entry.getValue().getServiceKey());
			assertEquals(USERNAME, entry.getValue().getUsername());
		}
	}
	
	@Test
	public void UpdateConfigurationTest() {
		final String key = addConfig(configs, config1);
		ContrastCoreActivator.saveConfigurations(configs);
		savedConfigs = ContrastCoreActivator.getConfigurations();
		
		assertEquals(API_KEY_ONE, savedConfigs.get(key).getApiKey());
		assertEquals(USERNAME, savedConfigs.get(key).getUsername());
		
		final String newApiKey = "newApiKey";
		final String newUsername = "newUser";
		savedConfigs = ContrastCoreActivator.getConfigurations();
		ConnectionConfig config = savedConfigs.get(key);
		config.setApiKey(newApiKey);
		config.setUsername(newUsername);
		
		ContrastCoreActivator.saveConfigurations(savedConfigs);
		savedConfigs = ContrastCoreActivator.getConfigurations();
		
		assertEquals(newApiKey, savedConfigs.get(key).getApiKey());
		assertEquals(newUsername, savedConfigs.get(key).getUsername());
	}
	
	@Test
	public void removeConfigurationTest() throws BackingStoreException {
		String key1 = addConfig(configs, config1);
		String key2 = addConfig(configs, config2);
		
		ContrastCoreActivator.saveConfigurations(configs);
		savedConfigs = ContrastCoreActivator.getConfigurations();
		
		assertEquals(2, savedConfigs.size());
		
		savedConfigs.remove(key1);
		ContrastCoreActivator.saveConfigurations(savedConfigs);
		savedConfigs = ContrastCoreActivator.getConfigurations();
		
		assertEquals(1, savedConfigs.size());
		assertTrue(savedConfigs.containsKey(key2));
		assertTrue(!savedConfigs.containsKey(key1));
	}
	
	@Test
	public void selectAConfigurationTest() {
		String key1 = ConnectionConfigUtil.generateConfigurationKey(config1);
		String key2 = ConnectionConfigUtil.generateConfigurationKey(config2);
		
		ContrastCoreActivator.saveConfigurations(configs);
		ContrastCoreActivator.setSelectedConfiguration(key2, config2);
		
		assertEquals(key2, ContrastCoreActivator.getSelectedConfigKey());
		assertEquals(config2, ContrastCoreActivator.getSelectedConfig());
		
		ContrastCoreActivator.setSelectedConfiguration(key1, config1);
		
		assertEquals(key1, ContrastCoreActivator.getSelectedConfigKey());
		assertEquals(config1, ContrastCoreActivator.getSelectedConfig());;
	}

}
