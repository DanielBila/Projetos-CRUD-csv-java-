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

public class CsvServices {

	private static final String SEPARATOR = ";";
	private static final String HEADER = "name;location;date;organizer";

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

	// Lê o CSV (pulando o cabeçalho) e envia cada evento para ListService.AddList
	public static int ReadInputCsv(String filePath) {
		int added = 0;
		try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] f = line.split(SEPARATOR);
				if (f.length != 4) continue;
				try {
					ListService.AddList(f[0].trim(), f[1].trim(), LocalDate.parse(f[2].trim()), f[3].trim());
					added++;
				} catch (DateTimeParseException e) {
					System.out.println("Linha ignorada: " + line);
				}
			}
		} catch (IOException e) {
			System.out.println("Erro ao ler o arquivo '" + filePath + "': " + e.getMessage());
		}
		return added;
	}

	// Adiciona um evento no final do CSV (cria o arquivo com cabeçalho se não existir)
	public static void WriteEventCsv(String filePath, String name, String location,
									 LocalDate date, String organizer) throws IOException {
		Path path = Paths.get(filePath);
		boolean newFile = !Files.exists(path);
		try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			if (newFile) {
				bw.write(HEADER);
				bw.newLine();
			}
			bw.write(toLine(name, location, date, organizer));
			bw.newLine();
		}
	}

	// Apaga do CSV o(s) evento(s) com o nome E a data informados; retorna quantos foram apagados
	public static int DeleteEventCsv(String filePath, String name, LocalDate date) throws IOException {
		Path path = Paths.get(filePath);
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		List<String> kept = new ArrayList<>();
		kept.add(lines.get(0));
		for (String line : lines.subList(1, lines.size())) {
			if (!matches(line, name, date)) kept.add(line);
		}
		Files.write(path, kept, StandardCharsets.UTF_8);
		return lines.size() - kept.size();
	}

	// Substitui no CSV o(s) evento(s) com o nome E a data informados pelos novos dados; retorna quantos foram editados
	public static int EditEventCsv(String filePath, String name, LocalDate date,
								   String newName, String newLocation, LocalDate newDate, String newOrganizer) throws IOException {
		Path path = Paths.get(filePath);
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		String newLine = toLine(newName, newLocation, newDate, newOrganizer);
		int edited = 0;
		for (int i = 1; i < lines.size(); i++) {
			if (matches(lines.get(i), name, date)) {
				lines.set(i, newLine);
				edited++;
			}
		}
		Files.write(path, lines, StandardCharsets.UTF_8);
		return edited;
	}

	// Data no formato 2024-11-10
	private static String toLine(String name, String location, LocalDate date, String organizer) {
		return String.join(SEPARATOR, name, location, date.toString(), organizer);
	}

	// Verifica se a linha do CSV tem o nome (ignorando maiúsculas) e a data informados
	private static boolean matches(String line, String name, LocalDate date) {
		String[] f = line.split(SEPARATOR);
		return f.length == 4
				&& f[0].trim().equalsIgnoreCase(name.trim())
				&& f[2].trim().equals(date.toString());
	}
}
