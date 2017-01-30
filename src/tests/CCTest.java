package tests;

import static org.junit.Assert.assertEquals;

import java.util.BitSet;
import org.junit.Test;

import datastructures.Dictionary;
import driver.ContextCleanser;

public class CCTest {
	
		@Test
		public void CCTest1() {
			Dictionary dic = new Dictionary();
			dic.addAttribute("0");
			dic.addAttribute("1");
			dic.addAttribute("2");
			dic.addAttribute("3");
			dic.addAttribute("4");
			dic.addAttribute("5");
			dic.addAttribute("6");
			dic.addAttribute("7");
			BitSet set = new BitSet(dic.getSize());
			set.set(0);
			set.set(1);
			set.set(2);
			
			ContextCleanser cc = new ContextCleanser(dic);
			String hash = cc.bitsetHash(set);
			
			assertEquals(dic.getSize(), 8);
			assertEquals(hash.length(), dic.getSize());
			assertEquals("11100000", hash);
	}
		
		@Test
		public void CCTest2() {
			Dictionary dic = new Dictionary();
			dic.addAttribute("0");
			dic.addAttribute("1");
			dic.addAttribute("2");
			dic.addAttribute("3");
			BitSet set = new BitSet(dic.getSize());
			
			ContextCleanser cc = new ContextCleanser(dic);
			String hash = cc.bitsetHash(set);
			
			assertEquals(dic.getSize(), 4);
			assertEquals(hash.length(), dic.getSize());
			assertEquals("0000", hash);
	}
}