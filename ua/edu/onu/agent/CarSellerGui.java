package ua.edu.onu.agent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class CarSellerGui extends JFrame {
    private CarSellerAgent myAgent;

    public static final String[] HEADER = new String[]{"Id", "Mileage", "Price", "Status"};
    private JTextField mileageField, priceField;
    private JTable table;

    CarSellerGui(CarSellerAgent a) {
        super(a.getLocalName());

        myAgent = a;

        JPanel mainPanel = new JPanel();
        table = new JTable();
        render(new String[][]{{}});
        mainPanel.add(new JScrollPane(table));
        mainPanel.add(new JLabel("Car mileage:"));
        mileageField = new JTextField(15);
        mainPanel.add(mileageField);
        mainPanel.add(new JLabel("Price:"));
        priceField = new JTextField(15);
        mainPanel.add(priceField);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String mileage = mileageField.getText().trim();
                    String price = priceField.getText().trim();
                    myAgent.updateCatalogue(mileage, Integer.parseInt(price));
                    mileageField.setText("");
                    priceField.setText("");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CarSellerGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        mainPanel = new JPanel();
        mainPanel.add(addButton);
        getContentPane().add(mainPanel, BorderLayout.SOUTH);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });

        setResizable(false);
    }

    void render(String[][] data) {
        table.setModel(new DefaultTableModel(data, HEADER));
    }

    public void show() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.show();
    }
}