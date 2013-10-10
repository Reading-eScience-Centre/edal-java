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
