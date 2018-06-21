/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.services.query.events.handlers;

import java.util.Date;
import java.util.UUID;
import javax.persistence.EntityManager;

import org.activiti.cloud.services.query.app.repository.TaskRepository;
import org.activiti.cloud.services.query.model.ProcessInstance;
import org.activiti.runtime.api.event.TaskRuntimeEvent;
import org.activiti.runtime.api.event.impl.CloudTaskCreatedEventImpl;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.impl.TaskImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TaskCreatedEventHandlerTest {

    @InjectMocks
    private TaskCreatedEventHandler handler;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EntityManager entityManager;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void handleShouldStoreNewTaskInstance() {
        //given
        TaskImpl task = new TaskImpl(Task.TaskStatus.CREATED,
                                     "task",
                                     UUID.randomUUID().toString());
        task.setCreatedDate(new Date());
        task.setProcessInstanceId(UUID.randomUUID().toString());
        CloudTaskCreatedEventImpl event = new CloudTaskCreatedEventImpl(
                task
        );
        event.setServiceName("runtime-bundle-a");

        ProcessInstance processInstance = mock(ProcessInstance.class);
        when(entityManager.getReference(ProcessInstance.class,
                                        task.getProcessInstanceId()))
                .thenReturn(processInstance);

        //when
        handler.handle(event);

        //then
        ArgumentCaptor<org.activiti.cloud.services.query.model.Task> captor = ArgumentCaptor.forClass(org.activiti.cloud.services.query.model.Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("CREATED");
        assertThat(captor.getValue().getLastModified()).isNotNull();
        assertThat(captor.getValue().getProcessInstance()).isEqualTo(processInstance);
        assertThat(captor.getValue().getServiceName()).isEqualTo("runtime-bundle-a");
    }

    @Test
    public void getHandledEventShouldReturnTaskCreatedEvent() {
        //when
        String handledEvent = handler.getHandledEvent();

        //then
        assertThat(handledEvent).isEqualTo(TaskRuntimeEvent.TaskEvents.TASK_CREATED.name());
    }
}