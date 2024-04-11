package electron.utils;

import java.util.Date;

public class logger {
	private static Date date = new Date();
	
	public static void log(Object msg) {
		System.out.println(getTime()+" INFO: "+msg);
	}
	public static void warn(Object msg) {
		System.err.println(getTime()+" WARN: "+msg);
	}
	public static void error(Object msg) {
		System.err.println(getTime()+" ERROR: "+msg);
	}
	public static void cmd(Object msg) {
		System.out.println(getTime()+" CONSOLE: "+msg);
	}
	@SuppressWarnings("deprecation")
	public static String getTime() {
		date.setTime(System.currentTimeMillis());
		return date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
	}

}
