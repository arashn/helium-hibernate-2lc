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

import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;

import com.levyx.helium.Helium;
import com.serisys.helium.jcache.util.DynamicThreadLocalItemPool;
import com.serisys.helium.jcache.util.ItemManager;

public class ConsolidatedCacheConfig<K, V> extends MutableConfiguration<K, V> {
	public static final String HELIUM_FACTORY_NAME = "com.serisys.helium.factory"; 
	private Factory<Helium> heliumFactory;
	private Factory<ItemManager> itemManagerFactory;
	
	public ConsolidatedCacheConfig() {
		super();
	}

	public ConsolidatedCacheConfig(Configuration<K, V> configuration) {
		super();
		this.keyType = configuration.getKeyType();
		this.valueType = configuration.getValueType();
		setStoreByValue(configuration.isStoreByValue());
	}

	public ConsolidatedCacheConfig(CompleteConfiguration<K, V> configuration) {
		super(configuration);
	}

	public ConsolidatedCacheConfig(ConsolidatedCacheConfig<K, V> configuration) {
		super(configuration);
		setHeliumFactory(configuration.getHeliumFactory());
		setItemManagerFactory(configuration.getItemManagerFactory());
	}

	public Factory<Helium> getHeliumFactory() {
		if (heliumFactory == null && System.getProperty(HELIUM_FACTORY_NAME) != null) {
			try {
				Class<Factory<Helium>> claz = (Class<Factory<Helium>>) Class.forName(System.getProperty(HELIUM_FACTORY_NAME));
				return claz.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return heliumFactory;
	}
	
	public Factory<ItemManager> getItemManagerFactory() {
		return itemManagerFactory == null ? new Factory<ItemManager>() {
				@Override
				public ItemManager create() {
					return new DynamicThreadLocalItemPool();
				}
			} 
			: itemManagerFactory;
	}
	
	public void setHeliumFactory(Factory<Helium> fact) { 
		heliumFactory = fact;
	}
	
	public void setItemManagerFactory(Factory<ItemManager> fact) {
		itemManagerFactory = fact;
	}
}
