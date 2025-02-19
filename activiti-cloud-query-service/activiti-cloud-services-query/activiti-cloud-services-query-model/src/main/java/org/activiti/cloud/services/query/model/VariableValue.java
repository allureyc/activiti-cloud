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
package org.activiti.cloud.services.query.model;

public class VariableValue<T> {

	private T value;

    public VariableValue() {
    }

    public VariableValue(T value) {
        this.value = value;
    }

	public T getValue() {
        return value;
    }


    /**
     * Encountered Java type [class org.activiti.cloud.services.query.model.VariableValue] for which we could not locate a JavaTypeDescriptor
     * and which does not appear to implement equals and/or hashCode.  This can lead to significant performance problems when performing
     * equality/dirty checking involving this Java type.
     *
     * Consider registering a custom JavaTypeDescriptor or at least implementing equals/hashCode.
     *
     */
    @Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableValue<?> other = (VariableValue<?>) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VariableValue [value=" + value + "]";
	}

}
