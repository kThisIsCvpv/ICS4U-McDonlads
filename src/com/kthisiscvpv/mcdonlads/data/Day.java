package com.kthisiscvpv.mcdonlads.data;

/**
 * Enum variable representing each day of the week and their respective character representation
 * @author Charles
 */
public enum Day {

    MONDAY('M'), TUESDAY('T'), WEDNESDAY('W'), THURSDAY('R'), FRIDAY('F'), SATURDAY('S'), SUNDAY('U');

    private char charRep;

    Day(char charRep) {
        this.charRep = charRep;
    }

    /**
     * Returns the character representation of the day of the week
     * @return the character representation of the day of the week
     */
    public char getCharacter() {
        return this.charRep;
    }

    /**
     * Returns the day of the week from their character representation
     * @param charRep the character representation of the day of the week
     * @return the day of the week of which the character represents, null if it does not represent anything
     */
    public static Day fromChar(char charRep) {
        for (Day day : Day.values()) {
            if (day.getCharacter() == charRep) {
                return day;
            }
        }
        return null;
    }
}
