package uk.ac.rdg.resc.godiva.client.widgets;

import com.google.gwt.user.client.ui.Label;

public class CopyrightInfo extends BaseSelector implements CopyrightInfoIF {
    private Label copyright;
    public CopyrightInfo() {
        super("Copyright");
        copyright = new Label();
        copyright.setStylePrimaryName("labelStyle");
        copyright.addStyleDependentName("light");
        label.setTitle("Information about the copyright of this dataset");
        add(copyright);
    }
    
    @Override
    public void setCopyrightInfo(String copyright){
        this.copyright.setText(copyright);
        this.copyright.setTitle("Copyright information about the current dataset");
        setEnabled(true);
    }
    
    @Override
    public void setEnabled(boolean enabled){
        if(enabled){
            copyright.removeStyleDependentName("inactive");
            label.removeStyleDependentName("inactive");
        } else {
            copyright.addStyleDependentName("inactive");
            label.addStyleDependentName("inactive");
        }
    }

    @Override
    public boolean hasCopyright() {
        return (copyright.getText() != null && !copyright.getText().equals(""));
    }

    @Override
    public String getCopyrightInfo() {
        return copyright.getText();
    }
}
