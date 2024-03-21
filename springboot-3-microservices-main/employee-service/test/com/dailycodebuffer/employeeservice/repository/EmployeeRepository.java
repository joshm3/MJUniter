package com.dailycodebuffer.employeeservice.repository;
import com.dailycodebuffer.employeeservice.model.Employee;
@org.springframework.stereotype.Repository
public class EmployeeRepository {
    private java.util.List<com.dailycodebuffer.employeeservice.model.Employee> employees = new java.util.ArrayList<>();

    public com.dailycodebuffer.employeeservice.model.Employee add(com.dailycodebuffer.employeeservice.model.Employee employee) {
        employees.add(employee);
        return employee;
    }

    public com.dailycodebuffer.employeeservice.model.Employee findById(java.lang.Long id) {
        return employees.stream().filter(a -> a.id().equals(id)).findFirst().orElseThrow();
    }

    public java.util.List<com.dailycodebuffer.employeeservice.model.Employee> findAll() {
        return employees;
    }

    public java.util.List<com.dailycodebuffer.employeeservice.model.Employee> findByDepartment(java.lang.Long departmentId) {
        return employees.stream().filter(a -> a.departmentId().equals(departmentId)).toList();
    }
}