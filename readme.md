This is an example project that demonstrates the different capabilities in the SDL OData framework.

## Starting the Service
There are several ways to start the OData Service, in this demo we have made this even easier by adding a demo class.

### From Maven
It is quite easy to run the example service from the command line, the example utilises Spring-Boot which allows
running the service using a single packed jar. 

In order to run the example, in the cloned git repository execute the following maven command:  
```mvn -f example-service/pom.xml spring-boot:run```

### From Your favorite IDE
In order to execute the Example service in your IDE, please execute the following the Java Main method in class:  
```com.sdl.odata.example.service.ServiceContainer```


## Creating Example Content
The example service by default has only one Person in the dataset "Darkwing Duck", in order to add more example content
in the service the following steps can be used. This also demonstrates how the OData service allows data creation.

In order to create some content the below sample content can be used to post data to the OData Service.
Example command to create Mickey as a Person in the service, using a sample json file:

```
curl -i -X POST -d @src/samples/mickey.json http://localhost:8080/example.svc/Persons --header "Content-Type:application/json"
```

The content structure that is posted looks as follows:  
```json
{ 
    "@odata.context" : "http://localhost:8080/example.svc/$metadata#/Persons/$entity",  
    "@odata.id" : "/Persons(55)",  
    "id" : 55,  
    "firstName" : "Mickey",  
    "lastName" : "Mouse",  
    "age" : 83  
}
```

We also have two more Persons (Donald and Scrooge), which you can post as following:

```
curl -i -X POST -d @src/samples/donald.json http://localhost:8080/example.svc/Persons --header "Content-Type:application/json"          
```

```
curl -i -X POST -d @src/samples/scrooge.json http://localhost:8080/example.svc/Persons --header "Content-Type:application/json"
```
