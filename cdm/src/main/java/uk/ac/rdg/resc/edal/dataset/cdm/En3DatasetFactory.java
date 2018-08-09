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

package uk.ac.rdg.resc.edal.dataset.cdm;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;

/**
 * {@link DatasetFactory} that creates {@link Dataset}s representing profile
 * data from the EN3/4 database read through the Unidata Common Data Model.
 *
 * The EN3 and EN4 databases use an almost identical format to the general Argo
 * NetCDF files. The only difference is a few variable names, and the depth axis
 * units.
 * 
 * This class simply extends {@link ArgoDatasetFactory}, and overrides the
 * required "constants".
 *
 * @author Guy Griffiths
 */
public final class En3DatasetFactory extends ArgoDatasetFactory {

    public En3DatasetFactory() {
        super();
        DEPTH = "DEPH_CORRECTED";

        TEMP_PARAMETER = new Parameter("POTM_CORRECTED", "Sea Water Potential Temperature",
                "The potential temperature, in degrees celcius, of the sea water", "degrees_C",
                "sea_water_potential_temperature");
        PSAL_PARAMETER = new Parameter("PSAL_CORRECTED", "Sea Water Salinity",
                "The measured salinity, in practical salinity units (psu) of the sea water ", "psu",
                "sea_water_salinity");

        TEMP_QC = "PROFILE_POTM_QC";
        PSAL_QC = "PROFILE_PSAL_QC";

        VERTICAL_CRS = new VerticalCrsImpl("m", false, false, false);
    }
}