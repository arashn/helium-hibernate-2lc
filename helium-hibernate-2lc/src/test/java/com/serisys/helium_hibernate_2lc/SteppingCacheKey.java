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

import java.util.concurrent.Semaphore;

public class SteppingCacheKey extends CustomCacheKey {
	private static final long serialVersionUID = -421666978295810070L;
	private transient Semaphore stepper;
	
	public SteppingCacheKey(String value) {
		this(value, null);
	}

	public SteppingCacheKey(String value, Semaphore stepper) {
		super(value);
		this.stepper = stepper;
	}
	
	@Override
	protected void step() {
		if (stepper != null) {
			try {
				stepper.acquire();
			} catch (InterruptedException e) {
				
			}
		}
	}

}
