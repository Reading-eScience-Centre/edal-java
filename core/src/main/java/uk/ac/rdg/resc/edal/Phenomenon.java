/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/

package uk.ac.rdg.resc.edal;

/**
 * <p>
 * Simple immutable class consisting of a string and vocabulary that acts as a
 * namespace for the string. Instances of this class are created through the
 * static factory methods, which give the possibility in future to cache
 * instances of this class, saving object creation and garbage collection.
 * </p>
 * 
 * @author Jon Blower
 */
public final class Phenomenon {

    private final String stdName;
    private final PhenomenonVocabulary phenomVocab;

    private Phenomenon(String stdName, PhenomenonVocabulary phenomVocab) {
        this.stdName = stdName;
        this.phenomVocab = phenomVocab;
    }

    /**
     * Gets an instance of a phenomenon with the given standard name in the
     * given vocabulary.
     */
    public static Phenomenon getPhenomenon(String stdName, PhenomenonVocabulary phenomVocab) {
        return new Phenomenon(stdName, phenomVocab);
    }

    /**
     * Gets an instance of a phenomenon with the given standard name in an
     * unknown vocabulary.
     */
    public static Phenomenon getPhenomenon(String stdName) {
        return getPhenomenon(stdName, PhenomenonVocabulary.UNKNOWN);
    }

    /**
     * @return a {@link String} containing the standard name of this
     *         {@link Phenomenon}
     */
    public String getStandardName() {
        return stdName;
    }

    /**
     * @return the {@link PhenomenonVocabulary} of which this {@link Phenomenon}
     *         is a part of
     */
    public PhenomenonVocabulary getVocabulary() {
        return phenomVocab;
    }

}
