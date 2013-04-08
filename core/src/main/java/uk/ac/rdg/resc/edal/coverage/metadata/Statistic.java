/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
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

package uk.ac.rdg.resc.edal.coverage.metadata;

import uk.ac.rdg.resc.edal.Phenomenon;

/**
 * A descriptor for a statistical measure
 * 
 * @param <N>
 *            the type of the values used to describe the statistic (will
 *            usually be Double, maybe occasionally Float or even BigDecimal).
 * @author Jon
 */
public interface Statistic extends ScalarMetadata {

    /*
     * TODO This is all named types of uncertML. Most are not used.
     */
    public enum StatisticType {
        CENTRED_MOMENT, COEFFICIENT_OF_VARIATION, CONFIDENCE_INTERVAL, CONFUSION_MATRIX,
        CORRELATION, COVARIANCE_MATRIX, CREDIBLE_INTERVAL, DECILE, DISCRETE_PROBABILITY,
        INTERQUARTILE_RANGE, KURTOSIS, MEAN, MEDIAN, MODE, MOMENT, PERCENTILE, PROBABILITY,
        QUANTILE, QUARTILE, RANGE, SKEWNESS, STANDARD_DEVIATION, VARIANCE, LOWER_CONFIDENCE_BOUND, UPPER_CONFIDENCE_BOUND
    }

    /**
     * Returns an identifier indicating the type of the statistic (mean,
     * variance, median etc).
     */
    public StatisticType getStatisticType();

    @Override
    public StatisticsCollection getParent();

    /**
     * {@inheritDoc}
     * <p>
     * This will usually match the Phenomenon of the parent
     * {@link StatisticsCollection}. However, in some vocabularies, different
     * statistical quantities may be expressed using different terms.
     * </p>
     */
    @Override
    public Phenomenon getParameter();

}
