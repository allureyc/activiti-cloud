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
package org.activiti.cloud.services.rest.api;

import org.activiti.api.process.model.payloads.CreateProcessInstancePayload;
import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.api.process.model.payloads.StartMessagePayload;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.process.model.payloads.UpdateProcessPayload;
import org.activiti.cloud.api.process.model.CloudProcessInstance;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping(
        value = "/v1/process-instances",
        produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
public interface ProcessInstanceController {

    @GetMapping()
    PagedModel<EntityModel<CloudProcessInstance>> getProcessInstances(Pageable pageable);

    @PostMapping(headers = "Content-type=application/json")
    EntityModel<CloudProcessInstance> startProcess(@RequestBody StartProcessPayload cmd);

    @PostMapping(value = "/{processInstanceId}/start", headers = "Content-type=application/json")
    EntityModel<CloudProcessInstance> startCreatedProcess(
            @PathVariable(value = "processInstanceId") String processInstanceId,
            @RequestBody(required = false) StartProcessPayload payload);

    @PostMapping(value = "/create", headers = "Content-type=application/json")
    EntityModel<CloudProcessInstance> createProcessInstance(
            @RequestBody CreateProcessInstancePayload cmd);

    @GetMapping(value = "/{processInstanceId}")
    EntityModel<CloudProcessInstance> getProcessInstanceById(
            @PathVariable(value = "processInstanceId") String processInstanceId);

    @GetMapping(value = "/{processInstanceId}/model", produces = "image/svg+xml")
    @ResponseBody
    String getProcessDiagram(@PathVariable(value = "processInstanceId") String processInstanceId);

    @PostMapping(value = "/signal", headers = "Content-type=application/json")
    ResponseEntity<Void> sendSignal(@RequestBody SignalPayload signalPayload);

    @PostMapping(value = "/message", headers = "Content-type=application/json")
    EntityModel<CloudProcessInstance> sendStartMessage(
            @RequestBody StartMessagePayload startMessagePayload);

    @PutMapping(value = "/message", headers = "Content-type=application/json")
    ResponseEntity<Void> receive(@RequestBody ReceiveMessagePayload receiveMessagePayload);

    @PostMapping(value = "/{processInstanceId}/suspend", headers = "Content-type=application/json")
    EntityModel<CloudProcessInstance> suspend(
            @PathVariable(value = "processInstanceId") String processInstanceId);

    @PostMapping(value = "/{processInstanceId}/resume", headers = "Content-type=application/json")
    EntityModel<CloudProcessInstance> resume(
            @PathVariable(value = "processInstanceId") String processInstanceId);

    @DeleteMapping(value = "/{processInstanceId}")
    EntityModel<CloudProcessInstance> deleteProcessInstance(
            @PathVariable(value = "processInstanceId") String processInstanceId);

    @PutMapping(value = "/{processInstanceId}", headers = "Content-type=application/json")
    EntityModel<CloudProcessInstance> updateProcess(
            @PathVariable(value = "processInstanceId") String processInstanceId,
            @RequestBody UpdateProcessPayload payload);

    @GetMapping(value = "/{processInstanceId}/subprocesses")
    PagedModel<EntityModel<CloudProcessInstance>> subprocesses(
            @PathVariable(value = "processInstanceId") String processInstanceId, Pageable pageable);
}
