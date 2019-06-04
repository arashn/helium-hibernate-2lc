/*
 * 
 * Copyright © Serisys Solutions (Europe) Limited 2018-2019
 *
 *
    This file is part of helium-hibernate-2lc.

    helium-hibernate-2lc is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    helium-hibernate-2lc is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with helium-hibernate-2lc.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.serisys.helium_hibernate_2lc;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.levyx.helium.Helium;
import com.levyx.helium.HeliumException;
import com.levyx.helium.HeliumItem;
import com.levyx.helium.HeliumIterator;

public class HeliumTestApp {
	
	protected static Helium helium;
	
	private static HeliumItem item(String key, String value) {
		byte[] byte_key = key.getBytes(Charset.forName("UTF-8"));
		byte[] byte_val = value.getBytes(Charset.forName("UTF-8"));
		HeliumItem item = new HeliumItem(byte_key.length, byte_val.length);
		item.setKeyBytes(byte_key);
		item.setValueBytes(byte_val);
		return item;
	}
	
	private static HeliumItem item(String key) {
		byte[] byte_key = key.getBytes(Charset.forName("UTF-8"));
		HeliumItem item = new HeliumItem(byte_key.length, 512);
		item.setKeyBytes(byte_key);
		return item;
	}
	
	public static void main(String[] args) {
		String DEVICE_PATH = args.length < 1 ? "D:/Levyx/Helium/HE_BLOCK.DAT" : args[0];
        String heliumURL = String.format("he://./%s", DEVICE_PATH);
        System.out.printf("Opening \"%s\"...\n", heliumURL);
		helium = new Helium(heliumURL, "HeliumTest", Helium.HE_O_CREATE | Helium.HE_O_TRUNCATE | Helium.HE_O_VOLUME_CREATE);
		
		final String akey = "t0003_1";
		final String akey2 = "t0003_2";
		final String avalue = "t0003_defaultCache_put_getAll";
		final String avalue2 = "t0003 some other string";
		
		helium.insert(item(akey, avalue));
		helium.insert(item(akey2, avalue2));

		// clear
		String bkey = "t0009";
		String bvalue = "t0009_defaultCache_clear";
		helium.insert(item(bkey, bvalue));
		HeliumIterator he_iterator = helium.iterator();
		List<byte[]> to_remove = new java.util.ArrayList<byte[]>();
		while (he_iterator.hasNext()) {
			HeliumItem item = he_iterator.next();
			to_remove.add(item.getKeyBytes());
		}
		for (byte[] byte_key : to_remove) {
			HeliumItem item = new HeliumItem(byte_key.length, 512);
			item.setKeyBytes(byte_key);
			helium.delete(item);
		}
		try {
			helium.lookup(item(bkey));
		} catch (HeliumException hex) { 
			assertEquals("Unexpected HeliumException "+hex, Helium.HE_ERR_ITEM_NOT_FOUND, hex.getErrorCode());
		}
		he_iterator = helium.iterator();
		boolean something = he_iterator.hasNext();
		assertFalse("Iterator should have been empty!" , something);

		// Iterator issue?
		// Insert 8 objects with keys t0010_0 ... t0010_7
		// Add the keys and values into the Map "check"
		Map<String,String> check = new java.util.HashMap<String,String>();
		String kroot = "x0010_";
		String vroot = "x0010_defaultCache_iterator_";
		for (int i = 0; i < 8; i++) {
			String ikey = new StringBuilder().append(kroot).append(i).toString(); 
			String ivalue = new StringBuilder().append(vroot).append(i).toString(); 
			System.out.println("Adding " + ikey + " -> " + ivalue);
			helium.update(item(ikey, ivalue));
			check.put(ikey, ivalue);
		}
		// Iterate over the cache contents and remove everything we find from "check"
		Iterator<HeliumItem> cache_contents = helium.iterator();
		while (cache_contents.hasNext()) {
			HeliumItem item = cache_contents.next();
			String ikey = new String(item.getKeyBytes());
			String ivalue = new String(item.getValueBytes());
			System.out.printf("Iterator.next(): %s => %s\n", ikey, ivalue);
			System.out.println("Removing " + ikey + " -> " + ivalue);
			String match = check.remove(ikey);
		}
		// "check" should now be empty
		System.out.println("check contents: " + check);
		assertTrue("****** check is not empty!!!! " + check, check.isEmpty());
		
		for (Map.Entry<String, String> remaining : check.entrySet()) {
			try {
				System.out.println("Looking up residual item " + remaining.getKey());
				HeliumItem item_remain = item(remaining.getKey());
				helium.lookup(item_remain);
				String value_remain = new String(item_remain.getValueBytes());
				System.out.println(" ... residual value " + value_remain);
			} catch (HeliumException hex) {
				if (Helium.HE_ERR_ITEM_NOT_FOUND == hex.getErrorCode()) {
					System.out.println(" ... HE_ERR_ITEM_NOT_FOUND ");
				} else {
					System.out.println(" ... other exception: " + hex.getErrorCode());
				}
			}
		}
		helium.close();
	}
	
	public static void assertEquals(String msg, Object o1, Object o2) {
		boolean equal = o1 == null ? 
			o2 == null : o1.equals(o2);
		if (!equal) {
			System.out.println(msg);
		}
	}
	
	public static void assertFalse(String msg, boolean condition) {
		if (condition) {
			System.out.println(msg);
		}
	}
	
	public static void assertTrue(String msg, boolean condition) {
		if (!condition) {
			System.out.println(msg);
		}
	}
	

}
