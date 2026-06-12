package org.example.ui;

import javax.swing.*;
import java.awt.*;

public class UIStyleUtils {
    public static JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return label;
    }
}
