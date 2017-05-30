package com.kthisiscvpv.mcdonlads.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Schedule object for each day of the week
 * Used to sort employees neatly
 * @author Charles
 */
@SuppressWarnings("unchecked")
public class TimeTable {

    private Day day; // General variable declarations
    private List<Employee> allEmployees;
    private List<Employee>[] hourlyEmployees;
    private int[] requiredEmployees;
    private HashMap<Employee, Integer> hoursWorked;

    /**
     * Main constructor for a timetable
     * @param day day of the week the table represents
     * @param allEmployees a list of all employees to be assigned
     * @param hoursWorked a map of the hours that each employee has worked
     */
    public TimeTable(Day day, List<Employee> allEmployees, HashMap<Employee, Integer> hoursWorked) {
        this.day = day; // General variable assignments
        this.allEmployees = allEmployees;
        this.hoursWorked = hoursWorked;

        this.hourlyEmployees = new List[24]; // Creates a new array for each hour of the day
        for (int i = 0; i < this.hourlyEmployees.length; i++) { // Iterates through each hour of the day
            this.hourlyEmployees[i] = new ArrayList<Employee>(); // Creates a default list for each hour containing the employees that will work in those hours
        }

        this.requiredEmployees = new int[24]; // Creates a new array for each hour of the day
        for (int i = 0; i < this.requiredEmployees.length; i++) { // Iterates through each hour of the day
            this.requiredEmployees[i] = -1; // Defines a default value for each hour of the day to -1, meaning that the store is closed.
        }
    }

    /**
     * Deploy the sorting algorithm with the information set.
     * Employees will be sorted into their respective categories and employees will be removed due to their hourly slots.
     */
    public void filter() {
        int totalHours = 0; // Integer presentation of the total amount of hours accumulated on the schedule
        for (int i = 0; i < this.requiredEmployees.length; i++) { // Iterates through the length of the required employees list
            if (this.requiredEmployees[i] != -1) { // Checks that the store is not closed
                totalHours += this.requiredEmployees[i]; // Adds the required amount of employees onto the total hours number
            }
        }

        List<Employee>[] newSchedule = new List[24]; // Creates a new schedule to work off from (different from the main array) to avoid concurrent modification errors
        for (int i = 0; i < newSchedule.length; i++) { // Iterates through the new array
            newSchedule[i] = new ArrayList<Employee>(); // Defines a blank array for each hour in the new array
        }

        int averageHours = (int) Math.floor(((double) totalHours) / ((double) this.allEmployees.size())); // This number represents the total amount of hours an employee "should" work

        List<Employee> sortedList = new ArrayList<Employee>(this.allEmployees); // Duplicates the original array of employees to prevent concurrent modification errors 
        Collections.sort(sortedList, new Comparator<Employee>() { // Sort the list of employees based on an custom comparator (different from the one in their respective classes)
            @Override
            public int compare(Employee a, Employee b) {
                int totalHoursCompare = Integer.compare(hoursWorked.get(a), hoursWorked.get(b)); // First compare the total hours that each employee has worked (through the entire program, not just this day schedule). We want the workers with less hours to be assigned first.
                if (totalHoursCompare == 0) { // If the total amount of time worked is the same, then it is a useless comparison and we will compare their "versatility". We want the less versatile workers to be assigned first.
                    return Integer.compare(getHoursWorked(a, hourlyEmployees), getHoursWorked(b, hourlyEmployees));
                } else {
                    return totalHoursCompare; // Total hour comparison was different and will be returned.
                }
            }
        });

        // Begin assigning the employee hours based on their versatility comparison done above.
        for (Employee employee : sortedList) { // Iterates through each of the employees in the sorted list. The sorted list tells us which employees should be assigned first.
            int placementHours = getHoursWorked(employee, this.hourlyEmployees); // This variable represents the total amount of hours that the employee can work.
            if (placementHours > averageHours) { // If the total hours that the employee can work is greater than the maximum 'average' hours, we lower the count.
                placementHours = averageHours; // Update the new amount of hours
            }

            for (int hours = 0; hours < placementHours; hours++) { // Iterates through each of the placement hours. We will assign these amount of placement slots to the employee initially.
                for (int i = 0; i < 24; i++) { // We will now find the next available time slot of the employee. Begin by iterating through each day of the week.
                    List<Employee> currentEmployees = newSchedule[i]; // Returns the schedule on that day of the week.
                    if (currentEmployees.size() < this.getRequiredEmployees(i) && !currentEmployees.contains(employee) && employee.isAvailable(this.day, i)) { // Check that the shift is available to the employee and does not break the schedule
                        currentEmployees.add(employee); // Gives the employee the shift
                        this.hoursWorked.put(employee, this.hoursWorked.get(employee) + 1); // Increments the hours the employee has worked
                        break;
                    }
                }
            }
        }

        // Fill in the gaps in the schedule after the employees have been scattered.
        for (int i = 0; i < 24; i++) { // Iterates through each hour of the day
            List<Employee> currentEmployees = newSchedule[i]; // Retrieves the employees working in that hour of the day
            if (currentEmployees.size() < this.getRequiredEmployees(i)) { // Checks whether employees need to be added
                int amountRequired = this.getRequiredEmployees(i) - currentEmployees.size(); // Counts the amount of employees that need to be added
                for (int x = 0; x < amountRequired; x++) { // Iterates through the amount of employees that need to be added
                    for (Employee employee : sortedList) { // Iterate through every employee
                        if (employee.isAvailable(this.day, i) && !currentEmployees.contains(employee)) { // Check if the employee is available during that shift
                            currentEmployees.add(employee); // Give the employee the shift
                            this.hoursWorked.put(employee, this.hoursWorked.get(employee) + 1); // Increments the hours the employee has worked
                            break;
                        }
                    }
                }
            }
        }

        this.hourlyEmployees = newSchedule; // Defines the new updated schedule that was just parsed
    }

    /**
     * Retrieves the amount of hours an employee has worked
     * @param employee the employee in question
     * @param timetable the timetable that we are checking from
     * @return
     */
    public int getHoursWorked(Employee employee, List<Employee>[] timetable) {
        int count = 0; // Variable with the hours count of the employee
        for (int i = 0; i < timetable.length; i++) { // Iterate through each of the slots in the timetable
            List<Employee> list = timetable[i]; // Retrieves the list of employees in the specific timetable slot
            if (list.contains(employee)) { // Check if list contains the employee
                count++; // Increment the count by 1
            }
        }
        return count; // Return the count value
    }

    /**
     * Returns the day of the week the time table represents
     * @return Day of the wee the timetable represents
     */
    public Day getDay() {
        return this.day;
    }

    /**
     * Returns the timetable of all employees in their respective time slots
     * @return Array of Lists of all employees in their time slots
     */
    public List<Employee>[] getEmployees() {
        return this.hourlyEmployees;
    }

    /**
     * Add an employee to the hour of the day
     * @param employee the employee to add
     * @param hour the hour of the day to add them to
     */
    public void addEmployee(Employee employee, int hour) {
        this.hourlyEmployees[hour].add(employee);
    }

    /**
     * Retrieves all the employees working in a specified hour
     * @param hour hour of the day to check
     * @return list of employees that are working in that hour
     */
    public List<Employee> getEmployees(int hour) {
        return this.hourlyEmployees[hour];
    }

    /**
     * Gets the required amount of employees in a specific hour
     * @param hour the specific hour to check
     * @return integer representation of the amount of employees needed
     */
    public int getRequiredEmployees(int hour) {
        return this.requiredEmployees[hour];
    }

    /**
     * Sets the required amount of employees in a specific hour
     * @param hour the specific hour to update
     * @param amount the new amount of employees needed
     */
    public void setRequiredEmployees(int hour, int amount) {
        this.requiredEmployees[hour] = amount;
    }
}