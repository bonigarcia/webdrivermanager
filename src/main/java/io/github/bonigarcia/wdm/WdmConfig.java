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
package io.github.bonigarcia.wdm;

import static io.github.bonigarcia.wdm.Architecture.valueOf;
import static io.github.bonigarcia.wdm.OperativeSystem.LINUX;
import static io.github.bonigarcia.wdm.OperativeSystem.MAC;
import static io.github.bonigarcia.wdm.OperativeSystem.WIN;
import static java.lang.Boolean.getBoolean;
import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;

/**
 * Configuration utility class.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class WdmConfig {

    static final Logger log = getLogger(lookup().lookupClass());

    static final String HOME = "~";

    protected Architecture architecture; // architecture()
    protected String myOsName; // operativeSystem()
    protected Boolean useBetaVersions; // useBetaVersions()
    protected String version; // version()
    protected String exportParameter; // exportParameter()
    protected Boolean forcingCache; // forceCache()
    protected Boolean forcingDownload; // forceDownload()
    protected Boolean useMirror; // useMirror()
    protected URL driverUrl; // driverRepositoryUrl()
    protected String proxyValue; // proxy()
    protected String proxyUser; // proxyUser()
    protected String proxyPass; // proxyPass()
    protected String[] ignoredVersions; // ignoredVersions()
    protected String gitHubTokenName; // gitHubTokenName()
    protected String gitHubTokenSecret; // gitHubTokenSecret()
    protected String targetPath; // targetPath()
    protected Integer timeout; // timeout()

    public WdmConfig() {
        reset();
    }

    protected void reset() {
        setArchitecture(defaultArchitecture());
        setMyOsName(defaultOsName());
        setUseBetaVersions(null);
        setVersion(null);
        setExportParameter(null);
        setForcingCache(null);
        setForcingDownload(null);
        setUseMirror(null);
        setDriverUrl(null);
        setProxyValue(null);
        setProxyUser(null);
        setProxyPass(null);
        setIgnoredVersions(null);
        setGitHubTokenName(null);
        setGitHubTokenSecret(null);
        setTargetPath(null);
        setTimeout(null);
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

    private Architecture defaultArchitecture() {
        return valueOf("X" + System.getProperty("sun.arch.data.model"));
    }

    protected static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    protected boolean isExecutable(File file) {
        return getMyOsName().equalsIgnoreCase("win")
                ? file.getName().toLowerCase().endsWith(".exe")
                : file.canExecute();
    }

    private String resolveConfigKey(String key) {
        String value = null;
        if (!key.equals("")) {
            value = System.getenv(key.toUpperCase().replace(".", "_"));
            if (value == null) {
                value = System.getProperty(key);
            }
        }
        return value;
    }

    private Object resolveConfigKey(String key, Object configValue) {
        Object value = resolveConfigKey(key);
        if (value == null && configValue != null) {
            value = configValue;
        }
        if (value == null) {
            value = getProperty(key);
        }
        return value;
    }

    private boolean resolveBoolean(String key, Object configValue) {
        Object resolved = resolveConfigKey(key, configValue);
        try {
            return (boolean) resolved;
        } catch (Exception e) {
            return getBoolean((String) resolved);
        }
    }

    private String getProperty(String key) {
        String value = null;
        Properties properties = new Properties();
        try {
            String wdmProperties = resolveConfigKey("wdm.properties");
            if (wdmProperties == null) {
                wdmProperties = "/webdrivermanager.properties";
            }
            InputStream inputStream = WdmConfig.class
                    .getResourceAsStream(wdmProperties);
            properties.load(inputStream);
            value = properties.getProperty(key);
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

    // Getters and setters

    protected String getVersion(String driverVersionKey) {
        return (String) resolveConfigKey(driverVersionKey, version);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    protected String getExportParameter(String exportParameterKey) {
        return (String) resolveConfigKey(exportParameterKey, exportParameter);
    }

    public void setExportParameter(String exportParameter) {
        this.exportParameter = exportParameter;
    }

    protected URL getDriverUrl(String driverUrlKey) {
        try {
            return (URL) resolveConfigKey(driverUrlKey, driverUrl);
        } catch (Exception e1) {
            try {
                return new URL(
                        (String) resolveConfigKey(driverUrlKey, driverUrl));
            } catch (MalformedURLException e2) {
                throw new WebDriverManagerException(e2);
            }
        }
    }

    public void setDriverUrl(URL driverUrl) {
        this.driverUrl = driverUrl;
    }

    protected Boolean getUseMirror(String driverMirrorUrlKey) {
        if (isNullOrEmpty(driverMirrorUrlKey)) {
            throw new WebDriverManagerException("Mirror URL not available");
        }
        return resolveBoolean("wdm.useMirror", useMirror);
    }

    public void setUseMirror(Boolean useMirror) {
        this.useMirror = useMirror;
    }

    protected boolean isForcingCache() {
        return resolveBoolean("wdm.forceCache", forcingCache);
    }

    public void setForcingCache(Boolean isForcingCache) {
        this.forcingCache = isForcingCache;
    }

    protected boolean isForcingDownload() {
        return resolveBoolean("wdm.override", forcingDownload);
    }

    public void setForcingDownload(Boolean isForcingDownload) {
        this.forcingDownload = isForcingDownload;
    }

    protected boolean isUseBetaVersions() {
        return resolveBoolean("wdm.useBetaVersions", useBetaVersions);
    }

    public void setUseBetaVersions(Boolean useBetaVersions) {
        this.useBetaVersions = useBetaVersions;
    }

    protected Architecture getArchitecture() {
        return (Architecture) resolveConfigKey("wdm.architecture",
                architecture);
    }

    public void setArchitecture(Architecture architecture) {
        this.architecture = architecture;
    }

    protected String getMyOsName() {
        return (String) resolveConfigKey("wdm.os", myOsName);
    }

    public void setMyOsName(String myOsName) {
        this.myOsName = myOsName;
    }

    protected String getProxyValue() {
        return (String) resolveConfigKey("wdm.proxy", proxyValue);
    }

    public void setProxyValue(String proxyValue) {
        this.proxyValue = proxyValue;
    }

    protected String getProxyUser() {
        return (String) resolveConfigKey("wdm.proxyUser", proxyUser);
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    protected String getProxyPass() {
        return (String) resolveConfigKey("wdm.proxyPass", proxyPass);
    }

    public void setProxyPass(String proxyPass) {
        this.proxyPass = proxyPass;
    }

    protected String[] getIgnoredVersions() {
        Object resolved = resolveConfigKey("wdm.ignoreVersions",
                ignoredVersions);
        try {
            return (String[]) resolved;
        } catch (Exception e) {
            String[] out = {};
            String ignored = (String) resolved;
            if (!isNullOrEmpty(ignored)) {
                out = ignored.split(",");
            }
            return out;
        }
    }

    public void setIgnoredVersions(String[] ignoredVersions) {
        this.ignoredVersions = ignoredVersions;
    }

    protected String getGitHubTokenName() {
        return (String) resolveConfigKey("wdm.gitHubTokenName",
                gitHubTokenName);
    }

    public void setGitHubTokenName(String gitHubTokenName) {
        this.gitHubTokenName = gitHubTokenName;
    }

    protected String getGitHubTokenSecret() {
        return (String) resolveConfigKey("wdm.gitHubTokenSecret",
                gitHubTokenSecret);
    }

    public void setGitHubTokenSecret(String gitHubTokenSecret) {
        this.gitHubTokenSecret = gitHubTokenSecret;
    }

    protected Integer getTimeout() {
        return Integer
                .parseInt((String) resolveConfigKey("wdm.timeout", timeout));
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    protected String getTargetPath() {
        String path = (String) resolveConfigKey("wdm.targetPath", targetPath);
        if (path.contains(HOME)) {
            path = path.replace(HOME, System.getProperty("user.home"));
        }
        return path;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

}
