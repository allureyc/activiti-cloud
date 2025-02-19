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

import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryEventHandlerContext {

    private static Logger LOGGER = LoggerFactory.getLogger(QueryEventHandlerContext.class);

    private final Map<String, QueryEventHandler> handlers;

    public QueryEventHandlerContext(Set<QueryEventHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toMap(QueryEventHandler::getHandledEvent,
                                                                   Function.identity()));
    }

    public void handle(CloudRuntimeEvent<?, ?>... events) {
        if (events != null) {
            Stream.of(events)
                  .forEach(event -> {
                      QueryEventHandler handler = handlers.get(event.getEventType()
                                                                    .name());
                      if (handler != null) {
                          LOGGER.debug("Handling event: " + handler.getHandledEvent());
                          handler.handle(event);
                      } else {
                          LOGGER.debug("No handler found for event: " + event.getEventType()
                                                                             .name() + ". Ignoring event");
                      }
                  });
        }
    }

    protected Map<String, QueryEventHandler> getHandlers() {
        return handlers;
    }
}
