package io.github.thelegendofbrian.utility;

import static org.junit.Assert.*;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;

public class MainTest {
	
	@Before
	public void setUp() throws Exception {
	}
	
	
	@Test
	public void testRoundDateToSeconds1() {
		long unroundedMillis = 1495407727000L;
		Date unroundedDate = new Date(unroundedMillis);
		Date roundedDate = Main.roundDateToSeconds(unroundedDate);
		long roundedMillis = roundedDate.getTime();
		assertEquals(1495407727000L, roundedMillis);
	}
	
	@Test
	public void testRoundDateToSeconds2() {
		long unroundedMillis = 1495407727034L;
		Date unroundedDate = new Date(unroundedMillis);
		Date roundedDate = Main.roundDateToSeconds(unroundedDate);
		long roundedMillis = roundedDate.getTime();
		assertEquals(1495407727000L, roundedMillis);
	}
	
	@Test
	public void testRoundDateToSeconds3() {
		long unroundedMillis = 1495407727067L;
		Date unroundedDate = new Date(unroundedMillis);
		Date roundedDate = Main.roundDateToSeconds(unroundedDate);
		long roundedMillis = roundedDate.getTime();
		assertEquals(1495407727000L, roundedMillis);
	}
	
	@Test
	public void testRoundDateToSeconds4() {
		long unroundedMillis = 0L;
		Date unroundedDate = new Date(unroundedMillis);
		Date roundedDate = Main.roundDateToSeconds(unroundedDate);
		long roundedMillis = roundedDate.getTime();
		assertEquals(0L, roundedMillis);
	}
	
}
