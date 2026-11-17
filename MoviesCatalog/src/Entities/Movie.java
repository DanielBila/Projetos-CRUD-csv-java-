package Entities;

public class Movie {

	private String Title;
	private String Director;
	private int ReleaseYear;
	private Genre Genre;

	// Construtor
	public Movie(String title, String director, int releaseYear, Genre genre) {
		Edition(title, director, releaseYear, genre);
	}

	public void Edition(String title, String director, int releaseYear, Genre genre) {
		Title = title;
		Director = director;
		ReleaseYear = releaseYear;
		Genre = genre;
	}

	public String getTitle() { return Title; }
	public String getDirector() { return Director; }
	public int getReleaseYear() { return ReleaseYear; }
	public Genre getGenre() { return Genre; }

	@Override
	public String toString() {
		return "[Title=" + Title + ", Director=" + Director + ", ReleaseYear=" + ReleaseYear + ", Genre=" + Genre + "]";
	}
}
