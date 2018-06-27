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

package org.activiti.cloud.services.audit.api.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import org.activiti.cloud.alfresco.data.domain.AlfrescoPagedResourcesAssembler;
import org.activiti.cloud.services.audit.api.assembler.EventResourceAssembler;
import org.activiti.cloud.services.audit.api.converters.APIEventToEntityConverters;
import org.activiti.cloud.services.audit.api.converters.EventToEntityConverter;
import org.activiti.cloud.services.audit.api.resources.EventResource;
import org.activiti.cloud.services.audit.api.resources.EventsRelProvider;
import org.activiti.cloud.services.audit.events.AuditEventEntity;
import org.activiti.cloud.services.audit.repository.EventSpecificationsBuilder;
import org.activiti.cloud.services.audit.repository.EventsRepository;
import org.activiti.cloud.services.audit.repository.SearchOperation;
import org.activiti.cloud.services.audit.security.SecurityPoliciesApplicationService;
import org.activiti.cloud.services.security.SecurityPolicy;
import org.activiti.runtime.api.event.CloudRuntimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/" + EventsRelProvider.COLLECTION_RESOURCE_REL, produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
public class AuditEventsControllerImpl implements AuditEventsController {

    private static Logger LOGGER = LoggerFactory.getLogger(AuditEventsAdminControllerImpl.class);

    private final EventsRepository eventsRepository;

    private final EventResourceAssembler eventResourceAssembler;

    private final AlfrescoPagedResourcesAssembler<CloudRuntimeEvent> pagedResourcesAssembler;

    private SecurityPoliciesApplicationService securityPoliciesApplicationService;

    private final APIEventToEntityConverters eventConverters;

    @Autowired
    public AuditEventsControllerImpl(EventsRepository eventsRepository,
                                     EventResourceAssembler eventResourceAssembler,
                                     APIEventToEntityConverters eventConverters,
                                     SecurityPoliciesApplicationService securityPoliciesApplicationService,
                                     AlfrescoPagedResourcesAssembler<CloudRuntimeEvent> pagedResourcesAssembler) {
        this.eventsRepository = eventsRepository;
        this.eventResourceAssembler = eventResourceAssembler;
        this.eventConverters = eventConverters;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.securityPoliciesApplicationService = securityPoliciesApplicationService;
    }

    @RequestMapping(value = "/{eventId}", method = RequestMethod.GET)
    public EventResource findById(@PathVariable String eventId) {
        Optional<AuditEventEntity> findResult = eventsRepository.findByEventId(eventId);
        if (!findResult.isPresent()) {
            throw new RuntimeException("Unable to find event for the given id:'" + eventId + "'");
        }
        AuditEventEntity auditEventEntity = findResult.get();
        if (!securityPoliciesApplicationService.canRead(auditEventEntity.getProcessDefinitionId(),
                                                        auditEventEntity.getServiceFullName())) {
            throw new RuntimeException("Operation not permitted for " + auditEventEntity.getProcessDefinitionId());
        }

        CloudRuntimeEvent cloudRuntimeEvent = eventConverters.getConverterByEventTypeName(auditEventEntity.getEventType()).convertToAPI(auditEventEntity);
        return eventResourceAssembler.toResource(cloudRuntimeEvent);
    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<EventResource> findAll(@RequestParam(value = "search", required = false) String search,
                                                 Pageable pageable) {

        Specification<AuditEventEntity> spec = createSearchSpec(search);

        spec = securityPoliciesApplicationService.createSpecWithSecurity(spec,
                                                                         SecurityPolicy.READ);

        Page<AuditEventEntity> allAuditInPage = eventsRepository.findAll(spec,
                                                                         pageable);
        List<CloudRuntimeEvent> events = new ArrayList<>();

        for (AuditEventEntity aee : allAuditInPage.getContent()) {
            EventToEntityConverter converterByEventTypeName = eventConverters.getConverterByEventTypeName(aee.getEventType());
            if (converterByEventTypeName != null) {
                events.add(converterByEventTypeName.convertToAPI(aee));
            } else {
                LOGGER.warn("Converter not found for Event Type: " + aee.getEventType());
            }
        }

        return pagedResourcesAssembler.toResource(pageable,
                                                  new PageImpl<CloudRuntimeEvent>(events,
                                                                                  pageable,
                                                                                  allAuditInPage.getTotalElements()),
                                                  eventResourceAssembler);
    }

    private Specification<AuditEventEntity> createSearchSpec(String search) {
        EventSpecificationsBuilder builder = new EventSpecificationsBuilder();

        String operationSetExper = Joiner.on("|")
                .join(SearchOperation.SIMPLE_OPERATION_SET);
        Pattern pattern = Pattern.compile("(\\w+?)(" + operationSetExper + ")(\\p{Punct}?)(\\w+?)(\\p{Punct}?),");
        Matcher matcher = pattern.matcher(search + ",");
        while (matcher.find()) {
            builder.with(matcher.group(1),
                         matcher.group(2),
                         matcher.group(4),
                         matcher.group(3),
                         matcher.group(5));
        }

        return builder.build();
    }
}