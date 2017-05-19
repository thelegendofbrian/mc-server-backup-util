package io.github.thelegendofbrian.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.talkarcabbage.logger.LoggerManager;

public class Main {
	
	private static Logger logger = LoggerManager.getInstance().getLogger("main");
	static long mostRecentTime = 0L;
	
	public static void main(String[] args) {
		
		// LoggerManager.getInstance().setGlobalLoggingLevel(level);
		
		// Get config settings or make one if one doesn't exist
		// TODO
		
		String pathToServers = "servers";
		String pathToBackups = "backups";
		
		// Check if servers directory exists
		// TODO
		
		// Make backup directory if it doesn't exist
		// TODO
		
		// Get a list of the folders in the servers directory
		File[] serverList = new File(pathToServers).listFiles(File::isDirectory);
		// Find the most recently changed file in each directory and store when it was last modified
		for (File serverDir : serverList) {
			try {
				System.out.println(lastModifiedInFolder(serverDir));
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Exception caught while scanning server directory for most recently modified file: ", e);
			}
		}
		
		// Get a list of the folders in the backup directory
		File[] backupList = new File(pathToBackups).listFiles(File::isDirectory);
		// Get the most recent time stamp in each backup directory
		for (File backupDir : backupList) {
			System.out.println(getBackupTimeStamp(getLatestBackup(backupDir)));
		}
		
		// Check which servers have been modified since the last backup
//		for (int i = 0; i < serverList.length; i++) {
//			logger.info("Making server instance for " + serverList[i].getName() + " folder.");
//		}
		

		
	}
	
	/**
	 * Recursively finds the Date of the most recently changed file in a directory.
	 * @return mostRecentDate 
	 * @throws IOException
	 */
	public static Date lastModifiedInFolder(File file) throws IOException {
		Files.find(
				file.toPath(),
				Integer.MAX_VALUE,
				(filePath, fileAttr) -> true
			).forEach(x -> {
				if (mostRecentTime < x.toFile().lastModified())
					mostRecentTime = x.toFile().lastModified();
			});
		
		Date mostRecentDate = new Date(mostRecentTime);
		
		return mostRecentDate;
	}
	
	
	/**
	 * Gets the time stamp of the backup archive with the most recent time stamp in its filename
	 */
	public static File getLatestBackup(File singleBackupDirectory) {
		// Example: FTB_2017-05-17_14-20-00.zip
		// Get a list of all the files in the directory
		File[] backupList = singleBackupDirectory.listFiles(File::isFile);
		
		// Check which filename contains the most recent time stamp
		Arrays.sort(backupList);
		File mostRecentBackup = backupList[backupList.length - 1];

		return mostRecentBackup;
	}
	
	public static Date getBackupTimeStamp(File backupFile) {
		String nameOfFile = backupFile.getName().toString();
		
		// Remove file extension
		if (nameOfFile.indexOf('.') > 0) {
			nameOfFile = nameOfFile.substring(0, nameOfFile.lastIndexOf('.'));
		}
		
		// Remove backup name
		nameOfFile = nameOfFile.substring(nameOfFile.length() - 19);
		
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").parse(nameOfFile);
		} catch (ParseException e) {
			logger.log(Level.SEVERE, "Unable to parse date format of backup archive: ", e);
		}
		
		return date;
	}
	
}
