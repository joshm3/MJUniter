package modules;
@org.springframework.boot.autoconfigure.SpringBootApplication
@org.springframework.cloud.client.discovery.EnableDiscoveryClient
class Application {
    public static void main(java.lang.String[] ) {
        SpringApplication.run(DepartmentServiceApplication.class, args);
    }
}