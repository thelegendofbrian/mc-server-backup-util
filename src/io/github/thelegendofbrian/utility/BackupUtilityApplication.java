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

public class BackupUtilityApplication {
	
	private static final Logger logger = LoggerManager.getInstance().getLogger("main");
	
	// Define config property literals
	private static final String SERVERS_DIRECTORY = "serversDirectory";
	private static final String BACKUPS_DIRECTORY = "backupsDirectory";
	private static final String LOG_LEVEL = "logLevel";
	private static final String ENABLE_PRUNING = "enablePruning";
	private static final String PRUNING_THRESHOLD = "pruningThreshold";
	
	private static final File configFile = new File("config.ini");
	
	public static void main(String[] args) {
		LoggerManager.getInstance().getFormatter().setLoggerNameLevel(Level.FINE);
		
		// Load in properties from the config file
		final Properties properties = getPropertiesFromFile();
		
		// Apply the settings from properties
		LoggerManager.getInstance().setGlobalLoggingLevel(Level.parse(properties.getProperty(LOG_LEVEL)));
		logger.fine("Logging level found in config: " + properties.getProperty(LOG_LEVEL));
		
		final File serversDirectory = new File(properties.getProperty(SERVERS_DIRECTORY));
		logger.fine("Servers directory found in config: " + serversDirectory.getAbsolutePath());
		
		final File backupsDirectory = new File(properties.getProperty(BACKUPS_DIRECTORY));
		logger.fine("Backups directory found in config: " + backupsDirectory.getAbsolutePath());
		
		// Make the backups directory if needed and check that it is writable
		checkMainBackupsDirectory(backupsDirectory);

		// Parse the existing servers and backups
		final HashMap<Server, Backup> serverAndBackupMap = getServerAndBackupList(serversDirectory, backupsDirectory);
		
		// Figure out which backups are out of date
		final ArrayList<Server> serversToBackup = getServersToBackup(serverAndBackupMap);
		
		// Back up the appropriate servers
		backupServers(serverAndBackupMap, serversToBackup);
		
		logger.info("Backup process complete.");
	}
	
	/**
	 * Sets default config values and loads the config file.
	 */
	static final Properties getPropertiesFromFile() {
		logger.fine("Reading configuration file.");
		
		final Properties defaultProps = new Properties();
		defaultProps.setProperty(LOG_LEVEL, "CONFIG");
		defaultProps.setProperty(ENABLE_PRUNING, "false");
		defaultProps.setProperty(PRUNING_THRESHOLD, "60");
		
		Properties properties = new Properties(defaultProps);
		properties.setProperty(SERVERS_DIRECTORY, "");
		properties.setProperty(BACKUPS_DIRECTORY, "");
		
		// Load the config file into properties
		loadConfig(properties);
		
		// Verify validity of config values
		if ("".equals(properties.getProperty(SERVERS_DIRECTORY)) || "".equals(properties.getProperty(BACKUPS_DIRECTORY))) {
			// TODO: Add GUI and explanation of how to set up for no GUI
			logger.severe("Invalid directory configuration set.");
			logger.severe(() -> "Edit " + configFile.getName() + " and specify the " + SERVERS_DIRECTORY + " and " + BACKUPS_DIRECTORY + ".");
			crashProgram();
		}
		
		return properties;
	}
	
	/**
	 * Attempts to load the config file into the passed properties parameter. If the config file doesn't exist, it will be created.
	 */
	static final void loadConfig(Properties properties) {
		// Try to create a config file if one doesn't exist
		try {
			if (configFile.createNewFile()) {
				logger.info("Configuration file \"" + configFile.getName() + "\" created successfully.");
				saveConfig(properties);
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
	static final void saveConfig(Properties properties) {
		try (OutputStream out = new FileOutputStream(configFile)) {
			properties.store(out, null);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to save to configuration file: ", e);
			crashProgram();
		}
	}
	
	static final ArrayList<Server> getServerList(File serversDirectory) {
		// Check if servers directory exists
		logger.fine("Checking for valid server file structure.");
		if (!serversDirectory.exists()) {
			logger.severe("The specified servers directory " + serversDirectory.getAbsolutePath() + " does not exist.");
			crashProgram();
		}
		
		// Store the list of directories in the servers directory
		final File[] serverFolders = serversDirectory.listFiles(File::isDirectory);
		
		ArrayList<Server> serverList = new ArrayList<>();
		for (File server : serverFolders) {
			serverList.add(new Server(server));
		}
		
		if (serverList.isEmpty()) {
			logger.severe("The servers directory does not contain any server folders.");
			crashProgram();
		}
		
		return serverList;
	}
	
	/**
	 * Verifies the usability of a backups directory.
	 * @param serverList
	 * @param backupsDirectory
	 */
	private static final void checkMainBackupsDirectory(File backupsDirectory) {
		// Make backups directory if it doesn't exist
		logger.fine("Checking for backups directory.");
		
		if (backupsDirectory.mkdir()) {
			logger.info("Backups folder \"" + backupsDirectory.getName() + "\" created successfully.");
		}
		
		if (!backupsDirectory.canWrite()) {
			logger.severe("The specified backups directory cannot be written to.");
			crashProgram();
		}
	}
	
	static final HashMap<Server, Backup> getServerAndBackupList(File serversDirectory, File backupsDirectory) {
		final ArrayList<Server> serverList = getServerList(serversDirectory);
		HashMap<Server, Backup> serverAndBackupList = new HashMap<>();
		
		// Make a Backup object for each Server object
		for (Server server : serverList) {
			Backup backup = server.generateCorrespondingBackup(backupsDirectory);
			serverAndBackupList.put(server, backup);
		}
		return serverAndBackupList;
	}
	
	/**
	 * Returns which servers need to be backed up.
	 */
	static final ArrayList<Server> getServersToBackup(HashMap<Server, Backup> serverAndBackupMap) {
		ArrayList<Server> serversToBackup = new ArrayList<>();
		for (Map.Entry<Server, Backup> entry : serverAndBackupMap.entrySet()) {
			Server server = entry.getKey();
			Backup backup = entry.getValue();
			try {
				if (server.isBackupRequired(backup)) {
					serversToBackup.add(server);
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Unable to read file(s) in server folder while scanning for modification dates: ", e);
				crashProgram();
			} catch (ParseException e) {
				logger.log(Level.SEVERE, "Unable to parse backup archive file name as a Date: ", e);
				crashProgram();
			}
		}
		return serversToBackup;
	}
	
	static final void backupServers(HashMap<Server, Backup> serverAndBackupMap, ArrayList<Server> serversToBackup) {
		if (serversToBackup.isEmpty()) {
			logger.info("All backups were already up-to-date.");
		} else {
			for (Server server : serversToBackup) {
				logger.info("Backing up server: " + server.getName());
				
				// If the backup folder doesn't exist for this server, make it
				if (serverAndBackupMap.get(server).getFile().mkdir()) {
					logger.info("Backup directory did not exist for \"" + server.getName() + "\". Creating directory.");
				}
				
				try {
					backupSpecificServer(server, serverAndBackupMap);
				} catch (ZipException e) {
					logger.log(Level.WARNING, "Server folder \"" + server.getName() + "\" contains no files: ", e);
				} catch (IOException e) {
					logger.log(Level.WARNING, "Unable to read file(s) in server folder while scanning for modification dates: ", e);
				}
			}
		}
	}
	
	/**
	 * Zips the contents of the server directory into the corresponding backup directory. Appends a time stamp to the end
	 * of the zip file name indicating when the server was last modified.
	 * @param serverFolder
	 * @param backupFolder
	 * @throws IOException 
	 */
	static final void backupSpecificServer(Server server, HashMap<Server, Backup> serverAndBackupMap) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String zipFile = serverAndBackupMap.get(server).getFile().getAbsolutePath() + File.separator + server.getName() + "_" + sdf.format(server.getLastModifiedFileDate()) + ".zip";
		
		ZipUtil.pack(server.getFile(), new File(zipFile));
	}
	
	/**
	 * Returns a severe log message and exits the program.
	 */
	private static final void crashProgram() {
		logger.severe("The program has encountered a problem and must stop.");
		System.exit(-1);
	}
	
	/**
	 * Rounds the Date object to the nearest second.
	 * @param date
	 * @return
	 */
	static Date roundDateToSeconds(Date date) {
		long roundedDate = (date.getTime() / 1000) * 1000;
		return new Date(roundedDate);
	}
	
	static final class Server {
		
		private final String name;
		private final File folderFile;
		
		public Server(File folderFile) {
			this.folderFile = folderFile;
			this.name = folderFile.getName();
		}
		
		public final boolean isBackupRequired(Backup backup) throws IOException, ParseException {
			return backup.getFile().exists() ? getLastModifiedFileDate().after(backup.getMostRecentBackupDate()) : true;
		}
		
		public final Backup generateCorrespondingBackup(File backupsDirectory) {
			return new Backup(new File(backupsDirectory.getAbsolutePath() + File.separator + this.getName()));
		}
		
		public final Date getLastModifiedFileDate() throws IOException {
			return roundDateToSeconds(lastModifiedInFolder(folderFile));
		}
		
		/**
		 * Recursively finds the Date of the most recently changed file or folder in a directory.
		 * @return
		 * @throws IOException
		 */
		public final Date lastModifiedInFolder(File file) throws IOException {
			Wrapper mostRecentTime = new Wrapper();
			Files.find(file.toPath(), Integer.MAX_VALUE, (filePath, fileAttr) -> true)
					.forEach(x -> {
						if (mostRecentTime.getValue() < x.toFile().lastModified())
							mostRecentTime.setValue(x.toFile().lastModified());
					});
			
			return new Date(mostRecentTime.getValue());
		}
		
		public File getFile() {
			return folderFile;
		}
		
		public final String getName() {
			return name;
		}
		
		@Override
		public final String toString() {
			return name;
		}
		
		private class Wrapper {
			
			private long value;
			
			public Wrapper() {
				this.value = 0;
			}
			
			public long getValue() {
				return value;
			}
			
			public void setValue(long value) {
				this.value = value;
			}
		}
		
	}
	
	static final class Backup {
		
		private final String name;
		private final File folderFile;
		
		public Backup(File folderFile) {
			this.folderFile = folderFile;
			this.name = folderFile.getName();
		}
		
		/**
		 * Returns the Date in the file name of the most recent backup archive, rounded to the nearest second.
		 * @return
		 * @throws ParseException
		 */
		public final Date getMostRecentBackupDate() throws ParseException {
			return roundDateToSeconds(getBackupTimeStamp(getLatestBackupFile()));
		}
		
		/**
		 * Returns the Date in the file name of a backup archive.
		 * @param backupFile
		 * @return
		 * @throws ParseException
		 */
		static final Date getBackupTimeStamp(File backupFile) throws ParseException {
			String nameOfFile = backupFile.getName();
			
			// Remove file extension
			if (nameOfFile.indexOf('.') > 0) { // NOSONAR: This is needed to handle files starting with '.'
				nameOfFile = nameOfFile.substring(0, nameOfFile.lastIndexOf('.'));
			}
			
			// Remove backup name
			nameOfFile = nameOfFile.substring(nameOfFile.length() - 19);
			
			return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").parse(nameOfFile);
		}
		
		/**
		 * Returns the backup archive File with the most recent time stamp in its filename.
		 */
		final File getLatestBackupFile() {
			// Get a list of all the files in the directory
			File[] fullBackupList = folderFile.listFiles(File::isFile);
			
			// Check which filename contains the most recent time stamp
			Arrays.sort(fullBackupList);
			
			// TODO: Add check to make sure the file name format is correct
			
			return fullBackupList[fullBackupList.length - 1];
		}
		
		public File getFile() {
			return folderFile;
		}
		
		public final String getName() {
			return name;
		}
		
		@Override
		public final String toString() {
			return name;
		}
		
	}
	
}
