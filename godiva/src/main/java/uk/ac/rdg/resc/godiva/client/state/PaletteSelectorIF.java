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
 * Interface defining a palette selector widget
 * 
 * @author Guy Griffiths
 * 
 */
public interface PaletteSelectorIF extends IsWidget {
    public enum OutOfRangeState {
        OVERRIDE, BLACK, EXTEND, TRANSPARENT
    }

    /**
     * Sets the layer ID which this palette selector is currently active on
     * 
     * @param id
     *            The ID
     */
    public void setId(String id);

    /**
     * Populates the list of available palette names
     */
    public void populatePalettes(List<String> availablePalettes);

    /**
     * @return The name of the currently selected palette
     */
    public String getSelectedPalette();

    /**
     * Select the named palette, if available
     * 
     * @param paletteString
     *            The name of the palette to select
     */
    public void selectPalette(String paletteString);

    /**
     * @return The string representation of the colour to display when values
     *         are above the maximum
     */
    public String getAboveMaxString();

    /**
     * Sets the behaviour of values above the maximum
     */
    public void setAboveMax(OutOfRangeState state);

    /**
     * Sets a colour to use in addition to black/extend/transparent for values
     * above the maximum scale value
     * 
     * @param aboveMaxColour
     *            The colour to display for values above the maximum scale range
     */
    public void setExtraAboveMaxColour(String aboveMaxColour);

    /**
     * @return The string representation of the colour to display when values
     *         are below the minimum
     */
    public String getBelowMinString();

    /**
     * Sets the behaviour of values below the minimum
     */
    public void setBelowMin(OutOfRangeState state);

    /**
     * Sets a colour to use in addition to black/extend/transparent for values
     * below the minimum scale value
     * 
     * @param belowMinColour
     *            The colour to display for values below the minimum scale range
     */
    public void setExtraBelowMinColour(String belowMinColour);

    /**
     * Populates the list of available style names
     */
    public void populateStyles(List<String> availableStyles);

    /**
     * @return The name of the currently selected style
     */
    public String getSelectedStyle();

    /**
     * Select the named style, if available
     * 
     * @param styleString
     *            The name of the style to select
     */
    public void selectStyle(String styleString);

    /**
     * Sets the scale range
     * 
     * @param scaleRange
     *            The desired scale range, of the form [min],[max]
     * @param logScale
     *            Whether we want this to be a log scale or not
     * @return <code>true</code> if the operation was successful (may not be if
     *         the palette is locked, or we want a negative value in a
     *         logarithmic range)
     */
    public boolean setScaleRange(String scaleRange, Boolean logScale);

    /**
     * @return A {@link String} of the form "[min],[max]" which represents the
     *         current scale range
     */
    public String getScaleRange();

    /**
     * @return The number of colour bands in the currently selected palette
     */
    public int getNumColorBands();

    /**
     * Sets the number of colour bands in the current palette
     */
    public void setNumColorBands(int nBands);

    /**
     * @return Whether or not this is a logarithmic scale
     */
    public boolean isLogScale();

    /**
     * @return Whether or not the palette is locked
     */
    public boolean isLocked();

    /**
     * Enables/disables the palette selector
     */
    public void setEnabled(boolean enabled);

    /**
     * @return Whether or not the current palette selector is enabled
     */
    public boolean isEnabled();

    /**
     * @return The opacity of the current palette as a float ranging from 0-1
     */
    public float getOpacity();

    /**
     * Sets the opacity of the current palette
     * 
     * @param opacity
     *            The opacity, as a float ranging from 0-1
     */
    public void setOpacity(float opacity);

    /**
     * @param noDataColour
     *            The colour to display for missing data
     */
    public void setNoDataColour(String noDataColour);
    
    /**
     * @return The colour to display for missing data
     */
    public String getNoDataColour();
}
