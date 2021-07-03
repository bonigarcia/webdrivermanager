/*
 * (C) Copyright 2020 Boni Garcia (http://bonigarcia.github.io/)
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Plain-Old Java Object to parse JSON BitBucket API (e.g.
 * https://bitbucket.org/api/2.0/repositories/ariya/phantomjs/downloads) by
 * means of GSON.
 *
 * @author Boni Garcia
 * @since 4.1.0
 */
public class BitBucketApi {

    private int pagelen;
    private int size;
    private List<BitBucketValue> values;

    public int getPagelen() {
        return pagelen;
    }

    public int getSize() {
        return size;
    }

    public List<BitBucketValue> getValues() {
        return values;
    }

    public List<URL> getUrls() throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        for (BitBucketValue value : this.getValues()) {
            urls.add(new URL(value.getLinks().getSelf().getHref()));
        }
        return urls;
    }

    class BitBucketValue {
        private String name;
        private BitBucketLink links;
        private int downloads;

        @SerializedName("created_on")
        private String createOn;

        private Object user;
        private String type;
        private int size;

        public String getName() {
            return name;
        }

        public BitBucketLink getLinks() {
            return links;
        }

        public int getDownloads() {
            return downloads;
        }

        public String getCreateOn() {
            return createOn;
        }

        public Object getUser() {
            return user;
        }

        public String getType() {
            return type;
        }

        public int getSize() {
            return size;
        }
    }

    class BitBucketLink {
        private BitBucketSelf self;

        public BitBucketSelf getSelf() {
            return self;
        }
    }

    class BitBucketSelf {
        private String href;

        public String getHref() {
            return href;
        }
    }

}
