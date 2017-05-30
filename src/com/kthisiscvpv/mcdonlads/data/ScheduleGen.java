package com.kthisiscvpv.mcdonlads.data;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Border;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * The Main Schedule Generator Object
 * @author Charles
 */
@SuppressWarnings({ "resource", "deprecation" })
public class ScheduleGen {

    private HashMap<Day, TimeTable> dayTables; // General variable declarations
    private HashMap<Employee, Integer> hoursWorked;

    /**
     * Generate a schedule with no given output file (a default will be used)
     * @param inputFile input file
     * @param employees list of employees
     * @throws IOException File writing error occurs
     * @throws WriteException Excel data writing error occurs
     */
    public ScheduleGen(File inputFile, List<Employee> employees) throws IOException, WriteException {
        this(inputFile, new File(new SimpleDateFormat("MM-dd-yyyy hh-mm-ss").format(System.currentTimeMillis()) + ".xls"), employees); // Default output file will be named according to the current date of the launch
    }

    /**
     * Generate a schedule using the data from the input file and the employees in the list to the output file
     * @param inputFile input file
     * @param outputFile output file
     * @param employees list of employees
     * @throws IOException File writing error occurs
     * @throws WriteException Excel data writing error occurs
     */
    public ScheduleGen(File inputFile, File outputFile, List<Employee> employees) throws IOException, WriteException {
        if (inputFile == null || !inputFile.exists()) { // Check if the file does not exit
            throw new IOException("Unable to generate schedule! The specified input file does not exist!"); // Throw an error if it does not exist
        }

        this.dayTables = new HashMap<Day, TimeTable>(); // Declaration of the map containing each of the time tables for each of the day
        Day[] daysOfWeek = Day.values(); // Array containing each day of the week
        List<Day> tempList = Arrays.asList(daysOfWeek); // List declaration of the array containing the days of the week
        Collections.reverse(tempList); // Reverse the list from Monday -> Sunday to Sunday -> Monday. We want to render the last date first for excel order.
        daysOfWeek = (Day[]) tempList.toArray(); // Reverses the list back to an array to iterate from using integer representations

        this.hoursWorked = new HashMap<Employee, Integer>(); // Declarations of a map of the hours worked per client
        for (Employee employee : employees) { // Loop through each client on the map
            hoursWorked.put(employee, 0); // Defines a default value to each of the client
        }

        for (Day day : daysOfWeek) { // Loop through each day of the week
            TimeTable timeTable = new TimeTable(day, employees, hoursWorked); // Defines a default time table for each day of the week
            this.dayTables.put(day, timeTable); // Puts the default time table into the origional map
        }

        Scanner scanner = new Scanner(inputFile); // Scanner for the input file
        Day currentDay = null; // The current day the file is reading

        while (scanner.hasNextLine()) { // Iterates as long as the scanner has another line
            String nextLine = scanner.nextLine(); // Retrieves the next line in the flie
            if (nextLine.trim().length() == 0) { // Makes sure that it is not an empty line (Ex. a space)
                continue; // Continues if it is as there's nothing to analyse
            }

            if (nextLine.length() == 1) { // Check if the length of the line in 1 character long, assuming that it is a day declaration
                currentDay = Day.fromChar(nextLine.charAt(0)); // Retrieves the day from the character
                if (currentDay == null) { // Check if it is an invalid declaration
                    throw new NumberFormatException("Unable to identify date from value '" + nextLine + "'."); // Throw an error to the user
                }
            } else { // Length is greater than one, assuming that it was a time declaration
                if (currentDay == null) { // Verify that a day has been declared before the time
                    throw new NumberFormatException("Day of the week has not been defined! Cannot begin processing schedule!"); // Throw an error if date was not declared
                }

                TimeTable timeTable = this.dayTables.get(currentDay); // Get the time table of the day
                String[] scheduleArgs = nextLine.split(" "); // Split the argument up by the space
                int employeesNeeded = Integer.parseInt(scheduleArgs[1]); // Integer representation of the amount of workers needed within that frame

                String[] timeArguments = scheduleArgs[0].split("-"); // Split the time up by the 'to' symbol, start to finish
                int startHour = timeArguments[0].contains(":") ? Integer.parseInt(timeArguments[0].split(":")[0]) : Integer.parseInt(timeArguments[0]); // We only care about the hour interval so only the numbers before the ':' are concerned. This is the start time in argument 0.
                int endHour = timeArguments[1].contains(":") ? Integer.parseInt(timeArguments[1].split(":")[0]) : Integer.parseInt(timeArguments[1]); // Same reasoning as above, this is the end time in argument 1.

                for (int i = startHour; i < endHour; i++) { // Iterates from the start hour to the end hour
                    timeTable.setRequiredEmployees(i, employeesNeeded); // Sets the amount of employees required to the value that was read.
                }
            }
        }

        scanner.close(); // No more need to scan the file. Closes the scanner.

        HashMap<Employee, Integer> employeeColIndex = new HashMap<Employee, Integer>(); // This value maps the relationship between employee and their column on the table outputted below. This value remains unchanged.
        for (Employee employee : employees) { // Iterates through each of the employees
            employeeColIndex.put(employee, employeeColIndex.size() + 1); // Assigns each employee a value on the table
        }

        for (Day day : daysOfWeek) { // Iterates through each day of the week
            TimeTable timeTable = this.dayTables.get(day); // Retrieves the timetable of that day of the week
            for (Employee employee : employees) { // Iterates through each of the employees
                for (int i = 0; i < 24; i++) { // Iterates through each hour of the day
                    if (employee.isAvailable(day, i)) { // Check if the employee
                        timeTable.addEmployee(employee, i); // Adds the employee to the list of employees in the respective hour on the schedule
                    }
                }
            }
        }

        boolean validSchedule = true; // Boolean that checks whether the schedule is valid or not
        for (Day day : daysOfWeek) { // Iterates through each day of the week
            TimeTable timeTable = this.dayTables.get(day); // Retrieves the schedule of the day
            for (int i = 0; i < 24; i++) { // Iterates through each hour of the day
                if (timeTable.getEmployees(i).size() < timeTable.getRequiredEmployees(i)) { // Check if the amount of employees available during that period fits the amount of required employees
                    if (validSchedule) { // Checks if the schedule being valid has already been declared
                        validSchedule = false; // Scheduling algorithm has failed, toggling boolean
                        System.out.println("The algorithm has failed to generate the appropriate schedule!"); // Letting user know of the initial fail
                    }

                    int requiredEmployees = timeTable.getRequiredEmployees(i) - timeTable.getEmployees(i).size(); // Calculates the amount of employees that are needed
                    System.out.println("There are not enough employees in " + day + " @ " + i + ":00! Please hire " + requiredEmployees + " more people!"); // Gives the user an action of suggestion
                }
            }
        }

        if (!validSchedule) { // Check if the schedule has been invalidated
            return; // Stops the algorithm
        }

        WorkbookSettings wbSettings = new WorkbookSettings(); // Declaration of a MS Excel Document Setting
        wbSettings.setLocale(new Locale("en", "EN")); // Sets the Locale to English
        WritableWorkbook workbook = Workbook.createWorkbook(outputFile, wbSettings); // Declares a MS Excel Document based on the settings

        for (Day day : daysOfWeek) { // Iterates through each day of the week
            WritableSheet sheet = workbook.createSheet(day.name(), 0); // Create a sheet on the document based on the day of the week
            TimeTable timeTable = this.dayTables.get(day); // Retrieves the time table of the day of the week
            timeTable.filter(); // Calls a method to sort the employees into their appropriate time slot (sorting algorithm happens here)

            // Prints the X Axis Border onto the Table (Time Slots)
            for (int i = 0; i < 24; i++) { // Iterates through each hour of the day
                Label timeLabel = new Label(0, i + 1, String.format("%2d:00-%2d:00", i, i + 1).replaceAll(" ", "0")); // Creates a label with the correct time formatting
                WritableCellFormat timeFormat = new WritableCellFormat(); // Creates a writable cell
                timeFormat.setBackground(timeTable.getRequiredEmployees(i) == -1 ? Colour.GREY_40_PERCENT : Colour.VERY_LIGHT_YELLOW); // -1 represents if whether the store was closed. If it was, use gray. If not, use yellow.
                timeFormat.setAlignment(Alignment.CENTRE); // Align text to center
                timeFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // Border line of thin black color
                timeLabel.setCellFormat(timeFormat); // Sets the cell formatting to the formatting defined.
                sheet.addCell(timeLabel); // Add the cell onto the spreadsheet
            }

            CellView dateView = sheet.getColumnView(0); // Retrieves the column view of the first column (Time Slots)
            dateView.setSize(4500); // Sets the size to 4500, enough to fit all the time labels nicely
            sheet.setColumnView(0, dateView); // Updates the column with the new view changes

            // Prints the Y Axis Border onto the Table (Employee Names)
            for (int i = 0; i < employees.size(); i++) { // Iterates through each of the employees through the size of the list
                Employee employee = employees.get(i); // Retrieves the employee from the list
                String employeeName = employee.getLastName() + ", " + employee.getFirstName(); // String representation of the employee name for later use

                Label employeeLabel = new Label(1 + i, 0, employeeName); // Create a label based on the employee's name
                WritableCellFormat employeeFormat = new WritableCellFormat();
                employeeFormat.setBackground(employee instanceof Manager ? Colour.LAVENDER : Colour.PALE_BLUE); // If the employee is a manager, color the cell dark pink (lavender). If they are a worker, color it pale blue.
                employeeFormat.setAlignment(Alignment.CENTRE);
                employeeFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
                employeeLabel.setCellFormat(employeeFormat);
                sheet.addCell(employeeLabel);

                CellView cw = sheet.getColumnView(1 + i); // Retrieves the column view of the column the employee is currently in
                cw.setSize((employeeName.length() * 300) + 100); // Set the size of the column based on the length of the employee's name (so it fits nicely)
                sheet.setColumnView(1 + i, cw); // Updates the column with the new view changes
            }

            for (int i = 0; i < 24; i++) { // Iterates through each hour of the day
                List<Employee> availableEmployees = timeTable.getEmployees(i); // Retrieves the employees working in that hour
                for (Employee employee : availableEmployees) { // Iterates through the employees working in that hour
                    Label employeeLabel = new Label(employeeColIndex.get(employee), 1 + i, "Shift"); // Creates a 'Shift' label at the corresponding time slot (x) & employee name (y)
                    WritableCellFormat employeeFormat = new WritableCellFormat();
                    employeeFormat.setBackground(Colour.LIGHT_GREEN); // Colour it light green
                    employeeFormat.setAlignment(Alignment.CENTRE);
                    employeeFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
                    employeeLabel.setCellFormat(employeeFormat);
                    sheet.addCell(employeeLabel);
                }
            }

            for (int i = 0; i < 24; i++) { // Iterates through each hour of the day
                if (timeTable.getRequiredEmployees(i) == -1) { // Check if the required employees is -1 (store closed, default value)
                    for (int x = 0; x < employees.size(); x++) { // Iterates through the size of the employees
                        Label closedLabel = new Label(x + 1, i + 1, ""); // Creates a blank cell as we only need to color it, not text it
                        WritableCellFormat closedFormat = new WritableCellFormat();
                        closedFormat.setBackground(Colour.GREY_25_PERCENT); // Sets the color to gray, bluring it out
                        closedFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
                        closedLabel.setCellFormat(closedFormat);
                        sheet.addCell(closedLabel);
                    }
                }
            }
        }

        workbook.write(); // Write the workbook to the output file
        workbook.close(); // Closes the workbook as we no longer need it

        Desktop.getDesktop().open(outputFile); // Attempts to open the spreadsheet using the default system file
    }

    /**
     * Gets the total hours worked by an employee on the schedule
     * @param employee the employee in question
     * @return integer representation of the hours worked by the employee
     */
    public int getHoursWorked(Employee employee) {
        return this.hoursWorked.get(employee);
    }
}
