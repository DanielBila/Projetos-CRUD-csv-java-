package Services;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entities.Event;

public class ListService {

	private static List<Event> events = new ArrayList<>();
	private static String filePath;

	// Prepara o diretório/CSV e carrega os eventos do arquivo para a lista
	public static int LoadCatalog(String directory, String fileName) {
		filePath = Paths.get(directory, fileName).toString();
		events.clear();
		return CsvServices.InitializeCatalog(directory, fileName);
	}

	// Adiciona só na lista em memória (usado pelo CsvServices ao ler o arquivo)
	public static void AddList(String name, String location, LocalDate date, String organizer) {
		events.add(new Event(name, location, date, organizer));
	}

	// Adiciona um evento novo: grava no CSV e depois coloca na lista
	public static void AddEvent(String name, String location, LocalDate date, String organizer) throws IOException {
		CsvServices.WriteEventCsv(filePath, name, location, date, organizer);
		AddList(name, location, date, organizer);
	}

	// Apaga o(s) evento(s) com o nome e a data informados do CSV e da lista
	public static int DeleteEvent(String name, LocalDate date) throws IOException {
		int deleted = CsvServices.DeleteEventCsv(filePath, name, date);
		events.removeIf(e -> matches(e, name, date));
		return deleted;
	}

	// Edita o(s) evento(s) com o nome e a data informados no CSV e na lista
	public static int EditEvent(String name, LocalDate date,
								String newName, String newLocation, LocalDate newDate, String newOrganizer) throws IOException {
		int edited = CsvServices.EditEventCsv(filePath, name, date, newName, newLocation, newDate, newOrganizer);
		for (Event e : events) {
			if (matches(e, name, date)) e.Edition(newName, newLocation, newDate, newOrganizer);
		}
		return edited;
	}

	// Busca um evento pelo nome e data; retorna null se não encontrar
	public static Event FindEvent(String name, LocalDate date) {
		for (Event e : events) {
			if (matches(e, name, date)) return e;
		}
		return null;
	}

	// Lista somente leitura, para a interface exibir
	public static List<Event> GetEvents() {
		return Collections.unmodifiableList(events);
	}

	// Mesmo critério do CsvServices: nome (ignorando maiúsculas) + data
	private static boolean matches(Event e, String name, LocalDate date) {
		return e.getName().trim().equalsIgnoreCase(name.trim()) && e.getDate().equals(date);
	}
}
