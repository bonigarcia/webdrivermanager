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

import static io.github.bonigarcia.wdm.online.Parser.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonSyntaxException;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;

class ParserTest {

    @Test
    void testBrokenJSON() throws IOException {
        HttpClient client = new HttpClient(new Config());
        try {
            // known HTML endpoint
            parseJson(client, "https://www.example.com/",
                    LastGoodVersions.class);
            fail("should have barfed");
        } catch (WebDriverManagerException e) {
            assertEquals(
                    "Bad JSON. First 100 chars <!doctype html><html><head>    <title>Example Domain</title>    <meta charset=\"utf-8\" />    <meta ht",
                    e.getMessage());
            assertSame(e.getCause().getClass(), JsonSyntaxException.class);
        }
    }

}