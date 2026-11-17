package Services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CsvServices {

	private static final String SEPARATOR = ";";
	private static final String HEADER = "name;role;salary;admissionDate";

	// Cria o diretório e o CSV se não existirem; se existirem, carrega o CSV na lista
	public static int InitializeCatalog(String directory, String fileName) {
		Path file = Paths.get(directory, fileName);
		try {
			Files.createDirectories(file.getParent());
			if (!Files.exists(file)) {
				Files.writeString(file, HEADER + System.lineSeparator(), StandardCharsets.UTF_8);
				return 0;
			}
		} catch (IOException e) {
			System.out.println("Erro ao preparar o diretório '" + directory + "': " + e.getMessage());
			return 0;
		}
		return ReadInputCsv(file.toString());
	}

	// Lê o CSV (pulando o cabeçalho) e envia cada funcionário para ListService.AddList
	public static int ReadInputCsv(String filePath) {
		int added = 0;
		try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] f = line.split(SEPARATOR);
				if (f.length != 4) continue;
				try {
					ListService.AddList(f[0].trim(), f[1].trim(), Double.parseDouble(f[2].trim()), LocalDate.parse(f[3].trim()));
					added++;
				} catch (IllegalArgumentException | DateTimeParseException e) {
					System.out.println("Linha ignorada: " + line);
				}
			}
		} catch (IOException e) {
			System.out.println("Erro ao ler o arquivo '" + filePath + "': " + e.getMessage());
		}
		return added;
	}

	// Adiciona um funcionário no final do CSV (cria o arquivo com cabeçalho se não existir)
	public static void WriteEmployeeCsv(String filePath, String name, String role,
										double salary, LocalDate admissionDate) throws IOException {
		Path path = Paths.get(filePath);
		boolean newFile = !Files.exists(path);
		try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			if (newFile) {
				bw.write(HEADER);
				bw.newLine();
			}
			bw.write(toLine(name, role, salary, admissionDate));
			bw.newLine();
		}
	}

	// Apaga do CSV o(s) funcionário(s) com o nome E a data de admissão informados; retorna quantos foram apagados
	public static int DeleteEmployeeCsv(String filePath, String name, LocalDate admissionDate) throws IOException {
		Path path = Paths.get(filePath);
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		List<String> kept = new ArrayList<>();
		kept.add(lines.get(0));
		for (String line : lines.subList(1, lines.size())) {
			if (!matches(line, name, admissionDate)) kept.add(line);
		}
		Files.write(path, kept, StandardCharsets.UTF_8);
		return lines.size() - kept.size();
	}

	// Substitui no CSV o(s) funcionário(s) com o nome E a data informados pelos novos dados; retorna quantos foram editados
	public static int EditEmployeeCsv(String filePath, String name, LocalDate admissionDate,
									  String newName, String newRole, double newSalary, LocalDate newAdmissionDate) throws IOException {
		Path path = Paths.get(filePath);
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		String newLine = toLine(newName, newRole, newSalary, newAdmissionDate);
		int edited = 0;
		for (int i = 1; i < lines.size(); i++) {
			if (matches(lines.get(i), name, admissionDate)) {
				lines.set(i, newLine);
				edited++;
			}
		}
		Files.write(path, lines, StandardCharsets.UTF_8);
		return edited;
	}

	// Salário com ponto e 2 casas (ex.: 7500.00) e data no formato 2020-08-15
	private static String toLine(String name, String role, double salary, LocalDate admissionDate) {
		return String.join(SEPARATOR, name, role, String.format(Locale.US, "%.2f", salary), admissionDate.toString());
	}

	// Verifica se a linha do CSV tem o nome (ignorando maiúsculas) e a data de admissão informados
	private static boolean matches(String line, String name, LocalDate admissionDate) {
		String[] f = line.split(SEPARATOR);
		return f.length == 4
				&& f[0].trim().equalsIgnoreCase(name.trim())
				&& f[3].trim().equals(admissionDate.toString());
	}
}
