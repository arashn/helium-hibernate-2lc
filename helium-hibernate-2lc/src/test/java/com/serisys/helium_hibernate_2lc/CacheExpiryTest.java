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

import java.util.concurrent.TimeUnit;

import javax.cache.expiry.Duration;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class CacheExpiryTest extends CacheTestBase {
	private static final TimeUnit UNIT = TimeUnit.MILLISECONDS;
	private static final long EXPIRY_MILLIS = 200l;
	public static final Duration EXPIRY = new Duration(UNIT, EXPIRY_MILLIS); 
	public static final long FIRST_HALF_WAIT = EXPIRY_MILLIS / 2;
	public static final long SECOND_HALF_WAIT = FIRST_HALF_WAIT + 1l; 
	public static final long FULL_WAIT = FIRST_HALF_WAIT+SECOND_HALF_WAIT; 
	
	@BeforeClass
	public static void setUp() {
	}
	
	@AfterClass
	public static void tearDown() {
		CacheTestBase.tearDown();
	}
	
	public CacheExpiryTest() {
	}

	public abstract void t0001_get_expiry_create();
	
	public abstract void t0002_get_expiry_update();

	public abstract void t0003_get_expiry_access();
	
//	public abstract void t0004_get_expiry_touch();

	public abstract void t0005_iterate_expiry_create();
	
	public abstract void t0006_iterate_expiry_update();

	public abstract void t0007_iterate_expiry_access();
	
//	public abstract void t0008_iterate_expiry_touch();

	public abstract void t0009_contains_expiry_create();
	
	public abstract void t0010_contains_expiry_update();

	public abstract void t0011_contains_expiry_access();
	
//	public abstract void t0012_contains_expiry_touch();

	public abstract void t0013_putIfAbsent_expiry_create();
	
	public abstract void t0014_putIfAbsent_expiry_update();

	public abstract void t0015_putIfAbsent_expiry_access();
	
//	public abstract void t0016_putIfAbsent_expiry_touch();

	public abstract void t0017_replace_expiry_create();
	
	public abstract void t0018_replace_expiry_update();

	public abstract void t0019_replace_expiry_access();
	
	public abstract void t0021_replaceIfSame_expiry_create();
	
	public abstract void t0022_replaceIfSame_expiry_update();

	public abstract void t0023_replaceIfSame_expiry_access();
	
	public abstract void t0025_remove_expiry_create();
	
	public abstract void t0026_remove_expiry_update();

	public abstract void t0027_remove_expiry_access();
	
	public abstract void t0029_removeIfSame_expiry_create();
	
	public abstract void t0030_removeIfSame_expiry_update();

	public abstract void t0031_removeIfSame_expiry_access();
	
	protected static void sleep(long millis) { 
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	}
}
