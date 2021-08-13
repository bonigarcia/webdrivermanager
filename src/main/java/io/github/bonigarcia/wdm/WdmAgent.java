/*
 * (C) Copyright 2020 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm;

import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROME;
import static io.github.bonigarcia.wdm.config.DriverManagerType.EDGE;
import static io.github.bonigarcia.wdm.config.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.config.DriverManagerType.IEXPLORER;
import static io.github.bonigarcia.wdm.config.DriverManagerType.OPERA;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.DriverManagerType;

/**
 * WebDriverManager agent.
 *
 * @author Boni Garcia
 * @since 4.0.0
 */
public class WdmAgent {

    static final Logger log = getLogger(lookup().lookupClass());

    private WdmAgent() {
        throw new IllegalStateException(
                "WebDriverManager agent is used for static instrumentation");
    }

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new DefineTransformer(), true);
    }

    static class DefineTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classfileBuffer) throws IllegalClassFormatException {

            DriverManagerType driverManagerType = null;
            switch (className) {
            case "org/openqa/selenium/chrome/ChromeDriver":
                driverManagerType = CHROME;
                break;
            case "org/openqa/selenium/firefox/FirefoxDriver":
                driverManagerType = FIREFOX;
                break;
            case "org/openqa/selenium/opera/OperaDriver":
                driverManagerType = OPERA;
                break;
            case "org/openqa/selenium/edge/EdgeDriver":
                driverManagerType = EDGE;
                break;
            case "org/openqa/selenium/ie/InternetExplorerDriver":
                driverManagerType = IEXPLORER;
                break;
            default:
                break;
            }
            if (driverManagerType != null) {
                log.debug(
                        "WebDriverManager Agent is going to resolve the driver for {}",
                        driverManagerType);
                WebDriverManager.getInstance(driverManagerType).setup();
            }

            return classfileBuffer;
        }
    }

}