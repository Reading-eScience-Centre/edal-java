/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.edal;

import java.net.URI;

/**
 * Calendar system used to interpret dates and times.
 * @author Jon
 */
public interface CalendarSystem {

    /**
     * Returns true if date/times in this calendar system can be converted
     * to the standard (ISO8601) calendar system, which is a proleptic Gregorian
     * calendar (TODO check this).  (Some calendar systems used by the
     * environmental modelling community, for example the 360-day calendar, do
     * not represent real dates and times and are thus not convertible to the
     * ISO calendar.)
     * @return
     */
    public boolean isConvertibleToISO8601();

    /**
     * Returns a URI that identifies this calendar system
     */
    public URI getURI();

}
