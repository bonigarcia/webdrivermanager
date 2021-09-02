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
package io.github.bonigarcia.wdm.webdriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.CapabilityType;

/**
 * Options which allows to include arguments (for compatibility with Selenium 3
 * and 4).
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class OptionsWithArguments extends MutableCapabilities {

    private static final long serialVersionUID = -5948442823984189597L;

    private String capability;

    private List<String> args = new ArrayList<>();

    public OptionsWithArguments(String browserType, String capability) {
        setCapability(CapabilityType.BROWSER_NAME, browserType);
        this.capability = capability;
    }

    public OptionsWithArguments addArguments(String... arguments) {
        addArguments(Collections.unmodifiableList(Arrays.asList(arguments)));
        return this;
    }

    public OptionsWithArguments addArguments(List<String> arguments) {
        args.addAll(arguments);
        return this;
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> toReturn = new TreeMap<>(super.asMap());
        Map<String, Object> options = new TreeMap<>();
        options.put("args", Collections.unmodifiableList(args));
        toReturn.put(capability, options);
        return Collections.unmodifiableMap(toReturn);
    }

}
