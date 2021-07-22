/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.webdriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;

import com.google.common.collect.ImmutableList;

/**
 * Edge options which allows to include arguments (not available in Selenium 3).
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class EdgeOptionsWithArguments extends MutableCapabilities {

    public static final String CAPABILITY = "ms:edgeOptions";

    private List<String> args = new ArrayList<>();

    public EdgeOptionsWithArguments() {
        setCapability(CapabilityType.BROWSER_NAME, BrowserType.EDGE);
    }

    public EdgeOptionsWithArguments addArguments(String... arguments) {
        addArguments(ImmutableList.copyOf(arguments));
        return this;
    }

    public EdgeOptionsWithArguments addArguments(List<String> arguments) {
        args.addAll(arguments);
        return this;
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> toReturn = new TreeMap<>(super.asMap());
        Map<String, Object> options = new TreeMap<>();
        options.put("args", ImmutableList.copyOf(args));
        toReturn.put(CAPABILITY, options);
        return Collections.unmodifiableMap(toReturn);
    }

}
