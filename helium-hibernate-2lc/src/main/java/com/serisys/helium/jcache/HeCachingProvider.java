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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.cache.CacheManager;
import javax.cache.configuration.OptionalFeature;
import javax.cache.spi.CachingProvider;

public class HeCachingProvider implements CachingProvider {
	public final static Logger logger = Logger.getLogger("HeCachingProvider");
	public final static String HECACHE_CONFIG_FILE="hecache.properties";
	
	private String defaultURIString = "com.serisys.helium.jcache"; 
	private URI defaultURI = URI.create(defaultURIString);
	private final Properties defaultProperties = new Properties(); 
	private final Map<URI, Map<ClassLoader, HeCacheManager>> cacheManagersByURI = new java.util.HashMap<URI, Map<ClassLoader, HeCacheManager>>();
	private final Map<ClassLoader, Map<URI, HeCacheManager>> cacheManagersByClassLoader = new java.util.HashMap<ClassLoader, Map<URI, HeCacheManager>>();
	
	public HeCachingProvider() {
		defaultProperties.setProperty(HeCacheProperties.HE_O_CREATE_PROPERTY, "true");
		defaultProperties.setProperty(HeCacheProperties.HE_O_TRUNCATE_PROPERTY, "true");
		defaultProperties.setProperty(HeCacheProperties.HE_O_VOLUME_CREATE_PROPERTY, "true");

		InputStream instr = null; 
		try {
			instr = HeCachingProvider.class.getResourceAsStream("/"+HECACHE_CONFIG_FILE);
//			instr = new FileInputStream(HECACHE_CONFIG_FILE);
			if (instr != null) {
				defaultProperties.load(instr);
				instr.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public HeCachingProvider(String defURIString, Properties defProperties) {
		setDefaultURIString(defURIString);
		setDefaultProperties(defProperties);
	}

	@Override
	public CacheManager getCacheManager(URI uri, ClassLoader classLoader, Properties properties) {
		uri = uri == null ? getDefaultURI() : uri; 
		classLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
		HeCacheManager mngr = managed(uri, classLoader);
		if (mngr == null) {
			properties = properties == null ? getDefaultProperties() : properties; 
			String cmid = generateCleanCMID(uri, classLoader);
			mngr = new HeCacheManager(cmid, uri, this, classLoader, properties);
			manage(mngr, uri, classLoader);
		}
		return mngr;
	}
	
	private void manage(HeCacheManager cacheManager, URI uri, ClassLoader loader) {
		// remember it one way ...
		Map<ClassLoader,HeCacheManager> byLoaderForURI = cacheManagersByURI.get(uri);
		if (byLoaderForURI == null) {
			byLoaderForURI = new java.util.HashMap<ClassLoader, HeCacheManager>();
			cacheManagersByURI.put(uri, byLoaderForURI);
		};
		byLoaderForURI.put(loader, cacheManager);
		// ... and the other
		Map<URI,HeCacheManager> byURIForLoader = cacheManagersByClassLoader.get(loader);
		if (byURIForLoader == null) {
			byURIForLoader = new java.util.HashMap<URI, HeCacheManager>();
			cacheManagersByClassLoader.put(loader, byURIForLoader);
		}
		byURIForLoader.put(uri, cacheManager);
	}
	
	private HeCacheManager managed(URI uri, ClassLoader loader) {
		Map<ClassLoader,HeCacheManager> byLoaderForURI = cacheManagersByURI.get(uri);
		return byLoaderForURI == null ? null : byLoaderForURI.get(loader);
	}
	
	@Override
	public ClassLoader getDefaultClassLoader() {
		return this.getClass().getClassLoader();
	}

	@Override
	public URI getDefaultURI() {
		return defaultURI;
	}

	public String getDefaultURIString() {
		return defaultURIString;
	}

	@Override
	public Properties getDefaultProperties() {
		return defaultProperties;
	}

	@Override
	public CacheManager getCacheManager(URI uri, ClassLoader classLoader) {
		return getCacheManager(uri, classLoader, getDefaultProperties());
	}

	@Override
	public CacheManager getCacheManager() {
		return getCacheManager(getDefaultURI(), getDefaultClassLoader());
	}

	@Override
	public void close() {
		for (ClassLoader classLoader : cacheManagersByClassLoader.keySet()) {
			close(classLoader);
		}
	}

	@Override
	public void close(ClassLoader classLoader) {
		Map<URI,HeCacheManager> byURIForLoader = cacheManagersByClassLoader.get(classLoader);
		if (byURIForLoader == null) {
			return;
		}
		for (Map.Entry<URI,HeCacheManager> entry : byURIForLoader.entrySet()) {
			URI uri = entry.getKey();
			HeCacheManager mngr = entry.getValue();
			cacheManagersByURI.get(uri).remove(classLoader);
			mngr.close();
		}
	}

	@Override
	public void close(URI uri, ClassLoader classLoader) {
		Map<URI,HeCacheManager> byURIForLoader = cacheManagersByClassLoader.get(classLoader);
		if (byURIForLoader == null) {
			return;
		}		
		HeCacheManager mngr = byURIForLoader.remove(uri);
		if (mngr != null) {
			cacheManagersByURI.get(uri).remove(classLoader);
			mngr.close();
		}
	}

	@Override
	public boolean isSupported(OptionalFeature optionalFeature) {
		switch (optionalFeature) {
		case STORE_BY_REFERENCE:
			return false;
		default:
			return false;
			//throw new IllegalArgumentException("Unknown optional feature " + optionalFeature);
		}
	}
	
	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties.putAll(defaultProperties);
	}
	
	public void setDefaultURIString(String defURIString) {
		this.defaultURIString = defURIString;
		if (defaultURIString != null && defaultURIString.trim().length() > 0) {
			defaultURI = URI.create(defaultURIString);
		}
	}

	private String generateCleanCMID(URI uri, ClassLoader cloader) {
		StringBuilder bob = new StringBuilder();
		bob.append(uri.toASCIIString())
			.append("_")
			.append(cloader.equals(getDefaultClassLoader()) ? "DCL" : System.identityHashCode(cloader));
		return bob.toString();
	}
}
