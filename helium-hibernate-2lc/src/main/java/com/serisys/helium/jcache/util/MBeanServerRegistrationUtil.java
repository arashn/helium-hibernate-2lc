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

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class MBeanServerRegistrationUtil {

	// ensure everything gets put in one MBeanServer
	private static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	
	public static enum MBeanType {
	    CacheConfiguration,
	    CacheStatistics
	  }

	public static ObjectName objectNameFor(Cache cache, MBeanType cacheType) {
		StringBuilder bob = new StringBuilder();
		bob.append("javax.cache:type=")
			.append(cacheType)
			.append(",CacheManager=")
			.append(cache.getCacheManager().getURI().toASCIIString())
			.append(",Cache=")
			.append(cache.getName());
		String on = bob.toString();
		try {
			return new ObjectName(on);
		} catch (MalformedObjectNameException e) {
			bob = new StringBuilder();
			bob.append("Illegal ObjectName for Management Bean. CacheManager=[")
				.append(cache.getCacheManager())
				.append("], Cache=[")
				.append(cache.getName())
				.append("]");
			throw new CacheException(bob.toString(), e);
		}
	}

	private static boolean isRegistered(Object cache, ObjectName objectName) {
		return !mBeanServer.queryNames(objectName, null).isEmpty();
	}

	public static void registerMBean(Object cache, ObjectName objectName) {
		if (!isRegistered(cache, objectName)) {
			try {
				mBeanServer.registerMBean(cache, objectName);
			} catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
				StringBuilder bob = new StringBuilder();
				bob.append("Error registering cache MXBeans for CacheManager ")
					.append(objectName)
					.append(" . Error was ")
					.append(e.getMessage());
				throw new CacheException(bob.toString(), e);
			}
		}
	}

	public static void unregisterMBean(ObjectName objectName) {

		Set<ObjectName> registeredObjectNames = mBeanServer.queryNames(objectName, null);
		// should just be one
		for (ObjectName registeredObjectName : registeredObjectNames) {
			try {
				mBeanServer.unregisterMBean(registeredObjectName);
			} catch (InstanceNotFoundException | MBeanRegistrationException e) {
				StringBuilder bob = new StringBuilder();
				bob.append("Error unregistering object instance ")
					.append(registeredObjectName)
					.append(" . Error was ")
					.append(e.getMessage());
				throw new CacheException(bob.toString(), e);
			}
		}
	}

	private MBeanServerRegistrationUtil() {
	}

}
