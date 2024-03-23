package modules.departmentservice.com.dailycodebuffer.departmentservice.repository;
@org.springframework.stereotype.Repository
public class DepartmentRepository {
    private java.util.List<com.dailycodebuffer.departmentservice.model.Department> departments = new java.util.ArrayList<>();

    public com.dailycodebuffer.departmentservice.model.Department addDepartment(com.dailycodebuffer.departmentservice.model.Department department) {
        departments.add(department);
        return department;
    }

    public com.dailycodebuffer.departmentservice.model.Department findById(java.lang.Long id) {
        return departments.stream().filter(department -> department.getId().equals(id)).findFirst().orElseThrow();
    }

    public java.util.List<com.dailycodebuffer.departmentservice.model.Department> findAll() {
        return departments;
    }
}