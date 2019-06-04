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

import static com.serisys.helium.jcache.util.ItemManager.DEFAULT_VALUE_BYTE_LENGTH;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.management.ObjectName;


import com.levyx.helium.Helium;
import com.levyx.helium.HeliumException;
import com.levyx.helium.HeliumItem;
import com.levyx.helium.HeliumIterator;
import com.serisys.helium.jcache.events.EventHarness;
import com.serisys.helium.jcache.mx.CacheMXBeanImpl;
import com.serisys.helium.jcache.synch.SynchHandling;
import com.serisys.helium.jcache.synch.ThreadsafeExecution;
import com.serisys.helium.jcache.util.ItemManager;
import com.serisys.helium.jcache.util.MBeanServerRegistrationUtil;
import com.serisys.helium.jcache.util.MBeanServerRegistrationUtil.MBeanType;

public class HeCache<K, V> implements WrappableCache<K, V> {
	public static boolean equals(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		return o2 == null ? false : o1.equals(o2);
	} 
	static byte[] strip(byte[] entry_bytes) {
		byte[] sub_bytes = new byte[entry_bytes.length-8];
		System.arraycopy(entry_bytes, 8, sub_bytes, 0, sub_bytes.length);
		return sub_bytes;	
	}
	static boolean equals_WithMeta_NoMeta(byte[] b1_withMeta, byte[] b2_noMeta) {
		// Java9 - supposedly has a Arrays.compare that could do this
		// https://docs.oracle.com/javase/9/docs/api/java/util/Arrays.html
		int len1 = b1_withMeta.length;
		if (len1 - 8 == b2_noMeta.length) {
			int len2 = b2_noMeta.length;
	        for (int i = 0; i < len2; i++) {
	            if (b1_withMeta[i+8] != b2_noMeta[i]) {
	                return false;
	            }
	        }
	        return true;
		}
		else {
			return false;
		}
	}
	
	static String generateCleanName(HeCacheManager manager, String cacheName) {
		String patt = "[^A-Za-z0-9._:\\\\//]";  // Dollar sign? other odds? 
		String repl = "_";
		String manager_scope = manager.getCMID().replaceAll(patt, repl);
		StringBuilder bob = new StringBuilder();
		bob.append(manager_scope)
			.append("_")
			.append(cacheName);
		return bob.toString();
	}
	
	public static void log(String mess) {
		System.out.println(Thread.currentThread().getName() + " : " + mess);
	}

	private final HeCacheManager manager;
	private final String url;
	private final String cacheName; 
	private final String datastoreName; 
	private final int flags;
	private Helium datastore;
	private final DataAdaptor<byte[], K> keyAdaptor;
	private final DataAndMetaDataAdaptor<byte[], V>  valueAndMetaAdaptor;
	private final CacheLoader<K, V> cacheLoader;
	private CacheWriter<K, V> cacheWriter;
	private final ConsolidatedCacheConfig<K, V> configuration;
	private boolean closed = true; 
	private final EventHarness<K, V> eventListeners = new EventHarness<K, V>(); 
	private final ThreadsafeExecution<K, V> synchHandler; 
	private CacheWrapper<K,V> wrapper;
	private final ExpiryPolicy expiry;
	private final ItemManager itemManager;
	
	public HeCache(HeCacheManager manager, String cacheName, PropertiesHelper properties, DataAdaptor<byte[], K> keyAdaptor, DataAdaptor<byte[], V> valueAdaptor, CacheLoader<K, V> cacheLoader, CacheWriter<K, V> cacheWriter, ConsolidatedCacheConfig<K, V> configuration) {
		this.configuration = configuration;
		this.manager = manager;
		this.url = properties.getDeviceURL(cacheName);
		this.cacheName = cacheName;
		this.datastoreName = generateCleanName(manager, cacheName);
		configureDefaultProperties(properties);
		this.flags = properties.getFlags(cacheName);
		this.datastore = open(flags);
		this.keyAdaptor = keyAdaptor;
		this.valueAndMetaAdaptor = new DataAndMetaDataAdaptor<byte[], V>() {
			private DataAdaptor<byte[], V> valAdaptor = valueAdaptor;

			@Override
			public byte[] convertToCached(V domainObject) {
				return valAdaptor.convertToCached(domainObject);
			}

			@Override
			public V convertToDomain(byte[] cacheEntry) {
				return valAdaptor.convertToDomain(strip(cacheEntry));
			}

			@Override
			public long getMetadata(byte[] cacheEntry) {
		        return (((long)cacheEntry[0] << 56) +
		                ((long)(cacheEntry[1] & 255) << 48) +
		                ((long)(cacheEntry[2] & 255) << 40) +
		                ((long)(cacheEntry[3] & 255) << 32) +
		                ((long)(cacheEntry[4] & 255) << 24) +
		                ((cacheEntry[5] & 255) << 16) +
		                ((cacheEntry[6] & 255) <<  8) +
		                ((cacheEntry[7] & 255) <<  0));
			}

			@Override
			public byte[] convertToCached(byte[] val_bytes, long v) {
				byte[] all_bytes = new byte[val_bytes.length+8];
				all_bytes[0] = (byte)(v >>> 56);
				all_bytes[1] = (byte)(v >>> 48);
				all_bytes[2] = (byte)(v >>> 40);
				all_bytes[3] = (byte)(v >>> 32);
				all_bytes[4] = (byte)(v >>> 24);
				all_bytes[5] = (byte)(v >>> 16);
				all_bytes[6] = (byte)(v >>>  8);
				all_bytes[7] = (byte)(v >>>  0);
				System.arraycopy(val_bytes, 0, all_bytes, 8, val_bytes.length);
				return all_bytes;
			}
			
		};
		this.cacheLoader = cacheLoader;
		this.cacheWriter = new CacheWriterHandler(cacheWriter);
		String synchHandlerClazName = properties.getSynchHandlerClazName();
		this.synchHandler = (ThreadsafeExecution<K, V>) FactoryBuilder.factoryOf(synchHandlerClazName).create();
		wrapper = this;
		this.expiry = configuration.getExpiryPolicyFactory() == null ? 
				new EternalExpiryPolicy() : 
					configuration.getExpiryPolicyFactory().create();
		// register any listeners defined in the configs
		for (CacheEntryListenerConfiguration<K, V> ent : configuration.getCacheEntryListenerConfigurations()) {
			eventListeners.registerCacheEntryListener(ent);
		}
		this.itemManager = configuration.getItemManagerFactory().create();
	}
	
	private Helium open(int modes) {
		if (!closed) {
			throw new IllegalStateException("cache is open");
		}
		HeliumFactory factory = null; 
		if (ConsolidatedCacheConfig.class.isAssignableFrom(configuration.getClass())) {
			ConsolidatedCacheConfig<K,V> ccc = ConsolidatedCacheConfig.class.cast(configuration);
			factory = (HeliumFactory) ccc.getHeliumFactory();
		}
		if (factory == null) {
			factory = new HeliumFactory(); 
		}
		factory.set(url, datastoreName, modes);
		Helium he = factory.create();
		closed = false; 
		return he;
	}
	
	private void checkOpen() {
		if (closed) {
			throw new IllegalStateException("cache is closed");
		}
	}
	
	private void checkNullArg(Object arg) {
		if (arg == null) {
			throw new NullPointerException();
		}
	}

	private void checkValidKey(K key) {
		checkNullArg(key);
	}

	private void checkValidKeys(Collection<? extends K> keys) {
		for (K key : keys) {
			checkValidKey(key);
		}
	}
	
	private void checkValidValue(V value) {
		checkNullArg(value);
	}

	private void checkValidValues(Collection<? extends V> values) {
		for (V value : values) {
			checkValidValue(value);
		}
	}
	
	@Override
	public void clear() {
		checkOpen();
		HeliumIterator he_iterator = datastore.iterator();
		while (he_iterator.hasNext()) {
			HeliumItem item = he_iterator.next();
			he_iterator.remove();
		}
	}
	
	
	@Override
	public void close() {

	    if (!closed) {
	      //ensure that any further access to this Cache will raise an
	      // IllegalStateException
	      closed = true;

	      //ensure that the cache may no longer be accessed via the CacheManager
	      manager.forgetCache(cacheName);

	      //disable management
	      setManagementEnabled(false);
	      // disable statistics done on the wrapper if there is one

	      //close the configured CacheLoader
	      if (cacheLoader instanceof Closeable) {
	        try {
	          ((Closeable) cacheLoader).close();
	        } catch (IOException e) {
	          Logger.getLogger(getName()).log(Level.WARNING, "Problem closing CacheLoader " + cacheLoader.getClass(), e);
	        }
	      }

	      //close the configured CacheWriter
	      if (cacheWriter instanceof Closeable) {
	        try {
	          ((Closeable) cacheWriter).close();
	        } catch (IOException e) {
	          Logger.getLogger(getName()).log(Level.WARNING, "Problem closing CacheWriter " + cacheWriter.getClass(), e);
	        }
	      }

	      //close the configured ExpiryPolicy
	      if (expiry instanceof Closeable) {
	        try {
	          ((Closeable) expiry).close();
	        } catch (IOException e) {
	          Logger.getLogger(getName()).log(Level.WARNING, "Problem closing ExpiryPolicy " + expiry.getClass(), e);
	        }
	      }
	      
	      // close the configured CacheEntryListeners
	      eventListeners.close(getName());

	      datastore.close();
	    }
	}
	
	private void configureDefaultProperties(PropertiesHelper props) {
		if (!props.containsKey(HeCacheProperties.HE_O_CREATE_PROPERTY)) {
			props.setProperty(HeCacheProperties.HE_O_CREATE_PROPERTY, "true");
		}
		if (!props.containsKey(HeCacheProperties.HE_O_TRUNCATE_PROPERTY)) {
			props.setProperty(HeCacheProperties.HE_O_TRUNCATE_PROPERTY, "true");
		}
		if (!props.containsKey(HeCacheProperties.HE_O_VOLUME_CREATE_PROPERTY)) {
			props.setProperty(HeCacheProperties.HE_O_VOLUME_CREATE_PROPERTY, "true");
		}
	}

	@Override
	public boolean containsKey(K key) {
		checkOpen();
		checkValidKey(key);
		CacheEntryBundle bundle = synchHandler.bobserver(key, fn_o_containsKey());
		if (bundle == null) {
			return false;
		} else {
			if (bundle.isPreviousValueExpired()) {
				eventListeners.entryExpired(this, bundle.getKey(), bundle.getValue());
				return false;
			} else {
				return true;
			}
		}
	}

	// containsKey does not update the timestamp
	// but it can expire
	private Function<K,CacheEntryBundle> fn_o_containsKey() {
		return 	key-> {
			byte[] byte_key = keyAdaptor.convertToCached(key);
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key);
				byte[] val_bytes = null;
				try {
					val_bytes = lookup(item);
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						return null;
					default:
						throw new CacheException(hex);
					}
				}
				if (isExpired(val_bytes)) {
					CacheEntryBundle bundle = new ExpiredCacheEntryBundle(key, byte_key, val_bytes);
					synchHandler.updater(bundle, fn_u_expire());
					return bundle;
				}			
				return new CacheEntryBundle(key, null);
			} finally {
				itemManager.release(item);
			}
		};
	}
	
	@Override
	public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
		checkOpen();
		eventListeners.deregisterCacheEntryListener(cacheEntryListenerConfiguration);
		configuration.removeCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);
	}
	@Override
	public V get(K key) {
		checkOpen();
		checkValidKey(key);
		CacheEntryBundle bundle = synchHandler.observer(key, fn_o_get());
		
		V ret = null;
		if (bundle.isPreviousValueExpired()) {
			eventListeners.entryExpired(this, bundle.getKey(), bundle.getValue());
			ret = null;
		} else {
			ret = bundle.getValue();
		}
		//log("**** HeCache.get(Key) key: " + key + " found: " + ret);
		return ret;
	}

	private Function<K,CacheEntryBundle> fn_o_get() {
		return key -> {
			byte[] byte_key = keyAdaptor.convertToCached(key);
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key);
				byte[] val_bytes = null;
				try {
					val_bytes = lookup(item);
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						 return new CacheEntryBundle(key, readThrough(key));
					default:
						throw new CacheException(hex);
					}
				}
				if (isExpired(val_bytes)) {
					CacheEntryBundle bundle = new ExpiredCacheEntryBundle(key, byte_key, val_bytes);
					synchHandler.updater(bundle, fn_u_expire());
					return bundle;
				} else {
					CacheEntryBundle bundle = new ReplacingCacheEntryBundle(key, byte_key, val_bytes, val_bytes);
					if (expiry.getExpiryForAccess() != null) {
						synchHandler.updater(bundle, fn_u_access());
					}
					return bundle;
				}
			} finally { 
				itemManager.release(item);
			}
		};
	}
	
	public ValueHolder getNoExpiryCheck(K key) {
		checkOpen();
		checkValidKey(key);
		byte[] byte_key = keyAdaptor.convertToCached(key);
		HeliumItem item = null;
		try {
			item = newHeliumItem(byte_key);
			byte[] val_bytes = null;
			try {
				val_bytes = lookup(item);
			} catch (HeliumException hex) {
				switch (hex.getErrorCode()) {
				case Helium.HE_ERR_ITEM_NOT_FOUND:
					 return null;
				default:
					throw new CacheException(hex);
				}
			}
			return new ValueHolder(val_bytes);
		} finally {
			itemManager.release(item);
		}
	}
	
	private long now() {
		return System.currentTimeMillis();
	}
	
	protected boolean isExpired(byte[] cache_bytes) {
		return isExpiredAt(cache_bytes, now());
	}
	
	// Only use this with HeItems returned by Helium from its iterator
	// They don't need the multiple stage lookups, compared to those 
	// which we have passed by reference to an API call. 
	private boolean isExpired(HeliumItem item) {
		// this is the right value bytes if we are overflowing
		return isExpiredAt(item.getValueBytes(), now());
	}
	
	private boolean isExpiredAt(byte[] cache_bytes, long epoch_millis_now) {
		long horizon = valueAndMetaAdaptor.getMetadata(cache_bytes);
		return horizon <= epoch_millis_now;
	}
	
	private boolean isImmediatelyExpiredOnCreation() {
		return expiry.getExpiryForCreation().equals(Duration.ZERO);
	}
	
	@Override
	public Map<K, V> getAll(Set<? extends K> keys) {
		checkOpen();
		checkValidKeys(keys);
		Map<K, V> requested = new java.util.HashMap<K, V>();
		for (K key : keys) {
			V value = get(key);
			if (value != null) {
				requested.put(key, value);
			}
		}
		return requested;
	}

	@Override
	public V getAndPut(K key, V value) {
		V prev_value = null;
		try {
			prev_value = wrappedGetAndPut(key, value);
		} catch (CacheNotModified e) {
			// Nothing happens. Only thrown in the case where a value was put for 
			// a hitherto absent key and the expiry policy returns Duration.ZERO 
			// for getExpiryForCreation(). There was no previous value so nothing
			// is affected, nothing is added, removed or updated, and no events
			// will have been queued.
		}
		return prev_value;
	}

	@Override
	public V wrappedGetAndPut(K key, V value) throws CacheNotModified {
		V prev_value = null;
		try {
			prev_value = _getAndPut(key, value);
		} catch (ImmediatelyExpired e) {
			throw new CacheNotModified();
		}
		eventListeners.fireEvents();
		return prev_value;
	}

	private V _getAndPut(K key, V value) {
		checkOpen();
		checkValidKey(key);
		checkValidValue(value);
		CacheEntryBundle bundle = synchHandler.observer(key, value, fn_o_getAndPut());
		V prev_value =  bundle.getPreviousValue();
		// events are dispatched OUTSIDE of any locking
		if (prev_value == null) {
			eventListeners.entryCreated(this, key, value);
		} else {
			if (bundle.isPreviousValueExpired()) {
				eventListeners.entryExpired(this, key, prev_value);
			}
			eventListeners.entryUpdated(this, key, value, prev_value);
		}
		return prev_value;
	}
	
	private byte[] lookup(HeliumItem item) throws HeliumException {
		datastore.lookup(item);
		int val_len = item.getValueLength();
		if (val_len > DEFAULT_VALUE_BYTE_LENGTH) {
			//log("HeCache.lookup_val for value byte length: " + val_len);
			byte[] a = item.getValueBytes();
			datastore.lookup(item, DEFAULT_VALUE_BYTE_LENGTH, val_len-DEFAULT_VALUE_BYTE_LENGTH);
			byte[] b = item.getValueBytes();
		    byte[] c = new byte[item.getValueLength()]; 
		    System.arraycopy(a, 0, c, 0, DEFAULT_VALUE_BYTE_LENGTH);
		    System.arraycopy(b, 0, c, DEFAULT_VALUE_BYTE_LENGTH, val_len-DEFAULT_VALUE_BYTE_LENGTH);
		    return c;
		}
		else {
			return item.getValueBytes();
		}
	}
	private byte[] deleteLookup(HeliumItem item) throws HeliumException {
		datastore.deleteLookup(item);
		int val_len = item.getValueLength();
		if (val_len > DEFAULT_VALUE_BYTE_LENGTH) {
			//log("HeCache.lookup_val for value byte length: " + val_len);
			byte[] a = item.getValueBytes();
			datastore.deleteLookup(item, DEFAULT_VALUE_BYTE_LENGTH, val_len-DEFAULT_VALUE_BYTE_LENGTH);
			byte[] b = item.getValueBytes();
		    byte[] c = new byte[item.getValueLength()]; 
		    System.arraycopy(a, 0, c, 0, DEFAULT_VALUE_BYTE_LENGTH);
		    System.arraycopy(b, 0, c, DEFAULT_VALUE_BYTE_LENGTH, val_len-DEFAULT_VALUE_BYTE_LENGTH);
		    return c;
		}
		else {
			return item.getValueBytes();
		}
	}
	private BiFunction<K,V,HeCache<K,V>.CacheEntryBundle> fn_o_getAndPut() {
		return (key, value) -> {
			byte[] byte_key = keyAdaptor.convertToCached(key);
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key);
				byte[] prev_val_bytes = null;
				try {
					prev_val_bytes = lookup(item);
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						break;
					default:
						throw new CacheException(hex);
					}
				}
				CacheEntryBundle bundle = null;
				Consumer<CacheEntryBundle> updater = null;
				if (prev_val_bytes == null) {
					if (isImmediatelyExpiredOnCreation()) {
						// Immediate expiry - this is such a daft concept 
						// that I have gone for the god-awful bodge of 
						// throwing out an exception. I wouldn't do this
						// for implementing any sensible logic. 
						throw new ImmediatelyExpired();
					}
					bundle = new CacheEntryBundle(key, byte_key, value);
					updater = fn_u_getAndPut_insert();
					synchHandler.updater(bundle, updater);
				} else {
					// You would think we need to check for immediate expiry on 
					// update, but the RI doesn't and the RI tests don't check for
					// this. Therefore I'm not putting it in as the whole immediate
					// expiry business seems like a waste of time. 
					// if (isImmediatelyExpiredOnUpdate())
					bundle = new ReplacingCacheEntryBundle(key, byte_key, value, prev_val_bytes);
					// And this is what I think is the correct behaviour
					/*
					updater =  prev_value.equals(value) ?
									fn_u_access_wt() :
									fn_u_getAndPut_update();
					*/
					// but this is what the spec mandates:
					updater = fn_u_getAndPut_update();
					synchHandler.updater(bundle, updater);
				}
				return bundle;
			} finally {
				itemManager.release(item);
			}
		};
	}
	
	private Consumer<CacheEntryBundle> fn_u_getAndPut_insert() {
		return  (bundle) -> {
			byte[] byte_key = bundle.getCacheKey();
			byte[] byte_value = bundle.getCacheValueForCreation();
			HeliumItem itemu = null;
			try {
				itemu = newHeliumItem(byte_key, byte_value);
				// "throws CacheWriterException - if the write fails. If thrown the cache mutation will not occur."
				cacheWriter.write(new CacheEntryImpl<K,V>(bundle.getKey(), bundle.getValue()));
				datastore.insert(itemu);
			} finally {
				itemManager.release(itemu);
			}
		};
	}
	
	private Consumer<CacheEntryBundle> fn_u_getAndPut_update() {
		return  (bundle) -> {
			byte[] byte_key = bundle.getCacheKey();
			byte[] byte_value = bundle.getCacheValueForUpdate();
			HeliumItem itemu = null;
			try {
				itemu = newHeliumItem(byte_key, byte_value);
				cacheWriter.write(new CacheEntryImpl<K,V>(bundle.getKey(), bundle.getValue()));
				datastore.update(itemu);
			} finally {
				itemManager.release(itemu);
			}
		};
	}

	// updates expiry timestamp only - so not needed if no expiry. 
	// This one does not invoke write-through - just marks cache entry as touched
	private Consumer<CacheEntryBundle> fn_u_access() {
		return  (bundle) -> {
			byte[] byte_key = bundle.getCacheKey();
			byte[] byte_value = bundle.getCacheValueForAccess();
			HeliumItem itemu = null;
			try {
				itemu = newHeliumItem(byte_key, byte_value);
				datastore.update(itemu);
			} finally {
				itemManager.release(itemu);
			}
		};
	}
	
	// only used on readThrough
	protected void _put(K key, V value) {
		synchHandler.updater(new CacheEntryBundle(key, value), fn_u_put());
	}
	
	private Consumer<CacheEntryBundle> fn_u_put() {
		return (bundle) -> {
			byte[] byte_key = bundle.getCacheKey();
			HeliumItem item = null; 
			try {
				item = newHeliumItem(byte_key);
				boolean exists = false;
				byte[] val_bytes = null;
				try {
					val_bytes = lookup(item);
					exists = true;
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						exists = false;
						break;
					default:
						throw new CacheException(hex);
					}
				}
				byte[] byte_value = null;
				if (exists) {
					bundle = new ReplacingCacheEntryBundle(bundle.getKey(), byte_key, bundle.getValue(), val_bytes);
					byte_value = bundle.getCacheValueForUpdate();
				} else { 
					byte_value = bundle.getCacheValueForCreation();
				}
				//item = newHeliumItem(byte_key, byte_value);
				item.setKeyLength(byte_key.length);
				item.setValueLength(byte_value.length);
				item.setKeyBytes(byte_key);
				item.setValueBytes(byte_value);
				datastore.update(item);
			} finally {
				itemManager.release(item);
			}
		};
		
	}

	@Override
	public V getAndRemove(K key) {
		checkOpen();
		checkValidKey(key);
		byte[] byte_key = keyAdaptor.convertToCached(key);
		HeliumItem item = null;
		try {
			item = newHeliumItem(byte_key);
			ValueHolder old_cached_holder = synchHandler.updater(key, fn_u_getAndRemove()); 
			if (old_cached_holder == null) {
				return null;
			}
			V old_cached = old_cached_holder.getValue();
			// events are dispatched OUTSIDE of any locking
			eventListeners.entryRemoved(this, key, old_cached);
			eventListeners.fireEvents();
			return old_cached;
		} finally {
			itemManager.release(item);
		}
	}
	
	private Function<K,ValueHolder> fn_u_getAndRemove() {
		return key -> {
			byte[] byte_key = keyAdaptor.convertToCached(key);
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key);
				cacheWriter.delete(key);
				try {
					byte[] val_bytes = deleteLookup(item);
					return new ValueHolder(val_bytes);
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						return null;
					default:
						throw new CacheException(hex);
					}
				}
			} finally {
				itemManager.release(item);
			}
		};
	}

	@Override
	public V getAndReplace(K key, V value) {
		checkOpen();
		checkValidKey(key);
		checkValidValue(value);
		BiFunction<K,V,CacheEntryBundle> observer = fn_o_getAndReplace();
		CacheEntryBundle bundle = synchHandler.observer(key, value, observer);
		V old_cached = bundle == null ? null : bundle.getPreviousValue();
		if (old_cached != null) {
			// events are dispatched OUTSIDE of any locking
			eventListeners.entryUpdated(this, key, value, old_cached);
			eventListeners.fireEvents();
		}
		return old_cached; 
	}
	
	private BiFunction<K,V,CacheEntryBundle> fn_o_getAndReplace() {
		return (key,value) -> {
			HeliumItem item =  null;
			try {
				byte[] byte_key = keyAdaptor.convertToCached(key);
				item = newHeliumItem(byte_key);
				byte[] old_cached_bytes = lookup(item);
				Consumer<CacheEntryBundle> updater = fn_u_getAndReplace();
				CacheEntryBundle bundle = new ReplacingCacheEntryBundle(key, byte_key, value, old_cached_bytes);
				synchHandler.updater(bundle, updater);
				return bundle;
			} catch (HeliumException hex) {
				switch (hex.getErrorCode()) {
				case Helium.HE_ERR_ITEM_NOT_FOUND:
					return null;
				default:
					throw new CacheException(hex);
				}
			} finally {
				if (item != null) {
					itemManager.release(item);
				}
			}
		};
	}
	
	private Consumer<CacheEntryBundle> fn_u_getAndReplace() {
		return (bundle) -> {
			byte[] byte_key = bundle.getCacheKey();
			byte[] byte_value = bundle.getCacheValueForUpdate();
			HeliumItem itemu = null;
			try {
				itemu = newHeliumItem(byte_key, byte_value);
				cacheWriter.write(new CacheEntryImpl<K,V>(bundle.getKey(), bundle.getValue()));
				datastore.replace(itemu);
			} finally {
				itemManager.release(itemu);
			}
		};
	}

	@Override
	public CacheManager getCacheManager() {
		return manager;
	}

	@Override
	public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
		if (MutableConfiguration.class.isAssignableFrom(clazz)) {
			return (C) configuration;
		} else if (clazz.isAssignableFrom(ConsolidatedCacheConfig.class)) {
			ConsolidatedCacheConfig<K, V> con = new ConsolidatedCacheConfig<K, V>(configuration);
			return clazz.cast(con);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String getName() {
		return cacheName;
	}

	@Override
	public <T> T invoke(final K key, final EntryProcessor<K, V, T> entryProcessor, final Object... arguments)
			throws EntryProcessorException {
		if (entryProcessor == null) {
			throw new NullPointerException();
		}
		return synchHandler.observe(key, new EntryProcessorInvocation<K,V,T>(entryProcessor, arguments), fn_o_invoke());
	}
	
	private <T> BiFunction<K, EntryProcessorInvocation<K, V, T>, T> fn_o_invoke() {
		return (key, invoc) -> {
			ValueHolder value_holder = wrapper.getNoExpiryCheck(key);
			MutableHeCacheForwardGotEntry<K,V> mute = new MutableHeCacheForwardGotEntry<K, V>(key, value_holder, wrapper, this, cacheLoader);
			T tea = null;
			try {
				tea = (T) invoc.processor.process(mute, invoc.arguments);
				synchHandler.updater(key, mute, fn_u_invoke());
			} catch (Exception e) {
				throw new EntryProcessorException(e);
			}
			return tea;
		};
	}
	
	private Consumer<MutableHeCacheForwardGotEntry<K,V>> fn_u_invoke() {
		return mute -> {
			mute.apply();
		};
	}

	@Override
	public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor,
			Object... arguments) {
		checkOpen();
		checkNullArg(keys);
		checkNullArg(entryProcessor);
		Map<K, EntryProcessorResult<T>> results = new java.util.HashMap<K, EntryProcessorResult<T>>(keys.size());
		for (K key : keys) {
			// this method sits "outside" the cache wrt stats etc so we invoke the get through the wrapper. 
			T tea = null;
			EntryProcessorException mpe = null;
			// if key is null then we can't apply a key-level lock so just plough on ...
			if (key == null) {
				MutableHeCacheForwardGotEntry<K,V> mute = new MutableHeCacheForwardGotEntry<K, V>(null, null, wrapper, this, cacheLoader);
				try {
					tea = (T) entryProcessor.process(mute, arguments);
					mute.apply();
				} catch (Exception e) {
					mpe = new EntryProcessorException(e);
				}
			} else {
				try {
					tea = synchHandler.observe(key, new EntryProcessorInvocation<K, V, T>(entryProcessor, arguments), fn_o_invoke());
				} catch (EntryProcessorException e) {
					mpe = e;
				} catch (Exception e) {
					mpe = new EntryProcessorException(e);
				}
			}
			EntryProcessorResult<T> result = mpe == null ?
				tea == null ? null : new EntryProcessorResultImpl<T>(tea) : 
				new EntryProcessorExceptionNoResult<T>(mpe);
			if (result != null) {
				results.put(key, result);
			}
		}
		return results;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		checkOpen();
		final HeliumIterator he_iterator = datastore.iterator();
		Iterator<Entry<K,V>> iterator = new HeCacheLookAheadIterator(he_iterator);
		return iterator;
	}

	@Override
	public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
		checkOpen();
		checkNullArg(keys);
		if (keys.contains(null)) {
			throw new NullPointerException("Invalid null key");
		}
		if (completionListener == null) {
			completionListener = new CompletionListener() {
				@Override
				public void onException(Exception e) {}				
				@Override
				public void onCompletion() {}
			};
		}
		try {
			readThrough(keys, replaceExistingValues);
			completionListener.onCompletion();
		} catch (Exception e) {
			completionListener.onException(new CacheLoaderException(e));
		}
	}

	@Override
	public void put(K key, V value) {
		try {
			wrappedPut(key, value);
		} catch (CacheNotModified e) {
			// nothing to do
		}
	}
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		try {
			wrappedPutAll(map);
		} catch (CacheNotModified e) {
			// nothing to do
		}
	}

	@Override
	public boolean putIfAbsent(K key, V value) {
		checkOpen();
		checkValidKey(key);
		checkValidValue(value);
		boolean updated;
		try {
			updated = synchHandler.bobserver(key, value, fn_o_putIfAbsent());
			if (updated) {
				// events are dispatched OUTSIDE of any locking
				eventListeners.entryCreated(this, key, value);
			}
		} catch (ImmediatelyExpired e) {
			// fire no events
			// what do we return? 
			updated = false;
		}
		eventListeners.fireEvents();
		return updated;
	}
	
	private BiFunction<K, V, Boolean> fn_o_putIfAbsent() {
		return (key, value) -> {
			byte[] byte_key = keyAdaptor.convertToCached(key);
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key);
				byte[] val_bytes;
				try {
					val_bytes = lookup(item);
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						if (isImmediatelyExpiredOnCreation()) {
							throw new ImmediatelyExpired();
						}
						CacheEntryBundle bundle = new CacheEntryBundle(key, byte_key, value);
						return synchHandler.updater(bundle, fn_u_putIfAbsent());
					default:
						throw new CacheException(hex);
					}
				}
				if (isExpired(val_bytes)) {
					if (isImmediatelyExpiredOnCreation()) {
						throw new ImmediatelyExpired();
					}
					CacheEntryBundle bundle = new CacheEntryBundle(key, byte_key, value);
					return synchHandler.updater(bundle, fn_u_putIfAbsent_exp());
				}
				return false;
			} finally {
				itemManager.release(item);
			}
		};
	}
	
	private Function<CacheEntryBundle,Boolean> fn_u_putIfAbsent() {
		return (bundle) -> {
			byte[] byte_key = bundle.getCacheKey();
			byte[] byte_value = bundle.getCacheValueForCreation();
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key, byte_value);
				cacheWriter.write(new CacheEntryImpl<K,V>(bundle.getKey(), bundle.getValue()));
				datastore.insert(item);
				return true;
			} catch (HeliumException next_hex) {
				switch (next_hex.getErrorCode()) {
				case Helium.HE_ERR_ITEM_EXISTS:
					return false;
				default:
					throw new CacheException(next_hex);
				}
			} finally {
				itemManager.release(item);
			}
		};
	}

	private Function<CacheEntryBundle,Boolean> fn_u_putIfAbsent_exp() {
		return (bundle) -> {
			byte[] byte_key = bundle.getCacheKey();
			byte[] byte_value = bundle.getCacheValueForCreation();
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key, byte_value);
				cacheWriter.write(new CacheEntryImpl<K,V>(bundle.getKey(), bundle.getValue()));
				datastore.update(item);
				return true;
			} catch (HeliumException next_hex) {
				throw new CacheException(next_hex);
			} finally {
				itemManager.release(item);
			}
		};
	}

	@Override
	public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
		checkOpen();
		configuration.addCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);
		eventListeners.registerCacheEntryListener(cacheEntryListenerConfiguration);
	}

	@Override
	public boolean remove(K key) {
		checkOpen();
		checkValidKey(key);
		ValueHolder del_value_holder = synchHandler.updater(key, fn_u_removeK());
		if (del_value_holder == null) {
			return false;
		}
		V del_value = del_value_holder.getValue();
		// events are dispatched OUTSIDE of any locking
		eventListeners.entryRemoved(this, key, del_value);
		eventListeners.fireEvents();
		return true;
	}
	
	private Function<K,ValueHolder> fn_u_removeK() {
		return key -> {
			byte[] byte_key = keyAdaptor.convertToCached(key);
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key);
				byte[] val_bytes = null;
				try {
					cacheWriter.delete(key);
					val_bytes = deleteLookup(item);
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						return null;
					default:
						throw new CacheException(hex);
					}
				}		
				if (isExpired(val_bytes)) {
					return null;
				} else {
					return new ValueHolder(val_bytes);
				}
			} finally {
				itemManager.release(item);
			}
		};
	}

	/*
	 * Process an expiry:
	 * - no write through
	 * - send an event - must be done outside locking so CHECK WHERE YOU ARE USING this!
	 */
	private Consumer<CacheEntryBundle> fn_u_expire() {
		return bundle -> {
			byte[] byte_key = bundle.getCacheKey();
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key);
				try {
					datastore.delete(item);
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						// well i don't know, it was there a moment ago. 
						break;
					default:
						throw new CacheException(hex);
					}
				}
			} finally {
				itemManager.release(item);
			}
		};
	}

	/*
	 * Process an expiry:
	 * - no write through
	 * - send an event - must be done outside locking so CHECK WHERE YOU ARE USING this!
	 */
	private Consumer<HeliumIterator> fn_u_it_expire() {
		return iterator -> {
			try {
				iterator.remove();
			} catch (HeliumException hex) {
				switch (hex.getErrorCode()) {
				case Helium.HE_ERR_ITEM_NOT_FOUND:
					// well i don't know, it was there a moment ago. 
					break;
				default:
					throw new CacheException(hex);
				}
			}
		};
	}

	@Override
	public boolean remove(K key, V oldValue) {
		checkOpen();
		checkValidKey(key);
		checkValidValue(oldValue);
		byte[] old_val_bytes = valueAndMetaAdaptor.convertToCached(oldValue);
		boolean removed = synchHandler.bobserver(key, old_val_bytes, fn_o_removeKV());
		if (removed) {
			// events are dispatched OUTSIDE of any locking
			eventListeners.entryRemoved(this, key, oldValue);
			eventListeners.fireEvents();
		}
		return removed;
	}
	
	private BiFunction<K,byte[],Boolean> fn_o_removeKV() {
		return (key, oldValue) -> {
			return synchHandler.updater(key, oldValue, fn_u_removeKV());
		};
	}
	
	private BiFunction<K,byte[],Boolean> fn_u_removeKV() {
		return (key, oldValue_bytes) -> {
			byte[] byte_key = keyAdaptor.convertToCached(key);
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key);
				try {
					byte[] old_cached_bytes = lookup(item);
					if (isExpired(old_cached_bytes)) {
						datastore.delete(item);
						return false; 
					} else if (equals_WithMeta_NoMeta(old_cached_bytes, oldValue_bytes)) {
						cacheWriter.delete(key);
						datastore.delete(item);
						return true;
					} else {
						if (expiry.getExpiryForAccess() != null) {
							synchHandler.updater(new ReplacingCacheEntryBundle(key, byte_key, old_cached_bytes, old_cached_bytes), fn_u_access());
						}
						return false;
					}
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						return false;
					default:
						throw new CacheException(hex);
					}
				}
			} finally {
				itemManager.release(item);
			}
		};
	}

	@Override
	public void removeAll() {
		checkOpen();
		HeliumIterator he_iterator = datastore.iterator();
		List<K> to_remove = new java.util.ArrayList<K>();
		while (he_iterator.hasNext()) {
			HeliumItem item = he_iterator.next();
			K key = keyAdaptor.convertToDomain(item.getKeyBytes());
			to_remove.add(key);
		}
		if (!to_remove.isEmpty()) {
			_removeAll(to_remove);
		}
	}

	private void _removeAll(Collection<? extends K> to_remove) {
		CacheWriterException maybe = null;
		Set<K> writer_remove = new java.util.HashSet<K>();
		writer_remove.addAll(to_remove);
		// write-through
		try {
			cacheWriter.deleteAll(writer_remove);
		} catch (CacheWriterException e) {
			// indicates that a partial remove has occurred.
			maybe = e;
		} catch (Throwable t) {
			maybe = new CacheWriterException(t);
		} 
		
		// to_remove will now be empty (if cache writer successfully removed everything)
		// or will contain residual keys which the cache writer was for some reason 
		// unable to remove. Such keys may not be removed from the cache. 
		for (K key : to_remove) { 
			if (!writer_remove.contains(key)) {
				ValueHolder value_holder = synchHandler.updater(key, fn_u_removeAll());
				if (value_holder != null) {
					// events are dispatched OUTSIDE of any locking
					eventListeners.entryRemoved(this, key, value_holder.getValue());
				}
			}
		}
		eventListeners.fireEvents();
		
		if (maybe != null) {
			throw maybe;
		}
	}
	
	private Function<K,ValueHolder> fn_u_removeAll() {
		return key -> {
			byte[] byte_key = keyAdaptor.convertToCached(key);
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key);
				try {
					byte[] val_bytes = deleteLookup(item);
					return new ValueHolder(val_bytes);
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						break;
					default:
						throw new CacheException(hex);
					}
				}
				return null;
			} finally {
				itemManager.release(item);
			}
		};
	}

	@Override
	public void removeAll(Set<? extends K> keys) {
		checkOpen();
		checkValidKeys(keys);
		_removeAll(keys);
	}

	@Override
	public boolean replace(K key, V value) {
		checkOpen();
		checkValidKey(key);
		checkValidValue(value);
		CacheEntryBundle bundle = synchHandler.observer(key, value, fn_o_replace());
		if (bundle == null) {
			return false;
		} else {
			if (bundle.isPreviousValueExpired()) {
				eventListeners.entryExpired(this, key, bundle.getPreviousValue());
				return false;
			} else {
				V old_value = bundle.getPreviousValue();
				eventListeners.entryUpdated(this, key, value, old_value);
				eventListeners.fireEvents();
				return true;
			}
		}
	}
	
	private BiFunction<K,V,CacheEntryBundle> fn_o_replace() {
		return (key, value) -> {
			byte[] byte_key = keyAdaptor.convertToCached(key);
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key);
				try {
					byte[] old_value_bytes = lookup(item);
					CacheEntryBundle bundle = null;
					if (isExpired(old_value_bytes)) {
						bundle = new ExpiredCacheEntryBundle(key, byte_key, old_value_bytes);
						synchHandler.updater(bundle, fn_u_expire());
					} else {
						bundle = new ReplacingCacheEntryBundle(key, byte_key, value, old_value_bytes);
						synchHandler.updater(bundle, fn_u_replace());
					}
					return bundle;
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						return null;
					default:
						throw new CacheException(hex);
					}
				}
			} finally {
				itemManager.release(item);
			}
		};
	}
	
	private Consumer<CacheEntryBundle> fn_u_replace() {
		return (bundle) -> {
			cacheWriter.write(new CacheEntryImpl<K,V>(bundle.getKey(), bundle.getValue()));
			byte[] byte_key = bundle.getCacheKey();
			byte[] byte_value = bundle.getCacheValueForUpdate();
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key, byte_value);
				datastore.replace(item);
			} finally {
				itemManager.release(item);
			}
		};
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		checkOpen();
		checkValidKey(key);
		checkValidValue(oldValue);
		checkValidValue(newValue);
		byte[] old_val_bytes = valueAndMetaAdaptor.convertToCached(oldValue);
		CacheEntryBundle bundle = synchHandler.observer(key, old_val_bytes, newValue, fn_o_replaceKVV());
		if (bundle == null) {
			return false; 
		} else {
			if (bundle.isPreviousValueExpired()) {
				eventListeners.entryExpired(this, bundle.getKey(), bundle.getValue());
				return false;
			} else {
				// events are dispatched OUTSIDE of any locking
				eventListeners.entryUpdated(this, key, newValue, oldValue);
				eventListeners.fireEvents();
				return true;
			}
		}
	}
	
	private BiFunction<Cache.Entry<K, byte[]>, V, CacheEntryBundle> fn_o_replaceKVV() {
		return (keyToOldValue, newValue) -> {
			K key = keyToOldValue.getKey();
			byte[] old_value_bytes = keyToOldValue.getValue();
			byte[] byte_key = keyAdaptor.convertToCached(key);
			HeliumItem item = null;
			try {
				item = newHeliumItem(byte_key);
				try {
					byte[] old_cached_bytes = lookup(item);
					if (isExpired(old_cached_bytes)) {
						CacheEntryBundle bundle = new ExpiredCacheEntryBundle(key, byte_key, old_cached_bytes);
						synchHandler.updater(bundle, fn_u_expire());
						return bundle;
					}
					if (equals_WithMeta_NoMeta(old_cached_bytes, old_value_bytes)) {
						CacheEntryBundle bundle = new ReplacingCacheEntryBundle(key, byte_key, newValue, old_cached_bytes);
						synchHandler.updater(bundle, fn_u_replaceKVV());
						return bundle;
					} else {
						if (expiry.getExpiryForAccess() != null) {
							synchHandler.updater(new ReplacingCacheEntryBundle(key, byte_key, old_cached_bytes, old_cached_bytes), fn_u_access());
						}
						return null;
					}
				} catch (HeliumException hex) {
					switch (hex.getErrorCode()) {
					case Helium.HE_ERR_ITEM_NOT_FOUND:
						return null;
					default:
						throw new CacheException(hex);
					}
				}
			} finally {
				itemManager.release(item);
			}
		};
	}
	
	private Consumer<CacheEntryBundle> fn_u_replaceKVV() {
		return (bundle) -> {
			cacheWriter.write(new CacheEntryImpl<K,V>(bundle.getKey(), bundle.getValue()));
			byte[] byte_key = bundle.getCacheKey();
			byte[] byte_value = bundle.getCacheValueForUpdate();
			HeliumItem item = null;
			try { 
				item = newHeliumItem(byte_key, byte_value);
				datastore.replace(item);
			} finally {
				itemManager.release(item);
			}
		};
	}
	
	@Override
	public <T> T unwrap(Class<T> clazz) {
		if (clazz.isAssignableFrom(HeCache.class)) { 
			return clazz.cast(this);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private V readThrough(K key) {
		V value = null;
		try {
			value = cacheLoader.load(key);
		} catch (Throwable t) { 
			throw new CacheLoaderException(t);
		}
		if (value != null) {
			_put(key, value);
		}
		return value;
	}

	private void readThrough(Set<? extends K> keys, boolean replaceExistingValues) {
		Set<? extends K> to_load = null;
		if (replaceExistingValues) {
			// force load of all
			to_load = keys;
		} else {
			// reject loaded keys
			to_load = keys.stream()
				.filter(key -> !this.containsKey(key))
				.collect(Collectors.toSet());
		}
		Map<K,V> kvs = null;
		try {
			kvs = cacheLoader.loadAll(to_load);
		} catch (Throwable t) {
			throw new CacheLoaderException(t);
		}
		for (Map.Entry<K, V> kv : kvs.entrySet()) {
			K key = kv.getKey();
			V value = kv.getValue();
			if (key != null && value != null) {
				_put(key, value);
			}
		}
	}

	final class HeCacheLookAheadIterator implements Iterator<Entry<K,V>> {
		final HeliumIterator he_iterator;
		Entry<K,V> current;
		HeliumItem curr_item = null;
		HeliumItem next_item = null;
		HeCacheLookAheadIterator(HeliumIterator he_iterator) {
			this.he_iterator = he_iterator;
		}
		@Override
		public boolean hasNext() {
			while (next_item == null && he_iterator.hasNext()) {
				HeliumItem item = he_iterator.next();
				if (isExpired(item)) {
					byte[] key_bytes = item.getKeyBytes();
					K key = keyAdaptor.convertToDomain(key_bytes);
					V valueToUse = valueAndMetaAdaptor.convertToDomain(item.getValueBytes());
					synchHandler.observerUpdateIterator(key, he_iterator, fn_u_it_expire());
					eventListeners.entryExpired(HeCache.this, key, valueToUse);					
				} else {
					next_item = item;
				}
			}
			return next_item != null;
		}
		@Override
		public Entry<K, V> next() {
			hasNext();
			curr_item = next_item;
			next_item = null;
			if (curr_item == null) {
				throw new NoSuchElementException();
			}
			final K key = keyAdaptor.convertToDomain(curr_item.getKeyBytes());
			final V value = valueAndMetaAdaptor.convertToDomain(curr_item.getValueBytes());
			synchHandler.observer(key, value, fn_o_iterate_next());
			current = new CacheEntryImpl<K,V>(key, value);
			return current;
		}
		
		@Override
		public void remove() {
			if (current == null) {
				throw new IllegalStateException();
			}
			synchHandler.observerUpdater(current.getKey(), _remove());
			eventListeners.entryRemoved(HeCache.this, current.getKey(), current.getValue());
			eventListeners.fireEvents();
		}
		
		private Consumer<K> _remove() {
			return key -> {
				cacheWriter.delete(key);
				he_iterator.remove();
			};
		}
	}
	
	final class HeCacheIterator implements Iterator<Entry<K,V>> {
		final HeliumIterator he_iterator;
		HeliumItem item; 
		Entry<K,V> current;
		public HeCacheIterator(HeliumIterator he_iterator) {
			this.he_iterator = he_iterator;
		}

		@Override
		public boolean hasNext() {
			return he_iterator.hasNext();
		}

		@Override
		public Entry<K, V> next() {
			HeliumItem item = he_iterator.next();
			final K key = keyAdaptor.convertToDomain(item.getKeyBytes());
			final V value = valueAndMetaAdaptor.convertToDomain(item.getValueBytes());
			synchHandler.observer(key, value, fn_o_iterate_next());
			current = new CacheEntryImpl<K,V>(key, value);
			return current;
		}
		@Override
		public void remove() {
			he_iterator.remove();
			eventListeners.entryRemoved(HeCache.this, current.getKey(), current.getValue());
			eventListeners.fireEvents();
		}

	}
	
	private BiFunction<K,V,CacheEntryBundle> fn_o_iterate_next() {
		return (key, value) -> {
			update_exp_for_access(key, value);
			return null;
		};
	}
	
	
	protected void update_exp_for_access(K key, V value) {
		if (expiry.getExpiryForAccess() != null) {
			synchHandler.updater(new CacheEntryBundle(key, value), fn_u_access());
		}
	}

	public SynchHandling getSynchHandlingLevel() {
		return synchHandler.getSynchHandlingLevel();
	}

	public void beWrapped(CacheWrapper<K,V> wrapping_cache) {
		this.wrapper = wrapping_cache;
	}

	public void setManagementEnabled(boolean enabled) {
		ObjectName objectName = MBeanServerRegistrationUtil.objectNameFor(this, MBeanType.CacheConfiguration);
		if (enabled) {
			MBeanServerRegistrationUtil.registerMBean(new CacheMXBeanImpl(objectName, configuration), objectName);
		}
		else {
			MBeanServerRegistrationUtil.unregisterMBean(objectName);
		}
		configuration.setManagementEnabled(enabled);
	}
	
	@Override
	public void wrappedPut(K key, V value) throws CacheNotModified {
		try {
			_getAndPut(key, value);
		} catch (ImmediatelyExpired e) {
			throw new CacheNotModified();
		}
		eventListeners.fireEvents();
	}
	
	@Override
	public void wrappedPutAll(Map<? extends K, ? extends V> map) throws KeysNotModified {
		checkOpen();
		checkValidKeys(map.keySet());
		checkValidValues(map.values());
		CacheWriterException maybe = null;
		Collection<Entry<? extends K, ? extends V>> writer_writes = new java.util.HashSet<Entry<? extends K, ? extends V>>();
		for (Map.Entry<? extends K, ? extends V> item_pair : map.entrySet()) {
			writer_writes.add(new CacheEntryImpl<K,V>(item_pair.getKey(), item_pair.getValue()));
		}
		try {
			cacheWriter.writeAll(writer_writes);
		} catch (CacheWriterException e) {
			maybe = e;
		} catch (Throwable t) {
			maybe = new CacheWriterException(t);
		}
		Map<K,V> to_write = new java.util.HashMap<K,V>();
		to_write.putAll(map);
		for (Entry<? extends K, ? extends V> writer_unwrit : writer_writes) { 
			to_write.remove(writer_unwrit.getKey());
		}
		CacheWriter my_cacheWriter = cacheWriter;
		// swizzle in the null writer whilst we do the individual puts ...
		cacheWriter = NoWriteThroughCacheWriter.SINGLETON;
		List<K> not_put = new java.util.ArrayList<K>();
		try {
			for (Map.Entry<? extends K, ? extends V> item_pair : to_write.entrySet()) {
				K key = item_pair.getKey();
				V value = item_pair.getValue();
				try {
					_getAndPut(key, value);
				} catch (ImmediatelyExpired e) {
					// Nothing happens. Only thrown in the case where a value was put for 
					// a hitherto absent key and the expiry policy returns Duration.ZERO 
					// for getExpiryForCreation(). There was no previous value so nothing
					// is affected, nothing is added, removed or updated, and no events
					// will have been queued.
					not_put.add(key);
				} 
			}
		} finally {
			cacheWriter = my_cacheWriter;
		}
		eventListeners.fireEvents();
		if (maybe != null) {
			throw maybe;
		}
		if (!not_put.isEmpty()) {
			throw new KeysNotModified(not_put);
		}
	}

	public class ValueHolder {
		
		private byte[] value_bytes;
		V value_to_use;
		
		ValueHolder(byte[] value_bytes) {
			this.value_bytes = value_bytes;	
		}
		ValueHolder(V value) {
			this.value_to_use = value;	
		}		
		
		V getValue() {
			if (value_to_use == null) {
				if (value_bytes == null) {
					return null;
				}
				value_to_use = valueAndMetaAdaptor.convertToDomain(value_bytes);
			}
			return value_to_use;
		}
		byte[] getValueBytes() {
			if (value_bytes == null) {
				if (value_to_use == null) {
					throw new IllegalArgumentException("One of value_bytes or value_to_use must be supplied");
				}
				value_bytes = valueAndMetaAdaptor.convertToCached(value_to_use);
			}
			return value_bytes;
		}
		byte[] getValueBytes_NoMeta() {
			if (value_bytes == null) {
				if (value_to_use == null) {
					throw new IllegalArgumentException("One of value_bytes or value_to_use must be supplied");
				}
				return valueAndMetaAdaptor.convertToCached(value_to_use);
			}
			return strip(value_bytes);
		}
	}
		
	public class CacheEntryBundle {
		private final K key;
		private final byte[] keyBytes;
		private ValueHolder value;
		
		CacheEntryBundle(K existingKey, V valueToUse) {
			this(existingKey, keyAdaptor.convertToCached(existingKey), valueToUse);
		}
		
		CacheEntryBundle(K existingKey, byte[] existingKeyBytes, V valueToUse) {
			this.key = existingKey;
			this.keyBytes = existingKeyBytes;
			this.value = new ValueHolder(valueToUse);
		}
		CacheEntryBundle(K existingKey, byte[] existingKeyBytes, byte[] value_bytes) {
			this.key = existingKey;
			this.keyBytes = existingKeyBytes;
			this.value = new ValueHolder(value_bytes);
		}
		
		public final byte[] getCacheKey() {
			return keyBytes;
		}
		
		public final byte[] getCacheValueForAccess() {
			return valueAndMetaAdaptor.convertToCached(value.getValueBytes_NoMeta(), accessTimestamp());
		}
		
		public final byte[] getCacheValueForCreation() {
			return valueAndMetaAdaptor.convertToCached(value.getValueBytes_NoMeta(), creationTimestamp());
		}
		
		public final byte[] getCacheValueForUpdate() {
			return valueAndMetaAdaptor.convertToCached(value.getValueBytes_NoMeta(), updateTimestamp());
		}
		
		public final K getKey() {
			return key;
		}
		
		public final V getValue() {
			return value.getValue();
		}

		protected long accessTimestamp() {
			Duration dur = expiry.getExpiryForAccess();
			return dur == null ? 
				creationTimestamp() :
				dur.getAdjustedTime(System.currentTimeMillis());
		}

		protected long creationTimestamp() {
			Duration dur = expiry.getExpiryForCreation();
			return dur.getAdjustedTime(System.currentTimeMillis());
		}
		
		protected long updateTimestamp() {
			Duration dur = expiry.getExpiryForUpdate();
			return dur == null ? 
				creationTimestamp() :
				dur.getAdjustedTime(System.currentTimeMillis());
		}
		
		public boolean isPreviousValueExpired() {
			return false; 
		}
		
		public V getPreviousValue() {
			return null;
		}
	}
	
	class ExpiredCacheEntryBundle extends CacheEntryBundle {
		ExpiredCacheEntryBundle(K existingKey, byte[] existingKeyBytes, byte[] value_bytes) {
			super(existingKey, existingKeyBytes, value_bytes);
		}
//		ExpiredCacheEntryBundle(K existingKey, byte[] existingKeyBytes, byte[] value_bytes) {
//			super(existingKey, existingKeyBytes, value_bytes);
//		}
		
		@Override
		public boolean isPreviousValueExpired() {
			return true;
		}
		
		@Override
		public V getPreviousValue() {
			return super.getValue();
		}
	}
	
	class ReplacingCacheEntryBundle extends CacheEntryBundle {
		private final ValueHolder previous;

		ReplacingCacheEntryBundle(K existingKey, byte[] existingKeyBytes, V valueToUse, byte[] previous_value_bytes) {
			super(existingKey, existingKeyBytes, valueToUse);			
			this.previous = new ValueHolder(previous_value_bytes);
		}
		ReplacingCacheEntryBundle(K existingKey, byte[] existingKeyBytes, byte[] value_bytes, byte[] previous_value_bytes) {
			super(existingKey, existingKeyBytes, value_bytes);			
			this.previous = new ValueHolder(previous_value_bytes);
		}

		@Override
		protected long creationTimestamp() {
			Duration dur = expiry.getExpiryForCreation();
			return dur == null ? 
				valueAndMetaAdaptor.getMetadata(previous.getValueBytes()) :
				dur.getAdjustedTime(System.currentTimeMillis());
		}
		@Override
		protected long updateTimestamp() {
			Duration dur = expiry.getExpiryForUpdate();
			return dur == null ? 
				valueAndMetaAdaptor.getMetadata(previous.getValueBytes()) :
				dur.getAdjustedTime(System.currentTimeMillis());
		}
		@Override
		protected long accessTimestamp() {
			Duration dur = expiry.getExpiryForAccess();
			return dur == null ? 
				valueAndMetaAdaptor.getMetadata(previous.getValueBytes()) :
				dur.getAdjustedTime(System.currentTimeMillis());
		}
		
		@Override
		public boolean isPreviousValueExpired() {
			return isExpired(previous.getValueBytes());
		}
		
		@Override
		public V getPreviousValue() {
			return previous.getValue();
		}
	}
	
	public class EntryProcessorInvocation<K,V,T> {
		final EntryProcessor<K,V,T> processor;
		final Object[] arguments; 
		EntryProcessorInvocation(EntryProcessor<K,V,T> processor, Object... arguments) {
			this.processor = processor;
			this.arguments = arguments;
		}
	}

	
	private HeliumItem newHeliumItem(byte[] byte_key, byte[] byte_value) {
		HeliumItem item = itemManager.take();
		item.setKeyLength(byte_key.length);
		item.setValueLength(byte_value.length);
		item.setKeyBytes(byte_key);
		item.setValueBytes(byte_value);
		return item;
	}
	
	private HeliumItem newHeliumItem(byte[] byte_key) {
		HeliumItem item = itemManager.take();
		item.setKeyLength(byte_key.length);
		item.setKeyBytes(byte_key);
		item.setValueLength(DEFAULT_VALUE_BYTE_LENGTH);
		return item;
	}
}
