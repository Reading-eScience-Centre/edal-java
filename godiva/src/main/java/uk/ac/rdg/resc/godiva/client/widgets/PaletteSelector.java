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

import java.util.List;

import uk.ac.rdg.resc.godiva.client.handlers.PaletteSelectionHandler;
import uk.ac.rdg.resc.godiva.client.state.LayerSelectorIF;
import uk.ac.rdg.resc.godiva.client.state.PaletteSelectorIF;
import uk.ac.rdg.resc.godiva.client.util.UnitConverter;
import uk.ac.rdg.resc.godiva.client.widgets.DialogBoxWithCloseButton.CentrePosIF;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Implementation of {@link PaletteSelectorIF} which can be either horizontally
 * or vertically oriented, and contains controls for setting all palette
 * variables
 * 
 * @author Guy Griffiths
 * 
 */
public class PaletteSelector implements PaletteSelectorIF {
    private TextBox minScale;
    private TextBox maxScale;
    private ListBox nColorBands;
    private ListBox styles;
    private ListBox opacity;
    private ListBox logScale;
    private PushButton autoButton;
    private ToggleButton lockButton;
    private Label mhLabel;
    private Label mlLabel;

    /* Height of out-of-range elements */
    private static final int OOR_SIZE = 20;

    private PushButton aboveMax;
    private PushButton belowMin;
    private OutOfRangeState aboveMaxState = OutOfRangeState.BLACK;
    private OutOfRangeState belowMinState = OutOfRangeState.BLACK;

    /*
     * If the server has specified a specific colour for out of range values,
     * store it here
     */
    private String aboveMaxColourOverride = null;
    private String belowMinColourOverride = null;

    private String noDataColour = null;

    private final NumberFormat format = NumberFormat.getFormat("#0.000");

    private CellPanel mainPanel;

    /*
     * Whether the palette selector is vertically orientated
     */
    private boolean vertical;
    private boolean categorical = false;

    /**
     * A List of styles which do not use a palette
     */
    private List<String> noPaletteStyles;
    private List<String> availablePalettes;
    private String currentPalette;
    private int height;
    private int width;

    private Image paletteImage;
    private LayerSelectorIF wmsUrlProvider;
    private final PaletteSelectionHandler paletteHandler;

    private DialogBoxWithCloseButton popup;
    private HorizontalPanel palettesPanel;

    private boolean enabled;
    private boolean paletteEnabled;

    private String wmsLayerId;

    private UnitConverter converter = null;

    /** Whether to use normal or inverted palettes */
    private boolean inverted = false;

    /*
     * Used to repopulate the scale min/max boxes in the event of invalid input
     */
    private String lastMinScaleValue = "";
    private String lastMaxScaleValue = "";
    private Image legend;

    /**
     * Instantiates a new {@link PaletteSelector}
     * 
     * @param wmsLayerId
     *            The ID of the WMS layer
     * @param height
     *            The height of the palette image
     * @param width
     *            The width of the palette image
     * @param handler
     *            The {@link PaletteSelectionHandler} to handle palette events
     * @param wmsUrlProvider
     *            A {@link LayerSelectorIF} which can be used to obtain the WMS
     *            URL for the current WMS layer
     * @param localCentre
     *            A {@link CentrePosIF} to define the local centre (usually over
     *            the centre of the map)
     * @param vertical
     *            <code>true</code> if this palette selector should be
     *            vertically aligned
     */
    public PaletteSelector(String wmsLayerId, int height, int width,
            final PaletteSelectionHandler handler, LayerSelectorIF wmsUrlProvider,
            final CentrePosIF localCentre, boolean vertical) {
        this.wmsLayerId = wmsLayerId;
        this.wmsUrlProvider = wmsUrlProvider;
        this.height = height;
        this.width = width;
        this.paletteHandler = handler;

        this.vertical = vertical;

        enabled = true;

        nColorBands = new ListBox();
        nColorBands.addItem("10");
        nColorBands.addItem("20");
        nColorBands.addItem("50");
        nColorBands.addItem("100");
        nColorBands.addItem("250");
        nColorBands.setTitle("Select the number of colour bands to use for this data");

        paletteImage = new Image();
        paletteImage.setWidth("0px");
        paletteImage.setHeight("0px");
        paletteImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (paletteEnabled && !isLocked())
                    popupPaletteSelector(localCentre);
            }
        });
        paletteImage.setTitle("Click to choose palette and number of colour bands");
        paletteImage.setStylePrimaryName("imageStyle");

        ValueChangeHandler<String> scaleChangeHandler = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                float minVal;
                try {
                    minVal = Float.parseFloat(minScale.getValue());
                } catch (NumberFormatException e) {
                    Window.alert(minScale.getValue() + " is not a number!");
                    minScale.setValue(lastMinScaleValue);
                    return;
                }
                float maxVal;
                try {
                    maxVal = Float.parseFloat(maxScale.getValue());
                } catch (NumberFormatException e) {
                    Window.alert(maxScale.getValue() + " is not a number!");
                    maxScale.setValue(lastMaxScaleValue);
                    return;
                }
                if (maxVal < minVal) {
                    Window.alert(maxScale.getValue() + " is less than " + minScale.getValue());
                    minScale.setValue(lastMinScaleValue);
                    maxScale.setValue(lastMaxScaleValue);
                    return;
                }
                lastMinScaleValue = minScale.getValue();
                lastMaxScaleValue = maxScale.getValue();
                handler.scaleRangeChanged(PaletteSelector.this.wmsLayerId, getScaleRange());
                setScaleLabels();
            }
        };

        maxScale = new TextBox();
        maxScale.setWidth("80px");
        maxScale.addValueChangeHandler(scaleChangeHandler);
        maxScale.setTitle("The maximum value of the colour range");
        maxScale.setMaxLength(8);

        styles = new ListBox();
        styles.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                styleSelected(getSelectedStyle());
                paletteHandler.paletteChanged(PaletteSelector.this.wmsLayerId, currentPalette,
                        getSelectedStyle(), getNumColorBands());
            }
        });
        styles.setTitle("Select the style to plot this layer with");

        opacity = new ListBox();
        opacity.addItem("25%", "0.25");
        opacity.addItem("50%", "0.5");
        opacity.addItem("75%", "0.75");
        opacity.addItem("opaque", "1.0");
        opacity.setSelectedIndex(3);
        opacity.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                PaletteSelector.this.paletteHandler.setOpacity(PaletteSelector.this.wmsLayerId,
                        getOpacity());
            }
        });
        opacity.setTitle("Select the opacity of the layer");

        logScale = new ListBox();
        logScale.addItem("linear");
        logScale.addItem("log");
        logScale.setWidth("80px");
        logScale.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (isLogScale() && Float.parseFloat(minScale.getValue()) <= 0) {
                    setLogScale(false);
                    ErrorBox.popupErrorMessage("Cannot use a negative or zero value for logarithmic scale");
                } else {
                    setScaleLabels();
                    handler.logScaleChanged(PaletteSelector.this.wmsLayerId, isLogScale());
                }
            }
        });
        logScale.setTitle("Choose between a linear and a logarithmic scale");

        minScale = new TextBox();
        minScale.setWidth("80px");
        minScale.addValueChangeHandler(scaleChangeHandler);
        minScale.setTitle("The minimum value of the colour range");
        minScale.setMaxLength(8);

        autoButton = new PushButton(new Image(GWT.getModuleBaseURL() + "img/color_wheel.png"));
        autoButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!isLocked())
                    handler.autoAdjustPalette(PaletteSelector.this.wmsLayerId);
            }
        });
        autoButton.setTitle("Auto-adjust the colour range");
        lockButton = new ToggleButton(new Image(GWT.getModuleBaseURL() + "img/lock_open.png"),
                new Image(GWT.getModuleBaseURL() + "img/lock.png"));
        lockButton.setTitle("Lock the colour range");
        lockButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean unlocked = !event.getValue();
                logScale.setEnabled(unlocked);
                autoButton.setEnabled(unlocked);
                maxScale.setEnabled(unlocked);
                minScale.setEnabled(unlocked);
                aboveMax.setEnabled(unlocked);
                belowMin.setEnabled(unlocked);
                lockButton.setTitle(unlocked ? "Lock the colour range" : "Unlock the colour range");
            }
        });

        mhLabel = new Label();
        mhLabel.setStylePrimaryName("tickmark");
        mlLabel = new Label();
        mlLabel.setStylePrimaryName("tickmark");

        aboveMax = new PushButton();
        aboveMax.setWidth("0px");
        aboveMax.setHeight("0px");
        aboveMax.setStylePrimaryName("paletteOutOfRangeButton");
        aboveMax.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (paletteEnabled && !isLocked()) {
                    switch (aboveMaxState) {
                    case OVERRIDE:
                        aboveMaxState = OutOfRangeState.BLACK;
                        break;
                    case BLACK:
                        aboveMaxState = OutOfRangeState.EXTEND;
                        break;
                    case EXTEND:
                        aboveMaxState = OutOfRangeState.TRANSPARENT;
                        break;
                    case TRANSPARENT:
                        if (aboveMaxColourOverride != null) {
                            aboveMaxState = OutOfRangeState.OVERRIDE;
                        } else {
                            aboveMaxState = OutOfRangeState.BLACK;
                        }
                        break;
                    default:
                        break;
                    }
                    setOutOfRangeImages();
                    paletteHandler.paletteChanged(PaletteSelector.this.wmsLayerId, currentPalette,
                            getSelectedStyle(), getNumColorBands());
                }
            }
        });

        belowMin = new PushButton();
        belowMin.setWidth("0px");
        belowMin.setHeight("0px");
        belowMin.setStylePrimaryName("paletteOutOfRangeButton");
        belowMin.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (paletteEnabled && !isLocked()) {
                    switch (belowMinState) {
                    case OVERRIDE:
                        belowMinState = OutOfRangeState.BLACK;
                        break;
                    case BLACK:
                        belowMinState = OutOfRangeState.EXTEND;
                        break;
                    case EXTEND:
                        belowMinState = OutOfRangeState.TRANSPARENT;
                        break;
                    case TRANSPARENT:
                        if (belowMinColourOverride != null) {
                            belowMinState = OutOfRangeState.OVERRIDE;
                        } else {
                            belowMinState = OutOfRangeState.BLACK;
                        }
                        break;
                    default:
                        break;
                    }
                    setOutOfRangeImages();
                    paletteHandler.paletteChanged(PaletteSelector.this.wmsLayerId, currentPalette,
                            getSelectedStyle(), getNumColorBands());
                }
            }
        });

        legend = new Image();

        if (vertical) {
            mainPanel = new HorizontalPanel();
        } else {
            mainPanel = new VerticalPanel();
        }
        mainPanel.setSpacing(5);
        resetLayout();
    }

    private void resetLayout() {
        if (categorical) {
            initCategorical();
        } else if (vertical) {
            initVertical();
        } else {
            initHorizontal();
        }
    }

    /*
     * Sets up the layout for a vertical palette
     */
    private void initVertical() {
        GWT.log("initing vertical...");
        mainPanel.clear();
        VerticalPanel palettePanel = new VerticalPanel();
        palettePanel.add(aboveMax);
        palettePanel.setCellVerticalAlignment(aboveMax, HasVerticalAlignment.ALIGN_TOP);
        palettePanel.add(paletteImage);
        palettePanel.add(belowMin);
        palettePanel.setCellVerticalAlignment(belowMin, HasVerticalAlignment.ALIGN_BOTTOM);

        belowMin.getElement().getStyle().setMarginTop(-2, Unit.PX);
        aboveMax.getElement().getStyle().setMarginBottom(1, Unit.PX);

        mainPanel.add(palettePanel);

        VerticalPanel vp = new VerticalPanel();
        vp.setHeight(height + "px");
        vp.setWidth((width + 45) + "px");

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.add(autoButton);
        buttonsPanel.add(lockButton);
        buttonsPanel.setCellVerticalAlignment(autoButton, HasVerticalAlignment.ALIGN_TOP);
        buttonsPanel.setCellVerticalAlignment(lockButton, HasVerticalAlignment.ALIGN_TOP);
        buttonsPanel.setCellHorizontalAlignment(autoButton, HasHorizontalAlignment.ALIGN_CENTER);
        buttonsPanel.setCellHorizontalAlignment(lockButton, HasHorizontalAlignment.ALIGN_CENTER);

        vp.add(maxScale);
        vp.setCellHeight(maxScale, "20px");
        maxScale.getElement().getStyle().setMarginTop(OOR_SIZE, Unit.PX);

        int margin = (int) ((height - 2 * OOR_SIZE - 20) / 3.0) - 20;
        mhLabel.getElement().getStyle().setMarginTop(margin, Unit.PX);

        vp.setCellVerticalAlignment(maxScale, HasVerticalAlignment.ALIGN_TOP);
        vp.add(mhLabel);
        vp.setCellVerticalAlignment(mhLabel, HasVerticalAlignment.ALIGN_TOP);

        vp.add(styles);
        vp.setCellHeight(styles, "20px");
        styles.setWidth("100%");
        vp.setCellVerticalAlignment(styles, HasVerticalAlignment.ALIGN_TOP);
        vp.add(opacity);
        vp.setCellHeight(opacity, "20px");
        opacity.setWidth("100%");
        vp.setCellVerticalAlignment(styles, HasVerticalAlignment.ALIGN_TOP);
        vp.add(logScale);
        vp.setCellHeight(logScale, "20px");
        logScale.setWidth("100%");
        vp.setCellVerticalAlignment(logScale, HasVerticalAlignment.ALIGN_TOP);
        vp.add(buttonsPanel);
        buttonsPanel.setWidth("100%");
        vp.setCellHeight(buttonsPanel, "26px");
        vp.setCellWidth(buttonsPanel, "100%");
        vp.setCellVerticalAlignment(buttonsPanel, HasVerticalAlignment.ALIGN_TOP);

        vp.add(mlLabel);
        vp.setCellVerticalAlignment(mlLabel, HasVerticalAlignment.ALIGN_BOTTOM);

        mlLabel.getElement().getStyle().setMarginBottom(margin, Unit.PX);

        vp.add(minScale);
        vp.setCellHeight(minScale, "20px");
        vp.setCellVerticalAlignment(minScale, HasVerticalAlignment.ALIGN_BOTTOM);
        minScale.getElement().getStyle().setMarginBottom(OOR_SIZE, Unit.PX);

        mainPanel.add(vp);
    }

    /*
     * Sets the layout for a horizontal palette
     */
    private void initHorizontal() {
        mainPanel.clear();
        HorizontalPanel palettePanel = new HorizontalPanel();
        palettePanel.add(belowMin);
        palettePanel.setCellVerticalAlignment(belowMin, HasVerticalAlignment.ALIGN_TOP);
        palettePanel.setCellHorizontalAlignment(belowMin, HasHorizontalAlignment.ALIGN_RIGHT);
        palettePanel.add(paletteImage);
        palettePanel.add(aboveMax);
        palettePanel.setCellVerticalAlignment(aboveMax, HasVerticalAlignment.ALIGN_TOP);
        palettePanel.setCellHorizontalAlignment(aboveMax, HasHorizontalAlignment.ALIGN_LEFT);

        belowMin.getElement().getStyle().setMarginTop(0, Unit.PX);
        aboveMax.getElement().getStyle().setMarginTop(0, Unit.PX);
        belowMin.getElement().getStyle().setMarginRight(1, Unit.PX);
        aboveMax.getElement().getStyle().setMarginLeft(1, Unit.PX);

        mainPanel.add(palettePanel);

        HorizontalPanel hp = new HorizontalPanel();
        hp.setHeight((height + 40) + "px");
        hp.setWidth(width + "px");

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.add(autoButton);
        buttonsPanel.add(lockButton);

        VerticalPanel buttonsAndLogPanel = new VerticalPanel();
        buttonsAndLogPanel.add(buttonsPanel);
        buttonsAndLogPanel.add(styles);
        buttonsAndLogPanel.add(opacity);
        buttonsAndLogPanel.add(logScale);

        hp.add(minScale);
        hp.setCellHeight(minScale, "30px");
        minScale.getElement().getStyle().setMarginLeft(OOR_SIZE, Unit.PX);
        hp.setCellHorizontalAlignment(minScale, HasHorizontalAlignment.ALIGN_LEFT);

        hp.add(mlLabel);
        hp.setCellHorizontalAlignment(mlLabel, HasHorizontalAlignment.ALIGN_RIGHT);

        hp.add(buttonsAndLogPanel);

        hp.add(mhLabel);
        hp.setCellHorizontalAlignment(mhLabel, HasHorizontalAlignment.ALIGN_LEFT);

        hp.add(maxScale);
        hp.setCellHeight(maxScale, "30px");
        maxScale.getElement().getStyle().setMarginRight(OOR_SIZE, Unit.PX);
        hp.setCellHorizontalAlignment(maxScale, HasHorizontalAlignment.ALIGN_RIGHT);

        mainPanel.add(hp);
    }

    private void initCategorical() {
        mainPanel.clear();
        VerticalPanel vp = new VerticalPanel();
        vp.add(legend);
        vp.add(opacity);
        mainPanel.add(vp);
    }

    /*
     * Pops up a selector with images for each palette which can be selected
     */
    private void popupPaletteSelector(CentrePosIF localCentre) {
        if (popup == null) {
            popup = new DialogBoxWithCloseButton(localCentre);

            nColorBands.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    populatePaletteSelector();
                }
            });

            popup.setHTML("Click to choose a colour palette");
            populatePaletteSelector();
        }
        VerticalPanel popupPanel = new VerticalPanel();
        HorizontalPanel nCBPanel = new HorizontalPanel();
        nCBPanel.add(new Label("Colour bands:"));
        nCBPanel.add(nColorBands);
        Button invert = new Button("Flip");
        invert.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                inverted = !inverted;
                populatePaletteSelector();
            }
        });
        nCBPanel.add(invert);
        popupPanel.add(nCBPanel);
        popupPanel.setCellHorizontalAlignment(nCBPanel, HasHorizontalAlignment.ALIGN_CENTER);
        popupPanel.add(palettesPanel);

        popup.setAutoHideEnabled(true);
        popup.setModal(true);
        popup.setWidget(popupPanel);
        popup.setGlassEnabled(true);
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                selectPalette(currentPalette);
                paletteHandler.paletteChanged(wmsLayerId, currentPalette, getSelectedStyle(),
                        getNumColorBands());
            }
        });
        popup.center();
    }

    /*
     * Gets the palette images and displays them in the palettesPanel
     */
    private void populatePaletteSelector() {
        if (palettesPanel == null) {
            palettesPanel = new HorizontalPanel();
        }
        palettesPanel.clear();
        /*
         * 750px for the entire width should fit in almost any browser. Smaller
         * than that will be having to scroll for other things anyway.
         * 
         * Divide by 2 because we will generally have 2 of each palette - normal
         * and inverted
         */
        int width = 750 / (availablePalettes.size() / 2);
        for (final String paletteName : availablePalettes) {
            if (inverted && !paletteName.endsWith("-inv")) {
                continue;
            } else if (!inverted && paletteName.endsWith("inv")) {
                continue;
            }
            Image pImage = new Image(getImageUrl(paletteName, 200, 1));
            pImage.setHeight("200px");
            pImage.setWidth(width + "px");
            pImage.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectPalette(paletteName);
                    popup.hide();
                }
            });
            pImage.setTitle(paletteName);
            palettesPanel.add(pImage);
        }
    }

    /*
     * Gets the URL for the palette image
     */
    private String getImageUrl(String paletteName, int height, int width) {
        String url = "?request=GetLegendGraphic" + "&height=" + height + "&width=" + width
                + "&numcolorbands=" + getNumColorBands() + "&colorbaronly=true" + "&vertical="
                + vertical + "&palette=" + paletteName;
        return URL.encode(wmsUrlProvider.getWmsUrl() + url);
    }

    @Override
    public void setId(String id) {
        this.wmsLayerId = id;
    }

    @Override
    public void populatePalettes(List<String> availablePalettes) {
        this.availablePalettes = availablePalettes;
        if (!isLocked()) {
            setEnabled(true);
        }
    }

    @Override
    public String getSelectedPalette() {
        return currentPalette;
    }

    @Override
    public String getAboveMaxString() {
        switch (aboveMaxState) {
        case EXTEND:
            return "extend";
        case TRANSPARENT:
            return "transparent";
        case OVERRIDE:
            return aboveMaxColourOverride;
        case BLACK:
        default:
            return "0x000000";
        }
    }

    @Override
    public void setAboveMax(OutOfRangeState state) {
        aboveMaxState = state;
    }

    @Override
    public void setExtraAboveMaxColour(String aboveMaxColour) {
        if (aboveMaxColour == null) {
            return;
        } else if ("extend".equalsIgnoreCase(aboveMaxColour)) {
            aboveMaxState = OutOfRangeState.EXTEND;
        } else if ("transparent".equalsIgnoreCase(aboveMaxColour)) {
            aboveMaxState = OutOfRangeState.TRANSPARENT;
        } else if ("0x000000".equalsIgnoreCase(aboveMaxColour)
                || "#000000".equalsIgnoreCase(aboveMaxColour)
                || "0xff000000".equalsIgnoreCase(aboveMaxColour)
                || "#ff000000".equalsIgnoreCase(aboveMaxColour)) {
            aboveMaxState = OutOfRangeState.BLACK;
        } else {
            aboveMaxState = OutOfRangeState.OVERRIDE;
            this.aboveMaxColourOverride = aboveMaxColour;
        }
        setOutOfRangeImages();
    }

    @Override
    public String getBelowMinString() {
        switch (belowMinState) {
        case EXTEND:
            return "extend";
        case TRANSPARENT:
            return "transparent";
        case OVERRIDE:
            return belowMinColourOverride;
        case BLACK:
        default:
            return "0x000000";
        }
    }

    @Override
    public void setBelowMin(OutOfRangeState state) {
        belowMinState = state;
    }

    @Override
    public void setExtraBelowMinColour(String belowMinColour) {
        if (belowMinColour == null) {
            return;
        } else if ("extend".equalsIgnoreCase(belowMinColour)) {
            belowMinState = OutOfRangeState.EXTEND;
        } else if ("transparent".equalsIgnoreCase(belowMinColour)) {
            belowMinState = OutOfRangeState.TRANSPARENT;
        } else if ("0x000000".equalsIgnoreCase(belowMinColour)
                || "#000000".equalsIgnoreCase(belowMinColour)
                || "0xff000000".equalsIgnoreCase(belowMinColour)
                || "#ff000000".equalsIgnoreCase(belowMinColour)) {
            belowMinState = OutOfRangeState.BLACK;
        } else {
            belowMinState = OutOfRangeState.OVERRIDE;
            this.belowMinColourOverride = belowMinColour;
        }
        setOutOfRangeImages();
    }

    @Override
    public void setNoDataColour(String noDataColour) {
        this.noDataColour = noDataColour;
    }

    @Override
    public String getNoDataColour() {
        return noDataColour;
    }

    @Override
    public void selectPalette(String paletteString) {
        currentPalette = paletteString;

        if (vertical) {
            paletteImage.setUrl(getImageUrl(paletteString, height, 1));
            paletteImage.setPixelSize(width, height - 2 * OOR_SIZE);
        } else {
            paletteImage.setUrl(getImageUrl(paletteString, 1, width));
            paletteImage.setPixelSize(width - 2 * OOR_SIZE, height);
        }
        setOutOfRangeImages();
    }

    private void setOutOfRangeImages() {
        String baseText = "Click to change the colour displayed when values are ";
        String aboveText = baseText + "above the maximum.  Currently: ";
        String belowText = baseText + "below the mimimum.  Currently: ";
        Image aboveImage = new Image();
        Image belowImage = new Image();

        switch (aboveMaxState) {
        case OVERRIDE:
            aboveImage = new Image(GWT.getModuleBaseURL() + "img/extend_transparent.png");
            if (vertical) {
                aboveImage.setPixelSize(width, OOR_SIZE);
            } else {
                aboveImage.setPixelSize(OOR_SIZE, height);
            }
            String amc;
            if (aboveMaxColourOverride.length() == 9) {
                amc = "#" + aboveMaxColourOverride.substring(3);
            } else {
                amc = aboveMaxColourOverride;
            }
            aboveImage.getElement().getStyle().setProperty("backgroundColor", amc);
            aboveMax.setTitle(aboveText + " server default (" + aboveMaxColourOverride + ")");
            break;
        case EXTEND:
            aboveImage.setUrl(paletteImage.getUrl());
            if (vertical) {
                aboveImage.setPixelSize(width, OOR_SIZE * height);
            } else {
                aboveImage.setPixelSize(OOR_SIZE * width, height);
                aboveImage.getElement().getStyle().setMarginLeft(-(width - 1) * OOR_SIZE, Unit.PX);
            }
            aboveMax.setTitle(aboveText + "Saturate");
            break;
        case TRANSPARENT:
            aboveImage = new Image(GWT.getModuleBaseURL() + "img/extend_transparent.png");
            if (vertical) {
                aboveImage.setPixelSize(width, OOR_SIZE);
            } else {
                aboveImage.setPixelSize(OOR_SIZE, height);
            }
            aboveMax.setTitle(aboveText + "Transparent");
            break;
        case BLACK:
        default:
            aboveImage = new Image(GWT.getModuleBaseURL() + "img/extend_black.png");
            if (vertical) {
                aboveImage.setPixelSize(width, OOR_SIZE);
            } else {
                aboveImage.setPixelSize(OOR_SIZE, height);
            }
            aboveMax.setTitle(aboveText + "Black");
            break;
        }

        belowMin.getElement().getStyle().setProperty("backgroundColor", belowMinColourOverride);
        switch (belowMinState) {
        case OVERRIDE:
            belowImage = new Image(GWT.getModuleBaseURL() + "img/extend_transparent.png");
            if (vertical) {
                belowImage.setPixelSize(width, OOR_SIZE);
            } else {
                belowImage.setPixelSize(OOR_SIZE, height);
            }
            String bmc;
            if (belowMinColourOverride.length() == 9) {
                bmc = "#" + belowMinColourOverride.substring(3);
            } else {
                bmc = belowMinColourOverride;
            }
            belowImage.getElement().getStyle().setProperty("backgroundColor", bmc);
            belowMin.setTitle(belowText + " server default (" + belowMinColourOverride + ")");
            break;
        case EXTEND:
            belowImage.setUrl(paletteImage.getUrl());
            if (vertical) {
                belowImage.setPixelSize(width, OOR_SIZE * height);
                belowImage.getElement().getStyle().setMarginTop((1 - height) * OOR_SIZE, Unit.PX);
            } else {
                belowImage.setPixelSize(OOR_SIZE * width, height);
            }
            belowMin.setTitle(belowText + "Saturate");
            break;
        case TRANSPARENT:
            belowImage = new Image(GWT.getModuleBaseURL() + "img/extend_transparent.png");
            if (vertical) {
                belowImage.setPixelSize(width, OOR_SIZE);
            } else {
                belowImage.setPixelSize(OOR_SIZE, height);
            }
            belowImage.getElement().getStyle().setMarginTop(0, Unit.PX);
            belowMin.setTitle(belowText + "Transparent");
            break;
        case BLACK:
        default:
            belowImage = new Image(GWT.getModuleBaseURL() + "img/extend_black.png");
            if (vertical) {
                belowImage.setPixelSize(width, OOR_SIZE);
            } else {
                belowImage.setPixelSize(OOR_SIZE, height);
            }
            belowImage.getElement().getStyle().setMarginTop(0, Unit.PX);
            belowMin.setTitle(belowText + "Black");
            break;
        }

        aboveMax.getUpFace().setImage(aboveImage);
        belowMin.getUpFace().setImage(belowImage);
        if (vertical) {
            aboveMax.setPixelSize(width, OOR_SIZE);
            belowMin.setPixelSize(width, OOR_SIZE);
        } else {
            aboveMax.setPixelSize(OOR_SIZE, height);
            belowMin.setPixelSize(OOR_SIZE, height);
        }

    }

    @Override
    public boolean setScaleRange(String scaleRange, Boolean isLogScale) {
        String[] vals = scaleRange.split(",");
        if (vals.length == 0) {
            /*
             * We have no scale - we're plotting non-numerical data. Disable the
             * palette
             */
            setEnabled(false);
            return false;
        } else if (vals.length != 2) {
            /*
             * Invalid string. Keep the palette enabled with the previous
             * values.
             */
            return false;
        }
        if (isLogScale == null) {
            isLogScale = isLogScale();
        }
        float minVal = Float.parseFloat(vals[0]);
        if (isLogScale && minVal <= 0) {
            ErrorBox.popupErrorMessage("Cannot use a negative or zero value for logarithmic scale");
            return false;
        }
        float maxVal = Float.parseFloat(vals[1]);

        if (converter != null) {
            minVal = converter.convertToDisplayUnit(minVal);
            maxVal = converter.convertToDisplayUnit(maxVal);
        }

        /*
         * We don't format the output, in case the user has entered something
         * more precise
         */
        minScale.setValue(minVal + "");
        maxScale.setValue(maxVal + "");
        lastMinScaleValue = minScale.getValue();
        lastMaxScaleValue = maxScale.getValue();
        setLogScale(isLogScale);
        setScaleLabels();
        return true;
    }

    public void setScaleLabels() {
        boolean log = logScale.getSelectedIndex() == 1;
        double min = log ? Math.log(Double.parseDouble(minScale.getValue())) : Double
                .parseDouble(minScale.getValue());
        double max = log ? Math.log(Double.parseDouble(maxScale.getValue())) : Double
                .parseDouble(maxScale.getValue());
        double third = (max - min) / 3;
        double sOneThird = log ? Math.exp(min + third) : min + third;
        double sTwoThird = log ? Math.exp(min + 2 * third) : min + 2 * third;
        mlLabel.setText(format.format(sOneThird));
        mhLabel.setText(format.format(sTwoThird));
    }

    @Override
    public String getScaleRange() {
        if (converter != null) {
            /*
             * Convert the values if required
             */
            return converter.convertFromDisplayUnit(Float.parseFloat(minScale.getValue())) + ","
                    + converter.convertFromDisplayUnit(Float.parseFloat(maxScale.getValue()));
        } else {
            return getDisplayScaleRange();
        }
    }

    @Override
    public String getDisplayScaleRange() {
        return minScale.getValue() + "," + maxScale.getValue();
    }

    @Override
    public int getNumColorBands() {
        return Integer.parseInt(nColorBands.getValue(nColorBands.getSelectedIndex()));
    }

    @Override
    public void setNumColorBands(int nBands) {
        int diff = 250 * 250;
        int minIndex = 0;
        for (int i = 0; i < nColorBands.getItemCount(); i++) {
            int value = Integer.parseInt(nColorBands.getValue(i));
            if (value == nBands) {
                nColorBands.setSelectedIndex(i);
                return;
            } else {
                int abs = (value - nBands) * (value - nBands);
                if (abs < diff) {
                    diff = abs;
                    minIndex = i;
                }
            }
        }
        nColorBands.setSelectedIndex(minIndex);
    }

    public void setLogScale(boolean isLogScale) {
        logScale.setSelectedIndex(isLogScale ? 1 : 0);
    }

    @Override
    public boolean isLogScale() {
        if (logScale.getSelectedIndex() == 0) {
            // Linear scale
            return false;
        } else {
            // Log scale
            return true;
        }
    }

    @Override
    public boolean isLocked() {
        return lockButton.getValue();
    }

    @Override
    public void setEnabled(boolean enabled) {
        aboveMax.setEnabled(enabled);
        belowMin.setEnabled(enabled);
        minScale.setEnabled(enabled);
        maxScale.setEnabled(enabled);
        autoButton.setEnabled(enabled);
        lockButton.setEnabled(enabled);
        styles.setEnabled(enabled);
        opacity.setEnabled(enabled);
        logScale.setEnabled(enabled);
        if (enabled) {
            aboveMax.removeStyleDependentName("inactive");
            belowMin.removeStyleDependentName("inactive");
            paletteImage.removeStyleDependentName("inactive");
            mlLabel.removeStyleDependentName("inactive");
            mhLabel.removeStyleDependentName("inactive");
        } else {
            aboveMax.addStyleDependentName("inactive");
            belowMin.addStyleDependentName("inactive");
            paletteImage.addStyleDependentName("inactive");
            mlLabel.addStyleDependentName("inactive");
            mhLabel.addStyleDependentName("inactive");
        }
        this.enabled = enabled;
        this.paletteEnabled = enabled;
    }

    @Override
    public Widget asWidget() {
        return mainPanel;
    }

    @Override
    public void populateStyles(List<String> availableStyles) {
        styles.clear();
        for (String style : availableStyles) {
            styles.addItem(style);
        }

        if (availableStyles.size() <= 1
                || ((availableStyles.size() == 2) && availableStyles.get(0).equalsIgnoreCase(
                        "default"))) {
            styles.setVisible(false);
        } else {
            styles.setVisible(true);
        }
    }

    @Override
    public void setNoPaletteStyles(List<String> noPaletteStyles) {
        this.noPaletteStyles = noPaletteStyles;
        /*
         * Fire a change event to disable the palette if need be
         */
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), styles);
    }

    @Override
    public String getSelectedStyle() {
        if (styles.getSelectedIndex() >= 0)
            return styles.getValue(styles.getSelectedIndex());
        else
            return "default";
    }

    @Override
    public void selectStyle(String styleString) {
        for (int i = 0; i < styles.getItemCount(); i++) {
            String style = styles.getValue(i);
            if (styleString.equals(style)) {
                styles.setSelectedIndex(i);
                styleSelected(style);
                return;
            }
        }
    }

    private void styleSelected(String style) {
        if (noPaletteStyles.contains(style)) {
            paletteEnabled = false;
            aboveMax.setEnabled(false);
            belowMin.setEnabled(false);
            paletteImage.addStyleDependentName("inactive");
            aboveMax.addStyleDependentName("inactive");
            belowMin.addStyleDependentName("inactive");
        } else {
            paletteEnabled = true;
            aboveMax.setEnabled(true);
            belowMin.setEnabled(true);
            paletteImage.removeStyleDependentName("inactive");
            aboveMax.removeStyleDependentName("inactive");
            belowMin.removeStyleDependentName("inactive");
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public float getOpacity() {
        return Float.parseFloat(opacity.getValue(opacity.getSelectedIndex()));
    }

    @Override
    public void setOpacity(float opacityValue) {
        float minDist = Float.MAX_VALUE;
        for (int i = 0; i < opacity.getItemCount(); i++) {
            float listVal = Float.parseFloat(opacity.getValue(i));
            float dist = Math.abs(listVal - opacityValue);
            if (dist < minDist) {
                minDist = dist;
                opacity.setSelectedIndex(i);
            }
        }
        paletteHandler.setOpacity(PaletteSelector.this.wmsLayerId, opacityValue);
    }

    public void setUnitConverter(UnitConverter converter) {
        this.converter = converter;
    }

    @Override
    public void setCategorical(boolean categorical) {
        if (categorical) {
            legend.setUrl(wmsUrlProvider.getWmsUrl() + "?STYLES=default-categorical&LAYERS="
                    + wmsUrlProvider.getSelectedId()
                    + "&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetLegendGraphic");
        }
        if (categorical != this.categorical) {
            this.categorical = categorical;
            resetLayout();
        }
    }
}
