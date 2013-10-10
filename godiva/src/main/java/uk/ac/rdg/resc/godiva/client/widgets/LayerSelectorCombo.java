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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rdg.resc.godiva.client.handlers.LayerSelectionHandler;
import uk.ac.rdg.resc.godiva.client.state.LayerSelectorIF;
import uk.ac.rdg.resc.godiva.shared.LayerMenuItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * An implementation of {@link LayerSelectorIF} which presents the layers in a
 * popup tree.
 * 
 * @author Guy Griffiths
 * 
 */
public class LayerSelectorCombo extends Button implements LayerSelectorIF {
    private LayerSelectionHandler layerSelectionHandler;
    private PopupPanel popup;
    private Tree tree;
    private Map<String, String> layerIdWmsUrlToTitle;
    private Map<String, LayerMenuItem> layerIdWmsUrlToMenuEntry; 
    private String selectedLayer;
    private boolean firstUse = true;
    public String firstTitle = null;
    
    private LayerMenuItem selectedNode;
    private String wmsUrl;

    public LayerSelectorCombo(LayerSelectionHandler layerHandler) {
        this(layerHandler, "Click here to start", true);
    }
    
    public LayerSelectorCombo(LayerSelectionHandler layerHandler, String firstText, boolean showRefreshButton) {
        super("Loading");
        this.layerSelectionHandler = layerHandler;

        layerIdWmsUrlToTitle = new HashMap<String, String>();
        layerIdWmsUrlToMenuEntry = new HashMap<String, LayerMenuItem>();

        popup = new PopupPanel();
        popup.setAutoHideEnabled(true);

        setStylePrimaryName("hiddenButton");
        addStyleDependentName("title");
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popup.setPopupPosition(
                        LayerSelectorCombo.this.getAbsoluteLeft(),
                        LayerSelectorCombo.this.getAbsoluteTop()
                                + LayerSelectorCombo.this.getOffsetHeight());
                if (!popup.isShowing()) {
                    popup.show();
                }
                if(firstUse) {
                    setText(firstTitle);
                    firstUse = false;
                }
            }
        });

        setText(firstText);
        
        VerticalPanel vPanel = new VerticalPanel();
        tree = new Tree();
        vPanel.add(tree);
        
        if(showRefreshButton) {
            PushButton button = new PushButton("Refresh");
            button.addStyleDependentName("CentreAndMargin");
            button.setTitle("Click to refresh the layers list");
            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    layerSelectionHandler.refreshLayerList();
                }
            });
            vPanel.add(button);
        }
        
        popup.add(vPanel);
    }

    
    @Override
    public void populateLayers(LayerMenuItem topItem){
        tree.clear();
        String nodeLabel = topItem.getTitle();
        if(firstUse){
            firstTitle = nodeLabel;
        } else {
            setText(nodeLabel);
        }
        List<LayerMenuItem> children = topItem.getChildren();
        if(children != null){
            for(LayerMenuItem child : children){
                addNode(child, null);
            }
        } else {
            tree.add(new Label("No data available"));
        }
    }
    
    /*
     * This is called recursively to transfer the information in the
     * LayerMenuItem tree to the Tree widget
     */
    private void addNode(final LayerMenuItem item, final TreeItem parentNode) {
        String label = item.getTitle();
        final String id = item.getId();
        
        Label node = new Label(label);
        if(parentNode != null){
            final String parentName = parentNode.getText();
            layerIdWmsUrlToTitle.put(id+item.getWmsUrl(), parentName + "<div class=\"subtitle\">  &nbsp>&nbsp" + label + "</div>");
        } else {
            layerIdWmsUrlToTitle.put(id+item.getWmsUrl(), label);
        }
        
        layerIdWmsUrlToMenuEntry.put(id+item.getWmsUrl(), item);
        
        /*
         * If the item is plottable, we need a click handler
         */
        if(item.isPlottable()){
            node.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectLayer(id, item.getWmsUrl(), true);
                }
            });
        }
        
        if(item.getDescription() != null){
            node.setTitle(item.getDescription());
        }
        
        if(item.isLeaf()){
            /*
             * We have a leaf node
             */
            if(parentNode != null)
                parentNode.addItem(node);
            else
                tree.addItem(node);
        } else {
            /*
             * We have a branch node
             */
            
            TreeItem nextNode = new TreeItem(node);
            
            if (parentNode == null) {
                tree.addItem(nextNode);
            } else {
                parentNode.addItem(nextNode);
            }
            for (LayerMenuItem child : item.getChildren()) {
                addNode(child, nextNode);
            }
        }
    }

    @Override
    public String getSelectedId() {
        return selectedLayer;
    }

    @Override
    public void selectLayer(String id, String wmsUrl, boolean autoZoomAndPalette) {
        selectedLayer = id;
        setHTML(layerIdWmsUrlToTitle.get(id+wmsUrl));
        if (popup.isShowing()) {
            popup.hide();
        }
        LayerMenuItem item = layerIdWmsUrlToMenuEntry.get(id+wmsUrl);
        this.wmsUrl = wmsUrl;
        selectedNode = item;
        layerSelectionHandler.layerSelected(wmsUrl, id, autoZoomAndPalette);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(enabled)
            removeStyleDependentName("inactive");
        else
            addStyleDependentName("inactive");
    }
    
    @Override
    public List<String> getTitleElements(){
        List<String> title = new ArrayList<String>();
        LayerMenuItem currentNode = selectedNode;
        LayerMenuItem parentNode;
        if(currentNode != null) {
            while((parentNode = currentNode.getParent()) != null){
                title.add(currentNode.getTitle());
                currentNode = parentNode;
            }
        }
        return title;
    }


    @Override
    public String getWmsUrl() {
        return wmsUrl;
    }
    
}
