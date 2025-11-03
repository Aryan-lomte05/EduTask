package com.edutask.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class IconGenerator {

    public static ImageIcon createAppIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background circle
        g2.setColor(new Color(41, 128, 185));
        g2.fillOval(2, 2, 60, 60);

        // White check mark / task symbol
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(18, 32, 28, 42);
        g2.drawLine(28, 42, 48, 20);

        g2.dispose();
        return new ImageIcon(image);
    }

    public static void setAppIcon(JFrame frame) {
        frame.setIconImage(createAppIcon().getImage());
    }
}
