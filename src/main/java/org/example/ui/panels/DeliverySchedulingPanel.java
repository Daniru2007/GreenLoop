package org.example.ui.panels;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.config.DBManager;
import org.example.controller.OrderService;
import org.example.model.CustomerOrder;
import org.example.model.DeliveryAgent;
import org.example.ui.UIStyleUtils;
import org.example.ui.wrappers.AgentWrapper;
import org.example.ui.wrappers.OrderWrapper;
import org.example.utils.EmailUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DeliverySchedulingPanel extends JPanel {

    private final OrderService orderService;
    private final DeliveryAgentImpl agentRepo;
    private final Runnable refreshCallback;

    private JComboBox<OrderWrapper> comboPendingOrders;
    private JComboBox<AgentWrapper> comboAvailableAgents;

    private JTable tableAgents;
    private DefaultTableModel modelAgents;

    public DeliverySchedulingPanel(OrderService orderService, DeliveryAgentImpl agentRepo, Runnable refreshCallback) {
        this.orderService = orderService;
        this.agentRepo = agentRepo;
        this.refreshCallback = refreshCallback;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT ASSIGN FORM
        JPanel assignForm = new JPanel(new GridBagLayout());
        assignForm.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(30, 100, 160), 2), 
                " Assign Agent to Order ", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14), new Color(130, 180, 230)));
        assignForm.setPreferredSize(new Dimension(380, 620));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Select Pending Order
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        assignForm.add(UIStyleUtils.createStyledLabel("Select Order:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        comboPendingOrders = new JComboBox<>();
        comboPendingOrders.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboPendingOrders.putClientProperty("JComponent.roundRect", true);
        comboPendingOrders.setPreferredSize(new Dimension(220, 28));
        assignForm.add(comboPendingOrders, gbc);
 
        // Row 1: Select Available Agent
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        assignForm.add(UIStyleUtils.createStyledLabel("Select Agent:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        comboAvailableAgents = new JComboBox<>();
        comboAvailableAgents.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboAvailableAgents.putClientProperty("JComponent.roundRect", true);
        comboAvailableAgents.setPreferredSize(new Dimension(220, 28));
        assignForm.add(comboAvailableAgents, gbc);
 
        // Row 2: Assign Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(30, 10, 10, 10);
        JButton btnAssignAgent = new JButton("Assign Delivery Agent");
        btnAssignAgent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAssignAgent.setBackground(new Color(30, 100, 160));
        btnAssignAgent.setForeground(Color.WHITE);
        btnAssignAgent.setFocusPainted(false);
        btnAssignAgent.putClientProperty("JButton.buttonType", "roundRect");
        btnAssignAgent.putClientProperty("JButton.boldText", true);
        btnAssignAgent.addActionListener(e -> handleAssignAgent());
        assignForm.add(btnAssignAgent, gbc);

        add(assignForm, BorderLayout.WEST);

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
        tableAgents.setRowHeight(28);
        tableAgents.setShowHorizontalLines(true);
        tableAgents.setShowVerticalLines(false);
        tableAgents.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scrollPane = new JScrollPane(tableAgents);
        agentsListPanel.add(scrollPane, BorderLayout.CENTER);
 
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnRefreshAgents = new JButton("Refresh Agents");
        btnRefreshAgents.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefreshAgents.putClientProperty("JButton.buttonType", "roundRect");
        btnRefreshAgents.addActionListener(e -> refreshCallback.run());
        bottomPanel.add(btnRefreshAgents);
        agentsListPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(agentsListPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        // Clear models/combos
        modelAgents.setRowCount(0);
        comboPendingOrders.removeAllItems();
        comboAvailableAgents.removeAllItems();

        try {
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
            JOptionPane.showMessageDialog(this, "Delivery agent assigned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            boolean clientMail = EmailUtils.isLastEmailClientSuccess();
            boolean agentMail = EmailUtils.isLastEmailAgentSuccess();
            if (clientMail && agentMail) {
                JOptionPane.showMessageDialog(this, "Emails sent to client and delivery agent successfully!", "Email Sent", JOptionPane.INFORMATION_MESSAGE);
            } else if (!clientMail && !agentMail) {
                JOptionPane.showMessageDialog(this, "Failed to send emails to client and delivery agent!", "Email Error", JOptionPane.ERROR_MESSAGE);
            } else if (!clientMail) {
                JOptionPane.showMessageDialog(this, "Failed to send email to client!", "Email Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to send email to delivery agent!", "Email Error", JOptionPane.ERROR_MESSAGE);
            }
            refreshCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to assign agent.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
