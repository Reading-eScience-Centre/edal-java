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

/*
 * Copyright (c) 2013 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of The University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.rdg.resc.edal.feature;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import uk.ac.rdg.resc.edal.domain.DiscreteDomain;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.util.Array;

/**
 * A partial implementation of a {@link Feature} containing common functionality
 * 
 * @param <P>
 *            The type of object used to identify positions within the feature's
 *            domain. This may be a spatial, temporal, or combined
 *            spatiotemporal position.
 * @param <DO>
 *            The type of domain object
 * 
 * @author Guy
 */
public abstract class AbstractDiscreteFeature<P, DO> implements DiscreteFeature<P, DO> {

    private final String id;
    private final String name;
    private final String description;
    private final DiscreteDomain<P, DO> domain;
    private final Map<String, Parameter> parameters;
    private final Map<String, ? extends Array<Number>> values;
    private final Properties properties;

    public AbstractDiscreteFeature(String id, String name, String description,
            DiscreteDomain<P, DO> domain, Map<String, Parameter> parameters,
            Map<String, ? extends Array<Number>> values) {
        for (Array<Number> valuesArray : values.values()) {
            if (!Arrays.equals(valuesArray.getShape(), domain.getDomainObjects().getShape())) {
                throw new IllegalArgumentException(
                        "All values arrays in a feature must have the same shape as the domain of the feature.  Your values have shape: "
                                + Arrays.toString(valuesArray.getShape())
                                + ", but your domain has shape: "
                                + Arrays.toString(domain.getDomainObjects().getShape()));
            }
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.domain = domain;
        this.values = values;
        if (parameters != null) {
            this.parameters = parameters;
        } else {
            this.parameters = new HashMap<>();
        }
        /*
         * Create empty properties. Can be populated by using
         * getFeatureProperties().put(..., ...)
         */
        this.properties = new Properties();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<String> getVariableIds() {
        return parameters.keySet();
    }

    @Override
    public Parameter getParameter(String parameterId) {
        return parameters.get(parameterId);
    }

    @Override
    public Map<String, Parameter> getParameterMap() {
        return parameters;
    }

    @Override
    public Array<Number> getValues(String paramId) {
        return values.get(paramId);
    }

    @Override
    public DiscreteDomain<P, DO> getDomain() {
        return domain;
    }

    @Override
    public Properties getFeatureProperties() {
        return properties;
    }
}
