package tests;

import static org.junit.Assert.assertEquals;

import java.util.BitSet;
import org.junit.Test;

import driver.ContextCleanser;

public class CCTest {
	
		@Test
		public void CCTest() {
			BitSet set = new BitSet(8);
			set.set(0);
			set.set(1);
			set.set(2);
			
			ContextCleanser cc = new ContextCleanser();
			String hash = cc.bitsetHash(set);
			assertEquals("11100000", hash.substring(0, 8));
	}
}