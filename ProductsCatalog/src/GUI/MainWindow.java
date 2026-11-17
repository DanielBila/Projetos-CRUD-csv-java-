package GUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.Locale;

import javax.swing.JButton;
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

import Entities.Product;
import Services.ListService;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String FILE_NAME = "products.csv";

	private final DefaultTableModel model = new DefaultTableModel(
			new String[] { "Nome do Produto", "Preço (R$)", "Categoria", "Código" }, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false; // edição só pelo botão "Editar"
		}
	};
	private final JTable table = new JTable(model);

	public MainWindow() {
		super("Cadastro de Produtos");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(750, 450);
		setLocationRelativeTo(null);

		JButton addButton = new JButton("Adicionar");
		JButton editButton = new JButton("Editar");
		JButton deleteButton = new JButton("Apagar");
		addButton.addActionListener(e -> addProduct());
		editButton.addActionListener(e -> editProduct());
		deleteButton.addActionListener(e -> deleteProduct());

		JPanel buttons = new JPanel();
		buttons.add(addButton);
		buttons.add(editButton);
		buttons.add(deleteButton);

		add(new JScrollPane(table), BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);

		refreshTable();
	}

	private static String formatPrice(double price) {
		return String.format(Locale.US, "%.2f", price);
	}

	// Recarrega a tabela a partir da lista do ListService
	private void refreshTable() {
		model.setRowCount(0);
		for (Product p : ListService.GetProducts()) {
			model.addRow(new Object[] { p.getName(), formatPrice(p.getPrice()), p.getCategory(), p.getCode() });
		}
	}

	private void addProduct() {
		Product p = showProductForm("Adicionar produto", null);
		if (p == null) return;
		try {
			ListService.AddProduct(p.getName(), p.getPrice(), p.getCategory(), p.getCode());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao adicionar: " + ex.getMessage());
		}
	}

	private void editProduct() {
		Product selected = selectedProduct();
		if (selected == null) return;
		Product p = showProductForm("Editar produto", selected);
		if (p == null) return;
		try {
			ListService.EditProduct(selected.getCode(), p.getName(), p.getPrice(), p.getCategory(), p.getCode());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao editar: " + ex.getMessage());
		}
	}

	private void deleteProduct() {
		Product selected = selectedProduct();
		if (selected == null) return;
		int answer = JOptionPane.showConfirmDialog(this,
				"Apagar \"" + selected.getName() + "\" (" + selected.getCode() + ")?",
				"Confirmar", JOptionPane.YES_NO_OPTION);
		if (answer != JOptionPane.YES_OPTION) return;
		try {
			ListService.DeleteProduct(selected.getCode());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao apagar: " + ex.getMessage());
		}
	}

	// Produto da linha selecionada na tabela (ou null com aviso)
	private Product selectedProduct() {
		int row = table.getSelectedRow();
		if (row < 0) {
			showError("Selecione um produto na tabela.");
			return null;
		}
		return ListService.GetProducts().get(row);
	}

	/** Mostra o formulário; retorna um Product com os dados digitados ou null se cancelar. */
	private Product showProductForm(String dialogTitle, Product initial) {
		JTextField nameField = new JTextField(initial == null ? "" : initial.getName());
		JTextField priceField = new JTextField(initial == null ? "" : formatPrice(initial.getPrice()));
		JTextField categoryField = new JTextField(initial == null ? "" : initial.getCategory());
		JTextField codeField = new JTextField(initial == null ? "" : initial.getCode());

		JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
		form.add(new JLabel("Nome do Produto:"));
		form.add(nameField);
		form.add(new JLabel("Preço (ex: 350.00):"));
		form.add(priceField);
		form.add(new JLabel("Categoria:"));
		form.add(categoryField);
		form.add(new JLabel("Código (ex: PRD001):"));
		form.add(codeField);

		while (true) {
			int result = JOptionPane.showConfirmDialog(this, form, dialogTitle,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result != JOptionPane.OK_OPTION) return null;

			String n = nameField.getText().trim();
			String pr = priceField.getText().trim().replace(",", "."); // aceita 350,00 ou 350.00
			String c = categoryField.getText().trim();
			String cd = codeField.getText().trim().toUpperCase();

			if (n.isEmpty() || c.isEmpty() || cd.isEmpty()) {
				showError("Nome, categoria e código são obrigatórios.");
			} else if (n.contains(";") || c.contains(";") || cd.contains(";")) {
				showError("Os campos não podem conter ';'.");
			} else if (!pr.matches("\\d+(\\.\\d{1,2})?")) {
				showError("Preço inválido. Use números, ex: 350.00");
			} else {
				return new Product(n, Double.parseDouble(pr), c, cd);
			}
		}
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Atenção", JOptionPane.WARNING_MESSAGE);
	}

	// Pop-up inicial: pergunta em qual pasta o estoque está (ou será criado)
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
			window.setTitle("Cadastro de Produtos - " + dir.getAbsolutePath() + " (" + loaded + " carregados)");
			window.setVisible(true);
		});
	}
}
