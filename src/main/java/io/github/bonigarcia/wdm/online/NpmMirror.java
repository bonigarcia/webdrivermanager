/*
 * (C) Copyright 2022 Boni Garcia (https://bonigarcia.github.io/)
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

import java.net.URL;

/**
 * Plain-Old Java Object to parse JSON of https://registry.npmmirror.com/.
 *
 * @author Boni Garcia
 * @since 5.1.0
 */
public class NpmMirror {

    private String id;

    private String category;

    private String name;

    private String date;

    private String type;

    private URL url;

    private String modified;

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public URL getUrl() {
        return url;
    }

    public String getModified() {
        return modified;
    }

}
