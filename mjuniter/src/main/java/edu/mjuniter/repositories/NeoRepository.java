package edu.mjuniter.repositories;

import java.util.HashMap;
import java.util.Map;

import java.io.InputStream;
import java.util.Properties;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.TransactionContext;
import org.neo4j.driver.Session;

public class NeoRepository implements AutoCloseable{

    private Session session;
    private static NeoRepository instance = null;

    public static NeoRepository inst() {
        if (instance == null)
            instance = new NeoRepository();
        return instance;
    }

    public NeoRepository(){
        //load properties
        String dbUri;
        String dbUser;
        String dbPassword;
        Properties prop = new Properties();
        try(InputStream fis = NeoRepository.class.getClassLoader().getResourceAsStream("neo4j.properties");) {
            prop.load(fis);
            dbUri = prop.getProperty("uri");
            dbUser = prop.getProperty("user");
            dbPassword = prop.getProperty("pass");
        }
        catch(Exception e) {
            System.out.println("Error loading neo4j.properties");
            return;
        }

        //start database session
        try {
            var driver = GraphDatabase.driver(dbUri, AuthTokens.basic(dbUser, dbPassword));
            session = driver.session(SessionConfig.builder().withDatabase("neo4j").build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void close(){
        session.close();
    }


    //CREATEs a microservice node, the primary building block of the microservice system
    public void addMicroservice(String serviceName){
        boolean success = session.executeWrite(tx -> addMicroserviceTx(tx, serviceName));
        if (!success) System.out.println("Failed to add service " + serviceName);
    }

    static boolean addMicroserviceTx(TransactionContext tx, String serviceName) {
        // Create new Service node with given name, if not exists already
        var result = tx.run("CREATE (ms:Microservice {name:$serviceName,service:$serviceName})", Map.of("serviceName", serviceName));
        return result!=null;
    }


    //CREATes a controller, must belong to and come after a microservice node
    public void addController(String serviceName, String controllerName){
        boolean success = session.executeWrite(tx -> addControllerTx(tx, serviceName, controllerName));
        if (!success) System.out.println("Failed to add dependency");
    }

    static boolean addControllerTx(TransactionContext tx, String serviceName, String controllerName) {
        // Create new Person node with given name, if not exists already
        Map<String, Object> params = new HashMap<>();
        params.put("serviceName", serviceName);
        params.put("controllerName", controllerName);
        var result = tx.run("""
            MATCH (ms:Microservice {name: $serviceName})
            CREATE (ms)-[:CONTAINS]->(con:Controller {name:$controllerName, service:$serviceName})
                    """, params);
        return result!=null;
    }


    /**MERGEs an endpoint, called from the controller's SERVER_FOR relationship 
        - this will contain the microservice label, and controller name
    */
    public void addEndpointFromController(String uri, String serviceName, String controllerName){
        String standardUri = uri.replaceAll("\\{.*?\\}", "{param}");
        boolean success = session.executeWrite(tx -> addEndpointFromControllerTx(tx, standardUri, serviceName, controllerName));
        if (!success) System.out.println("Failed to add dependency");
    }

    static boolean addEndpointFromControllerTx(TransactionContext tx, String uri, String serviceName, String controllerName) {
        // Create new Person node with given name, if not exists already
        Map<String, Object> params = new HashMap<>();
        params.put("uri", uri);
        params.put("serviceName", serviceName);
        params.put("controllerName", controllerName);
        var result = tx.run("""
            MERGE (ep:EndPoint {uriName:$uri})
            SET ep.service = $serviceName
            WITH ep
            MATCH (con:Controller {name:$controllerName, service:$serviceName})
            MERGE (con)-[:SERVER_FOR]->(ep)
            return ep
                    """, params);
        return result!=null;
    }


    /**MERGEs an endpoint, called from the client's CLIENT_FOR relationship 
        - this will have the client name
    */
    public void addEndpointFromClient(String uri, String clientName, String clientService){
        String standardUri = uri.replaceAll("\\{.*?\\}", "{param}");
        boolean success = session.executeWrite(tx -> addEndpointFromClientTx(tx, standardUri, clientName, clientService));
        if (!success) System.out.println("Failed to add dependency");
    }

    static boolean addEndpointFromClientTx(TransactionContext tx, String uri, String clientName, String clientService) {
        // Create new Person node with given name, if not exists already
        Map<String, Object> params = new HashMap<>();
        params.put("uri", uri);
        params.put("clientName", clientName);
        params.put("clientService", clientService);
        var result = tx.run("""
            MERGE (ep:EndPoint {uriName: $uri})
            WITH ep
            MATCH (client:Client {name:$clientName, service:$clientService})
            MERGE (client)-[:CLIENT_FOR]->(ep)
                    """, params);
        return result!=null;
    }


    //CREATEs a client
    public void addClient(String serviceName, String clientName){
        boolean success = session.executeWrite(tx -> addClientTx(tx, serviceName, clientName));
        if (!success) System.out.println("Failed to add dependency");
    }

    static boolean addClientTx(TransactionContext tx, String serviceName, String clientName) {
        // Create new Person node with given name, if not exists already
        Map<String, Object> params = new HashMap<>();
        params.put("serviceName", serviceName);
        params.put("clientName", clientName);
        var result = tx.run("""
            MATCH (ms:Microservice {name:$serviceName})
            CREATE (ms)-[:CONTAINS]->(client:Client {name:$clientName, service:$serviceName})
                    """, params);
        return result!=null;
    }


    //erases all nodes and relationships from the database
    public void clearDB(){
        boolean success = session.executeWrite(tx -> clearTx(tx));
        if(!success) System.out.println("Failed to clear neo4j database");
    }
    
    static boolean clearTx(TransactionContext tx){
        var result = tx.run("MATCH (n) DETACH DELETE n");
        //should probably get result here eventually
        return result!=null;
    }















    //Nothing after this is necessary to keep
    public void test(){
        NeoRepository neo = NeoRepository.inst();

        neo.addMicroservice("department-service");
        neo.addClient("department-service", "EmployeeClient");
        neo.addEndpointFromClient("/employee/department/{departmentId}", "EmployeeClient", "department-service");
        neo.addController("department-service", "DepartmentController");
        neo.addEndpointFromController("/department", "department-service", "DepartmentController");
        neo.addEndpointFromController("/department/{id}", "department-service", "DepartmentController");
        neo.addEndpointFromController("/department/with-employees", "department-service", "DepartmentController");

        neo.addMicroservice("employee-service");
        neo.addController("employee-service", "EmployeeController");
        neo.addEndpointFromController("/employee", "employee-service", "EmployeeController");
        neo.addEndpointFromController("/employee/id", "employee-service", "EmployeeController");
        neo.addEndpointFromController("/employee/department/{departmentId}", "employee-service", "EmployeeController");
    }


    static String createOrganization(TransactionContext tx) {
        var result = tx.run("""
            CREATE (o:Organization {id: randomuuid(), createdDate: datetime()})
            RETURN o.id AS id
        """);
        var org = result.single();
        var orgId = org.get("id").asString();
        return orgId;
    }

}