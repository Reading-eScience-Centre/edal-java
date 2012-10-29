package uk.ac.rdg.resc.edal.graphics;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorComponent;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorComponent.VectorComponentType;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;

public enum PlotStyle {
    BOXFILL {
        @Override
        public boolean usesPalette() {
            return true;
        }
    },
    VECTOR {
        @Override
        public boolean usesPalette() {
            return false;
        }
    },
    POINT {
        @Override
        public boolean usesPalette() {
            return true;
        }
    },
    TRAJECTORY {
        @Override
        public boolean usesPalette() {
            return true;
        }
    },
    CONTOUR {
        @Override
        public boolean usesPalette() {
            return false;
        }
    },
    GRIDPOINT {
        @Override
        public boolean usesPalette() {
            return false;
        }
    },
    DEFAULT {
        @Override
        public boolean usesPalette() {
            return true;
        }
    };

    public abstract boolean usesPalette();
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
    
    public static List<PlotStyle> getAllowedPlotStyles(Feature feature, ScalarMetadata metadata) {
        List<PlotStyle> allowedPlotStyles = new ArrayList<PlotStyle>();

        /*
         * Any feature can have its gridpoint(s) plotted
         */
        allowedPlotStyles.add(GRIDPOINT);

        boolean numerical = Number.class.isAssignableFrom(metadata.getValueType());
        if (numerical) {
            if (metadata instanceof VectorComponent
                    && (((VectorComponent) metadata).getComponentType() == VectorComponentType.DIRECTION)) {
                /*
                 * If we have a vector direction, we can plot it as a vector.
                 */
                allowedPlotStyles.add(VECTOR);
            } else {
                /*
                 * Any other numerical data can be plotted as a coloured point
                 */
                allowedPlotStyles.add(POINT);
                if ((feature instanceof GridSeriesFeature || feature instanceof GridFeature)) {
                    /*
                     * Gridded features can be plotted in the boxfill or contour styles (when implemented)
                     */
                    allowedPlotStyles.add(BOXFILL);
                    allowedPlotStyles.add(CONTOUR);
                } else if ((feature instanceof TrajectoryFeature)) {
                    /*
                     * Trajectory features can be plotted as a trajectory
                     */
                    allowedPlotStyles.add(TRAJECTORY);
                }
            }
        }
        return allowedPlotStyles;
    }
    
    public static PlotStyle getDefaultPlotStyle(Feature feature, ScalarMetadata metadata) {
        if(feature instanceof PointSeriesFeature || feature instanceof ProfileFeature){
            /*
             * PointSeries and Profile features have the style of point, even if
             * non-numerical (will be plotted as white icons)
             */
            return POINT;
        }
        if(feature instanceof TrajectoryFeature){
            /*
             * Trajectory features always have the default style of trajectory, even if they are non-numerical
             */
            return TRAJECTORY;
        }
        if(!Number.class.isAssignableFrom(metadata.getValueType())){
            /*
             * Otherwise non-numerical data has the default style of gridpoint.
             */
            return GRIDPOINT;
        }
        if(metadata instanceof VectorComponent
                && (((VectorComponent) metadata).getComponentType() == VectorComponentType.DIRECTION)){
            /*
             * Directional data always has the default style of vector 
             */
            return VECTOR;
        }
        /*
         * Insert conditions for default contour plot here
         */
        if(feature instanceof GridFeature || feature instanceof GridSeriesFeature){
            return BOXFILL;
        }
        
        /*
         * We don't meet any of the other conditions. This normally means that
         * we have not fully specified all possibilities. We can still plot as
         * gridpoint style...
         */
        return GRIDPOINT;
    }
}
