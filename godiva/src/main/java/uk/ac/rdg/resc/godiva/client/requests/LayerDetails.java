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

package uk.ac.rdg.resc.godiva.client.requests;

import java.util.List;

/**
 * Encapsulates the details which can get returned by the
 * GetMetadata&item=layerDetails method. This is what will be passed to a
 * {@link LayerRequestCallback}
 * 
 * @author Guy Griffiths
 * 
 */
public class LayerDetails {
    private final String id;

    private String units = null;
    private String extents = "-180,-90,180,90";
    private String scaleRange = null;
    private int nColorBands = 50;
    private boolean logScale = false;
    private List<String> supportedStyles = null;
    private List<String> noPaletteStyles = null;
    private boolean categorical = false;
    private String zUnits = null;
    private boolean zPositive = true;
    private boolean pressure = false;
    private List<String> availableZs = null;
    private String moreInfo = null;
    private String copyright = null;
    private List<String> availablePalettes = null;
    private String selectedPalette = null;
    private String aboveMaxColour = null;
    private String belowMinColour = null;
    private String noDataColour = null;
    private List<String> availableDates = null;
    private String nearestTime = null;
    private String nearestDate = null;

    private boolean continuousT = false;
    private String startTime = null;
    private String endTime = null;
    private boolean continuousZ = false;
    private String startZ = null;
    private String endZ = null;

    private boolean queryable = true;
    private boolean downloadable = false;

    private boolean timeseriesSupported = false;
    private boolean profilesSupported = false;
    private boolean transectsSupported = false;


    public LayerDetails(String layerId) {
        id = layerId;
    }

    public String getId() {
        return id;
    }

    public String getUnits() {
        return units;
    }

    public String getExtents() {
        return extents;
    }

    public String getScaleRange() {
        return scaleRange;
    }

    public int getNumColorBands() {
        return nColorBands;
    }

    public boolean isLogScale() {
        return logScale;
    }

    public List<String> getSupportedStyles() {
        return supportedStyles;
    }

    public List<String> getNoPaletteStyles() {
        return noPaletteStyles;
    }

    public boolean isCategorical() {
        return categorical;
    }

    public String getZUnits() {
        return zUnits;
    }
    
    public boolean isPressure() {
        return pressure;
    }

    public boolean isZPositive() {
        return zPositive;
    }

    public List<String> getAvailableZs() {
        return availableZs;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public String getCopyright() {
        return copyright;
    }

    public List<String> getAvailablePalettes() {
        return availablePalettes;
    }

    public String getSelectedPalette() {
        return selectedPalette;
    }

    public String getAboveMaxColour() {
        return aboveMaxColour;
    }

    public String getBelowMinColour() {
        return belowMinColour;
    }

    public String getNoDataColour() {
        return noDataColour;
    }

    public List<String> getAvailableDates() {
        return availableDates;
    }

    public String getNearestDateTime() {
        return nearestTime;
    }

    public String getNearestDate() {
        return nearestDate;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setExtents(String extents) {
        this.extents = extents;
    }

    public void setScaleRange(String scaleRange) {
        this.scaleRange = scaleRange;
    }

    public void setNColorBands(int nColorBands) {
        this.nColorBands = nColorBands;
    }

    public void setLogScale(boolean logScale) {
        this.logScale = logScale;
    }

    public void setSupportedStyles(List<String> supportedStyles) {
        this.supportedStyles = supportedStyles;
    }

    public void setNoPaletteStyles(List<String> noPaletteStyles) {
        this.noPaletteStyles = noPaletteStyles;
    }

    public void setIsCategorical(boolean categorical) {
        this.categorical = categorical;
    }

    public void setZUnits(String zUnits) {
        this.zUnits = zUnits;
    }

    public void setIsPressure(boolean pressure) {
        this.pressure = pressure;
    }
    
    public void setZPositive(boolean zPositive) {
        this.zPositive = zPositive;
    }

    public void setAvailableZs(List<String> availableZs) {
        this.availableZs = availableZs;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public void setAvailablePalettes(List<String> availablePalettes) {
        this.availablePalettes = availablePalettes;
    }

    public void setSelectedPalette(String selectedPalette) {
        this.selectedPalette = selectedPalette;
    }

    public void setAboveMaxColour(String aboveMaxColour) {
        this.aboveMaxColour = aboveMaxColour;
    }

    public void setBelowMinColour(String belowMinColour) {
        this.belowMinColour = belowMinColour;
    }

    public void setNoDataColour(String noDataColour) {
        this.noDataColour = noDataColour;
    }

    public void setAvailableDates(List<String> availableDates) {
        this.availableDates = availableDates;
    }

    public void setNearestTime(String nearestTime) {
        this.nearestTime = nearestTime;
    }

    public void setNearestDate(String nearestDate) {
        this.nearestDate = nearestDate;
    }

    public boolean isContinuousT() {
        return continuousT;
    }

    public void setContinuousT(boolean continuousT) {
        this.continuousT = continuousT;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isContinuousZ() {
        return continuousZ;
    }

    public void setContinuousZ(boolean continuousZ) {
        this.continuousZ = continuousZ;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartZ() {
        return startZ;
    }

    public String getEndZ() {
        return endZ;
    }

    public void setStartZ(String startZ) {
        this.startZ = startZ;
    }

    public void setEndZ(String endZ) {
        this.endZ = endZ;
    }

    public boolean isQueryable() {
        return queryable;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
    }

    public boolean isDownloadable() {
        return downloadable;
    }

    public void setDownloadable(boolean downloadable) {
        this.downloadable = downloadable;
    }

    public boolean supportsTimeseries() {
        return timeseriesSupported;
    }

    public boolean supportsProfiles() {
        return profilesSupported;
    }

    public boolean supportsTransects() {
        return transectsSupported;
    }

    public void setTimeseriesSupported(boolean timeseriesSupported) {
        this.timeseriesSupported = timeseriesSupported;
    }

    public void setProfilesSupported(boolean profilesSupported) {
        this.profilesSupported = profilesSupported;
    }

    public void setTransectsSupported(boolean transectsSupported) {
        this.transectsSupported = transectsSupported;
    }
}
