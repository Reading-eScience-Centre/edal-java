/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package uk.ac.rdg.resc.edal.position;

/**
 * An interface defining periods of time
 * 
 * @author Guy Griffiths
 */
public interface TimePeriod {
    /**
     * Set the number of seconds in this time period
     * 
     * @return the {@link TimePosition} object
     */
    public TimePeriod withSeconds(int seconds);

    /**
     * Set the number of minutes in this time period
     * 
     * @return the {@link TimePosition} object
     */
    public TimePeriod withMinutes(int minutes);

    /**
     * Set the number of hours in this time period
     * 
     * @return the {@link TimePosition} object
     */
    public TimePeriod withHours(int hours);

    /**
     * Set the number of days in this time period
     * 
     * @return the {@link TimePosition} object
     */
    public TimePeriod withDays(int days);

    /**
     * Set the number of weeks in this time period
     * 
     * @return the {@link TimePosition} object
     */
    public TimePeriod withWeeks(int weeks);

    /**
     * Set the number of months in this time period
     * 
     * @return the {@link TimePosition} object
     */
    public TimePeriod withMonths(int months);

    /**
     * Set the number of years in this time period
     * 
     * @return the {@link TimePosition} object
     */
    public TimePeriod withYears(int years);

    /**
     * Get the number of seconds in this time period
     * 
     * @return the number of seconds
     */
    public int getSeconds();

    /**
     * Get the number of minutes in this time period
     * 
     * @return the number of minutes
     */
    public int getMinutes();

    /**
     * Get the number of hours in this time period
     * 
     * @return the number of hours
     */
    public int getHours();

    /**
     * Get the number of days in this time period
     * 
     * @return the number of days
     */
    public int getDays();

    /**
     * Get the number of weeks in this time period
     * 
     * @return the number of weeks
     */
    public int getWeeks();

    /**
     * Get the number of months in this time period
     * 
     * @return the number of months
     */
    public int getMonths();

    /**
     * Get the number of years in this time period
     * 
     * @return the number of years
     */
    public int getYears();
}
