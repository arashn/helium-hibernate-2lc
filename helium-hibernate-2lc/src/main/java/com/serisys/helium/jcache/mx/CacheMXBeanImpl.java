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

import javax.cache.configuration.CompleteConfiguration;
import javax.cache.management.CacheMXBean;
import javax.management.ObjectName;

public class CacheMXBeanImpl extends CacheMXBeanRoot implements CacheMXBean {
	private final CompleteConfiguration config;
	
	public CacheMXBeanImpl(ObjectName on, CompleteConfiguration config) {
		super(on);
		this.config = config;
	}
	
	  @Override
	  public String getKeyType() {
	    return config.getKeyType().getName();
	  }

	  @Override
	  public String getValueType() {
	    return config.getValueType().getName();
	  }

	  @Override
	  public boolean isReadThrough() {
	    return config.isReadThrough();
	  }

	  @Override
	  public boolean isWriteThrough() {
	    return config.isWriteThrough();
	  }

	  @Override
	  public boolean isStoreByValue() {
	    return config.isStoreByValue();
	  }

	  @Override
	  public boolean isStatisticsEnabled() {
	    return config.isStatisticsEnabled();
	  }

	  @Override
	  public boolean isManagementEnabled() {
	    return config.isManagementEnabled();
	  }
}
