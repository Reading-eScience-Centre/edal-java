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

package uk.ac.rdg.resc.godiva.client;

import uk.ac.rdg.resc.godiva.client.state.ElevationSelectorIF;
import uk.ac.rdg.resc.godiva.client.state.LayerSelectorIF;
import uk.ac.rdg.resc.godiva.client.state.PaletteSelectorIF;
import uk.ac.rdg.resc.godiva.client.state.TimeSelectorIF;
import uk.ac.rdg.resc.godiva.client.state.UnitsInfoIF;
import uk.ac.rdg.resc.godiva.client.widgets.AnimationButton;
import uk.ac.rdg.resc.godiva.client.widgets.MapArea;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A class containing static methods for returning different layouts. This means
 * that multiple viewports can be configured here from the same codebase with
 * very little change
 * 
 * @author Guy Griffiths
 */
public class LayoutManager {
    public static Widget getTraditionalGodiva3Layout(IsWidget layerSelector, Label title,
            UnitsInfoIF unitsInfo, TimeSelectorIF timeSelector,
            ElevationSelectorIF elevationSelector, PaletteSelectorIF paletteSelector,
            Anchor kmzLink, Anchor permalink, Anchor email, Anchor screenshot, Image rescLogo,
            MapArea mapArea, Image loadingImage, AnimationButton anim, PushButton infoButton) {

        kmzLink.setStylePrimaryName("linkStyle");
        permalink.setStylePrimaryName("linkStyle");
        email.setStylePrimaryName("linkStyle");
        screenshot.setStylePrimaryName("linkStyle");

        VerticalPanel selectors = new VerticalPanel();
        selectors.add(title);
        selectors.add(unitsInfo);
        selectors.add(timeSelector);
        selectors.add(elevationSelector);

        selectors.setHeight("150px");

        HorizontalPanel bottomPanel = new HorizontalPanel();
        bottomPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        anim.setWidth("16px");
        bottomPanel.add(anim);
        bottomPanel.setWidth("100%");
        bottomPanel.add(kmzLink);
        bottomPanel.add(permalink);
        bottomPanel.add(email);
        bottomPanel.add(screenshot);
        infoButton.setWidth("16px");
        bottomPanel.add(infoButton);

//        HorizontalPanel topPanel = new HorizontalPanel();

//        topPanel.add(rescLogo);
//        topPanel.add(selectors);
//        topPanel.setCellVerticalAlignment(rescLogo, HasVerticalAlignment.ALIGN_MIDDLE);

        HorizontalPanel mapPalettePanel = new HorizontalPanel();
        mapPalettePanel.add(mapArea);
        mapPalettePanel.add(paletteSelector);

        /*
         * We introduce an AbsolutePanel here. Generally I like to avoid them,
         * but it allows us to overlay a loading image. It's introduced at the
         * last possible moment to avoid any ugliness related to absolute
         * positioning
         */
        AbsolutePanel mapPaletteLoaderPanel = new AbsolutePanel();
        mapPaletteLoaderPanel.add(mapPalettePanel);
        int loaderHeight = loadingImage.getHeight();
        int loaderWidth = loadingImage.getWidth();
        if (loaderHeight == 0)
            loaderHeight = 19;
        if (loaderWidth == 0)
            loaderWidth = 220;
        mapPaletteLoaderPanel.add(loadingImage,
                (int) (mapArea.getMap().getSize().getWidth() - loaderWidth) / 2, (int) (mapArea
                        .getMap().getSize().getHeight() - loaderHeight) / 2);

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(selectors);

        vPanel.setCellHeight(selectors, "150px");
        vPanel.setCellWidth(selectors, ((int) mapArea.getMap().getSize().getWidth() + 100) + "px");
        vPanel.add(mapPaletteLoaderPanel);

        vPanel.add(bottomPanel);
        vPanel.setCellHeight(bottomPanel, "40px");
        vPanel.setCellVerticalAlignment(bottomPanel, HasVerticalAlignment.ALIGN_MIDDLE);

        int logoSpace = 80;
        HorizontalPanel mainWindow = new HorizontalPanel();
        ScrollPanel layerScrollPanel = new ScrollPanel(layerSelector.asWidget());
        layerScrollPanel.setHeight(((int) mapArea.getMap().getSize().getHeight() + 190 - logoSpace) + "px");
        layerScrollPanel.setWidth("250px");
        layerSelector.asWidget().setWidth("250px");
        
        VerticalPanel leftPanel = new VerticalPanel();
        leftPanel.add(rescLogo);
        leftPanel.setCellHeight(rescLogo, logoSpace+"px");
        leftPanel.setCellVerticalAlignment(rescLogo, HasVerticalAlignment.ALIGN_MIDDLE);
        leftPanel.setCellHorizontalAlignment(rescLogo, HasHorizontalAlignment.ALIGN_CENTER);
        leftPanel.add(layerScrollPanel);
        
        mainWindow.add(leftPanel);
        mainWindow.add(vPanel);
        mainWindow.setCellHeight(layerScrollPanel, "100%");
        
        ScrollPanel scrollPanel = new ScrollPanel(mainWindow);
        return scrollPanel;
    }

    public static Widget getCompactGodiva3Layout(LayerSelectorIF layerSelector,
            UnitsInfoIF unitsInfo, TimeSelectorIF timeSelector,
            ElevationSelectorIF elevationSelector, PaletteSelectorIF paletteSelector,
            Anchor kmzLink, Anchor permalink, Anchor email, Anchor screenshot, Image rescLogo,
            MapArea mapArea, Image loadingImage, AnimationButton anim, PushButton infoButton) {

        kmzLink.setStylePrimaryName("linkStyle");
        permalink.setStylePrimaryName("linkStyle");
        email.setStylePrimaryName("linkStyle");
        screenshot.setStylePrimaryName("linkStyle");

        VerticalPanel selectors = new VerticalPanel();
        selectors.add(layerSelector);
        selectors.add(unitsInfo);
        selectors.add(timeSelector);
        selectors.add(elevationSelector);

        /*
         * The image height is hardcoded here, because when running with IE8,
         * rescLogo.getHeight() returns 0 instead of the actual height...
         */
        int logoHeight = rescLogo.getHeight();
        if (logoHeight == 0)
            logoHeight = 52;
        selectors.setHeight(logoHeight + "px");

        HorizontalPanel bottomPanel = new HorizontalPanel();
        bottomPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        anim.setWidth("16px");
        bottomPanel.add(anim);
        bottomPanel.setWidth("100%");
        bottomPanel.add(kmzLink);
        bottomPanel.add(permalink);
        bottomPanel.add(email);
        bottomPanel.add(screenshot);
        infoButton.setWidth("16px");
        bottomPanel.add(infoButton);

        HorizontalPanel topPanel = new HorizontalPanel();

        topPanel.add(rescLogo);
        topPanel.add(selectors);
        topPanel.setCellVerticalAlignment(rescLogo, HasVerticalAlignment.ALIGN_MIDDLE);

        HorizontalPanel mapPalettePanel = new HorizontalPanel();
        mapPalettePanel.add(mapArea);
        mapPalettePanel.add(paletteSelector);

        /*
         * We introduce an AbsolutePanel here. Generally I like to avoid them,
         * but it allows us to overlay a loading image. It's introduced at the
         * last possible moment to avoid any ugliness related to absolute
         * positioning
         */
        AbsolutePanel mapPaletteLoaderPanel = new AbsolutePanel();
        mapPaletteLoaderPanel.add(mapPalettePanel);
        int loaderHeight = loadingImage.getHeight();
        int loaderWidth = loadingImage.getWidth();
        if (loaderHeight == 0)
            loaderHeight = 19;
        if (loaderWidth == 0)
            loaderWidth = 220;
        mapPaletteLoaderPanel.add(loadingImage,
                (int) (mapArea.getMap().getSize().getWidth() - loaderWidth) / 2, (int) (mapArea
                        .getMap().getSize().getHeight() - loaderHeight) / 2);

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(topPanel);

        vPanel.setCellHeight(topPanel, logoHeight + "px");
        vPanel.setCellWidth(topPanel, ((int) mapArea.getMap().getSize().getWidth() + 100) + "px");
        vPanel.add(mapPaletteLoaderPanel);

        vPanel.add(bottomPanel);
        vPanel.setCellHeight(bottomPanel, "100%");
        vPanel.setCellVerticalAlignment(bottomPanel, HasVerticalAlignment.ALIGN_MIDDLE);

        ScrollPanel scrollPanel = new ScrollPanel(vPanel);

        return scrollPanel;
    }
}
