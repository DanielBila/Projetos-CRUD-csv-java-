package GUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

import Entities.Employee;
import Services.ListService;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String FILE_NAME = "employees.csv";

	private final DefaultTableModel model = new DefaultTableModel(
			new String[] { "Nome", "Cargo", "Salário (R$)", "Data de Admissão" }, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false; // edição só pelo botão "Editar"
		}
	};
	private final JTable table = new JTable(model);

	public MainWindow() {
		super("Cadastro de Funcionários");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(750, 450);
		setLocationRelativeTo(null);

		JButton addButton = new JButton("Adicionar");
		JButton editButton = new JButton("Editar");
		JButton deleteButton = new JButton("Apagar");
		addButton.addActionListener(e -> addEmployee());
		editButton.addActionListener(e -> editEmployee());
		deleteButton.addActionListener(e -> deleteEmployee());

		JPanel buttons = new JPanel();
		buttons.add(addButton);
		buttons.add(editButton);
		buttons.add(deleteButton);

		add(new JScrollPane(table), BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);

		refreshTable();
	}

	private static String formatSalary(double salary) {
		return String.format(Locale.US, "%.2f", salary);
	}

	// Recarrega a tabela a partir da lista do ListService
	private void refreshTable() {
		model.setRowCount(0);
		for (Employee e : ListService.GetEmployees()) {
			model.addRow(new Object[] { e.getName(), e.getRole(), formatSalary(e.getSalary()), e.getAdmissionDate() });
		}
	}

	private void addEmployee() {
		Employee e = showEmployeeForm("Adicionar funcionário", null);
		if (e == null) return;
		try {
			ListService.AddEmployee(e.getName(), e.getRole(), e.getSalary(), e.getAdmissionDate());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao adicionar: " + ex.getMessage());
		}
	}

	private void editEmployee() {
		Employee selected = selectedEmployee();
		if (selected == null) return;
		Employee e = showEmployeeForm("Editar funcionário", selected);
		if (e == null) return;
		try {
			ListService.EditEmployee(selected.getName(), selected.getAdmissionDate(),
					e.getName(), e.getRole(), e.getSalary(), e.getAdmissionDate());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao editar: " + ex.getMessage());
		}
	}

	private void deleteEmployee() {
		Employee selected = selectedEmployee();
		if (selected == null) return;
		int answer = JOptionPane.showConfirmDialog(this,
				"Apagar \"" + selected.getName() + "\" (admitido em " + selected.getAdmissionDate() + ")?",
				"Confirmar", JOptionPane.YES_NO_OPTION);
		if (answer != JOptionPane.YES_OPTION) return;
		try {
			ListService.DeleteEmployee(selected.getName(), selected.getAdmissionDate());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao apagar: " + ex.getMessage());
		}
	}

	// Funcionário da linha selecionada na tabela (ou null com aviso)
	private Employee selectedEmployee() {
		int row = table.getSelectedRow();
		if (row < 0) {
			showError("Selecione um funcionário na tabela.");
			return null;
		}
		return ListService.GetEmployees().get(row);
	}

	/** Mostra o formulário; retorna um Employee com os dados digitados ou null se cancelar. */
	private Employee showEmployeeForm(String dialogTitle, Employee initial) {
		JTextField nameField = new JTextField(initial == null ? "" : initial.getName());
		JTextField roleField = new JTextField(initial == null ? "" : initial.getRole());
		JTextField salaryField = new JTextField(initial == null ? "" : formatSalary(initial.getSalary()));
		JTextField dateField = new JTextField(initial == null ? "" : initial.getAdmissionDate().toString());

		JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
		form.add(new JLabel("Nome:"));
		form.add(nameField);
		form.add(new JLabel("Cargo:"));
		form.add(roleField);
		form.add(new JLabel("Salário (ex: 7500.00):"));
		form.add(salaryField);
		form.add(new JLabel("Data de Admissão (aaaa-mm-dd):"));
		form.add(dateField);

		while (true) {
			int result = JOptionPane.showConfirmDialog(this, form, dialogTitle,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result != JOptionPane.OK_OPTION) return null;

			String n = nameField.getText().trim();
			String r = roleField.getText().trim();
			String s = salaryField.getText().trim().replace(",", "."); // aceita 7500,00 ou 7500.00

			if (n.isEmpty() || r.isEmpty()) {
				showError("Nome e cargo são obrigatórios.");
			} else if (n.contains(";") || r.contains(";")) {
				showError("Nome e cargo não podem conter ';'.");
			} else if (!s.matches("\\d+(\\.\\d{1,2})?")) {
				showError("Salário inválido. Use números, ex: 7500.00");
			} else {
				try {
					LocalDate date = LocalDate.parse(dateField.getText().trim());
					return new Employee(n, r, Double.parseDouble(s), date);
				} catch (DateTimeParseException ex) {
					showError("Data inválida. Use o formato aaaa-mm-dd, ex: 2020-08-15");
				}
			}
		}
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Atenção", JOptionPane.WARNING_MESSAGE);
	}

	// Pop-up inicial: pergunta em qual pasta o cadastro está (ou será criado)
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
			window.setTitle("Cadastro de Funcionários - " + dir.getAbsolutePath() + " (" + loaded + " carregados)");
			window.setVisible(true);
		});
	}
}
