package com.contrastsecurity.ide.eclipse.ui.internal.preferences;

import com.contrastsecurity.ide.eclipse.core.internal.preferences.ConnectionConfig;

public interface IConnectionConfigListener {
	
	void onConnectionSave(ConnectionConfig config);
	void onConnectionUpdate(ConnectionConfig config, String previousKey);

}
