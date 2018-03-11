/*
 * (C) Copyright 2018 Boni Garcia (http://bonigarcia.github.io/)
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

import static io.github.bonigarcia.wdm.OperativeSystem.LINUX;
import static io.github.bonigarcia.wdm.OperativeSystem.MAC;
import static io.github.bonigarcia.wdm.OperativeSystem.WIN;
import static java.lang.String.join;
import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;

import com.github.drapostolos.typeparser.TypeParser;

/**
 * Configuration class.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 2.2.0
 */
public class Config {

    final Logger log = getLogger(lookup().lookupClass());

    static final String HOME = "~";

    TypeParser parser = TypeParser.newBuilder().build();

    ConfigKey<String> properties = new ConfigKey<>("wdm.properties",
            String.class, "webdrivermanager.properties");

    ConfigKey<String> targetPath = new ConfigKey<>("wdm.targetPath",
            String.class);
    ConfigKey<Boolean> forceCache = new ConfigKey<>("wdm.forceCache",
            Boolean.class);
    ConfigKey<Boolean> override = new ConfigKey<>("wdm.override",
            Boolean.class);
    ConfigKey<Boolean> useMirror = new ConfigKey<>("wdm.useMirror",
            Boolean.class);
    ConfigKey<Boolean> useBetaVersions = new ConfigKey<>("wdm.useBetaVersions",
            Boolean.class);
    ConfigKey<Boolean> avoidExport = new ConfigKey<>("wdm.avoidExport",
            Boolean.class);
    ConfigKey<Boolean> avoidOutputTree = new ConfigKey<>("wdm.avoidOutputTree",
            Boolean.class);
    ConfigKey<Integer> timeout = new ConfigKey<>("wdm.timeout", Integer.class);

    ConfigKey<String> architecture = new ConfigKey<>("wdm.architecture",
            String.class, defaultArchitecture());
    ConfigKey<String> os = new ConfigKey<>("wdm.os", String.class,
            defaultOsName());
    ConfigKey<String> proxy = new ConfigKey<>("wdm.proxy", String.class);
    ConfigKey<String> proxyUser = new ConfigKey<>("wdm.proxyUser",
            String.class);
    ConfigKey<String> proxyPass = new ConfigKey<>("wdm.proxyPass",
            String.class);
    ConfigKey<String> ignoreVersions = new ConfigKey<>("wdm.ignoreVersions",
            String.class);
    ConfigKey<String> gitHubTokenName = new ConfigKey<>("wdm.gitHubTokenName",
            String.class);
    ConfigKey<String> gitHubTokenSecret = new ConfigKey<>(
            "wdm.gitHubTokenSecret", String.class);

    ConfigKey<String> version = new ConfigKey<>(String.class);
    ConfigKey<URL> driverUrl = new ConfigKey<>(URL.class);
    ConfigKey<URL> driverMirrorUrl = new ConfigKey<>(URL.class);
    ConfigKey<String> driverExport = new ConfigKey<>(String.class);

    private <T> T resolve(ConfigKey<T> configKey) {
        String name = configKey.getName();
        T tValue = configKey.getValue();
        Class<T> type = configKey.getType();

        return resolver(name, tValue, type);
    }

    private <T> T resolver(String name, T tValue, Class<T> type) {
        String strValue;
        strValue = System.getenv(name.toUpperCase().replace(".", "_"));
        if (strValue == null) {
            strValue = System.getProperty(name);
        }
        if (strValue == null && tValue != null) {
            return tValue;
        }
        if (strValue == null) {
            strValue = getProperty(name);
        }
        return parser.parse(strValue, type);
    }

    private String getProperty(String key) {
        String value = null;
        Properties props = new Properties();
        try {
            InputStream inputStream = Config.class
                    .getResourceAsStream("/" + getProperties());
            props.load(inputStream);
            value = props.getProperty(key);
        } catch (Exception e) {
            throw new WebDriverManagerException(e);
        } finally {
            if (value == null) {
                log.trace("Property key {} not found, using default value",
                        key);
                value = "";
            }
        }
        return value;
    }

    public void reset() {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getType() == ConfigKey.class) {
                try {
                    ((ConfigKey<?>) field.get(this)).reset();
                } catch (Exception e) {
                    log.warn("Exception reseting {}", field);
                }
            }
        }
    }

    private String defaultOsName() {
        String os = getProperty("os.name").toLowerCase();
        if (IS_OS_WINDOWS) {
            os = WIN.name();
        } else if (IS_OS_LINUX) {
            os = LINUX.name();
        } else if (IS_OS_MAC) {
            os = MAC.name();
        }
        return os;
    }

    private String defaultArchitecture() {
        return "X" + System.getProperty("sun.arch.data.model");
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public boolean isExecutable(File file) {
        return resolve(os).equalsIgnoreCase("win")
                ? file.getName().toLowerCase().endsWith(".exe")
                : file.canExecute();
    }

    // Getters and setters

    public String getProperties() {
        return resolve(properties);
    }

    public void setProperties(String properties) {
        this.properties.setValue(properties);
    }

    public String getTargetPath() {
        String resolved = resolve(targetPath);
        String path = null;

        if (resolved != null) {
            path = (String) resolved;
            if (path.contains(HOME)) {
                path = path.replace(HOME, System.getProperty("user.home"));
            }
            if (path.equals(".")) {
                path = Paths.get("").toAbsolutePath().toString();
            }
        }
        return path;
    }

    public void setTargetPath(String value) {
        this.targetPath.setValue(value);
    }

    public boolean isForceCache() {
        return resolve(forceCache);
    }

    public void setForceCache(boolean value) {
        this.forceCache.setValue(value);
    }

    public boolean isOverride() {
        return resolve(override);
    }

    public void setOverride(boolean value) {
        this.override.setValue(value);
    }

    public boolean isUseMirror() {
        return resolve(useMirror);
    }

    public void setUseMirror(boolean value) {
        this.useMirror.setValue(value);
    }

    public boolean isUseBetaVersions() {
        return resolve(useBetaVersions);
    }

    public void setUseBetaVersions(boolean value) {
        this.useBetaVersions.setValue(value);
    }

    public boolean isAvoidExport() {
        return resolve(avoidExport);
    }

    public void setAvoidExport(boolean value) {
        this.avoidExport.setValue(value);
    }

    public boolean isAvoidOutputTree() {
        return resolve(avoidOutputTree);
    }

    public void setAvoidOutputTree(boolean value) {
        this.avoidOutputTree.setValue(value);
    }

    public int getTimeout() {
        return resolve(timeout);
    }

    public void setTimeout(int value) {
        this.timeout.setValue(value);
    }

    public Architecture getArchitecture() {
        return Architecture.valueOf(resolve(architecture));
    }

    public void setArchitecture(Architecture value) {
        this.architecture.setValue(value.name());
    }

    public String getOs() {
        return resolve(os);
    }

    public void setOs(String value) {
        this.os.setValue(value);
    }

    public String getProxy() {
        return resolve(proxy);
    }

    public void setProxy(String value) {
        this.proxy.setValue(value);
    }

    public String getProxyUser() {
        return resolve(proxyUser);
    }

    public void setProxyUser(String value) {
        this.proxyUser.setValue(value);
    }

    public String getProxyPass() {
        return resolve(proxyPass);
    }

    public void setProxyPass(String value) {
        this.proxyPass.setValue(value);
    }

    public String[] getIgnoreVersions() {
        String ignored = resolve(ignoreVersions);
        String[] out = {};
        if (!isNullOrEmpty(ignored)) {
            out = ignored.split(",");
        }
        return out;
    }

    public void setIgnoreVersions(String[] value) {
        this.ignoreVersions.setValue(join(",", value));
    }

    public String getGitHubTokenName() {
        return resolve(gitHubTokenName);
    }

    public void setGitHubTokenName(String value) {
        this.gitHubTokenName.setValue(value);
    }

    public String getGitHubTokenSecret() {
        return resolve(gitHubTokenSecret);
    }

    public void setGitHubTokenSecret(String value) {
        this.gitHubTokenSecret.setValue(value);
    }

    public String getVersion(String name) {
        return resolver(name, version.getValue(), version.getType());
    }

    public void setVersion(String value) {
        this.version.setValue(value);
    }

    public URL getDriverUrl(String name) {
        return resolver(name, driverUrl.getValue(), driverUrl.getType());
    }

    public void setDriverUrl(URL value) {
        this.driverUrl.setValue(value);
    }

    public URL getDriverMirrorUrl(String name) {
        return resolver(name, driverMirrorUrl.getValue(),
                driverMirrorUrl.getType());
    }

    public void setDriverMirrorUrl(URL value) {
        this.driverMirrorUrl.setValue(value);
    }

    public String getDriverExport(String name) {
        return resolver(name, driverExport.getValue(), driverExport.getType());
    }

    public void setDriverExport(String value) {
        this.driverExport.setValue(value);
    }

    public Boolean getUseMirror(String driverMirrorUrlKey) {
        if (isNullOrEmpty(driverMirrorUrlKey)) {
            throw new WebDriverManagerException("Mirror URL not available");
        }
        return resolve(useMirror);
    }

}
