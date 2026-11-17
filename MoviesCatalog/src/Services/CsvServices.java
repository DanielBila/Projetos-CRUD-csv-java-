package Services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import Entities.Genre;

public class CsvServices {

	private static final String SEPARATOR = ";";
	private static final String HEADER = "title;director;releaseYear;genre";

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

	// Lê o CSV (pulando o cabeçalho) e envia cada filme para ListService.AddList
	public static int ReadInputCsv(String filePath) {
		int added = 0;
		try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] f = line.split(SEPARATOR);
				if (f.length != 4) continue;
				try {
					ListService.AddList(f[0].trim(), f[1].trim(), Integer.parseInt(f[2].trim()), Genre.fromText(f[3].trim()));
					added++;
				} catch (IllegalArgumentException e) {
					System.out.println("Linha ignorada: " + line);
				}
			}
		} catch (IOException e) {
			System.out.println("Erro ao ler o arquivo '" + filePath + "': " + e.getMessage());
		}
		return added;
	}

	// Adiciona um filme no final do CSV (cria o arquivo com cabeçalho se não existir)
	public static void WriteMovieCsv(String filePath, String title, String director,
									 int releaseYear, Genre genre) throws IOException {
		Path path = Paths.get(filePath);
		boolean newFile = !Files.exists(path);
		try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			if (newFile) {
				bw.write(HEADER);
				bw.newLine();
			}
			bw.write(toLine(title, director, releaseYear, genre));
			bw.newLine();
		}
	}

	// Apaga do CSV o(s) filme(s) com o título E o ano informados; retorna quantos foram apagados
	public static int DeleteMovieCsv(String filePath, String title, int releaseYear) throws IOException {
		Path path = Paths.get(filePath);
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		List<String> kept = new ArrayList<>();
		kept.add(lines.get(0));
		for (String line : lines.subList(1, lines.size())) {
			if (!matches(line, title, releaseYear)) kept.add(line);
		}
		Files.write(path, kept, StandardCharsets.UTF_8);
		return lines.size() - kept.size();
	}

	// Substitui no CSV o(s) filme(s) com o título E o ano informados pelos novos dados; retorna quantos foram editados
	public static int EditMovieCsv(String filePath, String title, int releaseYear,
								   String newTitle, String newDirector, int newReleaseYear, Genre newGenre) throws IOException {
		Path path = Paths.get(filePath);
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		String newLine = toLine(newTitle, newDirector, newReleaseYear, newGenre);
		int edited = 0;
		for (int i = 1; i < lines.size(); i++) {
			if (matches(lines.get(i), title, releaseYear)) {
				lines.set(i, newLine);
				edited++;
			}
		}
		Files.write(path, lines, StandardCharsets.UTF_8);
		return edited;
	}

	private static String toLine(String title, String director, int releaseYear, Genre genre) {
		return String.join(SEPARATOR, title, director, String.valueOf(releaseYear), genre.toString());
	}

	// Verifica se a linha do CSV tem o título (ignorando maiúsculas) e o ano informados
	private static boolean matches(String line, String title, int releaseYear) {
		String[] f = line.split(SEPARATOR);
		return f.length == 4
				&& f[0].trim().equalsIgnoreCase(title.trim())
				&& f[2].trim().equals(String.valueOf(releaseYear));
	}
}
