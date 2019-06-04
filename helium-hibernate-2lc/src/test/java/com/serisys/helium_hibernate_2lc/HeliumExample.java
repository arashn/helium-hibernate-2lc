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
import java.util.Random;

import com.levyx.helium.Helium;
import com.levyx.helium.HeliumException;
import com.levyx.helium.HeliumItem;
import com.levyx.helium.HeliumIterator;

public class HeliumExample {
    // Replace this with the device file you want to run Helium on,
    // such as "/dev/sda".
    // Any data on this device will be destroyed!
    public static final String DEVICE_PATH = "D:/Levyx/Helium/HE_BLOCK.DAT";

    public static final int ITERATIONS = 1000;

    private static final Random rand = new Random();

    public static void main(final String[] args) {
    	String lib_path = System.getProperty("java.library.path");
//    	lib_path = "I:\\Downloads\\Levyx\\serisys-helium-rel1;"+lib_path;
//    	System.setProperty("java.library.path", lib_path);
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

        // Operations are done with items, which are two buffers, one
        // for the key and the other for the value. The true buffer
        // size is abstracted away by the class for convenience.
        // However if you wish, you can manually manipulate the underlying
        // ByteBuffer with the getKeyBuffer() and getValueBuffer() calls.
        HeliumItem item = new HeliumItem(20, 20);

        for (int i = 0; i < ITERATIONS; i++) {
            String keyString = String.format("key_%d", i);
            item.setKeyBytes(keyString.getBytes());

            String valueString = String.format("ABCDEFGHIJKLMNOPQRSTUVWXYZ %d_%d", i * i, Math.abs(rand.nextInt() % 100));
            item.setValueBytes(valueString.getBytes());

            System.out.printf("Inserting: %s, %s\n", keyString, valueString);

            try {
                he.insert(item);
            } catch (HeliumException ex) {
                // Helium functions can throw HeliumException, which is an unchecked
                // exception. You can get the original Helium error code by
                // invoking getErrorCode(), which can be used in functions like
                // Helium.getError()

                Helium.printError("Insert failed");
                he.close();
                System.exit(1);
            }
        }

        for (int i = 0; i < ITERATIONS; i++) {
            String keyString = String.format("key_%d", i);
            item.setKeyBytes(keyString.getBytes());

            // Since the exceptions are unchecked, you don't
            // need to catch them if you know the operation
            // will not fail.
            he.lookup(item);

            String valueString = new String(item.getValueBytes());
            System.out.printf("%s => %s\n", keyString, valueString);
        }
        
        
        HeliumIterator iterator = he.iterator();
        while (iterator.hasNext()) {
        	HeliumItem this_item = iterator.next();
            String keyString = new String(this_item.getKeyBytes());
            String valueString = new String(this_item.getValueBytes());
            System.out.printf("Iterator.next(): %s => %s\n", keyString, valueString);
        }
        
        String keyString = "No such key";
        item.setKeyBytes(keyString.getBytes());

        // Since the exceptions are unchecked, you don't
        // need to catch them if you know the operation
        // will not fail.
        try {
        	he.lookup(item);
        } catch (HeliumException hex) {
        	System.out.printf("Exception for key %s\n", keyString);
        	if (hex.getErrorCode() == Helium.HE_ERR_ITEM_NOT_FOUND) {
        		System.out.printf("Error code %d indicates item not found\n", hex.getErrorCode());
        	}
        	hex.printStackTrace();
        }

        // Make sure you always he.close() before exiting.
        System.out.println("Closing datastore...");
        he.close();
    }
}

