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

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.slf4j.Logger;

import com.google.gson.GsonBuilder;

/**
 * JSON parser for online endpoints.
 *
 * @author Boni Garcia
 * @since 5.4.0
 */
public class Parser {

    static final Logger log = getLogger(lookup().lookupClass());

    private Parser() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> T parseJson(HttpClient client, String url, Class<T> klass)
            throws IOException {
        HttpGet get = client.createHttpGet(new URL(url));
        InputStream content = client.execute(get).getEntity().getContent();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(content))) {
            return new GsonBuilder().create().fromJson(reader, klass);
        }
    }
}
