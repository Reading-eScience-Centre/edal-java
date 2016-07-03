/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
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

package uk.ac.rdg.resc.edal.examples;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.dataset.cdm.CdmGridDatasetFactory;
import uk.ac.rdg.resc.edal.domain.PointCollectionDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.PointCollectionFeature;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

public class ReadPointData {
    /*
     * An example program which opens a gridded dataset and extracts point data from it
     */
    public static void main(String[] args) throws IOException, EdalException {
        /*
         * Get the data file
         */
        URL resource = ReadPointData.class.getResource("/synthetic_rectilinear_data.nc");

        /*
         * Creates a dataset factory for reading NetCDF datasets
         */
        CdmGridDatasetFactory datasetFactory = new CdmGridDatasetFactory();

        /*
         * Create a gridded dataset from the NetCDF file, with the ID
         * "griddataset"
         */
        Dataset rawDataset = datasetFactory.createDataset("griddataset", resource.getFile());
        
        if(!(rawDataset instanceof GriddedDataset)) {
            throw new EdalException("Dataset is not gridded");
        }
        GriddedDataset dataset = (GriddedDataset) rawDataset;

        /*
         * Create a list of horizontal positions at which data should be
         * extracted
         */
        List<HorizontalPosition> positions = new ArrayList<>();
        for (int i = -170; i <= 170; i += 10) {
            for (int j = -80; j <= 80; j += 10) {
                positions.add(new HorizontalPosition(i, j, DefaultGeographicCRS.WGS84));
            }
        }

        /*
         * Choose a variable to extract data for. We are just using the first
         * one in the (unordered) set because it doesn't matter for this example
         */
        String varId = dataset.getVariableIds().iterator().next();

        /*
         * This is how to get available metadata about the variable. We'll use
         * this to select a suitable time and depth to extract data from.
         */
        VariableMetadata variableMetadata = dataset.getVariableMetadata(varId);

        /*
         * Cast to a GridVariableMetadata. Generally VariableMetadata from a
         * GriddedDataset can be cast to GridVariableMetadata with no issues.
         * 
         * ASIDE (feel free to ignore this comment - it's not relevant to data
         * extraction):
         * 
         * The only case where a GriddedDataset will have variables which are
         * non-gridded is if a variable is dynamically generated (using a
         * VariablePlugin). This allows us to have variables generated on the
         * fly which are not guaranteed to be gridded. For example, we could
         * create a dynamically-generated variable which had a continuous domain
         * and interpolated a gridded variable.
         * 
         * This is not relevant here, although this dataset does have several
         * dynamically-generated variables. All of the "*[NE]Comp" variables are
         * present in the data file, and represent components of a vector. The
         * variables "*:*-(mag|dir|group)" are then dynamically-generated
         * variables representing the magnitude and direction, and a parent
         * grouping variable.
         */
        GridVariableMetadata gridVariableMetadata = (GridVariableMetadata) variableMetadata;

        /*
         * Fixed time/depth to extract at
         */
        DateTime time = gridVariableMetadata.getTemporalDomain().getCoordinateValue(0);
        Double zVal = gridVariableMetadata.getVerticalDomain().getCoordinateValue(0);
        VerticalCrs zCrs = gridVariableMetadata.getVerticalDomain().getVerticalCrs();
        VerticalPosition zPos = new VerticalPosition(zVal, zCrs);

        /*
         * The domain to extract data over
         */
        PointCollectionDomain domain = new PointCollectionDomain(positions, zPos, time);

        /*
         * Perform the data extraction
         */
        PointCollectionFeature pointCollection = dataset.extractPointCollection(
                CollectionUtils.setOf(varId), domain);

        /*
         * The extracted domain (will match the supplied domain values)
         */
        Array1D<HorizontalPosition> extractedDomain = pointCollection.getDomain()
                .getDomainObjects();
        /*
         * The extracted values. There will be a one-to-one correspondance
         * between these values and the extracted domain.
         */
        Array1D<Number> values = pointCollection.getValues(varId);
        
        /*
         * Just to demonstrate they are the same
         */
        assert(extractedDomain.size() == values.size());

        System.out.println("Data for variable: "+varId);
        for (int i = 0; i < values.size(); i++) {
            System.out.println(extractedDomain.get(i)+": "+values.get(i));
        }
    }
}
