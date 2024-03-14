package edu.mjuniter.repositories;

import java.util.Map;

import java.io.InputStream;
import java.util.Properties;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.TransactionContext;
import org.neo4j.driver.exceptions.NoSuchRecordException;
import org.neo4j.driver.Session;

public class NeoRepository implements AutoCloseable{

    private Session session;

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


    public void addService(String serviceName){
        boolean success = session.executeWrite(tx -> addServiceTx(tx, serviceName));
        if (!success) System.out.println("Failed to add service " + serviceName);
    }

    static boolean addServiceTx(TransactionContext tx, String serviceName) {
        // Create new Service node with given name, if not exists already
        tx.run("CREATE (ms:Microservice {name: $serviceName})", Map.of("serviceName", serviceName));
        return true;
    }


    public void addDependency(String client, String server, String URL){
        boolean success = session.executeWrite(tx -> addDependencyTx(tx, URL));
        System.out.printf("User %s added to organization %s.%n", URL);
    }

    static boolean addDependencyTx(TransactionContext tx, String name) {
        // Create new Person node with given name, if not exists already
        tx.run("MERGE (p:Person {name: $name})", Map.of("name", name));
        return true;
    }


    public void clearDB(){
        boolean success = session.executeWrite(tx -> clearTx(tx));
        if(!success) System.out.println("Failed to clear neo4j database");
    }
    
    static boolean clearTx(TransactionContext tx){
        tx.run("MATCH (n) DETACH DELETE n");
        //should probably get result here eventually
        return true;
    }









//Nothing after this is necessary to keep
    public void test(){
        boolean success = session.executeWrite(tx -> clearTx(tx));
        if(!success) System.out.println("Failed to clear neo4j database");

        for (int i=0; i<100; i++) {
            String name = String.format("Thor%d", i);

            try {
                String orgId = session.executeWrite(tx -> employPersonTx(tx, name));
                System.out.printf("User %s added to organization %s.%n", name, orgId);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    


    static String employPersonTx(TransactionContext tx, String name) {
        final int employeeThreshold = 10;

        // Create new Person node with given name, if not exists already
        tx.run("MERGE (p:Person {name: $name})", Map.of("name", name));

        // Obtain most recent organization ID and the number of people linked to it
        var result = tx.run("""
            MATCH (o:Organization)
            RETURN o.id AS id, COUNT{(p:Person)-[r:WORKS_FOR]->(o)} AS employeesN
            ORDER BY o.createdDate DESC
            LIMIT 1
            """);

        Record org = null;
        String orgId = null;
        int employeesN = 0;
        try {
            org = result.single();
            orgId = org.get("id").asString();
            employeesN = org.get("employeesN").asInt();
        } catch (NoSuchRecordException e) {
            // The query is guaranteed to return <= 1 results, so if.single() throws, it means there's none.
            // If no organization exists, create one and add Person to it
            orgId = createOrganization(tx);
            System.out.printf("No orgs available, created %s.%n", orgId);
        }

        // If org does not have too many employees, add this Person to it
        if (employeesN < employeeThreshold) {
            addPersonToOrganization(tx, name, orgId);
            // If the above throws, the transaction will roll back
            // -> not even Person is created!

        // Otherwise, create a new Organization and link Person to it
        } else {
            orgId = createOrganization(tx);
            System.out.printf("Latest org is full, created %s.%n", orgId);
            addPersonToOrganization(tx, name, orgId);
            // If any of the above throws, the transaction will roll back
            // -> not even Person is created!
        }

        return orgId;  // Organization ID to which the new Person ends up in
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

    static void addPersonToOrganization(TransactionContext tx, String personName, String orgId) {
        tx.run("""
            MATCH (o:Organization {id: $orgId})
            MATCH (p:Person {name: $name})
            MERGE (p)-[:WORKS_FOR]->(o)
            """, Map.of("orgId", orgId, "name", personName)
        );
    }
}