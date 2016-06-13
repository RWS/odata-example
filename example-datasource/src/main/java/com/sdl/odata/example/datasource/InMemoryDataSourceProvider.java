/**
 * Copyright (c) 2015 SDL Group
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
package com.sdl.odata.example.datasource;

import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.edm.util.EdmUtil;
import com.sdl.odata.api.parser.TargetType;
import com.sdl.odata.api.processor.datasource.DataSource;
import com.sdl.odata.api.processor.datasource.DataSourceProvider;
import com.sdl.odata.api.processor.datasource.ODataDataSourceException;
import com.sdl.odata.api.processor.query.QueryOperation;
import com.sdl.odata.api.processor.query.QueryResult;
import com.sdl.odata.api.processor.query.strategy.QueryOperationStrategy;
import com.sdl.odata.api.service.ODataRequestContext;
import com.sdl.odata.example.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This is an example data source provide that uses in memory structures to demonstrate how to provide
 * entities, storing and querying capabilities to the OData v4 framework.
 */
@Component
public class InMemoryDataSourceProvider implements DataSourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataSourceProvider.class);

    @Autowired
    private InMemoryDataSource inMemoryDataSource;

    @Override
    public boolean isSuitableFor(ODataRequestContext oDataRequestContext, String entityType) throws ODataDataSourceException {
        return oDataRequestContext.getEntityDataModel().getType(entityType).getJavaType().equals(Person.class);
    }

    @Override
    public DataSource getDataSource(ODataRequestContext oDataRequestContext) {
        return inMemoryDataSource;
    }

    @Override
    public QueryOperationStrategy getStrategy(ODataRequestContext oDataRequestContext, QueryOperation queryOperation, TargetType targetType) throws ODataException {
        StrategyBuilder builder = new StrategyBuilder();
        List<Predicate<Person>> predicateList = builder.buildCriteria(queryOperation, oDataRequestContext);

        int limit = builder.getLimit();
        int skip = builder.getSkip();
        List<String> propertyNames = builder.getPropertyNames();

        return () -> {
            LOG.debug("Executing query against in memory data");
            Stream<Person> personStream = inMemoryDataSource.getPersonConcurrentMap().values().stream();

            List<Person> filteredPersons = personStream.filter(p -> predicateList.stream()
                    .allMatch(f -> f.test(p))).collect(Collectors.toList());

            long count = 0;
            if (builder.isCount() || builder.includeCount()) {
                count = filteredPersons.size();
                LOG.debug("Counted {} persons matching query", count);

                if (builder.isCount()) {
                    return QueryResult.from(count);
                }
            }

            if (skip != 0 || limit != Integer.MAX_VALUE) {
                filteredPersons = filteredPersons.stream().skip(skip).limit(limit).collect(Collectors.toList());
            }

            LOG.debug("Found {} persons matching query", filteredPersons.size());

            if (propertyNames != null && !propertyNames.isEmpty()) {
                try {
                    LOG.debug("Selecting {} properties of person", propertyNames);
                    return QueryResult.from(EdmUtil.getEdmPropertyValue(filteredPersons.get(0), propertyNames.get(0)));
                } catch (IllegalAccessException e) {
                    LOG.error(e.getMessage(), e);
                    return QueryResult.from(Collections.emptyList());
                }
            }

            QueryResult result = QueryResult.from(filteredPersons);
            if (builder.includeCount()) {
                result = result.withCount(count);
            }
            return result;
        };
    }
}
