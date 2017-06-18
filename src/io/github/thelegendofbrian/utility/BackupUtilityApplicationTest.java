package io.github.thelegendofbrian.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.File;
import java.util.Date;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import io.github.talkarcabbage.logger.LoggerManager;

public class BackupUtilityApplicationTest {
	
	private static final Logger logger = LoggerManager.getInstance().getLogger("mainTest");
	
	private static File tempDirectory = new File("temp");
	
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
	public void testGetPropertiesFromFile() {
		fail("Not yet implemented");
	}
	
	@Ignore
	@Test
	public void testLoadConfig() {
		fail("Not yet implemented");
	}
	
	@Ignore
	@Test
	public void testSaveConfig() {
		fail("Not yet implemented");
	}
	
	@Ignore
	@Test
	public void testGetServerList() {
		fail("Not yet implemented");
	}
	
	@Ignore
	@Test
	public void testGetServerAndBackupList() {
		fail("Not yet implemented");
	}
	
	@Ignore
	@Test
	public void testGetServersToBackup() {
		fail("Not yet implemented");
	}
	
	@Ignore
	@Test
	public void testBackupServers() {
		fail("Not yet implemented");
	}
	
	@Ignore
	@Test
	public void testBackupSpecificServer() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testRoundDateToSeconds() {
		final long[] times = {0L, 1495407727000L, 1495407727067L, 1495407727500L};
		final long[] expectedTimes = {0L, 1495407727000L, 1495407727000L, 1495407728000L};
		Date roundCheck = new Date(0L);
		for (int i = 0; i < 3; i++) {
			roundCheck.setTime(times[i]);
			roundCheck = BackupUtilityApplication.roundDateToSeconds(roundCheck);
			long roundedTime = roundCheck.getTime();
			assertEquals(expectedTimes[i], roundedTime);
		}
	}
	
}
