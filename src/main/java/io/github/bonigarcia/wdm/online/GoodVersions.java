/*
 * (C) Copyright 2023 Boni Garcia (https://bonigarcia.github.io/)
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

/**
 * POJO to parse the Chrome for Testing (CfT) JSON endpoints
 * (https://googlechromelabs.github.io/chrome-for-testing/known-good-versions-with-downloads.json).
 *
 * @author Boni Garcia
 * @since 5.4.0
 */
public class GoodVersions {

    public String timestamp;
    public List<Versions> versions;

    public class Versions {
        public String version;
        public String revision;
        public Downloads downloads;
    }

    public class Downloads {
        public List<PlatformUrl> chrome;
        public List<PlatformUrl> chromedriver;
    }

    public class PlatformUrl {
        public String platform;
        public String url;
    }

}
