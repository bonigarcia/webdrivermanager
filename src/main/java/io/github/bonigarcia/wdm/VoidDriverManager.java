/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package io.github.bonigarcia.wdm;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Void manager.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.3.1
 */
public class VoidDriverManager extends BrowserManager {

	private static VoidDriverManager instance;

	public VoidDriverManager() {
	}

	public static synchronized VoidDriverManager getInstance() {
		if (instance == null) {
			instance = new VoidDriverManager();
		}
		return instance;
	}

	@Override
	public List<URL> getDrivers() throws Exception {
		return Collections.emptyList();
	}

	@Override
	protected String getExportParameter() {
		return "";
	}

	@Override
	protected String getDriverVersion() {
		return "";
	}

	@Override
	protected URL getDriverUrl() throws MalformedURLException {
		return null;
	}

	@Override
	protected List<String> getDriverName() {
		return Arrays.asList("");
	}

}
