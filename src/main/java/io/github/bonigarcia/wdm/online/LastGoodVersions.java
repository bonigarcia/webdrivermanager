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

import com.google.gson.annotations.SerializedName;

import io.github.bonigarcia.wdm.online.GoodVersions.Downloads;

/**
 * POJO to parse the Chrome for Testing (CfT) JSON endpoints
 * (https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json).
 *
 * @author Boni Garcia
 * @since 5.4.0
 */
public class LastGoodVersions {

    public String timestamp;
    public Channels channels;

    public class Channels {
        @SerializedName("Stable")
        public Channel stable;

        @SerializedName("Beta")
        public Channel beta;

        @SerializedName("Dev")
        public Channel dev;

        @SerializedName("Canary")
        public Channel canary;
    }

    public class Channel {
        public String channel;
        public String version;
        public String revision;
        public Downloads downloads;
    }

}
