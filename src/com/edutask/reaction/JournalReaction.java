package com.edutask.reaction;

import com.edutask.model.Task;
import com.edutask.ui.ReflectionDialog;
import javax.swing.SwingUtilities;

public class JournalReaction implements Reaction {

    @Override
    public void execute(Task task) {
        SwingUtilities.invokeLater(() -> {
            ReflectionDialog dialog = new ReflectionDialog(null, task);
            dialog.setVisible(true);
        });
    }
}
