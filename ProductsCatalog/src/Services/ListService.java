package Services;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entities.Product;

public class ListService {

	private static List<Product> products = new ArrayList<>();
	private static String filePath;

	// Prepara o diretório/CSV e carrega os produtos do arquivo para a lista
	public static int LoadCatalog(String directory, String fileName) {
		filePath = Paths.get(directory, fileName).toString();
		products.clear();
		return CsvServices.InitializeCatalog(directory, fileName);
	}

	// Adiciona só na lista em memória (usado pelo CsvServices ao ler o arquivo)
	public static void AddList(String name, double price, String category, String code) {
		products.add(new Product(name, price, category, code));
	}

	// Adiciona um produto novo: grava no CSV e depois coloca na lista
	public static void AddProduct(String name, double price, String category, String code) throws IOException {
		if (FindProduct(code) != null) {
			throw new IllegalArgumentException("Já existe um produto com o código " + code);
		}
		CsvServices.WriteProductCsv(filePath, name, price, category, code);
		AddList(name, price, category, code);
	}

	// Apaga o produto com o código informado do CSV e da lista
	public static int DeleteProduct(String code) throws IOException {
		int deleted = CsvServices.DeleteProductCsv(filePath, code);
		products.removeIf(p -> p.getCode().equalsIgnoreCase(code.trim()));
		return deleted;
	}

	// Edita o produto com o código informado no CSV e na lista
	public static int EditProduct(String code,
								  String newName, double newPrice, String newCategory, String newCode) throws IOException {
		if (!newCode.trim().equalsIgnoreCase(code.trim()) && FindProduct(newCode) != null) {
			throw new IllegalArgumentException("Já existe um produto com o código " + newCode);
		}
		int edited = CsvServices.EditProductCsv(filePath, code, newName, newPrice, newCategory, newCode);
		Product p = FindProduct(code);
		if (p != null) p.Edition(newName, newPrice, newCategory, newCode);
		return edited;
	}

	// Busca um produto pelo código; retorna null se não encontrar
	public static Product FindProduct(String code) {
		for (Product p : products) {
			if (p.getCode().equalsIgnoreCase(code.trim())) return p;
		}
		return null;
	}

	// Lista somente leitura, para a interface exibir
	public static List<Product> GetProducts() {
		return Collections.unmodifiableList(products);
	}
}
