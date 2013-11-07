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

package uk.ac.rdg.resc.godiva.client.widgets;

import uk.ac.rdg.resc.godiva.client.state.UnitsInfoIF;

import com.google.gwt.user.client.ui.Label;

/**
 * Implementation of {@link UnitsInfoIF} which displays the units as a
 * {@link Label}
 * 
 * @author Guy Griffiths
 * 
 */
public class UnitsInfo extends BaseSelector implements UnitsInfoIF {
    private Label units;

    public UnitsInfo() {
        super("Units");
        units = new Label();
        units.setStylePrimaryName("labelStyle");
        units.addStyleDependentName("light");
        label.setTitle("Units of measurement for the data");
        add(units);
    }

    @Override
    public void setUnits(String units) {
        this.units.setText(units);
        this.units.setTitle("The units of the data are " + units);
        setEnabled(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            units.removeStyleDependentName("inactive");
            label.removeStyleDependentName("inactive");
        } else {
            units.addStyleDependentName("inactive");
            label.addStyleDependentName("inactive");
        }
    }

    @Override
    public String getUnits() {
        return units.getText();
    }
}
