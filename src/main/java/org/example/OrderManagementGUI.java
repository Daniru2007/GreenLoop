package org.example;

import com.formdev.flatlaf.FlatDarkLaf;
import com.mongodb.client.MongoDatabase;
import org.example.config.DBManager;
import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.controller.DeliveryAgentController;
import org.example.controller.OrderService;
import org.example.controller.ProductController;
import org.example.controller.ReportController;
import org.example.model.CustomerOrder;
import org.example.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class OrderManagementGUI extends JFrame {

    private final OrderService orderService;
    private final ProductController productController;
    private final DeliveryAgentImpl agentRepo;
    private final DeliveryAgentController agentController;
    private final ReportController reportController;

    // Dashboard Stat Labels
    private JLabel lblStatPending;
    private JLabel lblStatProcessing;
    private JLabel lblStatAgents;

    // Extracted Tab Panels
    private OrderProcessingPanel orderProcessingPanel;
    private DeliverySchedulingPanel deliverySchedulingPanel;
    private AgentRegistrationPanel agentRegistrationPanel;
    private ProductManagementPanel productManagementPanel;
    private DeliveryAgentPanel deliveryAgentPanel;
    private ReportPanel reportPanel;

    public OrderManagementGUI() {
        // Initialize services
        orderService = new OrderService();
        productController = new ProductController();
        agentRepo = new DeliveryAgentImpl();
        agentController = new DeliveryAgentController();
        reportController = new ReportController();

        // Frame Setup
        setTitle("GreenLoop - Order & Delivery Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 750);
        setLocationRelativeTo(null);

        // Styling / Theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Could not initialize FlatLaf. Using default metal theme.");
        }

        setLayout(new BorderLayout(0, 0));

        // Create Header/Dashboard Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Instantiate panel components and link refresh callbacks
        Runnable refreshCallback = this::refreshAllData;
        orderProcessingPanel = new OrderProcessingPanel(orderService, productController, refreshCallback);
        deliverySchedulingPanel = new DeliverySchedulingPanel(orderService, agentRepo, refreshCallback);
        agentRegistrationPanel = new AgentRegistrationPanel(agentRepo, refreshCallback);
        productManagementPanel = new ProductManagementPanel(productController, refreshCallback);
        deliveryAgentPanel = new DeliveryAgentPanel(agentController, refreshCallback);
        reportPanel = new ReportPanel(reportController, refreshCallback);

        // Create Tabs
        tabbedPane.addTab("Order Processing", orderProcessingPanel);
        tabbedPane.addTab("Delivery & Scheduling", deliverySchedulingPanel);
//        tabbedPane.addTab("Add Delivery Agent", agentRegistrationPanel);
        tabbedPane.addTab("Product Management", productManagementPanel);
        tabbedPane.addTab("Manage Delivery Agents", deliveryAgentPanel);
        tabbedPane.addTab("Generate Reports", reportPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Load initial data
        refreshAllData();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBorder(new EmptyBorder(15, 20, 10, 20));
        headerPanel.setBackground(new Color(30, 33, 35));

        // Title and Subtitle panel
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("GreenLoop Logistics Portal");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(120, 220, 150)); // Fresh light-green color

        JLabel lblSub = new JLabel("Eco-Friendly Order & Delivery Tracking Console");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(170, 180, 175));

        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Stats Panel (East)
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);

        lblStatPending = new JLabel("0");
        lblStatProcessing = new JLabel("0");
        lblStatAgents = new JLabel("0");

        statsPanel.add(createStatCard("PENDING ORDERS", lblStatPending, new Color(241, 196, 15))); // Yellow/Amber
        statsPanel.add(createStatCard("IN PROCESSING", lblStatProcessing, new Color(52, 152, 219))); // Blue
        statsPanel.add(createStatCard("ACTIVE AGENTS", lblStatAgents, new Color(46, 204, 113))); // Green

        headerPanel.add(statsPanel, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 65, 70)));
        return headerPanel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(2, 2));
        card.setPreferredSize(new Dimension(145, 50));
        card.setBackground(new Color(43, 47, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 65, 70), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        card.putClientProperty("JComponent.roundRect", true);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
        titleLabel.setForeground(new Color(160, 160, 160));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void refreshAllData() {
        if (orderProcessingPanel != null) orderProcessingPanel.refreshData();
        if (deliverySchedulingPanel != null) deliverySchedulingPanel.refreshData();
        if (productManagementPanel != null) productManagementPanel.refreshData();
        if (deliveryAgentPanel != null) deliveryAgentPanel.refreshData();
        if (reportPanel != null) reportPanel.refreshData();

        updateDashboardStats();
    }

    private void updateDashboardStats() {
        try {
            int pending = 0;
            int processing = 0;
            List<CustomerOrder> orders = orderService.getAllOrders();
            for (CustomerOrder o : orders) {
                if (CustomerOrder.STATUS_PENDING.equals(o.getStatus())) {
                    pending++;
                } else if (CustomerOrder.STATUS_PROCESSING.equals(o.getStatus())) {
                    processing++;
                }
            }

            int activeAgents = 0;
            MongoDatabase database = DBManager.getDatabase();
            activeAgents = (int) database.getCollection("delivery_agents").countDocuments();

            if (lblStatPending != null) lblStatPending.setText(String.valueOf(pending));
            if (lblStatProcessing != null) lblStatProcessing.setText(String.valueOf(processing));
            if (lblStatAgents != null) lblStatAgents.setText(String.valueOf(activeAgents));
        } catch (Exception e) {
            System.err.println("Error updating dashboard stats: " + e.getMessage());
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
