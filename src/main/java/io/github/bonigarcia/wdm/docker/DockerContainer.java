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

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Mount;

/**
 * Docker Container.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class DockerContainer {

    private List<String> exposedPorts;
    private List<String> extraHosts;
    private String imageId;
    private Optional<List<Bind>> binds;
    private Optional<List<String>> envs;
    private Optional<String> network;
    private Optional<List<String>> cmd;
    private Optional<List<String>> entryPoint;
    private Optional<List<Mount>> mounts;
    private Optional<Long> shmSize;
    private boolean privileged;
    private boolean sysadmin;
    private String containerId;
    private String containerUrl;
    private String gateway;
    private String address;
    private String vncPort;
    private String vncAddress;
    private String sessionId;
    private String browserName;
    private Path recordingPath;

    private DockerContainer(DockerBuilder builder) {
        this.imageId = builder.imageId;
        this.exposedPorts = builder.exposedPorts != null ? builder.exposedPorts
                : new ArrayList<>();
        this.extraHosts = builder.extraHosts != null ? builder.extraHosts : new ArrayList<>();
        this.binds = builder.binds != null ? of(builder.binds) : empty();
        this.envs = builder.envs != null ? of(builder.envs) : empty();
        this.network = builder.network != null ? of(builder.network) : empty();
        this.cmd = builder.cmd != null ? of(builder.cmd) : empty();
        this.entryPoint = builder.entryPoint != null ? of(builder.entryPoint)
                : empty();
        this.mounts = builder.mounts != null ? of(builder.mounts) : empty();
        this.privileged = builder.privileged;
        this.sysadmin = builder.sysadmin;
        this.shmSize = builder.shmSize != 0 ? of(builder.shmSize) : empty();
    }

    public static DockerBuilder dockerBuilder(String imageId) {
        return new DockerBuilder(imageId);
    }

    public String getImageId() {
        return imageId;
    }

    public Optional<List<Bind>> getBinds() {
        return binds;
    }

    public Optional<List<String>> getEnvs() {
        return envs;
    }

    public List<String> getExposedPorts() {
        return exposedPorts;
    }

    public String[] getExtraHosts() {
        return Arrays.copyOf(extraHosts.toArray(), extraHosts.size(), String[].class);
    }

    public Optional<String> getNetwork() {
        return network;
    }

    public Optional<List<String>> getCmd() {
        return cmd;
    }

    public Optional<List<String>> getEntryPoint() {
        return entryPoint;
    }

    public Optional<List<Mount>> getMounts() {
        return mounts;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerUrl() {
        return containerUrl;
    }

    public void setContainerUrl(String containerUrl) {
        this.containerUrl = containerUrl;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public boolean isSysadmin() {
        return sysadmin;
    }

    public Optional<Long> getShmSize() {
        return shmSize;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVncPort() {
        return vncPort;
    }

    public void setVncPort(String vncPort) {
        this.vncPort = vncPort;
    }

    public String getVncAddress() {
        return vncAddress;
    }

    public void setVncAddress(String vncAddress) {
        this.vncAddress = vncAddress;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public Path getRecordingPath() {
        return recordingPath;
    }

    public void setRecordingPath(Path recordingPath) {
        this.recordingPath = recordingPath;
    }

    public static class DockerBuilder {
        private String imageId;
        private List<Bind> binds;
        private List<String> envs;
        private List<String> cmd;
        private String network;
        private List<String> entryPoint;
        private List<Mount> mounts;
        private Long shmSize = 0L;
        private boolean privileged = false;
        private boolean sysadmin = false;
        private List<String> exposedPorts;
        private List<String> extraHosts;

        public DockerBuilder(String imageId) {
            this.imageId = imageId;
        }

        public DockerBuilder exposedPorts(List<String> ports) {
            this.exposedPorts = ports;
            return this;
        }

        public DockerBuilder extraHosts(List<String> extraHosts) {
            this.extraHosts = extraHosts;
            return this;
        }

        public DockerBuilder binds(List<String> binds) {
            this.binds = binds.stream().map(Bind::parse)
                    .collect(Collectors.toList());
            return this;
        }

        public DockerBuilder envs(List<String> envs) {
            this.envs = envs;
            return this;
        }

        public DockerBuilder network(String network) {
            this.network = network;
            return this;
        }

        public DockerBuilder cmd(List<String> cmd) {
            this.cmd = cmd;
            return this;
        }

        public DockerBuilder entryPoint(List<String> entryPoint) {
            this.entryPoint = entryPoint;
            return this;
        }

        public DockerBuilder mounts(List<Mount> mounts) {
            this.mounts = mounts;
            return this;
        }

        public DockerBuilder shmSize(Long shmSize) {
            this.shmSize = shmSize;
            return this;
        }

        public DockerBuilder sysadmin() {
            this.sysadmin = true;
            return this;
        }

        public DockerBuilder privileged() {
            this.privileged = true;
            return this;
        }

        public DockerContainer build() {
            return new DockerContainer(this);
        }
    }

}
