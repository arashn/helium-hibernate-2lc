**About this project**

This project provides a JSR-107 javax.cache implementation using Levyx Helium to provide fast, unbounded caching in SSD storage with close to in-memory performance. This is suitable for integration in Hibernate projects as a provider for the second-level cache. 
*We recommend that you familiarise yourself with the specification [JSR-107](https://github.com/jsr107) and with the relevant [Hibernate documentation](http://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#caching) before use.*

This project was developed within the Eclipse IDE as a Java / Maven project and certain of the project structure files (.project, .classpath) reflect this.

---

## Required libraries

1. You will need minimally the [community edition of Levyx Helium](https://helium.levyx.com/) and a corresponding compatible version of the Helium Java driver.
2. The Helium Java driver is comprised of two JAR files, helium-java.jar and helium-util.jar. You may add these to the project within the lib/repo local Maven repository structure renaming them as xxxx-rel7.jar. See the comments in the pom file for placement of these. 
3. For Windows, the hejni.dll needs to be made available on the build path (by for example placing in the lib directory of the project).

---

## Configuration

1. The location of the Helium data store may be specified using the system property / VM argument helium.device_url (eg, -Dhelium.device_url=he://./C:/Levyx/Helium/HE_BLOCK.DAT). Refer to the Helium documentation for the correct format for referencing large files and volumes to use as the data store.
2. The system properties com.serisys.jcache.management.enabled and com.serisys.jcache.statistics.enabled, when set to true, enable cache management and statistics respectively.
3. To use the cache implementation in Hibernate, specify hibernate.javax.cache.provider = com.serisys.helium.jcache.HeCachingProvider
4. To use the implementation as a JSR-107 cache, specify for example 
	-Djavax.cache.Cache=com.serisys.helium.jcache.HeCache -Djavax.cache.Cache.Entry=com.serisys.helium.jcache.CacheEntryImpl 

---

## License
This project is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

The development of this project was originally performed by the authors working at Serisys Ltd; the company decided to open-source this work in order to best benefit the Levyx, Hibernate and enterprise Java communities. 

Authors: 
Richard Barnes [Serisys email](mailto:richard.barnes@serisys.com) and James Clayton [Serisys email](james.clayton@serisys.com)