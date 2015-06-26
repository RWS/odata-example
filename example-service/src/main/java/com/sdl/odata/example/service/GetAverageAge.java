package com.sdl.odata.example.service;

import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.edm.annotations.EdmFunction;
import com.sdl.odata.api.edm.annotations.EdmReturnType;
import com.sdl.odata.api.edm.model.Operation;
import com.sdl.odata.api.processor.datasource.factory.DataSourceFactory;
import com.sdl.odata.api.service.ODataRequestContext;
import com.sdl.odata.example.Person;
import com.sdl.odata.example.datasource.InMemoryDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

/**
 * @author rdevries
 */
@EdmFunction(name = "GetAverageAge", namespace = "SDL.OData.Example", isBound = true)
@EdmReturnType(type = "Edm.Double")
public class GetAverageAge implements Operation<Double> {
    private static final Logger LOG = LoggerFactory.getLogger(GetAverageAge.class);

    @Override
    public Double doOperation(ODataRequestContext oDataRequestContext, DataSourceFactory dataSourceFactory) throws ODataException {
        LOG.debug("Executing function 'GetAverageAge'");

        InMemoryDataSource dataSource = (InMemoryDataSource) dataSourceFactory.getDataSource(oDataRequestContext, "SDL.OData.Example.Person");
        ConcurrentMap<String, Person> personConcurrentMap = dataSource.getPersonConcurrentMap();
        Double result = personConcurrentMap.values().stream().mapToInt(Person::getAge).average().getAsDouble();
        LOG.debug("Average age: {}", result);

        return result;
    }
}
