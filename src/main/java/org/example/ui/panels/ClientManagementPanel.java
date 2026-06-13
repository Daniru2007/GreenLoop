package org.example.ui.panels;

import org.example.controller.ClientController;
import org.example.model.Client;
import org.example.ui.UIStyleUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientManagementPanel extends JPanel {

    private final ClientController clientController;
    private final Runnable refreshCallback;

    // Form fields
    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;

    // Table
    private JTable tableClients;
    private DefaultTableModel modelClients;

    // Track selected mongoId for update/delete
    private String selectedMongoId = null;

    public ClientManagementPanel(ClientController clientController, Runnable refreshCallback) {
        this.clientController = clientController;
        this.refreshCallback = refreshCallback;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(46, 111, 64), 2),
                " Client Details ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(120, 220, 150)
        ));
        formPanel.setPreferredSize(new Dimension(380, 620));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtName = createTextField("e.g. Retailer Co");
        formPanel.add(txtName, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Email Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtEmail = createTextField("e.g. contact@retailer.com");
        formPanel.add(txtEmail, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Phone Number:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPhone = createTextField("e.g. 0771122334");
        formPanel.add(txtPhone, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Delivery Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtAddress = createTextField("e.g. 123 Main St, Colombo");
        formPanel.add(txtAddress, gbc);

        // Add Button
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(20, 8, 4, 8);
        JButton btnAdd = createButton("Add Client", new Color(46, 111, 64));
        btnAdd.addActionListener(e -> handleAdd());
        formPanel.add(btnAdd, gbc);

        // Update Button
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 8, 4, 8);
        JButton btnUpdate = createButton("Update Selected", new Color(52, 120, 180));
        btnUpdate.addActionListener(e -> handleUpdate());
        formPanel.add(btnUpdate, gbc);

        // Clear Button
        gbc.gridy = 6;
        gbc.insets = new Insets(4, 8, 8, 8);
        JButton btnClear = createButton("Clear Fields", new Color(80, 80, 80));
        btnClear.addActionListener(e -> clearFields());
        formPanel.add(btnClear, gbc);

        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                " Registered Clients ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        String[] columns = {"ID", "Name", "Email", "Phone", "Address"};
        modelClients = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tableClients = new JTable(modelClients);
        tableClients.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableClients.setRowHeight(28);
        tableClients.setShowHorizontalLines(true);
        tableClients.setShowVerticalLines(false);
        tableClients.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Hide MongoDB ObjectId column from view
        tableClients.getColumnModel().getColumn(0).setMinWidth(0);
        tableClients.getColumnModel().getColumn(0).setMaxWidth(0);
        tableClients.getColumnModel().getColumn(0).setWidth(0);

        tableClients.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromTable();
            }
        });

        tablePanel.add(new JScrollPane(tableClients), BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton btnDelete = createButton("Delete Client", new Color(160, 50, 50));
        btnDelete.addActionListener(e -> handleDelete());
        toolbar.add(btnDelete);

        JButton btnRefresh = createButton("Refresh", new Color(70, 70, 70));
        btnRefresh.addActionListener(e -> refreshData());
        toolbar.add(btnRefresh);

        tablePanel.add(toolbar, BorderLayout.SOUTH);
        return tablePanel;
    }

    private void handleAdd() {
        if (!validateFields()) return;

        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        Client c = clientController.addClient(name, email, phone, address);
        if (c != null) {
            JOptionPane.showMessageDialog(this, "Client registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            refreshCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to register client.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedMongoId == null) {
            JOptionPane.showMessageDialog(this, "Select a client from the table to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateFields()) return;

        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        Client c = clientController.updateClient(selectedMongoId, name, email, phone, address);
        if (c != null) {
            JOptionPane.showMessageDialog(this, "Client details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            refreshCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update client.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        int selectedRow = tableClients.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a client from the table to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = modelClients.getValueAt(selectedRow, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete client: " + name + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            clientController.deleteClient(selectedMongoId);
            JOptionPane.showMessageDialog(this, "Client deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            refreshCallback.run();
        }
    }

    private void populateFormFromTable() {
        int selectedRow = tableClients.getSelectedRow();
        if (selectedRow == -1) return;

        selectedMongoId = modelClients.getValueAt(selectedRow, 0).toString();
        txtName.setText(modelClients.getValueAt(selectedRow, 1).toString());
        txtEmail.setText(modelClients.getValueAt(selectedRow, 2).toString());
        txtPhone.setText(modelClients.getValueAt(selectedRow, 3).toString());
        txtAddress.setText(modelClients.getValueAt(selectedRow, 4).toString());
    }

    private void clearFields() {
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        selectedMongoId = null;
        tableClients.clearSelection();
    }

    private boolean validateFields() {
        if (txtName.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty() ||
                txtPhone.getText().trim().isEmpty() ||
                txtAddress.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public void refreshData() {
        modelClients.setRowCount(0);
        try {
            List<Client> clients = clientController.getAllClients();
            for (Client c : clients) {
                modelClients.addRow(new Object[]{
                        c.getMongoId(),
                        c.getName(),
                        c.getEmail(),
                        c.getPhone(),
                        c.getAddress()
                });
            }
        } catch (Exception e) {
            System.err.println("[ClientPanel] Error loading clients: " + e.getMessage());
        }
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("JComponent.roundRect", true);
        return field;
    }

    private JButton createButton(String text, Color background) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(background);
        btn.setForeground(Color.WHITE);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }
}
