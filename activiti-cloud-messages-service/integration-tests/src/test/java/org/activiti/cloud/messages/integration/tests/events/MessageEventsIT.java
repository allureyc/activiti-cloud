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
package org.activiti.cloud.messages.integration.tests.events;

import org.activiti.api.model.shared.Payload;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.StartMessageDeploymentDefinition;
import org.activiti.api.process.model.StartMessageSubscription;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.StartMessageDeployedEvent;
import org.activiti.api.process.model.payloads.DeleteProcessPayload;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.process.model.results.ProcessInstanceResult;
import org.activiti.cloud.services.core.commands.CommandEndpoint;
import org.activiti.cloud.services.core.commands.ReceiveMessageCmdExecutor;
import org.activiti.cloud.services.core.commands.StartMessageCmdExecutor;
import org.activiti.cloud.services.messages.events.producer.*;
import org.activiti.cloud.services.test.containers.KeycloakContainerApplicationInitializer;
import org.activiti.cloud.services.test.containers.RabbitMQContainerApplicationInitializer;
import org.activiti.cloud.starter.rb.configuration.ActivitiRuntimeBundle;
import org.activiti.engine.RuntimeService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.platform=postgresql",
                "activiti.cloud.application.name=messages-app",
                "spring.application.name=rb",
                "spring.jmx.enabled=false",
        })
@DirtiesContext
@Testcontainers
@ContextConfiguration(initializers = {RabbitMQContainerApplicationInitializer.class,
                                      KeycloakContainerApplicationInitializer.class})
class MessageEventsIT {

    @Container
    private static PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:10");

    private static final String BOUNDARY_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS1 = "BoundarySubprocessThrowCatchMessageIT_Process1";
    private static final String EVENT_SUBPROCESS_NON_INTERRUPTING_THROW_CATCH_MESSAGE_IT_PROCESS1 = "EventSubprocessNonInterruptingThrowCatchMessageIT_Process1";
    private static final String EVENT_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS1 = "EventSubprocessThrowCatchMessageIT_Process1";
    private static final String BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS1 = "BoundaryThrowCatchMessageIT_Process1";
    private static final String THROW_CATCH_MESSAGE_IT_PROCESS1 = "ThrowCatchMessageIT_Process1";
    private static final String CORRELATION_ID = "correlationId";
    private static final String CORRELATION_KEY = "correlationKey";
    private static final String BUSINESS_KEY = "businessKey";
    private static final String INTERMEDIATE_CATCH_MESSAGE_PROCESS = "IntermediateCatchMessageProcess";
    private static final String INTERMEDIATE_THROW_MESSAGE_PROCESS = "IntermediateThrowMessageProcess";

    @Autowired
    private RuntimeService runtimeService;

    @SpyBean
    private BpmnMessageReceivedEventMessageProducer bpmnMessageReceivedEventMessageProducer;

    @SpyBean
    private BpmnMessageSentEventMessageProducer bpmnMessageSentEventMessageProducer;

    @SpyBean
    private BpmnMessageWaitingEventMessageProducer bpmnMessageWaitingEventMessageProducer;

    @SpyBean
    private StartMessageCmdExecutor startMessageCmdExecutor;

    @SpyBean
    private ReceiveMessageCmdExecutor receiveMessageCmdExecutor;

    @SpyBean
    private MessageSubscriptionCancelledEventMessageProducer messageSubscriptionCancelledEventMessageProducer;

    @SpyBean(reset = MockReset.NONE)
    private StartMessageDeployedEventMessageProducer startMessageDeployedEventMessageProducer;

    @Autowired
    private CommandEndpoint<Payload> commandEndpoint;

    @Autowired
    private MessageGroupStore messageGroupStore;

    @SuppressWarnings("unused")
    @SpringBootApplication
    @ActivitiRuntimeBundle
    static class Application {
        /* no op */
    }

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresContainer.getPassword());
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty("spring.datasource.url");
        System.clearProperty("spring.datasource.username");
        System.clearProperty("spring.datasource.password");
    }

    @Test
    void shouldProduceStartMessageDeployedEvents() {
        // given
        String expectedStartEventNames[] = {
                "EventSubprocessThrowEndMessage",
                "EventSubprocessStartProcess3",
                "BoundaryThrowEndMessage",
                "BoundaryThrowIntermediateMessage",
                "EventSubprocessNonInterruptingThrowEndMessage",
                "EventSubprocessStartProcessNonInterrupting3",
                "ThrowEndMessage",
                "ThrowIntermediateMessage",
                "BoundarySubprocessThrowEndMessage",
                "SartBoundarySubprocessThrowIntermediateMessage"
        };

        // when
        ArgumentCaptor<StartMessageDeployedEvent> argumentCaptor = ArgumentCaptor
                .forClass(StartMessageDeployedEvent.class);

        // then
        verify(startMessageDeployedEventMessageProducer, atLeast(expectedStartEventNames.length))
                .onEvent(argumentCaptor.capture());

        assertThat(argumentCaptor.getAllValues()).extracting(StartMessageDeployedEvent::getEntity)
                .extracting(StartMessageDeploymentDefinition::getMessageSubscription)
                .extracting(StartMessageSubscription::getEventName)
                .contains(expectedStartEventNames);

        Stream.of(expectedStartEventNames)
                .forEach(messageName -> {
                    String groupId = "messages-app:" + messageName;
                    assertThat(messageGroupStore.getMessagesForGroup(groupId)).hasSize(1);
                });
    }

    @Test
    void shouldThrowCatchBpmnMessage() {
        //given
        StartProcessPayload throwProcessPayload = ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(INTERMEDIATE_THROW_MESSAGE_PROCESS)
                .withBusinessKey(BUSINESS_KEY)
                .build();

        StartProcessPayload catchProcessPayload = ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                .withBusinessKey(BUSINESS_KEY)
                .build();
        //when
        commandEndpoint.execute(throwProcessPayload);
        commandEndpoint.execute(catchProcessPayload);

        // then
        await().untilAsserted(() -> {
            verify(bpmnMessageSentEventMessageProducer, times(1)).onEvent(any());
            verify(bpmnMessageWaitingEventMessageProducer, times(1)).onEvent(any());
            verify(bpmnMessageReceivedEventMessageProducer, times(1)).onEvent(any());

            verify(receiveMessageCmdExecutor, times(1)).execute(any());
            verify(startMessageCmdExecutor, never()).execute(any());
        });
    }

    @Test
    void shouldCompleteComplexBpmnMessageEventProcessWithIntermediateCatchEvent() {
        //given
        StartProcessPayload throwProcessPayload = ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(THROW_CATCH_MESSAGE_IT_PROCESS1)
                .withBusinessKey(BUSINESS_KEY)
                .withVariable(CORRELATION_KEY, CORRELATION_ID)
                .build();
        //when
        commandEndpoint.execute(throwProcessPayload);

        //then
        await().untilAsserted(() -> {
            verify(bpmnMessageSentEventMessageProducer, times(3)).onEvent(any());
            verify(startMessageCmdExecutor, times(2)).execute(any());
            verify(bpmnMessageWaitingEventMessageProducer, times(1)).onEvent(any());
            verify(receiveMessageCmdExecutor, times(1)).execute(any());
            verify(bpmnMessageReceivedEventMessageProducer, times(3)).onEvent(any());
        });

    }

    @Test
    void shouldCompleteComplexBpmnMessageEventProcessWithBoundaryCatchEvent() {
        //given
        StartProcessPayload throwProcessPayload = ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS1)
                .withBusinessKey(BUSINESS_KEY)
                .build();
        //when
        commandEndpoint.execute(throwProcessPayload);

        // then
        await().untilAsserted(() -> {
            verify(bpmnMessageSentEventMessageProducer, times(3)).onEvent(any());
            verify(bpmnMessageWaitingEventMessageProducer, times(1)).onEvent(any());
            verify(bpmnMessageReceivedEventMessageProducer, times(3)).onEvent(any());

            verify(receiveMessageCmdExecutor, times(1)).execute(any());
            verify(startMessageCmdExecutor, times(2)).execute(any());
        });
    }


    @Test
    void shouldCompleteComplexBpmnMessageEventMultipleProcessesWithIntermediateCatchEvent() {
        // given
        int processInstances = 10;

        //when
        IntStream.rangeClosed(1, processInstances)
                .mapToObj(i -> ProcessPayloadBuilder.start()
                        .withProcessDefinitionKey(THROW_CATCH_MESSAGE_IT_PROCESS1)
                        .withBusinessKey(BUSINESS_KEY + i)
                        .build())
                .forEach(commandEndpoint::execute);

        // then
        await().untilAsserted(() -> {
            verify(bpmnMessageSentEventMessageProducer, times(3 * processInstances)).onEvent(any());
            verify(bpmnMessageWaitingEventMessageProducer, times(processInstances)).onEvent(any());
            verify(bpmnMessageReceivedEventMessageProducer, times(3 * processInstances))
                    .onEvent(any());

            verify(receiveMessageCmdExecutor, times(processInstances)).execute(any());
            verify(startMessageCmdExecutor, times(2 * processInstances)).execute(any());
        });
    }

    @Test
    void shouldCompleteComplexBpmnMessageEventMultipleProcessesWithBoundaryTaskMessageCatchEvent() {
        // given
        int processInstances = 10;

        //when
        IntStream.rangeClosed(1, processInstances)
                .mapToObj(i -> ProcessPayloadBuilder.start()
                        .withProcessDefinitionKey(BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS1)
                        .withBusinessKey(BUSINESS_KEY + i)
                        .build())
                .forEach(commandEndpoint::execute);

        // then
        await().untilAsserted(() -> {
            verify(bpmnMessageSentEventMessageProducer, times(3 * processInstances)).onEvent(any());
            verify(bpmnMessageWaitingEventMessageProducer, times(processInstances)).onEvent(any());
            verify(bpmnMessageReceivedEventMessageProducer, times(3 * processInstances))
                    .onEvent(any());

            verify(receiveMessageCmdExecutor, times(processInstances)).execute(any());
            verify(startMessageCmdExecutor, times(2 * processInstances)).execute(any());
        });
    }

    @Test
    void shouldCompleteComplexBpmnMessageEventMultipleProcessesWithBoundarySubprocessMessageCatchEvent() {
        // given
        int processInstances = 10;

        //when
        IntStream.rangeClosed(1, processInstances)
                .mapToObj(i -> ProcessPayloadBuilder.start()
                        .withProcessDefinitionKey(BOUNDARY_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS1)
                        .withBusinessKey(BUSINESS_KEY + i)
                        .build())
                .forEach(commandEndpoint::execute);

        // then
        await().untilAsserted(() -> {
            verify(bpmnMessageSentEventMessageProducer, times(3 * processInstances)).onEvent(any());
            verify(bpmnMessageWaitingEventMessageProducer, times(processInstances)).onEvent(any());
            verify(bpmnMessageReceivedEventMessageProducer, times(3 * processInstances))
                    .onEvent(any());

            verify(receiveMessageCmdExecutor, times(processInstances)).execute(any());
            verify(startMessageCmdExecutor, times(2 * processInstances)).execute(any());
        });

    }

    @Test
    void shouldCompleteComplexBpmnMessageEventMultipleProcessesWithStartEventSubprocessEvent() {
        // given
        int processInstances = 10;

        //when
        IntStream.rangeClosed(1, processInstances)
                .mapToObj(i -> ProcessPayloadBuilder.start()
                        .withProcessDefinitionKey(EVENT_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS1)
                        .withBusinessKey(BUSINESS_KEY + i)
                        .build())
                .forEach(commandEndpoint::execute);

        // then
        await().untilAsserted(() -> {
            verify(bpmnMessageSentEventMessageProducer, times(4 * processInstances)).onEvent(any());
            verify(bpmnMessageWaitingEventMessageProducer, times(2 * processInstances))
                    .onEvent(any());
            verify(bpmnMessageReceivedEventMessageProducer, times(4 * processInstances))
                    .onEvent(any());

            verify(receiveMessageCmdExecutor, times(2 * processInstances)).execute(any());
            verify(startMessageCmdExecutor, times(2 * processInstances)).execute(any());
        });

    }

    @Test
    void shouldCompleteComplexBpmnMessageEventMultipleProcessesWithStartEventSubprocessNonInterruptingEvent() {
        // given
        int processInstances = 10;

        //when
        IntStream.rangeClosed(1, processInstances)
                .mapToObj(i -> ProcessPayloadBuilder.start()
                        .withProcessDefinitionKey(
                                EVENT_SUBPROCESS_NON_INTERRUPTING_THROW_CATCH_MESSAGE_IT_PROCESS1)
                        .withBusinessKey(BUSINESS_KEY + i)
                        .build())
                .forEach(commandEndpoint::execute);

        // then
        await().untilAsserted(() -> {
            verify(bpmnMessageSentEventMessageProducer, times(4 * processInstances)).onEvent(any());
            verify(bpmnMessageWaitingEventMessageProducer, times(2 * processInstances))
                    .onEvent(any());
            verify(bpmnMessageReceivedEventMessageProducer, times(4 * processInstances))
                    .onEvent(any());

            verify(receiveMessageCmdExecutor, times(2 * processInstances)).execute(any());
            verify(startMessageCmdExecutor, times(2 * processInstances)).execute(any());
        });
    }

    @Test
    void shouldThrowCatchBpmnMessages() {
        // given
        int processInstances = 10;

        // when
        IntStream.rangeClosed(1, processInstances)
                .mapToObj(i -> ProcessPayloadBuilder.start()
                        .withProcessDefinitionKey(INTERMEDIATE_THROW_MESSAGE_PROCESS)
                        .withBusinessKey(BUSINESS_KEY + i)
                        .build())
                .forEach(commandEndpoint::execute);

        IntStream.rangeClosed(1, processInstances)
                .mapToObj(i -> ProcessPayloadBuilder.start()
                        .withProcessDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                        .withBusinessKey(BUSINESS_KEY + i)
                        .build())
                .forEach(commandEndpoint::execute);

        // then
        await().untilAsserted(() -> {
            verify(bpmnMessageSentEventMessageProducer, times(processInstances)).onEvent(any());
            verify(bpmnMessageWaitingEventMessageProducer, times(processInstances)).onEvent(any());
            verify(bpmnMessageReceivedEventMessageProducer, times(processInstances)).onEvent(any());

            verify(receiveMessageCmdExecutor, times(processInstances)).execute(any());
            verify(startMessageCmdExecutor, never()).execute(any());
        });
    }

    @Test
    void shouldCatchThrowBpmnMessages() {
        // given
        int processInstances = 10;

        // when
        IntStream.rangeClosed(1, processInstances)
                .mapToObj(i -> ProcessPayloadBuilder.start()
                        .withProcessDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                        .withBusinessKey(BUSINESS_KEY + i)
                        .build())
                .forEach(commandEndpoint::execute);

        IntStream.rangeClosed(1, processInstances)
                .mapToObj(i -> ProcessPayloadBuilder.start()
                        .withProcessDefinitionKey(INTERMEDIATE_THROW_MESSAGE_PROCESS)
                        .withBusinessKey(BUSINESS_KEY + i)
                        .build())
                .forEach(commandEndpoint::execute);

        // then
        await().untilAsserted(() -> {
            verify(bpmnMessageSentEventMessageProducer, times(processInstances)).onEvent(any());
            verify(bpmnMessageWaitingEventMessageProducer, times(processInstances)).onEvent(any());
            verify(bpmnMessageReceivedEventMessageProducer, times(processInstances)).onEvent(any());

            verify(receiveMessageCmdExecutor, times(processInstances)).execute(any());
            verify(startMessageCmdExecutor, never()).execute(any());
        });
    }

    @Test
    void shouldCancelWaitingMessageSubscription() {
        // given
        int processInstances = 10;
        List<ProcessInstance> instances = new ArrayList<>();

        // when
        IntStream.range(0, processInstances)
                .mapToObj(i -> ProcessPayloadBuilder.start()
                        .withProcessDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                        .withBusinessKey(BUSINESS_KEY + i)
                        .build())
                .<ProcessInstanceResult>map(commandEndpoint::execute)
                .map(ProcessInstanceResult::getEntity)
                .forEach(instances::add);

        // then
        assertThat(runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                .list()).hasSize(processInstances);

        verify(bpmnMessageWaitingEventMessageProducer,
                times(processInstances)).onEvent(any());

        // when
        IntStream.range(0, processInstances)
                .mapToObj(i -> instances.get(i))
                .map(it -> new DeleteProcessPayload(it.getId(), "cancelled"))
                .forEach(commandEndpoint::execute);

        // then
        assertThat(runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                .list()).isEmpty();

        verify(messageSubscriptionCancelledEventMessageProducer,
                times(processInstances)).onEvent(any());

        IntStream.range(0, processInstances)
                .mapToObj(i -> BUSINESS_KEY + i)
                .map("messages-app:BpmnMessage:"::concat)
                .forEach(
                        groupId -> assertThat(messageGroupStore.getMessagesForGroup(groupId)).isEmpty());

    }
}
