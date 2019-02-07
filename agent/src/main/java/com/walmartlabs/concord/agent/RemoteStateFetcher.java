package com.walmartlabs.concord.agent;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2019 Walmart Inc.
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

import com.walmartlabs.concord.client.ClientUtils;
import com.walmartlabs.concord.client.ProcessApi;
import com.walmartlabs.concord.common.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class RemoteStateFetcher implements Worker.StateFetcher {

    private static final Logger log = LoggerFactory.getLogger(RemoteStateFetcher.class);

    private final ProcessApi processApi;
    private final int maxRetries;
    private final long retryDelay;

    public RemoteStateFetcher(ProcessApi processApi, int maxRetries, long retryDelay) {
        this.processApi = processApi;
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
    }

    @Override
    public void downloadState(JobRequest job) throws Exception {
        File payload = null;
        try {
            payload = ClientUtils.withRetry(maxRetries, retryDelay, () -> processApi.downloadState(job.getInstanceId()));
            IOUtils.unzip(payload.toPath(), job.getPayloadDir(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            if (payload != null) {
                delete(payload.toPath());
            }
        }
    }

    private static void delete(Path dir) {
        if (dir == null) {
            return;
        }

        try {
            IOUtils.deleteRecursively(dir);
        } catch (Exception e) {
            log.warn("delete ['{}'] -> error", dir, e);
        }
    }
}
