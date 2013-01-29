package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.image.BufferedImage;

import uk.ac.rdg.resc.edal.graphics.style.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.Id2FeatureAndMember;

public abstract class Drawable {
    public abstract BufferedImage drawImage(GlobalPlottingParams params, Id2FeatureAndMember id2Feature);
}
