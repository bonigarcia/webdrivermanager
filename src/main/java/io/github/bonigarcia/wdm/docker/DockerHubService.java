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

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.docker.DockerHubTags.DockerHubTag;
import io.github.bonigarcia.wdm.online.HttpClient;

/**
 * Docker Hub service.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class DockerHubService {

    final Logger log = getLogger(lookup().lookupClass());

    static final String GET_IMAGE_TAGS_PATH_FORMAT = "v2/repositories/%s/tags?page=%s&page_size=1024";

    private Config config;
    private HttpClient client;

    public DockerHubService(Config config, HttpClient client) {
        this.config = config;
        this.client = client;
    }

    public List<DockerHubTag> listTags() {
        log.debug("Getting browser image list from Docker Hub");
        List<DockerHubTag> results = new ArrayList<>();

        String dockerHubUrl = config.getDockerHubUrl();
        String dockerImageFormat = config.getDockerBrowserStableImageFormat();
        String repo = dockerImageFormat.substring(0,
                dockerImageFormat.indexOf(":"));
        Object path = String.format(dockerHubUrl + GET_IMAGE_TAGS_PATH_FORMAT,
                repo, 1);
        Gson gson = new GsonBuilder().create();

        try {
            do {
                HttpGet createHttpGet = client
                        .createHttpGet(new URL(path.toString()));
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(client.execute(createHttpGet)
                                .getEntity().getContent()));
                DockerHubTags dockerHubTags = gson.fromJson(reader,
                        DockerHubTags.class);

                results.addAll(dockerHubTags.getResults());
                path = dockerHubTags.next;

            } while (path != null);

        } catch (Exception e) {
            log.warn("Exception getting browser image list from Docker Hub", e);
        }

        return results;
    }

}
