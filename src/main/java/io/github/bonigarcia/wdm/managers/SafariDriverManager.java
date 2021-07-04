/*
 * (C) Copyright 2021 Boni Garcia (http://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.managers;

import static io.github.bonigarcia.wdm.config.DriverManagerType.SAFARI;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.DriverManagerType;

/**
 * Manager for Safari.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class SafariDriverManager extends VoidDriverManager {

    protected static final Logger log = getLogger(lookup().lookupClass());

    @Override
    public DriverManagerType getDriverManagerType() {
        return SAFARI;
    }

    @Override
    protected String getDriverName() {
        return "safaridriver";
    }

    @Override
    public synchronized void setup() {
        log.warn(
                "There is no need to manage the driver for the Safari browser (i.e., safaridriver) since it is built-in in Mac OS");
    }

}
