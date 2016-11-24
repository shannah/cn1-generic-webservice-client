# Generic Webservice Client Library for Codename One

A library for consuming RESTful web services in Codename One.

## License

Copyright 2016 [Codename One](https://www.codenameone.com)

Licensed under Apache 2.0

## Synopsis

This library provides a starting point for consuming RESTful web services in Codename One.  It provides minimal client class named `RESTfulWebserviceClient` which provides the following basic methods (and a few others):

~~~~
// Delete a record with id asynchronously
public void delete(String id, SuccessCallback<Boolean> callback);

// Create a new record with the given data asynchronously
public void create(Map data, SuccessCallback<Boolean> callback);

// Edit an existing record with given data asynchronously.
public void edit(String id, Map data, SuccessCallback<Boolean> callback);

// Count the number of records provided by the web service.
public void count(SuccessCallback<Integer> callback);

// Find records provided by the web service
public void find(Query query, SuccessCallback<RowSet> callback)
~~~~

## Examples

**Adding a Record**

~~~~
RESTfulWebServiceClient userClient = new RESTfulWebServiceClient("http://localhost:8080/SakilaRESTServer/webresources/com.codename1.demos.sakilarestserver.users");
Map m = new HashMap();
m.put("username", "test1");
m.put("password", "testpass1");
m.put("email", "test1@weblite.ca");
m.put("role", "ADMIN");

userClient.create(m, res->{
    // res will be boolean to indicate success or failure
    System.out.println(res);
});
~~~~

**Updatng an existing record**

~~~~
RESTfulWebServiceClient userClient = new RESTfulWebServiceClient("http://localhost:8080/SakilaRESTServer/webresources/com.codename1.demos.sakilarestserver.users");
Map m = new HashMap();
m.put("username", "test1");
m.put("password", "testpass1"+System.currentTimeMillis());
m.put("email", "test1@weblite.ca");
m.put("role", "ADMIN");
System.out.println("Setting values of test1 to "+m);

userClient.edit("test1", m, res->{
    System.out.println(res);
});
~~~~

**Get a record by ID**

~~~~
Query q = new Query().id("10");
client.find(q, rowset->{
    for (Map m : rowset) {
        System.out.println(m);
    }
        
});
~~~~

**Get first 2 records**

~~~~
client.find(new Query().limit(2), rowset->{
    for (Map m : rowset) {
        System.out.println(m);
    }
});
~~~~

**Delete Record**

~~~~
RESTfulWebServiceClient userClient = new RESTfulWebServiceClient("http://localhost:8080/SakilaRESTServer/webresources/com.codename1.demos.sakilarestserver.users");

userClient.delete("test1", res->{
    System.out.println(res);
});
~~~~

**Count Records**

~~~~
RESTfulWebServiceClient userClient = new RESTfulWebServiceClient("http://localhost:8080/SakilaRESTServer/webresources/com.codename1.demos.sakilarestserver.users");

userClient.count(res->{
    System.out.println(res);
});
~~~~

## Credits

* Developed by [Steve Hannah](http://sjhannah.com)

