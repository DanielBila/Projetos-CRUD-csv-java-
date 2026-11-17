package Entities;

public enum Genre {
	Acao("Ação"), Comedia("Comédia"), Drama("Drama"), FiccaoCientifica("Ficção Científica"),
	Terror("Terror"), Romance("Romance"), Animacao("Animação"), Suspense("Suspense"),
	Documentario("Documentário"), Fantasia("Fantasia");

	private final String label;

	Genre(String label) {
		this.label = label;
	}

	// Texto exibido na tela e gravado no CSV (ex.: "Ficção Científica")
	@Override
	public String toString() {
		return label;
	}

	// Converte o texto do CSV para o enum (aceita o rótulo ou o nome, ignorando maiúsculas)
	public static Genre fromText(String value) {
		for (Genre g : values()) {
			if (g.label.equalsIgnoreCase(value) || g.name().equalsIgnoreCase(value)) return g;
		}
		throw new IllegalArgumentException("Gênero inválido: " + value);
	}
}
