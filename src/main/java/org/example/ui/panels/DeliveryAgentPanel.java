package org.example.ui.panels;

import org.example.controller.DeliveryAgentController;
import org.example.model.DeliveryAgent;
import org.example.ui.UIStyleUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DeliveryAgentPanel extends JPanel {

    private final DeliveryAgentController agentController;
    private final Runnable refreshCallback;

    // Form fields
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtLicense;
    private JTextField txtVehicleType;
    private JTextField txtVehiclePlate;
    private JTextField txtVehicleModel;

    // Table
    private JTable tableAgents;
    private DefaultTableModel modelAgents;

    // Track selected mongoId for update/delete
    private String selectedMongoId = null;

    public DeliveryAgentPanel(DeliveryAgentController agentController, Runnable refreshCallback) {
        this.agentController  = agentController;
        this.refreshCallback  = refreshCallback;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    // ── Form Panel ────────────────────────────────────────────────────────────

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(46, 111, 64), 2),
                " Delivery Agent Details ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(120, 220, 150)
        ));
        formPanel.setPreferredSize(new Dimension(380, 620));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtName = createTextField("e.g. Kamal Perera");
        formPanel.add(txtName, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPhone = createTextField("e.g. 0771234567");
        formPanel.add(txtPhone, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtEmail = createTextField("e.g. kamal@example.com");
        formPanel.add(txtEmail, gbc);

        // License
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("License No:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtLicense = createTextField("e.g. LK-001-2020");
        formPanel.add(txtLicense, gbc);

        // Vehicle Type
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Vehicle Type:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtVehicleType = createTextField("e.g. Bike, Van, Truck");
        formPanel.add(txtVehicleType, gbc);

        // Vehicle Plate
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Vehicle Plate:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtVehiclePlate = createTextField("e.g. WP-1234");
        formPanel.add(txtVehiclePlate, gbc);

        // Vehicle Model
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Vehicle Model:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtVehicleModel = createTextField("e.g. Honda CB150");
        formPanel.add(txtVehicleModel, gbc);

        // ── Buttons ───────────────────────────────────────────────────────────

        // Add Button
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 8, 4, 8);
        JButton btnAdd = createButton("Add Agent", new Color(46, 111, 64));
        btnAdd.addActionListener(e -> handleAdd());
        formPanel.add(btnAdd, gbc);

        // Update Button
        gbc.gridy = 8;
        gbc.insets = new Insets(4, 8, 4, 8);
        JButton btnUpdate = createButton("Update Selected", new Color(52, 120, 180));
        btnUpdate.addActionListener(e -> handleUpdate());
        formPanel.add(btnUpdate, gbc);

        // Clear Button
        gbc.gridy = 9;
        gbc.insets = new Insets(4, 8, 8, 8);
        JButton btnClear = createButton("Clear Fields", new Color(80, 80, 80));
        btnClear.addActionListener(e -> clearFields());
        formPanel.add(btnClear, gbc);

        return formPanel;
    }

    // ── Table Panel ───────────────────────────────────────────────────────────

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                " Registered Delivery Agents ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        String[] columns = {"ID", "Name", "Phone", "Email", "License", "Vehicle Type", "Plate", "Model", "Available"};
        modelAgents = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tableAgents = new JTable(modelAgents);
        tableAgents.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableAgents.setRowHeight(28);
        tableAgents.setShowHorizontalLines(true);
        tableAgents.setShowVerticalLines(false);
        tableAgents.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // hide the mongoId column from view but keep it in model
        tableAgents.getColumnModel().getColumn(0).setMinWidth(0);
        tableAgents.getColumnModel().getColumn(0).setMaxWidth(0);
        tableAgents.getColumnModel().getColumn(0).setWidth(0);

        // when a row is selected, populate the form fields
        tableAgents.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromTable();
            }
        });

        tablePanel.add(new JScrollPane(tableAgents), BorderLayout.CENTER);

        // Bottom toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton btnDelete = createButton("Delete Agent", new Color(160, 50, 50));
        btnDelete.addActionListener(e -> handleDelete());
        toolbar.add(btnDelete);

        JButton btnRefresh = createButton("Refresh", new Color(70, 70, 70));
        btnRefresh.addActionListener(e -> refreshData());
        toolbar.add(btnRefresh);

        tablePanel.add(toolbar, BorderLayout.SOUTH);
        return tablePanel;
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleAdd() {
        if (!validateRequiredFields()) return;

        DeliveryAgent agent = buildAgentFromForm();
        DeliveryAgent saved = agentController.addAgent(agent);

        if (saved != null) {
            JOptionPane.showMessageDialog(this,
                    "Delivery agent added successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            refreshCallback.run();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to add agent. Check logs for details.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedMongoId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an agent from the table to update.", "Select Agent",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateRequiredFields()) return;

        DeliveryAgent agent = buildAgentFromForm();
        agent.setMongoId(selectedMongoId);

        DeliveryAgent updated = agentController.updateAgent(agent);
        if (updated != null) {
            JOptionPane.showMessageDialog(this,
                    "Agent updated successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            refreshCallback.run();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update agent.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        int selectedRow = tableAgents.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an agent from the table to delete.", "Select Agent",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = (String) modelAgents.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete agent: " + name + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            agentController.deleteAgent(selectedMongoId);
            JOptionPane.showMessageDialog(this,
                    "Agent deleted successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            refreshCallback.run();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void populateFormFromTable() {
        int selectedRow = tableAgents.getSelectedRow();
        if (selectedRow == -1) return;

        selectedMongoId = (String) modelAgents.getValueAt(selectedRow, 0);
        txtName.setText((String) modelAgents.getValueAt(selectedRow, 1));
        txtPhone.setText((String) modelAgents.getValueAt(selectedRow, 2));
        txtEmail.setText((String) modelAgents.getValueAt(selectedRow, 3));
        txtLicense.setText((String) modelAgents.getValueAt(selectedRow, 4));
        txtVehicleType.setText((String) modelAgents.getValueAt(selectedRow, 5));
        txtVehiclePlate.setText((String) modelAgents.getValueAt(selectedRow, 6));
        txtVehicleModel.setText((String) modelAgents.getValueAt(selectedRow, 7));
    }

    private DeliveryAgent buildAgentFromForm() {
        DeliveryAgent agent = new DeliveryAgent();
        agent.setName(txtName.getText().trim());
        agent.setPhone(txtPhone.getText().trim());
        agent.setEmail(txtEmail.getText().trim());
        agent.setLicenseNumber(txtLicense.getText().trim());
        agent.setVehicleType(txtVehicleType.getText().trim());
        agent.setVehiclePlate(txtVehiclePlate.getText().trim());
        agent.setVehicleModel(txtVehicleModel.getText().trim());
        
        boolean isAvailable = true;
        if (selectedMongoId != null) {
            int selectedRow = tableAgents.getSelectedRow();
            if (selectedRow != -1) {
                String availStr = (String) modelAgents.getValueAt(selectedRow, 8);
                isAvailable = "Yes".equalsIgnoreCase(availStr);
            }
        }
        agent.setAvailable(isAvailable);
        return agent;
    }

    private boolean validateRequiredFields() {
        if (txtName.getText().trim().isEmpty() ||
                txtLicense.getText().trim().isEmpty() ||
                txtVehiclePlate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Name, License Number, and Vehicle Plate are required.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearFields() {
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtLicense.setText("");
        txtVehicleType.setText("");
        txtVehiclePlate.setText("");
        txtVehicleModel.setText("");
        selectedMongoId = null;
        tableAgents.clearSelection();
    }

    public void refreshData() {
        modelAgents.setRowCount(0);
        try {
            List<DeliveryAgent> agents = agentController.getAllAgents();
            for (DeliveryAgent a : agents) {
                modelAgents.addRow(new Object[]{
                        a.getMongoId(),
                        a.getName(),
                        a.getPhone(),
                        a.getEmail(),
                        a.getLicenseNumber(),
                        a.getVehicleType(),
                        a.getVehiclePlate(),
                        a.getVehicleModel(),
                        a.isAvailable() ? "Yes" : "No"
                });
            }
        } catch (Exception e) {
            System.err.println("[DeliveryAgentPanel] Error loading agents: " + e.getMessage());
        }
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────

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