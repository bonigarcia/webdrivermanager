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
package io.github.bonigarcia.wdm.docker;

import static io.github.bonigarcia.wdm.config.Config.isNullOrEmpty;
import static io.github.bonigarcia.wdm.docker.DockerHost.defaultAddress;
import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient.Builder;

import io.github.bonigarcia.wdm.cache.ResolutionCache;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;
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

    private Config config;
    private HttpClient httpClient;
    private String dockerDefaultSocket;
    private int dockerRecordingTimeoutSec;
    private DockerClient dockerClient;
    private ResolutionCache resolutionCache;
    private URI dockerHostUri;

    public DockerService(Config config, HttpClient httpClient,
            ResolutionCache resolutionCache) {
        this.config = config;
        this.httpClient = httpClient;
        this.resolutionCache = resolutionCache;

        dockerDefaultSocket = config.getDockerDefaultSocket();
        dockerRecordingTimeoutSec = config.getDockerRecordingTimeoutSec();

        DockerHost dockerHostFromEnv = DockerHost.fromEnv();
        dockerClient = getDockerClient(dockerHostFromEnv.endpoint());
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

    public String getHost(String containerId, String network)
            throws DockerException {
        return Optional.ofNullable(dockerHostUri.getHost())
                .orElse(defaultAddress());
    }

    public String getGateway(String containerId, String network) {
        return dockerClient.inspectContainerCmd(containerId).exec()
                .getNetworkSettings().getNetworks().get(network).getGateway();
    }

    public synchronized String startContainer(DockerContainer dockerContainer)
            throws DockerException {
        String imageId = dockerContainer.getImageId();
        log.info("Starting Docker container {}", imageId);
        HostConfig hostConfigBuilder = new HostConfig();
        String containerId = null;

        try (CreateContainerCmd containerConfigBuilder = dockerClient
                .createContainerCmd(imageId)) {

            if (dockerContainer.isSysadmin()) {
                log.trace("Adding sysadmin capabilty");
                hostConfigBuilder.withCapAdd(Capability.SYS_ADMIN);
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

            containerId = containerConfigBuilder
                    .withHostConfig(hostConfigBuilder).exec().getId();
            dockerClient.startContainerCmd(containerId).exec();
        }

        return containerId;
    }

    public String execCommandInContainer(String containerId, String... command)
            throws DockerException {
        String commandStr = Arrays.toString(command);
        log.trace("Running command {} in container {}", commandStr,
                containerId);
        String execId = dockerClient.execCreateCmd(containerId).withCmd(command)
                .withAttachStdout(true).withAttachStderr(true).exec().getId();
        final StringBuilder output = new StringBuilder();
        dockerClient.execStartCmd(execId).exec(new Adapter<Frame>() {
            @Override
            public void onNext(Frame object) {
                output.append(new String(object.getPayload(), UTF_8));
                super.onNext(object);
            }
        });
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
            String browserVersion) throws DockerException {
        if (!config.isAvoidingResolutionCache()
                && !resolutionCache.checkKeyInResolutionCache(cacheKey)) {
            try {
                log.info(
                        "Pulling Docker image {} (this might take some time, but only the first time)",
                        imageId);
                dockerClient.pullImageCmd(imageId)
                        .exec(new Adapter<PullResponseItem>() {
                        }).awaitCompletion();
                log.trace("Docker image {} pulled", imageId);

                if (!config.isAvoidingResolutionCache()) {
                    resolutionCache.putValueInResolutionCacheIfEmpty(cacheKey,
                            browserVersion, config.getTtlForBrowsers());
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

    public String getDockerDefaultSocket() {
        return dockerDefaultSocket;
    }

    public int getDockerWaitTimeoutSec() {
        return dockerRecordingTimeoutSec;
    }

    public void close() throws IOException {
        dockerClient.close();
    }

    public void updateDockerClient(String url) {
        log.debug("Updating Docker client using URL {}", url);
        dockerClient = getDockerClient(url);
    }

    public String getLatestVersionFromDockerHub(
            DriverManagerType driverManagerType, String cacheKey) {
        String latestVersion = null;

        if (!config.isAvoidingResolutionCache() && !resolutionCache
                .checkKeyInResolutionCache(cacheKey, false)) {

            VersionComparator versionComparator = new VersionComparator();
            List<String> browserList = null;
            DockerHubService dockerHubService = new DockerHubService(config,
                    httpClient);
            List<DockerHubTag> dockerHubTags;
            String browserName = driverManagerType.getNameLowerCase();
            String tagPreffix = browserName + "_";

            switch (driverManagerType) {
            case CHROME:
            case FIREFOX:
                dockerHubTags = dockerHubService
                        .listTags(config.getDockerBrowserSelenoidImageFormat());

                browserList = dockerHubTags.stream()
                        .filter(p -> p.getName().startsWith(tagPreffix))
                        .map(p -> p.getName().replace(tagPreffix, ""))
                        .sorted(versionComparator::compare).collect(toList());
                latestVersion = browserList.get(browserList.size() - 1);
                break;

            case OPERA:
                dockerHubTags = dockerHubService
                        .listTags(config.getDockerBrowserSelenoidImageFormat());
                browserList = dockerHubTags.stream()
                        .filter(p -> p.getName().startsWith(tagPreffix))
                        .map(p -> p.getName().replace(tagPreffix, ""))
                        .sorted(versionComparator::compare).skip(1)
                        .collect(toList());
                latestVersion = browserList.get(browserList.size() - 1);
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
                latestVersion = browserList.get(browserList.size() - 1);
                break;

            default:
                throw new WebDriverManagerException(
                        driverManagerType.getBrowserName()
                                + " is not available as Docker container");
            }
            log.debug("The latest version of {} in Docker Hub is {}",
                    driverManagerType.getBrowserName(), latestVersion);

        } else {
            latestVersion = resolutionCache
                    .getValueFromResolutionCache(cacheKey);
        }

        return latestVersion;
    }

    public String getDockerImage(String browserName, String browserVersion) {
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
            dockerImageFormat = config.getDockerBrowserSelenoidImageFormat();
            dockerImage = String.format(dockerImageFormat, browserName,
                    browserVersion);
            break;
        }

        log.trace("Docker image: {}", dockerImage);
        return dockerImage;
    }

    public DockerContainer startNoVncContainer(String dockerImage,
            String cacheKey, String browserVersion,
            DockerContainer browserContainer) {
        // pull image
        pullImageIfNecessary(cacheKey, dockerImage, browserVersion);

        // exposed ports
        List<String> exposedPorts = new ArrayList<>();
        String dockerNoVncPort = String.valueOf(config.getDockerNoVncPort());
        exposedPorts.add(dockerNoVncPort);

        // envs
        List<String> envs = new ArrayList<>();
        envs.add("AUTOCONNECT=true");
        envs.add("VNC_PASSWORD=" + config.getDockerVncPassword());
        envs.add("VNC_SERVER=" + browserContainer.getGateway() + ":"
                + browserContainer.getVncPort());

        // network
        String network = config.getDockerNetwork();

        // builder
        DockerContainer noVncContainer = DockerContainer
                .dockerBuilder(dockerImage).exposedPorts(exposedPorts)
                .network(network).envs(envs).build();

        String containerId = startContainer(noVncContainer);

        noVncContainer.setContainerId(containerId);
        String noVncHost = getHost(containerId, network);
        String noVncPort = getBindPort(containerId, dockerNoVncPort + "/tcp");
        String noVncUrlFormat = "http://%s:%s/";
        String noVncUrl = format(noVncUrlFormat, noVncHost, noVncPort);
        noVncContainer.setContainerUrl(noVncUrl);

        return noVncContainer;
    }

    public DockerContainer startBrowserContainer(String dockerImage,
            String cacheKey, String browserVersion) {
        // pull image
        pullImageIfNecessary(cacheKey, dockerImage, browserVersion);

        // exposed ports
        List<String> exposedPorts = new ArrayList<>();
        String dockerBrowserPort = String
                .valueOf(config.getDockerBrowserPort());
        exposedPorts.add(dockerBrowserPort);

        // envs
        List<String> envs = new ArrayList<>();
        envs.add("TZ=" + config.getDockerTimezone());
        envs.add("LANG=" + config.getDockerLang());
        String dockerVncPort = String.valueOf(config.getDockerVncPort());
        if (config.isEnabledDockerVnc()) {
            envs.add("ENABLE_VNC=true");
            exposedPorts.add(dockerVncPort);
        }

        // network
        String network = config.getDockerNetwork();

        // builder
        DockerContainer browserContainer = DockerContainer
                .dockerBuilder(dockerImage).exposedPorts(exposedPorts)
                .network(network).envs(envs).sysadmin().build();

        String containerId = startContainer(browserContainer);
        browserContainer.setContainerId(containerId);
        String browserHost = getHost(containerId, network);
        String browserPort = getBindPort(containerId,
                dockerBrowserPort + "/tcp");
        String browserUrlFormat = "http://%s:%s/";
        if (dockerImage.contains("firefox")) {
            browserUrlFormat += "wd/hub";
        }

        String browserUrl = format(browserUrlFormat, browserHost, browserPort);
        browserContainer.setContainerUrl(browserUrl);
        String gateway = getGateway(containerId, network);
        browserContainer.setGateway(gateway);
        log.trace("Browser remote URL {}", browserUrl);

        if (config.isEnabledDockerVnc()) {
            String vncPort = getBindPort(containerId, dockerVncPort + "/tcp");
            browserContainer.setVncPort(vncPort);
        }

        return browserContainer;
    }

}
