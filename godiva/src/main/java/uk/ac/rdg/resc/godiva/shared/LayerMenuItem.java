package uk.ac.rdg.resc.godiva.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LayerMenuItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private String description = null;
    private String id = null;
    private boolean plottable = true;
    private String wmsUrl = null;
    private List<LayerMenuItem> childItems = null;
    private LayerMenuItem parent = null;
    
    protected LayerMenuItem(){
    }
    
    public LayerMenuItem(String title, String id, boolean plottable) {
        this(title, null, id, plottable, null);
    }
    
    public LayerMenuItem(String title, String description, String id, boolean plottable, String wmsUrl) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.plottable = plottable;
        this.wmsUrl = wmsUrl;
    }
    
    public void addChildItem(LayerMenuItem item){
        if(childItems == null){
            childItems = new ArrayList<LayerMenuItem>();
        }
        childItems.add(item);
        item.parent = this;
    }
    
    public LayerMenuItem getParent() {
        return parent;
    }
    
    public String getTitle(){
        return title;
    }
    
    public String getId(){
        return id;
    }
    
    public boolean isPlottable(){
        return plottable;
    }
    
    public List<LayerMenuItem> getChildren(){
        return childItems;
    }
    
    public boolean isLeaf(){
        return childItems == null || childItems.size() == 0;
    }
    
    public String getWmsUrl() {
        if (wmsUrl == null) {
            if (parent == null) {
                return null;
//                throw new IllegalStateException("A layer menu tree must have a WmsUrl defined");
            } else {
                return parent.getWmsUrl();
            }
        } else {
            return wmsUrl;
        }
    }
    
    public String getDescription(){
        return description;
    }
    
    public void setId(String newId){
        this.id = newId;
    }
}
