/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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

package uk.ac.rdg.resc.edal.ncwms;

import java.util.ArrayList;
import java.util.List;

import winstone.AuthenticationPrincipal;
import winstone.AuthenticationRealm;

/**
 * A class providing authentication for the ncWMS-admin role, needed to perform
 * admin in ncWMS. This gives access to any user and is used when running ncWMS
 * as a standalone (i.e. inherently local) application using the Winstone
 * servlet container.
 *
 * @author Guy Griffiths
 */
public class StandaloneAuthentication implements AuthenticationRealm {
    private List<String> roles;

    public StandaloneAuthentication() {
        roles = new ArrayList<>();
        roles.add("ncWMS-admin");
    }

    @Override
    public AuthenticationPrincipal authenticateByUsernamePassword(String userName, String password) {
        return new AuthenticationPrincipal("admin", "", roles);
    }

    @Override
    public AuthenticationPrincipal retrieveUser(String user) {
        return new AuthenticationPrincipal("admin", "", roles);
    }

}
