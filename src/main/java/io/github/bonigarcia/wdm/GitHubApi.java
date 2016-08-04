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

import java.util.List;

import com.google.gson.internal.LinkedTreeMap;

/**
 * Plain-Old Java Object to parse JSON GitHub API (e.g.
 * https://api.github.com/repos/operasoftware/operachromiumdriver/releases) by
 * means of GSON.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class GitHubApi {

	private String name;
	private String tag_name;
	private List<LinkedTreeMap<String, Object>> assets;

	public GitHubApi() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTagName() {
		return tag_name;
	}

	public void setTagName(String tagName) {
		this.tag_name = tagName;
	}

	public List<LinkedTreeMap<String, Object>> getAssets() {
		return assets;
	}

	public void setAssets(List<LinkedTreeMap<String, Object>> assets) {
		this.assets = assets;
	}

}
