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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.cache.Cache;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import com.serisys.helium.jcache.HeCache;
import com.serisys.helium.jcache.WrappableCacheWrapper;
import com.serisys.helium.jcache.mx.CacheMXStatsBeanImpl;
import com.serisys.helium.jcache.mx.CacheStats_Snapshot;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CacheStatisticsTest extends CacheContentTest {

	@BeforeClass
	public static void setUp() {
		CacheTestBase.setUp(true);
	}

	@AfterClass
	public static void tearDown() {
		CacheTestBase.tearDown();
	}

	public CacheStatisticsTest() {
	}
	
	@Before
	public void clear() { 

	}

	private CacheStats_Snapshot getStats_Snapshot() {
		return ((CacheMXStatsBeanImpl<String, String>)((WrappableCacheWrapper<String, String>) cache).getInnerCache()).getStats().getSnapshot();
	}
	
	@Test
	@Override
	public void t0000_defaultCache_containsKey() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0000_defaultCache_containsKey();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits(), snap1.getCacheHits());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
	}

	@Test
	@Override
	public void t0001_defaultCache_put_get() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0001_defaultCache_put_get();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
	} 

	@Test
	@Override
	public void t0002_defaultCache_put_get_overwrite() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0002_defaultCache_put_get_overwrite();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+2, snap1.getCacheHits());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
	} 
	
	@Test
	@Override
	public void t0003_defaultCache_getAll() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0003_defaultCache_getAll();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+4, snap1.getCacheHits());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
	}
	
	@Test
	@Override
	public void t0004_defaultCache_getAndPut() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0004_defaultCache_getAndPut();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+3, snap1.getCacheHits());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
	} 

	@Test
	@Override
	public void t0004_defaultCache_getAndPut_2() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0004_defaultCache_getAndPut_2();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+3, snap1.getCacheHits());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
	}
	
	@Test
	@Override
	public void t0005_defaultCache_getAndRemove() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0005_defaultCache_getAndRemove();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+2, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals()+1, snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0006_defaultCache_getAndReplace() {	
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0006_defaultCache_getAndReplace();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+3, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0007_defaultCache_invoke() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0007_defaultCache_invoke();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+3, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+3, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0007_defaultCache_invoke_noSuchKey() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0007_defaultCache_invoke_noSuchKey();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts(), snap1.getCachePuts());
		assertEquals(snap0.getCacheHits(), snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0008_defaultCache_invokeAll() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0008_defaultCache_invokeAll();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+2, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0009_defaultCache_clear() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0009_defaultCache_clear();
		// the super call invokes a put & a miss before calling clear on the cache - this does not clear the stats
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0010_defaultCache_iterator() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0010_defaultCache_iterator();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+10, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+10, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0010_defaultCache_iterator_remove() {
		cache.clear();
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0010_defaultCache_iterator_remove();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+10, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+10, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals()+10, snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
		
	}

	@Test
	@Override
	public void t0011_defaultCache_putAll() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0011_defaultCache_putAll();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+10, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+10, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0012_defaultCache_putIfAbsent() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0012_defaultCache_putIfAbsent();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+2, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0013_defaultCache_remove() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0013_defaultCache_remove();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals()+1, snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0014_defaultCache_remove() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0014_defaultCache_remove();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+2, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0015_defaultCache_removeAll() {
		cache.clear();
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0015_defaultCache_removeAll();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals()+1, snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0016_defaultCache_removeAll() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0016_defaultCache_removeAll();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+3, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals()+1, snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());	
	}

	@Test
	@Override
	public void t0017_defaultCache_replace() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0017_defaultCache_replace();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+3, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());	
	}

	@Test
	@Override
	public void t0018_defaultCache_replace() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0018_defaultCache_replace();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts(), snap1.getCachePuts());
		assertEquals(snap0.getCacheHits(), snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());	
	}

	@Test
	@Override
	public void t0019_defaultCache_replace() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0019_defaultCache_replace();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+3, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());	
	}

	@Test
	@Override
	public void t0020_defaultCache_replace() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0020_defaultCache_replace();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+3, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0021_defaultCache_replace() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0021_defaultCache_replace();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts(), snap1.getCachePuts());
		assertEquals(snap0.getCacheHits(), snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0022_defaultCache_removeKV() {	
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0022_defaultCache_removeKV();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals()+1, snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0023_defaultCache_removeKV() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0023_defaultCache_removeKV();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits(), snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}

	@Test
	@Override
	public void t0024_defaultCache_removeKV() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0024_defaultCache_removeKV();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits(), snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0025_iterate_variableSizes() {
		super.t0025_iterate_variableSizes();
	}

	@Test
	@Override
	public void t0026_get_large_overflowing() {
		super.t0026_get_large_overflowing();
	}
	
	@Test
	@Override
	public void t0027_defaultCache_invoke_setting() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0027_defaultCache_invoke_setting();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+2, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0028_defaultCache_invoke_setting() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0028_defaultCache_invoke_setting();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+2, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0029_defaultCache_invoke_removing() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0029_defaultCache_invoke_removing();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals()+1, snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0030_defaultCache_invoke_exists() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0030_defaultCache_invoke_exists();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0032_defaultCache_invokeAll_setting() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0032_defaultCache_invokeAll_setting();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+2, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0033_defaultCache_invokeAll_setting() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0033_defaultCache_invokeAll_setting();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+2, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+2, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0034_defaultCache_invokeAll_removing() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0034_defaultCache_invokeAll_removing();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals()+1, snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0035_defaultCache_invokeAll_exists() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0035_defaultCache_invokeAll_exists();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0036_defaultCache_invokeAll_exists() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0036_defaultCache_invokeAll_exists();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts(), snap1.getCachePuts());
		assertEquals(snap0.getCacheHits(), snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0037_defaultCache_invoke_getting() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0037_defaultCache_invoke_getting();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0038_defaultCache_invokeAll_getting() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0038_defaultCache_invokeAll_getting();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0039_defaultCache_invokeAll_getting() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0039_defaultCache_invokeAll_getting();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0040_defaultCache_invokeAll_setting() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0040_defaultCache_invokeAll_setting();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+3, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+2, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0041_defaultCache_invokeAll_removing() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0041_defaultCache_invokeAll_removing();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals()+1, snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0042_defaultCache_invokeAll_exists() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0042_defaultCache_invokeAll_exists();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+1, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+1, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses()+1, snap1.getCacheMisses());
	}
	
	@Test
	@Override
	public void t0043_defaultCache_invokeAll_setting_throwing() {
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		super.t0043_defaultCache_invokeAll_setting_throwing();
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts()+21, snap1.getCachePuts());
		assertEquals(snap0.getCacheHits()+22, snap1.getCacheHits());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
	}
	
	
	
	@SuppressWarnings("rawtypes")
	@Test 
	public void t0101_unwrap() {
		Class<Cache> claz = Cache.class;
		IllegalArgumentException unexpected = null;
		try {
			cache.unwrap(claz);
		} catch (IllegalArgumentException e) {
			unexpected = e;
		}
		assertNull(unexpected);
	}
	
	@SuppressWarnings("rawtypes")
	@Test 
	public void t0102_unwrap() {
		Class<HeCache> claz = HeCache.class;
		IllegalArgumentException unexpected = null;
		try {
			cache.unwrap(claz);
		} catch (IllegalArgumentException e) {
			unexpected = e;
		}
		assertNull(unexpected);
	}
	
	@SuppressWarnings("rawtypes")
	@Test 
	public void t0103_unwrap() {
		Class<UnimplementedCache> claz = UnimplementedCache.class;
		IllegalArgumentException expected = null;
		try {
			cache.unwrap(claz);
		} catch (IllegalArgumentException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	@SuppressWarnings("rawtypes")
	@Test 
	public void t0104_unwrap() {
		Class<CacheMXStatsBeanImpl> claz = CacheMXStatsBeanImpl.class;
		IllegalArgumentException unexpected = null;
		try {
			cache.unwrap(claz);
		} catch (IllegalArgumentException e) {
			unexpected = e;
		}
		assertNull(unexpected);
	}
	

	
}
