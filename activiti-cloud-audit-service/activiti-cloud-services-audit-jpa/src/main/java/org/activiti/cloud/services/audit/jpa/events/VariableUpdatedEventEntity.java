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
package org.activiti.cloud.services.audit.jpa.events;

import org.activiti.cloud.api.model.shared.events.CloudVariableUpdatedEvent;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = VariableUpdatedEventEntity.VARIABLE_UPDATED_EVENT)
@DiscriminatorValue(value = VariableUpdatedEventEntity.VARIABLE_UPDATED_EVENT)
public class VariableUpdatedEventEntity extends VariableAuditEventEntity {

    protected static final String VARIABLE_UPDATED_EVENT = "VariableUpdatedEvent";

    @Convert(converter = VariableValueJpaConverter.class)
    @Column(name = "variable_previous_value", columnDefinition = "text")
    private VariableValue<?> previousValue;

    public VariableUpdatedEventEntity() {}

    public VariableUpdatedEventEntity(CloudVariableUpdatedEvent cloudEvent) {
        super(cloudEvent);
        this.previousValue = new VariableValue<>(cloudEvent.getPreviousValue());
    }

    public VariableValue<?> getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(VariableValue<?> previousValue) {
        this.previousValue = previousValue;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VariableUpdatedEventEntity [toString()=")
                .append(super.toString())
                .append("]");
        return builder.toString();
    }
}
