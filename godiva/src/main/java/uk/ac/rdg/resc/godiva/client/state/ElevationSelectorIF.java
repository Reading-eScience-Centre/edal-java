/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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
 ******************************************************************************/

package uk.ac.rdg.resc.godiva.client.state;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface defining a widget for selecting elevation
 * 
 * @author Guy Griffiths
 * 
 */
public interface ElevationSelectorIF extends IsWidget {
    /**
     * Sets the layer ID which this widget is referring to
     */
    public void setId(String id);

    /**
     * Populates the available elevations
     * 
     * @param availableElevations
     *            The available elevations, represented as a {@link List} of
     *            {@link String}s
     */
    public void populateElevations(List<String> availableElevations);

    /**
     * Sets the units and direction of the vertical axis
     * 
     * @param units
     *            A string representing the units
     * @param positive
     *            <code>true</code> if increasing elevation value means
     *            increasing height. <code>false</code> if increasing elevation
     *            value means increasing depth
     * @param pressure
     *            <code>true</code> if this axis represents pressure
     */
    public void setUnitsAndDirection(String units, boolean positive, boolean pressure);

    /**
     * Gets the currently selected elevation as a {@link String}
     */
    public String getSelectedElevation();

    /**
     * Gets the currently selected elevation range if applicable
     */
    public String getSelectedElevationRange();

    /**
     * Sets the current elevation to the desired value
     */
    public void setSelectedElevation(String currentElevation);

    /**
     * Enables/disables the elevation selector
     */
    public void setEnabled(boolean enabled);

    /**
     * @return The number of available elevations
     */
    public int getNElevations();

    /**
     * @return The units of the vertical axis
     */
    public String getVerticalUnits();

    /**
     * Sets this elevation selector to be continuous
     */
    public void setContinuous(boolean continuous);

    /**
     * @return <code>true</code> if this currently represents a continuous
     *         vertical axis
     */
    public boolean isContinuous();
}
