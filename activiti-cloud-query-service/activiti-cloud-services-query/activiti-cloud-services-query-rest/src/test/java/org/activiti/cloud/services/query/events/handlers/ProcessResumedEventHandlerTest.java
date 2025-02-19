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
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.cloud.api.process.model.events.CloudProcessResumedEvent;
import org.activiti.cloud.api.process.model.impl.events.CloudProcessResumedEventImpl;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.activiti.cloud.services.query.model.QueryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProcessResumedEventHandlerTest {

    @InjectMocks
    private ProcessResumedEventHandler handler;

    @Mock
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void handleShouldUpdateCurrentProcessInstanceStateToRunning() {
        //given
        ProcessInstanceImpl eventProcessInstance = new ProcessInstanceImpl();
        eventProcessInstance.setId(UUID.randomUUID().toString());
        CloudProcessResumedEvent event = new CloudProcessResumedEventImpl(eventProcessInstance);

        ProcessInstanceEntity currentProcessInstanceEntity = mock(ProcessInstanceEntity.class);
        given(entityManager.find(ProcessInstanceEntity.class, eventProcessInstance.getId())).willReturn(currentProcessInstanceEntity);

        //when
        handler.handle(event);

        //then
        verify(entityManager).persist(currentProcessInstanceEntity);
        verify(currentProcessInstanceEntity).setStatus(ProcessInstance.ProcessInstanceStatus.RUNNING);
        verify(currentProcessInstanceEntity).setLastModified(any(Date.class));
    }

    @Test
    public void handleShouldThrowExceptionWhenRelatedProcessInstanceIsNotFound() {
        //given
        ProcessInstanceImpl eventProcessInstance = new ProcessInstanceImpl();
        eventProcessInstance.setId(UUID.randomUUID().toString());
        CloudProcessResumedEvent event = new CloudProcessResumedEventImpl(eventProcessInstance);

        given(entityManager.find(ProcessInstanceEntity.class, eventProcessInstance.getId())).willReturn(null);

        //then
        //when
        assertThatExceptionOfType(QueryException.class)
            .isThrownBy(() -> handler.handle(event))
            .withMessageContaining("Unable to find process instance with the given id: ");
    }

    @Test
    public void getHandledEventShouldReturnProcessResumedEvent() {
        //when
        String handledEvent = handler.getHandledEvent();

        //then
        assertThat(handledEvent).isEqualTo(ProcessRuntimeEvent.ProcessEvents.PROCESS_RESUMED.name());
    }
}
