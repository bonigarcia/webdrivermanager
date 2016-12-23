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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Configuration (wrapper for Java properties).
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class WdmConfig {

	private static WdmConfig instance;
	private Config conf;

	protected WdmConfig() {
		conf = ConfigFactory.load(WdmConfig.class.getClassLoader(),
				System.getProperty("wdm.properties", "webdrivermanager.properties"));
	}

	public static synchronized WdmConfig getInstance() {
		if (instance == null) {
			instance = new WdmConfig();
		}
		return instance;
	}

	public static String getString(String key) {
		return WdmConfig.getInstance().conf.getString(key);
	}

	public static int getInt(String key) {
		return WdmConfig.getInstance().conf.getInt(key);
	}

	public static boolean getBoolean(String key) {
		return WdmConfig.getInstance().conf.getBoolean(key);
	}

	public static URL getUrl(String key) throws MalformedURLException {
		return new URL(WdmConfig.getInstance().conf.getString(key));
	}

}
