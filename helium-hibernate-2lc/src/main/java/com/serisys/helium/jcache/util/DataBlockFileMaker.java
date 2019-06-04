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

package com.serisys.helium.jcache.util;

import java.io.FileOutputStream;
import java.io.IOException;

public abstract class DataBlockFileMaker {

	public static void main(String[] args) {
		String filename = args[0];
		int gigas = 1;
		if (args.length == 2) {
			try {
				gigas = Integer.valueOf(args[1]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				gigas = 1;
			}
		}
/*		
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(filename, "rw");
			file.setLength(1024*1024*1024*8);
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
*/
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			byte[] bytes = new byte[1024*1024];
			for (int g = 0; g < gigas; g++) {
				for (int m = 0; m < 1024; m++) {
					fos.write(bytes);
					fos.flush();
				}
			}
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
