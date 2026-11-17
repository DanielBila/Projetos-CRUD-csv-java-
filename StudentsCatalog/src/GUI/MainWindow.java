package GUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;

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

import Entities.Student;
import Services.ListService;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String FILE_NAME = "students.csv";

	private final DefaultTableModel model = new DefaultTableModel(
			new String[] { "Nome", "Idade", "Curso", "Matrícula" }, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false; // edição só pelo botão "Editar"
		}
	};
	private final JTable table = new JTable(model);

	public MainWindow() {
		super("Cadastro de Alunos");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(750, 450);
		setLocationRelativeTo(null);

		JButton addButton = new JButton("Adicionar");
		JButton editButton = new JButton("Editar");
		JButton deleteButton = new JButton("Apagar");
		addButton.addActionListener(e -> addStudent());
		editButton.addActionListener(e -> editStudent());
		deleteButton.addActionListener(e -> deleteStudent());

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
		for (Student s : ListService.GetStudents()) {
			model.addRow(new Object[] { s.getName(), s.getAge(), s.getCourse(), s.getRegistration() });
		}
	}

	private void addStudent() {
		Student s = showStudentForm("Adicionar aluno", null);
		if (s == null) return;
		try {
			ListService.AddStudent(s.getName(), s.getAge(), s.getCourse(), s.getRegistration());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao adicionar: " + ex.getMessage());
		}
	}

	private void editStudent() {
		Student selected = selectedStudent();
		if (selected == null) return;
		Student s = showStudentForm("Editar aluno", selected);
		if (s == null) return;
		try {
			ListService.EditStudent(selected.getRegistration(),
					s.getName(), s.getAge(), s.getCourse(), s.getRegistration());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao editar: " + ex.getMessage());
		}
	}

	private void deleteStudent() {
		Student selected = selectedStudent();
		if (selected == null) return;
		int answer = JOptionPane.showConfirmDialog(this,
				"Apagar \"" + selected.getName() + "\" (matrícula " + selected.getRegistration() + ")?",
				"Confirmar", JOptionPane.YES_NO_OPTION);
		if (answer != JOptionPane.YES_OPTION) return;
		try {
			ListService.DeleteStudent(selected.getRegistration());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao apagar: " + ex.getMessage());
		}
	}

	// Aluno da linha selecionada na tabela (ou null com aviso)
	private Student selectedStudent() {
		int row = table.getSelectedRow();
		if (row < 0) {
			showError("Selecione um aluno na tabela.");
			return null;
		}
		return ListService.GetStudents().get(row);
	}

	/** Mostra o formulário; retorna um Student com os dados digitados ou null se cancelar. */
	private Student showStudentForm(String dialogTitle, Student initial) {
		JTextField nameField = new JTextField(initial == null ? "" : initial.getName());
		JTextField ageField = new JTextField(initial == null ? "" : String.valueOf(initial.getAge()));
		JTextField courseField = new JTextField(initial == null ? "" : initial.getCourse());
		JTextField registrationField = new JTextField(initial == null ? "" : initial.getRegistration());

		JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
		form.add(new JLabel("Nome:"));
		form.add(nameField);
		form.add(new JLabel("Idade:"));
		form.add(ageField);
		form.add(new JLabel("Curso:"));
		form.add(courseField);
		form.add(new JLabel("Matrícula (só números):"));
		form.add(registrationField);

		while (true) {
			int result = JOptionPane.showConfirmDialog(this, form, dialogTitle,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result != JOptionPane.OK_OPTION) return null;

			String n = nameField.getText().trim();
			String a = ageField.getText().trim();
			String c = courseField.getText().trim();
			String r = registrationField.getText().trim();

			if (n.isEmpty() || c.isEmpty()) {
				showError("Nome e curso são obrigatórios.");
			} else if (n.contains(";") || c.contains(";")) {
				showError("Nome e curso não podem conter ';'.");
			} else if (!a.matches("\\d{1,3}")) {
				showError("Idade inválida. Use só números, ex: 22.");
			} else if (!r.matches("\\d+")) {
				showError("Matrícula inválida. Use só números, ex: 202312345.");
			} else {
				return new Student(n, Integer.parseInt(a), c, r);
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
			window.setTitle("Cadastro de Alunos - " + dir.getAbsolutePath() + " (" + loaded + " carregados)");
			window.setVisible(true);
		});
	}
}
