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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;
import io.github.bonigarcia.wdm.docker.DockerHubTags.DockerHubTag;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Docker Hub service.
 *
 * @author Boni Garcia
 * @since 5.0.0
 */
public class DockerHubService {

    final Logger log = getLogger(lookup().lookupClass());

    static final Long PAGE_SIZE = 1024L;

    DockerHubApi dockerHubApi;

    public DockerHubService(Config config) {
        String dockerHubUrl = config.getDockerHubUrl();
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder().client(okHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(dockerHubUrl).build();
        dockerHubApi = retrofit.create(DockerHubApi.class);
    }

    public List<DockerHubTag> listTags() {
        log.debug("Getting browser image list from Docker Hub");
        long page = 0L;
        List<DockerHubTag> results = new ArrayList<>();
        Response<DockerHubTags> listTagsResponse;

        try {
            do {
                listTagsResponse = (++page > 1)
                        ? dockerHubApi.listTagsNext(page, PAGE_SIZE).execute()
                        : dockerHubApi.listTags(PAGE_SIZE).execute();

                if (!listTagsResponse.isSuccessful()) {
                    String errorBody = listTagsResponse.errorBody().string();
                    throw new WebDriverManagerException(errorBody);
                }
                results.addAll(listTagsResponse.body().getResults());

            } while (listTagsResponse.body().next != null);
        } catch (Exception e) {
            log.warn("Exception getting browser image list from Docker Hub", e);
        }

        return results;
    }

}
