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

package com.serisys.helium.jcache;

import java.util.Collection;

public class KeysNotModified extends CacheNotModified {
	private static final long serialVersionUID = 9051603259216203010L;

	private Collection<?> unwrittenKeys; 

	public KeysNotModified() {
		
	}

	public KeysNotModified(String message) {
		super(message);
		
	}

	public KeysNotModified(Throwable cause) {
		super(cause);
		
	}

	public KeysNotModified(String message, Throwable cause) {
		super(message, cause);
		
	}

	public KeysNotModified(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}
	
	public KeysNotModified(Collection<?> unwrittenKeys) {
		this();
		this.unwrittenKeys = unwrittenKeys;
	}
	
	public Collection<?> getUnwrittenKeys() {
		return unwrittenKeys;
	}

}
