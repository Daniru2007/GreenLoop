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
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProdName = new JTextField();
        txtProdName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdName.putClientProperty("JTextField.placeholderText", "e.g. Biodegradable Plates");
        txtProdName.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdName, gbc);

        // Row 1: Category
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Category:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProdCategory = new JTextField();
        txtProdCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdCategory.putClientProperty("JTextField.placeholderText", "e.g. Plates, Boxes");
        txtProdCategory.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdCategory, gbc);

        // Row 2: Material
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Material:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProdMaterial = new JTextField();
        txtProdMaterial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdMaterial.putClientProperty("JTextField.placeholderText", "e.g. Sugarcane Bagasse");
        txtProdMaterial.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdMaterial, gbc);

        // Row 3: Price
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Unit Price (Rs):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProdPrice = new JTextField();
        txtProdPrice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdPrice.putClientProperty("JTextField.placeholderText", "e.g. 1250.00");
        txtProdPrice.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdPrice, gbc);

        // Row 4: Initial Stock
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Initial Stock:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProdStock = new JTextField();
        txtProdStock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdStock.putClientProperty("JTextField.placeholderText", "e.g. 200");
        txtProdStock.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdStock, gbc);

        // Row 5: Unit
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Unit type:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProdUnit = new JTextField();
        txtProdUnit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdUnit.putClientProperty("JTextField.placeholderText", "e.g. pack, piece, roll");
        txtProdUnit.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdUnit, gbc);

        // Row 6: Supplier
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Supplier Name:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProdSupplier = new JTextField();
        txtProdSupplier.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdSupplier.putClientProperty("JTextField.placeholderText", "e.g. EcoSupply Co");
        txtProdSupplier.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdSupplier, gbc);

        // Row 7: Reorder Level
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Reorder Level:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProdReorderLevel = new JTextField();
        txtProdReorderLevel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProdReorderLevel.putClientProperty("JTextField.placeholderText", "e.g. 30");
        txtProdReorderLevel.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtProdReorderLevel, gbc);

        // Row 8: Add Product Button
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
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
        tableProducts.getColumnModel().getColumn(5)
                .setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (value != null) {
                            try {
                                int stock = Integer.parseInt(value.toString());
                                Object reorderVal = table.getValueAt(row, 8);
                                int reorderLevel = reorderVal != null ? Integer.parseInt(reorderVal.toString()) : 0;
                                if (stock == 0) {
                                    c.setForeground(Color.RED);
                                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                                } else if (stock <= reorderLevel) {
                                    c.setForeground(Color.ORANGE);
                                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                                } else {
                                    c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                                }
                            } catch (NumberFormatException ex) {
                                c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                                c.setFont(c.getFont().deriveFont(Font.PLAIN));
                            }
                        }
                        return c;
                    }
                });

        tableProducts.getColumnModel().getColumn(9)
                .setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (value != null) {
                            String status = value.toString();
                            if (status.contains("Out of Stock")) {
                                c.setForeground(Color.RED);
                                c.setFont(c.getFont().deriveFont(Font.BOLD));
                            } else if (status.contains("Low Stock")) {
                                c.setForeground(Color.ORANGE);
                                c.setFont(c.getFont().deriveFont(Font.BOLD));
                            } else if (status.contains("Moderate Stock")) {
                                c.setForeground(Color.YELLOW);
                                c.setFont(c.getFont().deriveFont(Font.BOLD));
                            } else {
                                c.setForeground(new Color(0, 180, 0));
                                c.setFont(c.getFont().deriveFont(Font.PLAIN));
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


        JButton btnUpdateProduct = new JButton("Update Product");
        btnUpdateProduct.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpdateProduct.setBackground(new Color(30, 90, 160));
        btnUpdateProduct.setForeground(Color.WHITE);
        btnUpdateProduct.putClientProperty("JButton.buttonType", "roundRect");
        btnUpdateProduct.addActionListener(e -> handleUpdateProduct());
        actionToolbar.add(btnUpdateProduct);


        JButton btnStockIn = new JButton("Stock-In");
        btnStockIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnStockIn.setBackground(new Color(46, 111, 64));
        btnStockIn.setForeground(Color.WHITE);
        btnStockIn.putClientProperty("JButton.buttonType", "roundRect");
        btnStockIn.addActionListener(e -> handleStockIn());
        actionToolbar.add(btnStockIn);

        JButton btnAdjustStock = new JButton("Adjust Stock");
        btnAdjustStock.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdjustStock.setBackground(new Color(212, 136, 32));
        btnAdjustStock.setForeground(Color.WHITE);
        btnAdjustStock.putClientProperty("JButton.buttonType", "roundRect");
        btnAdjustStock.addActionListener(e -> handleAdjustStock());
        actionToolbar.add(btnAdjustStock);

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

    private String getStr(Object val) {
        return val == null ? "" : val.toString();
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

    private void handleUpdateProduct() {
        int selectedRow = tableProducts.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to update.", "Select Product", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = modelProducts.getValueAt(selectedRow, 0).toString();
        String currentName = getStr(modelProducts.getValueAt(selectedRow, 1));
        String currentCategory = getStr(modelProducts.getValueAt(selectedRow, 2));
        String currentMaterial = getStr(modelProducts.getValueAt(selectedRow, 3));
        
        String currentPrice = getStr(modelProducts.getValueAt(selectedRow, 4));
        currentPrice = currentPrice.replace("Rs.", "").replace("Rs. ", "").trim();
        
        String currentStock = getStr(modelProducts.getValueAt(selectedRow, 5));
        String currentUnit = getStr(modelProducts.getValueAt(selectedRow, 6));
        String currentSupplier = getStr(modelProducts.getValueAt(selectedRow, 7));
        String currentReorder = getStr(modelProducts.getValueAt(selectedRow, 8));

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Product Details", true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Product Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtName = new JTextField(currentName);
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtName.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtName, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtCategory = new JTextField(currentCategory);
        txtCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCategory.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtCategory, gbc);

        // Material
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Material:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtMaterial = new JTextField(currentMaterial);
        txtMaterial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMaterial.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtMaterial, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Unit Price (Rs):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtPrice = new JTextField(currentPrice);
        txtPrice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPrice.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtPrice, gbc);

        // Stock
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtStock = new JTextField(currentStock);
        txtStock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtStock.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtStock, gbc);

        // Unit
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Unit type:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtUnit = new JTextField(currentUnit);
        txtUnit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUnit.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtUnit, gbc);

        // Supplier
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Supplier Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtSupplier = new JTextField(currentSupplier);
        txtSupplier.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSupplier.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtSupplier, gbc);

        // Reorder Level
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.0;
        formPanel.add(UIStyleUtils.createStyledLabel("Reorder Level:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtReorder = new JTextField(currentReorder);
        txtReorder.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtReorder.putClientProperty("JComponent.roundRect", true);
        formPanel.add(txtReorder, gbc);

        // Submit Button
        JButton btnSubmit = new JButton("Save Changes");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.setBackground(new Color(30, 90, 160));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.putClientProperty("JButton.buttonType", "roundRect");
        btnSubmit.addActionListener(e -> {
            String name = txtName.getText().trim();
            String category = txtCategory.getText().trim();
            String material = txtMaterial.getText().trim();
            String priceStr = txtPrice.getText().trim();
            String stockStr = txtStock.getText().trim();
            String unit = txtUnit.getText().trim();
            String supplier = txtSupplier.getText().trim();
            String reorderStr = txtReorder.getText().trim();

            if (name.isEmpty() || category.isEmpty() || material.isEmpty() ||
                    priceStr.isEmpty() || stockStr.isEmpty() || unit.isEmpty() ||
                    supplier.isEmpty() || reorderStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                int reorder = Integer.parseInt(reorderStr);

                if (stock < 0 || reorder < 0) {
                    JOptionPane.showMessageDialog(dialog, "Stock and reorder level cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Product updated = productController.updateProduct(
                        productId, name, category, material, price, stock, unit, supplier, reorder
                );

                if (updated != null) {
                    JOptionPane.showMessageDialog(dialog, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshCallback.run();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Update failed. Check database logs.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Price, Stock, and Reorder Level must be valid numbers.", "Format Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Cancel Button
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.putClientProperty("JButton.buttonType", "roundRect");
        btnCancel.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSubmit);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleStockIn() {
        int selectedRow = tableProducts.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to perform stock-in.", "Select Product", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = modelProducts.getValueAt(selectedRow, 0).toString();
        String prodName = modelProducts.getValueAt(selectedRow, 1).toString();
        String defaultSupplier = modelProducts.getValueAt(selectedRow, 7).toString();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Receive Stock (Stock-In)", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Product Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panel.add(UIStyleUtils.createStyledLabel("Product:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtName = new JTextField(prodName);
        txtName.setEditable(false);
        txtName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtName.putClientProperty("JComponent.roundRect", true);
        panel.add(txtName, gbc);

        // Supplier
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panel.add(UIStyleUtils.createStyledLabel("Supplier:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtSupplier = new JTextField(defaultSupplier);
        txtSupplier.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSupplier.putClientProperty("JComponent.roundRect", true);
        panel.add(txtSupplier, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panel.add(UIStyleUtils.createStyledLabel("Quantity:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtQty = new JTextField();
        txtQty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtQty.putClientProperty("JComponent.roundRect", true);
        txtQty.putClientProperty("JTextField.placeholderText", "e.g. 50");
        panel.add(txtQty, gbc);

        // Cost Per Unit
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        panel.add(UIStyleUtils.createStyledLabel("Cost per Unit (Rs):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtCost = new JTextField();
        txtCost.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCost.putClientProperty("JComponent.roundRect", true);
        txtCost.putClientProperty("JTextField.placeholderText", "e.g. 850.00");
        panel.add(txtCost, gbc);

        // Submit Button
        JButton btnSubmit = new JButton("Confirm Stock-In");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.setBackground(new Color(46, 111, 64));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.putClientProperty("JButton.buttonType", "roundRect");
        btnSubmit.addActionListener(e -> {
            String qtyStr = txtQty.getText().trim();
            String costStr = txtCost.getText().trim();
            String supplier = txtSupplier.getText().trim();

            if (qtyStr.isEmpty() || costStr.isEmpty() || supplier.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int qty = Integer.parseInt(qtyStr);
                double cost = Double.parseDouble(costStr);

                if (qty <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (cost < 0) {
                    JOptionPane.showMessageDialog(dialog, "Cost per unit cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success = productController.receiveStockFromSupplier(productId, qty, supplier, cost);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Stock received and updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshCallback.run();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to receive stock. Check logs.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Quantity and Cost must be valid numbers.", "Format Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Cancel Button
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.putClientProperty("JButton.buttonType", "roundRect");
        btnCancel.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSubmit);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleAdjustStock() {
        int selectedRow = tableProducts.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to adjust stock.", "Select Product", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = modelProducts.getValueAt(selectedRow, 0).toString();
        String prodName = modelProducts.getValueAt(selectedRow, 1).toString();
        String currentStockStr = modelProducts.getValueAt(selectedRow, 5).toString();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Adjust Stock Quantity", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Product Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panel.add(UIStyleUtils.createStyledLabel("Product:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtName = new JTextField(prodName);
        txtName.setEditable(false);
        txtName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtName.putClientProperty("JComponent.roundRect", true);
        panel.add(txtName, gbc);

        // Current Stock
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panel.add(UIStyleUtils.createStyledLabel("Current Stock:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtCurrentStock = new JTextField(currentStockStr);
        txtCurrentStock.setEditable(false);
        txtCurrentStock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCurrentStock.putClientProperty("JComponent.roundRect", true);
        panel.add(txtCurrentStock, gbc);

        // Adjustment Quantity
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panel.add(UIStyleUtils.createStyledLabel("Adjustment (+/-):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtAdjustment = new JTextField();
        txtAdjustment.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAdjustment.putClientProperty("JComponent.roundRect", true);
        txtAdjustment.putClientProperty("JTextField.placeholderText", "e.g. -5 or 10");
        panel.add(txtAdjustment, gbc);

        // Reason
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        panel.add(UIStyleUtils.createStyledLabel("Reason:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtReason = new JTextField();
        txtReason.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtReason.putClientProperty("JComponent.roundRect", true);
        txtReason.putClientProperty("JTextField.placeholderText", "e.g. Damaged during logistics");
        panel.add(txtReason, gbc);

        // Submit Button
        JButton btnSubmit = new JButton("Confirm Adjustment");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.setBackground(new Color(212, 136, 32));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.putClientProperty("JButton.buttonType", "roundRect");
        btnSubmit.addActionListener(e -> {
            String adjustStr = txtAdjustment.getText().trim();
            String reason = txtReason.getText().trim();

            if (adjustStr.isEmpty() || reason.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int adjustment = Integer.parseInt(adjustStr);
                int currentStock = Integer.parseInt(currentStockStr);

                if (currentStock + adjustment < 0) {
                    JOptionPane.showMessageDialog(dialog, "Total stock cannot become negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success = productController.adjustInventory(productId, adjustment, reason);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Stock adjusted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshCallback.run();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to adjust stock. Check logs.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Adjustment must be a valid integer.", "Format Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Cancel Button
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.putClientProperty("JButton.buttonType", "roundRect");
        btnCancel.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSubmit);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}

