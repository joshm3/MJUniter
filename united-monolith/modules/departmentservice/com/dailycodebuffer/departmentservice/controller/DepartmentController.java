package modules.departmentservice.com.dailycodebuffer.departmentservice.controller;
@org.springframework.stereotype.Controller
@org.springframework.web.bind.annotation.RequestMapping("/department")
public class DepartmentController {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(com.dailycodebuffer.departmentservice.controller.DepartmentController.class);

    @org.springframework.beans.factory.annotation.Autowired
    private com.dailycodebuffer.departmentservice.repository.DepartmentRepository repository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.dailycodebuffer.departmentservice.client.EmployeeClient employeeClient;

    @org.springframework.web.bind.annotation.PostMapping
    public com.dailycodebuffer.departmentservice.model.Department add(@org.springframework.web.bind.annotation.RequestBody
    com.dailycodebuffer.departmentservice.model.Department department) {
        modules.departmentservice.com.dailycodebuffer.departmentservice.controller.DepartmentController.info("Department add: {}", department);
        return repository.addDepartment(department);
    }

    @org.springframework.web.bind.annotation.GetMapping
    public java.util.List<com.dailycodebuffer.departmentservice.model.Department> findAll() {
        com.dailycodebuffer.departmentservice.controller.DepartmentController.LOGGER.info("Department find");
        return repository.findAll();
    }

    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public com.dailycodebuffer.departmentservice.model.Department findById(@org.springframework.web.bind.annotation.PathVariable
    java.lang.Long id) {
        com.dailycodebuffer.departmentservice.controller.DepartmentController.LOGGER.info("Department find: id={}", id);
        return repository.findById(id);
    }

    @org.springframework.web.bind.annotation.GetMapping("/with-employees")
    public java.util.List<com.dailycodebuffer.departmentservice.model.Department> findAllWithEmployees() {
        com.dailycodebuffer.departmentservice.controller.DepartmentController.LOGGER.info("Department find");
        java.util.List<com.dailycodebuffer.departmentservice.model.Department> departments = repository.findAll();
        departments.forEach(department -> department.setEmployees(employeeClient.findByDepartment(department.getId())));
        return departments;
    }
}