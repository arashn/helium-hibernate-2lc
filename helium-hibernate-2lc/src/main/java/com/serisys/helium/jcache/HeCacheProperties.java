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

public interface HeCacheProperties {
	public final String SYNCH_HANDLER_CLASS_NAME = "com.serisys.jcache.synchhandlerclass";
	public final String ENABLE_MANAGEMENT = "com.serisys.jcache.management.enabled";
	public final String ENABLE_STATISTICS = "com.serisys.jcache.statistics.enabled";
	public final String HE_CACHE_URL_PROPERTY = "helium.device_url";
	public final String HE_O_CLEAN_PROPERTY = "helium.he_o_clean";
	public final String HE_O_COMPRESS_PROPERTY = "helium.he_o_compress";
	public final String HE_O_CREATE_PROPERTY = "helium.he_o_create";
	public final String HE_O_NOSORT_PROPERTY = "helium.he_o_nosort";
	public final String HE_O_READONLY_PROPERTY = "helium.he_o_readonly";
	public final String HE_O_SCAN_PROPERTY = "helium.he_o_scan";
	public final String HE_O_TRUNCATE_PROPERTY = "helium.he_o_truncate";
	public final String HE_O_VOLUME_CREATE_PROPERTY = "helium.he_o_volume_create";
	public final String HE_O_VOLUME_NOTRIM_PROPERTY = "helium.he_o_volume_notrim";
	public final String HE_O_VOLUME_TRUNCATE_PROPERTY = "helium.he_o_volume_truncate";
}
