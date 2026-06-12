package org.example.ui.panels;

import org.example.controller.OrderService;
import org.example.controller.ProductController;
import org.example.model.CustomerOrder;
import org.example.model.Product;
import org.example.ui.UIStyleUtils;
import org.example.ui.wrappers.ProductWrapper;
import org.example.utils.EmailUtils;
import org.example.model.OrderItem;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

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
    private JTable tableCart;
    private DefaultTableModel modelCart;
    private JLabel lblCartTotal;

    private JTable tableOrders;
    private DefaultTableModel modelOrders;

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
        comboProducts.addActionListener(e -> updateCalculations());
        formPanel.add(comboProducts, gbc);

        // Row 5: Quantity
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Quantity Ordered:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spinQuantity.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinQuantity.putClientProperty("JComponent.roundRect", true);
        spinQuantity.addChangeListener(e -> updateCalculations());
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

        // Row 8: Item Total (Total Amount)
        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Item Total:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        lblTotalAmount = new JLabel("Rs. 0.00");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalAmount.setForeground(new Color(100, 200, 120));
        formPanel.add(lblTotalAmount, gbc);

        // Row 9: Add to Cart Button
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 8, 10, 8);
        JButton btnAddToCart = new JButton("Add to Cart");
        btnAddToCart.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddToCart.setBackground(new Color(40, 140, 80));
        btnAddToCart.setForeground(Color.WHITE);
        btnAddToCart.setFocusPainted(false);
        btnAddToCart.putClientProperty("JButton.buttonType", "roundRect");
        btnAddToCart.addActionListener(e -> handleAddToCart());
        formPanel.add(btnAddToCart, gbc);

        // Row 10: Cart Table
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 8, 5, 8);
        String[] cartColumns = {"Product ID", "Product Name", "Qty", "Unit Price", "Total"};
        modelCart = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableCart = new JTable(modelCart);
        tableCart.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCart.setRowHeight(22);
        tableCart.setShowHorizontalLines(true);
        tableCart.setShowVerticalLines(false);
        JScrollPane scrollCart = new JScrollPane(tableCart);
        scrollCart.setPreferredSize(new Dimension(340, 150));
        formPanel.add(scrollCart, gbc);

        // Row 11: Cart Controls Panel
        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 5, 8);
        JPanel cartControlPanel = new JPanel(new BorderLayout(5, 5));
        cartControlPanel.setOpaque(false);

        JButton btnRemoveFromCart = new JButton("Remove Selected");
        btnRemoveFromCart.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRemoveFromCart.setBackground(new Color(160, 50, 50));
        btnRemoveFromCart.setForeground(Color.WHITE);
        btnRemoveFromCart.putClientProperty("JButton.buttonType", "roundRect");
        btnRemoveFromCart.addActionListener(e -> handleRemoveFromCart());
        cartControlPanel.add(btnRemoveFromCart, BorderLayout.WEST);

        lblCartTotal = new JLabel("Order Total: Rs. 0.00");
        lblCartTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCartTotal.setForeground(new Color(120, 220, 150));
        cartControlPanel.add(lblCartTotal, BorderLayout.EAST);

        formPanel.add(cartControlPanel, gbc);

        // Row 12: Place Order Button
        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 8, 8, 8);
        JButton btnPlaceOrder = new JButton("Place Client Order");
        btnPlaceOrder.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPlaceOrder.setBackground(new Color(46, 111, 64));
        btnPlaceOrder.setForeground(Color.WHITE);
        btnPlaceOrder.setFocusPainted(false);
        btnPlaceOrder.putClientProperty("JButton.buttonType", "roundRect");
        btnPlaceOrder.putClientProperty("JButton.boldText", true);
        btnPlaceOrder.addActionListener(e -> handlePlaceOrder());
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
        tableOrders = new JTable(modelOrders) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row >= 0) {
                    return "Double-click to view order details and products";
                }
                return super.getToolTipText(e);
            }
        };
        tableOrders.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableOrders.setRowHeight(28);
        tableOrders.setShowHorizontalLines(true);
        tableOrders.setShowVerticalLines(false);
        tableOrders.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Click listener on any column/row to show order items dialog on double-click
        tableOrders.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableOrders.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        int modelRow = tableOrders.convertRowIndexToModel(row);
                        String orderId = (String) modelOrders.getValueAt(modelRow, 0);
                        showOrderItemsDialog(orderId);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableOrders);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions Toolbar
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
 
        JButton btnCompleteDelivery = new JButton("Complete Delivery");
        btnCompleteDelivery.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCompleteDelivery.setBackground(new Color(30, 100, 160));
        btnCompleteDelivery.setForeground(Color.WHITE);
        btnCompleteDelivery.putClientProperty("JButton.buttonType", "roundRect");
        btnCompleteDelivery.addActionListener(e -> handleCompleteDelivery());
        actionToolbar.add(btnCompleteDelivery);
 
        JButton btnCancelOrder = new JButton("Cancel Order");
        btnCancelOrder.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelOrder.setBackground(new Color(160, 50, 50));
        btnCancelOrder.setForeground(Color.WHITE);
        btnCancelOrder.putClientProperty("JButton.buttonType", "roundRect");
        btnCancelOrder.addActionListener(e -> handleCancelOrder());
        actionToolbar.add(btnCancelOrder);
 
        JButton btnRefreshOrders = new JButton("Refresh");
        btnRefreshOrders.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefreshOrders.putClientProperty("JButton.buttonType", "roundRect");
        btnRefreshOrders.addActionListener(e -> refreshCallback.run());
        actionToolbar.add(btnRefreshOrders);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JLabel lblHint = new JLabel("💡 Double-click a row to view full order details");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(Color.GRAY);
        lblHint.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        bottomPanel.add(lblHint, BorderLayout.WEST);

        bottomPanel.add(actionToolbar, BorderLayout.EAST);

        tablePanel.add(bottomPanel, BorderLayout.SOUTH);
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
                StringBuilder prodSummary = new StringBuilder();
                int totalQty = 0;
                if (o.getOrderItems() != null) {
                    for (int i = 0; i < o.getOrderItems().size(); i++) {
                        org.example.model.OrderItem item = o.getOrderItems().get(i);
                        if (i > 0) prodSummary.append(", ");
                        prodSummary.append(item.getProductName()).append(" (x").append(item.getQuantity()).append(")");
                        totalQty += item.getQuantity();
                    }
                }
                modelOrders.addRow(new Object[]{
                        o.getMongoId(),
                        o.getCustomerName(),
                        prodSummary.toString(),
                        totalQty,
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

        if (modelCart.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add at least one product to the order.", "Cart Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<org.example.model.OrderItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < modelCart.getRowCount(); i++) {
            String pId = (String) modelCart.getValueAt(i, 0);
            String pName = (String) modelCart.getValueAt(i, 1);
            int qty = (Integer) modelCart.getValueAt(i, 2);
            items.add(new org.example.model.OrderItem(pId, pName, qty, 0.0));
        }

        CustomerOrder order = orderService.placeOrder(items, clientName, clientEmail, clientPhone, clientAddress, LocalDate.now());
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
            modelCart.setRowCount(0);
            updateCartTotal();
            refreshCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to place order. Check logs for details.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddToCart() {
        ProductWrapper productWrapper = (ProductWrapper) comboProducts.getSelectedItem();
        if (productWrapper == null) {
            JOptionPane.showMessageDialog(this, "Please select a product.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Product p = productWrapper.getProduct();
        int qty = (Integer) spinQuantity.getValue();
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if item already exists in the cart
        int existingRow = -1;
        for (int i = 0; i < modelCart.getRowCount(); i++) {
            if (p.getMongoId().equals(modelCart.getValueAt(i, 0))) {
                existingRow = i;
                break;
            }
        }

        int totalQty = qty;
        if (existingRow != -1) {
            totalQty += (Integer) modelCart.getValueAt(existingRow, 2);
        }

        if (p.getStockQuantity() < totalQty) {
            JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + p.getStockQuantity() + 
                    (existingRow != -1 ? " (currently in cart: " + modelCart.getValueAt(existingRow, 2) + ")" : ""), 
                    "Stock Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double unitPrice = p.getPrice();
        double total = unitPrice * totalQty;

        if (existingRow != -1) {
            modelCart.setValueAt(totalQty, existingRow, 2);
            modelCart.setValueAt(String.format("Rs. %.2f", total), existingRow, 4);
        } else {
            modelCart.addRow(new Object[]{
                    p.getMongoId(),
                    p.getName(),
                    totalQty,
                    String.format("Rs. %.2f", unitPrice),
                    String.format("Rs. %.2f", total)
            });
        }

        updateCartTotal();
        spinQuantity.setValue(1);
    }

    private void handleRemoveFromCart() {
        int selectedRow = tableCart.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item from the cart to remove.", 
                    "Select Item", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modelCart.removeRow(selectedRow);
        updateCartTotal();
    }

    private void updateCartTotal() {
        double total = 0.0;
        for (int i = 0; i < modelCart.getRowCount(); i++) {
            String totalStr = (String) modelCart.getValueAt(i, 4);
            try {
                String cleanStr = totalStr.replace("Rs. ", "").trim();
                total += Double.parseDouble(cleanStr);
            } catch (Exception e) {
                // Ignore
            }
        }
        lblCartTotal.setText(String.format("Order Total: Rs. %.2f", total));
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

    private void showOrderItemsDialog(String orderId) {
        CustomerOrder order = orderService.getOrderById(orderId);
        if (order == null) {
            JOptionPane.showMessageDialog(this, "Could not retrieve order details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog;
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, "Order Details - #" + orderId, true);
        } else {
            dialog = new JDialog((Dialog) parentWindow, "Order Details - #" + orderId, true);
        }
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(650, 400);
        dialog.setLocationRelativeTo(this);

        // Header panel
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 65, 70)),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        headerPanel.setBackground(new Color(30, 33, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblOrder = new JLabel("Order: #" + orderId);
        lblOrder.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblOrder.setForeground(new Color(120, 220, 150));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        headerPanel.add(lblOrder, gbc);

        JLabel lblClientText = new JLabel("Client: ");
        lblClientText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblClientText.setForeground(Color.LIGHT_GRAY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        headerPanel.add(lblClientText, gbc);

        JLabel lblClientVal = new JLabel(order.getCustomerName() != null ? order.getCustomerName() : "N/A");
        lblClientVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblClientVal.setForeground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = 1;
        headerPanel.add(lblClientVal, gbc);

        JLabel lblStatusText = new JLabel("Status: ");
        lblStatusText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatusText.setForeground(Color.LIGHT_GRAY);
        gbc.gridx = 0; gbc.gridy = 2;
        headerPanel.add(lblStatusText, gbc);

        JLabel lblStatusVal = new JLabel(order.getStatus());
        lblStatusVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        if ("DELIVERED".equals(order.getStatus())) {
            lblStatusVal.setForeground(new Color(46, 204, 113));
        } else if ("CANCELLED".equals(order.getStatus())) {
            lblStatusVal.setForeground(new Color(231, 76, 60));
        } else if ("PROCESSING".equals(order.getStatus())) {
            lblStatusVal.setForeground(new Color(52, 152, 219));
        } else {
            lblStatusVal.setForeground(new Color(241, 196, 15));
        }
        gbc.gridx = 1; gbc.gridy = 2;
        headerPanel.add(lblStatusVal, gbc);

        dialog.add(headerPanel, BorderLayout.NORTH);

        // Table panel
        String[] cols = {"Product ID", "Product Name", "Quantity", "Unit Price", "Total Price"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                double total = item.getQuantity() * item.getUnitPrice();
                model.addRow(new Object[]{
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        String.format("Rs. %.2f", item.getUnitPrice()),
                        String.format("Rs. %.2f", total)
                });
            }
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(25);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        dialog.add(scroll, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JLabel lblTotal = new JLabel(String.format("Total Amount: Rs. %.2f", order.getTotalAmount()));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotal.setForeground(new Color(120, 220, 150));
        footerPanel.add(lblTotal, BorderLayout.WEST);

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.putClientProperty("JButton.buttonType", "roundRect");
        btnClose.addActionListener(evt -> dialog.dispose());
        footerPanel.add(btnClose, BorderLayout.EAST);

        dialog.add(footerPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
