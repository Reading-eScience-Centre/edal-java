package uk.ac.rdg.resc.godiva.client.widgets;

import com.google.gwt.user.client.ui.Label;

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
    public void setUnits(String units){
        this.units.setText(units);
        this.units.setTitle("The units of the data are "+units);
        setEnabled(true);
    }
    
    @Override
    public void setEnabled(boolean enabled){
        if(enabled){
            units.removeStyleDependentName("inactive");
            label.removeStyleDependentName("inactive");
        } else {
            units.addStyleDependentName("inactive");
            label.addStyleDependentName("inactive");
        }
    }
    
    @Override
    public String getUnits(){
        return units.getText();
    }
}
