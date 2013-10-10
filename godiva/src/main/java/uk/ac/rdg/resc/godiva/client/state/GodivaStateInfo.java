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

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A collection of interfaces providing state information about a particular
 * layer. The elements it holds must be widgets, since the interfaces all
 * inherit from {@link IsWidget} which can provide the currently selected state.
 * 
 * Note that this class may never return null values for any of its methods.
 * 
 * @author Guy Griffiths
 * 
 */
public class GodivaStateInfo {
    private ElevationSelectorIF elevationSelector;
    private TimeSelectorIF timeSelector;
    private PaletteSelectorIF paletteSelector;
    private UnitsInfoIF unitsInfo;
    private CopyrightInfoIF copyrightInfo;
    private InfoIF moreInfo;
    private LayerSelectorIF wmsUrl;

    public GodivaStateInfo(ElevationSelectorIF elevationSelector, TimeSelectorIF timeSelector,
            PaletteSelectorIF paletteSelector, UnitsInfoIF unitsInfo,
            CopyrightInfoIF copyrightInfo, InfoIF moreInfo, LayerSelectorIF wmsUrl) {
        super();
        if (elevationSelector == null || timeSelector == null || paletteSelector == null
                || unitsInfo == null || copyrightInfo == null || moreInfo == null || wmsUrl == null) {
            throw new IllegalArgumentException("Cannot provide a null state getter");
        }
        this.elevationSelector = elevationSelector;
        this.timeSelector = timeSelector;
        this.paletteSelector = paletteSelector;
        this.unitsInfo = unitsInfo;
        this.copyrightInfo = copyrightInfo;
        this.moreInfo = moreInfo;
        this.wmsUrl = wmsUrl;
    }

    public ElevationSelectorIF getElevationSelector() {
        return elevationSelector;
    }

    public TimeSelectorIF getTimeSelector() {
        return timeSelector;
    }

    public PaletteSelectorIF getPaletteSelector() {
        return paletteSelector;
    }

    public UnitsInfoIF getUnitsInfo() {
        return unitsInfo;
    }

    public CopyrightInfoIF getCopyrightInfo() {
        return copyrightInfo;
    }

    public InfoIF getMoreInfo() {
        return moreInfo;
    }

    public LayerSelectorIF getWmsUrlProvider() {
        return wmsUrl;
    }
}
