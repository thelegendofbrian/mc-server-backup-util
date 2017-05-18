package io.github.thelegendofbrian.utility;

import java.io.File;
import java.util.logging.Logger;
import io.github.talkarcabbage.logger.LoggerManager;

public class Main {
	
	public static void main(String[] args) {
		Logger logger = LoggerManager.getInstance().getLogger("main");
		// LoggerManager.getInstance().setGlobalLoggingLevel(level);
		
		Server[] serverList;
		
		// Get config settings or make one if one doesn't exist
		// TODO
		
		// Get a list of the folders in the servers directory
		File[] directories = new File(".").listFiles(File::isDirectory);
		serverList = new Server[directories.length];
		
		// Make a Server object for each folder
		for (int i = 0; i < directories.length; i++) {
			logger.info("Making server instance for " + directories[i].getName() + " folder.");
			serverList[i] = new Server(directories[i]);
		}
		
		// Get a list of the folders in the backup directory
		
		// Make a Backup object for each folder
		
	}
	
}
