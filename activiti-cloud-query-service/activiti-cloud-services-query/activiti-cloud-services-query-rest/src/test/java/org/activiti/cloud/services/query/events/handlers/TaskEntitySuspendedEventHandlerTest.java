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

import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.cloud.api.task.model.impl.events.CloudTaskSuspendedEventImpl;
import org.activiti.cloud.services.query.model.QueryException;
import org.activiti.cloud.services.query.model.TaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.UUID;

import static org.activiti.cloud.services.query.events.handlers.TaskBuilder.aTask;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class TaskEntitySuspendedEventHandlerTest {

    @InjectMocks
    private TaskSuspendedEventHandler handler;

    @Mock
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void handleShouldUpdateTaskStatusToSuspended() {
        //given
        CloudTaskSuspendedEventImpl event = buildTaskSuspendedEvent();
        String taskId = event.getEntity().getId();
        TaskEntity taskEntity = aTask()
                .withId(taskId)
                .build();

        given(entityManager.find(TaskEntity.class, taskId)).willReturn(taskEntity);

        //when
        handler.handle(event);

        //then
        verify(entityManager).persist(taskEntity);
        verify(taskEntity).setStatus(Task.TaskStatus.SUSPENDED);
        verify(taskEntity).setLastModified(any(Date.class));
    }

    private CloudTaskSuspendedEventImpl buildTaskSuspendedEvent() {
        return new CloudTaskSuspendedEventImpl(new TaskImpl(UUID.randomUUID().toString(),
                                                            "task",
                                                            Task.TaskStatus.SUSPENDED));
    }

    @Test
    public void handleShouldThrowExceptionWhenNoTaskIsFoundForTheGivenId() {
        //given
        CloudTaskSuspendedEventImpl event = buildTaskSuspendedEvent();
        String taskId = event.getEntity().getId();

        given(entityManager.find(TaskEntity.class, taskId)).willReturn(null);

        //then
        //when
        assertThatExceptionOfType(QueryException.class)
            .isThrownBy(() -> handler.handle(event))
            .withMessageContaining("Unable to find task with id: " + taskId);
    }

    @Test
    public void getHandledEventShouldReturnTaskSuspendedEvent() {
        //when
        String handledEvent = handler.getHandledEvent();

        //then
        assertThat(handledEvent).isEqualTo(TaskRuntimeEvent.TaskEvents.TASK_SUSPENDED.name());
    }
}
