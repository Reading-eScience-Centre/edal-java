package uk.ac.rdg.resc.godiva.client.widgets;

/**
 * A collection of interfaces providing state information about a particular
 * layer. The elements it holds will usually be widgets which can provide the
 * currently selected state.
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
    private WmsUrlProvider wmsUrl;

    public GodivaStateInfo(ElevationSelectorIF elevationSelector, TimeSelectorIF timeSelector,
            PaletteSelectorIF paletteSelector, UnitsInfoIF unitsInfo,
            CopyrightInfoIF copyrightInfo, InfoIF moreInfo, WmsUrlProvider wmsUrl) {
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

    public WmsUrlProvider getWmsUrlProvider() {
        return wmsUrl;
    }
}
