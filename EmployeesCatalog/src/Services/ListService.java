package Services;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entities.Employee;

public class ListService {

	private static List<Employee> employees = new ArrayList<>();
	private static String filePath;

	// Prepara o diretório/CSV e carrega os funcionários do arquivo para a lista
	public static int LoadCatalog(String directory, String fileName) {
		filePath = Paths.get(directory, fileName).toString();
		employees.clear();
		return CsvServices.InitializeCatalog(directory, fileName);
	}

	// Adiciona só na lista em memória (usado pelo CsvServices ao ler o arquivo)
	public static void AddList(String name, String role, double salary, LocalDate admissionDate) {
		employees.add(new Employee(name, role, salary, admissionDate));
	}

	// Adiciona um funcionário novo: grava no CSV e depois coloca na lista
	public static void AddEmployee(String name, String role, double salary, LocalDate admissionDate) throws IOException {
		CsvServices.WriteEmployeeCsv(filePath, name, role, salary, admissionDate);
		AddList(name, role, salary, admissionDate);
	}

	// Apaga o(s) funcionário(s) com o nome e a data de admissão informados do CSV e da lista
	public static int DeleteEmployee(String name, LocalDate admissionDate) throws IOException {
		int deleted = CsvServices.DeleteEmployeeCsv(filePath, name, admissionDate);
		employees.removeIf(e -> matches(e, name, admissionDate));
		return deleted;
	}

	// Edita o(s) funcionário(s) com o nome e a data de admissão informados no CSV e na lista
	public static int EditEmployee(String name, LocalDate admissionDate,
								   String newName, String newRole, double newSalary, LocalDate newAdmissionDate) throws IOException {
		int edited = CsvServices.EditEmployeeCsv(filePath, name, admissionDate, newName, newRole, newSalary, newAdmissionDate);
		for (Employee e : employees) {
			if (matches(e, name, admissionDate)) e.Edition(newName, newRole, newSalary, newAdmissionDate);
		}
		return edited;
	}

	// Busca um funcionário pelo nome e data de admissão; retorna null se não encontrar
	public static Employee FindEmployee(String name, LocalDate admissionDate) {
		for (Employee e : employees) {
			if (matches(e, name, admissionDate)) return e;
		}
		return null;
	}

	// Lista somente leitura, para a interface exibir
	public static List<Employee> GetEmployees() {
		return Collections.unmodifiableList(employees);
	}

	// Mesmo critério do CsvServices: nome (ignorando maiúsculas) + data de admissão
	private static boolean matches(Employee e, String name, LocalDate admissionDate) {
		return e.getName().trim().equalsIgnoreCase(name.trim()) && e.getAdmissionDate().equals(admissionDate);
	}
}
