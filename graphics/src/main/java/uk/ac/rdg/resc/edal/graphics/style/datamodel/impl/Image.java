package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import uk.ac.rdg.resc.edal.graphics.style.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.Id2FeatureAndMember;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.model.ArrowData;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.model.DrawableData;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.model.ImageData;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.model.RasterData;

public class Image extends Drawable {
    
    private ImageData imageData;
    
    public Image(ImageData imageData) {
        this.imageData = imageData;
    }
    
    @Override
    public BufferedImage drawImage(GlobalPlottingParams params, Id2FeatureAndMember id2Feature) {
        BufferedImage finalImage = new BufferedImage(params.getWidth(), params.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = finalImage.createGraphics();
        
        for(DrawableData data : imageData.getLayers()) {
            Drawable drawable = null;
            if(data instanceof ImageData) {
                ImageData newImageData = (ImageData) data;
                drawable = new Image(newImageData);
            } else if(data instanceof RasterData) {
                RasterData rasterData = (RasterData) data;
                drawable = new RasterPlotter(rasterData);
            } else if(data instanceof ArrowData) {
                ArrowData ArrowData = (ArrowData) data;
                drawable = new ArrowPlotter(ArrowData);
            }
            if(drawable != null) {
                graphics.drawImage(drawable.drawImage(params, id2Feature), 0, 0, null);
            }
        }
        return finalImage;
    }
}
