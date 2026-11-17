package GUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import Entities.Genre;
import Entities.Movie;
import Services.ListService;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String FILE_NAME = "movies.csv";

	private final DefaultTableModel model = new DefaultTableModel(
			new String[] { "Título", "Diretor", "Ano de Lançamento", "Gênero" }, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false; // edição só pelo botão "Editar"
		}
	};
	private final JTable table = new JTable(model);

	public MainWindow() {
		super("Cadastro de Filmes");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(750, 450);
		setLocationRelativeTo(null);

		JButton addButton = new JButton("Adicionar");
		JButton editButton = new JButton("Editar");
		JButton deleteButton = new JButton("Apagar");
		addButton.addActionListener(e -> addMovie());
		editButton.addActionListener(e -> editMovie());
		deleteButton.addActionListener(e -> deleteMovie());

		JPanel buttons = new JPanel();
		buttons.add(addButton);
		buttons.add(editButton);
		buttons.add(deleteButton);

		add(new JScrollPane(table), BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);

		refreshTable();
	}

	// Recarrega a tabela a partir da lista do ListService
	private void refreshTable() {
		model.setRowCount(0);
		for (Movie m : ListService.GetMovies()) {
			model.addRow(new Object[] { m.getTitle(), m.getDirector(), m.getReleaseYear(), m.getGenre() });
		}
	}

	private void addMovie() {
		Movie m = showMovieForm("Adicionar filme", null);
		if (m == null) return;
		try {
			ListService.AddMovie(m.getTitle(), m.getDirector(), m.getReleaseYear(), m.getGenre());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao adicionar: " + ex.getMessage());
		}
	}

	private void editMovie() {
		Movie selected = selectedMovie();
		if (selected == null) return;
		Movie m = showMovieForm("Editar filme", selected);
		if (m == null) return;
		try {
			ListService.EditMovie(selected.getTitle(), selected.getReleaseYear(),
					m.getTitle(), m.getDirector(), m.getReleaseYear(), m.getGenre());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao editar: " + ex.getMessage());
		}
	}

	private void deleteMovie() {
		Movie selected = selectedMovie();
		if (selected == null) return;
		int answer = JOptionPane.showConfirmDialog(this,
				"Apagar \"" + selected.getTitle() + "\" (" + selected.getReleaseYear() + ")?",
				"Confirmar", JOptionPane.YES_NO_OPTION);
		if (answer != JOptionPane.YES_OPTION) return;
		try {
			ListService.DeleteMovie(selected.getTitle(), selected.getReleaseYear());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao apagar: " + ex.getMessage());
		}
	}

	// Filme da linha selecionada na tabela (ou null com aviso)
	private Movie selectedMovie() {
		int row = table.getSelectedRow();
		if (row < 0) {
			showError("Selecione um filme na tabela.");
			return null;
		}
		return ListService.GetMovies().get(row);
	}

	/** Mostra o formulário; retorna um Movie com os dados digitados ou null se cancelar. */
	private Movie showMovieForm(String dialogTitle, Movie initial) {
		JTextField titleField = new JTextField(initial == null ? "" : initial.getTitle());
		JTextField directorField = new JTextField(initial == null ? "" : initial.getDirector());
		JTextField yearField = new JTextField(initial == null ? "" : String.valueOf(initial.getReleaseYear()));
		JComboBox<Genre> genreBox = new JComboBox<>(Genre.values());
		if (initial != null) genreBox.setSelectedItem(initial.getGenre());

		JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
		form.add(new JLabel("Título:"));
		form.add(titleField);
		form.add(new JLabel("Diretor:"));
		form.add(directorField);
		form.add(new JLabel("Ano de Lançamento (ex: 2010):"));
		form.add(yearField);
		form.add(new JLabel("Gênero:"));
		form.add(genreBox);

		while (true) {
			int result = JOptionPane.showConfirmDialog(this, form, dialogTitle,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result != JOptionPane.OK_OPTION) return null;

			String t = titleField.getText().trim();
			String d = directorField.getText().trim();
			String y = yearField.getText().trim();

			if (t.isEmpty() || d.isEmpty()) {
				showError("Título e diretor são obrigatórios.");
			} else if (t.contains(";") || d.contains(";")) {
				showError("Título e diretor não podem conter ';'.");
			} else if (!y.matches("\\d{4}")) {
				showError("Ano inválido. Use 4 números, ex: 2010.");
			} else {
				return new Movie(t, d, Integer.parseInt(y), (Genre) genreBox.getSelectedItem());
			}
		}
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Atenção", JOptionPane.WARNING_MESSAGE);
	}

	// Pop-up inicial: pergunta em qual pasta o catálogo está (ou será criado)
	private static File askDirectory() {
		JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
		chooser.setDialogTitle("Escolha a pasta onde está (ou onde será criado) o " + FILE_NAME);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return null;
		return chooser.getSelectedFile();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			File dir = askDirectory();
			if (dir == null) {
				JOptionPane.showMessageDialog(null, "Nenhuma pasta escolhida. O programa será fechado.");
				System.exit(0);
			}
			int loaded = ListService.LoadCatalog(dir.getAbsolutePath(), FILE_NAME);
			MainWindow window = new MainWindow();
			window.setTitle("Cadastro de Filmes - " + dir.getAbsolutePath() + " (" + loaded + " carregados)");
			window.setVisible(true);
		});
	}
}
