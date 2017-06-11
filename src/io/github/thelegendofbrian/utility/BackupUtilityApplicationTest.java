package io.github.thelegendofbrian.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import io.github.talkarcabbage.logger.LoggerManager;

public class BackupUtilityApplicationTest {
	
	private static final Logger logger = LoggerManager.getInstance().getLogger("test");
	
	private static File tempDirectory = new File("temp");
	
	@Before
	public void setUp() {
		// In case the temp directory already exists, delete it
		if (tempDirectory.exists()) {
			deleteFileOrFolder(tempDirectory);
		}
		
		// Create a temp directory
		if (!tempDirectory.mkdir()) {
			logger.severe("Unable to create a new temporary directory to test in.");
			fail();
		}
		
	}
	
	@After
	public void tearDown() {
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
	
	/*
	 * roundDateToSeconds()
	 */
	
	@Test
	public void testRoundDateToSeconds1() {
		long unroundedMillis = 1495407727000L;
		Date unroundedDate = new Date(unroundedMillis);
		Date roundedDate = BackupUtilityApplication.roundDateToSeconds(unroundedDate);
		long roundedMillis = roundedDate.getTime();
		assertEquals(1495407727000L, roundedMillis);
	}
	
	@Test
	public void testRoundDateToSeconds2() {
		long unroundedMillis = 1495407727034L;
		Date unroundedDate = new Date(unroundedMillis);
		Date roundedDate = BackupUtilityApplication.roundDateToSeconds(unroundedDate);
		long roundedMillis = roundedDate.getTime();
		assertEquals(1495407727000L, roundedMillis);
	}
	
	@Test
	public void testRoundDateToSeconds3() {
		long unroundedMillis = 1495407727067L;
		Date unroundedDate = new Date(unroundedMillis);
		Date roundedDate = BackupUtilityApplication.roundDateToSeconds(unroundedDate);
		long roundedMillis = roundedDate.getTime();
		assertEquals(1495407727000L, roundedMillis);
	}
	
	@Test
	public void testRoundDateToSeconds4() {
		long unroundedMillis = 1495407727167L;
		Date unroundedDate = new Date(unroundedMillis);
		Date roundedDate = BackupUtilityApplication.roundDateToSeconds(unroundedDate);
		long roundedMillis = roundedDate.getTime();
		assertEquals(1495407727000L, roundedMillis);
	}
	
	@Test
	public void testRoundDateToSeconds5() {
		long unroundedMillis = 0L;
		Date unroundedDate = new Date(unroundedMillis);
		Date roundedDate = BackupUtilityApplication.roundDateToSeconds(unroundedDate);
		long roundedMillis = roundedDate.getTime();
		assertEquals(0L, roundedMillis);
	}
	
	@Ignore
	@Test
	public void testConfigSetup() {
		fail("Not yet implemented"); // NOSONAR
	}
	
	@Ignore
	@Test
	public void testLoadConfig() {
		fail("Not yet implemented"); // NOSONAR
	}
	
	@Ignore
	@Test
	public void testSaveConfig() {
		fail("Not yet implemented"); // NOSONAR
	}
	
	@Test
	public void testLastModifiedInFolder() throws IOException {
		long timeMin = 0L;
		long time1 = 420L;
		long time2 = 1337L;
		long time3 = 9000L;
		long time4 = 9001L;
		long time5 = 696969L;
		long timeMax = Long.MAX_VALUE;
		
		/*
		 * Directory 1
		 */
		
		File testServerDirectory1 = new File(tempDirectory, "server1");
		testServerDirectory1.mkdir();
		
		File testServerDirectory1File1 = new File(testServerDirectory1, "file1");
		File testServerDirectory1File2 = new File(testServerDirectory1, "file2");
		File testServerDirectory1File3 = new File(testServerDirectory1, "file3");
		File testServerDirectory1File4 = new File(testServerDirectory1, "file4");
		File testServerDirectory1File5 = new File(testServerDirectory1, "file5");
		
		testServerDirectory1File1.createNewFile();
		testServerDirectory1File2.createNewFile();
		testServerDirectory1File3.createNewFile();
		testServerDirectory1File4.createNewFile();
		testServerDirectory1File5.createNewFile();
		
		// Tests 1 duplicate date modified where the duplicate date is not the most current
		testServerDirectory1.setLastModified(timeMin);
		testServerDirectory1File1.setLastModified(timeMin);
		testServerDirectory1File2.setLastModified(time1);
		testServerDirectory1File3.setLastModified(time2);
		testServerDirectory1File4.setLastModified(time2);
		testServerDirectory1File5.setLastModified(time3);
		
		/*
		 * Directory 2
		 */

		File testServerDirectory2 = new File(tempDirectory, "server2");
		testServerDirectory2.mkdir();
		
		File testServerDirectory2File1 = new File(testServerDirectory2, "file1");
		File testServerDirectory2File2 = new File(testServerDirectory2, "file2");
		File testServerDirectory2File3 = new File(testServerDirectory2, "file3");
		File testServerDirectory2File4 = new File(testServerDirectory2, "file4");
		File testServerDirectory2File5 = new File(testServerDirectory2, "file5");
		
		testServerDirectory2File1.createNewFile();
		testServerDirectory2File2.createNewFile();
		testServerDirectory2File3.createNewFile();
		testServerDirectory2File4.createNewFile();
		testServerDirectory2File5.createNewFile();
		
		// Tests 1 duplicate date modified where the duplicate date is the most current
		testServerDirectory2.setLastModified(timeMin);
		testServerDirectory2File1.setLastModified(timeMin);
		testServerDirectory2File2.setLastModified(time1);
		testServerDirectory2File3.setLastModified(time2);
		testServerDirectory2File4.setLastModified(time3);
		testServerDirectory2File5.setLastModified(time3);
		
		/*
		 * Directory 3
		 */

		File testServerDirectory3 = new File(tempDirectory, "server3");
		File testServerDirectory3SubDirectory1 = new File(testServerDirectory3, "subdirectory");
		testServerDirectory3.mkdir();
		testServerDirectory3SubDirectory1.mkdir();
		
		File testServerDirectory3File1 = new File(testServerDirectory3, "file1");
		File testServerDirectory3File2 = new File(testServerDirectory3, "file2");
		File testServerDirectory3File3 = new File(testServerDirectory3, "file3");
		File testServerDirectory3File4 = new File(testServerDirectory3SubDirectory1, "file4");
		File testServerDirectory3File5 = new File(testServerDirectory3SubDirectory1, "file5");
		
		testServerDirectory3File1.createNewFile();
		testServerDirectory3File2.createNewFile();
		testServerDirectory3File3.createNewFile();
		testServerDirectory3File4.createNewFile();
		testServerDirectory3File5.createNewFile();
		
		// Tests non-chronologically set date modified with the most recent time in a subdirectory
		testServerDirectory3.setLastModified(timeMin);
		testServerDirectory3File1.setLastModified(time4);
		testServerDirectory3File2.setLastModified(time2);
		testServerDirectory3File3.setLastModified(time1);
		testServerDirectory3SubDirectory1.setLastModified(timeMin);
		testServerDirectory3File4.setLastModified(time5);
		testServerDirectory3File5.setLastModified(time1);
		
		/*
		 * Compare actual and expected dates
		 */
		
		Date actual1 = BackupUtilityApplication.lastModifiedInFolder(testServerDirectory1);
		Date actual2 = BackupUtilityApplication.lastModifiedInFolder(testServerDirectory2);
		Date actual3 = BackupUtilityApplication.lastModifiedInFolder(testServerDirectory3);
		
		Date expected1 = new Date(time3);
		Date expected2 = new Date(time3);
		Date expected3 = new Date(time5);
		
		assertTrue(expected1.equals(actual1));
		assertTrue(expected2.equals(actual2));		
		assertTrue(expected3.equals(actual3));
	}
	
	@Ignore
	@Test
	public void testDetermineServersToBackup() {
		fail("Not yet implemented"); // NOSONAR
	}
	
	/*
	 * getLatestBackup()
	 */
	
	@Test
	public void testGetLatestBackup1() throws IOException {
		ArrayList<String> backupNames = new ArrayList<>();
		backupNames.add("Backup_2000-01-01_00-00-00.zip"); // NOSONAR
		backupNames.add("Backup_2001-01-01_00-00-00.zip");
		backupNames.add("Backup_2002-01-01_00-00-00.zip");
		backupNames.add("Backup_2003-01-01_00-00-00.zip");
		
		for (String zipName : backupNames) {
			makeFileInTempDirectory(zipName);
		}
		
		File actual = BackupUtilityApplication.getLatestBackup(tempDirectory);
		File expected = new File(tempDirectory, "Backup_2003-01-01_00-00-00.zip");
		
		assertTrue(expected.equals(actual));
	}
	
	@Test
	public void testGetLatestBackup2() throws IOException {
		ArrayList<String> backupNames = new ArrayList<>();
		backupNames.add("Backup_2000-01-01_00-00-00.zip"); // NOSONAR
		backupNames.add("Backup_2000-02-01_00-00-00.zip");
		backupNames.add("Backup_2000-03-01_00-00-00.zip");
		backupNames.add("Backup_2000-04-01_00-00-00.zip");
		
		for (String zipName : backupNames) {
			makeFileInTempDirectory(zipName);
		}
		
		File actual = BackupUtilityApplication.getLatestBackup(tempDirectory);
		File expected = new File(tempDirectory, "Backup_2000-04-01_00-00-00.zip");
		
		assertTrue(expected.equals(actual));
	}
	
	@Test
	public void testGetLatestBackup3() throws IOException {
		ArrayList<String> backupNames = new ArrayList<>();
		backupNames.add("Backup_2000-01-01_00-00-00.zip"); // NOSONAR
		backupNames.add("Backup_2000-01-02_00-00-00.zip");
		backupNames.add("Backup_2000-01-03_00-00-00.zip");
		backupNames.add("Backup_2000-01-04_00-00-00.zip");
		
		for (String zipName : backupNames) {
			makeFileInTempDirectory(zipName);
		}
		
		File actual = BackupUtilityApplication.getLatestBackup(tempDirectory);
		File expected = new File(tempDirectory, "Backup_2000-01-04_00-00-00.zip");
		
		assertTrue(expected.equals(actual));
	}
	
	@Test
	public void testGetLatestBackup4() throws IOException {
		ArrayList<String> backupNames = new ArrayList<>();
		backupNames.add("Backup_2000-01-01_00-00-00.zip"); // NOSONAR
		backupNames.add("Backup_2000-01-01_01-00-00.zip");
		backupNames.add("Backup_2000-01-01_02-00-00.zip");
		backupNames.add("Backup_2000-01-01_03-00-00.zip");
		
		for (String zipName : backupNames) {
			makeFileInTempDirectory(zipName);
		}
		
		File actual = BackupUtilityApplication.getLatestBackup(tempDirectory);
		File expected = new File(tempDirectory, "Backup_2000-01-01_03-00-00.zip");
		
		assertTrue(expected.equals(actual));
	}
	
	@Test
	public void testGetLatestBackup5() throws IOException {
		ArrayList<String> backupNames = new ArrayList<>();
		backupNames.add("Backup_2000-01-01_00-00-00.zip"); // NOSONAR
		backupNames.add("Backup_2000-01-01_00-01-00.zip");
		backupNames.add("Backup_2000-01-01_00-02-00.zip");
		backupNames.add("Backup_2000-01-01_00-03-00.zip");
		
		for (String zipName : backupNames) {
			makeFileInTempDirectory(zipName);
		}
		
		File actual = BackupUtilityApplication.getLatestBackup(tempDirectory);
		File expected = new File(tempDirectory, "Backup_2000-01-01_00-03-00.zip");
		
		assertTrue(expected.equals(actual));
	}
	
	@Test
	public void testGetLatestBackup6() throws IOException {
		ArrayList<String> backupNames = new ArrayList<>();
		backupNames.add("Backup_2000-01-01_00-00-00.zip"); // NOSONAR
		backupNames.add("Backup_2000-01-01_00-00-01.zip");
		backupNames.add("Backup_2000-01-01_00-00-02.zip");
		backupNames.add("Backup_2000-01-01_00-00-03.zip");
		
		for (String zipName : backupNames) {
			makeFileInTempDirectory(zipName);
		}
		
		File actual = BackupUtilityApplication.getLatestBackup(tempDirectory);
		File expected = new File(tempDirectory, "Backup_2000-01-01_00-00-03.zip");
		
		assertTrue(expected.equals(actual));
	}
	
	private void makeFileInTempDirectory(String fileName) throws IOException {
		File file = new File(tempDirectory, fileName);
		if (!file.createNewFile()) {
			logger.severe("File " + file.getName() + " already exists.");
			fail();
		}
	}
	
	/*
	 * getBackupTimeStamp()
	 */
	
	@Test
	public void testGetBackupTimeStamp1() {
		File file = new File(tempDirectory, "Backup_2017-06-01_09-28-49.zip");
		
		Date actual = BackupUtilityApplication.getBackupTimeStamp(file);
		Date expected = new GregorianCalendar(2017, 6 - 1, 1, 9, 28, 49).getTime();
		
		assertTrue(expected.equals(actual));
	}
	
	@Test
	public void testGetBackupTimeStamp2() {
		File file = new File(tempDirectory, "Backup_2016-11-21_12-13-00.zip");
		
		Date actual = BackupUtilityApplication.getBackupTimeStamp(file);
		Date expected = new GregorianCalendar(2016, 11 - 1, 21, 12, 13, 00).getTime();
		
		assertTrue(expected.equals(actual));
	}
	
	@Ignore
	@Test
	public void testBackupSpecificServer() {
		fail("Not yet implemented"); // NOSONAR
	}
	
	/*
	 * runBackupUtility()
	 */

	@Ignore
	@Test
	public void testRunBackupUtility() {
		fail("Not yet implemented"); // NOSONAR
	}
	
}
