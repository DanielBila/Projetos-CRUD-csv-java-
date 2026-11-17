package Services;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entities.Student;

public class ListService {

	private static List<Student> students = new ArrayList<>();
	private static String filePath;

	// Prepara o diretório/CSV e carrega os alunos do arquivo para a lista
	public static int LoadCatalog(String directory, String fileName) {
		filePath = Paths.get(directory, fileName).toString();
		students.clear();
		return CsvServices.InitializeCatalog(directory, fileName);
	}

	// Adiciona só na lista em memória (usado pelo CsvServices ao ler o arquivo)
	public static void AddList(String name, int age, String course, String registration) {
		students.add(new Student(name, age, course, registration));
	}

	// Adiciona um aluno novo: grava no CSV e depois coloca na lista
	public static void AddStudent(String name, int age, String course, String registration) throws IOException {
		if (FindStudent(registration) != null) {
			throw new IllegalArgumentException("Já existe um aluno com a matrícula " + registration);
		}
		CsvServices.WriteStudentCsv(filePath, name, age, course, registration);
		AddList(name, age, course, registration);
	}

	// Apaga o aluno com a matrícula informada do CSV e da lista
	public static int DeleteStudent(String registration) throws IOException {
		int deleted = CsvServices.DeleteStudentCsv(filePath, registration);
		students.removeIf(s -> s.getRegistration().equals(registration.trim()));
		return deleted;
	}

	// Edita o aluno com a matrícula informada no CSV e na lista
	public static int EditStudent(String registration,
								  String newName, int newAge, String newCourse, String newRegistration) throws IOException {
		if (!newRegistration.trim().equals(registration.trim()) && FindStudent(newRegistration) != null) {
			throw new IllegalArgumentException("Já existe um aluno com a matrícula " + newRegistration);
		}
		int edited = CsvServices.EditStudentCsv(filePath, registration, newName, newAge, newCourse, newRegistration);
		Student s = FindStudent(registration);
		if (s != null) s.Edition(newName, newAge, newCourse, newRegistration);
		return edited;
	}

	// Busca um aluno pela matrícula; retorna null se não encontrar
	public static Student FindStudent(String registration) {
		for (Student s : students) {
			if (s.getRegistration().equals(registration.trim())) return s;
		}
		return null;
	}

	// Lista somente leitura, para a interface exibir
	public static List<Student> GetStudents() {
		return Collections.unmodifiableList(students);
	}
}
