# MJUniter
A tool for automating the migration from Java Spring-Boot microservice systems to monolith systems.

1. Get neo4j desktop running
2. Fill in src/main/resources/*.properties files
3. mvn clean package
4. java -cp target/mjuniter-dev-0.1.jar edu.mjuniter.MJUniter


TODO:
1. Implementation - Finish Microservice.analyze() and related functions
2. Implementation - Start and finish UnitedSystem to build the modular monoltih
3. Find 5 good open source microservice case studies in all or mostly java with spring boot
4. Implementation - make sure mjuniter works on each one
5. Evaluation - Conduct more literature review to find ideal metrics
6. Evaluation - run mjuniter on the open source microservice applications and get metrics
Finish writing paper - May 12