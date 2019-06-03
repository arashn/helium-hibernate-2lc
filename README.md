**About this project**

This project provides a JSR-107 javax.cache implementation using Levyx Helium to provide fast, unbounded caching in SSD storage with close to in-memory performance. This is suitable for integration in Hibernate projects as a provider for the second-level cache. 
*We recommend that you familiarise yourself with the specification [JSR-107](https://github.com/jsr107) and with the relevant [Hibernate documentation](http://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#caching) before use.*

---

## Configuration

*Under construction*

1. You will need minimally the community edition of Levy Helium and a corresponding compatible version of the Helium Java driver.
2. The Helium Java driver is comprised of two JAR files, helium-java.jar and helium-util.jar. You may add these to the project within the lib/repo local Maven repository structure renaming them as xxxx-rel7.jar 
3. For Windows, the hejni.dll needs to be made available on the build path (by for example placing in the lib directory of the project).

---

## License
This project is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Authors: 
Richard Barnes [Serisys email](mailto:richard.barnes@serisys.com) and James Clayton [Serisys email](james.clayton@serisys.com)