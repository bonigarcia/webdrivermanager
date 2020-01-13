/*
 * (C) Copyright 2019 Boni Garcia (http://bonigarcia.github.io/)
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

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Void manager.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 3.2.0
 */
public class VoidDriverManager extends WebDriverManager {

    @Override
    protected List<URL> getDrivers() throws IOException {
        return emptyList();
    }

    @Override
    protected Optional<String> getBrowserVersion() {
        return empty();
    }

    @Override
    protected String getDriverVersion() {
        return "";
    }

    @Override
    protected URL getDriverUrl() {
        return null;
    }

    @Override
    protected Optional<URL> getMirrorUrl() {
        return empty();
    }

    @Override
    protected Optional<String> getExportParameter() {
        return empty();
    }

    @Override
    protected DriverManagerType getDriverManagerType() {
        return null;
    }

    @Override
    protected String getDriverName() {
        return "";
    }

    @Override
    protected void setDriverVersion(String version) {
        // Nothing required
    }

    @Override
    protected void setDriverUrl(URL url) {
        // Nothing required
    }

}
