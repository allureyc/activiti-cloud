/*
 * Copyright 2017-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.cloud.services.query.events.handlers;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.cloud.api.process.model.events.CloudProcessSuspendedEvent;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.activiti.cloud.services.query.model.QueryException;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.Optional;

public class ProcessSuspendedEventHandler implements QueryEventHandler {

    private final EntityManager entityManager;

    public ProcessSuspendedEventHandler(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void handle(CloudRuntimeEvent<?, ?> event) {
        CloudProcessSuspendedEvent suspendedEvent = (CloudProcessSuspendedEvent) event;
        String processInstanceId = suspendedEvent.getEntity().getId();

        ProcessInstanceEntity processInstanceEntity = Optional.ofNullable(entityManager.find(ProcessInstanceEntity.class,
                                                                                             processInstanceId))
                                                              .orElseThrow(() -> new QueryException("Unable to find process instance with the given id: " + processInstanceId));
        processInstanceEntity.setStatus(ProcessInstance.ProcessInstanceStatus.SUSPENDED);
        processInstanceEntity.setLastModified(new Date(suspendedEvent.getTimestamp()));
        processInstanceEntity.setSuspendedDate(new Date(suspendedEvent.getTimestamp()));

        //All important parameters like processDefinitionKey, businessKey, processDefinitionId etc. are already set by CloudProcessCreatedEvent
        entityManager.persist(processInstanceEntity);
    }

    @Override
    public String getHandledEvent() {
        return ProcessRuntimeEvent.ProcessEvents.PROCESS_SUSPENDED.name();
    }
}
