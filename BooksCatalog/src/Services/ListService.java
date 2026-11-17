package Services;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import Entities.Book;
import Entities.Genre;

public class ListService {

	private static final SimpleDateFormat YEAR = new SimpleDateFormat("yyyy");
	private static List<Book> books = new ArrayList<>();
	private static String filePath;

	// Prepara o diretório/CSV e carrega os livros do arquivo para a lista
	public static int LoadCatalog(String directory, String fileName) {
		filePath = Paths.get(directory, fileName).toString();
		books.clear();
		return CsvServices.InitializeCatalog(directory, fileName);
	}

	// Adiciona só na lista em memória (usado pelo CsvServices ao ler o arquivo)
	public static void AddList(String title, String author, Date yearOfPublication, Genre genre) {
		books.add(new Book(title, author, yearOfPublication, genre));
	}

	// Adiciona um livro novo: grava no CSV e depois coloca na lista
	public static void AddBook(String title, String author, Date yearOfPublication, Genre genre) throws IOException {
		CsvServices.WriteBookCsv(filePath, title, author, yearOfPublication, genre);
		AddList(title, author, yearOfPublication, genre);
	}

	// Apaga o(s) livro(s) com o título e o ano informados do CSV e da lista; retorna quantos foram apagados
	public static int DeleteBook(String title, Date yearOfPublication) throws IOException {
		int deleted = CsvServices.DeleteBookCsv(filePath, title, yearOfPublication);
		books.removeIf(b -> matches(b, title, yearOfPublication));
		return deleted;
	}

	// Edita o(s) livro(s) com o título e o ano informados no CSV e na lista; retorna quantos foram editados
	public static int EditBook(String title, Date yearOfPublication,
							   String newTitle, String newAuthor, Date newYearOfPublication, Genre newGenre) throws IOException {
		int edited = CsvServices.EditBookCsv(filePath, title, yearOfPublication,
				newTitle, newAuthor, newYearOfPublication, newGenre);
		for (Book b : books) {
			if (matches(b, title, yearOfPublication)) {
				b.Edition(newTitle, newAuthor, newYearOfPublication, newGenre);
			}
		}
		return edited;
	}

	// Busca um livro pelo título e ano; retorna null se não encontrar
	public static Book FindBook(String title, Date yearOfPublication) {
		for (Book b : books) {
			if (matches(b, title, yearOfPublication)) return b;
		}
		return null;
	}

	// Lista somente leitura, para a interface exibir (ex.: JTable/JList)
	public static List<Book> GetBooks() {
		return Collections.unmodifiableList(books);
	}

	// Mesmo critério do CsvServices: título (ignorando maiúsculas) + ano
	private static boolean matches(Book b, String title, Date yearOfPublication) {
		return b.getTitle().trim().equalsIgnoreCase(title.trim())
				&& YEAR.format(b.getYearOfPublication()).equals(YEAR.format(yearOfPublication));
	}
}
