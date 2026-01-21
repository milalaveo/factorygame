package app;

import ui.FactoryGameUI;

import javax.swing.SwingUtilities;

// I use this as the app entry point.
public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FactoryGameUI ui = new FactoryGameUI();
            ui.setLocationRelativeTo(null);
            ui.setVisible(true);
        });
    }
}
