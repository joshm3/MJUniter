package com.dailycodebuffer.employeeservice.controller;
import com.dailycodebuffer.employeeservice.model.Employee;
@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping("/employee")
public class EmployeeController {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(com.dailycodebuffer.employeeservice.controller.EmployeeController.class);

    @org.springframework.beans.factory.annotation.Autowired
    com.dailycodebuffer.employeeservice.repository.EmployeeRepository repository;

    @org.springframework.web.bind.annotation.PostMapping
    public com.dailycodebuffer.employeeservice.model.Employee add(@org.springframework.web.bind.annotation.RequestBody
    com.dailycodebuffer.employeeservice.model.Employee employee) {
        com.dailycodebuffer.employeeservice.controller.EmployeeController.LOGGER.info("Employee add: {}", employee);
        return repository.add(employee);
    }

    @org.springframework.web.bind.annotation.GetMapping
    public java.util.List<com.dailycodebuffer.employeeservice.model.Employee> findAll() {
        com.dailycodebuffer.employeeservice.controller.EmployeeController.LOGGER.info("Employee find");
        return repository.findAll();
    }

    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public com.dailycodebuffer.employeeservice.model.Employee findById(@org.springframework.web.bind.annotation.PathVariable("id")
    java.lang.Long id) {
        com.dailycodebuffer.employeeservice.controller.EmployeeController.LOGGER.info("Employee find: id={}", id);
        return repository.findById(id);
    }

    @org.springframework.web.bind.annotation.GetMapping("/department/{departmentId}")
    public java.util.List<com.dailycodebuffer.employeeservice.model.Employee> findByDepartment(@org.springframework.web.bind.annotation.PathVariable("departmentId")
    java.lang.Long departmentId) {
        com.dailycodebuffer.employeeservice.controller.EmployeeController.LOGGER.info("Employee find: departmentId={}", departmentId);
        return repository.findByDepartment(departmentId);
    }
}