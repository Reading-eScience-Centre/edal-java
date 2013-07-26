/*
 * Copyright (c) 2013 Reading e-Science Centre, University of Reading, UK
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
 * 3. Neither the name of Reading e-Science Centre, University of Reading, UK, nor the names of the
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
package uk.ac.rdg.resc.edal.dataset;

/**
 * Describes a grid of data held by a {@link GridDataset}.
 * @todo should this inherit from VariableMetadata?  Then we could specialize
 * the getXDomain() methods to return more specific types.
 * @author Jon
 */
public interface GridMetadata
{
    
    public String getId();
    
    /**
     * Gets the number of dimensions of the grid.
     */
    public int getNDim();
    
    public int[] getShape();
    
    /**
     * Returns the index of the x axis within the grid
     */
    public int getXAxisIndex();
    
    /**
     * Returns the index of the y axis within the grid
     */
    public int getYAxisIndex();
    
    /**
     * Returns the index of the vertical axis within the grid
     */
    public int getZAxisIndex();
    
    /**
     * Returns the index of the time axis within the grid
     */
    public int getTAxisIndex();
    
}
