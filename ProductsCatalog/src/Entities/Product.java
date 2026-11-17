package Entities;

public class Product {

	private String Name;
	private double Price;
	private String Category;
	private String Code; // código (único), ex.: PRD001

	// Construtor
	public Product(String name, double price, String category, String code) {
		Edition(name, price, category, code);
	}

	public void Edition(String name, double price, String category, String code) {
		Name = name;
		Price = price;
		Category = category;
		Code = code;
	}

	public String getName() { return Name; }
	public double getPrice() { return Price; }
	public String getCategory() { return Category; }
	public String getCode() { return Code; }

	@Override
	public String toString() {
		return "[Name=" + Name + ", Price=" + Price + ", Category=" + Category + ", Code=" + Code + "]";
	}
}
