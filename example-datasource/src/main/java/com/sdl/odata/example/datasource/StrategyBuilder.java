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
import com.sdl.odata.api.ODataSystemException;
import com.sdl.odata.api.processor.query.ComparisonCriteria;
import com.sdl.odata.api.processor.query.ComparisonOperator;
import com.sdl.odata.api.processor.query.Criteria;
import com.sdl.odata.api.processor.query.CriteriaFilterOperation;
import com.sdl.odata.api.processor.query.ExpandOperation;
import com.sdl.odata.api.processor.query.GtOperator$;
import com.sdl.odata.api.processor.query.LimitOperation;
import com.sdl.odata.api.processor.query.LiteralCriteriaValue;
import com.sdl.odata.api.processor.query.OrderByOperation;
import com.sdl.odata.api.processor.query.PropertyCriteriaValue;
import com.sdl.odata.api.processor.query.QueryOperation;
import com.sdl.odata.api.processor.query.SelectByKeyOperation;
import com.sdl.odata.api.processor.query.SelectOperation;
import com.sdl.odata.api.processor.query.SelectPropertiesOperation;
import com.sdl.odata.api.processor.query.SkipOperation;
import com.sdl.odata.example.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 *
 */
public class StrategyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(StrategyBuilder.class);

    private List<Predicate<Person>> predicates = new ArrayList<>();
    private int limit = Integer.MAX_VALUE;

    public List<Predicate<Person>> buildCriteria(QueryOperation queryOperation) throws ODataException {
        buildFromOperation(queryOperation);

        return predicates;
    }

    public int getLimit() {
        return limit;
    }

    private void buildFromOperation(QueryOperation operation) throws ODataException {
        if (operation instanceof SelectOperation) {
            buildFromSelect((SelectOperation) operation);
        } else if (operation instanceof SelectByKeyOperation) {
            buildFromSelectByKey((SelectByKeyOperation) operation);
        } else if (operation instanceof CriteriaFilterOperation) {
            buildFromFilter((CriteriaFilterOperation)operation);
        } else if (operation instanceof LimitOperation) {
            buildFromLimit((LimitOperation) operation);
        } else if (operation instanceof SkipOperation) {
            //not supported for now
        } else if (operation instanceof ExpandOperation) {
            //not supported for now
        } else if (operation instanceof OrderByOperation) {
            //not supported for now
        } else if (operation instanceof SelectPropertiesOperation) {
            //not supported for now
        } else {
            throw new ODataSystemException("Unsupported query operation: " + operation);
        }
    }

    private void buildFromLimit(LimitOperation operation) throws ODataException {
        this.limit = operation.getCount();
        LOG.debug("Limit has been set to: {}", limit);
        buildFromOperation(operation.getSource());
    }

    private void buildFromSelect(SelectOperation selectOperation) {
        LOG.debug("Selecting all persons, no predicates needed");
    }

    private void buildFromSelectByKey(SelectByKeyOperation selectByKeyOperation) {
        Map<String, Object> keys = selectByKeyOperation.getKeyAsJava();
        String personId = (String)keys.get("id");
        LOG.debug("Selecting by key: {}", personId);

        predicates.add(person -> person.getPersonId().equalsIgnoreCase(personId));
    }

    private void buildFromFilter(CriteriaFilterOperation criteriaFilterOperation) {
        Criteria criteria = criteriaFilterOperation.getCriteria();
        if(criteria instanceof ComparisonCriteria) {
            ComparisonCriteria comparisonCriteria = (ComparisonCriteria) criteria;
            ComparisonOperator operator = comparisonCriteria.getOperator();
            if(operator instanceof GtOperator$) {
                LOG.info("Greater then operator");
            }

            //For now we only support here property key/value comparisons, just to keep the example simple
            if(comparisonCriteria.getLeft() instanceof PropertyCriteriaValue
                    && comparisonCriteria.getRight() instanceof LiteralCriteriaValue) {

                PropertyCriteriaValue propertyCriteriaValue = (PropertyCriteriaValue) comparisonCriteria.getLeft();
                LiteralCriteriaValue literalCriteriaValue = (LiteralCriteriaValue) comparisonCriteria.getRight();

                Predicate<Person> p = person -> {
                    Object fieldValue = getPersonField(person, propertyCriteriaValue.getPropertyName());
                    Object queryValue = literalCriteriaValue.getValue();

                    LOG.debug("Comparing equality on value: {} to queried value: {}", fieldValue, queryValue);

                    return fieldValue != null && fieldValue.equals(literalCriteriaValue.getValue());
                };

                predicates.add(p);
            }
        }
    }

    private Object getPersonField(Person person, String propertyName) {
        try {
            Field field = person.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            return field.get(person);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOG.debug("Could not load property: " + propertyName);
            return null;
        }
    }



}
