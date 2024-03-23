package modules.departmentservice.com.dailycodebuffer.departmentservice.config;
@org.springframework.context.annotation.Configuration
public class WebClientConfig {
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction filterFunction;

    @org.springframework.context.annotation.Bean
    public org.springframework.web.reactive.function.client.WebClient employeeWebClient() {
        return modules.departmentservice.com.dailycodebuffer.departmentservice.config.WebClient.builder().baseUrl("http://employee-service").filter(filterFunction).build();
    }

    @org.springframework.context.annotation.Bean
    public com.dailycodebuffer.departmentservice.client.EmployeeClient employeeClient() {
        org.springframework.web.service.invoker.HttpServiceProxyFactory httpServiceProxyFactory = org.springframework.web.service.invoker.HttpServiceProxyFactory.builder(org.springframework.web.reactive.function.client.support.WebClientAdapter.forClient(employeeWebClient())).build();
        return httpServiceProxyFactory.createClient(com.dailycodebuffer.departmentservice.client.EmployeeClient.class);
    }
}