/*
 * (C) Copyright 2015 Boni Garcia (https://bonigarcia.github.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.github.bonigarcia.wdm.online;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

/**
 * Plain-Old Java Object to parse JSON GitHub API (e.g.
 * https://api.github.com/repos/operasoftware/operachromiumdriver/releases) by
 * means of GSON.
 *
 * @author Boni Garcia
 * @since 1.0.0
 */
public class GitHubApi {

    @SerializedName("tag_name")
    private String tagName;

    private String name;
    private List<LinkedTreeMap<String, Object>> assets;

    public String getTagName() {
        return tagName;
    }

    public String getName() {
        return name;
    }

    public List<LinkedTreeMap<String, Object>> getAssets() {
        return assets;
    }

}
