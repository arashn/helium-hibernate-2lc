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

import java.nio.charset.Charset;

import com.levyx.helium.Helium;
import com.levyx.helium.HeliumException;
import com.levyx.helium.HeliumItem;
import com.levyx.helium.HeliumIterator;

public class RawHeliumCacheContentTestApp {
	public static final String HE_FILEPATH = "D:/Levyx/Helium/HE_BLOCK.DAT";
	public static final String HE_DEV_URL = "he://./" + HE_FILEPATH;
	private static final int DEFAULT_VALUE_BYTE_LENGTH = 512;
	private static final Charset UTF = Charset.forName("UTF-8");

	public static void main(String[] args) {
		String cacheName = "TestDatastore";
		int modes = Helium.HE_O_CREATE | Helium.HE_O_TRUNCATE | Helium.HE_O_VOLUME_CREATE;
		Helium datastore = new Helium(HE_DEV_URL, cacheName, modes);

		System.out.println("Mimic test t0001_defaultCache_put_get()");

		String t0001_key = "t0001";
		String t0001_value = "A valuable piece of string";
		put(datastore, t0001_key, t0001_value);
		assertEquals(t0001_value, get(datastore, t0001_key));

		System.out.println(" ... done");
		System.out.println("Mimic test  t0007_defaultCache_invoke()");

		String no_vowels_key = "t0007_no_vowels";
		put(datastore, no_vowels_key, "cdfgxhbklz");
		String some_vowels_key = "t0007_some_vowels";
		put(datastore, some_vowels_key, "llangollen");
		String all_the_vowels_key = "t0007_all_the_vowels";
		put(datastore, all_the_vowels_key, "the quick brown fox jumped over the lazy dog");
		get(datastore, no_vowels_key);
		get(datastore, some_vowels_key);
		get(datastore, all_the_vowels_key);

		System.out.println(" ... done");
		System.out.println("Mimic test  t0007_defaultCache_invoke_noSuchKey()");

		String xkey = "t0007_no_such_key";
		get(datastore, xkey);

		System.out.println(" ... done");
		System.out.println("Mimic test  t0008_defaultCache_invokeAll()");
		
		String no_fruits_key = "t0008_fruitless";
		put(datastore, no_fruits_key, "the quick brown fox jumped over the lazy dog");
		String some_fruits_key = "t0007_some_fruits";
		put(datastore, some_fruits_key, "the quick brown banana jumped over the lazy grape");
		String no_such_key = "shurely shome mishtake";
		get(datastore, no_fruits_key);
		get(datastore, some_fruits_key);
		get(datastore, no_such_key);

		System.out.println(" ... done");
		System.out.println("Mimic test  t0009_defaultCache_clear()");

		String key = "t0009";
		String value = "t0009_defaultCache_clear";
		System.out.println("1");
		put(datastore, key, value);
		System.out.println("2");
		assertEquals(value, get(datastore, key));
		System.out.println("3");
		HeliumIterator he_iterator = datastore.iterator();
		while (he_iterator.hasNext()) {
			HeliumItem item = he_iterator.next();
			he_iterator.remove();
		}

		System.out.println("4");
		assertFalse(containsKey(datastore, key));
		System.out.println("5");
		he_iterator = datastore.iterator();
		System.out.println("6");
		boolean something = he_iterator.hasNext();
		System.out.println("7");
		assertFalse(something);
		System.out.println(" ... done");

		System.out.println("Closing cache");
		datastore.close();
		System.out.println(" ... done");
		System.out.println("####>>>>---- SUCCESS! ----<<<<####");
	}

	private static void print(boolean[] control) {
		StringBuilder bob = new StringBuilder();
		bob.append("Control: { ");
		bob.append(control[0]);
		for (int i = 1; i < control.length; i++) {
			bob.append(", ");
			bob.append(control[i]);

		}
		bob.append(" }");
		System.out.println(bob.toString());
	}

	static boolean containsKey(Helium datastore, String key) {
		byte[] byte_key = convertToCached(key);
		HeliumItem item = new HeliumItem(byte_key.length, DEFAULT_VALUE_BYTE_LENGTH);
		item.setKeyBytes(byte_key);
		return datastore.exists(item);
	}

	static void put(Helium datastore, String key, String value) {
		byte[] byte_key = convertToCached(key);
		byte[] byte_value = convertToCached(value);
		HeliumItem item = new HeliumItem(byte_key.length, DEFAULT_VALUE_BYTE_LENGTH);
		item.setKeyBytes(byte_key);
		String prev_value = null;
		try {
			datastore.lookup(item);
			prev_value = convertToDomain(item.getValueBytes());
		} catch (HeliumException hex) {
			switch (hex.getErrorCode()) {
			case Helium.HE_ERR_ITEM_NOT_FOUND:
				prev_value = null;
				break;
			default:
				throw hex;
			}
		}
		HeliumItem itemu = new HeliumItem(byte_key.length, byte_value.length);
		itemu.setKeyBytes(byte_key);
		itemu.setValueBytes(byte_value);
		datastore.update(itemu);
	}

	static String get(Helium datastore, String key) {
		byte[] byte_key = convertToCached(key);
		HeliumItem item = new HeliumItem(byte_key.length, DEFAULT_VALUE_BYTE_LENGTH);
		item.setKeyBytes(byte_key);
		try {
			datastore.lookup(item);
		} catch (HeliumException hex) {
			switch (hex.getErrorCode()) {
			case Helium.HE_ERR_ITEM_NOT_FOUND:
				return null;
			default:
				throw hex;
			}
		}
		return convertToDomain(item.getValueBytes());
	}

	static byte[] convertToCached(String s) {
		return s.getBytes(UTF);
	}

	static String convertToDomain(byte[] b) {
		return new String(b, UTF);
	}

	static void assertEquals(Object o1, Object o2) {
		boolean equal = o1 == null ? 
			o2 == null :
				o1.equals(o2);
		if (!equal) {
			throw new Error("objects not equal!");
		}
	}
	
	static void assertFalse(boolean b) {
		if (b) 
			throw new Error("boolean false condition not met!");
	}

}
