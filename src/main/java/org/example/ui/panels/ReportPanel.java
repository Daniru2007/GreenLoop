package org.example.ui.panels;

import org.example.controller.ReportController;
import org.example.model.CustomerOrder;
import org.example.model.Product;
import org.example.ui.UIStyleUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class ReportPanel extends JPanel {

    private final ReportController reportController;
    private final Runnable refreshCallback;

    // Controls
    private JComboBox<String> cmbMonth;
    private JSpinner spinnerYear;

    // Tables
    private JTable tableOrders;
    private DefaultTableModel modelOrders;
    private JTable tableLowStock;
    private DefaultTableModel modelLowStock;
    private JTable tableTopSellers;
    private DefaultTableModel modelTopSellers;

    // Summary labels
    private JLabel lblTotalOrders;
    private JLabel lblTotalRevenue;
    private JLabel lblLowStockCount;

    public ReportPanel(ReportController reportController, Runnable refreshCallback) {
        this.reportController = reportController;
        this.refreshCallback  = refreshCallback;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createControlPanel(), BorderLayout.NORTH);
        add(createMainPanel(),    BorderLayout.CENTER);
    }

    // -- Control Panel (top bar) -----------------------------------------------

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 111, 64), 2, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        // Month selector
        panel.add(UIStyleUtils.createStyledLabel("Month:"));
        String[] months = new String[12];
        for (int i = 1; i <= 12; i++) {
            months[i - 1] = Month.of(i).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        }
        cmbMonth = new JComboBox<>(months);
        cmbMonth.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbMonth.setSelectedIndex(java.time.LocalDate.now().getMonthValue() - 1);
        panel.add(cmbMonth);

        // Year selector
        panel.add(UIStyleUtils.createStyledLabel("Year:"));
        spinnerYear = new JSpinner(new SpinnerNumberModel(
                java.time.LocalDate.now().getYear(), 2000, 2100, 1
        ));
        spinnerYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinnerYear.setPreferredSize(new Dimension(80, 30));
        panel.add(spinnerYear);

        // Load Report button
        JButton btnLoad = createButton("Load Report", new Color(46, 111, 64));
        btnLoad.addActionListener(e -> refreshData());
        panel.add(btnLoad);

        // Generate PDF button
        JButton btnPDF = createButton("Generate PDF", new Color(52, 120, 180));
        btnPDF.addActionListener(e -> handleGeneratePDF());
        panel.add(btnPDF);

        // Summary cards (right side)
        panel.add(Box.createHorizontalStrut(20));

        lblTotalOrders  = new JLabel("0");
        lblTotalRevenue = new JLabel("Rs. 0.00");
        lblLowStockCount = new JLabel("0");

        panel.add(createSummaryCard("DELIVERED ORDERS", lblTotalOrders,  new Color(46, 204, 113)));
        panel.add(createSummaryCard("TOTAL REVENUE",    lblTotalRevenue, new Color(52, 152, 219)));
        panel.add(createSummaryCard("LOW STOCK ITEMS",  lblLowStockCount, new Color(231, 76, 60)));

        return panel;
    }

    // -- Main Panel (3 tables) -------------------------------------------------

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));

        // -- Delivered Orders Table --------------------------------------------
        JPanel ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(46, 111, 64), 1),
                " Delivered Orders ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(120, 220, 150)
        ));

        modelOrders = new DefaultTableModel(
                new String[]{"Product", "Customer", "Qty", "Unit Price", "Total", "Date"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableOrders = createStyledTable(modelOrders);
        ordersPanel.add(new JScrollPane(tableOrders), BorderLayout.CENTER);
        panel.add(ordersPanel);

        // -- Top Sellers Table -------------------------------------------------
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(52, 120, 180), 1),
                " Top 5 Selling Products ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(100, 180, 255)
        ));

        modelTopSellers = new DefaultTableModel(
                new String[]{"Rank", "Product", "Total Qty", "Revenue"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableTopSellers = createStyledTable(modelTopSellers);
        topPanel.add(new JScrollPane(tableTopSellers), BorderLayout.CENTER);
        panel.add(topPanel);

        // -- Low Stock Table ---------------------------------------------------
        JPanel stockPanel = new JPanel(new BorderLayout());
        stockPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(231, 76, 60), 1),
                " Low Stock Alerts ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(255, 120, 100)
        ));

        modelLowStock = new DefaultTableModel(
                new String[]{"Product", "Category", "Stock", "Reorder Level", "Supplier"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableLowStock = createStyledTable(modelLowStock);
        stockPanel.add(new JScrollPane(tableLowStock), BorderLayout.CENTER);
        panel.add(stockPanel);

        return panel;
    }

    // -- Refresh Data ----------------------------------------------------------

    public void refreshData() {
        int month = cmbMonth.getSelectedIndex() + 1;
        int year  = (int) spinnerYear.getValue();

        loadOrdersTable(month, year);
        loadTopSellersTable(month, year);
        loadLowStockTable();
        updateSummaryCards(month, year);
    }

    private void loadOrdersTable(int month, int year) {
        modelOrders.setRowCount(0);
        try {
            List<CustomerOrder> orders = reportController.getMonthlyOrders(month, year);
            for (CustomerOrder o : orders) {
                modelOrders.addRow(new Object[]{
                        o.getProductName(),
                        o.getCustomerName(),
                        o.getQuantityOrdered(),
                        String.format("Rs. %.2f", o.getUnitPrice()),
                        String.format("Rs. %.2f", o.getTotalAmount()),
                        o.getOrderDate()
                });
            }
        } catch (Exception e) {
            System.err.println("[ReportPanel] Error loading orders: " + e.getMessage());
        }
    }

    private void loadTopSellersTable(int month, int year) {
        modelTopSellers.setRowCount(0);
        try {
            List<CustomerOrder> top = reportController.getTopSellingProducts(month, year, 5);
            int rank = 1;
            for (CustomerOrder o : top) {
                modelTopSellers.addRow(new Object[]{
                        rank++,
                        o.getProductName(),
                        o.getQuantityOrdered(),
                        String.format("Rs. %.2f", o.getTotalAmount())
                });
            }
        } catch (Exception e) {
            System.err.println("[ReportPanel] Error loading top sellers: " + e.getMessage());
        }
    }

    private void loadLowStockTable() {
        modelLowStock.setRowCount(0);
        try {
            List<Product> lowStock = reportController.getLowStockProducts();
            for (Product p : lowStock) {
                modelLowStock.addRow(new Object[]{
                        p.getName(),
                        p.getCategory(),
                        p.getStockQuantity(),
                        p.getReorderLevel(),
                        p.getSupplierName()
                });
            }
        } catch (Exception e) {
            System.err.println("[ReportPanel] Error loading low stock: " + e.getMessage());
        }
    }

    private void updateSummaryCards(int month, int year) {
        try {
            List<CustomerOrder> orders = reportController.getMonthlyOrders(month, year);
            double revenue             = reportController.getMonthlyRevenue(month, year);
            List<Product> lowStock     = reportController.getLowStockProducts();

            lblTotalOrders.setText(String.valueOf(orders.size()));
            lblTotalRevenue.setText(String.format("Rs. %.2f", revenue));
            lblLowStockCount.setText(String.valueOf(lowStock.size()));
        } catch (Exception e) {
            System.err.println("[ReportPanel] Error updating summary: " + e.getMessage());
        }
    }

    // -- PDF Generation --------------------------------------------------------

    private void handleGeneratePDF() {
        int month = cmbMonth.getSelectedIndex() + 1;
        int year  = (int) spinnerYear.getValue();

        // open save dialog so user picks where to save
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report PDF");

        String defaultName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + "_" + year + "_GreenLoop_Report.pdf";
        fileChooser.setSelectedFile(new File(defaultName));

        int userChoice = fileChooser.showSaveDialog(this);
        if (userChoice != JFileChooser.APPROVE_OPTION) return;

        String filePath = fileChooser.getSelectedFile().getAbsolutePath();
        if (!filePath.endsWith(".pdf")) filePath += ".pdf";

        boolean success = reportController.generatePDFReport(month, year, filePath);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Report saved successfully!\n" + filePath,
                    "PDF Generated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to generate PDF. Check logs for details.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -- UI Helpers ------------------------------------------------------------

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        return table;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(2, 2));
        card.setPreferredSize(new Dimension(160, 50));
        card.setBackground(new Color(43, 47, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 65, 70), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
        titleLabel.setForeground(new Color(160, 160, 160));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
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