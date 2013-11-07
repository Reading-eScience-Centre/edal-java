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

package uk.ac.rdg.resc.edal.graphics.style;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = MapImage.NAMESPACE, propOrder = {}, name = "PatternScaleType")
public class PatternScale {

    @XmlElement(name = "PatternBands")
    private int nLevels = 10;
    
    @XmlElement(name = "OpaqueValue", required = true)
    private Float opaqueValue;
    
    @XmlElement(name = "TransparentValue", required = true)
    private Float transparentValue;
    
    @XmlElement(name = "Logarithmic")
    private Boolean logarithmic = false;
    
    @SuppressWarnings("unused")
    private PatternScale(){}
    
    public PatternScale(int nLevels, Float transparentValue, Float opaqueValue, Boolean logarithmic) {
        super();
        this.nLevels = nLevels;
        this.opaqueValue = opaqueValue;
        this.transparentValue = transparentValue;
        this.logarithmic = logarithmic;
    }

    public int getNLevels() {
        return nLevels;
    }

    public Float getOpaqueValue() {
        return opaqueValue;
    }

    public Float getTransparentValue() {
        return transparentValue;
    }

    /**
     * Gets the stippling level from 0 to nLevels - 1
     * 
     * The highest level represents solid colour, and zero represents
     * transparency
     * 
     * Anything between the values returned by getTransparentValue() and
     * getOpaqueValue() will be partially stippled. Outside of the range, either
     * solid black or completely transparent will be returned
     * 
     * @param value
     *            The data value
     * @return The stippling level
     */
    public int getLevel(Number value) {
        if(value == null || Float.isNaN(value.floatValue())) {
            return 0;
        }
        
        if(opaqueValue > transparentValue) {
            if(value.floatValue() < transparentValue) {
                return 0;
            } else if(value.floatValue() >= opaqueValue) {
                return nLevels - 1;
            }
            if(logarithmic) {
                return (int) Math.floor(1+(nLevels-2) 
                        * ((Math.log(value.floatValue()) - Math.log(transparentValue)) 
                                / (Math.log(opaqueValue) - Math.log(transparentValue)))) ;
            } else {
                return (int) Math.floor(1+(nLevels-2) 
                        * ((value.floatValue() - transparentValue) 
                                / (opaqueValue - transparentValue))) ;
            }
        } else {
            if(value.floatValue() >= transparentValue) {
                return 0;
            } else if(value.floatValue() < opaqueValue) {
                return nLevels - 1;
            }
            if(logarithmic) {
                return nLevels - 2 
                        - (int) Math.floor(2+(nLevels-2) 
                                * ((Math.log(transparentValue)- Math.log(value.floatValue())) 
                                        / (Math.log(opaqueValue) - Math.log(transparentValue)))) ;
            } else {
                return nLevels - 2 
                        - (int) Math.floor(2+(nLevels-2) 
                                * ((transparentValue - value.floatValue()) 
                                        / (opaqueValue - transparentValue)));
            }
        }
    }
    
    public static void main(String[] args) {
        PatternScale s = new PatternScale(4, 40f, 0f, false);
        System.out.println(s.getLevel(-0.1));
        System.out.println();
        System.out.println(s.getLevel(0.0));
        System.out.println(s.getLevel(5.0));
        System.out.println(s.getLevel(10.0));
        System.out.println(s.getLevel(15.0));
        System.out.println(s.getLevel(19.99999));
        System.out.println();
        System.out.println();
        System.out.println(s.getLevel(20.0));
        System.out.println(s.getLevel(25.0));
        System.out.println(s.getLevel(29.99999));
        System.out.println(s.getLevel(30.0));
        System.out.println(s.getLevel(35.0));
        System.out.println(s.getLevel(39.99999));
        System.out.println();
        System.out.println();
        System.out.println(s.getLevel(40.0));
    }
    
}
