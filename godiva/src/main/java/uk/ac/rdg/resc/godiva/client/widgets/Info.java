package uk.ac.rdg.resc.godiva.client.widgets;

import com.google.gwt.user.client.ui.Label;

public class Info extends BaseSelector implements InfoIF {
    private Label info;
    public Info() {
        super("Copyright");
        info = new Label();
        info.setStylePrimaryName("labelStyle");
        info.addStyleDependentName("light");
        label.setTitle("Information about the data");
        add(info);
    }
    
    @Override
    public void setInfo(String info){
        this.info.setText(info);
        this.info.setTitle("Information about the data");
        setEnabled(true);
    }
    
    @Override
    public void setEnabled(boolean enabled){
        if(enabled){
            info.removeStyleDependentName("inactive");
            label.removeStyleDependentName("inactive");
        } else {
            info.addStyleDependentName("inactive");
            label.addStyleDependentName("inactive");
        }
    }

    @Override
    public boolean hasInfo() {
        return (info.getText() != null && !info.getText().equals(""));
    }

    @Override
    public String getInfo() {
        return info.getText();
    }
}
