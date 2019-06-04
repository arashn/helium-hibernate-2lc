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

import javax.cache.configuration.Factory;

import com.levyx.helium.Helium;

public class HeliumFactory implements Factory<Helium> {
	private String url;
	private String datastoreName; 
	private int flags;
	private boolean setup = false; 
	private static final long serialVersionUID = 3918186149525572818L;

	public HeliumFactory() {
	}

	public void set(String url, String datastoreName, int flags) {
		setup = true;
		this.url = url;
		this.datastoreName = datastoreName;
		this.flags = flags;
	}

	public String getUrl() {
		return url;
	}

	public String getDatastoreName() {
		return datastoreName;
	}

	public int getFlags() {
		return flags;
	}

	@Override
	public Helium create() {
		if (!setup) 
			throw new IllegalArgumentException("Helium factory not initialized");
		long start = System.currentTimeMillis();
		Helium he_man = new Helium(url, datastoreName, flags);
		long stop = System.currentTimeMillis();
		HeCache.log("############## new Helium (ms): " + (stop-start) + " flags: " + flags);
		return he_man;
	}

}
