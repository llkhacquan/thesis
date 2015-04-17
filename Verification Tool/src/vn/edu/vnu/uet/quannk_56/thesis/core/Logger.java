package vn.edu.vnu.uet.quannk_56.thesis.core;

public class Logger {
	public static void log(String content){
		if (Config.isDebug()){
			System.out.println(content);
		}
	}
}
