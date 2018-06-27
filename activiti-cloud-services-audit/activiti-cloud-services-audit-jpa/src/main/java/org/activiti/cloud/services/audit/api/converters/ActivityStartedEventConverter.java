package org.activiti.cloud.services.audit.api.converters;

import org.activiti.cloud.services.audit.events.ActivityStartedAuditEventEntity;
import org.activiti.cloud.services.audit.events.AuditEventEntity;
import org.activiti.runtime.api.event.BPMNActivityEvent;
import org.activiti.runtime.api.event.CloudBPMNActivityStartedEvent;
import org.activiti.runtime.api.event.CloudRuntimeEvent;
import org.activiti.runtime.api.event.impl.CloudBPMNActivityStartedEventImpl;
import org.springframework.stereotype.Component;

@Component
public class ActivityStartedEventConverter implements EventToEntityConverter<AuditEventEntity> {

    @Override
    public String getSupportedEvent() {
        return BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED.name();
    }

    @Override
    public AuditEventEntity convertToEntity(CloudRuntimeEvent cloudRuntimeEvent) {
        CloudBPMNActivityStartedEvent cloudActivityStartedEvent = (CloudBPMNActivityStartedEvent) cloudRuntimeEvent;
        ActivityStartedAuditEventEntity activityStartedAuditEventEntity = new ActivityStartedAuditEventEntity(cloudActivityStartedEvent.getId(),
                                                                                                              cloudActivityStartedEvent.getTimestamp(),
                                                                                                              cloudActivityStartedEvent.getAppName(),
                                                                                                              cloudActivityStartedEvent.getAppVersion(),
                                                                                                              cloudActivityStartedEvent.getServiceFullName(),
                                                                                                              cloudActivityStartedEvent.getServiceName(),
                                                                                                              cloudActivityStartedEvent.getServiceType(),
                                                                                                              cloudActivityStartedEvent.getServiceVersion(),
                                                                                                              cloudActivityStartedEvent.getEntity());
        activityStartedAuditEventEntity.setProcessDefinitionId(cloudActivityStartedEvent.getProcessDefinitionId());
        activityStartedAuditEventEntity.setProcessInstanceId(cloudActivityStartedEvent.getProcessInstanceId());
        return activityStartedAuditEventEntity;
    }

    @Override
    public CloudRuntimeEvent convertToAPI(AuditEventEntity auditEventEntity) {
        ActivityStartedAuditEventEntity activityStartedAuditEventEntity = (ActivityStartedAuditEventEntity) auditEventEntity;

        CloudBPMNActivityStartedEventImpl bpmnActivityStartedEvent = new CloudBPMNActivityStartedEventImpl(activityStartedAuditEventEntity.getEventId(),
                                                                                                           activityStartedAuditEventEntity.getTimestamp(),
                                                                                                           activityStartedAuditEventEntity.getBpmnActivity());
        bpmnActivityStartedEvent.setAppName(activityStartedAuditEventEntity.getAppName());
        bpmnActivityStartedEvent.setAppVersion(activityStartedAuditEventEntity.getAppVersion());
        bpmnActivityStartedEvent.setServiceFullName(activityStartedAuditEventEntity.getServiceFullName());
        bpmnActivityStartedEvent.setServiceName(activityStartedAuditEventEntity.getServiceName());
        bpmnActivityStartedEvent.setServiceType(activityStartedAuditEventEntity.getServiceType());
        bpmnActivityStartedEvent.setServiceVersion(activityStartedAuditEventEntity.getServiceVersion());
        bpmnActivityStartedEvent.setProcessDefinitionId(activityStartedAuditEventEntity.getProcessDefinitionId());
        bpmnActivityStartedEvent.setProcessInstanceId(activityStartedAuditEventEntity.getProcessInstanceId());
        return bpmnActivityStartedEvent;
    }
}
