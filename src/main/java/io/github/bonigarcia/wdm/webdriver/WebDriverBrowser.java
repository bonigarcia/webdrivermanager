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
package io.github.bonigarcia.wdm.webdriver;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import io.github.bonigarcia.wdm.docker.DockerContainer;

/**
 * WebDriver instance and associated Docker containers (if any(.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class WebDriverBrowser {

    final Logger log = getLogger(lookup().lookupClass());

    WebDriver driver;
    List<DockerContainer> dockerContainerList;

    public WebDriverBrowser(WebDriver driver) {
        this.driver = driver;
        this.dockerContainerList = new ArrayList<>();
    }

    public WebDriver getDriver() {
        return driver;
    }

    public List<DockerContainer> getDockerContainerList() {
        return dockerContainerList;
    }

    public void addDockerContainer(DockerContainer dockerContainer) {
        dockerContainerList.add(dockerContainer);
    }

}
