package io.github.thelegendofbrian.utility;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import io.github.talkarcabbage.logger.LoggerManager;

public class ServerTest {
	
	private static final Logger logger = LoggerManager.getInstance().getLogger("serverTest");
	
	private static File tempDirectory = new File("temp");
	
	private static File testServerDirectory1;
	private static File testServerDirectory2;
	private static File testServerDirectory3;
	
	private static final long TIME_MIN = 0L;
	private static final long TIME_1 = 420L;
	private static final long TIME_2 = 1337L;
	private static final long TIME_3 = 9000L;
	private static final long TIME_4 = 9001L;
	private static final long TIME_5 = 696969L;
	
	private static final long FLOORED_TIME_MIN = 0L;
	private static final long FLOORED_TIME_1 = 0L;
	private static final long FLOORED_TIME_2 = 1000L;
	private static final long FLOORED_TIME_3 = 9000L;
	private static final long FLOORED_TIME_4 = 9000L;
	private static final long FLOORED_TIME_5 = 696000L;
	
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
		
		testServerDirectory1 = new File(tempDirectory, "server1");
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
		testServerDirectory1.setLastModified(TIME_MIN);
		testServerDirectory1File1.setLastModified(TIME_MIN);
		testServerDirectory1File2.setLastModified(TIME_1);
		testServerDirectory1File3.setLastModified(TIME_2);
		testServerDirectory1File4.setLastModified(TIME_2);
		testServerDirectory1File5.setLastModified(TIME_3);
		
		/*
		 * Directory 2
		 */

		testServerDirectory2 = new File(tempDirectory, "server2");
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
		testServerDirectory2.setLastModified(TIME_MIN);
		testServerDirectory2File1.setLastModified(TIME_MIN);
		testServerDirectory2File2.setLastModified(TIME_1);
		testServerDirectory2File3.setLastModified(TIME_2);
		testServerDirectory2File4.setLastModified(TIME_3);
		testServerDirectory2File5.setLastModified(TIME_3);
		
		/*
		 * Directory 3
		 */

		testServerDirectory3 = new File(tempDirectory, "server3");
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
		testServerDirectory3.setLastModified(TIME_MIN);
		testServerDirectory3File1.setLastModified(TIME_4);
		testServerDirectory3File2.setLastModified(TIME_2);
		testServerDirectory3File3.setLastModified(TIME_1);
		testServerDirectory3SubDirectory1.setLastModified(TIME_MIN);
		testServerDirectory3File4.setLastModified(TIME_5);
		testServerDirectory3File5.setLastModified(TIME_1);
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
	public void testIsBackupRequired() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testGetLastModifiedFileDate() throws IOException {

		BackupUtilityApplication.Server testServer1 = new BackupUtilityApplication.Server(testServerDirectory1);
		BackupUtilityApplication.Server testServer2 = new BackupUtilityApplication.Server(testServerDirectory2);
		BackupUtilityApplication.Server testServer3 = new BackupUtilityApplication.Server(testServerDirectory3);
		
		/*
		 * Compare actual and expected dates
		 */
		
		Date actual1 = testServer1.getLastModifiedFileDate();
		Date actual2 = testServer2.getLastModifiedFileDate();
		Date actual3 = testServer3.getLastModifiedFileDate();
		
		Date expected1 = new Date(FLOORED_TIME_3);
		Date expected2 = new Date(FLOORED_TIME_3);
		Date expected3 = new Date(FLOORED_TIME_5);
		
		assertTrue(expected1.equals(actual1));
		assertTrue(expected2.equals(actual2));		
		assertTrue(expected3.equals(actual3));
	}
	
	@Ignore
	@Test
	public void testLastModifiedInFolder() {
		fail("Not yet implemented");
	}
	
}
