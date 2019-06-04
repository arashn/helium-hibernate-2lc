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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CustomCacheKey implements Serializable, Comparable<String> {
	private static final long serialVersionUID = 2053816666394213490L;
	protected String value;
	
	public CustomCacheKey(String value) {
		this.value = value;
	}

	@Override
	public int compareTo(String o) {
		return value.compareTo(o);
	}
	
	@Override
	public boolean equals(Object obj) {		
		return obj == null ? 
			false : 
			obj instanceof CustomCacheKey ? 
				this.value.equals(((CustomCacheKey) obj).value) :
				false;
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		value = stream.readUTF();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		step();
		out.writeUTF(value);
	}
	
	protected void step() {
		
	}
}
