import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;
import net.sourceforge.jdatepicker.impl.DateComponentFormatter;

public class Main {
    private static AppointmentManager manager = new AppointmentManager();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Manage Your Appointments");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 450);
        frame.setLayout(new BorderLayout(15, 15));

        // Input section
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        JLabel typeLabel = new JLabel("Type:");
        String[] appointmentTypes = {"One-time", "Daily", "Monthly"};
        JComboBox<String> typeDropdown = new JComboBox<>(appointmentTypes);

        JLabel startLabel = new JLabel("Start Date:");
        JDatePickerImpl startPicker = createDatePicker();

        JLabel endLabel = new JLabel("End Date:");
        JDatePickerImpl endPicker = createDatePicker();

        JLabel descLabel = new JLabel("Description:");
        JTextField descField = new JTextField();

        inputPanel.add(typeLabel);
        inputPanel.add(typeDropdown);
        inputPanel.add(startLabel);
        inputPanel.add(startPicker);
        inputPanel.add(endLabel);
        inputPanel.add(endPicker);
        inputPanel.add(descLabel);
        inputPanel.add(descField);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton checkButton = new JButton("Check");
        JButton updateButton = new JButton("Update");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(checkButton);
        buttonPanel.add(updateButton);

        // Display panel
        JPanel displayPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> appointmentList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(appointmentList);

        JLabel statusLabel = new JLabel("", SwingConstants.CENTER);

        displayPanel.add(scrollPane, BorderLayout.CENTER);
        displayPanel.add(statusLabel, BorderLayout.SOUTH);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.add(displayPanel, BorderLayout.SOUTH);

        // Button actions
        addButton.addActionListener(e -> {
            try {
                String type = (String) typeDropdown.getSelectedItem();
                LocalDate start = convertToDate(startPicker);
                LocalDate end = convertToDate(endPicker);
                String description = descField.getText();

                Appointment appointment;
                if ("One-time".equals(type)) {
                    appointment = new OnetimeAppointment(start, end, description);
                } else if ("Daily".equals(type)) {
                    appointment = new DailyAppointment(start, end, description);
                } else {
                    appointment = new MonthlyAppointment(start, end, description);
                }

                manager.add(appointment);
                listModel.addElement(appointment.toString());
                statusLabel.setText("Added successfully.");
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        deleteButton.addActionListener(e -> {
            int selected = appointmentList.getSelectedIndex();
            if (selected >= 0) {
                Appointment toDelete = manager.getAppointments()
                        .stream()
                        .toList()
                        .get(selected);
                manager.delete(toDelete);
                listModel.remove(selected);
                statusLabel.setText("Deleted.");
            } else {
                statusLabel.setText("Select an appointment to delete.");
            }
        });

        checkButton.addActionListener(e -> {
            try {
                String input = JOptionPane.showInputDialog("Enter date (yyyy-mm-dd):");
                LocalDate date = LocalDate.parse(input);

                Appointment[] results = manager.getAppointmentsOn(date, null);
                if (results.length > 0) {
                    statusLabel.setText("Found: " + results[0].getDescription());
                } else {
                    statusLabel.setText("No appointments found.");
                }
            } catch (Exception ex) {
                statusLabel.setText("Invalid date.");
            }
        });

        updateButton.addActionListener(e -> {
            int selected = appointmentList.getSelectedIndex();
            if (selected >= 0) {
                Appointment current = manager.getAppointments()
                        .stream()
                        .toList()
                        .get(selected);

                String newDescription = JOptionPane.showInputDialog("New description:");
                if (newDescription == null || newDescription.isEmpty()) {
                    statusLabel.setText("Update canceled.");
                    return;
                }

                LocalDate newStart = LocalDate.parse(JOptionPane.showInputDialog("New start date (yyyy-mm-dd):"));
                LocalDate newEnd = LocalDate.parse(JOptionPane.showInputDialog("New end date (yyyy-mm-dd):"));

                String type = (String) typeDropdown.getSelectedItem();
                Appointment updated;
                if ("One-time".equals(type)) {
                    updated = new OnetimeAppointment(newStart, newEnd, newDescription);
                } else if ("Daily".equals(type)) {
                    updated = new DailyAppointment(newStart, newEnd, newDescription);
                } else {
                    updated = new MonthlyAppointment(newStart, newEnd, newDescription);
                }

                manager.update(current, updated);
                listModel.set(selected, updated.toString());
                statusLabel.setText("Updated.");
            } else {
                statusLabel.setText("Select an appointment to update.");
            }
        });

        frame.setVisible(true);
    }

    private static JDatePickerImpl createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        JDatePanelImpl panel = new JDatePanelImpl(model);
        return new JDatePickerImpl(panel, new DateComponentFormatter());
    }

    private static LocalDate convertToDate(JDatePickerImpl picker) {
        java.util.Date date = (java.util.Date) picker.getModel().getValue();
        if (date == null) {
            throw new IllegalArgumentException("Date not selected!");
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
