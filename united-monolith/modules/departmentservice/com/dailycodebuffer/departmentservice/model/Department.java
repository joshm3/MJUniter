package modules.departmentservice.com.dailycodebuffer.departmentservice.model;
public class Department {
    private java.lang.Long id;

    private java.lang.String name;

    private java.util.List<modules.departmentservice.com.dailycodebuffer.departmentservice.model.Employee> employees = new java.util.ArrayList<>();

    public Department() {
    }

    public Department(java.lang.Long id, java.lang.String name) {
        this.id = id;
        this.name = name;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((((("Department{" + "id=") + id) + ", name='") + name) + '\'') + ", employees=") + employees) + '}';
    }

    public java.lang.Long getId() {
        return id;
    }

    public void setId(java.lang.Long id) {
        this.id = id;
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public java.util.List<modules.departmentservice.com.dailycodebuffer.departmentservice.model.Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(java.util.List<modules.departmentservice.com.dailycodebuffer.departmentservice.model.Employee> employees) {
        this.employees = employees;
    }
}