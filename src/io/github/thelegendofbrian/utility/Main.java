package io.github.thelegendofbrian.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zeroturnaround.zip.ZipUtil;
import io.github.talkarcabbage.logger.LoggerManager;

public class Main {
	
	private static final Logger logger = LoggerManager.getInstance().getLogger("main");
	
	private static HashMap<File, Date> serverMap;
	
	public static String pathToServers = "servers";
	public static String pathToBackups = "backups";
	
	public static void main(String[] args) {
		// TODO: Use GMT instead of local time
		
		// Get config settings or make one if one doesn't exist
		logger.info("Reading configuration file.");
		pathToServers = "servers";
		logger.info("Servers directory found in config: " + new File(pathToServers).getAbsolutePath());
		pathToBackups = "backups";
		logger.info("Backups directory found in config: " + new File(pathToBackups).getAbsolutePath());
		// LoggerManager.getInstance().setGlobalLoggingLevel(level);
		// TODO
		
		// Check if servers directory exists
		logger.info("Checking for valid server file structure.");
		// TODO
		
		// Make backups directory if it doesn't exist
		logger.info("Checking for backups directory.");
		// TODO
		
		// Get a list of the folders in the servers directory
		File[] serverList = new File(pathToServers).listFiles(File::isDirectory);
		
		// Find the most recently changed file in each directory and store when it was last modified
		logger.info("Checking when each server was last modified.");
		serverMap = new HashMap<>();
		Date lastModified;
		for (File serverDir : serverList) {
			lastModified = lastModifiedInFolder(serverDir);
			logger.info("Found server named: \"" + serverDir.getName() + "\" last modified: " + lastModified);
			serverMap.put(serverDir, lastModified);
		}
		
		// Get a list of the folders in the backup directory
		File[] backupList = new File(pathToBackups).listFiles(File::isDirectory);
		// Get the most recent time stamp in each backup directory
		HashMap<File, Date> backupMap = new HashMap<>();
		for (File backupDir : backupList) {
			lastModified = getBackupTimeStamp(getLatestBackup(backupDir));
			logger.info("Found backup for server: \"" + backupDir.getName() + "\" last modified: " + lastModified);
			backupMap.put(backupDir, lastModified);
		}
		
		// Check which servers have been modified since the last backup
		logger.info("Checking which servers need to be backed up.");
		ArrayList<File> serversToBackup = new ArrayList<>();
		for (Map.Entry<File, Date> entry : serverMap.entrySet()) {
			File serverFile = entry.getKey();
		    Date serverLastModified = entry.getValue();
		    
			if (serverLastModified.compareTo(backupMap.get(generateBackupFileFromString(serverFile.getName()))) > 0) {
				logger.info("Server \"" + serverFile.getName() + "\" needs to be backed up.");
				serversToBackup.add(serverFile);
		    }
		}
		
		// Iterate through the servers that need to be backed up
		File backupFolder;
		for (File serverFolder : serversToBackup) {
			logger.info("Backing up server: " + serverFolder.toString());
			backupFolder = generateBackupFileFromString(serverFolder.getName());
			backupServer(serverFolder, backupFolder);
		}
		
	}
	
	/**
	 * Generate the corresponding backup File location given the name of the server folder.
	 * @param fileName
	 * @return
	 */
	public static File generateBackupFileFromString(String fileName) {
		File file = new File(pathToBackups, fileName);
		return file;
	}
	
	/**
	 * Recursively finds the Date of the most recently changed file in a directory.
	 * 
	 * @return mostRecentDate
	 * @throws IOException
	 */
	public static Date lastModifiedInFolder(File file) {
		Wrapper mostRecentTime = new Wrapper();
		try {
			Files.find(
					file.toPath(),
					Integer.MAX_VALUE,
					(filePath, fileAttr) -> true).forEach(x -> {
						if (mostRecentTime.getValue() < x.toFile().lastModified())
							mostRecentTime.setValue(x.toFile().lastModified());
					});
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception caught while scanning a server directory for most recently modified file: ", e);
		}
		
		Date mostRecentDate = new Date(mostRecentTime.getValue());
		
		return mostRecentDate;
	}
	
	
	/**
	 * Gets the time stamp of the backup archive with the most recent time stamp in its filename.
	 */
	public static File getLatestBackup(File singleBackupDirectory) {
		// Get a list of all the files in the directory
		File[] backupList = singleBackupDirectory.listFiles(File::isFile);
		
		// Check which filename contains the most recent time stamp
		Arrays.sort(backupList);
		File mostRecentBackup = backupList[backupList.length - 1];

		return mostRecentBackup;
	}
	
	public static Date getBackupTimeStamp(File backupFile) {
		String nameOfFile = backupFile.getName();
		
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
	
	public static void backupServer(File serverFolder, File backupFolder) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String zipFile = backupFolder.getAbsolutePath() + File.separator + serverFolder.getName() + "_" + sdf.format(serverMap.get(serverFolder)) + ".zip";
		
		ZipUtil.pack(serverFolder, new File(zipFile));
	}
	
}
