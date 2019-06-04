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

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class CacheStatsWithLastAccessUpdateTime extends CacheStats {

    protected static final AtomicLongFieldUpdater<CacheStats> LAST_ACCESS_TIME = AtomicLongFieldUpdater.newUpdater(CacheStats.class, "lastAccessTime");
    protected static final AtomicLongFieldUpdater<CacheStats> LAST_UPDATE_TIME = AtomicLongFieldUpdater.newUpdater(CacheStats.class, "lastUpdateTime");
    
    protected volatile long lastAccessTime;
    protected volatile long lastUpdateTime;

    
	public CacheStatsWithLastAccessUpdateTime(long creationTime) {
		super(creationTime);
	}

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public CacheStats_Snapshot getSnapshot() {
		return new CacheStatsWithLastAccessUpdateTime_Snapshot(this);
	}
    public void setLastAccessTime(long time) {
    	setMax(this, LAST_ACCESS_TIME, time);
    }
    
    public void setLastUpdateTime(long time) {
    	setMax(this, LAST_UPDATE_TIME, time);
    }

    @Override
    public void increaseCacheEvictions(long number) {
        super.increaseCacheEvictions(number);
        setLastUpdateTime(System.currentTimeMillis());
    }

    @Override
    public void increaseCacheExpiries(long number) {
        super.increaseCacheExpiries(number);
        setLastUpdateTime(System.currentTimeMillis());
    }

    @Override
    public void increaseCacheHits(long number, long duration) {
        super.increaseCacheHits(number, duration);
        setLastAccessTime(System.currentTimeMillis());
    }

    @Override
    public void increaseCacheHitsAndMisses(long no_hits, long no_mises, long duration) {
        super.increaseCacheHitsAndMisses(no_hits, no_mises, duration);
        setLastAccessTime(System.currentTimeMillis());
    }

    @Override
    public void increaseCacheMisses(long number, long duration) {
        super.increaseCacheMisses(number, duration);
        setLastAccessTime(System.currentTimeMillis());
    }

    @Override
    public void increaseCachePuts(long number, long duration) {
    	super.increaseCachePuts(number, duration);
        setLastUpdateTime(System.currentTimeMillis());
    }

    @Override
    public void increaseCacheRemovals(long number, long duration) {
        super.increaseCacheRemovals(number, duration);
        setLastUpdateTime(System.currentTimeMillis());
    }

    @Override
    public void incrementCacheEvictions() {
    	super.incrementCacheEvictions();
        setLastUpdateTime(System.currentTimeMillis());
    }

    @Override
    public void incrementCacheExpiries() {
        super.incrementCacheExpiries();
        setLastUpdateTime(System.currentTimeMillis());
    }

    @Override
    public void incrementCacheHits() {
        super.incrementCacheHits();
        setLastAccessTime(System.currentTimeMillis());
    }

    @Override
    public void incrementCacheHits(long duration) {
        super.incrementCacheHits(duration);
        setLastAccessTime(System.currentTimeMillis());
    }

    @Override
    public void incrementCacheMisses() {
        super.incrementCacheMisses();
        setLastAccessTime(System.currentTimeMillis());
    }

    @Override
    public void incrementCacheMisses(long duration) {
        super.incrementCacheMisses(duration);
        setLastAccessTime(System.currentTimeMillis());
    }

    @Override
    public void incrementCachePuts(long duration) {
        super.incrementCachePuts(duration);
        setLastUpdateTime(System.currentTimeMillis());
    }

    @Override
    public void incrementCacheRemovals(long duration) {
    	super.incrementCacheRemovals(duration);
        setLastUpdateTime(System.currentTimeMillis());
    }
    
    @Override
	protected void toString(StringBuilder bob) {
		super.toString(bob);
		bob.append(", lastAccessTime=")
			.append(lastAccessTime)
			.append(", lastUpdateTime=")
			.append(lastUpdateTime);
	}
}
