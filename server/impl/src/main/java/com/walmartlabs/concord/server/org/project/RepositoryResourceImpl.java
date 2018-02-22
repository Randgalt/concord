package com.walmartlabs.concord.server.org.project;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Wal-Mart Store, Inc.
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

import com.walmartlabs.concord.server.api.GenericOperationResultResponse;
import com.walmartlabs.concord.server.api.OperationResult;
import com.walmartlabs.concord.server.api.events.EventResource;
import com.walmartlabs.concord.server.api.org.OrganizationEntry;
import com.walmartlabs.concord.server.api.org.ResourceAccessLevel;
import com.walmartlabs.concord.server.api.org.project.ProjectEntry;
import com.walmartlabs.concord.server.api.org.project.RepositoryEntry;
import com.walmartlabs.concord.server.api.org.project.RepositoryResource;
import com.walmartlabs.concord.server.org.OrganizationManager;
import com.walmartlabs.concord.server.repository.CachedRepositoryManager.RepositoryCacheDao;
import org.sonatype.siesta.Resource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Named
public class RepositoryResourceImpl implements RepositoryResource, Resource {

    private final OrganizationManager orgManager;
    private final ProjectAccessManager accessManager;
    private final RepositoryCacheDao repositoryCacheDao;
    private final EventResource eventResource;
    private final ProjectDao projectDao;

    @Inject
    public RepositoryResourceImpl(OrganizationManager orgManager,
                                  ProjectAccessManager accessManager,
                                  RepositoryCacheDao repositoryCacheDao,
                                  EventResource eventResource,
                                  ProjectDao projectDao) {

        this.orgManager = orgManager;
        this.accessManager = accessManager;
        this.repositoryCacheDao = repositoryCacheDao;
        this.eventResource = eventResource;
        this.projectDao = projectDao;
    }

    @Override
    public GenericOperationResultResponse createOrUpdate(String orgName, String projectName, RepositoryEntry entry) {
        OrganizationEntry org = orgManager.assertAccess(orgName, true);

        UUID projectId = projectDao.getId(org.getId(), projectName);
        if (projectId == null) {
            throw new WebApplicationException("Project not found: " + projectName, Status.NOT_FOUND);
        }

        return null;
    }

    @Override
    public GenericOperationResultResponse refreshRepository(String orgName, String projectName, String repositoryName) {
        OrganizationEntry org = orgManager.assertAccess(orgName, true);

        UUID projectId = projectDao.getId(org.getId(), projectName);
        if (projectId == null) {
            throw new WebApplicationException("Project not found: " + projectName, Status.NOT_FOUND);
        }

        ProjectEntry prj = accessManager.assertProjectAccess(projectId, ResourceAccessLevel.READER, true);

        Map<String, RepositoryEntry> repos = prj.getRepositories();
        if (repos == null || !repos.containsKey(repositoryName)) {
            throw new WebApplicationException("Repository not found: " + projectName, Status.NOT_FOUND);
        }

        repositoryCacheDao.updateLastPushDate(repos.get(repositoryName).getId(), new Date());

        Map<String, Object> event = new HashMap<>();
        event.put("event", "repositoryRefresh");
        event.put("org", orgName);
        event.put("project", projectName);
        event.put("repository", repositoryName);

        eventResource.event("concord", event);

        return new GenericOperationResultResponse(OperationResult.UPDATED);
    }
}