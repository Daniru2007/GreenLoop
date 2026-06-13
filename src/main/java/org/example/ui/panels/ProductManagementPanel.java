package org.example.ui.panels;
import javax.swing.table.DefaultTableCellRenderer;
import org.example.controller.ProductController;
import org.example.model.Product;
import org.example.ui.UIStyleUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ProductManagementPanel extends JPanel {

    private final ProductController productController;
    private final Runnable refreshCallback;

    private JTextField txtProdName;
    private JTextField txtProdCategory;
    private JTextField txtProdMaterial;
    private JTextField txtProdPrice;
    private JTextField txtProdStock;
    private JTextField txtProdUnit;
    private JTextField txtProdSupplier;
    private JTextField txtProdReorderLevel;

    private JTable tableProducts;
    private DefaultTableModel modelProducts;

    public ProductManagementPanel(ProductController productController, Runnable refreshCallback) {
        this.productController = productController;
        this.refreshCallback = refreshCallback;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT FORM PANEL (Add Product)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(46, 111, 64), 2), 
                " Register New Product ", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14), new Color(120, 220, 150)));
        formPanel.setPreferredSize(new Dimension(380, 620));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Product Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtProdName = new JTextField();
        txtProdName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdName.putClientProperty("JTextField.placeholderText", "e.g. Biodegradable Plates");
        txtProdName.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdName, gbc);

        // Row 1: Category
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtProdCategory = new JTextField();
        txtProdCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdCategory.putClientProperty("JTextField.placeholderText", "e.g. Plates, Boxes");
        txtProdCategory.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdCategory, gbc);

        // Row 2: Material
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Material:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtProdMaterial = new JTextField();
        txtProdMaterial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdMaterial.putClientProperty("JTextField.placeholderText", "e.g. Sugarcane Bagasse");
        txtProdMaterial.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdMaterial, gbc);

        // Row 3: Price
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Unit Price (Rs):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtProdPrice = new JTextField();
        txtProdPrice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdPrice.putClientProperty("JTextField.placeholderText", "e.g. 1250.00");
        txtProdPrice.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdPrice, gbc);

        // Row 4: Initial Stock
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Initial Stock:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtProdStock = new JTextField();
        txtProdStock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdStock.putClientProperty("JTextField.placeholderText", "e.g. 200");
        txtProdStock.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdStock, gbc);

        // Row 5: Unit
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Unit type:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtProdUnit = new JTextField();
        txtProdUnit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdUnit.putClientProperty("JTextField.placeholderText", "e.g. pack, piece, roll");
        txtProdUnit.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdUnit, gbc);

        // Row 6: Supplier
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Supplier Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtProdSupplier = new JTextField();
        txtProdSupplier.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdSupplier.putClientProperty("JTextField.placeholderText", "e.g. EcoSupply Co");
        txtProdSupplier.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdSupplier, gbc);

        // Row 7: Reorder Level
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Reorder Level:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtProdReorderLevel = new JTextField();
        txtProdReorderLevel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdReorderLevel.putClientProperty("JTextField.placeholderText", "e.g. 30");
        txtProdReorderLevel.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdReorderLevel, gbc);

        // Row 8: Add Product Button
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 8, 8, 8);
        JButton btnAddProduct = new JButton("Register Product");
        btnAddProduct.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddProduct.setBackground(new Color(46, 111, 64));
        btnAddProduct.setForeground(Color.WHITE);
        btnAddProduct.putClientProperty("JButton.buttonType", "roundRect");
        btnAddProduct.putClientProperty("JButton.boldText", true);
        btnAddProduct.addActionListener(e -> handleAddProduct());
        formPanel.add(btnAddProduct, gbc);

        add(formPanel, BorderLayout.WEST);

        // RIGHT PANEL (Product Table)
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), 
                " Product Inventory ", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14)));

        String[] columns = {"Product ID", "Name", "Category", "Material", "Price", "Stock", "Unit", "Supplier", "Reorder Level","Stock Status"};
        modelProducts = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableProducts = new JTable(modelProducts);
        tableProducts.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableProducts.setRowHeight(28);
        tableProducts.setShowHorizontalLines(true);
        tableProducts.setShowVerticalLines(false);
        tableProducts.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableProducts.getColumnModel().getColumn(9)
                .setCellRenderer(new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {

                        Component c = super.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column);

                        if (value != null) {
                            String status = value.toString();

                            if (status.contains("Out of Stock")) {
                                c.setForeground(Color.RED);
                            }
                            else if (status.contains("Low Stock")) {
                                c.setForeground(Color.ORANGE);
                            }
                            else if (status.contains("Moderate Stock")) {
                                c.setForeground(Color.YELLOW);
                            }
                            else {
                                c.setForeground(new Color(0, 180, 0));
                            }
                        }

                        return c;
                    }
                });
        JScrollPane scrollPane = new JScrollPane(tableProducts);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton btnDeleteProduct = new JButton("Delete Product");
        btnDeleteProduct.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDeleteProduct.setBackground(new Color(160, 50, 50));
        btnDeleteProduct.setForeground(Color.WHITE);
        btnDeleteProduct.putClientProperty("JButton.buttonType", "roundRect");
        btnDeleteProduct.addActionListener(e -> handleDeleteProduct());
        actionToolbar.add(btnDeleteProduct);

        JButton btnRefreshProducts = new JButton("Refresh");
        btnRefreshProducts.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefreshProducts.putClientProperty("JButton.buttonType", "roundRect");
        btnRefreshProducts.addActionListener(e -> refreshCallback.run());
        actionToolbar.add(btnRefreshProducts);

        tablePanel.add(actionToolbar, BorderLayout.SOUTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        refreshProductsTable();
    }

    private void refreshProductsTable() {
        modelProducts.setRowCount(0);
        try {
            List<Product> products = productController.getAllProducts();
            for (Product p : products) {
                int stock = p.getStockQuantity();
                int reorderLevel = p.getReorderLevel();

                String stockStatus;

                if (stock == 0) {
                    stockStatus = "Out of Stock";
                }
                else if (stock <= reorderLevel) {
                    stockStatus = "Low Stock (RL: " + reorderLevel + ")";
                }
                else if (stock <= reorderLevel * 2) {
                    stockStatus = "Moderate Stock";
                }
                else {
                    stockStatus = "In Stock";
                }
                modelProducts.addRow(new Object[]{
                        p.getMongoId(),
                        p.getName(),
                        p.getCategory(),
                        p.getMaterial(),
                        String.format("Rs. %.2f", p.getPrice()),
                        p.getStockQuantity(),
                        p.getUnit(),
                        p.getSupplierName(),
                        p.getReorderLevel(),
                        stockStatus
                });
            }
        } catch (Exception e) {
            System.err.println("Error reloading products table: " + e.getMessage());
        }
    }

    private void handleAddProduct() {
        String name = txtProdName.getText().trim();
        String category = txtProdCategory.getText().trim();
        String material = txtProdMaterial.getText().trim();
        String priceStr = txtProdPrice.getText().trim();
        String stockStr = txtProdStock.getText().trim();
        String unit = txtProdUnit.getText().trim();
        String supplier = txtProdSupplier.getText().trim();
        String reorderStr = txtProdReorderLevel.getText().trim();

        if (name.isEmpty() || category.isEmpty() || material.isEmpty() || priceStr.isEmpty() || 
            stockStr.isEmpty() || unit.isEmpty() || supplier.isEmpty() || reorderStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all product details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);
            int reorder = Integer.parseInt(reorderStr);

            Product saved = productController.addProduct(name, category, material, price, stock, unit, supplier, reorder);
            if (saved != null) {
                JOptionPane.showMessageDialog(this, "Product registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Clear fields
                txtProdName.setText("");
                txtProdCategory.setText("");
                txtProdMaterial.setText("");
                txtProdPrice.setText("");
                txtProdStock.setText("");
                txtProdUnit.setText("");
                txtProdSupplier.setText("");
                txtProdReorderLevel.setText("");

                refreshCallback.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register product. Check logs for details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price, stock, and reorder level must be valid numbers.", "Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteProduct() {
        int selectedRow = tableProducts.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to delete.", "Select Product", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = (String) modelProducts.getValueAt(selectedRow, 0);
        String name = (String) modelProducts.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Product: " + name + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = productController.deleteProduct(productId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshCallback.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
