package uk.ac.rdg.resc.godiva.client.widgets;

import java.util.List;

import uk.ac.rdg.resc.godiva.client.handlers.OpacitySelectionHandler;
import uk.ac.rdg.resc.godiva.client.handlers.PaletteSelectionHandler;
import uk.ac.rdg.resc.godiva.client.state.LayerSelectorIF;
import uk.ac.rdg.resc.godiva.client.state.PaletteSelectorIF;
import uk.ac.rdg.resc.godiva.client.widgets.DialogBoxWithCloseButton.CentrePosIF;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.NumberFormat;
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
 * or verticall oriented, and contains controls for setting all palette variables
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
	private static final int OOR_SIZE=20;
	
	private PushButton aboveMax;
	private PushButton belowMin;
	private OutOfRangeState aboveMaxState = OutOfRangeState.BLACK;
	private OutOfRangeState belowMinState = OutOfRangeState.BLACK;	
	
	private final NumberFormat format = NumberFormat.getFormat("#0.000");
	
	private CellPanel mainPanel;
	
	/*
	 * Whether the palette selector is vertically orientated
	 */
	private boolean vertical;
	
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
	
	private String wmsLayerId;
	
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
     * @param opacitySelector
     *            The {@link OpacitySelectionHandler} used for when the opacity
     *            changes
     * @param localCentre
     *            A {@link CentrePosIF} to define the local centre (usually over
     *            the centre of the map)
     * @param vertical
     *            <code>true</code> if this palette selector should be
     *            vertically aligned
     */
    public PaletteSelector(String wmsLayerId, int height, int width, final PaletteSelectionHandler handler,
            LayerSelectorIF wmsUrlProvider, final CentrePosIF localCentre, boolean vertical) {
        
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
                if(enabled && !isLocked())
                    popupPaletteSelector(localCentre);
            }
        });
        paletteImage.setTitle("Click to choose palette and number of colour bands");
        paletteImage.setStylePrimaryName("imageStyle");

        ChangeHandler scaleChangeHandler = new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                handler.scaleRangeChanged(PaletteSelector.this.wmsLayerId, getScaleRange());
                setScaleLabels();
            }
        };
        
		maxScale = new TextBox();
		maxScale.setWidth("60px");
		maxScale.addChangeHandler(scaleChangeHandler);
		maxScale.setTitle("The maximum value of the colour range");
		maxScale.setMaxLength(8);
		
		styles = new ListBox();
		styles.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                paletteHandler.paletteChanged(PaletteSelector.this.wmsLayerId, currentPalette, getSelectedStyle(), getNumColorBands());
            }
        });
		
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
		logScale.setWidth("60px");
		logScale.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if(isLogScale() && Float.parseFloat(minScale.getValue()) <= 0) {
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
		minScale.setWidth("60px");
		minScale.addChangeHandler(scaleChangeHandler);
		minScale.setTitle("The minimum value of the colour range");
		minScale.setMaxLength(8);

		autoButton = new PushButton(new Image(GWT.getModuleBaseURL()+"img/color_wheel.png"));
		autoButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(!isLocked())
                    handler.autoAdjustPalette(PaletteSelector.this.wmsLayerId);
            }
        });
		autoButton.setTitle("Auto-adjust the colour range");
		lockButton = new ToggleButton(new Image(GWT.getModuleBaseURL()+"img/lock_open.png"),new Image(GWT.getModuleBaseURL()+"img/lock.png"));
		lockButton.setTitle("Lock the colour range");
		lockButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean unlocked = !event.getValue();
                logScale.setEnabled(unlocked);
                autoButton.setEnabled(unlocked);
                maxScale.setEnabled(unlocked);
                minScale.setEnabled(unlocked);
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
                switch (aboveMaxState) {
                case BLACK:
                    aboveMaxState = OutOfRangeState.EXTEND;
                    break;
                case EXTEND:
                    aboveMaxState = OutOfRangeState.TRANSPARENT;
                    break;
                case TRANSPARENT:
                    aboveMaxState = OutOfRangeState.BLACK;
                    break;
                default:
                    break;
                }
                setOutOfRangeImages();
                paletteHandler.paletteChanged(PaletteSelector.this.wmsLayerId, currentPalette, getSelectedStyle(), getNumColorBands());
            }
        });
		
		belowMin = new PushButton();
		belowMin.setWidth("0px");
		belowMin.setHeight("0px");
		belowMin.setStylePrimaryName("paletteOutOfRangeButton");
		belowMin.addClickHandler(new ClickHandler() {
		    @Override
		    public void onClick(ClickEvent event) {
		        switch (belowMinState) {
		        case BLACK:
		            belowMinState = OutOfRangeState.EXTEND;
		            break;
		        case EXTEND:
		            belowMinState = OutOfRangeState.TRANSPARENT;
		            break;
		        case TRANSPARENT:
		            belowMinState = OutOfRangeState.BLACK;
		            break;
		        default:
		            break;
		        }
		        setOutOfRangeImages();
		        paletteHandler.paletteChanged(PaletteSelector.this.wmsLayerId, currentPalette, getSelectedStyle(), getNumColorBands());
		    }
		});
		
		if(vertical){
		    initVertical();
		} else {
		    initHorizontal();
		}
	}
	
    /*
     * Sets up the layout for a vertical palette
     */
	private void initVertical(){
        mainPanel = new HorizontalPanel();

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
        vp.setWidth((width+40)+"px");

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.add(autoButton);
        buttonsPanel.add(lockButton);
        buttonsPanel.setCellVerticalAlignment(autoButton, HasVerticalAlignment.ALIGN_TOP);
        buttonsPanel.setCellVerticalAlignment(lockButton, HasVerticalAlignment.ALIGN_TOP);
        buttonsPanel.setCellHorizontalAlignment(autoButton, HasHorizontalAlignment.ALIGN_RIGHT);
        buttonsPanel.setCellHorizontalAlignment(lockButton, HasHorizontalAlignment.ALIGN_LEFT);

        vp.add(maxScale);
        vp.setCellHeight(maxScale, "20px");
        maxScale.getElement().getStyle().setMarginTop(OOR_SIZE, Unit.PX);
        
        int margin = (int) ((height - 2*OOR_SIZE - 20)/3.0) - 20;
        mhLabel.getElement().getStyle().setMarginTop(margin, Unit.PX);
        
        vp.setCellVerticalAlignment(maxScale, HasVerticalAlignment.ALIGN_TOP);
        vp.add(mhLabel);
        vp.setCellVerticalAlignment(mhLabel, HasVerticalAlignment.ALIGN_TOP);

        vp.add(styles);
        vp.setCellHeight(styles, "20px");
        vp.setCellVerticalAlignment(styles, HasVerticalAlignment.ALIGN_TOP);
        vp.add(opacity);
        vp.setCellHeight(opacity, "20px");
        vp.setCellVerticalAlignment(styles, HasVerticalAlignment.ALIGN_TOP);
        vp.add(logScale);
        vp.setCellHeight(logScale, "20px");
        vp.setCellVerticalAlignment(logScale, HasVerticalAlignment.ALIGN_TOP);
        vp.add(buttonsPanel);
        vp.setCellHeight(buttonsPanel, "26px");
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
	private void initHorizontal(){
	    mainPanel = new VerticalPanel();
	    
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
	    hp.setHeight((height+40) + "px");
	    hp.setWidth(width+"px");
	    
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
	
	/*
	 * Pops up a selector with images for each palette which can be selected
	 */
	private void popupPaletteSelector(CentrePosIF localCentre) {
	    if(popup == null){
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
                paletteHandler.paletteChanged(wmsLayerId, currentPalette, getSelectedStyle(), getNumColorBands());
            }
        });
        popup.center();
    }
	
	/*
	 * Gets the palette images and displays them in the palettesPanel
	 */
	private void populatePaletteSelector() {
	    if(palettesPanel == null){
	        palettesPanel = new HorizontalPanel();
	    }
	    palettesPanel.clear();
	    for(final String palette : availablePalettes){
	        Image pImage = new Image(getImageUrl(palette, 200, 1));
	        pImage.setHeight("200px");
	        pImage.setWidth("30px");
	        pImage.addClickHandler(new ClickHandler() {
	            @Override
	            public void onClick(ClickEvent event) {
	                selectPalette(palette);
	                popup.hide();
	            }
	        });
	        pImage.setTitle(palette);
	        palettesPanel.add(pImage);
	    }
    }

	/*
	 * Gets the URL for the palette image
	 */
    private String getImageUrl(String paletteName, int height, int width){
	    String url = "?request=GetLegendGraphic"
	        +"&height="+height
	        +"&width="+width
	        +"&numcolorbands="+getNumColorBands()
	        +"&colorbaronly=true"
	        +"&vertical="+vertical
	        +"&palette="+paletteName;
	    return URL.encode(wmsUrlProvider.getWmsUrl()+url);
	}

    @Override
    public void setId(String id) {
        this.wmsLayerId = id;
    }
    
    @Override
    public void populatePalettes(List<String> availablePalettes){
	    this.availablePalettes = availablePalettes;
	    setEnabled(true);
	}
	
	@Override
    public String getSelectedPalette(){
	    return currentPalette;
	}
	
    @Override
    public String getAboveMaxString() {
        switch (aboveMaxState) {
        case EXTEND:
            return "extend";
        case TRANSPARENT:
            return "transparent";
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
    public String getBelowMinString() {
        switch (belowMinState) {
        case EXTEND:
            return "extend";
        case TRANSPARENT:
            return "transparent";
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
    public void selectPalette(String paletteString){
	    currentPalette = paletteString;
        
	    if(vertical) {
	        paletteImage.setUrl(getImageUrl(paletteString, height, 1));
	        paletteImage.setPixelSize(width, height- 2* OOR_SIZE);
	    } else {
	        paletteImage.setUrl(getImageUrl(paletteString, 1, width));
	        paletteImage.setPixelSize(width - 2* OOR_SIZE, height);
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
	    case EXTEND:
	        aboveImage.setUrl(paletteImage.getUrl());
	        if (vertical) {
	            aboveImage.setPixelSize(width, OOR_SIZE * height);
	        } else {
	            aboveImage.setPixelSize(OOR_SIZE * width, height);
	            aboveImage.getElement().getStyle().setMarginLeft(-(width - 1) * OOR_SIZE, Unit.PX);
	        }
	        aboveMax.setTitle(aboveText+"Saturate");
            break;
	    case TRANSPARENT:
	        aboveImage = new Image(GWT.getModuleBaseURL()+"img/extend_transparent.png");
	        if(vertical) {
	            aboveImage.setPixelSize(width, OOR_SIZE);
	        } else {
	            aboveImage.setPixelSize(OOR_SIZE, height);
	        }
	        aboveMax.setTitle(aboveText+"Transparent");
	        break;
	    case BLACK:
        default:
            aboveImage = new Image(GWT.getModuleBaseURL()+"img/extend_black.png");
            if(vertical) {
                aboveImage.setPixelSize(width, OOR_SIZE);
            } else {
                aboveImage.setPixelSize(OOR_SIZE, height);
            }
            aboveMax.setTitle(aboveText+"Black");
            break;
        }
	    
	    switch (belowMinState) {
	    case EXTEND:
	        belowImage.setUrl(paletteImage.getUrl());
	        if (vertical) {
	            belowImage.setPixelSize(width, OOR_SIZE * height);
                belowImage.getElement().getStyle().setMarginTop((1 - height) * OOR_SIZE, Unit.PX);
	        } else {
	            belowImage.setPixelSize(OOR_SIZE * width, height);
	        }
	        belowMin.setTitle(belowText+"Saturate");
	        break;
	    case TRANSPARENT:
	        belowImage = new Image(GWT.getModuleBaseURL()+"img/extend_transparent.png");
	        if(vertical) {
	            belowImage.setPixelSize(width, OOR_SIZE);
	        } else {
	            belowImage.setPixelSize(OOR_SIZE, height);
	        }
	        belowImage.getElement().getStyle().setMarginTop(0, Unit.PX);
	        belowMin.setTitle(belowText+"Transparent");
	        break;
	    case BLACK:
	    default:
	        belowImage = new Image(GWT.getModuleBaseURL()+"img/extend_black.png");
	        if(vertical) {
	            belowImage.setPixelSize(width, OOR_SIZE);
	        } else {
	            belowImage.setPixelSize(OOR_SIZE, height);
	        }
	        belowImage.getElement().getStyle().setMarginTop(0, Unit.PX);
	        belowMin.setTitle(belowText+"Black");
	        break;
	    }
	    
	    aboveMax.getUpFace().setImage(aboveImage);
	    belowMin.getUpFace().setImage(belowImage);
	    if(vertical) {
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
	    if(vals.length == 0){
	        /*
	         * We have no scale - we're plotting non-numerical data.  Disable the palette
	         */
	        setEnabled(false);
	        return false;
	    }
	    if(isLogScale == null){
	        isLogScale = isLogScale();
	    }
	    float minVal = Float.parseFloat(vals[0]);
	    if(isLogScale && minVal <= 0){
	        ErrorBox.popupErrorMessage("Cannot use a negative or zero value for logarithmic scale");
	        return false;
	    }
        /*
         * We don't format the output, in case the user has entered something
         * more precise
         */
	    minScale.setValue(minVal+"");
	    maxScale.setValue(Float.parseFloat(vals[1])+"");
	    setLogScale(isLogScale);
	    setScaleLabels();
	    return true;
	}
	
	public void setScaleLabels(){
	    boolean log = logScale.getSelectedIndex() == 1;
	    double min = log ? Math.log(Double.parseDouble(minScale.getValue())) : Double.parseDouble(minScale.getValue());
	    double max = log ? Math.log(Double.parseDouble(maxScale.getValue())) : Double.parseDouble(maxScale.getValue());
	    double third = (max-min)/3;
	    double sOneThird = log ? Math.exp(min + third) : min + third;
	    double sTwoThird = log ? Math.exp(min + 2*third) : min + 2*third;
	    mlLabel.setText(format.format(sOneThird));
	    mhLabel.setText(format.format(sTwoThird));
	}

    @Override
    public String getScaleRange() {
        return minScale.getValue()+","+maxScale.getValue();
    }

    @Override
    public int getNumColorBands() {
        return Integer.parseInt(nColorBands.getValue(nColorBands.getSelectedIndex()));
    }
    
    @Override
    public void setNumColorBands(int nBands){
        int diff = 250*250;
        int minIndex = 0;
        for(int i=0; i< nColorBands.getItemCount(); i++){
            int value = Integer.parseInt(nColorBands.getValue(i));
            if(value == nBands){
                nColorBands.setSelectedIndex(i);
                return;
            } else {
                int abs = (value-nBands)*(value-nBands);
                if(abs < diff){
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
        minScale.setEnabled(enabled);
        maxScale.setEnabled(enabled);
        autoButton.setEnabled(enabled);
        lockButton.setEnabled(enabled);
        styles.setEnabled(enabled);
        opacity.setEnabled(enabled);
        logScale.setEnabled(enabled);
        if(enabled){
            paletteImage.removeStyleDependentName("inactive");
            mlLabel.removeStyleDependentName("inactive");
            mhLabel.removeStyleDependentName("inactive");
        } else {
            paletteImage.addStyleDependentName("inactive");
            mlLabel.addStyleDependentName("inactive");
            mhLabel.addStyleDependentName("inactive");
        }
        this.enabled = enabled;
    }

    @Override
    public Widget asWidget() {
        return mainPanel;
    }

    @Override
    public void populateStyles(List<String> availableStyles) {
        styles.clear();
        for(String style : availableStyles) {
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
    public String getSelectedStyle() {
        if(styles.getSelectedIndex() >= 0)
            return styles.getValue(styles.getSelectedIndex());
        else
            return "default";
    }

    @Override
    public void selectStyle(String styleString) {
        for(int i=0; i < styles.getItemCount(); i++){
            String style = styles.getValue(i);
            if(styleString.equals(style)){
                styles.setSelectedIndex(i);
                return;
            }
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
        for(int i = 0; i < opacity.getItemCount(); i++){
            float listVal = Float.parseFloat(opacity.getValue(i));
            float dist = Math.abs(listVal - opacityValue);
            if(dist < minDist) {
                minDist = dist;
                opacity.setSelectedIndex(i);
            }
        }
    }
}
