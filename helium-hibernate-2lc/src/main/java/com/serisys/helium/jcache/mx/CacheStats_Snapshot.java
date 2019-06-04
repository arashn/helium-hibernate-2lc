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

package com.serisys.helium.jcache.mx;

public class CacheStats_Snapshot {
	
    private final long creationTime;
    private final long evictions;
    private final long getCacheTimeTakenNanos;
    private final long hits;
    private final long misses;
    private final long puts;
    private final long putTimeTakenNanos;
    private final long removals;
    private final long removeTimeTakenNanos;

    CacheStats_Snapshot(CacheStats stats) {
    	this.creationTime = stats.creationTime;
    	this.evictions = stats.cacheEvictions.longValue();
    	this.getCacheTimeTakenNanos = stats.cacheGetTimeTakenNanos.longValue();
    	this.hits = stats.cacheHits.longValue();
    	this.misses = stats.cacheMisses.longValue();
    	this.puts = stats.cachePuts.longValue();
    	this.putTimeTakenNanos = stats.cachePutTimeTakenNanos.longValue();
    	this.removals = stats.cacheRemovals.longValue();
    	this.removeTimeTakenNanos = stats.cacheRemoveTimeTakenNanos.longValue();
    }
    
    public float getAverageGetTime() {
    	return CacheStats.getAverageGetTime(getCacheGets(), getCacheGetTimeTakenNanos());
    }

    public float getAveragePutTime() {
    	return CacheStats.getAverageGetTime(getCachePuts(), getCachePutTimeTakenNanos());
    }

    public float getAverageRemoveTime() {
    	return CacheStats.getAverageRemoveTime(getCacheRemovals(), getCacheRemoveTimeTakenNanos());
    }
    
    public long getCacheEvictions() {
        return evictions;
    }
    
    public long getCacheGets() {
        return getCacheHits() + getCacheMisses();
    }

    public long getCacheGetTimeTakenNanos() {
        return getCacheTimeTakenNanos;
    }

    public float getCacheHitPercentage() {
        return CacheStats.getCacheHitPercentage(getCacheHits(), getCacheGets());
    }

    public long getCacheHits() {
        return hits;
    }
    
    public long getCacheMisses() {
        return misses;
    }
    
    public float getCacheMissPercentage() {
    	return CacheStats.getCacheMissPercentage(getCacheMisses(), getCacheGets());
    }
    
    public long getCachePuts() {
        return puts;
    }
    
    public long getCachePutTimeTakenNanos() {
        return putTimeTakenNanos;
    }

    public long getCacheRemovals() {
        return removals;
    }

    public long getCacheRemoveTimeTakenNanos() {
        return removeTimeTakenNanos;
    }

    public long getCreationTime() {
        return creationTime;
    }

	public String toString() {
    	StringBuilder bob = new StringBuilder();
    	bob.append(getClass().getSimpleName());
    	toString(bob);
    	bob.append('}');
    	return bob.toString();
    }
	protected void toString(StringBuilder bob) {
		bob.append("{creationTime=")
			.append(creationTime)
			.append(", removals=")
			.append(removals)
			.append(", puts=")
			.append(puts)
			.append(", hits=")
			.append(hits)
			.append(", misses=")
			.append(misses)
			.append(", evictions=")
			.append(evictions)
			.append(", putTimeTakenNanos=")
			.append(putTimeTakenNanos)
			.append(", getCacheTimeTakenNanos=")
			.append(getCacheTimeTakenNanos)
			.append(", removeTimeTakenNanos=")
			.append(removeTimeTakenNanos);
	}
    
 }
