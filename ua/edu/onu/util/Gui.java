package ua.edu.onu.util;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Gui extends JFrame {

    private final JPanel mainPanel;
    public static final String[] HEADER = new String[]{"Buyer", "Milleage", "Price", "Status", "Deal details"};
    private JTable table;

    public Gui() throws HeadlessException {
        super("Car Market");
        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Buyer agents list", TitledBorder.CENTER, TitledBorder.TOP));
        table = new JTable();
        render(new String[][]{{}});
        mainPanel.add(new JScrollPane(table));
        this.add(mainPanel);
        this.setSize(550, 400);
        this.setVisible(true);
    }

    void render(String[][] data) {
        table.setModel(new DefaultTableModel(data, HEADER));
    }

    private void log(String text) {
        System.out.println(ConsoleColors.PURPLE + "[" + Gateway.getInstance().getRequestNumber() + "]" + text + ConsoleColors.RESET);
    }
}
