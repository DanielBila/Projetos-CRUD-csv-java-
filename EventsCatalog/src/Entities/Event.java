package Entities;

import java.time.LocalDate;

public class Event {

	private String Name;
	private String Location;
	private LocalDate Date;
	private String Organizer;

	// Construtor
	public Event(String name, String location, LocalDate date, String organizer) {
		Edition(name, location, date, organizer);
	}

	public void Edition(String name, String location, LocalDate date, String organizer) {
		Name = name;
		Location = location;
		Date = date;
		Organizer = organizer;
	}

	public String getName() { return Name; }
	public String getLocation() { return Location; }
	public LocalDate getDate() { return Date; }
	public String getOrganizer() { return Organizer; }

	@Override
	public String toString() {
		return "[Name=" + Name + ", Location=" + Location + ", Date=" + Date + ", Organizer=" + Organizer + "]";
	}
}
