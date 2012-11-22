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
