package com.edutask.reaction;

import com.edutask.model.Task;
import com.edutask.ui.CelebrationPanel;
import javax.swing.*;

public class CelebrationReaction implements Reaction {

    @Override
    public void execute(Task task) {
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog((JFrame) null, "Celebration!", true);
            dialog.setContentPane(new CelebrationPanel(task));
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        });
    }
}
