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

package org.activiti.cloud.services.messages.core.router;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.BeanFactoryMessageChannelDestinationResolver;
import org.springframework.messaging.core.DestinationResolutionException;

public class CommandConsumerMessageChannelResolver
    extends BeanFactoryMessageChannelDestinationResolver {

    private final BinderAwareChannelResolver binderAwareChannelResolver;
    private final BindingService bindingService;
    private final Function<String, String> destinationMapper;

    public CommandConsumerMessageChannelResolver(
        Function<String, String> destinationMapper,
        BinderAwareChannelResolver binderAwareChannelResolver,
        BindingService bindingService
    ) {
        this.destinationMapper = destinationMapper;
        this.binderAwareChannelResolver = binderAwareChannelResolver;
        this.bindingService = bindingService;
    }

    @Override
    public MessageChannel resolveDestination(String name)
        throws DestinationResolutionException {
        String destination = destinationMapper.apply(name);

        Optional<String> channelName = getChannelName(destination);

        return channelName
            .map(super::resolveDestination)
            .orElseGet(() ->
                binderAwareChannelResolver.resolveDestination(destination)
            );
    }

    protected Optional<String> getChannelName(String destination) {
        BindingServiceProperties bindingProperties = bindingService.getBindingServiceProperties();

        return bindingProperties
            .getBindings()
            .entrySet()
            .stream()
            .filter(entry ->
                entry.getValue().getDestination().equals(destination)
            )
            .map(Map.Entry::getKey)
            .findFirst();
    }
}
