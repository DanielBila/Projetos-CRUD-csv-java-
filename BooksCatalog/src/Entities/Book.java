package Entities;

import java.io.IOException;
import java.util.Date;

public class Book {

	private String Title;
	private String Author;
	private Date YearOfPublication;
	private Genre Genre;
	//Construtor 
	public Book(String title,String author,
				Date yearOfPublication, Genre genre) {
		Title = title;
		Author = author;
		YearOfPublication = yearOfPublication;
		Genre = genre;		
		
	}
	
	public void Edition(String title,String author,
			Date yearOfPublication, Genre genre) {

			Title = title;
			Author = author;
			YearOfPublication = yearOfPublication;
			Genre = genre;		
	
	}

	public String getTitle() { return Title; }
	public String getAuthor() { return Author; }
	public Date getYearOfPublication() { return YearOfPublication; }
	public Genre getGenre() { return Genre; }

	@Override
	public String toString() {
		return "[Title=" + Title + ", Author=" + Author + ", YearOfPublication=" + YearOfPublication + ", Genre="
				+ Genre + "]";
	}

	
}
