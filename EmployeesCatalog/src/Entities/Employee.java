package Entities;

import java.time.LocalDate;

public class Employee {

	private String Name;
	private String Role;
	private double Salary;
	private LocalDate AdmissionDate;

	// Construtor
	public Employee(String name, String role, double salary, LocalDate admissionDate) {
		Edition(name, role, salary, admissionDate);
	}

	public void Edition(String name, String role, double salary, LocalDate admissionDate) {
		Name = name;
		Role = role;
		Salary = salary;
		AdmissionDate = admissionDate;
	}

	public String getName() { return Name; }
	public String getRole() { return Role; }
	public double getSalary() { return Salary; }
	public LocalDate getAdmissionDate() { return AdmissionDate; }

	@Override
	public String toString() {
		return "[Name=" + Name + ", Role=" + Role + ", Salary=" + Salary + ", AdmissionDate=" + AdmissionDate + "]";
	}
}
