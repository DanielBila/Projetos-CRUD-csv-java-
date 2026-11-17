package Services;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entities.Genre;
import Entities.Movie;

public class ListService {

	private static List<Movie> movies = new ArrayList<>();
	private static String filePath;

	// Prepara o diretório/CSV e carrega os filmes do arquivo para a lista
	public static int LoadCatalog(String directory, String fileName) {
		filePath = Paths.get(directory, fileName).toString();
		movies.clear();
		return CsvServices.InitializeCatalog(directory, fileName);
	}

	// Adiciona só na lista em memória (usado pelo CsvServices ao ler o arquivo)
	public static void AddList(String title, String director, int releaseYear, Genre genre) {
		movies.add(new Movie(title, director, releaseYear, genre));
	}

	// Adiciona um filme novo: grava no CSV e depois coloca na lista
	public static void AddMovie(String title, String director, int releaseYear, Genre genre) throws IOException {
		CsvServices.WriteMovieCsv(filePath, title, director, releaseYear, genre);
		AddList(title, director, releaseYear, genre);
	}

	// Apaga o(s) filme(s) com o título e o ano informados do CSV e da lista
	public static int DeleteMovie(String title, int releaseYear) throws IOException {
		int deleted = CsvServices.DeleteMovieCsv(filePath, title, releaseYear);
		movies.removeIf(m -> matches(m, title, releaseYear));
		return deleted;
	}

	// Edita o(s) filme(s) com o título e o ano informados no CSV e na lista
	public static int EditMovie(String title, int releaseYear,
								String newTitle, String newDirector, int newReleaseYear, Genre newGenre) throws IOException {
		int edited = CsvServices.EditMovieCsv(filePath, title, releaseYear, newTitle, newDirector, newReleaseYear, newGenre);
		for (Movie m : movies) {
			if (matches(m, title, releaseYear)) m.Edition(newTitle, newDirector, newReleaseYear, newGenre);
		}
		return edited;
	}

	// Busca um filme pelo título e ano; retorna null se não encontrar
	public static Movie FindMovie(String title, int releaseYear) {
		for (Movie m : movies) {
			if (matches(m, title, releaseYear)) return m;
		}
		return null;
	}

	// Lista somente leitura, para a interface exibir
	public static List<Movie> GetMovies() {
		return Collections.unmodifiableList(movies);
	}

	// Mesmo critério do CsvServices: título (ignorando maiúsculas) + ano
	private static boolean matches(Movie m, String title, int releaseYear) {
		return m.getTitle().trim().equalsIgnoreCase(title.trim()) && m.getReleaseYear() == releaseYear;
	}
}
