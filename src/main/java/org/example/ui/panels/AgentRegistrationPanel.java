package org.example.ui.panels;

import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.model.DeliveryAgent;
import org.example.ui.UIStyleUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AgentRegistrationPanel extends JPanel {

    private final DeliveryAgentImpl agentRepo;
    private final Runnable refreshCallback;

    private JTextField txtAgentName;
    private JTextField txtAgentPhone;
    private JTextField txtAgentEmail;
    private JTextField txtLicenseNumber;
    private JTextField txtVehicleType;
    private JTextField txtVehiclePlate;
    private JTextField txtVehicleModel;

    public AgentRegistrationPanel(DeliveryAgentImpl agentRepo, Runnable refreshCallback) {
        this.agentRepo = agentRepo;
        this.refreshCallback = refreshCallback;

        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(30, 30, 30, 30));

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
        txtAgentName.putClientProperty("JTextField.placeholderText", "e.g. John Doe");
        txtAgentPhone = new JTextField(20);
        txtAgentPhone.putClientProperty("JTextField.placeholderText", "e.g. 0772223334");
        txtAgentEmail = new JTextField(20);
        txtAgentEmail.putClientProperty("JTextField.placeholderText", "e.g. john@example.com");
        txtLicenseNumber = new JTextField(20);
        txtLicenseNumber.putClientProperty("JTextField.placeholderText", "e.g. EP-BJT-1234");
        txtVehicleType = new JTextField(20);
        txtVehicleType.putClientProperty("JTextField.placeholderText", "e.g. Electric Bike");
        txtVehiclePlate = new JTextField(20);
        txtVehiclePlate.putClientProperty("JTextField.placeholderText", "e.g. EP-BJT-1234");
        txtVehicleModel = new JTextField(20);
        txtVehicleModel.putClientProperty("JTextField.placeholderText", "e.g. E-Bike 200");
 
        String[] labelTexts = {"Full Name:", "Phone Number:", "Email Address:", 
                               "License Number:", "Vehicle Type:", "Vehicle Plate:", "Vehicle Model:"};
        JTextField[] fields = {txtAgentName, txtAgentPhone, txtAgentEmail, 
                               txtLicenseNumber, txtVehicleType, txtVehiclePlate, txtVehicleModel};
 
        for (int i = 0; i < labelTexts.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.0;
            innerForm.add(UIStyleUtils.createStyledLabel(labelTexts[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fields[i].putClientProperty("JComponent.roundRect", true);
            innerForm.add(fields[i], gbc);
        }
 
        // Add Agent Button
        gbc.gridx = 0; gbc.gridy = labelTexts.length; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(20, 15, 15, 15);
        JButton btnAddAgent = new JButton("Register Agent");
        btnAddAgent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddAgent.setBackground(new Color(46, 111, 64));
        btnAddAgent.setForeground(Color.WHITE);
        btnAddAgent.putClientProperty("JButton.buttonType", "roundRect");
        btnAddAgent.putClientProperty("JButton.boldText", true);
        btnAddAgent.addActionListener(e -> handleAddAgent());
        innerForm.add(btnAddAgent, gbc);

        add(innerForm);
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
            
            refreshCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to register agent. Check database connection logs.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
