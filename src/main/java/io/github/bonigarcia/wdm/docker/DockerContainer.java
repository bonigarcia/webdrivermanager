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

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.dockerjava.api.model.Bind;

/**
 * Docker Container.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class DockerContainer {

    private List<String> exposedPorts;
    private String imageId;
    private Optional<List<Bind>> binds;
    private Optional<List<String>> envs;
    private Optional<String> network;
    private Optional<List<String>> cmd;
    private Optional<List<String>> entryPoint;
    private boolean sysadmin;
    private String containerId;
    private String containerUrl;

    private DockerContainer(DockerBuilder builder) {
        this.imageId = builder.imageId;
        this.exposedPorts = builder.exposedPorts != null ? builder.exposedPorts
                : new ArrayList<>();
        this.binds = builder.binds != null ? of(builder.binds) : empty();
        this.envs = builder.envs != null ? of(builder.envs) : empty();
        this.network = builder.network != null ? of(builder.network) : empty();
        this.cmd = builder.cmd != null ? of(builder.cmd) : empty();
        this.entryPoint = builder.entryPoint != null ? of(builder.entryPoint)
                : empty();
        this.sysadmin = builder.sysadmin;
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

    public Optional<String> getNetwork() {
        return network;
    }

    public Optional<List<String>> getCmd() {
        return cmd;
    }

    public Optional<List<String>> getEntryPoint() {
        return entryPoint;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getContainerUrl() {
        return containerUrl;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public void setContainerUrl(String containerUrl) {
        this.containerUrl = containerUrl;
    }

    public boolean isSysadmin() {
        return sysadmin;
    }

    public void setPrivileged(boolean privileged) {
        this.sysadmin = privileged;
    }

    public static class DockerBuilder {
        private String imageId;
        private List<Bind> binds;
        private List<String> envs;
        private List<String> cmd;
        private String network;
        private List<String> entryPoint;
        private boolean sysadmin = false;
        private List<String> exposedPorts;

        public DockerBuilder(String imageId) {
            this.imageId = imageId;
        }

        public DockerBuilder exposedPorts(List<String> ports) {
            this.exposedPorts = ports;
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

        public DockerBuilder sysadmin() {
            this.sysadmin = true;
            return this;
        }

        public DockerContainer build() {
            return new DockerContainer(this);
        }
    }

}
