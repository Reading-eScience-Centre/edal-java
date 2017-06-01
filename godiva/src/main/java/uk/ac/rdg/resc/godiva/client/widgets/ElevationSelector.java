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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rdg.resc.godiva.client.handlers.ElevationSelectionHandler;
import uk.ac.rdg.resc.godiva.client.state.ElevationSelectorIF;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.ListBox;

/**
 * An implementation of {@link ElevationSelectorIF} which presents the available
 * elevations as a drop down list. In the case where we have a continuous depth
 * axis, this will still be the case, but ranges will be generated, and the
 * values shown will be the centre points of these ranges. The protocol for
 * continuous vertical axes is more flexible, but in practice this is sufficient
 * 
 * @author Guy Griffiths
 * 
 */
public class ElevationSelector extends BaseSelector implements ElevationSelectorIF {
    private ListBox elevations;
    private final NumberFormat format = NumberFormat.getFormat("#0.##");
    /*
     * This is required because the server will respond with the exact values it
     * knows about, and expects them back. This gets ugly with rounding errors.
     */
    private Map<String, String> formattedValuesToRealValues;
    private String id;
    private String units;
    private boolean continuous;
    private boolean pressure = false;

    public ElevationSelector(String id, String title, final ElevationSelectionHandler handler) {
        super(title);
        this.id = id;

        elevations = new ListBox();
        elevations.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                handler.elevationSelected(ElevationSelector.this.id, getSelectedElevation());
            }
        });
        add(elevations);

    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void populateElevations(List<String> availableElevations) {
        String currentElevation = getSelectedElevation();
        elevations.clear();
        formattedValuesToRealValues = new HashMap<String, String>();
        label.setStylePrimaryName("labelStyle");
        if (availableElevations == null || availableElevations.size() == 0) {
            label.addStyleDependentName("inactive");
            elevations.setEnabled(false);
        } else {
            if (continuous) {
                if (availableElevations.size() == 2) {
                    /*
                     * We have a start and end elevation. Calculate some
                     * sensible intermediate values
                     */
                    double firstVal = Double.parseDouble(availableElevations.get(0));
                    double secondVal = Double.parseDouble(availableElevations.get(1));
                    double dZ = getOptimumDz(firstVal, secondVal, 20);
                    int i = 0;

                    String formattedElevationStr = format.format(firstVal);
                    String formattedElevationRange = format.format(firstVal) + "/"
                            + format.format(firstVal + 0.5 * dZ);
                    elevations.addItem(formattedElevationStr);
                    formattedValuesToRealValues.put(formattedElevationStr, formattedElevationRange);
                    if (formattedElevationStr.equals(currentElevation)) {
                        elevations.setSelectedIndex(i);
                    }
                    i++;
                    for (double v = firstVal + dZ; v <= secondVal - dZ; v += dZ) {
                        formattedElevationStr = format.format(v);
                        formattedElevationRange = format.format(v - 0.5 * dZ) + "/"
                                + format.format(v + 0.5 * dZ);
                        elevations.addItem(formattedElevationStr);
                        formattedValuesToRealValues.put(formattedElevationStr,
                                formattedElevationRange);
                        if (formattedElevationStr.equals(currentElevation)) {
                            elevations.setSelectedIndex(i);
                        }
                        i++;
                    }
                    formattedElevationStr = format.format(secondVal);
                    formattedElevationRange = format.format(secondVal - 0.5 * dZ) + "/"
                            + format.format(secondVal);
                    elevations.addItem(formattedElevationStr);
                    formattedValuesToRealValues.put(formattedElevationStr, formattedElevationRange);
                    if (formattedElevationStr.equals(currentElevation)) {
                        elevations.setSelectedIndex(i);
                    }
                } else {
                    /*
                     * The server has provided us with axis values. We assume
                     * that these are to be midpoints in a range (apart from
                     * endpoints which are endpoints).
                     */
                    double previousVal = Double.parseDouble(availableElevations.get(0));
                    for (int i = 0; i < availableElevations.size(); i++) {
                        double currentVal = Double.parseDouble(availableElevations.get(i));
                        double nextVal;
                        if (i != availableElevations.size() - 1) {
                            nextVal = Double.parseDouble(availableElevations.get(i + 1));
                        } else {
                            nextVal = currentVal;
                        }
                        double prevBound = (currentVal + previousVal) / 2.0;
                        double nextBound = (currentVal + nextVal) / 2.0;

                        String formattedElevationStr = format.format(currentVal);
                        String formattedElevationRange = format.format(prevBound) + "/"
                                + format.format(nextBound);
                        elevations.addItem(
                                formattedElevationStr + units + "  (" + format.format(prevBound)
                                        + units + "-" + format.format(nextBound) + units + ")",
                                formattedElevationStr);
                        formattedValuesToRealValues.put(formattedElevationStr,
                                formattedElevationRange);
                        if (formattedElevationStr.equals(currentElevation)) {
                            elevations.setSelectedIndex(i);
                        }
                        previousVal = currentVal;
                    }

                }

            } else {
                int i = 0;
                boolean elSet = false;
                for (String elevationStr : availableElevations) {
                    Float elevation = Float.parseFloat(elevationStr);
                    String formattedElevationStr = format.format(elevation);
                    elevations.addItem(formattedElevationStr);
                    formattedValuesToRealValues.put(formattedElevationStr, elevationStr);
                    if (elevationStr.equals(currentElevation)) {
                        elevations.setSelectedIndex(i);
                        elSet = true;
                    }
                    i++;
                }
                if (!elSet && this.pressure) {
                    /*
                     * Select the final value if this is a pressure axis (and we
                     * haven't already set the elevation)
                     */
                    elevations.setSelectedIndex(i-1);
                }
            }
            label.removeStyleDependentName("inactive");
            elevations.setEnabled(true);
        }
    }

    /*
     * This method just picks a nice step value, based on start value, stop
     * value, and number of steps
     */
    private double getOptimumDz(double firstVal, double secondVal, int numberOfSteps) {
        double dz = (secondVal - firstVal) / numberOfSteps;
        double[] niceSteps = new double[] { 1e-3, 1e-2, 1e-1, 1, 5, 10, 20, 50, 100, 250, 500,
                1000, 10000 };
        double last = dz;
        for (double test : niceSteps) {
            if (dz > test) {
                last = test;
                continue;
            } else {
                dz = last;
                break;
            }
        }
        return dz;
    }

    @Override
    public void setUnitsAndDirection(String units, boolean positive, boolean pressure) {
        this.units = units;
        this.pressure = pressure;
        if (positive || pressure) {
            label.setText("Elevation");
            elevations.setTitle("Select the elevation");
        } else {
            label.setText("Depth");
            elevations.setTitle("Select the depth");
        }
        if (units != null) {
            label.setText(label.getText() + " (" + units + "):");
        } else {
            label.setText(label.getText() + ":");
        }
    }

    @Override
    public String getSelectedElevation() {
        if (!elevations.isEnabled())
            return null;
        int index = elevations.getSelectedIndex();
        if (index < 0)
            return null;
        if (continuous) {
            return elevations.getValue(index);
        } else {
            return formattedValuesToRealValues.get(elevations.getValue(index));
        }
    }

    @Override
    public void setSelectedElevation(String currentElevation) {
        for (int i = 0; i < elevations.getItemCount(); i++) {
            String elevation = elevations.getValue(i);
            if (currentElevation.equals(elevation)) {
                elevations.setSelectedIndex(i);
                return;
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (elevations.getItemCount() > 1)
            elevations.setEnabled(enabled);
        else
            elevations.setEnabled(false);

        if (!elevations.isEnabled()) {
            label.addStyleDependentName("inactive");
        } else {
            label.removeStyleDependentName("inactive");
        }
    }

    @Override
    public int getNElevations() {
        return elevations.getItemCount();
    }

    @Override
    public String getVerticalUnits() {
        return units;
    }

    @Override
    public String getSelectedElevationRange() {
        if (!elevations.isEnabled())
            return null;
        int index = elevations.getSelectedIndex();
        if (index < 0)
            return null;
        return formattedValuesToRealValues.get(elevations.getValue(index));
    }

    @Override
    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    @Override
    public boolean isContinuous() {
        return continuous;
    }
}
