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

package uk.ac.rdg.resc.godiva.client.widgets;

import uk.ac.rdg.resc.godiva.client.state.InfoIF;

import com.google.gwt.user.client.ui.Label;

/**
 * Implementation of the {@link InfoIF} which just displays the information as a
 * {@link Label}
 * 
 * @author Guy Griffiths
 * 
 */
public class Info extends BaseSelector implements InfoIF {
    private Label info;

    public Info() {
        super("Copyright");
        info = new Label();
        info.setStylePrimaryName("labelStyle");
        info.addStyleDependentName("light");
        label.setTitle("Information about the data");
        add(info);
    }

    @Override
    public void setInfo(String info) {
        this.info.setText(info);
        this.info.setTitle("Information about the data");
        setEnabled(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            info.removeStyleDependentName("inactive");
            label.removeStyleDependentName("inactive");
        } else {
            info.addStyleDependentName("inactive");
            label.addStyleDependentName("inactive");
        }
    }

    @Override
    public boolean hasInfo() {
        return (info.getText() != null && !info.getText().equals(""));
    }

    @Override
    public String getInfo() {
        return info.getText();
    }
}
