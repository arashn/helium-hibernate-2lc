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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;
import javax.cache.spi.CachingProvider;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import com.serisys.helium.jcache.mx.CacheMXBeanImpl;
import com.serisys.helium.jcache.mx.CacheMXStatsBeanImpl;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class HeCacheManager implements CacheManager {
	
	//ensure everything gets put in one MBeanServer
	 private static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	
	private boolean closed;
	private final String cmid; 
	private final URI uri;
	private final HeCachingProvider heCachingProvider;
	private final ClassLoader classLoader; 
	private final PropertiesHelper properties; 
	private final Map<String, Cache> cachesByName = new java.util.HashMap<String, Cache>();
	private final Map<String, CompleteConfiguration> configsByName = new java.util.HashMap<String,CompleteConfiguration>();

	private static final DataAdaptor<byte[], String> STRING_DOMAIN_DATA_ADAPTOR = new DataAdaptor<byte[], String>() {
		private final Charset charset = Charset.forName("UTF-8");
		@Override
		public byte[] convertToCached(String domainObject) {
			return domainObject.getBytes(charset);
		}

		@Override
		public String convertToDomain(byte[] cacheEntry) {
			return new String(cacheEntry, charset);
		}
	};
	private static final DataAdaptor<byte[],byte[]> BYTE_ARRAY_DOMAIN_DATA_ADAPTOR = new DataAdaptor<byte[],byte[]>() {

		@Override
		public byte[] convertToCached(byte[] domainObject) {
			byte[] copy = new byte[domainObject.length];
			System.arraycopy(domainObject, 0, copy, 0, domainObject.length);
			return domainObject;
		}

		@Override
		public byte[] convertToDomain(byte[] cacheEntry) {
			byte[] copy = new byte[cacheEntry.length];
			System.arraycopy(cacheEntry, 0, copy, 0, cacheEntry.length);
			return cacheEntry;
		}
		
	};
	private static final DataAdaptor<byte[], Serializable> SERIALIZABLE_DOMAIN_DATA_ADAPTOR = new DataAdaptor<byte[], Serializable>() {

		private Pool<Kryo> kryoPool;
		private Pool<Input> inputPool;
		private Pool<Output> outputPool;

		{
			kryoPool = new Pool<Kryo>(true, false, 16) {
				protected Kryo create () {
					Kryo kryo = new Kryo();
					// Configure the Kryo instance.
					kryo.setRegistrationRequired(false);
					kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
					return kryo;
				}
			};

			inputPool = new Pool<Input>(true, false, 16) {
				@Override
				protected Input create() {
					return new Input(4096);
				}
			};

			outputPool = new Pool<Output>(true, false, 16) {
				@Override
				protected Output create() {
					return new Output(4096, 4096);
				}
			};
		}

		@Override
		public byte[] convertToCached(Serializable domainObject) {
			byte[] cacheEntry = null;
			Kryo kryo = kryoPool.obtain();
			ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
			Output output = outputPool.obtain();
			try {
				output.setOutputStream(bytes);
				kryo.writeClassAndObject(output, domainObject);
				output.flush();
				bytes.flush();
				cacheEntry = bytes.toByteArray();
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				outputPool.free(output);
				kryoPool.free(kryo);
			}
			return cacheEntry;
		}

		@Override
		public Serializable convertToDomain(byte[] cacheEntry) {
			Kryo kryo = kryoPool.obtain();
			Input input = inputPool.obtain();
			input.setInputStream(new ByteArrayInputStream(cacheEntry));
			Serializable domainObject = (Serializable) kryo.readClassAndObject(input);
			input.close();
			inputPool.free(input);
			kryoPool.free(kryo);
			return domainObject;
		}
	};
	
	public HeCacheManager(String cmid, URI uri, HeCachingProvider provider, ClassLoader classLoader, Properties properties) {
		this.cmid = cmid;
		this.uri = uri;
		this.heCachingProvider = provider; 
		this.classLoader = classLoader;
		this.properties = new PropertiesHelper(properties); 
	}

	private HeCachingProvider getHeCachingProvider() {
		return heCachingProvider;
	}
	
	@Override
	public CachingProvider getCachingProvider() {
		return heCachingProvider;
	}

	@Override
	public URI getURI() {
		return uri;
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}
	
	protected String getCMID() {
		return cmid;
	}
	
	protected Cache managedCache(String cacheName) {
		return cachesByName.get(cacheName);
	}
	
	private CompleteConfiguration managedConfig(String cacheName) {
		return configsByName.get(cacheName);
	}
	
	private void manageCache(String cacheName, Cache cache, CompleteConfiguration config) {
		cachesByName.put(cacheName, cache);
		configsByName.put(cacheName, config);
	}

	protected void forgetCache(String cacheName) {
		checkCacheName(cacheName);
		cachesByName.remove(cacheName);
		configsByName.remove(cacheName);
	}
	
	private void checkCacheName(String cacheName) {
		if (cacheName == null) {
			throw new NullPointerException("Null cache name supplied");
		}
	}
	
	private void checkOpen() {
		if (closed) {
			throw new IllegalStateException("Cache manager is closed");
		}
	}

	@Override
	public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String cacheName, C configuration)
			throws IllegalArgumentException {
		checkOpen();
		checkCacheName(cacheName);
		if (configuration == null) {
			throw new NullPointerException("Null configuration supplied");
		}
		if (managedCache(cacheName) != null) {
			throw new CacheException("A cache is already in existence for the specified name");
		}
		ConsolidatedCacheConfig<K,V> complete = configuration instanceof ConsolidatedCacheConfig ? 
			new ConsolidatedCacheConfig<K,V>((ConsolidatedCacheConfig<K,V>) configuration) : 
			configuration instanceof CompleteConfiguration? 
				new ConsolidatedCacheConfig<K, V>((CompleteConfiguration)configuration) : 
				new ConsolidatedCacheConfig<K, V>(configuration); 
		Class<K> keyType = complete.getKeyType();
		Class<V> valueType = complete.getValueType();
		if (!Serializable.class.isAssignableFrom(keyType) || !Serializable.class.isAssignableFrom(valueType)) {
			// We can only really support key and value types which are serializable. 
			// Need to remove this check for RI test pack conformance but it WILL now blow
			// up at runtime if the key or value type cannot be serialized as byte[]
//			throw new IllegalArgumentException("Only serializable key / value types are supported by Helium cache");
		}
		boolean store_by_ref = !complete.isStoreByValue();
		if (store_by_ref) {
			throw new UnsupportedOperationException("Store-by-reference cache requested but not supported by Helium cache");
		}
		DataAdaptor<byte[],K> keyAdaptor = dataAdaptor(keyType);
		DataAdaptor<byte[],V> valueAdaptor = dataAdaptor(valueType);
		if (complete.isReadThrough() && complete.getCacheLoaderFactory() == null) {
			throw new IllegalArgumentException("CacheLoader factory must be specified for a read-through cache");
		}
		CacheLoader<K, V> cacheLoader = null;
		if (complete.getCacheLoaderFactory() == null) {
			cacheLoader = NoReadThroughCacheLoader.SINGLETON;
		} else {
			cacheLoader = complete.getCacheLoaderFactory().create();
			if (!complete.isReadThrough()) {
				cacheLoader = new ReadThroughDisabledCacheLoader(cacheLoader);
			}
		}
		if (complete.isWriteThrough() && complete.getCacheWriterFactory() == null) {
			throw new IllegalArgumentException("CacheWriter factory must be specified for a write-through cache");
		}
		CacheWriter cacheWriter = complete.getCacheWriterFactory() == null ? 
				NoWriteThroughCacheWriter.SINGLETON : 
				complete.getCacheWriterFactory().create();		
		HeCache<K, V> new_cache = new HeCache<K, V>(this, cacheName, properties, keyAdaptor, valueAdaptor, cacheLoader, cacheWriter, complete);
		WrappableCacheWrapper<K, V> ho_ho = new WrappableCacheWrapper<K, V>(new_cache);
		
		if (complete.isStatisticsEnabled() || Boolean.valueOf(getProperties().getProperty(HeCacheProperties.ENABLE_STATISTICS))) {
			ho_ho.setStatisticsEnabled(true);
		}
		if (complete.isManagementEnabled() || Boolean.valueOf(getProperties().getProperty(HeCacheProperties.ENABLE_MANAGEMENT))) {
			ho_ho.setManagementEnabled(true);
		}
		manageCache(cacheName, ho_ho, complete);
		return ho_ho;
	}

	@Override
	public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
		checkOpen();
		checkCacheName(cacheName);
		CompleteConfiguration  complete = managedConfig(cacheName);
		if (complete == null) {
			return null;
		}
		if (keyType.isAssignableFrom(complete.getKeyType()) &&
			valueType.isAssignableFrom(complete.getValueType())) {
			Cache<K, V> cache = managedCache(cacheName);
			return cache;
		} else {
			if (keyType.isAssignableFrom(complete.getKeyType())) {
				// key is okay - value is invalid
				throw new ClassCastException("Illegal valueType: " + valueType);
			}
			else {
				if (valueType.isAssignableFrom(complete.getValueType())) {
					// key is invalid - value is okay
					throw new ClassCastException("Illegal keyType: " + keyType);
				}
				else {
					// Both key & value are invalid
					throw new ClassCastException("Illegal keyType: " + keyType + " Illegal valueType: " + valueType);
				}
				
			}
			
		}
	}

	/**
	  * https://github.com/jsr107/jsr107spec/issues/340
	  * in 1.1 we relaxed {@link CacheManager#getCache(String)} to not enforce a check.
	  */	@Override
	public <K, V> Cache<K, V> getCache(String cacheName) {
		checkOpen();
		checkCacheName(cacheName);
		CompleteConfiguration config = managedConfig(cacheName);
		if (config == null) {
			return null;
		}
		Class type = config.getKeyType();
		if (type != null && !type.isAssignableFrom(Object.class)) {
			System.out.println("Invalid key type: " + type);
//			throw new IllegalArgumentException("cache for requested name has restricted key type");
		}
		type = config.getValueType();
		if (type != null && !type.isAssignableFrom(Object.class)) {
			System.out.println("Invalid value type: " + type);
//			throw new IllegalArgumentException("cache for requested name has restricted value type");
		}
		return managedCache(cacheName);
	}

	@Override
	public Iterable<String> getCacheNames() {
		checkOpen();
		// Add into another set to stop ConcurrentModifcationException if we just wrap the keySet in the unmodifiable wrapper 
		HashSet<String> set = new HashSet<String>();
		set.addAll(cachesByName.keySet());
		return Collections.unmodifiableSet(set);
	}

	@Override
	public void destroyCache(String cacheName) {
		checkOpen();
		checkCacheName(cacheName);
		Cache cache = managedCache(cacheName);
		if (cache != null) {
			forgetCache(cacheName);
//			cache.clear();
			cache.close();			
		}
	}

	@Override
	public void enableManagement(String cacheName, boolean enabled) {
		 if (isClosed()) {
		      throw new IllegalStateException();
		    }
	    if (cacheName == null) {
	      throw new NullPointerException();
	    }
	    WrappableCacheWrapper cache = (WrappableCacheWrapper) managedCache(cacheName);
	    cache.setManagementEnabled(enabled);
	}

	@Override
	public void enableStatistics(String cacheName, boolean enabled) {
		 if (isClosed()) {
		      throw new IllegalStateException();
		    }
	    if (cacheName == null) {
	      throw new NullPointerException();
	    }
	    WrappableCacheWrapper hoho_cache = (WrappableCacheWrapper) managedCache(cacheName);
	    hoho_cache.setStatisticsEnabled(enabled);
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		Iterable<String> cacheNames = getCacheNames();
		for (String cacheName : cacheNames) {
			try {
				managedCache(cacheName).close();
			} catch (Throwable t) {
				// as per contract for this method - ignore exceptions on Cache.close()
				t.printStackTrace();
			}
		}
		closed = true;
		heCachingProvider.close(uri, classLoader);
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
		if (clazz.isAssignableFrom(getClass())) {
			return clazz.cast(this);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	private <T> DataAdaptor<byte[], T> dataAdaptor(Class<T> domainType) {
		Class cache_claz = byte[].class;
		DataAdaptor keyAdaptor = null;
		if (String.class.isAssignableFrom(domainType)) {
			keyAdaptor = STRING_DOMAIN_DATA_ADAPTOR;
		} else if (cache_claz.isAssignableFrom(domainType)) {
			keyAdaptor = BYTE_ARRAY_DOMAIN_DATA_ADAPTOR;
		} else {
			keyAdaptor = SERIALIZABLE_DOMAIN_DATA_ADAPTOR;
		}
		return keyAdaptor;
	}
}
