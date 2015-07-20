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

package uk.ac.rdg.resc.edal.catalogue.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.rdg.resc.edal.graphics.style.util.FeatureCatalogue;

/**
 * The required information to configure an in-memory cache in a
 * {@link FeatureCatalogue}
 *
 * @author Guy Griffiths
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CacheInfo {
    @XmlAttribute(name = "enabled")
    private boolean enabled = false;
    @XmlElement(name = "inMemorySizeMB")
    private int inMemorySizeMB = 256;
    @XmlElement(name = "elementLifetimeMinutes")
    private float elementLifetimeMinutes = 0;

    public CacheInfo() {
    }

    public CacheInfo(boolean enabled, int inMemorySizeMB, float elementLifetimeMinutes) {
        this.enabled = enabled;
        this.inMemorySizeMB = inMemorySizeMB;
        this.elementLifetimeMinutes = elementLifetimeMinutes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getInMemorySizeMB() {
        return inMemorySizeMB;
    }

    public void setInMemorySizeMB(int inMemorySizeMB) {
        this.inMemorySizeMB = inMemorySizeMB;
    }

    public void setElementLifetimeMinutes(float elementLifetimeMinutes) {
        this.elementLifetimeMinutes = elementLifetimeMinutes;
    }

    public float getElementLifetimeMinutes() {
        return elementLifetimeMinutes;
    }
}
