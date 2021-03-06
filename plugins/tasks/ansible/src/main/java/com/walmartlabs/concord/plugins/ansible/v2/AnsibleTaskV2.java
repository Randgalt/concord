package com.walmartlabs.concord.plugins.ansible.v2;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2020 Walmart Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.walmartlabs.concord.ApiClient;
import com.walmartlabs.concord.plugins.ansible.*;
import com.walmartlabs.concord.runtime.v2.model.ProcessConfiguration;
import com.walmartlabs.concord.runtime.v2.sdk.*;
import com.walmartlabs.concord.sdk.ApiConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Named("ansible")
public class AnsibleTaskV2 implements Task {

    private final Context context;
    private final ApiConfiguration apiConfiguration;
    private final ApiClient apiClient;
    private final FileService fileService;
    private final AnsibleDockerService dockerService;
    private final AnsibleSecretService secretService;
    private final ProcessConfiguration processConfiguration;

    @DefaultVariables
    Map<String, Object> defaults;

    @Inject
    public AnsibleTaskV2(Context context,
                         ApiConfiguration apiConfiguration,
                         ApiClient apiClient,
                         FileService fileService,
                         DockerService dockerService,
                         SecretService secretService,
                         ProcessConfiguration processConfiguration) {

        this.context = context;
        this.apiConfiguration = apiConfiguration;
        this.apiClient = apiClient;
        this.fileService = fileService;
        this.dockerService = new DockerServiceV2(dockerService);
        this.secretService = new SecretServiceV2(secretService);
        this.processConfiguration = processConfiguration;
    }

    @Override
    public Serializable execute(Variables input) throws Exception {
        Map<String, Object> in = input.toMap();
        Path workDir = context.workingDirectory();

        PlaybookProcessRunner runner = new PlaybookProcessRunnerFactory(dockerService, workDir)
                .create(in);

        AnsibleTask task = new AnsibleTask(apiClient,
                new AnsibleAuthFactory(secretService),
                secretService, apiConfiguration);

        UUID instanceId = Objects.requireNonNull(processConfiguration.instanceId());
        Path tmpDir = fileService.createTempDirectory("ansible");

        AnsibleContext ctx = AnsibleContext.builder()
                .instanceId(instanceId)
                .workDir(workDir)
                .tmpDir(tmpDir)
                .defaults(defaults)
                .args(in)
                .sessionToken(processConfiguration.processInfo().sessionToken())
                .eventCorrelationId(context.execution().correlationId())
                .orgName(processConfiguration.projectInfo().orgName())
                .retryCount((Integer) context.execution().state().peekFrame(context.execution().currentThreadId()).getLocal("__retry_attempNo")) // TODO provide a SDK method for this
                .build();

        TaskResult result = task.run(ctx, runner);
        if (!result.isSuccess()) {
            throw new IllegalStateException("Process finished with exit code " + result.getExitCode());
        }

        return new HashMap<>(result.getResult());
    }
}
