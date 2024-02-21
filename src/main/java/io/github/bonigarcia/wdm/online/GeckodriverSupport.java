/*
 * (C) Copyright 2024 Boni Garcia (https://bonigarcia.github.io/)
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

/**
 * POJO to parse geckodriver-support.json
 * (https://raw.githubusercontent.com/SeleniumHQ/selenium/trunk/common/geckodriver/geckodriver-support.json).
 *
 * @author Boni Garcia
 * @since 5.7.0
 */
public class GeckodriverSupport {

    @SerializedName("geckodriver-releases")
    public List<GeckodriverRelease> geckodriverReleases;

    public class GeckodriverRelease {
        @SerializedName("geckodriver-version")
        public String geckodriverVersion;

        @SerializedName("min-firefox-version")
        public Integer minFirefoxVersion;

        @SerializedName("max-firefox-version")
        public Integer maxFirefoxVersion;
    }

}
