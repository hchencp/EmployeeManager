
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;








public class EmployeeSchedulingApp {




    private JFrame mainFrame;
    private JLabel titleLabel;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JLabel positionLabel;
    private JComboBox<String> positionComboBox;
    private JButton addButton;
    private JTable employeeTable;
    private static Connection connection;
    private DefaultTableModel tableModel;
    private JComboBox<String> queryComboBox;
    private JButton executeButton;








    public EmployeeSchedulingApp() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://ambari-node5.csc.calpoly.edu:3306/Team2";
            String username = "Team2";
            String password = "1234";
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }




        prepareGUI();
        populateEmployeeTable();




    }
    private int addEmployee(String employeeName, String age, String address, String phoneNumber, String position) {
        String insertEmployeeSql = "INSERT INTO Employee (name, age, address, phoneNumber) VALUES (?, ?, ?, ?)";
        String insertRoleSql = "INSERT INTO Role (empID, posID) VALUES (?, ?)";








        try {
            PreparedStatement insertEmployeeStatement = connection.prepareStatement(insertEmployeeSql,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            insertEmployeeStatement.setString(1, employeeName);
            insertEmployeeStatement.setString(2, age);
            insertEmployeeStatement.setString(3, address);
            insertEmployeeStatement.setString(4, phoneNumber);
            int affectedRows = insertEmployeeStatement.executeUpdate();








            if (affectedRows > 0) {
                ResultSet generatedKeys = insertEmployeeStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int empID = generatedKeys.getInt(1);








                    String getPosIDSql = "SELECT posID FROM Positions WHERE posName = ?";
                    PreparedStatement getPosIDStatement = connection.prepareStatement(getPosIDSql);
                    getPosIDStatement.setString(1, position);
                    ResultSet posIDResultSet = getPosIDStatement.executeQuery();












                    if (posIDResultSet.next()) {
                        int posID = posIDResultSet.getInt("posID");








                        PreparedStatement insertRoleStatement = connection.prepareStatement(insertRoleSql);
                        insertRoleStatement.setInt(1, empID);
                        insertRoleStatement.setInt(2, posID);
                        insertRoleStatement.executeUpdate();




                        JOptionPane.showMessageDialog(mainFrame, "Employee added successfully!");
                        populateEmployeeTable(); // Refresh the table
                    }
                    return empID;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error adding employee.");
        }
        return 0;
    }




    private void addAvailability(int empID, String Mon, String Tues, String Wed, String Thurs, String Fri, String Sat, String Sun) {
        String insertAvailabilitySql = "INSERT INTO WeeklyAvailability (empID, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";








        try {
            PreparedStatement insertAvailabilityStatement = connection.prepareStatement(insertAvailabilitySql,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            insertAvailabilityStatement.setInt(1, empID);
            insertAvailabilityStatement.setString(2, Mon);
            insertAvailabilityStatement.setString(3, Tues);
            insertAvailabilityStatement.setString(4, Wed);
            insertAvailabilityStatement.setString(5, Thurs);
            insertAvailabilityStatement.setString(6, Fri);
            insertAvailabilityStatement.setString(7, Sat);
            insertAvailabilityStatement.setString(8, Sun);
            insertAvailabilityStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error adding employee.");
        }
    }




    private void prepareGUI() {
        mainFrame = new JFrame("Employee Scheduling App");
        mainFrame.setSize(800, 600);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);








        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);








        titleLabel = new JLabel("Employee Scheduling App", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));








        tableModel = new DefaultTableModel();
        employeeTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(employeeTable);












        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBackground(new Color(38, 113, 63)); // Light blue background
        topPanel.add(titleLabel);












        JPanel middlePanel = new JPanel(new FlowLayout());
        middlePanel.setBackground(Color.WHITE);
        JButton showScheduleForSpecificDayButton = new JButton("Show Schedule for a Specific Day");
        JButton showAllSchedulesButton = new JButton("Show All Schedules");
        JTable allSchedulesTable = new JTable(); // Create an empty JTable for all schedules












        showScheduleForSpecificDayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String inputDate = JOptionPane.showInputDialog("Enter a specific day (YYYY-MM-DD):");
                if (inputDate != null) {
                    showScheduleForSpecificDay(inputDate);
                }
            }
        });








        showAllSchedulesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                allSchedulesTable.setModel(getAllSchedulesTableModel()); // Set the model for the all schedules table
                JScrollPane allSchedulesScrollPane = new JScrollPane(allSchedulesTable);
                middlePanel.removeAll(); // Clear the middle panel
                middlePanel.add(allSchedulesScrollPane); // Add the new all schedules table to the middle panel
                mainFrame.revalidate(); // Refresh the frame
            }
        });
        JPanel middleButtonPanel = new JPanel(new FlowLayout());
        middleButtonPanel.add(showScheduleForSpecificDayButton);
        middleButtonPanel.add(showAllSchedulesButton);
        middlePanel.add(middleButtonPanel);












        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(middlePanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);








        mainFrame.add(mainPanel);








        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBackground(new Color(243, 210, 52)); // Light yellow background




        queryComboBox = new JComboBox<>(new String[]{
                "Show Schedule for a Specific Day",
                "Get Employee ID",
                "Add New Employee",
                "Determine Availability (Shift & Days of Week)",
                "Update Availability",
                "Assign Role",
                "Add Employee to Schedule",
                "Sort by Position"
        });








        queryComboBox.setPreferredSize(new Dimension(300, 25));
        executeButton = new JButton("Execute Query");
        executeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedQuery = (String) queryComboBox.getSelectedItem();
                executeQuery(selectedQuery);
            }
        });








        bottomPanel.add(queryComboBox);
        bottomPanel.add(executeButton);
        mainFrame.add(bottomPanel, BorderLayout.SOUTH);




        mainFrame.setVisible(true);
    }












    private void populatePositionComboBox() {
        String sql = "SELECT posName FROM Positions";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String positionName = resultSet.getString("posName");
                positionComboBox.addItem(positionName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private DefaultTableModel getAllSchedulesTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Shift");
        model.addColumn("Position");
        model.addColumn("Employee Name");
        model.addColumn("Date"); // Add date column to display the schedule date








// Replace this with your SQL query and populate the table model
        String query = "SELECT s.shift, p.posName, e.name, s.day " +
                "FROM Scheduled s " +
                "JOIN Role r ON s.roleID = r.roleID " +
                "JOIN Employee e ON r.empID = e.empID " +
                "JOIN Positions p ON r.posID = p.posID " +
                "ORDER BY s.day ASC, s.shift ASC, p.posName ASC";
















        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();












            while (resultSet.next()) {
                String shift = resultSet.getString("shift");
                String posName = resultSet.getString("posName");
                String empName = resultSet.getString("name");
                String day = resultSet.getString("day");
                model.addRow(new Object[]{shift, posName, empName, day});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error fetching schedules.");
        }




        return model;
    }




    private void populateEmployeeTable() {
        tableModel.setColumnIdentifiers(new String[]{"Employee ID", "Name", "Position"});
        tableModel.setRowCount(0); // Clear existing rows








        String sql = "SELECT e.empID, e.name, p.posName FROM Employee e " +
                "JOIN Role r ON e.empID = r.empID " +
                "JOIN Positions p ON r.posID = p.posID" + " ORDER BY e.empID DESC";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int empID = resultSet.getInt("empID");
                String name = resultSet.getString("name");
                String position = resultSet.getString("posName");




                tableModel.addRow(new Object[]{empID, name, position});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void showScheduleForSpecificDay(String day) {
        String query = "SELECT s.shift, p.posName, e.name " +
                "FROM Scheduled s " +
                "JOIN Role r ON s.roleID = r.roleID " +
                "JOIN Employee e ON r.empID = e.empID " +
                "JOIN Positions p ON r.posID = p.posID " +
                "WHERE s.day = ? " +
                "ORDER BY s.shift ASC, p.posName ASC";








        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Shift");
        model.addColumn("Position");
        model.addColumn("Employee Name");




        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, day);
            ResultSet resultSet = preparedStatement.executeQuery();








            while (resultSet.next()) {
                String shift = resultSet.getString("shift");
                String posName = resultSet.getString("posName");
                String empName = resultSet.getString("name");
                model.addRow(new Object[]{shift, posName, empName});
            }




            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            JOptionPane.showMessageDialog(mainFrame, scrollPane, "Schedule for " + day, JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error fetching schedule.");
        }
    }
    private DefaultTableModel getScheduleTableModel(String day) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Shift");
        model.addColumn("Position");
        model.addColumn("Employee Name");




        String query = "SELECT s.shift, p.posName, e.name " +
                "FROM Scheduled s " +
                "JOIN Role r ON s.roleID = r.roleID " +
                "JOIN Employee e ON r.empID = e.empID " +
                "JOIN Positions p ON r.posID = p.posID " +
                "WHERE s.day = ? " +
                "ORDER BY s.shift ASC, p.posName ASC";








        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, day);
            ResultSet resultSet = preparedStatement.executeQuery();












            while (resultSet.next()) {
                String shift = resultSet.getString("shift");
                String posName = resultSet.getString("posName");
                String empName = resultSet.getString("name");
                model.addRow(new Object[]{shift, posName, empName});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error fetching schedule.");
        }




        return model;
    }
    private void createScheduleForSpecificDay(String day) {
        String query = "INSERT INTO Scheduled (empID, roleID, wkavailID, day, shift) " +
                "SELECT r.empID, r.roleID, a.wkavailID, ?, a.Monday " +
                "FROM Role r " +
                "JOIN WeeklyAvailability a ON r.empID = a.empID " +
                "WHERE a.Monday = 'FirstShift' OR a.Monday = 'BothShifts'";




        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, day);
            int affectedRows = preparedStatement.executeUpdate();




            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(mainFrame, "Schedule created successfully!");
            } else {
                JOptionPane.showMessageDialog(mainFrame, "No employees available for scheduling.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error creating schedule.");
        }
    }




    private void updateAvailabilityForTheWeek(String empID, String weekDay, String shift) {




// Update the availability in the database
        String updateAvailabilitySql = "UPDATE WeeklyAvailability SET " +
                weekDay + " = ? " +
                "WHERE empID = " + empID;




        try {
            PreparedStatement updateStatement = connection.prepareStatement(updateAvailabilitySql);
            updateStatement.setString(1, shift);




            int affectedRows = updateStatement.executeUpdate();




            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(mainFrame, "Availability updated successfully!");
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Error updating availability.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error updating availability.");
        }
    }








    private void addEmployeeScheduleForSpecificDay(String empDay, String Shift, String empID) {
        String query = "INSERT INTO Scheduled (empID, roleID, wkavailID, day, shift) " +
                "SELECT e.empID, r.roleID, wa.wkavailid, ?, ?" +
                "FROM Role r, WeeklyAvailability wa, Employee e " +
                "WHERE e.empID = " + empID + " and r.empID = " + empID + " and wa.empID = " + empID;




        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, empDay);
            preparedStatement.setString(2, Shift);
            int affectedRows = preparedStatement.executeUpdate();












            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(mainFrame, "Schedule created successfully!");
            } else {
                JOptionPane.showMessageDialog(mainFrame, "No employees available for scheduling.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error creating schedule.");
        }
    }








    private void sortByPosition(String selectedPosition) {
        tableModel.setRowCount(0); // Clear existing rows








        String sql = "SELECT e.empID, e.name, p.posName FROM Employee e " +
                "JOIN Role r ON e.empID = r.empID " +
                "JOIN Positions p ON r.posID = p.posID " +
                "WHERE p.posName = ? " + // Filter by selected position
                "ORDER BY e.name ASC"; // Sort by employee name








        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, selectedPosition);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int empID = resultSet.getInt("empID");
                String name = resultSet.getString("name");
                String position = resultSet.getString("posName");




                tableModel.addRow(new Object[]{empID, name, position});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }








    private void getEmployeeID(String employeeName, String number) {








        String getEmployeeID = "SELECT empID FROM Employee WHERE name = ? and phoneNumber = ?";








        try{
            PreparedStatement getEmpIDStatement = connection.prepareStatement(getEmployeeID);
            getEmpIDStatement.setString(1, employeeName);
            getEmpIDStatement.setString(2, number);
            ResultSet empIDResultSet = getEmpIDStatement.executeQuery();




            if (empIDResultSet.next()) {
                int empID = empIDResultSet.getInt("empID");
                JOptionPane.showMessageDialog(mainFrame, employeeName + "'s ID: " + empID);
            }
            else {
                JOptionPane.showMessageDialog(mainFrame, "Employee Not Found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error finding Employee ID.");
        }
    }




    private void executeQuery(String selectedQuery) {
        int empID = -1;
        switch (selectedQuery) {
            case "Show Schedule for a Specific Day":
                String inputDate = JOptionPane.showInputDialog("Enter a specific day (YYYY-MM-DD):");
                if (inputDate != null) {
                    showScheduleForSpecificDay(inputDate);
                }
                break;
            case "Add New Employee":
                JTextField name = new JTextField();
                JTextField strage = new JTextField();
                JTextField addy = new JTextField();
                JTextField pn = new JTextField();
                positionComboBox = new JComboBox<>();
                populatePositionComboBox();




                Object [] fields = {
                        "Name: ", name,
                        "Age: ", strage,
                        "Address: ", addy,
                        "Phone Number: ", pn,
                        "Position: ", positionComboBox,
                };




                JOptionPane.showConfirmDialog(null,fields,"Add Employee Information",JOptionPane.OK_CANCEL_OPTION);


                String[] addshifts = {"FirstShift", "SecondShift", "BothShifts", "NeitherShift"};
                JComboBox addmondayBox = new JComboBox<>(addshifts);
                JComboBox addtuesdayBox = new JComboBox<>(addshifts);
                JComboBox addwednesdayBox = new JComboBox<>(addshifts);
                JComboBox addthursdayBox = new JComboBox<>(addshifts);
                JComboBox addfridayBox = new JComboBox<>(addshifts);
                JComboBox addsaturdayBox = new JComboBox<>(addshifts);
                JComboBox addsundayBox = new JComboBox<>(addshifts);




                Object [] addAvailfields = {
                        "Monday: ", addmondayBox,
                        "Tuesday: ", addtuesdayBox,
                        "Wednesday: ", addwednesdayBox,
                        "Thursday: ", addthursdayBox,
                        "Friday: ", addfridayBox,
                        "Saturday: ", addsaturdayBox,
                        "Sunday: ", addsundayBox,
                };




                empID = addEmployee(name.getText(), strage.getText(), addy.getText(), pn.getText(), positionComboBox.getSelectedItem().toString());




                if (empID != 0) {




                    JOptionPane.showConfirmDialog(null, addAvailfields, "Add Employee Availability", JOptionPane.OK_CANCEL_OPTION);




                    addAvailability(empID, addmondayBox.getSelectedItem().toString(), addtuesdayBox.getSelectedItem().toString(),
                            addwednesdayBox.getSelectedItem().toString(), addthursdayBox.getSelectedItem().toString(), addfridayBox.getSelectedItem().toString(),
                            addsaturdayBox.getSelectedItem().toString(), addsundayBox.getSelectedItem().toString());
                }
                break;
            case "Determine Availability (Shift & Days of Week)":
                empID = Integer.parseInt(JOptionPane.showInputDialog("Enter employee ID:"));
                String availabilityQuery = "SELECT * FROM WeeklyAvailability WHERE empID = ?";
                try {
                    PreparedStatement availabilityStatement = connection.prepareStatement(availabilityQuery);
                    availabilityStatement.setInt(1, empID);
                    ResultSet availabilityResultSet = availabilityStatement.executeQuery();




                    if (availabilityResultSet.next()) {
                        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                        String availabilityMessage = "Availability for Employee ID " + empID + ":\n";




                        for (String day : daysOfWeek) {
                            String availability = availabilityResultSet.getString(day);
                            availabilityMessage += day + ": " + availability + "\n";
                        }




                        JOptionPane.showMessageDialog(mainFrame, availabilityMessage);
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Employee not found or no availability data.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, "Error determining availability.");
                }
                break;
            case "Assign Role":
                empID = Integer.parseInt(JOptionPane.showInputDialog("Enter employee ID:"));
                positionComboBox = new JComboBox<>();
                populatePositionComboBox();
                JOptionPane.showConfirmDialog(null, positionComboBox, "Select Role:", JOptionPane.OK_CANCEL_OPTION);








                String getPosIDSql = "SELECT posID FROM Positions WHERE posName = ?";
                String updateRoleSql = "UPDATE Role SET posID = ? WHERE empID = ?";




                try {
                    PreparedStatement getPosIDStatement = connection.prepareStatement(getPosIDSql);
                    getPosIDStatement.setString(1, positionComboBox.getSelectedItem().toString());
                    ResultSet posIDResultSet = getPosIDStatement.executeQuery();




                    if (posIDResultSet.next()) {
                        int posID = posIDResultSet.getInt("posID");




                        PreparedStatement updateRoleStatement = connection.prepareStatement(updateRoleSql);
                        updateRoleStatement.setInt(1, posID);
                        updateRoleStatement.setInt(2, empID);
                        int affectedRows = updateRoleStatement.executeUpdate();




                        if (affectedRows > 0) {
                            JOptionPane.showMessageDialog(mainFrame, "Role updated successfully!");
                            populateEmployeeTable(); // Refresh the table
                        } else {
                            JOptionPane.showMessageDialog(mainFrame, "Employee not found.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Role not found.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, "Error updating role.");
                }
                break;
            case "Sort by Position":
                positionComboBox = new JComboBox<>();
                populatePositionComboBox();
                JOptionPane.showConfirmDialog(null, positionComboBox, "Select Position:", JOptionPane.OK_CANCEL_OPTION);
                sortByPosition(positionComboBox.getSelectedItem().toString());
                break;
            case "Update Availability":
                JTextField eid = new JTextField();
                String[] shifts = {"FirstShift", "SecondShift", "BothShifts", "NeitherShift"};
                JComboBox shift = new JComboBox<>(shifts);
                String[] weekDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                JComboBox weekDay = new JComboBox<>(weekDays);


                Object [] upAvailfields = {
                        "Employee ID: ", eid,
                        "Day of Week: ", weekDay,
                        "Shift: ", shift
                };




                JOptionPane.showConfirmDialog(null,upAvailfields,"Update Employee Availability",JOptionPane.OK_CANCEL_OPTION);
                updateAvailabilityForTheWeek(eid.getText(), weekDay.getSelectedItem().toString(), shift.getSelectedItem().toString());
                break;
            case "Get Employee ID":
                JTextField getEmpID = new JTextField();
                JTextField empPhoneNum = new JTextField();


                Object [] empIDfields = {
                        "Name: ", getEmpID,
                        "Phone Number: ", empPhoneNum
                };




                JOptionPane.showConfirmDialog(null,empIDfields,"Find Employee ID",JOptionPane.OK_CANCEL_OPTION);
                getEmployeeID(getEmpID.getText(), empPhoneNum.getText());
                break;
            case "Add Employee to Schedule":
                JTextField newEID = new JTextField();
                JTextField empDay = new JTextField();
                String[] addshiftstoday = {"FirstShift", "SecondShift", "BothShifts"};
                JComboBox addshiftBox = new JComboBox<>(addshiftstoday);


                Object [] emptoSchfields = {
                        "Employee ID: ", newEID,
                        "Day (YYYY-MM-DD):" , empDay,
                        "Position: ", addshiftBox,
                };
                JOptionPane.showConfirmDialog(null, emptoSchfields, "Schedule Employee:", JOptionPane.OK_CANCEL_OPTION);
                addEmployeeScheduleForSpecificDay(empDay.getText(), addshiftBox.getSelectedItem().toString(), newEID.getText());
                break;








            default:
                break;
        }
    }












    public static void main(String[] args) {
        EmployeeSchedulingApp app = new EmployeeSchedulingApp();
    }
}

