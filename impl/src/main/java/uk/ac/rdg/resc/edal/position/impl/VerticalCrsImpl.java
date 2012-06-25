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

package uk.ac.rdg.resc.edal.position.impl;

import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

public class VerticalCrsImpl implements VerticalCrs {

    private final PositiveDirection posDir;
    private final Unit units;
    private final Boolean isPressure;
    
    public VerticalCrsImpl(Unit units, PositiveDirection posDir, boolean isPressure) {
        this.units = units;
        this.posDir = posDir;
        this.isPressure = isPressure;
    }
    
    @Override
    public PositiveDirection getPositiveDirection() {
        return posDir;
    }

    @Override
    public Unit getUnits() {
        return units;
    }

    @Override
    public boolean isDimensionless() {
        if(units == null)
            return true;
        else
            return units.getUnitString().equals("");
    }

    @Override
    public boolean isPressure() {
        return isPressure;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof VerticalCrsImpl){
            VerticalCrsImpl vCrs = (VerticalCrsImpl) obj;
            return (vCrs.isPressure == isPressure) && (vCrs.posDir == posDir)
                    && (vCrs.units.equals(units));
        } else {
            return false;
        }
    }
}
