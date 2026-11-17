package GUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

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

import Entities.Book;
import Entities.Genre;
import Services.ListService;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String FILE_NAME = "books.csv";
	private static final SimpleDateFormat YEAR = new SimpleDateFormat("yyyy");

	private final DefaultTableModel model = new DefaultTableModel(
			new String[] { "Título", "Autor", "Ano", "Gênero" }, 0) {
		@Override
		public boolean isCellEditable(int row, int column) {
			return false; // edição só pelo botão "Editar"
		}
	};
	private final JTable table = new JTable(model);

	public MainWindow() {
		super("Catálogo de Livros");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(700, 400);
		setLocationRelativeTo(null);

		JButton addButton = new JButton("Adicionar");
		JButton editButton = new JButton("Editar");
		JButton deleteButton = new JButton("Apagar");
		addButton.addActionListener(e -> addBook());
		editButton.addActionListener(e -> editBook());
		deleteButton.addActionListener(e -> deleteBook());

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
		for (Book b : ListService.GetBooks()) {
			model.addRow(new Object[] { b.getTitle(), b.getAuthor(),
					YEAR.format(b.getYearOfPublication()), b.getGenre() });
		}
	}

	private void addBook() {
		Object[] data = showBookForm("Adicionar livro", "", "", "", Genre.values()[0]);
		if (data == null) return;
		try {
			ListService.AddBook((String) data[0], (String) data[1], (Date) data[2], (Genre) data[3]);
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao adicionar: " + ex.getMessage());
		}
	}

	private void editBook() {
		int row = table.getSelectedRow();
		if (row < 0) {
			showError("Selecione um livro na tabela.");
			return;
		}
		String title = (String) model.getValueAt(row, 0);
		String year = (String) model.getValueAt(row, 2);
		Object[] data = showBookForm("Editar livro", title, (String) model.getValueAt(row, 1),
				year, (Genre) model.getValueAt(row, 3));
		if (data == null) return;
		try {
			ListService.EditBook(title, YEAR.parse(year),
					(String) data[0], (String) data[1], (Date) data[2], (Genre) data[3]);
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao editar: " + ex.getMessage());
		}
	}

	private void deleteBook() {
		int row = table.getSelectedRow();
		if (row < 0) {
			showError("Selecione um livro na tabela.");
			return;
		}
		String title = (String) model.getValueAt(row, 0);
		String year = (String) model.getValueAt(row, 2);
		int answer = JOptionPane.showConfirmDialog(this, "Apagar \"" + title + "\" (" + year + ")?",
				"Confirmar", JOptionPane.YES_NO_OPTION);
		if (answer != JOptionPane.YES_OPTION) return;
		try {
			ListService.DeleteBook(title, YEAR.parse(year));
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao apagar: " + ex.getMessage());
		}
	}

	/**
	 * Mostra um formulário com os campos do livro.
	 * Retorna {título, autor, ano (Date), gênero} ou null se o usuário cancelar.
	 */
	private Object[] showBookForm(String dialogTitle, String title, String author, String year, Genre genre) {
		JTextField titleField = new JTextField(title);
		JTextField authorField = new JTextField(author);
		JTextField yearField = new JTextField(year);
		JComboBox<Genre> genreBox = new JComboBox<>(Genre.values());
		genreBox.setSelectedItem(genre);

		JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
		form.add(new JLabel("Título:"));
		form.add(titleField);
		form.add(new JLabel("Autor:"));
		form.add(authorField);
		form.add(new JLabel("Ano (ex: 1965):"));
		form.add(yearField);
		form.add(new JLabel("Gênero:"));
		form.add(genreBox);

		while (true) {
			int result = JOptionPane.showConfirmDialog(this, form, dialogTitle,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result != JOptionPane.OK_OPTION) return null;

			String t = titleField.getText().trim();
			String a = authorField.getText().trim();
			String y = yearField.getText().trim();

			if (t.isEmpty() || a.isEmpty()) {
				showError("Título e autor são obrigatórios.");
			} else if (t.contains(";") || a.contains(";")) {
				showError("Título e autor não podem conter ';'.");
			} else if (!y.matches("\\d{1,4}")) {
				showError("Ano inválido. Use só números, ex: 1965.");
			} else {
				try {
					return new Object[] { t, a, YEAR.parse(y), genreBox.getSelectedItem() };
				} catch (Exception ex) {
					showError("Ano inválido.");
				}
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
			window.setTitle("Catálogo de Livros - " + dir.getAbsolutePath() + " (" + loaded + " livros carregados)");
			window.setVisible(true);
		});
	}
}
