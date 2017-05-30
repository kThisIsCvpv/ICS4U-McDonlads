package com.kthisiscvpv.mcdonlads.data;

import java.util.HashMap;

import org.json.JSONObject;

/**
 * Manager subclass of the Employee superclass for Managers
 * @author Charles
 */
public class Manager extends Employee implements Comparable<Manager> {

    private double yearlyWage; // General variable declaration

    /**
     * General declaration of the Manager subclass in JSON format
     * @param json JSONObject with manager data
     */
    public Manager(JSONObject json) {
        super(json); // Calls the superclass constructor of the JSONObject to declare general entities
        this.yearlyWage = json.getDouble("pay_info"); // Retrieves the manager's salary and declares it
    }

    /**
     * General declaration of the Manager subclass using arguments
     * @param firstName First name of the client
     * @param lastName Last name of the client
     * @param address Address of the client
     * @param employeeNumber Employee number of the client
     * @param yearlyWage Yearly wage of the client
     * @param availability Availability of the client
     */
    public Manager(String firstName, String lastName, String address, int employeeNumber, double yearlyWage, HashMap<Day, boolean[]> availability) {
        super(firstName, lastName, address, employeeNumber, availability); 
        this.yearlyWage = yearlyWage;
    }

    /**
     * Gets the salary of the client given the amount of days worked
     * @param daysWorked integer representation of the amount of days worked
     * @return double of salary with the amount of days worked 
     */
    public double getPay(int daysWorked) {
        return this.yearlyWage / 30 / 12 * daysWorked;
    }

    /**
     * Returns the payment information of the manager, their salary per year
     */
    @Override
    public String getPayInfo() {
        return String.format("%.2f", this.yearlyWage);
    }

    /**
     * Compare one manager to another through their salaries
     */
    @Override
    public int compareTo(Manager manager) {
        return Double.compare(this.yearlyWage, manager.yearlyWage) * -1;
    }
}
