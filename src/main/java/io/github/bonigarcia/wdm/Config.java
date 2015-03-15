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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration (wrapper for Java properties).
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class Config {

	private static Config instance = null;
	private Properties properties;

	protected Config() throws IOException {
		properties = new Properties();
		InputStream input = null;

		input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("wdm.properties");
		properties.load(input);
		input.close();
	}

	public static Config getInstance() throws IOException {
		if (instance == null) {
			instance = new Config();
		}
		return instance;
	}

	public static String getProperty(String key) throws IOException {
		return Config.getInstance().properties.get(key).toString();
	}

}
