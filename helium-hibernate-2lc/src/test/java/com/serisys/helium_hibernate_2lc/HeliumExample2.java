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

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Random;

import javax.cache.CacheException;

import com.levyx.helium.Helium;
import com.levyx.helium.HeliumException;
import com.levyx.helium.HeliumItem;
import com.levyx.helium.HeliumIterator;;

public class HeliumExample2 {
	// Replace this with the device file you want to run Helium on,
	// such as "/dev/sda".
	// Any data on this device will be destroyed!
	public static final String DEVICE_PATH = "D:/Levyx/Helium/HE_BLOCK.DAT";

	public static final int ITERATIONS = 10;

	private static final Random rand = new Random();

	public static void main(final String[] args) {
		String lib_path = System.getProperty("java.library.path");
		System.out.println(lib_path);
		if (!new File(DEVICE_PATH).isFile()) {
			System.err.printf("Device %s does not exist.\n", DEVICE_PATH);
			System.exit(1);
		}

		// See he_open(1) for an explanation of the Helium URL structure
		String heliumURL = String.format("he://./%s", DEVICE_PATH);
		System.out.printf("Opening \"%s\"...\n", heliumURL);

		Helium he = new Helium(heliumURL, "my_datastore_name",
				Helium.HE_O_CREATE | Helium.HE_O_TRUNCATE | Helium.HE_O_VOLUME_CREATE);

		if (!he.isValid()) {
			Helium.printError("Couldn't open datastore");
			System.exit(1);
		}

		Map<String,String> check = new java.util.HashMap<String,String>();
		String kroot = "t0010_";
		String vroot = "t0010_defaultCache_iterator_";

		for (int idx = 0; idx < 8; idx ++) {
			String key = new StringBuilder().append(kroot).append(idx).toString(); 
			String value = new StringBuilder().append(vroot).append(idx).toString(); 
			byte[] byte_key = key.getBytes(Charset.forName("UTF-8"));

			byte[] byte_value = value.getBytes(Charset.forName("UTF-8"));
			System.out.printf("Inserting: %s, %s\n", key, value);

			HeliumItem item = new HeliumItem(byte_key.length, 512);
			String prev_value = null;
			item.setKeyBytes(byte_key);
			try {
				he.lookup(item);
				prev_value = new String(item.getValueBytes());
			} catch (HeliumException hex) {
				switch (hex.getErrorCode()) {
				case Helium.HE_ERR_ITEM_NOT_FOUND:
					prev_value = null;
					break;
				default:
					throw new CacheException(hex);
				}
			}
			item = new HeliumItem(byte_key.length, byte_value.length);
			item.setKeyBytes(byte_key);
			item.setValueBytes(byte_value);
			// "throws CacheWriterException - if the write fails. If thrown the cache mutation will not occur."
			he.update(item);
			
			check.put(key, value);
		}

		HeliumIterator iterator = he.iterator();
		while (iterator.hasNext()) {
			HeliumItem this_item = iterator.next();
			String key = new String(this_item.getKeyBytes());
			String value = new String(this_item.getValueBytes());
			System.out.printf("Removing: %s, %s\n", key, value);
			iterator.remove();
			check.remove(key, value);
		}
		System.out.println("Check size: " + check.size());
		
		
		iterator = he.iterator();
		System.out.println("iterator has next? " + iterator.hasNext());
		
		// Make sure you always he.close() before exiting.
		System.out.println("Closing datastore...");
		he.close();
	}
}
