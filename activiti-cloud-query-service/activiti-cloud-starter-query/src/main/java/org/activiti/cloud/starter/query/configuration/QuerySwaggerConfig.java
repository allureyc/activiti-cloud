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
package org.activiti.cloud.starter.query.configuration;

import com.fasterxml.classmate.TypeResolver;
import java.util.List;
import org.activiti.cloud.common.swagger.BaseAPIInfoBuilder;
import org.activiti.cloud.common.swagger.DocketCustomizer;
import org.activiti.cloud.common.swagger.SwaggerDocketBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class QuerySwaggerConfig {

    @Bean(name = "queryApiDocket")
    @ConditionalOnMissingBean(name = "queryApiDocket")
    public Docket queryApiDocket(SwaggerDocketBuilder docketBuilder,
        @Value("${activiti.cloud.swagger.query-base-path:}") String querySwaggerBasePath) {
        return docketBuilder.buildApiDocket("Query Service ReST API", "Query",
            querySwaggerBasePath, "org.activiti.cloud.services.query.rest");
    }

    @Bean
    @ConditionalOnMissingBean
    public VariableSearchDocketCustomizer variableSearchDocketCustomizer(TypeResolver typeResolver) {
        return new VariableSearchDocketCustomizer(typeResolver);
    }
}
