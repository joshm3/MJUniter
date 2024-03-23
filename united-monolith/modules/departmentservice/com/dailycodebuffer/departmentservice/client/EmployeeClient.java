package modules.departmentservice.com.dailycodebuffer.departmentservice.client;
@org.springframework.web.service.annotation.HttpExchange
public interface EmployeeClient {
    @org.springframework.web.service.annotation.GetExchange("/employee/department/{departmentId}")
    public java.util.List<com.dailycodebuffer.departmentservice.model.Employee> findByDepartment(@org.springframework.web.bind.annotation.PathVariable("departmentId")
    java.lang.Long departmentId);
}