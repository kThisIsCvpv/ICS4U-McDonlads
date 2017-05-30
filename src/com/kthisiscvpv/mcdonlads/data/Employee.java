package com.kthisiscvpv.mcdonlads.data;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Superclass for all employees
 * @author Charles
 */
@SuppressWarnings("serial")
public abstract class Employee {

    private String firstName; // General variable declarations
    private String lastName;
    private String address;
    private int employeeNumber;
    private Map<Day, boolean[]> availability;

    /**
     * JSONObject Initialization of an Employee Superclass
     * @param json JSON string in JSONObject format
     */
    public Employee(JSONObject json) {
        this.firstName = json.getString("first_name"); // Declares and retrieves the variables from the json
        this.lastName = json.getString("last_name");
        this.address = json.getString("address");
        this.employeeNumber = json.getInt("employee_number");

        this.availability = new HashMap<Day, boolean[]>(); // Initializes a new schedule availability class
        JSONArray jsonSchedule = json.getJSONArray("schedule"); // Retrieves the schedule from the JSON data
        for (int i = 0; i < jsonSchedule.length(); i++) { // Iterates through the data elements
            JSONObject daySchedule = jsonSchedule.getJSONObject(i); // Subsplit the data into their respective days of the week
            Day day = Day.valueOf(daySchedule.getString("day")); // Identify the day
            boolean[] availability = new boolean[24]; // Array holds the client availability for each hour of the day
            for (int a = 0; a < availability.length; a++) { // Check client availability
                availability[a] = daySchedule.getBoolean(Integer.toString(a)); // Defines client availability
            }
            this.availability.put(day, availability); // Adds it to the client map
        }
    }

    /**
     * Argument Initialization of the Employee Superclass
     * @param firstName First name of the client
     * @param lastName Last name of the client
     * @param address Address of the client
     * @param employeeNumber Employee number of the client
     */
    public Employee(String firstName, String lastName, String address, int employeeNumber) {
        this(firstName, lastName, address, employeeNumber, new HashMap<Day, boolean[]>() { // Calls a method overload below
            {
                for (Day day : Day.values()) { // Default values are given to all days in the the scheduling map to avoid null conflicts.
                    this.put(day, new boolean[24]);
                }
            }
        });
    }

    /**
     * Argument Initialization of the Employee Superclass
     * @param firstName First name of the client
     * @param lastName Last name of the client
     * @param address Address of the client
     * @param employeeNumber Employee number of the client
     * @param availability Availability of the client each day
     */
    public Employee(String firstName, String lastName, String address, int employeeNumber, Map<Day, boolean[]> availability) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.employeeNumber = employeeNumber;
        this.availability = availability;
    }

    /**
     * Get the wage/salary of the client based on their respective inheritance sub classes
     * @return Wage/salary of the client in String format
     */
    public abstract String getPayInfo();

    /**
     * First Name of the Client
     * @return First Name of the Client
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Update First Name of the Client
     * @param firstName First Name of the Client
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Last Name of the Client
     * @return Last Name of the Client
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Update Last Name of the Client
     * @param lastName Last Name of the Client
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Full Name of the Client
     * @return Full Name of the Client
     * @deprecated Not for serious use due to multiple name splits (Ex. Charles Kevin Xu)
     */
    @Deprecated
    public String getName() {
        return this.firstName + " " + this.lastName;
    }

    /**
     * Address of the Client
     * @return Address of the Client
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Update Address of the Client
     * @param address Address of the Client
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Employee Number of the Client
     * @return Employee Number of the Client
     */
    public int getEmployeeNumber() {
        return this.employeeNumber;
    }

    /**
     * Update Employee Number of the Client
     * @param employeeNumber Employee Number of the Client
     */
    public void setEmployeeNumber(int employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    /**
     * Weekly Schedule of the Client
     * @return Weekly Schedule of the Client
     */
    public Map<Day, boolean[]> getWeeklySchedule() {
        return this.availability;
    }
    
    /**
     * Update Weekly Schedule of the Client
     * @param availability Weekly Schedule of the Client
     */
    public void setWeeklySchedule(Map<Day, boolean[]> availability) {
        this.availability = availability;
    }

    /**
     * Daily Schedule of the Client
     * @param day Day of the Week
     * @return Daily Schedule of the Client
     */
    public boolean[] getSchedule(Day day) {
        return this.availability.get(day);
    }

    /**
     * Sets the Work Availability of the Client on the Day of the Hour
     * @param day Day of the Week
     * @param hour Hour of the Day
     * @param available boolean value of availability
     */
    public void setAvailability(Day day, int hour, boolean available) {
        boolean[] daySchedule = this.availability.get(day);
        daySchedule[hour] = available;
        this.availability.put(day, daySchedule);
    }

    /**
     * Checks for Work Availability of the Client on the Day of the Hour
     * @param day Day of the Week
     * @param hour Hour of the Day
     * @return boolean value of availability
     */
    public boolean isAvailable(Day day, int hour) {
        return this.availability.get(day)[hour];
    }

    /**
     * Converts the Employee Data to a JSONObject for Safe String Data Transfers
     * @return JSONOBject containing all the data in the employee list
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject(); // Declares a new JSONObject to build off of

        json.put("first_name", this.firstName); // Build the default simple variables into the JSONObject declaration
        json.put("last_name", this.lastName);
        json.put("address", this.address);
        json.put("employee_number", this.employeeNumber);
        json.put("pay_info", this.getPayInfo());

        JSONArray jsonSchedule = new JSONArray(); // Declares a JSONArray to build the schedule of each day of the week
        for (Day day : Day.values()) { // Iterates through each day of the week
            JSONObject daySchedule = new JSONObject(); // Declares a JSONObject to build the hours of each hour of the day
            daySchedule.put("day", day.toString()); // Inputs the day of the week into the JSONObject declaration

            boolean[] dayAvailability = this.availability.get(day); // Gets the client availability of the hours of the day
            for (int i = 0; i < dayAvailability.length; i++) { // Iterates through the availability of the client
                daySchedule.put(Integer.toString(i), Boolean.toString(dayAvailability[i])); // Inputs the client's information into the JSONObject
            }

            jsonSchedule.put(daySchedule); // Inputs the JSONObject day schedule into the JSONArray main schedule
        }
        json.put("schedule", jsonSchedule); // Add the JSONArray main schedule into the main JSONObject instance

        return json; // Return the main JSONObject instance
    }

    /**
     * Overrides the Object method toString() to return the JSONObject representation of the string instead.
     */
    @Override
    public String toString() {
        return this.toJSON().toString(4); // Return the JSONObject of the string in string format indented with 4 spaces.
    }
}
