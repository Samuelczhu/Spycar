package com.elec3907.spycar;

import androidx.annotation.Nullable;

/**
 * This class defined the encoded direction
 * The direction is encoded in a String with 2 index.
 * The first index indicate front (f), back (b), or stop (s)
 * The second index indicate left(l), right(r), or stop (s)
 */
public class Direction {
    //constants for direction
    public static String FRONT = "f";
    public static String BACK = "b";
    public static String LEFT = "l";
    public static String RIGHT = "r";
    public static String STOP = "s";

    private String frontBack; //hold the direction for front back: f, b, s
    private String leftRight; //hold the direction for left right: l, r, s

    /**
     * Create a direction with stop
     */
    public Direction() {
        this.frontBack = STOP; //stop in front back direction
        this.leftRight = STOP; //stop in left right direction
    }

    /**
     * Method to go front and return the resulting direction String with two character
     * @return Direction String
     */
    public String goFront() {
        frontBack = FRONT;
        return this.frontBack + this.leftRight;
    }
    /**
     * Method to go back and return the resulting direction String with two character
     * @return Direction String
     */
    public String goBack() {
        frontBack = BACK;
        return this.frontBack + this.leftRight;
    }
    /**
     * Method to stop in front back direction and return the resulting direction String with two character
     * @return Direction String
     */
    public String stopFrontBack() {
        frontBack = STOP;
        return this.frontBack + this.leftRight;
    }

    /**
     * Method to go left and return the resulting direction String with two character
     * @return Direction String
     */
    public String goLeft() {
        leftRight = LEFT;
        return this.frontBack + this.leftRight;
    }
    /**
     * Method to go right and return the resulting direction String with two character
     * @return Direction String
     */
    public String goRight() {
        leftRight = RIGHT;
        return this.frontBack + this.leftRight;
    }
    /**
     * Method to stop in left right direction and return the resulting direction String with two character
     * @return Direction String
     */
    public String stopLeftRight() {
        leftRight = STOP;
        return this.frontBack + this.leftRight;
    }

    /**
     * Get the direction string
     * @return
     */
    public String toString() {
        return this.frontBack + this.leftRight;
    }

    /**
     * Getter for the front back string
     * @return String represent the front back direction
     */
    public String getFrontBack() {
        return this.frontBack;
    }

    /**
     * Getter for the left right string
     * @return
     */
    public String getLeftRight() {
        return this.leftRight;
    }

    /**
     * Comparing this direction object with other direction object
     * @param direction direction object compare with
     * @return true if the direction are the same, false otherwise
     */
    public boolean equals(Direction direction) {
        return this.frontBack.equals(direction.getFrontBack()) && this.leftRight.equals(direction.getLeftRight());
    }
}
