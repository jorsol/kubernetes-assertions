/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.forge.openshift;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigBuilder;
import io.fabric8.openshift.api.model.BuildParameters;
import io.fabric8.openshift.api.model.BuildParametersBuilder;
import io.fabric8.openshift.api.model.ImageRepository;
import io.fabric8.openshift.api.model.ImageRepositoryBuilder;
import io.fabric8.utils.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class BuildConfigs {

    public static Map<String, String> createBuildLabels(String buildName) {
        Map<String, String> answer = new HashMap<>();
        answer.put("name", buildName);
        return answer;
    }

    public static ImageRepository imageRepository(String buildName, Map<String, String> labels) {
        return new ImageRepositoryBuilder().
                withApiVersion(KubernetesHelper.defaultOsApiVersion).
                withKind("ImageRepository").
                withLabels(labels).
                withName(buildName).
                build();
    }

    public static BuildParameters addBuildParameterOutput(BuildParametersBuilder builder, String imageTag) {
        return builder.
                withNewOutput().
                withImageTag(imageTag).
                // TODO add to / name  on output
                        endOutput().
                build();
    }

    public static BuildParameters addBuildParameterGitSource(BuildParametersBuilder builder, String gitUrl) {
        return builder.
                withNewSource().
                withType("Git").
                withNewGit().withUri(gitUrl).endGit().
                endSource().
                build();
    }

    public static BuildParameters addBuildParameterStiStrategy(BuildParametersBuilder builder, String image) {
        return builder.
                withNewStrategy().
                withType("STI").
                // TODO add builderImage
                        withNewStiStrategy().withImage(image).
                endStiStrategy().
                endStrategy().
                build();
    }


    public static BuildConfigBuilder buildConfigBuilder(String buildName, Map<String, String> labels, BuildParameters parameters) {
        return buildConfigBuilder(buildName, labels).
                withParameters(parameters);
    }

    public static BuildConfigBuilder buildConfigBuilder(BuildConfigBuilder builder, String secret) {
        return builder.
                addNewTrigger().
                withType("github").
                withNewGithub().withSecret(secret).endGithub().
                endTrigger().

                addNewTrigger().
                withType("generic").
                withNewGeneric().withSecret(secret).endGeneric().
                endTrigger();
    }

    public static BuildConfigBuilder buildConfigBuilder(String buildName, Map<String, String> labels) {
        return new BuildConfigBuilder().
                withApiVersion(KubernetesHelper.defaultOsApiVersion).
                withKind("BuildConfig").
                withLabels(labels).
                withName(buildName);
    }

    public static BuildConfig createBuildConfig(String buildConfigName, Map<String, String> labels, String gitUrlText, String outputImageTagText, String imageText) {
        BuildParametersBuilder parametersBuilder = new BuildParametersBuilder();
        addBuildParameterGitSource(parametersBuilder, gitUrlText);
        if (Strings.isNotBlank(outputImageTagText)) {
            addBuildParameterOutput(parametersBuilder, outputImageTagText);
        }
        if (Strings.isNotBlank(imageText)) {
            addBuildParameterStiStrategy(parametersBuilder, imageText);
        }

        BuildConfigBuilder builder = buildConfigBuilder(buildConfigName, labels, parametersBuilder.build());
        return builder.build();
    }
}