package GUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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

import Entities.Event;
import Services.ListService;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String FILE_NAME = "events.csv";

	private final DefaultTableModel model = new DefaultTableModel(
			new String[] { "Nome do Evento", "Local", "Data", "Organizador" }, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false; // edição só pelo botão "Editar"
		}
	};
	private final JTable table = new JTable(model);

	public MainWindow() {
		super("Cadastro de Eventos");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(750, 450);
		setLocationRelativeTo(null);

		JButton addButton = new JButton("Adicionar");
		JButton editButton = new JButton("Editar");
		JButton deleteButton = new JButton("Apagar");
		addButton.addActionListener(e -> addEvent());
		editButton.addActionListener(e -> editEvent());
		deleteButton.addActionListener(e -> deleteEvent());

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
		for (Event e : ListService.GetEvents()) {
			model.addRow(new Object[] { e.getName(), e.getLocation(), e.getDate(), e.getOrganizer() });
		}
	}

	private void addEvent() {
		Event e = showEventForm("Adicionar evento", null);
		if (e == null) return;
		try {
			ListService.AddEvent(e.getName(), e.getLocation(), e.getDate(), e.getOrganizer());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao adicionar: " + ex.getMessage());
		}
	}

	private void editEvent() {
		Event selected = selectedEvent();
		if (selected == null) return;
		Event e = showEventForm("Editar evento", selected);
		if (e == null) return;
		try {
			ListService.EditEvent(selected.getName(), selected.getDate(),
					e.getName(), e.getLocation(), e.getDate(), e.getOrganizer());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao editar: " + ex.getMessage());
		}
	}

	private void deleteEvent() {
		Event selected = selectedEvent();
		if (selected == null) return;
		int answer = JOptionPane.showConfirmDialog(this,
				"Apagar \"" + selected.getName() + "\" (" + selected.getDate() + ")?",
				"Confirmar", JOptionPane.YES_NO_OPTION);
		if (answer != JOptionPane.YES_OPTION) return;
		try {
			ListService.DeleteEvent(selected.getName(), selected.getDate());
			refreshTable();
		} catch (Exception ex) {
			showError("Erro ao apagar: " + ex.getMessage());
		}
	}

	// Evento da linha selecionada na tabela (ou null com aviso)
	private Event selectedEvent() {
		int row = table.getSelectedRow();
		if (row < 0) {
			showError("Selecione um evento na tabela.");
			return null;
		}
		return ListService.GetEvents().get(row);
	}

	/** Mostra o formulário; retorna um Event com os dados digitados ou null se cancelar. */
	private Event showEventForm(String dialogTitle, Event initial) {
		JTextField nameField = new JTextField(initial == null ? "" : initial.getName());
		JTextField locationField = new JTextField(initial == null ? "" : initial.getLocation());
		JTextField dateField = new JTextField(initial == null ? "" : initial.getDate().toString());
		JTextField organizerField = new JTextField(initial == null ? "" : initial.getOrganizer());

		JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
		form.add(new JLabel("Nome do Evento:"));
		form.add(nameField);
		form.add(new JLabel("Local:"));
		form.add(locationField);
		form.add(new JLabel("Data (aaaa-mm-dd):"));
		form.add(dateField);
		form.add(new JLabel("Organizador:"));
		form.add(organizerField);

		while (true) {
			int result = JOptionPane.showConfirmDialog(this, form, dialogTitle,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result != JOptionPane.OK_OPTION) return null;

			String n = nameField.getText().trim();
			String l = locationField.getText().trim();
			String o = organizerField.getText().trim();

			if (n.isEmpty() || l.isEmpty() || o.isEmpty()) {
				showError("Nome, local e organizador são obrigatórios.");
			} else if (n.contains(";") || l.contains(";") || o.contains(";")) {
				showError("Os campos não podem conter ';'.");
			} else {
				try {
					return new Event(n, l, LocalDate.parse(dateField.getText().trim()), o);
				} catch (DateTimeParseException ex) {
					showError("Data inválida. Use o formato aaaa-mm-dd, ex: 2024-11-10");
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
			window.setTitle("Cadastro de Eventos - " + dir.getAbsolutePath() + " (" + loaded + " carregados)");
			window.setVisible(true);
		});
	}
}
