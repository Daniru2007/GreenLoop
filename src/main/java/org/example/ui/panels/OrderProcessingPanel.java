package org.example.ui.panels;

import org.example.controller.OrderService;
import org.example.controller.ProductController;
import org.example.model.CustomerOrder;
import org.example.model.Product;
import org.example.ui.UIStyleUtils;
import org.example.ui.wrappers.ProductWrapper;
import org.example.utils.EmailUtils;

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
import java.util.List;

public class OrderProcessingPanel extends JPanel {

    private final OrderService orderService;
    private final ProductController productController;
    private final Runnable refreshCallback;

    private JTextField txtClientName;
    private JTextField txtClientEmail;
    private JTextField txtClientPhone;
    private JTextField txtClientAddress;
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

    public OrderProcessingPanel(OrderService orderService, ProductController productController, Runnable refreshCallback) {
        this.orderService = orderService;
        this.productController = productController;
        this.refreshCallback = refreshCallback;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT FORM PANEL (Place Order)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(46, 111, 64), 2), 
                " Place Client Order ", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14), new Color(120, 220, 150)));
        formPanel.setPreferredSize(new Dimension(380, 620));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Client Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Client Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtClientName = new JTextField();
        txtClientName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtClientName.putClientProperty("JTextField.placeholderText", "Enter client full name...");
        txtClientName.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtClientName, gbc);

        // Row 1: Client Email
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Client Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtClientEmail = new JTextField();
        txtClientEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtClientEmail.putClientProperty("JTextField.placeholderText", "e.g. client@example.com");
        txtClientEmail.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtClientEmail, gbc);

        // Row 2: Client Phone
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Client Phone:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtClientPhone = new JTextField();
        txtClientPhone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtClientPhone.putClientProperty("JTextField.placeholderText", "e.g. 0771234567");
        txtClientPhone.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtClientPhone, gbc);

        // Row 3: Client Address
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Client Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtClientAddress = new JTextField();
        txtClientAddress.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtClientAddress.putClientProperty("JTextField.placeholderText", "e.g. 123 Main St, Colombo");
        txtClientAddress.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtClientAddress, gbc);

        // Row 4: Product Selection
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Select Product:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        comboProducts = new JComboBox<>();
        comboProducts.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboProducts.putClientProperty("JComponent.roundRect", true);
        comboProducts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCalculations();
            }
        });
        formPanel.add(comboProducts, gbc);

        // Row 5: Quantity
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Quantity Ordered:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spinQuantity.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinQuantity.putClientProperty("JComponent.roundRect", true);
        spinQuantity.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateCalculations();
            }
        });
        formPanel.add(spinQuantity, gbc);

        // Row 6: Available Stock
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Available Stock:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        lblStockAvailable = new JLabel("-");
        lblStockAvailable.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblStockAvailable, gbc);

        // Row 7: Unit Price
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Unit Price:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        lblUnitPrice = new JLabel("Rs. 0.00");
        lblUnitPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblUnitPrice, gbc);

        // Row 8: Total Amount
        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Total Amount:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        lblTotalAmount = new JLabel("Rs. 0.00");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalAmount.setForeground(new Color(100, 200, 120));
        formPanel.add(lblTotalAmount, gbc);

        // Row 9: Place Order Button
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(25, 8, 8, 8);
        btnPlaceOrder = new JButton("Place Client Order");
        btnPlaceOrder.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPlaceOrder.setBackground(new Color(46, 111, 64));
        btnPlaceOrder.setForeground(Color.WHITE);
        btnPlaceOrder.setFocusPainted(false);
        btnPlaceOrder.putClientProperty("JButton.buttonType", "roundRect");
        btnPlaceOrder.putClientProperty("JButton.boldText", true);
        btnPlaceOrder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePlaceOrder();
            }
        });
        formPanel.add(btnPlaceOrder, gbc);

        add(formPanel, BorderLayout.WEST);

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
        tableOrders.setRowHeight(28);
        tableOrders.setShowHorizontalLines(true);
        tableOrders.setShowVerticalLines(false);
        tableOrders.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scrollPane = new JScrollPane(tableOrders);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions Toolbar
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
 
        btnCompleteDelivery = new JButton("Complete Delivery");
        btnCompleteDelivery.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCompleteDelivery.setBackground(new Color(30, 100, 160));
        btnCompleteDelivery.setForeground(Color.WHITE);
        btnCompleteDelivery.putClientProperty("JButton.buttonType", "roundRect");
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
        btnCancelOrder.putClientProperty("JButton.buttonType", "roundRect");
        btnCancelOrder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCancelOrder();
            }
        });
        actionToolbar.add(btnCancelOrder);
 
        btnRefreshOrders = new JButton("Refresh");
        btnRefreshOrders.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefreshOrders.putClientProperty("JButton.buttonType", "roundRect");
        btnRefreshOrders.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCallback.run();
            }
        });
        actionToolbar.add(btnRefreshOrders);

        tablePanel.add(actionToolbar, BorderLayout.SOUTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        refreshOrdersTable();
        refreshProductsCombo();
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

    private void refreshProductsCombo() {
        comboProducts.removeAllItems();
        try {
            List<Product> products = productController.getAllProducts();
            for (Product p : products) {
                comboProducts.addItem(new ProductWrapper(p));
            }
            updateCalculations();
        } catch (Exception e) {
            System.err.println("Error reloading products in order processing: " + e.getMessage());
        }
    }

    private void updateCalculations() {
        ProductWrapper selected = (ProductWrapper) comboProducts.getSelectedItem();
        if (selected == null) {
            lblStockAvailable.setText("-");
            lblUnitPrice.setText("Rs. 0.00");
            lblTotalAmount.setText("Rs. 0.00");
            return;
        }

        Product p = selected.getProduct();
        lblStockAvailable.setText(p.getStockQuantity() + " " + p.getUnit() + "(s)");
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

        String clientEmail = txtClientEmail.getText().trim();
        String clientPhone = txtClientPhone.getText().trim();
        String clientAddress = txtClientAddress.getText().trim();

        if (clientEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter client email.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (clientPhone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter client phone number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (clientAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter client address.", "Error", JOptionPane.ERROR_MESSAGE);
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

        CustomerOrder order = orderService.placeOrder(p.getMongoId(), qty, clientName, clientEmail, clientPhone, clientAddress, LocalDate.now());
        if (order != null) {
            JOptionPane.showMessageDialog(this, "Order placed successfully! Total: Rs. " + String.format("%.2f", order.getTotalAmount()), "Success", JOptionPane.INFORMATION_MESSAGE);
            if (EmailUtils.isLastEmailClientSuccess()) {
                JOptionPane.showMessageDialog(this, "Email sent to client successfully!", "Email Sent", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to send email to client!", "Email Error", JOptionPane.ERROR_MESSAGE);
            }
            txtClientName.setText("");
            txtClientEmail.setText("");
            txtClientPhone.setText("");
            txtClientAddress.setText("");
            spinQuantity.setValue(1);
            refreshCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to place order. Check logs for details.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
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
                if (EmailUtils.isLastEmailClientSuccess()) {
                    JOptionPane.showMessageDialog(this, "Email sent to client successfully!", "Email Sent", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to send email to client!", "Email Error", JOptionPane.ERROR_MESSAGE);
                }
                refreshCallback.run();
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
                refreshCallback.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel order.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
