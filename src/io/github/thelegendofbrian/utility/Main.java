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
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zeroturnaround.zip.ZipException;
import org.zeroturnaround.zip.ZipUtil;
import io.github.talkarcabbage.logger.LoggerManager;

public class Main {
	
	private File configFile;
	private Properties properties;
	
	private String pathToServers;
	private String pathToBackups;
	private File serversDirectory;
	private File backupsDirectory;
	
	private File[] serverList;
	private File[] backupList;
	
	private Date lastModified;
	
	private HashMap<File, Date> serverMap = new HashMap<>();
	private HashMap<File, Date> backupMap = new HashMap<>();
	private ArrayList<File> serversToBackup = new ArrayList<>();
	
	// Defined as non-static to promote thread safety
	private final SimpleDateFormat sdfPretty = new SimpleDateFormat("MMM dd yyyy - hh:mm:ss z");
	
	// Define config property literals
	private static final String SERVERS_DIRECTORY = "serversDirectory";
	private static final String BACKUPS_DIRECTORY = "backupsDirectory";
	private static final String LOG_LEVEL = "logLevel";
	private static final String ENABLE_PRUNING = "enablePruning";
	private static final String PRUNING_THRESHOLD = "pruningThreshold";
	
	private static final String CONFIG_NAME = "config.ini";
	
	private static final Logger logger = LoggerManager.getInstance().getLogger("main");
	
	public Main() {
		sdfPretty.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	public static void main(String[] args) {
		LoggerManager.getInstance().getFormatter().setLoggerNameLevel(Level.FINE);
		
		Main instance = new Main();
		instance.runBackupUtility();
	}
	
	public void runBackupUtility() {
		configSetup();
		storeServersDirectories();
		checkDirectories();
		createBackupDirectories();
		storeEachServerLastModified();
		storeBackupsDirectories();
		determineServersToBackup();
		backupServers();
		logger.info("Backup process complete.");
	}
	
	/**
	 * Sets default config values and loads the config file.
	 */
	public void configSetup() {
		logger.fine("Reading configuration file.");
		
		Properties defaultProps = new Properties();
		defaultProps.setProperty(LOG_LEVEL, "CONFIG");
		defaultProps.setProperty(ENABLE_PRUNING, "false");
		defaultProps.setProperty(PRUNING_THRESHOLD, "60");
		
		properties = new Properties(defaultProps);
		properties.setProperty(SERVERS_DIRECTORY, "");
		properties.setProperty(BACKUPS_DIRECTORY, "");
		
		configFile = new File(CONFIG_NAME);
		
		// Load the config file into properties
		loadConfig();
		
		// Verify validity of config values
		if ("".equals(properties.getProperty(SERVERS_DIRECTORY)) || "".equals(properties.getProperty(BACKUPS_DIRECTORY))) {
			// TODO: Add GUI and explanation of how to set up for no GUI
			logger.severe("Invalid directory configuration set.");
			logger.severe( () -> "Edit config.ini and specify the " + SERVERS_DIRECTORY + " and " + BACKUPS_DIRECTORY + ".");
			crashProgram();
		}
		
		// Apply the settings from config
		LoggerManager.getInstance().setGlobalLoggingLevel(Level.parse(properties.getProperty(LOG_LEVEL)));
		logger.fine("Logging level found in config: " + properties.getProperty(LOG_LEVEL));
		
		pathToServers = properties.getProperty(SERVERS_DIRECTORY);
		serversDirectory = new File(pathToServers);
		logger.fine("Servers directory found in config: " + serversDirectory.getAbsolutePath());
		
		pathToBackups = properties.getProperty(BACKUPS_DIRECTORY);
		backupsDirectory = new File(pathToBackups);
		logger.fine("Backups directory found in config: " + backupsDirectory.getAbsolutePath());
	}
	
	/**
	 * Attempts to load the config file into {@link #properties}. If the config file doesn't exist, it will be created.
	 */
	public void loadConfig() {
		// Try to create a config file if one doesn't exist
		try {
			if (configFile.createNewFile()) {
				logger.info("Configuration file \"" + configFile.getName() + "\" created successfully.");
				saveConfig();
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to create configuration file: ", e);
			crashProgram();
		}
		
		try (InputStream in = new FileInputStream(configFile)) {
			properties.load(in);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to load configuration file: ", e);
			crashProgram();
		}
	}
	
	/**
	 * Attempts to save the properties into the config file
	 */
	public void saveConfig() {
		try (OutputStream out = new FileOutputStream(configFile)) {
			properties.store(out, null);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to save to configuration file: ", e);
			crashProgram();
		}
	}
	
	/**
	 * Stores the list of directories in the servers directory into {@link #serversList}.
	 */
	private void storeServersDirectories() {
		serverList = serversDirectory.listFiles(File::isDirectory);
	}
	
	/**
	 * Stores the list of directories in the backups directory into {@link #backupsList}.
	 */
	private void storeBackupsDirectories() {
		backupList = backupsDirectory.listFiles(File::isDirectory);
	}
	
	private void checkDirectories() {
		// Check if servers directory exists
		logger.fine("Checking for valid server file structure.");
		if (!serversDirectory.exists()) {
			logger.severe("The specified servers directory " + serversDirectory.getAbsolutePath() + " does not exist.");
			crashProgram();
		}
		
		// Make backups directory if it doesn't exist
		logger.fine("Checking for backups directory.");
		
		if (backupsDirectory.mkdir()) {
			logger.info("Backups folder \"" + backupsDirectory.getName() + "\" created successfully.");
		}
		
		if (!backupsDirectory.canWrite()) {
			logger.severe("The specified backups directory cannot be written to.");
			crashProgram();
		}
		
		if (serverList.length == 0) {
			logger.severe("The servers directory does not contain any server folders.");
			crashProgram();
		}
	}
	
	/**
	 * Creates the backup folder directory structure based on the servers in the servers directory.
	 */
	private void createBackupDirectories() {
		File serverBackupFolder;
		for (File server : serverList) {
			serverBackupFolder = new File(backupsDirectory.getAbsolutePath() + File.separator + server.getName());
			if (serverBackupFolder.mkdir()) {
				logger.info("Backup directory did not exist for \"" + server.getName() + "\". Creating directory.");
			}
		}
	}
	
	/**
	 * Finds the most recently changed file in each server directory and stores when it was last modified.
	 */
	private void storeEachServerLastModified() {
		logger.fine("Checking when each server was last modified.");
		
		for (File serverDir : serverList) {
			lastModified = roundDateToSeconds(lastModifiedInFolder(serverDir));
			logger.fine(() -> "Found server named: \"" + serverDir.getName() + "\" last modified: " + sdfPretty.format(lastModified));
			serverMap.put(serverDir, lastModified);
		}
	}
	
	/**
	 * Determines which servers need to be backed up. Gets the most recent time stamp in each backup directory. If {@link #backupList()} is empty, all servers will be marked to back up.
	 */
	public void determineServersToBackup() {
		if (backupList.length != 0) {
			for (File backupDir : backupList) {
				// If the backup directory for a server is empty, make a backup for that server
				if (backupDir.list().length == 0) {
					logger.fine("Backup directory for server \"" + backupDir.getName() + "\" is empty. A backup will be made.");
					backupMap.put(backupDir, new Date(0L));
				} else {
					lastModified = roundDateToSeconds(getBackupTimeStamp(getLatestBackup(backupDir)));
					logger.fine(() -> "Found most recent backup for server: \"" + backupDir.getName() + "\" last modified: " + sdfPretty.format(lastModified));
					backupMap.put(backupDir, lastModified);
				}
			}
			
			// Check which servers have been modified since the last backup
			logger.fine("Checking which servers need to be backed up.");
			Date backupLastModified;
			for (Map.Entry<File, Date> entry : serverMap.entrySet()) {
				File serverFile = entry.getKey();
				Date serverLastModified = entry.getValue();
				
				backupLastModified = backupMap.get(generateBackupFileFromString(serverFile.getName(), pathToBackups));
				if (backupLastModified.getTime() == 0L || serverLastModified.compareTo(backupLastModified) > 0) {
					logger.fine("Server \"" + serverFile.getName() + "\" needs to be backed up.");
					serversToBackup.add(serverFile);
				}
			}
		} else {
			logger.fine("No backups were found. All servers will be backed up.");
			for (File server : serverList) {
				serversToBackup.add(server);
			}
		}
	}
	
	/**
	 * Iterates through the servers that need to be backed up.
	 */
	private void backupServers() {
		if (serversToBackup.isEmpty()) {
			logger.info("All backups were already up-to-date.");
		} else {
			for (File serverFolder : serversToBackup) {
				logger.info("Backing up server: " + serverFolder.getName());
				
				File backupFolder;
				backupFolder = generateBackupFileFromString(serverFolder.getName(), pathToBackups);
				try {
					backupSpecificServer(serverFolder, backupFolder);
				} catch (ZipException e) {
					logger.log(Level.WARNING, "Server folder \"" + serverFolder.getName() + "\" contains no files: ", e);
				}
			}
		}
	}
	
	/**
	 * Rounds the Date object to the nearest second.
	 * 
	 * @param date
	 * @return
	 */
	public static Date roundDateToSeconds(Date date) {
		long roundedDate = (date.getTime() / 1000) * 1000;
		return new Date(roundedDate);
	}
	
	/**
	 * Generate the corresponding backup File location given the name of the server folder.
	 * 
	 * @param fileName
	 * @return
	 */
	public static File generateBackupFileFromString(String fileName, String pathToBackups) {
		return new File(pathToBackups, fileName);
	}
	
	/**
	 * Recursively finds the Date of the most recently changed file in a directory.
	 * 
	 * @return
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
			crashProgram();
		}
		
		return new Date(mostRecentTime.getValue());
	}
	
	/**
	 * Gets the time stamp of the backup archive with the most recent time stamp in its filename.
	 */
	public static File getLatestBackup(File singleBackupDirectory) {
		// Get a list of all the files in the directory
		File[] backupList = singleBackupDirectory.listFiles(File::isFile);
		
		// Check which filename contains the most recent time stamp
		Arrays.sort(backupList);
		
		return backupList[backupList.length - 1];
	}
	
	/**
	 * Returns a Date corresponding to the time stamp in a backup archive's file name.
	 * 
	 * @param backupFile
	 * @return
	 */
	public static Date getBackupTimeStamp(File backupFile) {
		String nameOfFile = backupFile.getName();
		
		// Remove file extension
		if (nameOfFile.indexOf('.') > 0) { // NOSONAR: This is needed to handle files starting with '.'
			nameOfFile = nameOfFile.substring(0, nameOfFile.lastIndexOf('.'));
		}
		
		// Remove backup name
		nameOfFile = nameOfFile.substring(nameOfFile.length() - 19);
		
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").parse(nameOfFile);
		} catch (ParseException e) {
			logger.log(Level.SEVERE, "Unable to parse date format of backup archive: ", e);
			crashProgram();
		}
		
		return date;
	}
	
	/**
	 * Zips the contents of the serverFolder directory into the backupFolder directory. Appends a time stamp to the end
	 * of the zip file name indicating when the server was last modified.
	 * 
	 * @param serverFolder
	 * @param backupFolder
	 */
	public void backupSpecificServer(File serverFolder, File backupFolder) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String zipFile = backupFolder.getAbsolutePath() + File.separator + serverFolder.getName() + "_" + sdf.format(serverMap.get(serverFolder)) + ".zip";
		
		ZipUtil.pack(serverFolder, new File(zipFile));
	}
	
	/**
	 * Returns a severe log message and exits the program.
	 */
	public static void crashProgram() {
		logger.severe("The program has encountered a problem and must stop.");
		System.exit(-1);
	}
	
}
