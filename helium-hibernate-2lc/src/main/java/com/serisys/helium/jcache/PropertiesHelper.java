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

import java.util.Properties;

import com.levyx.helium.Helium;
import com.serisys.helium.jcache.synch.DualLevelLockHandler;

public class PropertiesHelper extends Properties implements HeCacheProperties {

	public PropertiesHelper(Properties properties) {
		super(properties); 
	}
	
	public String getDeviceURL(String cacheName) { 
		String he_dev_url = getPropertyForNamedCacheOrGlobal(HE_CACHE_URL_PROPERTY, cacheName);
		if (he_dev_url == null) {
			Helium.printError("No configured device URL for Helium");
			throw new IllegalArgumentException("No configured device URL for Helium");
		}
		return he_dev_url;
	}
	
	private boolean getFlagForNamedCacheOrGlobal(String propertyName, String cacheName, boolean default_flag_val) {
		String prop_val = getPropertyForNamedCacheOrGlobal(propertyName, cacheName);
		return prop_val == null ? default_flag_val : Boolean.parseBoolean(prop_val);
	}
	
	private boolean getFlagForNamedCacheOrGlobal(String propertyName, String cacheName) {
		String prop_val = getPropertyForNamedCacheOrGlobal(propertyName, cacheName);
		return prop_val == null ? false : Boolean.parseBoolean(prop_val);
	}
	
	private String getPropertyForNamedCacheOrGlobal(String propertyName, String cacheName, String default_prop_val) {
		String prop_val = getPropertyForNamedCacheOrGlobal(propertyName, cacheName);
		return prop_val == null ? default_prop_val : prop_val;
	}
	
	private String getPropertyForNamedCacheOrGlobal(String propertyName, String cacheName) {
		String prop_val = getPropertyForNamedCache(propertyName, cacheName);
		if (prop_val == null) {
			prop_val = getProperty(propertyName);
		}
		return prop_val;
	}
	
	private String getPropertyForNamedCache(String propertyName, String cacheName) {
		String prop_key = new StringBuilder()
			.append(propertyName)
			.append('.')
			.append(cacheName)
			.toString();
		return getProperty(prop_key);
	}
	
	@Override
	public String getProperty(String prop_key) {
		String he_dev_url = super.getProperty(prop_key);
		if (he_dev_url == null) {
			he_dev_url = System.getProperty(prop_key);
		}
		return he_dev_url;
	}
	
	String getSynchHandlerClazName() {
		return getProperty(SYNCH_HANDLER_CLASS_NAME, DualLevelLockHandler.class.getName());
	}
	
	public int getFlags(String cacheName) {
		int cumulative_flags = 0;
		cumulative_flags = combine(cumulative_flags, cacheName, HE_O_CLEAN_PROPERTY, Helium.HE_O_CLEAN);
		cumulative_flags = combine(cumulative_flags, cacheName, HE_O_COMPRESS_PROPERTY, Helium.HE_O_COMPRESS);
		cumulative_flags = combine(cumulative_flags, cacheName, HE_O_CREATE_PROPERTY, Helium.HE_O_CREATE);
		cumulative_flags = combine(cumulative_flags, cacheName, HE_O_NOSORT_PROPERTY, Helium.HE_O_NOSORT);
		cumulative_flags = combine(cumulative_flags, cacheName, HE_O_SCAN_PROPERTY, Helium.HE_O_SCAN);
		cumulative_flags = combine(cumulative_flags, cacheName, HE_O_TRUNCATE_PROPERTY, Helium.HE_O_TRUNCATE);
		cumulative_flags = combine(cumulative_flags, cacheName, HE_O_VOLUME_CREATE_PROPERTY, Helium.HE_O_VOLUME_CREATE);
		cumulative_flags = combine(cumulative_flags, cacheName, HE_O_VOLUME_TRUNCATE_PROPERTY, Helium.HE_O_VOLUME_TRUNCATE);
		return cumulative_flags;
	}
	
	private int combine(int cumulative_flags, String cacheName, String prop_key, int helium_flag) {
		if (getFlagForNamedCacheOrGlobal(prop_key, cacheName)) {
			cumulative_flags = cumulative_flags | helium_flag;
		}
		return cumulative_flags;
	}

}
