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

package com.serisys.helium.jcache.events;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.cache.CacheException;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.event.EventType;

import com.serisys.helium.jcache.HeCache;

public class EventHarness<K,V> {
	private final Map<CacheEntryListenerConfiguration<K, V>, CacheEntryCreated<K,V>> c_listeners = new java.util.HashMap<CacheEntryListenerConfiguration<K, V>, CacheEntryCreated<K,V>>(); 
	private final Map<CacheEntryListenerConfiguration<K, V>, CacheEntryExpired<K,V>> e_listeners = new java.util.HashMap<CacheEntryListenerConfiguration<K, V>, CacheEntryExpired<K,V>>(); 
	private final Map<CacheEntryListenerConfiguration<K, V>, CacheEntryRemoved<K,V>> r_listeners = new java.util.HashMap<CacheEntryListenerConfiguration<K, V>, CacheEntryRemoved<K,V>>(); 
	private final Map<CacheEntryListenerConfiguration<K, V>, CacheEntryUpdated<K,V>> u_listeners = new java.util.HashMap<CacheEntryListenerConfiguration<K, V>, CacheEntryUpdated<K,V>>(); 
	private final ExecutorService firingThreads = Executors.newFixedThreadPool(3);

	private final ThreadLocal<List<CacheEntryEvent<K, V>>> eventStack = new ThreadLocal<List<CacheEntryEvent<K, V>>>();

	
	
	public EventHarness() {
	}

	public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
		if (c_listeners.containsKey(cacheEntryListenerConfiguration)
			|| e_listeners.containsKey(cacheEntryListenerConfiguration)
				|| r_listeners.containsKey(cacheEntryListenerConfiguration)
					|| u_listeners.containsKey(cacheEntryListenerConfiguration)) {
			throw new IllegalStateException("May not register a given cache entry listener configuration multiple times");
		}
		// Some of the RI test seems to need a null filter factory 
			// org.jsr107.tck.event.CacheListenerTest.testDeregistration
		Factory filter_factory = cacheEntryListenerConfiguration.getCacheEntryEventFilterFactory();
		CacheEntryEventFilter<K, V> filter = (filter_factory == null) ? null : (CacheEntryEventFilter<K, V>) cacheEntryListenerConfiguration.getCacheEntryEventFilterFactory().create();
		CacheEntryListener<K, V> listener = (CacheEntryListener<K, V>) cacheEntryListenerConfiguration.getCacheEntryListenerFactory().create();
		
		if (listener instanceof CacheEntryCreatedListener) {
			synchronized (c_listeners) {
				c_listeners.put(cacheEntryListenerConfiguration, new CacheEntryCreated<K, V>(filter, (CacheEntryCreatedListener<K, V>) listener));
			}
		} 
		if (listener instanceof CacheEntryExpiredListener) {
			synchronized (e_listeners) {
				e_listeners.put(cacheEntryListenerConfiguration, new CacheEntryExpired<K, V>(filter, (CacheEntryExpiredListener<K, V>) listener));			
			}
		} 
		if (listener instanceof CacheEntryRemovedListener) {
			synchronized (r_listeners) {
				r_listeners.put(cacheEntryListenerConfiguration, new CacheEntryRemoved<K, V>(filter, (CacheEntryRemovedListener<K, V>) listener));			
			}
		}
		if (listener instanceof CacheEntryUpdatedListener) {
			synchronized (u_listeners) {
				u_listeners.put(cacheEntryListenerConfiguration, new CacheEntryUpdated<K, V>(filter, (CacheEntryUpdatedListener<K, V>) listener));			
			}
		}  
	}

	public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
		synchronized (c_listeners) {
			c_listeners.remove(cacheEntryListenerConfiguration);
		}
		synchronized (e_listeners) {
			e_listeners.remove(cacheEntryListenerConfiguration);
		}
		synchronized (r_listeners) {
			r_listeners.remove(cacheEntryListenerConfiguration);
		}
		synchronized (u_listeners) {
			u_listeners.remove(cacheEntryListenerConfiguration);
		}
	}

	private List<CacheEntryEvent<K,V>> getEventStack() {
		List<CacheEntryEvent<K,V>> local_stack = eventStack.get();
		if (local_stack == null) {
			local_stack = new java.util.ArrayList<CacheEntryEvent<K,V>>();
			eventStack.set(local_stack);
		}
		return local_stack;
	}
	
	public void entryCreated(HeCache cache, K key, V value) {
		getEventStack().add(new KeyValueEvent<K, V>(cache, EventType.CREATED, key, value));
	}
	
	public void entryExpired(HeCache cache, K key, V value) {
		getEventStack().add(new KeyValueEvent<K, V>(cache, EventType.EXPIRED, key, value));
	}
	
	public void entryRemoved(HeCache cache, K key, V value) {
		getEventStack().add(new KeyValueOldValueEvent<K, V>(cache, EventType.REMOVED, key, value, value));
	}
	
	public void entryUpdated(HeCache cache, K key, V value, V oldValue) {
		getEventStack().add(new KeyValueOldValueEvent<K, V>(cache, EventType.UPDATED, key, value, oldValue));
	}
	
	public void fireEvents() {
		Set<Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryCreated<K,V>>> c_lstnrs_current = null;
		Set<Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryExpired<K,V>>> e_lstnrs_current = null;
		Set<Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryRemoved<K,V>>> r_lstnrs_current = null;
		Set<Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryUpdated<K,V>>> u_lstnrs_current = null;
		List<CacheEntryEvent<K, V>> created_events = null;
		List<CacheEntryEvent<K, V>> expired_events = null;
		List<CacheEntryEvent<K, V>> removed_events = null;
		List<CacheEntryEvent<K, V>> updated_events = null;
		// loop over the pending (thread-local) events, making a snapshot of each category of listener we need to fire event(s) to
		for (CacheEntryEvent<K, V> event : getEventStack()) {
			switch (event.getEventType()) {
			case CREATED:
				if (c_lstnrs_current == null) {
					c_lstnrs_current = new java.util.HashSet<Map.Entry<CacheEntryListenerConfiguration<K,V>,CacheEntryCreated<K,V>>>();
					synchronized (c_listeners) {
						c_lstnrs_current.addAll(c_listeners.entrySet());
					}
				}
				if (created_events == null) {
					created_events = new java.util.ArrayList<CacheEntryEvent<K,V>>();
				}
				created_events.add(event);
				break;
			case EXPIRED:
				if (e_lstnrs_current == null) {
					e_lstnrs_current = new java.util.HashSet<Map.Entry<CacheEntryListenerConfiguration<K,V>,CacheEntryExpired<K,V>>>();
					synchronized (e_listeners) {
						e_lstnrs_current.addAll(e_listeners.entrySet());
					}
				}
				if (expired_events == null) {
					expired_events = new java.util.ArrayList<CacheEntryEvent<K,V>>();
				}
				expired_events.add(event);
				break;
			case REMOVED:
				if (r_lstnrs_current == null) {
					r_lstnrs_current = new java.util.HashSet<Map.Entry<CacheEntryListenerConfiguration<K,V>,CacheEntryRemoved<K,V>>>();
					synchronized (r_listeners) {
						r_lstnrs_current.addAll(r_listeners.entrySet());	
					}
				}
				if (removed_events == null) {
					removed_events = new java.util.ArrayList<CacheEntryEvent<K,V>>();
				}
				removed_events.add(event);
				break;
			case UPDATED:
				if (u_lstnrs_current == null) {
					u_lstnrs_current = new java.util.HashSet<Map.Entry<CacheEntryListenerConfiguration<K,V>,CacheEntryUpdated<K,V>>>();
					synchronized (u_listeners) {
						u_lstnrs_current.addAll(u_listeners.entrySet());
					}
				}
				if (updated_events == null) {
					updated_events = new java.util.ArrayList<CacheEntryEvent<K,V>>();
				}
				updated_events.add(event);
				break;
			}
		}
		// clear the (thread-local) event stack
		getEventStack().clear();
		if (created_events != null) {
			fireCreated(c_lstnrs_current, created_events);
		}
		if (expired_events != null) {
			fireExpired(e_lstnrs_current, expired_events);
		}
		if (removed_events != null) {
			fireRemoved(r_lstnrs_current, removed_events);
		}
		if (updated_events != null) {
			fireUpdated(u_lstnrs_current, updated_events);
		}
	}
	
	private void fireCreated(Set<Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryCreated<K,V>>> entries, Iterable<CacheEntryEvent<K, V>> events) {
		for (Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryCreated<K,V>> entry : entries) {
			CacheEntryEventFilter<? super K, ? super V> filter = entry.getValue().getFilter();
			final List<CacheEntryEvent<? extends K, ? extends V>> filtered_events = new java.util.ArrayList<CacheEntryEvent<? extends K, ? extends V>>();
			for (CacheEntryEvent<K, V> event : events) {			
				if (filter == null || filter.evaluate(event)) {
					filtered_events.add(event);
				}
			}
			final CacheEntryCreatedListener<K, V> listener = entry.getValue().getListener();
			if (entry.getKey().isSynchronous()) {
				listener.onCreated(filtered_events);
			} else {
				Runnable r = new Runnable() {
					public void run() {
						listener.onCreated(filtered_events);
					}
				};
				firingThreads.submit(r);
			}
		}
	}

	private void fireExpired(Set<Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryExpired<K,V>>> entries, Iterable<CacheEntryEvent<K, V>> events) {
		for (Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryExpired<K,V>> entry : entries) {
			CacheEntryEventFilter<? super K, ? super V> filter = entry.getValue().getFilter();
			final List<CacheEntryEvent<? extends K, ? extends V>> filtered_events = new java.util.ArrayList<CacheEntryEvent<? extends K, ? extends V>>();
			for (CacheEntryEvent<K, V> event : events) {			
				if (filter == null || filter.evaluate(event)) {
					filtered_events.add(event);
				}
			}
			final CacheEntryExpiredListener<K, V> listener = entry.getValue().getListener();
			if (entry.getKey().isSynchronous()) {
				listener.onExpired(filtered_events);
			} else {
				Runnable r = new Runnable() {
					public void run() {
						listener.onExpired(filtered_events);
					}
				};
				firingThreads.submit(r);
			}
		}
	}

	private void fireRemoved(Set<Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryRemoved<K,V>>> entries, Iterable<CacheEntryEvent<K, V>> events) {
		for (Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryRemoved<K,V>> entry : entries) {
			CacheEntryEventFilter<? super K, ? super V> filter = entry.getValue().getFilter();
			final List<CacheEntryEvent<? extends K, ? extends V>> filtered_events = new java.util.ArrayList<CacheEntryEvent<? extends K, ? extends V>>();
			for (CacheEntryEvent<K, V> event : events) {			
				if (filter == null || filter.evaluate(event)) {
					filtered_events.add(event);
				}
			}
			final CacheEntryRemovedListener<K, V> listener = entry.getValue().getListener();
			if (entry.getKey().isSynchronous()) {
				listener.onRemoved(filtered_events);
			} else {
				Runnable r = new Runnable() {
					public void run() {
						listener.onRemoved(filtered_events);
					}
				};
				firingThreads.submit(r);
			}
		}
	}

	private void fireUpdated(Set<Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryUpdated<K,V>>> entries, Iterable<CacheEntryEvent<K, V>> events) {
		for (Map.Entry<CacheEntryListenerConfiguration<K, V>, CacheEntryUpdated<K,V>> entry : entries) {
			CacheEntryEventFilter<? super K, ? super V> filter = entry.getValue().getFilter();
			final List<CacheEntryEvent<? extends K, ? extends V>> filtered_events = new java.util.ArrayList<CacheEntryEvent<? extends K, ? extends V>>();
			for (CacheEntryEvent<K, V> event : events) {			
				if (filter == null || filter.evaluate(event)) {
					filtered_events.add(event);
				}
			}
			final CacheEntryUpdatedListener<K, V> listener = entry.getValue().getListener();
			if (entry.getKey().isSynchronous()) {
				listener.onUpdated(filtered_events);
			} else {
				Runnable r = new Runnable() {
					public void run() {
						listener.onUpdated(filtered_events);
					}
				};
				firingThreads.submit(r);
			}
		}
	}

	public void close(String cacheName) {		
		close(cacheName, c_listeners.values());
		close(cacheName, e_listeners.values());
		close(cacheName, r_listeners.values());
		close(cacheName, u_listeners.values());
		
		//attempt to shutdown (and wait for the cache to shutdown)
		firingThreads.shutdown();
		try {
			firingThreads.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new CacheException(e);
		}
	}
	private void close(String cacheName, Collection<? extends CacheEntryListenerDoublet<K, V>> toClose) {
		for (CacheEntryListenerDoublet<K, V> d : toClose) {
			CacheEntryListener<K, V> listener = d.getListener();
			if (listener instanceof Closeable) {
				try {
					((Closeable) listener).close();
				} catch (IOException e) {
					Logger.getLogger(cacheName).log(Level.WARNING, "Problem closing listener " + listener.getClass(), e);
				}
			}
		}
	}
}
