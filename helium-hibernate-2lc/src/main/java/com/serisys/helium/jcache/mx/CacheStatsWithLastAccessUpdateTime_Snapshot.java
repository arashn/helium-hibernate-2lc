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

public class CacheStatsWithLastAccessUpdateTime_Snapshot extends CacheStats_Snapshot {

    private final long lastAccessTime;
    private final long lastUpdateTime;

	CacheStatsWithLastAccessUpdateTime_Snapshot(CacheStatsWithLastAccessUpdateTime stats) {
		super(stats);
    	this.lastAccessTime = stats.lastAccessTime;
    	this.lastUpdateTime = stats.lastUpdateTime;
	}
	
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
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
