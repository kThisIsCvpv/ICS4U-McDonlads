package com.kthisiscvpv.mcdonlads;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kthisiscvpv.mcdonlads.data.Day;
import com.kthisiscvpv.mcdonlads.data.Employee;
import com.kthisiscvpv.mcdonlads.data.Manager;
import com.kthisiscvpv.mcdonlads.data.ScheduleGen;
import com.kthisiscvpv.mcdonlads.data.Worker;

import jxl.write.WriteException;

/**
 * Main Instance Interface for the McDonlads Scheduling Program
 * @author Charles
 */
public class McDonalds {

    public static final String EMPLOYEES_FILE_PATH = "Employees.txt"; // Relative employees file location

    private Scanner scanner; // System input scanner
    private List<Employee> allEmployees; // List of all employees

    /**
     * Main constructor for the main interface of McDonlads
     */
    public McDonalds() {
        this.allEmployees = new ArrayList<Employee>(); // Defines all the employees that are managed in this console
        this.scanner = new Scanner(System.in); // Defines the system input stream that will be read through the console

        try {
            this.loadEmployeesFile(new File(McDonalds.EMPLOYEES_FILE_PATH)); // Attempts to load the default configuration file, if it exists.
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(); // This should never be called as error checking as null checks are done in the method above.
        }

        mainLoop: while (true) { // Loops through the entire program until user exits.
            System.out.println("Welcome to the McDonlads Interface!"); // Displays the main menu.
            System.out.println("\t1: Add/Edit/Remove Employee (Worker or Manager)");
            System.out.println("\t2: List All Employees");
            System.out.println("\t3: Display Specific Employee Information");
            System.out.println("\t4: Run and Display Scheduling Algorithm");
            System.out.println("\t5: Load New Employees Data File");
            System.out.println("\t6: Exit");
            System.out.print("Please select an option: ");

            int selectionNumber = this.retrieveNumberInput(1, 6, "You have entered an invalid number! Please try again: ");

            if (selectionNumber == 1) { // User wants to manage employees. Shows them the user interface for doing so.
                System.out.println("Welcome to the McDonlads Worker Interface!");
                System.out.println("\t1: Add a Brand New Employee");
                System.out.println("\t2: Remove an Existing Employee");
                System.out.println("\t3: Edit an Existing Employee's Information");
                System.out.print("Please select an option: ");

                selectionNumber = this.retrieveNumberInput(1, 3, "You have entered an invalid number! Please try again: ");

                if (selectionNumber == 1) { // User wants to add an employee. Ask them a bunch of questions regarding the employee.
                    System.out.print("Is the Following Worker a Manager? (Yes/No): ");
                    boolean isManager = this.retrieveYesNoAnswer("You have entered an invalid answer! Please try again: ");

                    System.out.print("Please Enter the Worker's Employee Number: ");
                    int employeeNumber = this.retrieveNumberInput("You have entered an invalid employee number! Please try again: ");

                    System.out.print("Please Enter the Worker's First Name: ");
                    String firstName = this.retrieveNotEmptyString("You have entered an invalid first name! Please try again: ");

                    System.out.print("Please Enter the Worker's Last Name: ");
                    String lastName = this.retrieveNotEmptyString("You have entered an invalid last name! Please try again: ");

                    System.out.print("Please Enter the Worker's Address: ");
                    String address = this.retrieveNotEmptyString("You have entered an invalid address! Please try again: ");

                    System.out.print("Please Enter the Worker's Wage: ");
                    double hourlyWage = this.retrieveDoubleInput("You have entered an invalid wage! Please try again: ");

                    HashMap<Day, boolean[]> availability = new HashMap<Day, boolean[]>();
                    for (Day day : Day.values()) { // Iterates through each day of the week & ask the user for the schedule of the employee on that specific day with 24 hour ranges
                        System.out.print("Please Enter the Worker's Available Work Hours on " + day.toString() + "(" + day.getCharacter() + ") (0hr -> 24hr): ");
                        String workHours = scanner.nextLine();
                        if (workHours.trim().length() == 0) { // Check that the input was valid. If the employee does not work that day, this will be null.
                            continue;
                        }

                        String[] hoursSplit = workHours.split(" "); // Split the values based on a space
                        boolean[] available = new boolean[24]; // Array containing boolean representations of 24 hours of the day

                        for (String split : hoursSplit) { // Analyze the values that were split above
                            if (split.contains("-")) { // Check if the value contains a 'to' sign
                                String[] innerSplit = split.split("-"); // Split the 'to' sign to analyze start and end times
                                try {
                                    int start = Integer.parseInt(innerSplit[0].contains(":") ? innerSplit[0].split(":")[0] : innerSplit[0]); // We only care about the hour, thus the minute hand is ignored. This analyzes the starting hour.
                                    int end = Integer.parseInt(innerSplit[1].contains(":") ? innerSplit[1].split(":")[0] : innerSplit[1]); // Same reason as above. This analyzes the ending hour.
                                    for (int i = start; i < end; i++) { // Iterates through the start and ending hours.
                                        available[i] = true; // Sets the value of the boolean array in that specific hour to true.
                                    }
                                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) { // Catch a malfunctioned timing format in the split identification.
                                    System.out.println("Unable to Identify Time Frame " + split + ". It has not been added to the scheduling."); // Notifies the user so they can make changes later on.
                                }
                            } else { // Input does not contain a 'to' sign. A single hour was given. This will assume a 1 hour grace period (Ex. 3:00 -> 4:00)
                                try {
                                    int hourParsed = Integer.parseInt(split.contains(":") ? split.split(":")[0] : split); // Parses the hour that was given.
                                    available[hourParsed] = true; // Updates the value in the boolean array
                                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) { // Catch a malfunctioned timing format in the split identification.
                                    System.out.println("Unable to Identify Time Frame " + split + ". It has not been added to the scheduling."); // Notifies the user so they can make changes later on.
                                }
                            }
                        }

                        availability.put(day, available); // Puts the availability information of the client on that specific date to the list above.
                    }

                    // Decide on which constructor to used based on whether the employee is a manager or not.
                    Employee employee = isManager ? new Manager(firstName, lastName, address, employeeNumber, hourlyWage, availability) : new Worker(firstName, lastName, address, employeeNumber, hourlyWage, availability);
                    this.allEmployees.add(employee); // Adds the employee to the list
                    this.sortEmployees(); // Sort the list after it has been added

                    try {
                        this.saveEmployeesFile(new File(McDonalds.EMPLOYEES_FILE_PATH)); // Updates the employee list file
                        System.out.println("You have Successfully Added " + employee.getLastName() + ", " + employee.getFirstName() + " to the Workplace!\n");
                    } catch (IOException e) { // An error has occurred while trying to update the file
                        System.out.println("An Error has Occured While Trying to Save the Employee File!");
                        e.printStackTrace();
                    }
                } else if (selectionNumber == 2) { // User wants to remove an employee
                    if (this.allEmployees.isEmpty()) { // Checks if whether there are any active employees or not
                        System.out.println("There are currently no employees to remove!");
                        continue mainLoop;
                    } else {
                        for (int i = 0; i < this.allEmployees.size(); i++) { // Iterates through all the employees and give them an identification tag
                            Employee employee = this.allEmployees.get(i);
                            System.out.println("[Employee Index #" + (i + 1) + "] " + employee.getLastName() + ", " + employee.getFirstName() + ((employee instanceof Manager) ? " (Manager)" : " (Worker)"));
                        }

                        System.out.println();
                        System.out.print("Please Enter the Worker's Index Number to Remove (0 to Cancel): "); // Ask the user to input the number of the employee they wish to remove
                        int employeeNumber = this.retrieveNumberInput(0, this.allEmployees.size(), "You have entered an invalid employee number! Please try again: ");

                        employeeNumber--; // Since we asked them for a number starting from 1, we subtract 1 since java starts at 0.
                        if (employeeNumber == -1) { // Check if the input was valid or if they just wanted to cancel.
                            continue mainLoop; // Continues to the main loop.
                        } else {
                            Employee employee = this.allEmployees.get(employeeNumber); // Retrieves the employee they wish to remove
                            this.allEmployees.remove(employee); // Remove that employee
                            try {
                                this.saveEmployeesFile(new File(McDonalds.EMPLOYEES_FILE_PATH)); // Updates the employee list file
                                System.out.println("You have Successfully Removed " + employee.getLastName() + ", " + employee.getFirstName() + ((employee instanceof Manager) ? " (Manager)" : " (Worker)") + " from the Workplace!\n");
                            } catch (IOException e) { // An error has occurred while trying to update the file
                                System.out.println("An Error has Occured While Trying to Save the Employee File!");
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (selectionNumber == 3) { // User wants to edit an employee
                    if (this.allEmployees.isEmpty()) { // Check if whether there are employees to edit
                        System.out.println("There are currently no employees to edit!");
                        continue mainLoop;
                    } else {
                        for (int i = 0; i < this.allEmployees.size(); i++) { // Iterates through all the employees and give them an identification tag
                            Employee employee = this.allEmployees.get(i);
                            System.out.println("[Employee Index #" + (i + 1) + "] " + employee.getLastName() + ", " + employee.getFirstName() + ((employee instanceof Manager) ? " (Manager)" : " (Worker)"));
                        }

                        System.out.println();
                        System.out.print("Please Enter the Worker's Index Number to Edit (0 to Cancel): "); // Ask the user to input the number of the employee they wish to edit
                        int employeeNumber = this.retrieveNumberInput(0, this.allEmployees.size(), "You have entered an invalid employee number! Please try again: ");

                        employeeNumber--; // Since we asked them for a number starting from 1, we subtract 1 since java starts at 0.
                        if (employeeNumber == -1) { // Retrieves the employee they wish to remove
                            continue mainLoop; // Continues to the main loop.
                        } else {
                            Employee employee = this.allEmployees.get(employeeNumber); // Retrieves the employee they wish to remove

                            System.out.println("Welcome to the McDonlads Worker Editor!"); // Shows a menu of all the possible edits on that specific employee
                            System.out.println("\t1: Change Employee's First Name");
                            System.out.println("\t2: Change Employee's Last Name");
                            System.out.println("\t3: Change Employee's Address");
                            System.out.println("\t4: Change Employee's Employee Number");
                            System.out.println("\t5: Change Employee's Schedule");
                            System.out.println("\t6: Exit");
                            System.out.print("Please select an option: ");

                            selectionNumber = this.retrieveNumberInput(1, 6, "You have entered an invalid number! Please try again: "); // Asks the user which option they would like to edit.

                            if (selectionNumber == 1) { // Gives the user input requests based on the option that they've selected.
                                System.out.print("Please Enter the Worker's First Name: ");
                                String firstName = this.retrieveNotEmptyString("You have entered an invalid first name! Please try again: ");
                                employee.setFirstName(firstName);
                            } else if (selectionNumber == 2) {
                                System.out.print("Please Enter the Worker's Last Name: ");
                                String lastName = this.retrieveNotEmptyString("You have entered an invalid last name! Please try again: ");
                                employee.setLastName(lastName);
                            } else if (selectionNumber == 3) {
                                System.out.print("Please Enter the Worker's Address: ");
                                String address = this.retrieveNotEmptyString("You have entered an invalid address! Please try again: ");
                                employee.setAddress(address);
                            } else if (selectionNumber == 4) {
                                System.out.print("Please Enter the Worker's Employee Number: ");
                                int newNumber = this.retrieveNumberInput("You have entered an invalid employee number! Please try again: ");
                                employee.setEmployeeNumber(newNumber);
                            } else if (selectionNumber == 5) {
                                HashMap<Day, boolean[]> availability = new HashMap<Day, boolean[]>();
                                for (Day day : Day.values()) { // Iterates through each day of the week & ask the user for the schedule of the employee on that specific day with 24 hour ranges
                                    System.out.print("Please Enter the Worker's Available Work Hours on " + day.toString() + "(" + day.getCharacter() + ") (0hr -> 24hr): ");
                                    String workHours = scanner.nextLine();
                                    if (workHours.trim().length() == 0) { // Check that the input was valid. If the employee does not work that day, this will be null.
                                        continue;
                                    }

                                    String[] hoursSplit = workHours.split(" "); // Split the values based on a space
                                    boolean[] available = new boolean[24]; // Array containing boolean representations of 24 hours of the day

                                    for (String split : hoursSplit) { // Analyze the values that were split above
                                        if (split.contains("-")) { // Check if the value contains a 'to' sign
                                            String[] innerSplit = split.split("-"); // Split the 'to' sign to analyze start and end times
                                            try {
                                                int start = Integer.parseInt(innerSplit[0].contains(":") ? innerSplit[0].split(":")[0] : innerSplit[0]); // We only care about the hour, thus the minute hand is ignored. This analyzes the starting hour.
                                                int end = Integer.parseInt(innerSplit[1].contains(":") ? innerSplit[1].split(":")[0] : innerSplit[1]); // Same reason as above. This analyzes the ending hour.
                                                for (int i = start; i < end; i++) { // Iterates through the start and ending hours.
                                                    available[i] = true; // Sets the value of the boolean array in that specific hour to true.
                                                }
                                            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) { // Catch a malfunctioned timing format in the split identification.
                                                System.out.println("Unable to Identify Time Frame " + split + ". It has not been added to the scheduling."); // Notifies the user so they can make changes later on.
                                            }
                                        } else { // Input does not contain a 'to' sign. A single hour was given. This will assume a 1 hour grace period (Ex. 3:00 -> 4:00)
                                            try {
                                                int hourParsed = Integer.parseInt(split.contains(":") ? split.split(":")[0] : split); // Parses the hour that was given.
                                                available[hourParsed] = true; // Updates the value in the boolean array
                                            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) { // Catch a malfunctioned timing format in the split identification.
                                                System.out.println("Unable to Identify Time Frame " + split + ". It has not been added to the scheduling."); // Notifies the user so they can make changes later on.
                                            }
                                        }
                                    }

                                    availability.put(day, available); // Puts the availability information of the client on that specific date to the list above.
                                }

                                employee.setWeeklySchedule(availability); // Updates the employee's weekly schedule based on the new availability timetable.
                            }
                            try {
                                this.saveEmployeesFile(new File(McDonalds.EMPLOYEES_FILE_PATH)); // Updates the employee list file
                                System.out.println("You have Successfully Updated " + employee.getLastName() + ", " + employee.getFirstName() + ((employee instanceof Manager) ? " (Manager)" : " (Worker)") + "'s Data in the Workplace!\n");
                            } catch (IOException e) { // An error has occurred while trying to update the file
                                System.out.println("An Error has Occured While Trying to Save the Employee File!");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else if (selectionNumber == 2) { // User wants to list all employees
                List<Manager> managers = this.getAllManagers(); // Get all the managers
                System.out.println("List of Managers (" + managers.size() + " entries): "); // Title for all the managers as well as the entry count
                for (int i = 0; i < managers.size(); i++) { // Iterates through the manager list
                    Manager manager = managers.get(i); // Get the manager at the specific slot and print it (it's already sorted in employee number order)
                    System.out.println("\t#" + (i + 1) + ": " + manager.getLastName() + ", " + manager.getFirstName() + " ($" + manager.getPayInfo() + "/year)");
                }
                System.out.println(); // Spacer

                List<Worker> workers = this.getAllWorkers(); // Get all the workers
                System.out.println("List of Worker (" + workers.size() + " entries): "); // Title for all the workers as well as the entry count
                for (int i = 0; i < workers.size(); i++) { // Iterates through the workers list
                    Worker worker = workers.get(i); // Get the workers at the specific slot and print it (it's already sorted in employee number order)
                    System.out.println("\t#" + (i + 1) + ": " + worker.getLastName() + ", " + worker.getFirstName() + " ($" + worker.getPayInfo() + "/hour)");
                }
                System.out.println(); // Spacer
            } else if (selectionNumber == 3) { // Users wants specific information on one employee
                for (int i = 0; i < this.allEmployees.size(); i++) { // Iterates through all the employees
                    Employee employee = this.allEmployees.get(i); // Assign each employee with a specific index number
                    System.out.println("[Employee Index #" + (i + 1) + "] " + employee.getLastName() + ", " + employee.getFirstName() + ((employee instanceof Manager) ? " (Manager)" : " (Worker)"));
                }

                System.out.println(); // Asks the user for the index number of the specific employee they would like to edit
                System.out.print("Please Enter the Worker's Index Number to View (0 to Cancel): ");
                int employeeNumber = this.retrieveNumberInput(0, this.allEmployees.size(), "You have entered an invalid employee number! Please try again: ");

                employeeNumber--; // Since we started to ask at 1, we minus 1 to go back to 0
                if (employeeNumber == -1) { // Check if input was valid if they just wanted to cancel
                    continue mainLoop; // Continues back to the main loop
                } else {
                    Employee employee = this.allEmployees.get(employeeNumber); // Retrieves the employee they wish to see
                    boolean isManager = employee instanceof Manager; // Check if whether the employees was a manager
                    System.out.println("Displaying Information on Requested Employee:"); // Displays the employee's information
                    System.out.println("\tEmployee ID: " + employee.getEmployeeNumber());
                    System.out.println("\tFirst Name: " + employee.getFirstName());
                    System.out.println("\tLast Name: " + employee.getLastName());
                    System.out.println("\tPosition: " + (isManager ? "Manager" : "Worker"));
                    System.out.println("\tAddress: " + employee.getAddress());
                    System.out.printf("\tEmployee Wage: $%.2f" + (isManager ? "/year" : "/hour") + "\n\n", Double.parseDouble(employee.getPayInfo()));
                }
            } else if (selectionNumber == 4) { // Users wishes to create a schedule
                System.out.print("Please enter the File Location of the Schedule Input File: "); // Asks the user for the location of the schedule input file
                String inputLoc = this.scanner.nextLine(); // Retrieves the input
                File inputFile = new File(inputLoc); // Defines the file at the input location
                try {
                    ScheduleGen generator = new ScheduleGen(inputFile, this.allEmployees); // Attempts to generate a schedule based on the file
                    System.out.println(); // Schedule was successfully generated! Displays the wage information of each employee based on the schedule
                    System.out.println("Employee Wage Information: "); // Title
                    for (Employee employee : this.allEmployees) { // Iterates through each of the employees
                        if (employee instanceof Manager) { // Check if the employee is a manger (as their output will be different)
                            System.out.println("\t[Employee ID #" + employee.getEmployeeNumber() + " - Manager] " + employee.getLastName() + ", " + employee.getFirstName() + " -> Works for " + generator.getHoursWorked(employee) + " hours | Wage: $" + employee.getPayInfo() + "/year");
                        } else { // Employee is a worker
                            double wage = Double.parseDouble(employee.getPayInfo()); // Parse their payment information
                            double salary = wage * generator.getHoursWorked(employee); // Calculate their salary based on hours worked
                            String salaryFormat = String.format("%.2f", salary); // Format their salary into a string & then print it out
                            System.out.println("\t[Employee ID #" + employee.getEmployeeNumber() + " - Worker] " + employee.getLastName() + ", " + employee.getFirstName() + " -> Works for " + generator.getHoursWorked(employee) + " hours | Wage: $" + employee.getPayInfo() + "/hour | Salary: $" + salaryFormat);

                        }
                    }
                } catch (WriteException | IOException e) { // Schedule generation has failed
                    System.out.println("An Error has Occured While Trying to Generate the Employee Schedule File!"); // An error has occurred while trying to generate the schedule, probably caused by the user with their terrible input :(
                    e.printStackTrace();
                }

                System.out.println();
            } else if (selectionNumber == 5) { // User wants to load another existing employees file into the program (which will override any existing ones)
                System.out.print("Please enter the File Location of the Input File: "); // Asks the user for the location of the input file
                String inputLoc = this.scanner.nextLine(); // Retrieves the user input
                File inputFile = new File(inputLoc); // Declares the file at the location of the input file
                if (inputFile == null || !inputFile.exists()) { // Check if the file exists and is not null
                    System.out.println("The Specified File does not exist!"); // Tells the user that the file exists
                } else {
                    try { // Tries to load the file
                        this.loadEmployeesFile(inputFile); // Load the input file into the system
                        this.saveEmployeesFile(new File(McDonalds.EMPLOYEES_FILE_PATH)); // Save the input file into the original save path
                        System.out.println("The Employees List has been Successfully Updated with the New Configurations!");
                    } catch (IOException e) { // Something went wrong while trying to read the new input file
                        System.out.println("An Error has Occured While Trying to Load the Employee File!");
                        e.printStackTrace();
                    }
                }
                System.out.println(); // Spacer 
            } else if (selectionNumber == 6) { // Users wants to close the program
                System.out.println("Goodbye! Closing McDonlads Interface...");
                break mainLoop; // Break the main loop
            }
        }

        scanner.close();
    }

    /**
     * Retrieves a number input from a the system scanner with min and max ranges
     * @param minimum minimum range
     * @param maximum maximum range
     * @param errorMessage error message if input is incorrect
     * @return the integer that the user has given
     */
    public int retrieveNumberInput(int minimum, int maximum, String errorMessage) {
        while (true) { // Iterates as long as the user does not give a valid response
            String input = this.scanner.nextLine(); // Retrieves the next line

            int selectionNumber;
            try { // Initial check -> makes sure that the input was a valid number
                selectionNumber = Integer.parseInt(input);
            } catch (NumberFormatException numError) {
                System.out.print(errorMessage); // Shows an error message if the number could not be parsed
                continue;
            }

            if (selectionNumber < minimum || selectionNumber > maximum) { // Checks that the number given falls within the min and max range
                System.out.print(errorMessage); // Gives an error message
                continue;
            } else {
                System.out.println(); // Prints a spacer
                return selectionNumber; // Returns the number given
            }
        }
    }

    /**
     * Retrieves a number input from a the system scanner
     * @param errorMessage error message if input is incorrect
     * @return integer of the response
     */
    public int retrieveNumberInput(String errorMessage) {
        while (true) {
            String input = this.scanner.nextLine();

            try {
                int selectionNumber = Integer.parseInt(input);
                System.out.println();
                return selectionNumber;
            } catch (NumberFormatException numError) {
                System.out.print(errorMessage);
                continue;
            }
        }
    }

    /**
     * Retrieves a double input from a the system scanner
     * @param errorMessage error message if input is incorrect
     * @return double of the reponse
     */
    public double retrieveDoubleInput(String errorMessage) {
        while (true) {
            String input = this.scanner.nextLine();

            try {
                double selectionNumber = Double.parseDouble(input);
                System.out.println();
                return selectionNumber;
            } catch (NumberFormatException numError) {
                System.out.print(errorMessage);
                continue;
            }
        }
    }

    /**
     * Retrieves a yes or no response from a the system scanner
     * @param errorMessage error message if input is incorrect
     * @return boolean representing yes or no
     */
    public boolean retrieveYesNoAnswer(String errorMessage) {
        while (true) {
            String input = this.scanner.nextLine();
            if (!input.equalsIgnoreCase("yes") && !input.equalsIgnoreCase("no")) {
                System.out.print(errorMessage);
                continue;
            } else {
                System.out.println();
                return input.equalsIgnoreCase("yes");
            }
        }
    }

    /**
     * Retrieves a non-empty string input from a the system scanner
     * @param errorMessage error message if input is incorrect
     * @return a non-empty string
     */
    public String retrieveNotEmptyString(String errorMessage) {
        while (true) {
            String input = this.scanner.nextLine();
            if (input.trim().length() == 0) {
                System.out.print(errorMessage);
                continue;
            } else {
                System.out.println();
                return input;
            }
        }
    }

    /**
     * Load the employee data from an input file
     * @param file the input file
     * @throws FileNotFoundException the file does not exist or something went wrong
     */
    public void loadEmployeesFile(File file) throws FileNotFoundException {
        try {
            this.allEmployees.clear(); // Clears the current employee list
            if (!file.exists()) { // Check that the file does not exist
                return; // Stop this method as nothing will be read
            }

            StringBuilder rawJSON = new StringBuilder(); // StringBuilder instance to append all the information in the file to
            Scanner scanner = new Scanner(file); // Scan the file
            while (scanner.hasNextLine()) { // Iterates as long as the scanner has another thing to read
                rawJSON.append(scanner.nextLine()); // Append the next line to the string builder instance
            }
            scanner.close(); // Closes the scanner

            if (rawJSON.toString().length() <= 2) { // Checks that the file that is valid by doing a simplistic length check ('[]' -> JSONObject, length 2)
                return; // Stop this method as nothing will be read
            }

            JSONObject mainBracket = new JSONObject(rawJSON.toString()); // Converts the data to a JSONObject

            JSONArray managers = mainBracket.getJSONArray("managers_info"); // Retrieves the array of managers from the JSONObject
            for (int i = 0; i < managers.length(); i++) { // Iterates through the array of managers
                this.allEmployees.add(new Manager(managers.getJSONObject(i))); // Append the managers to the list of employees
            }

            JSONArray workers = mainBracket.getJSONArray("workers_info"); // Retrieves the array of workers from the JSONObject
            for (int i = 0; i < workers.length(); i++) { // Iterates through the array of workers
                this.allEmployees.add(new Worker(workers.getJSONObject(i))); // Append the workers to the list of employees
            }

            this.sortEmployees(); // Sort the new list of employees
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // Something that shouldn't have gone wrong, went wrong.
        }
    }

    /**
     * Save the employee data into a file
     * @param file the output file
     * @throws IOException file writing error has occured
     */
    public void saveEmployeesFile(File file) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(file)); // PrintWriter for the output file

        JSONObject divider = new JSONObject(); // Data will be stored in JSON. This JSONObject will placehold both managers and workers.

        JSONArray managers = new JSONArray(); // JSONArray declaration to store all the manager data
        for (Manager manager : this.getAllManagers()) { // Iterates through all the managers
            managers.put(manager.toJSON()); // Append the JSONArray with the manager's data
        }
        divider.put("managers_info", managers); // Append the JSONArray to the main placeholder

        JSONArray workers = new JSONArray(); // JSONArray declaration to store all the worker'es data
        for (Worker worker : this.getAllWorkers()) { // Iterates through all the workers
            workers.put(worker.toJSON()); // Append the JSONArray with the worker's data
        }
        divider.put("workers_info", workers); // Append the JSONArray to the main placeholder

        pw.println(divider.toString(4)); // Prints the placeholder onto the file using an indentation of 4 spaces
        pw.close(); // closes the print writer
        this.sortEmployees(); // plain old force (safeguard) sorting after every input / output stream closes
    }

    /**
     * Sort the everchanging list of employees in this object based on their employee number (instead of their wages in their default superclass)
     */
    public void sortEmployees() {
        Collections.sort(this.allEmployees, new Comparator<Employee>() { // Calls the sort method with a new comparator
            @Override
            public int compare(Employee a, Employee b) {
                return Integer.compare(a.getEmployeeNumber(), b.getEmployeeNumber()); // Compare and return the client's employee numbers, lowest to highest.
            }
        });
    }

    /**
     * Gets all the employees (manager & workers) in the interface
     * @return List of all the employees (manager & workers) in the interface
     */
    public List<Employee> getAllEmployees() {
        return this.allEmployees;
    }

    /**
     * Gets all the managers in the interface
     * @return List of all the managers in the interface
     */
    public List<Manager> getAllManagers() {
        List<Manager> managers = new ArrayList<Manager>(); // Blank array list to append all the managers to
        for (Employee employee : this.getAllEmployees()) { // Get all the employees in the interface
            if (employee instanceof Manager) { // Check if they are a manager
                managers.add((Manager) employee); // Polymorph the employee object into a manager
            }
        }

        Collections.sort(managers); // Sort the list
        return managers; // Return the list
    }

    /**
     * Gets all the workers in the interface
     * @return List of all the workers in the interface
     */
    public List<Worker> getAllWorkers() {
        List<Worker> workers = new ArrayList<Worker>(); // Blank array list to append all the workers to
        for (Employee employee : this.getAllEmployees()) { // Get all the employees in the interface
            if (employee instanceof Worker) { // Check if they are a worker
                workers.add((Worker) employee); // Polymorph the employee object into a worker
            }
        }

        Collections.sort(workers); // Sort the list
        return workers; // Return the list
    }
}
