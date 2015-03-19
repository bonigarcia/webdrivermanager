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

import java.net.URL;
import java.util.List;

/**
 * Manager for Internet Explorer.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class InternetExplorerDriverManager extends BrowserManager {

	public static void setup() {
		new InternetExplorerDriverManager().manage();
	}

	@Override
	protected List<URL> getDrivers() throws Exception {
		return getDriversFromXml(
				WdmConfig.getUrl("wdm.internetExplorerDriverUrl"),
				"IEDriverServer");
	}

	@Override
	protected String getExportParameter() {
		return WdmConfig.getString("wdm.internetExplorerExport");
	}

}
