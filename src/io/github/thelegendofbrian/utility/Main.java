package io.github.thelegendofbrian.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.zeroturnaround.zip.ZipException;
import org.zeroturnaround.zip.ZipUtil;
import io.github.talkarcabbage.logger.LoggerManager;

public class Main {
	
	public static String pathToServers;
	public static String pathToBackups;
	
	private static final Logger logger = LoggerManager.getInstance().getLogger("main");
	private static HashMap<File, Date> serverMap;
	
	public static void main(String[] args) {
		// TODO: Use GMT instead of local time
		
		// Get config settings or make one if one doesn't exist
		logger.info("Reading configuration file.");
		
		Properties defaultProps = new Properties();
		defaultProps.setProperty("logLevel", "INFO");
		defaultProps.setProperty("enablePruning", "false");
		defaultProps.setProperty("pruningThreshold", "60");
		
		Properties properties = new Properties(defaultProps);
		properties.setProperty("serversDirectory", "");
		properties.setProperty("backupsDirectory", "");
		
		File configFile = new File("config.ini");
		loadConfig(configFile, properties);
		saveConfig(configFile, properties);
		
		// Verify validity of config values
		if ("".equals(properties.getProperty("serversDirectory")) || "".equals(properties.getProperty("backupsDirectory"))) {
			// TODO: Add GUI and explanation of how to set up for no GUI
			logger.severe("Invalid directory configuration set.");
			logger.severe("Edit config.ini and specify the serversDirectory and backupsDirectory.");
			System.exit(-1);
		}
		
		pathToServers = properties.getProperty("serversDirectory");
		logger.info("Servers directory found in config: " + new File(pathToServers).getAbsolutePath());
		pathToBackups = properties.getProperty("backupsDirectory");
		logger.info("Backups directory found in config: " + new File(pathToBackups).getAbsolutePath());
		 LoggerManager.getInstance().setGlobalLoggingLevel(Level.parse(properties.getProperty("logLevel")));
		
		// Check if servers directory exists
		logger.info("Checking for valid server file structure.");
		// TODO
		
		// Get a list of the folders in the servers directory
		File[] serverList = new File(pathToServers).listFiles(File::isDirectory);
		
		// Make backups directory if it doesn't exist
		logger.info("Checking for backups directory.");
		File backupsFolder = new File(pathToBackups);

		if (backupsFolder.mkdir()) {
			logger.info("Backups folder \"" + backupsFolder.getName() + "\" created successfully.");
		}
		
		if (!backupsFolder.canWrite()) {
			logger.severe("The specified backups directory cannot be written to.");
			System.exit(-1);
		}
		
		File serverBackupFolder;
		for (File server : serverList) {
			serverBackupFolder = new File(backupsFolder.getAbsolutePath() + File.separator + server.getName());
			if (serverBackupFolder.mkdir()) {
				logger.info("Backup directory did not exist for \"" + server.getName() + "\". Creating directory.");
			}
		}
		
		// Find the most recently changed file in each directory and store when it was last modified
		logger.info("Checking when each server was last modified.");
		serverMap = new HashMap<>();
		Date lastModified;
		for (File serverDir : serverList) {
			lastModified = roundDateToSeconds(lastModifiedInFolder(serverDir));
			// TODO: Format logged date
			logger.info("Found server named: \"" + serverDir.getName() + "\" last modified: " + lastModified);
			serverMap.put(serverDir, lastModified);
		}
		
		// Get a list of the folders in the backup directory
		File[] backupList = backupsFolder.listFiles(File::isDirectory);
		// Get the most recent time stamp in each backup directory
		HashMap<File, Date> backupMap = new HashMap<>();
		// If the backupList is empty, back up all servers
		ArrayList<File> serversToBackup = new ArrayList<>();
		if (backupList.length != 0) {
			for (File backupDir : backupList) {
				// If the backup directory for a server is empty, make a backup for that server
				if (backupDir.list().length == 0) {
					logger.info("Backup directory for server \"" + backupDir.getName() + "\" is empty. Backup will be made.");
					backupMap.put(backupDir, new Date(0L));
				} else {
					lastModified = roundDateToSeconds(getBackupTimeStamp(getLatestBackup(backupDir)));
					// TODO: Format logged date
					logger.info("Found most recent backup for server: \"" + backupDir.getName() + "\" last modified: " + lastModified);
					backupMap.put(backupDir, lastModified);
				}
			}
			
			// Check which servers have been modified since the last backup
			logger.info("Checking which servers need to be backed up.");
			Date backupLastModified;
			for (Map.Entry<File, Date> entry : serverMap.entrySet()) {
				File serverFile = entry.getKey();
			    Date serverLastModified = entry.getValue();

			    backupLastModified = backupMap.get(generateBackupFileFromString(serverFile.getName()));
				if (backupLastModified.getTime() == 0L || serverLastModified.compareTo(backupLastModified) > 0) {
					logger.info("Server \"" + serverFile.getName() + "\" needs to be backed up.");
					serversToBackup.add(serverFile);
			    }
			}
		} else {
			logger.info("No backups were found. Backing up all servers.");
			for (File server : serverList) {
				serversToBackup.add(server);
			}
		}
		
		// Iterate through the servers that need to be backed up
		File backupFolder;
		if (serversToBackup.isEmpty()) {
			logger.info("All backups were already up-to-date.");
		} else {
			for (File serverFolder : serversToBackup) {
				logger.info("Backing up server: " + serverFolder.getName());
				backupFolder = generateBackupFileFromString(serverFolder.getName());
				try {
					backupServer(serverFolder, backupFolder);
				} catch (ZipException e) {
					// FIXME: Figure out why this catch doesn't trigger the logger and just eats the exception
					logger.log(Level.WARNING, "Server folder \"" + serverFolder.getName() + "\" contains no files, skipping backup.", e);
				}
			}
		}
		
		logger.info("Backup process complete.");
		
	}
	
	/**
	 * Attempts to load the config file into properties. If the config file doesn't exist, it will be created.
	 */
	public static void loadConfig(File configFile, Properties properties) {
		// Try to create a config file if one doesn't exist
		try {
			if (configFile.createNewFile()) {
				logger.info("Configuration file \"" + configFile.getName() + "\" created successfully.");
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to create configuration file: ", e);
		}
		
		try (InputStream in = new FileInputStream(configFile)) {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Attempts to save the properties into the config file
	 */
	public static void saveConfig(File configFile, Properties properties) {
		try (OutputStream out = new FileOutputStream(configFile)) {
			properties.store(out, null);
		} catch (IOException e){
			logger.log(Level.SEVERE, "Unable to save to configuration file: ", e);
		}
	}
	
	public static Date roundDateToSeconds(Date date) {
		long roundedDate = (date.getTime() / 1000) * 1000;
		return new Date(roundedDate);
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
