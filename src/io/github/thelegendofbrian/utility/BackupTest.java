package io.github.thelegendofbrian.utility;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import io.github.talkarcabbage.logger.LoggerManager;

public class BackupTest {
	
	private static final Logger logger = LoggerManager.getInstance().getLogger("backupTest");
	
	private static File tempDirectory = new File("temp");
	
	private static File testBackupDirectory1;
	private static File testBackupDirectory2;
	private static File testBackupDirectory3;
	
	@Before
	public void setUp() throws Exception {
		// In case the temp directory already exists, delete it
		if (tempDirectory.exists()) {
			deleteFileOrFolder(tempDirectory);
		}
		
		// Create a temp directory
		if (!tempDirectory.mkdir()) {
			logger.severe("Unable to create a new temporary directory to test in.");
			fail();
		}
		
		/*
		 * Directory 1
		 */
		
		testBackupDirectory1 = new File(tempDirectory, "backup1");
		testBackupDirectory1.mkdir();
		
		File testBackupDirectory1File1 = new File(testBackupDirectory1, "backup1_2000-01-01_00-00-00.zip");
		File testBackupDirectory1File2 = new File(testBackupDirectory1, "backup1_2000-01-02_00-00-00.zip");
		File testBackupDirectory1File3 = new File(testBackupDirectory1, "backup1_2000-01-03_00-00-00.zip");
		File testBackupDirectory1File4 = new File(testBackupDirectory1, "backup1_2000-01-04_00-00-00.zip");
		File testBackupDirectory1File5 = new File(testBackupDirectory1, "backup1_2000-01-05_00-00-00.zip");
		
		testBackupDirectory1File1.createNewFile();
		testBackupDirectory1File2.createNewFile();
		testBackupDirectory1File3.createNewFile();
		testBackupDirectory1File4.createNewFile();
		testBackupDirectory1File5.createNewFile();
		
		/*
		 * Directory 2
		 */

		testBackupDirectory2 = new File(tempDirectory, "backup2");
		testBackupDirectory2.mkdir();
		
		File testBackupDirectory2File1 = new File(testBackupDirectory2, "backup2_2000-01-01_00-00-00.zip");
		File testBackupDirectory2File2 = new File(testBackupDirectory2, "backup2_2000-01-02_00-00-00.zip");
		File testBackupDirectory2File3 = new File(testBackupDirectory2, "backup2_2000-01-03_00-00-00.zip");
		File testBackupDirectory2File4 = new File(testBackupDirectory2, "backup2_2000-01-04_00-00-00.zip");
		File testBackupDirectory2File5 = new File(testBackupDirectory2, "doesnotbelong_2000-01-05_00-00-00.zip");
		
		testBackupDirectory2File1.createNewFile();
		testBackupDirectory2File2.createNewFile();
		testBackupDirectory2File3.createNewFile();
		testBackupDirectory2File4.createNewFile();
		testBackupDirectory2File5.createNewFile();
		
		/*
		 * Directory 3
		 */

		testBackupDirectory3 = new File(tempDirectory, "backup3 with spaces");
		testBackupDirectory3.mkdir();
		
		File testBackupDirectory3File1 = new File(testBackupDirectory3, "backup3 with spaces_2000-01-01_00-00-00.zip");
		File testBackupDirectory3File2 = new File(testBackupDirectory3, "backup3 with spaces_2000-01-02_00-00-00.zip");
		File testBackupDirectory3File3 = new File(testBackupDirectory3, "backup3 with spaces_2000-01-03_00-00-00.zip");
		File testBackupDirectory3File4 = new File(testBackupDirectory3, "backup3 with spaces_2000-01-04_00-00-00.zip");
		File testBackupDirectory3File5 = new File(testBackupDirectory3, "backup3 with spaces_2000-01-05_00-00-00.zip");
		
		testBackupDirectory3File1.createNewFile();
		testBackupDirectory3File2.createNewFile();
		testBackupDirectory3File3.createNewFile();
		testBackupDirectory3File4.createNewFile();
		testBackupDirectory3File5.createNewFile();
	}
	
	@After
	public void tearDown() throws Exception {
		// Delete the temp directory
		deleteFileOrFolder(tempDirectory);
	}
	
	private static void deleteFileOrFolder(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles(File::exists)) {
				deleteFileOrFolder(f);
			}
		}
		if (!file.delete()) {
			logger.severe("Unable to delete file: " + file.getName());
			fail();
		}
	}
	
	@Ignore
	@Test
	public void testGetMostRecentBackupDate() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testGetBackupTimeStamp1() throws ParseException {
		File file = new File(tempDirectory, "Backup_2017-06-01_09-28-49.zip");
		
		Date actual = BackupUtilityApplication.Backup.getBackupTimeStamp(file);
		Date expected = new GregorianCalendar(2017, 6 - 1, 1, 9, 28, 49).getTime();
		
		assertTrue(expected.equals(actual));
	}
	
	@Test
	public void testGetBackupTimeStamp2() throws ParseException {
		File file = new File(tempDirectory, "Backup_2016-11-21_12-13-00.zip");
		
		Date actual = BackupUtilityApplication.Backup.getBackupTimeStamp(file);
		Date expected = new GregorianCalendar(2016, 11 - 1, 21, 12, 13, 00).getTime();
		
		assertTrue(expected.equals(actual));
	}
	
	@Test
	public void testGetLatestBackup() throws IOException {
		BackupUtilityApplication.Backup backup1 = new BackupUtilityApplication.Backup(testBackupDirectory1);
		BackupUtilityApplication.Backup backup2 = new BackupUtilityApplication.Backup(testBackupDirectory2);
		BackupUtilityApplication.Backup backup3 = new BackupUtilityApplication.Backup(testBackupDirectory3);
		
		File actual1 = backup1.getLatestBackupFile();
		File expected1 = new File(testBackupDirectory1, "backup1_2000-01-05_00-00-00.zip");
		
		File actual2 = backup2.getLatestBackupFile();
		File expected2 = new File(testBackupDirectory2, "backup2_2000-01-04_00-00-00.zip");
		
		File actual3 = backup3.getLatestBackupFile();
		File expected3 = new File(testBackupDirectory3, "backup3 with spaces_2000-01-05_00-00-00.zip");
		
		assertTrue(expected1.equals(actual1));
//		assertTrue(expected2.equals(actual2)); // TODO: Add support for this test
		assertTrue(expected3.equals(actual3));
	}
	
}
