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
import static io.github.bonigarcia.wdm.config.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.docker.DockerHost.defaultAddress;
import static io.github.bonigarcia.wdm.versions.Shell.runAndWait;
import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback.Adapter;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
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
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;
import io.github.bonigarcia.wdm.docker.DockerContainer.DockerBuilder;
import io.github.bonigarcia.wdm.docker.DockerHubTags.DockerHubTag;
import io.github.bonigarcia.wdm.online.HttpClient;
import io.github.bonigarcia.wdm.versions.VersionComparator;

/**
 * Docker Service.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class DockerService {

    final Logger log = getLogger(lookup().lookupClass());

    private static final String BETA = "beta";
    private static final String DEV = "dev";
    private static final String LATEST_MINUS = "latest-";
    private static final String RECORDING_EXT = ".mp4";
    private static final String SEPARATOR = "_";
    private static final String DATE_FORMAT = "yyyy.MM.dd_HH.mm.ss.SSS";

    private Config config;
    private HttpClient httpClient;
    private DockerClient dockerClient;
    private ResolutionCache resolutionCache;
    private URI dockerHostUri;

    public DockerService(Config config, HttpClient httpClient,
            ResolutionCache resolutionCache) {
        this.config = config;
        this.httpClient = httpClient;
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

    public boolean isRunningInsideDocker() {
        String[] commandArray = new String[] { "bash", "-c",
                "cat /proc/self/cgroup | grep docker" };
        String commandOutput = runAndWait(false, commandArray);
        return !isNullOrEmpty(commandOutput);
    }

    public String getDefaultHost() {
        return Optional.ofNullable(dockerHostUri.getHost())
                .orElse(defaultAddress());
    }

    public String getGateway(String containerId, String network) {
        return dockerClient.inspectContainerCmd(containerId).exec()
                .getNetworkSettings().getNetworks().get(network).getGateway();
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
                log.trace("Using network: {}", network.get());
                hostConfigBuilder.withNetworkMode(network.get());
            }
            List<String> exposedPorts = dockerContainer.getExposedPorts();
            if (!exposedPorts.isEmpty()) {
                log.trace("Using exposed ports: {}", exposedPorts);
                containerConfigBuilder.withExposedPorts(exposedPorts.stream()
                        .map(ExposedPort::parse).collect(Collectors.toList()));
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
                    .withHostConfig(hostConfigBuilder)
                    .exec().getId();

            dockerClient.startContainerCmd(containerId).exec();
        }

        return containerId;
    }

    public String execCommandInContainer(String containerId,
            String... command) {
        String commandStr = Arrays.toString(command);
        log.trace("Running command {} in container {}", commandStr,
                containerId);
        String execId = dockerClient.execCreateCmd(containerId).withCmd(command)
                .withAttachStdout(true).withAttachStderr(true).exec().getId();
        final StringBuilder output = new StringBuilder();
        try {
            dockerClient.execStartCmd(execId).exec(new Adapter<Frame>() {
                @Override
                public void onNext(Frame object) {
                    output.append(new String(object.getPayload(), UTF_8));
                    super.onNext(object);
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
            log.error("Exception execution command {} on container {}",
                    commandStr, containerId, e);
            Thread.currentThread().interrupt();
        }
        log.trace("Result of command {} in container {}: {}", commandStr,
                containerId, output);
        return output.toString();
    }

    public String getBindPort(String containerId, String exposed)
            throws DockerException {
        Ports ports = dockerClient.inspectContainerCmd(containerId).exec()
                .getNetworkSettings().getPorts();
        Binding[] exposedPort = ports.getBindings()
                .get(ExposedPort.parse(exposed));
        log.trace("Port list {} -- Exposed port {} = {}", ports, exposed,
                exposedPort);
        if (ports.getBindings().isEmpty() || exposedPort.length == 0) {
            String dockerImage = dockerClient.inspectContainerCmd(containerId)
                    .exec().getConfig().getImage();
            throw new WebDriverManagerException("Port " + exposed
                    + " is not bindable in container " + dockerImage);
        }

        return exposedPort[0].getHostPortSpec();
    }

    public void pullImageIfNecessary(String cacheKey, String imageId,
            String imageVersion) throws DockerException {
        if (!resolutionCache.checkKeyInResolutionCache(cacheKey)) {
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

    public String getImageVersionFromDockerHub(
            DriverManagerType driverManagerType, String cacheKey,
            String browserName, String browserVersion, boolean androidEnabled) {
        String latestVersion = null;

        if (!resolutionCache.checkKeyInResolutionCache(cacheKey, false)) {
            VersionComparator versionComparator = new VersionComparator();
            List<String> browserList = null;
            DockerHubService dockerHubService = new DockerHubService(config,
                    httpClient);
            List<DockerHubTag> dockerHubTags;
            String tagPreffix = browserName + SEPARATOR;
            int minusIndex = getMinusIndex(browserVersion);

            String dockerBrowserImageFormat = config
                    .getDockerBrowserSelenoidImageFormat();
            switch (driverManagerType) {
            case CHROME:
            case FIREFOX:
                if (androidEnabled) {
                    dockerBrowserImageFormat = String.format(
                            config.getDockerBrowserMobileImageFormat(),
                            browserName, "");
                }
                dockerHubTags = dockerHubService
                        .listTags(dockerBrowserImageFormat);

                if (androidEnabled) {
                    browserList = dockerHubTags.stream()
                            .map(DockerHubTag::getName)
                            .sorted(versionComparator::compare)
                            .collect(toList());
                } else {
                    browserList = dockerHubTags.stream()
                            .filter(p -> p.getName().startsWith(tagPreffix))
                            .map(p -> p.getName().replace(tagPreffix, ""))
                            .sorted(versionComparator::compare)
                            .collect(toList());
                }
                latestVersion = browserList
                        .get(browserList.size() - 1 - minusIndex);
                break;

            case OPERA:
                dockerHubTags = dockerHubService
                        .listTags(dockerBrowserImageFormat);
                browserList = dockerHubTags.stream()
                        .filter(p -> p.getName().startsWith(tagPreffix))
                        .map(p -> p.getName().replace(tagPreffix, ""))
                        .sorted(versionComparator::compare).skip(1)
                        .collect(toList());
                latestVersion = browserList
                        .get(browserList.size() - 1 - minusIndex);
                break;

            case EDGE:
            case SAFARI:
                String dockerBrowserAerokubeImageFormat = String.format(
                        config.getDockerBrowserAerokubeImageFormat(),
                        browserName, "");
                dockerHubTags = dockerHubService
                        .listTags(dockerBrowserAerokubeImageFormat);
                browserList = dockerHubTags.stream().map(DockerHubTag::getName)
                        .sorted(versionComparator::compare).collect(toList());
                latestVersion = browserList
                        .get(browserList.size() - 1 - minusIndex);
                break;

            default:
                throw new WebDriverManagerException(
                        driverManagerType.getBrowserName()
                                + " is not available as Docker container");
            }
            if (minusIndex == 0) {
                log.debug("The latest version of {} in Docker Hub is {}",
                        driverManagerType.getBrowserName(), latestVersion);
            } else {
                log.debug("The version-{} of {} in Docker Hub is {}",
                        minusIndex, driverManagerType.getBrowserName(),
                        latestVersion);
            }

        } else {
            latestVersion = resolutionCache
                    .getValueFromResolutionCache(cacheKey);
        }

        return latestVersion;
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

    public String getDockerImage(String browserName, String browserVersion,
            boolean androidEnabled) {
        String dockerImageFormat;
        String dockerImage;
        switch (browserName) {
        case "edge":
        case "safari":
            dockerImageFormat = config.getDockerBrowserAerokubeImageFormat();
            dockerImage = String.format(dockerImageFormat, browserName,
                    browserVersion);
            break;

        default:
            dockerImageFormat = getDockerImageFormat(browserVersion,
                    androidEnabled);
            dockerImage = String.format(dockerImageFormat, browserName,
                    browserVersion);
            break;
        }

        log.trace("Docker image: {}", dockerImage);
        return dockerImage;
    }

    public String getDockerImageFormat(String browserVersion,
            boolean androidEnabled) {
        String dockerImageFormat;
        if (isBrowserVersionBetaOrDev(browserVersion)) {
            dockerImageFormat = config.getDockerBrowserTwilioImageFormat();
        } else if (androidEnabled) {
            dockerImageFormat = config.getDockerBrowserMobileImageFormat();
        } else {
            dockerImageFormat = config.getDockerBrowserSelenoidImageFormat();
        }
        return dockerImageFormat;
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

    /**
     * @deprecated Replaced by {@link #isBrowserVersionLatestMinus(String)}
     */
    @Deprecated
    public boolean isBrowserVersionLatesMinus(String browserVersion) {
        return isBrowserVersionLatestMinus(browserVersion);
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
        String vncAddress = browserContainer.getGateway();
        String vncPort = browserContainer.getVncPort();
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
        String noVncPort = getBindPort(containerId, dockerNoVncPort + "/tcp");
        String noVncUrlFormat = "http://%s:%s/";
        String noVncUrl = format(noVncUrlFormat, noVncHost, noVncPort);
        noVncContainer.setContainerUrl(noVncUrl);

        return noVncContainer;
    }

    public DockerContainer startBrowserContainer(String dockerImage,
            String cacheKey, String browserVersion, boolean androidEnabled) {
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
        envs.add("LANG=" + config.getDockerLang());
        envs.add("SCREEN_RESOLUTION=" + config.getDockerScreenResolution());
        envs.addAll(config.getDockerEnvVariables());

        String dockerVncPort = String.valueOf(config.getDockerVncPort());
        if (config.isDockerEnabledVnc()) {
            envs.add("ENABLE_VNC=true");
            exposedPorts.add(dockerVncPort);
        }
        if (androidEnabled) {
            envs.add("QTWEBENGINE_DISABLE_SANDBOX=1");
        }
        if (isChromeAllowedOrigins(dockerImage, browserVersion)) {
            envs.add("DRIVER_ARGS=--whitelisted-ips= --allowed-origins=*");
        }

        // network
        String network = config.getDockerNetwork();

        // extra hosts
        List<String> extraHosts = config.getDockerExtraHosts();

        // builder
        DockerBuilder dockerBuilder = DockerContainer.dockerBuilder(dockerImage)
                .exposedPorts(exposedPorts).network(network).mounts(mounts)
                .binds(binds).shmSize(shmSize).envs(envs).extraHosts(extraHosts).sysadmin();
        if (androidEnabled) {
            dockerBuilder = dockerBuilder.privileged();
        }
        DockerContainer browserContainer = dockerBuilder.build();

        String containerId = startContainer(browserContainer);
        browserContainer.setContainerId(containerId);
        String browserHost = getHost(containerId, network);
        String browserPort = getBindPort(containerId,
                dockerBrowserPort + "/tcp");
        String browserUrlFormat = "http://%s:%s/";
        if (dockerImage.contains("firefox") || androidEnabled) {
            browserUrlFormat += "wd/hub";
        }

        String browserUrl = format(browserUrlFormat, browserHost, browserPort);
        browserContainer.setContainerUrl(browserUrl);
        String gateway = getGateway(containerId, network);
        browserContainer.setGateway(gateway);
        String address = getAddress(containerId, network);
        browserContainer.setAddress(address);
        log.trace("Browser remote URL {}", browserUrl);

        if (config.isDockerEnabledVnc()) {
            String vncPort = getBindPort(containerId, dockerVncPort + "/tcp");
            browserContainer.setVncPort(vncPort);
            String vncAddress = format("vnc://%s:%s/", getDefaultHost(),
                    vncPort);
            log.debug("VNC server URL: {}", vncAddress);
            browserContainer.setVncAddress(vncAddress);
        }

        return browserContainer;
    }

    private boolean isChromeAllowedOrigins(String dockerImage,
            String browserVersion) {
        if (dockerImage.contains("chrome")) {
            browserVersion = browserVersion
                    .replaceAll(config.getBrowserVersionDetectionRegex(), "");
            return new VersionComparator().compare(browserVersion, "95") >= 0;
        }
        return false;
    }

    public DockerContainer startRecorderContainer(String dockerImage,
            String cacheKey, String recorderVersion,
            DockerContainer browserContainer) {
        dockerImage = getPrefixedDockerImage(dockerImage);
        // pull image
        pullImageIfNecessary(cacheKey, dockerImage, recorderVersion);

        // envs
        List<String> envs = new ArrayList<>();
        envs.add("BROWSER_CONTAINER_NAME=" + browserContainer.getAddress());
        Path recordingPath = getRecordingPath(browserContainer);
        envs.add("FILE_NAME=" + recordingPath.getFileName().toString());
        envs.add("VIDEO_SIZE=" + config.getDockerVideoSize());
        envs.add("FRAME_RATE=" + config.getDockerRecordingFrameRate());

        // network
        String network = config.getDockerNetwork();

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
                .endsWith(RECORDING_EXT)) {
            recordingPath = dockerRecordingPath;
        } else {
            String sessionId = browserContainer.getSessionId();
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            String recordingFileName = browserContainer.getBrowserName()
                    + SEPARATOR + dateFormat.format(now) + SEPARATOR + sessionId
                    + RECORDING_EXT;
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
