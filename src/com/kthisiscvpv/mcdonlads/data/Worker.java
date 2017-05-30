package com.kthisiscvpv.mcdonlads.data;

import java.util.HashMap;

import org.json.JSONObject;

/**
 * Manager subclass of the Employee superclass for Workers
 * @author Charles
 */
public class Worker extends Employee implements Comparable<Worker> {

    private double hourlyWage; // General variable declaration

    /**
     * General declaration of the Employee subclass in JSON format
     * @param json JSONObject with employee data
     */
    public Worker(JSONObject json) {
        super(json); // Calls the superclass constructor of the JSONObject to declare general entities
        this.hourlyWage = json.getDouble("pay_info"); // Retrieves the worker's wahe and declares it
    }

    /**
     * General declaration of the Employee subclass using arguments
     * @param firstName First name of the client
     * @param lastName Last name of the client
     * @param address Address of the client
     * @param employeeNumber Employee number of the client
     * @param hourly hourly wage of the client
     * @param availability Availability of the client
     */
    public Worker(String firstName, String lastName, String address, int employeeNumber, double hourlyWage, HashMap<Day, boolean[]> availability) {
        super(firstName, lastName, address, employeeNumber, availability);
        this.hourlyWage = hourlyWage;
    }

    /**
     * Gets the salary of the client given the amount of hours worked
     * @param hoursWorked integer representation of the amount of hours worked
     * @return double of salary with the amount of hours worked
     */
    public double getPay(int hoursWorked) {
        return this.hourlyWage * hoursWorked;
    }

    /**
     * Returns the payment information of the employee, their salary per hour
     */
    @Override
    public String getPayInfo() {
        return String.format("%.2f", this.hourlyWage);
    }

    /**
     * Compare one employee to another through their wages
     */
    @Override
    public int compareTo(Worker worker) {
        return Double.compare(this.hourlyWage, worker.hourlyWage) * -1;
    }
}
