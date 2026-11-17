package Entities;

public class Student {

	private String Name;
	private int Age;
	private String Course;
	private String Registration; // matrícula (única)

	// Construtor
	public Student(String name, int age, String course, String registration) {
		Edition(name, age, course, registration);
	}

	public void Edition(String name, int age, String course, String registration) {
		Name = name;
		Age = age;
		Course = course;
		Registration = registration;
	}

	public String getName() { return Name; }
	public int getAge() { return Age; }
	public String getCourse() { return Course; }
	public String getRegistration() { return Registration; }

	@Override
	public String toString() {
		return "[Name=" + Name + ", Age=" + Age + ", Course=" + Course + ", Registration=" + Registration + "]";
	}
}
