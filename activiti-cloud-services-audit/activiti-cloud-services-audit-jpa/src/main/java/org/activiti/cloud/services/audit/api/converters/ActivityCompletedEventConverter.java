package org.activiti.cloud.services.audit.api.converters;

import org.activiti.cloud.services.audit.events.ActivityCompletedAuditEventEntity;
import org.activiti.cloud.services.audit.events.AuditEventEntity;
import org.activiti.runtime.api.event.BPMNActivityEvent;
import org.activiti.runtime.api.event.CloudBPMNActivityCompletedEvent;
import org.activiti.runtime.api.event.CloudRuntimeEvent;
import org.activiti.runtime.api.event.impl.CloudBPMNActivityCompletedEventImpl;
import org.springframework.stereotype.Component;

@Component
public class ActivityCompletedEventConverter implements EventToEntityConverter<AuditEventEntity> {

    @Override
    public String getSupportedEvent() {
        return BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED.name();
    }

    @Override
    public AuditEventEntity convertToEntity(CloudRuntimeEvent cloudRuntimeEvent) {
        CloudBPMNActivityCompletedEvent cloudBPMNActivityCompletedEvent = (CloudBPMNActivityCompletedEvent) cloudRuntimeEvent;
        ActivityCompletedAuditEventEntity activityCompletedAuditEventEntity = new ActivityCompletedAuditEventEntity(cloudBPMNActivityCompletedEvent.getId(),
                                                                                                                    cloudBPMNActivityCompletedEvent.getTimestamp(),
                                                                                                                    cloudBPMNActivityCompletedEvent.getAppName(),
                                                                                                                    cloudBPMNActivityCompletedEvent.getAppVersion(),
                                                                                                                    cloudBPMNActivityCompletedEvent.getServiceFullName(),
                                                                                                                    cloudBPMNActivityCompletedEvent.getServiceName(),
                                                                                                                    cloudBPMNActivityCompletedEvent.getServiceType(),
                                                                                                                    cloudBPMNActivityCompletedEvent.getServiceVersion(),
                                                                                                                    cloudBPMNActivityCompletedEvent.getEntity());
        activityCompletedAuditEventEntity.setProcessDefinitionId(cloudBPMNActivityCompletedEvent.getProcessDefinitionId());
        activityCompletedAuditEventEntity.setProcessInstanceId(cloudBPMNActivityCompletedEvent.getProcessInstanceId());
        return activityCompletedAuditEventEntity;
    }

    @Override
    public CloudRuntimeEvent convertToAPI(AuditEventEntity auditEventEntity) {
        ActivityCompletedAuditEventEntity activityCompletedAuditEventEntity = (ActivityCompletedAuditEventEntity) auditEventEntity;

        CloudBPMNActivityCompletedEventImpl bpmnActivityCompletedEvent = new CloudBPMNActivityCompletedEventImpl(activityCompletedAuditEventEntity.getEventId(),
                                                                                                                 activityCompletedAuditEventEntity.getTimestamp(),
                                                                                                                 activityCompletedAuditEventEntity.getBpmnActivity());
        bpmnActivityCompletedEvent.setAppName(activityCompletedAuditEventEntity.getAppName());
        bpmnActivityCompletedEvent.setAppVersion(activityCompletedAuditEventEntity.getAppVersion());
        bpmnActivityCompletedEvent.setServiceFullName(activityCompletedAuditEventEntity.getServiceFullName());
        bpmnActivityCompletedEvent.setServiceName(activityCompletedAuditEventEntity.getServiceName());
        bpmnActivityCompletedEvent.setServiceType(activityCompletedAuditEventEntity.getServiceType());
        bpmnActivityCompletedEvent.setServiceVersion(activityCompletedAuditEventEntity.getServiceVersion());
        bpmnActivityCompletedEvent.setProcessDefinitionId(activityCompletedAuditEventEntity.getProcessDefinitionId());
        bpmnActivityCompletedEvent.setProcessInstanceId(activityCompletedAuditEventEntity.getProcessInstanceId());
        return bpmnActivityCompletedEvent;
    }
}
