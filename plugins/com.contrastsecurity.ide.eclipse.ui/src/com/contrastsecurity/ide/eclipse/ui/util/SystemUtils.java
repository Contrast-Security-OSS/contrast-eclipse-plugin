package com.contrastsecurity.ide.eclipse.ui.util;

public class SystemUtils {
	
	private final static String OS_SYSTEM = System.getProperty("os.name");
	
	private final static String WINDOWS_OS = "win";
	private final static String MAC_OS = "mac";
	private final static String UNIX_OS = "unix";
	
	public static boolean isWindowsOS() {
		return OS_SYSTEM.contains(WINDOWS_OS);
	}
	
	public static boolean isUnixOS() {
		return OS_SYSTEM.contains(UNIX_OS);
	}
	
	public static boolean isMacOS() {
		return OS_SYSTEM.contains(MAC_OS);
	}

}
