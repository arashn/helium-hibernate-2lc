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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(
		{ 
			CacheTest.class,
			CacheContentTest.class,
			DynamicThreadLocalItemPoolCacheContentTest.class, 
			ItemPoolCacheContentTest.class, 
			UnmanagedItemsCacheContentTest.class, 
			ClosedCacheTest.class,
			CacheListenersTest.class,
			NullKeyTest.class, 
			NullValueTest.class,
			CacheStatisticsTest.class,
			ExpireOnCreationStatsTest.class,
			CacheReadThroughTest.class, 
			CacheWriteThroughTest.class,
			UnsafeLockingHandlerTest.class,
			UnsafeLockingHandlerCacheContentTest.class,
			SingleLevelLockingTest.class,
			SingleLevelLockingCacheContentTest.class,
			DualLevelLockingTest.class,
			DualLockingCacheContentTest.class,
			AccessedExpiryTest.class,
			CreatedExpiryTest.class, 
			TouchedExpiryTest.class, 
			UpdatedExpiryTest.class
		}
)
public class AllCacheTests {
	public static final String HE_FILEPATH = "D:/Levyx/Helium/HE_BLOCK.DAT";
	public static final String HE_DEV_URL = "he://./"+HE_FILEPATH;

}
