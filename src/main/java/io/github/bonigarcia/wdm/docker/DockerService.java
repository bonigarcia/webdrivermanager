/*
 * (C) Copyright 2021 Boni Garcia (https://bonigarcia.github.io/)
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
package io.github.bonigarcia.wdm.docker;

import static io.github.bonigarcia.wdm.WebDriverManager.isDockerAvailable;
import static io.github.bonigarcia.wdm.config.Config.LATEST;
import static io.github.bonigarcia.wdm.config.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.config.DriverManagerType.CHROMIUM;
import static io.github.bonigarcia.wdm.config.DriverManagerType.FIREFOX;
import static io.github.bonigarcia.wdm.docker.DockerHost.defaultAddress;
import static io.github.bonigarcia.wdm.versions.Shell.runAndWait;
import static io.github.bonigarcia.wdm.versions.VersionDetector.getMajorVersion;
import static io.github.bonigarcia.wdm.versions.VersionDetector.parseVersion;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback.Adapter;
import com.github.dockerjava.api.command.AsyncDockerCmd;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.TmpfsOptions;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient.Builder;

import io.github.bonigarcia.wdm.cache.ResolutionCache;
import io.github.bonigarcia.wdm.config.Architecture;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;
import io.github.bonigarcia.wdm.docker.DockerContainer.DockerBuilder;
import io.github.bonigarcia.wdm.versions.VersionComparator;
import io.github.bonigarcia.wdm.webdriver.Recording;

/**
 * Docker Service.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class DockerService {

    final Logger log = getLogger(lookup().lookupClass());

    public static final String NETWORK_HOST = "host";
    public static final String NETWORK_DRIVER = "bridge";
    public static final String CACHE_KEY_LABEL = "-container-";
    public static final String CACHE_KEY_CUSTOM = "custom";
    private static final String SELENIUM_IMAGE_LABEL = "selenium";
    private static final String SELENIARM_IMAGE_LABEL = "seleniarm";
    private static final String DEFAULT_GATEWAY = "172.17.0.1";
    private static final String BETA = "beta";
    private static final String DEV = "dev";
    private static final String LATEST_MINUS = LATEST + "-";
    private static final int POLL_TIME_MSEC = 500;

    private Config config;
    private DockerClient dockerClient;
    private ResolutionCache resolutionCache;
    private URI dockerHostUri;

    public DockerService(Config config, ResolutionCache resolutionCache) {
        this.config = config;
        this.resolutionCache = resolutionCache;

        if (config.isDockerLocalFallback() && !isRunningInsideDocker()
                && !isDockerAvailable()) {
            log.warn(
                    "Docker is not available in your machine... local browsers are used instead");
        } else {
            this.dockerClient = createDockerClient();
        }
    }

    private DockerClient createDockerClient() {
        String dockerDaemonUrl = config.getDockerDaemonUrl();
        String dockerHost = isNullOrEmpty(dockerDaemonUrl)
                ? DockerHost.fromEnv().endpoint()
                : dockerDaemonUrl;
        return getDockerClient(dockerHost);
    }

    private DockerClient getDockerClient(String dockerHost) {
        DefaultDockerClientConfig.Builder dockerClientConfigBuilder = DefaultDockerClientConfig
                .createDefaultConfigBuilder();
        if (!isNullOrEmpty(dockerHost)) {
            dockerClientConfigBuilder.withDockerHost(dockerHost);
        }
        DockerClientConfig dockerClientConfig = dockerClientConfigBuilder
                .build();
        dockerHostUri = dockerClientConfig.getDockerHost();
        ApacheDockerHttpClient dockerHttpClient = new Builder()
                .dockerHost(dockerHostUri).build();

        return DockerClientBuilder.getInstance(dockerClientConfig)
                .withDockerHttpClient(dockerHttpClient).build();
    }

    public String getHost(String containerId, String network) {
        String host = getDefaultHost();
        if (IS_OS_LINUX && isRunningInsideDocker()) {
            host = getGateway(containerId, network);
            log.debug(
                    "WebDriverManager running inside a Docker container. Using gateway address: {}",
                    host);
        }
        return host;
    }

    private boolean isCommandResultPresent(String command) {
        String[] commandArray = new String[] { "bash", "-c", command };
        String commandOutput = runAndWait(false, commandArray);
        return !isNullOrEmpty(commandOutput);
    }

    public boolean isRunningInsideDocker() {
        return isCommandResultPresent("cat /proc/self/cgroup | grep docker")
                || isCommandResultPresent(
                        "cat /proc/self/mountinfo | grep docker/containers");
    }

    public String getDefaultHost() {
        return Optional.ofNullable(dockerHostUri.getHost())
                .orElse(defaultAddress());
    }

    public String getGateway(String containerId, String network) {
        String gateway = dockerClient.inspectContainerCmd(containerId).exec()
                .getNetworkSettings().getNetworks().get(network).getGateway();
        if (isNullOrEmpty(gateway)) {
            return DEFAULT_GATEWAY;
        }
        return gateway;
    }

    public String getAddress(String containerId, String network) {
        return dockerClient.inspectContainerCmd(containerId).exec()
                .getNetworkSettings().getNetworks().get(network).getIpAddress();
    }

    public synchronized String startContainer(DockerContainer dockerContainer)
            throws DockerException {
        String imageId = dockerContainer.getImageId();
        log.info("Starting Docker container {}", imageId);
        HostConfig hostConfigBuilder = new HostConfig();
        String containerId;

        try (CreateContainerCmd containerConfigBuilder = dockerClient
                .createContainerCmd(imageId)) {
            Optional<String> containerName = dockerContainer.getContainerName();
            if (containerName.isPresent()) {
                log.trace("Using container name: {}", containerName.get());
                containerConfigBuilder.withName(containerName.get());
            }

            boolean privileged = dockerContainer.isPrivileged();
            if (privileged) {
                log.trace("Using privileged mode");
                hostConfigBuilder.withPrivileged(true);
            }
            if (dockerContainer.isSysadmin()) {
                log.trace("Adding sysadmin capability");
                hostConfigBuilder.withCapAdd(Capability.SYS_ADMIN);
            }

            Optional<Long> shmSize = dockerContainer.getShmSize();
            if (shmSize.isPresent()) {
                log.trace("Using shm size: {}", shmSize.get());
                hostConfigBuilder.withShmSize(shmSize.get());
            }

            Optional<String> network = dockerContainer.getNetwork();
            if (network.isPresent()) {
                String dockerNetwork = network.get();
                createDockerNetworkIfNotExists(dockerNetwork);
                log.trace("Using network: {}", dockerNetwork);
                hostConfigBuilder.withNetworkMode(dockerNetwork);
            }
            List<String> exposedPorts = dockerContainer.getExposedPorts();
            if (!exposedPorts.isEmpty()) {
                log.trace("Using exposed ports: {}", exposedPorts);
                containerConfigBuilder.withExposedPorts(exposedPorts.stream()
                        .map(ExposedPort::parse).collect(Collectors.toList()));
                hostConfigBuilder.withPortBindings(exposedPorts.stream()
                        .map(PortBinding::parse).collect(Collectors.toList()));
                hostConfigBuilder.withPublishAllPorts(true);
            }
            Optional<List<Bind>> binds = dockerContainer.getBinds();
            if (binds.isPresent()) {
                log.trace("Using binds: {}", binds.get());
                hostConfigBuilder.withBinds(binds.get());
            }

            Optional<List<Mount>> mounts = dockerContainer.getMounts();
            if (mounts.isPresent()) {
                log.trace("Using mounts: {}", mounts.get());
                hostConfigBuilder.withMounts(mounts.get());
            }

            Optional<List<String>> envs = dockerContainer.getEnvs();
            if (envs.isPresent()) {
                log.trace("Using envs: {}", envs.get());
                containerConfigBuilder
                        .withEnv(envs.get().toArray(new String[] {}));
            }
            Optional<List<String>> cmd = dockerContainer.getCmd();
            if (cmd.isPresent()) {
                log.trace("Using cmd: {}", cmd.get());
                containerConfigBuilder
                        .withCmd(cmd.get().toArray(new String[] {}));
            }
            Optional<List<String>> entryPoint = dockerContainer.getEntryPoint();
            if (entryPoint.isPresent()) {
                log.trace("Using entryPoint: {}", entryPoint.get());
                containerConfigBuilder.withEntrypoint(
                        entryPoint.get().toArray(new String[] {}));
            }

            hostConfigBuilder.withExtraHosts(dockerContainer.getExtraHosts());

            containerId = containerConfigBuilder
                    .withHostConfig(hostConfigBuilder).exec().getId();

            dockerClient.startContainerCmd(containerId).exec();
        }

        return containerId;
    }

    public void createDockerNetworkIfNotExists(String networkName) {
        List<Network> networks = dockerClient.listNetworksCmd().exec();
        boolean networkExists = networks.stream()
                .anyMatch(network -> network.getName().equals(networkName));
        if (networkExists) {
            log.trace("Docker network {} already exits", networkName);
        } else {
            try (CreateNetworkCmd networkCmd = dockerClient
                    .createNetworkCmd()) {
                CreateNetworkResponse networkResponse = networkCmd
                        .withName(networkName).withDriver(NETWORK_DRIVER)
                        .exec();
                log.trace("Docker network {} created with id {}", networkName,
                        networkResponse.getId());
            }
        }
    }

    public String execCommandInContainer(String containerId,
            String... command) {
        String commandStr = Arrays.toString(command);
        log.trace("Running command {} in container {}", commandStr,
                containerId);
        String execId = dockerClient.execCreateCmd(containerId).withCmd(command)
                .withAttachStdout(true).withAttachStderr(true).exec().getId();
        ExecStartCmd execStartCmd = dockerClient.execStartCmd(execId);
        return getOutputFromCmd(execStartCmd);
    }

    public String getOutputFromCmd(AsyncDockerCmd<?, Frame> execStartCmd) {
        final StringBuilder output = new StringBuilder();
        try {
            execStartCmd.exec(new Adapter<Frame>() {
                @Override
                public void onNext(Frame object) {
                    output.append(new String(object.getPayload(), UTF_8));
                    super.onNext(object);
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
            log.error("Exception executing command on container {}",
                    e.getMessage());
            Thread.currentThread().interrupt();
        }
        return output.toString().trim();
    }

    public String getBindPort(String containerId, String exposed)
            throws DockerException {

        String bindPort = null;
        int waitTimeoutSec = config.getTimeout();
        long timeoutMs = System.currentTimeMillis()
                + Duration.ofSeconds(waitTimeoutSec).toMillis();
        do {
            Ports ports = dockerClient.inspectContainerCmd(containerId).exec()
                    .getNetworkSettings().getPorts();
            Binding[] exposedPort = ports.getBindings()
                    .get(ExposedPort.parse(exposed));
            log.trace("Port list {} -- Exposed port {} = {}", ports, exposed,
                    exposedPort);

            if (ports.getBindings() == null || exposedPort == null
                    || ports.getBindings().isEmpty()
                    || exposedPort.length == 0) {
                String dockerImage = dockerClient
                        .inspectContainerCmd(containerId).exec().getConfig()
                        .getImage();
                if (currentTimeMillis() > timeoutMs) {
                    throw new WebDriverManagerException("Timeout of "
                            + waitTimeoutSec
                            + " getting bind port in container " + dockerImage);
                } else {
                    try {
                        log.trace("Port {} is not bindable in container {}",
                                exposed, dockerImage);
                        Thread.sleep(POLL_TIME_MSEC);
                    } catch (InterruptedException e) {
                        log.warn("Interrupted exception getting bind port", e);
                        Thread.currentThread().interrupt();
                    }
                }
            } else {
                bindPort = exposedPort[0].getHostPortSpec();
            }
        } while (bindPort == null);

        return bindPort;
    }

    public void pullImageIfNecessary(String cacheKey, String imageId,
            String imageVersion) throws DockerException {
        if (!config.getDockerAvoidPulling()
                && !resolutionCache.checkKeyInResolutionCache(cacheKey)) {
            try {
                log.info(
                        "Pulling Docker image {} (this might take some time, but only the first time)",
                        imageId);
                dockerClient.pullImageCmd(imageId)
                        .exec(new Adapter<PullResponseItem>() {
                        }).awaitCompletion();
                log.trace("Docker image {} pulled", imageId);

                if (!config.isAvoidResolutionCache()) {
                    resolutionCache.putValueInResolutionCacheIfEmpty(cacheKey,
                            imageVersion, config.getTtlForBrowsers());
                }
            } catch (Exception e) {
                log.warn("Exception pulling image {}: {}", imageId,
                        e.getMessage());
            }
        }
    }

    public synchronized void stopAndRemoveContainer(
            DockerContainer dockerContainer) {
        String containerId = dockerContainer.getContainerId();
        String imageId = dockerContainer.getImageId();

        log.info("Stopping Docker container {}", imageId);
        try {
            stopContainer(containerId);
            removeContainer(containerId);
        } catch (Exception e) {
            log.warn("Exception stopping container {}", imageId, e);
        }
    }

    public synchronized void stopContainer(String containerId)
            throws DockerException {
        int stopTimeoutSec = config.getDockerStopTimeoutSec();
        if (stopTimeoutSec == 0) {
            log.trace("Killing container {}", containerId);
            dockerClient.killContainerCmd(containerId).exec();
        } else {
            log.trace("Stopping container {} (timeout {} seconds)", containerId,
                    stopTimeoutSec);
            dockerClient.stopContainerCmd(containerId)
                    .withTimeout(stopTimeoutSec).exec();
        }
    }

    public synchronized void removeContainer(String containerId)
            throws DockerException {
        log.trace("Removing container {}", containerId);
        int stopTimeoutSec = config.getDockerStopTimeoutSec();
        if (stopTimeoutSec == 0) {
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        } else {
            dockerClient.removeContainerCmd(containerId).exec();
        }
    }

    public void close() throws IOException {
        dockerClient.close();
    }

    public void updateDockerClient(String dockerHost) {
        log.debug("Updating Docker client using {}", dockerHost);
        dockerClient = getDockerClient(dockerHost);
    }

    public String getDockerImageVersion(DriverManagerType driverManagerType,
            String cacheKey, String browserName, String browserVersion) {
        String latestVersion = LATEST;

        int minusIndex = getMinusIndex(browserVersion);
        if (minusIndex != 0) {
            if (!resolutionCache.checkKeyInResolutionCache(cacheKey, false)) {
                String dockerImage = getDockerImage(driverManagerType,
                        browserName, latestVersion);
                String cacheKeyLatest = getCacheKey(browserName, latestVersion);
                String browserVersionFromContainer = getBrowserVersionFromContainer(
                        driverManagerType, cacheKeyLatest, latestVersion,
                        dockerImage);
                int majorBrowserVersion = Integer
                        .parseInt(getMajorVersion(browserVersionFromContainer));
                latestVersion = String.valueOf(majorBrowserVersion - minusIndex)
                        + ".0";
                if (!resolutionCache.checkKeyInResolutionCache(cacheKey,
                        false)) {
                    dockerImage = getDockerImage(driverManagerType, browserName,
                            latestVersion);
                    pullImageIfNecessary(cacheKey, dockerImage, latestVersion);
                }
            } else {
                latestVersion = resolutionCache
                        .getValueFromResolutionCache(cacheKey);
            }
        }

        return latestVersion;
    }

    public static String getCacheKey(String browserName,
            String browserVersion) {
        return browserName + CACHE_KEY_LABEL + browserVersion;
    }

    public String getBrowserVersionFromContainer(
            DriverManagerType driverManagerType, String cacheKey,
            String browserVersion, String dockerImage) {
        String browserVersionFromContainer = LATEST;
        try {
            pullImageIfNecessary(cacheKey, dockerImage, browserVersion);

            List<String> cmd = new ArrayList<>();
            switch (driverManagerType) {
            case CHROME:
                cmd.add("google-chrome");
                break;
            case FIREFOX:
                cmd.add("firefox");
                break;
            case EDGE:
                cmd.add("microsoft-edge");
                break;
            default:
                throw new WebDriverManagerException(
                        driverManagerType.getBrowserName()
                                + " is not available as Docker container");
            }
            cmd.add("--version");
            try (CreateContainerCmd containerCmd = dockerClient
                    .createContainerCmd(dockerImage)) {
                CreateContainerResponse container = containerCmd.withCmd(cmd)
                        .withHostConfig(
                                HostConfig.newHostConfig().withAutoRemove(true))
                        .exec();
                dockerClient.startContainerCmd(container.getId()).exec();
                LogContainerCmd logContainerCmd = dockerClient
                        .logContainerCmd(container.getId()).withStdOut(true)
                        .withFollowStream(true);
                browserVersionFromContainer = parseVersion(
                        getOutputFromCmd(logContainerCmd));
            }

        } catch (Exception e) {
            log.warn("Exception discovering browser version from container: {}",
                    e.getMessage());
        }
        return browserVersionFromContainer;
    }

    public int getMinusIndex(String browserVersion) {
        int minusIndex = 0;
        if (isBrowserVersionLatestMinus(browserVersion)) {
            minusIndex = Integer.parseInt(browserVersion
                    .substring(browserVersion.indexOf(LATEST_MINUS)
                            + LATEST_MINUS.length()));
        }
        return minusIndex;
    }

    public String getDockerImage(DriverManagerType driverManagerType,
            String browserName, String browserVersion) {
        String dockerImageFormat = getDockerImageFormat();
        String imageLabel = SELENIUM_IMAGE_LABEL;
        if (config.getArchitecture() == Architecture.ARM64
                && (driverManagerType == CHROMIUM
                        || driverManagerType == FIREFOX)) {
            imageLabel = SELENIARM_IMAGE_LABEL;
        }
        String dockerImage = String.format(dockerImageFormat, imageLabel,
                browserName, browserVersion);
        log.trace("Docker image: {}", dockerImage);
        return dockerImage;
    }

    public String getDockerImageFormat() {
        return config.getDockerBrowserImageFormat();
    }

    public boolean isBrowserVersionWildCard(String browserVersion) {
        return isBrowserVersionBetaOrDev(browserVersion)
                || isBrowserVersionLatestMinus(browserVersion);
    }

    public boolean isBrowserVersionBetaOrDev(String browserVersion) {
        return browserVersion.equalsIgnoreCase(BETA)
                || browserVersion.equalsIgnoreCase(DEV);
    }

    public boolean isBrowserVersionLatestMinus(String browserVersion) {
        return browserVersion.toLowerCase(ROOT).contains(LATEST_MINUS);
    }

    private String getPrefixedDockerImage(String dockerImage) {
        String newDockerImage = dockerImage;
        String prefix = config.getDockerPrivateEndpoint();
        if (StringUtils.isNotBlank(prefix)) {
            newDockerImage = String.format("%s/%s", prefix, dockerImage);
        }
        return newDockerImage;
    }

    public DockerContainer startNoVncContainer(String dockerImage,
            String cacheKey, String browserVersion,
            DockerContainer browserContainer) {

        dockerImage = getPrefixedDockerImage(dockerImage);
        // pull image
        pullImageIfNecessary(cacheKey, dockerImage, browserVersion);

        // exposed ports
        List<String> exposedPorts = new ArrayList<>();
        String dockerNoVncPort = String.valueOf(config.getDockerNoVncPort());
        exposedPorts.add(dockerNoVncPort);

        // envs
        List<String> envs = new ArrayList<>();
        envs.add("AUTOCONNECT=true");
        envs.add("VIEW_ONLY=" + config.isDockerViewOnly());
        envs.add("VNC_PASSWORD=" + config.getDockerVncPassword());
        String vncAddress = browserContainer.getAddress();
        String vncPort = String.valueOf(config.getDockerVncPort());
        envs.add("VNC_SERVER=" + vncAddress + ":" + vncPort);

        // network
        String network = config.getDockerNetwork();

        // extra hosts
        List<String> extraHosts = config.getDockerExtraHosts();

        // builder
        DockerContainer noVncContainer = DockerContainer
                .dockerBuilder(dockerImage).exposedPorts(exposedPorts)
                .network(network).extraHosts(extraHosts).envs(envs).build();

        String containerId = startContainer(noVncContainer);

        noVncContainer.setContainerId(containerId);
        String noVncHost = getDefaultHost();
        String noVncPort = isHost(network) ? dockerNoVncPort
                : getBindPort(containerId, dockerNoVncPort + "/tcp");
        String noVncUrlFormat = "http://%s:%s/";
        String noVncUrl = format(noVncUrlFormat, noVncHost, noVncPort);
        noVncContainer.setContainerUrl(noVncUrl);

        return noVncContainer;
    }

    public DockerContainer startBrowserContainer(String dockerImage,
            String cacheKey, String browserVersion) {
        dockerImage = getPrefixedDockerImage(dockerImage);
        // pull image
        pullImageIfNecessary(cacheKey, dockerImage, browserVersion);

        // exposed ports
        List<String> exposedPorts = new ArrayList<>();
        String dockerBrowserPort = String
                .valueOf(config.getDockerBrowserPort());
        exposedPorts.add(dockerBrowserPort);

        // shmSize
        long shmSize = config.getDockerMemSizeBytes(config.getDockerShmSize());

        // mounts
        List<Mount> mounts = new ArrayList<>();
        Mount tmpfsMount = new Mount()
                .withTmpfsOptions(new TmpfsOptions().withSizeBytes(config
                        .getDockerMemSizeBytes(config.getDockerTmpfsSize())))
                .withTarget(config.getDockerTmpfsMount());
        mounts.add(tmpfsMount);

        // binds
        List<String> binds = new ArrayList<>();
        String dockerVolumes = config.getDockerVolumes();
        if (!isNullOrEmpty(dockerVolumes)) {
            List<String> volumeList = Arrays.asList(dockerVolumes.split(","));
            log.trace("Using custom volumes: {}", volumeList);
            binds.addAll(volumeList);
        }

        // envs
        List<String> envs = new ArrayList<>();
        envs.add("TZ=" + config.getDockerTimezone());
        envs.add("LANGUAGE=" + config.getDockerLang());
        envs.add("SCREEN_RESOLUTION=" + config.getDockerScreenResolution());
        envs.addAll(config.getDockerEnvVariables());

        String dockerVncPort = String.valueOf(config.getDockerVncPort());
        if (config.isDockerEnabledVnc()) {
            envs.add("ENABLE_VNC=true");
            exposedPorts.add(dockerVncPort);
        }
        if (isChromeAllowedOrigins(dockerImage, browserVersion)) {
            envs.add("DRIVER_ARGS=--whitelisted-ips= --allowed-origins=*");
        }

        // network
        String network = config.getDockerNetwork();

        // extra hosts
        List<String> extraHosts = config.getDockerExtraHosts();

        // builder
        String containerName = "selenium_"
                + UUID.randomUUID().toString().substring(0, 6);
        DockerBuilder dockerBuilder = DockerContainer.dockerBuilder(dockerImage)
                .exposedPorts(exposedPorts).network(network).mounts(mounts)
                .binds(binds).shmSize(shmSize).envs(envs).extraHosts(extraHosts)
                .sysadmin().containerName(containerName);
        DockerContainer browserContainer = dockerBuilder.build();

        String containerId = startContainer(browserContainer);
        browserContainer.setContainerId(containerId);
        browserContainer.setContainerName(Optional.of(containerName));

        String gateway = getGateway(containerId, network);
        browserContainer.setGateway(gateway);
        String browserHost = getHost(containerId, network);
        String browserPort = isHost(network) ? dockerBrowserPort
                : getBindPort(containerId, dockerBrowserPort + "/tcp");
        String browserUrlFormat = "http://%s:%s/";
        if (dockerImage.contains("firefox")) {
            browserUrlFormat += "wd/hub";
        }

        String browserUrl = format(browserUrlFormat, browserHost, browserPort);
        browserContainer.setContainerUrl(browserUrl);
        String address = getAddress(containerId, network);
        browserContainer.setAddress(address);
        log.trace("Browser remote URL {}", browserUrl);

        if (config.isDockerEnabledVnc()) {
            String vncPort = isHost(network) ? dockerVncPort
                    : getBindPort(containerId, dockerVncPort + "/tcp");
            browserContainer.setVncPort(vncPort);
            String vncAddress = format("vnc://%s:%s/", getDefaultHost(),
                    vncPort);
            log.debug("VNC server URL: {}", vncAddress);
            browserContainer.setVncAddress(vncAddress);
        }

        return browserContainer;
    }

    private boolean isHost(String dockerNetwork) {
        return dockerNetwork.equalsIgnoreCase(NETWORK_HOST);
    }

    private boolean isChromeAllowedOrigins(String dockerImage,
            String browserVersion) {
        if (dockerImage.contains("chrome")) {
            String parsedVersion = parseVersion(browserVersion);
            return new VersionComparator().compare(parsedVersion, "95") >= 0;
        }
        return false;
    }

    public DockerContainer startRecorderContainer(String dockerImage,
            String cacheKey, String recorderVersion,
            DockerContainer browserContainer) {
        dockerImage = getPrefixedDockerImage(dockerImage);

        // pull image
        pullImageIfNecessary(cacheKey, dockerImage, recorderVersion);

        // network
        String network = config.getDockerNetwork();

        // envs
        List<String> envs = new ArrayList<>();
        Optional<String> containerName = browserContainer.getContainerName();
        if (containerName.isPresent()) {
            envs.add("BROWSER_CONTAINER_NAME=" + containerName.get());
        }
        Path recordingPath = getRecordingPath(browserContainer);
        envs.add("FILE_NAME=" + recordingPath.getFileName().toString());
        envs.add("VIDEO_SIZE=" + config.getDockerVideoSize());
        envs.add("FRAME_RATE=" + config.getDockerRecordingFrameRate());

        // extra hosts
        List<String> extraHosts = config.getDockerExtraHosts();

        // binds
        List<String> binds = new ArrayList<>();
        binds.add(recordingPath.toAbsolutePath().getParent().toString()
                + ":/data");
        String dockerVolumes = config.getDockerVolumes();
        if (!isNullOrEmpty(dockerVolumes)) {
            List<String> volumeList = Arrays.asList(dockerVolumes.split(","));
            log.trace("Using custom volumes: {}", volumeList);
            binds.addAll(volumeList);
        }

        // builder
        DockerContainer recorderContainer = DockerContainer
                .dockerBuilder(dockerImage).network(network).envs(envs)
                .binds(binds).extraHosts(extraHosts).sysadmin().build();

        String containerId = startContainer(recorderContainer);
        recorderContainer.setContainerId(containerId);
        recorderContainer.setRecordingPath(recordingPath);

        return recorderContainer;
    }

    public Path getRecordingPath(DockerContainer browserContainer) {
        Path recordingPath;
        Path dockerRecordingPath = config.getDockerRecordingOutput();

        if (dockerRecordingPath.toString().toLowerCase(ROOT)
                .endsWith(Recording.DOCKER_RECORDING_EXT)) {
            recordingPath = dockerRecordingPath;
        } else {
            String recordingFileName = Recording.getRecordingNameForDocker(
                    browserContainer.getBrowserName(),
                    browserContainer.getSessionId());
            String prefix = config.getDockerRecordingPrefix();
            if (!isNullOrEmpty(prefix)) {
                recordingFileName = prefix + recordingFileName;
            }
            recordingPath = Paths.get(dockerRecordingPath.toString(),
                    recordingFileName);
        }

        return recordingPath;
    }

    public String getVersionFromImage(String dockerImage) {
        return dockerImage.substring(dockerImage.indexOf(":") + 1);
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

}
