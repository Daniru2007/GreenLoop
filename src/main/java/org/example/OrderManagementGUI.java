package org.example;

import com.formdev.flatlaf.FlatDarkLaf;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.example.config.DBManager;
import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.controller.OrderService;
import org.example.controller.ProductController;
import org.example.model.CustomerOrder;
import org.example.model.DeliveryAgent;
import org.example.model.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderManagementGUI extends JFrame {

    private final OrderService orderService;
    private final ProductController productController;
    private final DeliveryAgentImpl agentRepo;

    // Tab 1 Components (Orders)
    private JTextField txtClientName;
    private JComboBox<ProductWrapper> comboProducts;
    private JSpinner spinQuantity;
    private JLabel lblUnitPrice;
    private JLabel lblTotalAmount;
    private JLabel lblStockAvailable;
    private JButton btnPlaceOrder;

    private JTable tableOrders;
    private DefaultTableModel modelOrders;
    private JButton btnCancelOrder;
    private JButton btnCompleteDelivery;
    private JButton btnRefreshOrders;

    // Tab 2 Components (Agent Assignment)
    private JComboBox<OrderWrapper> comboPendingOrders;
    private JComboBox<AgentWrapper> comboAvailableAgents;
    private JButton btnAssignAgent;

    private JTable tableAgents;
    private DefaultTableModel modelAgents;
    private JButton btnRefreshAgents;

    // Tab 3 Components (Add Agent)
    private JTextField txtAgentName;
    private JTextField txtAgentPhone;
    private JTextField txtAgentEmail;
    private JTextField txtLicenseNumber;
    private JTextField txtVehicleType;
    private JTextField txtVehiclePlate;
    private JTextField txtVehicleModel;
    private JButton btnAddAgent;

    public OrderManagementGUI() {
        // Initialize services
        orderService = new OrderService();
        productController = new ProductController();
        agentRepo = new DeliveryAgentImpl();

        // Frame Setup
        setTitle("GreenLoop - Order & Delivery Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 700);
        setLocationRelativeTo(null);

        // Styling / Theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Could not initialize FlatLaf. Using default metal theme.");
        }

        // Main Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Create Tabs
        tabbedPane.addTab("Order Processing", createOrderProcessingTab());
        tabbedPane.addTab("Delivery & Scheduling", createDeliverySchedulingTab());
        tabbedPane.addTab("Add Delivery Agent", createAddAgentTab());

        add(tabbedPane);

        // Load initial data
        refreshAllData();
    }

    // ==========================================
    // TAB 1: Order Processing Panel
    // ==========================================
    private JPanel createOrderProcessingTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT FORM PANEL (Place Order)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(46, 111, 64), 2), 
                " Place Client Order ", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14), new Color(120, 220, 150)));
        formPanel.setPreferredSize(new Dimension(380, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Client Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createStyledLabel("Client Name:"), gbc);
        gbc.gridx = 1; txtClientName = new JTextField();
        txtClientName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(txtClientName, gbc);

        // Row 1: Product Selection
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("Select Product:"), gbc);
        gbc.gridx = 1;
        comboProducts = new JComboBox<>();
        comboProducts.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboProducts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCalculations();
            }
        });
        formPanel.add(comboProducts, gbc);

        // Row 2: Quantity
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("Quantity Ordered:"), gbc);
        gbc.gridx = 1;
        spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spinQuantity.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinQuantity.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateCalculations();
            }
        });
        formPanel.add(spinQuantity, gbc);

        // Row 3: Available Stock
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createStyledLabel("Available Stock:"), gbc);
        gbc.gridx = 1;
        lblStockAvailable = new JLabel("-");
        lblStockAvailable.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblStockAvailable, gbc);

        // Row 4: Unit Price
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createStyledLabel("Unit Price:"), gbc);
        gbc.gridx = 1;
        lblUnitPrice = new JLabel("Rs. 0.00");
        lblUnitPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblUnitPrice, gbc);

        // Row 5: Total Amount
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createStyledLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        lblTotalAmount = new JLabel("Rs. 0.00");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalAmount.setForeground(new Color(100, 200, 120));
        formPanel.add(lblTotalAmount, gbc);

        // Row 6: Place Order Button
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 8, 8, 8);
        btnPlaceOrder = new JButton("Place Client Order");
        btnPlaceOrder.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPlaceOrder.setBackground(new Color(46, 111, 64));
        btnPlaceOrder.setForeground(Color.WHITE);
        btnPlaceOrder.setFocusPainted(false);
        btnPlaceOrder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePlaceOrder();
            }
        });
        formPanel.add(btnPlaceOrder, gbc);

        mainPanel.add(formPanel, BorderLayout.WEST);

        // RIGHT PANEL (Orders Table)
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), 
                " Order Status Tracking ", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14)));

        String[] columns = {"Order ID", "Client Name", "Product Name", "Qty", "Total Amount", "Status", "Delivery Agent"};
        modelOrders = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableOrders = new JTable(modelOrders);
        tableOrders.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableOrders.setRowHeight(25);
        tableOrders.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scrollPane = new JScrollPane(tableOrders);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions Toolbar
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        btnCompleteDelivery = new JButton("Complete Delivery");
        btnCompleteDelivery.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCompleteDelivery.setBackground(new Color(30, 100, 160));
        btnCompleteDelivery.setForeground(Color.WHITE);
        btnCompleteDelivery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCompleteDelivery();
            }
        });
        actionToolbar.add(btnCompleteDelivery);

        btnCancelOrder = new JButton("Cancel Order");
        btnCancelOrder.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelOrder.setBackground(new Color(160, 50, 50));
        btnCancelOrder.setForeground(Color.WHITE);
        btnCancelOrder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCancelOrder();
            }
        });
        actionToolbar.add(btnCancelOrder);

        btnRefreshOrders = new JButton("Refresh");
        btnRefreshOrders.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefreshOrders.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAllData();
            }
        });
        actionToolbar.add(btnRefreshOrders);

        tablePanel.add(actionToolbar, BorderLayout.SOUTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        return mainPanel;
    }

    // ==========================================
    // TAB 2: Delivery & Agent Scheduling Panel
    // ==========================================
    private JPanel createDeliverySchedulingTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT ASSIGN FORM
        JPanel assignForm = new JPanel(new GridBagLayout());
        assignForm.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(30, 100, 160), 2), 
                " Assign Agent to Order ", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14), new Color(130, 180, 230)));
        assignForm.setPreferredSize(new Dimension(380, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Select Pending Order
        gbc.gridx = 0; gbc.gridy = 0;
        assignForm.add(createStyledLabel("Select Order:"), gbc);
        gbc.gridx = 1;
        comboPendingOrders = new JComboBox<>();
        comboPendingOrders.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        assignForm.add(comboPendingOrders, gbc);

        // Row 1: Select Available Agent
        gbc.gridx = 0; gbc.gridy = 1;
        assignForm.add(createStyledLabel("Select Agent:"), gbc);
        gbc.gridx = 1;
        comboAvailableAgents = new JComboBox<>();
        comboAvailableAgents.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        assignForm.add(comboAvailableAgents, gbc);

        // Row 2: Assign Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        btnAssignAgent = new JButton("Assign Delivery Agent");
        btnAssignAgent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAssignAgent.setBackground(new Color(30, 100, 160));
        btnAssignAgent.setForeground(Color.WHITE);
        btnAssignAgent.setFocusPainted(false);
        btnAssignAgent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAssignAgent();
            }
        });
        assignForm.add(btnAssignAgent, gbc);

        mainPanel.add(assignForm, BorderLayout.WEST);

        // RIGHT PANEL (Agents Table)
        JPanel agentsListPanel = new JPanel(new BorderLayout(10, 10));
        agentsListPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), 
                " Active Delivery Agents ", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14)));

        String[] columns = {"Agent ID", "Name", "Phone", "Vehicle Type", "Plate Number", "Availability"};
        modelAgents = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableAgents = new JTable(modelAgents);
        tableAgents.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableAgents.setRowHeight(25);
        tableAgents.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scrollPane = new JScrollPane(tableAgents);
        agentsListPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnRefreshAgents = new JButton("Refresh Agents");
        btnRefreshAgents.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefreshAgents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAgentsAndCombos();
            }
        });
        bottomPanel.add(btnRefreshAgents);
        agentsListPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(agentsListPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    // ==========================================
    // TAB 3: Add Delivery Agent Panel
    // ==========================================
    private JPanel createAddAgentTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel innerForm = new JPanel(new GridBagLayout());
        innerForm.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 1), 
                " Register New Delivery Agent ", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14)));
        innerForm.setPreferredSize(new Dimension(500, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Fields
        txtAgentName = new JTextField(20);
        txtAgentPhone = new JTextField(20);
        txtAgentEmail = new JTextField(20);
        txtLicenseNumber = new JTextField(20);
        txtVehicleType = new JTextField(20);
        txtVehiclePlate = new JTextField(20);
        txtVehicleModel = new JTextField(20);

        String[] labelTexts = {"Full Name:", "Phone Number:", "Email Address:", 
                               "License Number:", "Vehicle Type:", "Vehicle Plate:", "Vehicle Model:"};
        JTextField[] fields = {txtAgentName, txtAgentPhone, txtAgentEmail, 
                               txtLicenseNumber, txtVehicleType, txtVehiclePlate, txtVehicleModel};

        for (int i = 0; i < labelTexts.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            innerForm.add(createStyledLabel(labelTexts[i]), gbc);
            gbc.gridx = 1;
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            innerForm.add(fields[i], gbc);
        }

        // Add Agent Button
        gbc.gridx = 0; gbc.gridy = labelTexts.length; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 15, 15, 15);
        btnAddAgent = new JButton("Register Agent");
        btnAddAgent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddAgent.setBackground(new Color(46, 111, 64));
        btnAddAgent.setForeground(Color.WHITE);
        btnAddAgent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddAgent();
            }
        });
        innerForm.add(btnAddAgent, gbc);

        panel.add(innerForm);
        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return label;
    }

    // ==========================================
    // Actions & Event Handling
    // ==========================================
    private void updateCalculations() {
        ProductWrapper selected = (ProductWrapper) comboProducts.getSelectedItem();
        if (selected == null) {
            lblStockAvailable.setText("-");
            lblUnitPrice.setText("Rs. 0.00");
            lblTotalAmount.setText("Rs. 0.00");
            return;
        }

        Product p = selected.getProduct();
        lblStockAvailable.setText(String.valueOf(p.getStockQuantity()) + " " + p.getUnit() + "(s)");
        lblUnitPrice.setText(String.format("Rs. %.2f", p.getPrice()));

        int qty = (Integer) spinQuantity.getValue();
        double total = p.getPrice() * qty;
        lblTotalAmount.setText(String.format("Rs. %.2f", total));
    }

    private void handlePlaceOrder() {
        String clientName = txtClientName.getText().trim();
        if (clientName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter client name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ProductWrapper productWrapper = (ProductWrapper) comboProducts.getSelectedItem();
        if (productWrapper == null) {
            JOptionPane.showMessageDialog(this, "Please select a product.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Product p = productWrapper.getProduct();
        int qty = (Integer) spinQuantity.getValue();

        if (p.getStockQuantity() < qty) {
            JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + p.getStockQuantity(), 
                    "Stock Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CustomerOrder order = orderService.placeOrder(p.getMongoId(), qty, clientName, LocalDate.now());
        if (order != null) {
            JOptionPane.showMessageDialog(this, "Order placed successfully! Total: Rs. " + order.getTotalAmount(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            txtClientName.setText("");
            spinQuantity.setValue(1);
            refreshAllData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to place order. Check logs for details.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAssignAgent() {
        OrderWrapper orderWrapper = (OrderWrapper) comboPendingOrders.getSelectedItem();
        AgentWrapper agentWrapper = (AgentWrapper) comboAvailableAgents.getSelectedItem();

        if (orderWrapper == null) {
            JOptionPane.showMessageDialog(this, "Please select a pending order.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (agentWrapper == null) {
            JOptionPane.showMessageDialog(this, "Please select an available agent.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = orderService.assignDeliveryAgent(orderWrapper.getOrder().getMongoId(), agentWrapper.getAgent().getMongoId());
        if (success) {
            JOptionPane.showMessageDialog(this, "Delivery agent assigned successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAllData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to assign agent.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCompleteDelivery() {
        int selectedRow = tableOrders.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table to complete delivery.", 
                    "Select Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) modelOrders.getValueAt(selectedRow, 0);
        String status = (String) modelOrders.getValueAt(selectedRow, 5);

        if ("DELIVERED".equals(status)) {
            JOptionPane.showMessageDialog(this, "Order is already marked as DELIVERED.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!"PROCESSING".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only orders in PROCESSING state can be completed.", 
                    "Invalid State", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Mark Order #" + orderId + " as delivered?", 
                "Confirm Delivery", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = orderService.completeDelivery(orderId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Delivery marked completed!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to complete delivery.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleCancelOrder() {
        int selectedRow = tableOrders.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table to cancel.", 
                    "Select Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) modelOrders.getValueAt(selectedRow, 0);
        String status = (String) modelOrders.getValueAt(selectedRow, 5);

        if ("CANCELLED".equals(status) || "DELIVERED".equals(status)) {
            JOptionPane.showMessageDialog(this, "Cannot cancel a completed or already cancelled order.", 
                    "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel Order #" + orderId + "?\nStock will be refunded and agent will be released.", 
                "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = orderService.cancelOrder(orderId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Order cancelled and stock refunded!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel order.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAddAgent() {
        String name = txtAgentName.getText().trim();
        String phone = txtAgentPhone.getText().trim();
        String email = txtAgentEmail.getText().trim();
        String license = txtLicenseNumber.getText().trim();
        String vehicleType = txtVehicleType.getText().trim();
        String vehiclePlate = txtVehiclePlate.getText().trim();
        String vehicleModel = txtVehicleModel.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || license.isEmpty() || 
            vehicleType.isEmpty() || vehiclePlate.isEmpty() || vehicleModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all agent details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DeliveryAgent da = new DeliveryAgent();
        da.setName(name);
        da.setPhone(phone);
        da.setEmail(email);
        da.setLicenseNumber(license);
        da.setVehicleType(vehicleType);
        da.setVehiclePlate(vehiclePlate);
        da.setVehicleModel(vehicleModel);
        da.setAvailable(true);

        DeliveryAgent saved = agentRepo.addDeliveryAgent(da);
        if (saved != null) {
            JOptionPane.showMessageDialog(this, "Delivery agent " + name + " registered successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            // Clear fields
            txtAgentName.setText("");
            txtAgentPhone.setText("");
            txtAgentEmail.setText("");
            txtLicenseNumber.setText("");
            txtVehicleType.setText("");
            txtVehiclePlate.setText("");
            txtVehicleModel.setText("");
            
            refreshAllData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to register agent. Check database connection logs.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // Data Loading & Syncing
    // ==========================================
    private void refreshAllData() {
        refreshOrdersTable();
        refreshAgentsAndCombos();
    }

    private void refreshOrdersTable() {
        modelOrders.setRowCount(0);
        try {
            List<CustomerOrder> orders = orderService.getAllOrders();
            for (CustomerOrder o : orders) {
                modelOrders.addRow(new Object[]{
                        o.getMongoId(),
                        o.getCustomerName(),
                        o.getProductName(),
                        o.getQuantityOrdered(),
                        String.format("Rs. %.2f", o.getTotalAmount()),
                        o.getStatus(),
                        o.getDeliveryAgentId() == null ? "None" : o.getDeliveryAgentId()
                });
            }
        } catch (Exception e) {
            System.err.println("Error reloading orders table: " + e.getMessage());
        }
    }

    private void refreshAgentsAndCombos() {
        // Clear models/combos
        modelAgents.setRowCount(0);
        comboProducts.removeAllItems();
        comboPendingOrders.removeAllItems();
        comboAvailableAgents.removeAllItems();

        try {
            // Load products
            List<Product> products = productController.getAllProducts();
            for (Product p : products) {
                comboProducts.addItem(new ProductWrapper(p));
            }
            updateCalculations();

            // Load agents directly from MongoDB to avoid repository interface limitations
            MongoDatabase database = DBManager.getDatabase();
            MongoCollection<Document> agentCollection = database.getCollection("delivery_agents");
            List<DeliveryAgent> agents = new ArrayList<>();
            for (Document doc : agentCollection.find()) {
                DeliveryAgent a = new DeliveryAgent();
                a.setMongoId(doc.getObjectId("_id").toString());
                a.setName(doc.getString("name"));
                a.setPhone(doc.getString("phone"));
                a.setEmail(doc.getString("email"));
                a.setLicenseNumber(doc.getString("license_number"));
                a.setVehicleType(doc.getString("vehicle_type"));
                a.setVehiclePlate(doc.getString("vehicle_plate"));
                a.setVehicleModel(doc.getString("vehicle_model"));
                a.setAvailable(Boolean.TRUE.equals(doc.getBoolean("is_available")));
                agents.add(a);
            }

            for (DeliveryAgent a : agents) {
                modelAgents.addRow(new Object[]{
                        a.getMongoId(),
                        a.getName(),
                        a.getPhone(),
                        a.getVehicleType(),
                        a.getVehiclePlate(),
                        a.isAvailable() ? "Available" : "Busy"
                });

                if (a.isAvailable()) {
                    comboAvailableAgents.addItem(new AgentWrapper(a));
                }
            }

            // Load pending orders for scheduling
            List<CustomerOrder> orders = orderService.getAllOrders();
            for (CustomerOrder o : orders) {
                if (CustomerOrder.STATUS_PENDING.equals(o.getStatus())) {
                    comboPendingOrders.addItem(new OrderWrapper(o));
                }
            }

        } catch (Exception e) {
            System.err.println("Error reloading agents and combo fields: " + e.getMessage());
        }
    }

    // ==========================================
    // ComboBox Wrappers for clean display
    // ==========================================
    private static class ProductWrapper {
        private final Product product;
        public ProductWrapper(Product product) { this.product = product; }
        public Product getProduct() { return product; }
        @Override
        public String toString() {
            return String.format("%s (Rs. %.2f)", product.getName(), product.getPrice());
        }
    }

    private static class AgentWrapper {
        private final DeliveryAgent agent;
        public AgentWrapper(DeliveryAgent agent) { this.agent = agent; }
        public DeliveryAgent getAgent() { return agent; }
        @Override
        public String toString() {
            return String.format("%s - %s (%s)", agent.getName(), agent.getVehicleType(), agent.getVehiclePlate());
        }
    }

    private static class OrderWrapper {
        private final CustomerOrder order;
        public OrderWrapper(CustomerOrder order) { this.order = order; }
        public CustomerOrder getOrder() { return order; }
        @Override
        public String toString() {
            return String.format("#%s: %s (%d x %s)", 
                    order.getMongoId().substring(Math.max(0, order.getMongoId().length() - 6)),
                    order.getCustomerName(), 
                    order.getQuantityOrdered(), 
                    order.getProductName());
        }
    }

    // Main entry point to launch GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OrderManagementGUI().setVisible(true);
            }
        });
    }
}
