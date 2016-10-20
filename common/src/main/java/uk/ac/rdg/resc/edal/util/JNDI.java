/*******************************************************************************
 * Copyright (c) 2016 The University of Reading
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
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2016, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package uk.ac.rdg.resc.edal.util;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.naming.event.NamingListener;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;

import org.apache.sis.internal.metadata.sql.Initializer;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.factory.MultiAuthoritiesFactory;
import org.opengis.util.FactoryException;

/**
 * Tiny JNDI context, used only for specifying an EPSG data source to Apache
 * SIS.
 *
 * @author Martin Desruisseaux (Geomatys)
 * @author Guy Griffiths
 * @module
 */
public final class JNDI implements EventContext, InitialContextFactory {
    private static DataSource epsgDataSource;

    /**
     * Invoked from {@link GISUtils} for setting a pseudo-JNDI environment.
     */
    public static void install(DataSource dataSource) {
        if (!Initializer.hasJNDI()) {
            System.setProperty(INITIAL_CONTEXT_FACTORY, JNDI.class.getName());
            try {
                ((MultiAuthoritiesFactory) CRS.getAuthorityFactory(null)).reload();
            } catch (FactoryException ex) {
                throw new IllegalStateException(ex); // Should never happen.
            }
            epsgDataSource = dataSource;
        }
    }

    /**
     * Can be used to remove our pseudo-JNDI environment.
     */
    public static void uninstall() {
        if (JNDI.class.getName().equals(System.getProperty(INITIAL_CONTEXT_FACTORY))) {
            System.clearProperty(INITIAL_CONTEXT_FACTORY);
        }
    }

    /**
     * Invoked by JNDI for creating the {@link Context}.
     *
     * @param environment
     *            ignored.
     * @return The context.
     */
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) {
        return this;
    }

    /**
     * Returns the object for the given name.
     *
     * @param name
     *            The object name.
     * @return Value associated to the given name.
     * @throws NamingException
     *             if the value can not be obtained.
     */
    @Override
    public Object lookup(final String name) throws NamingException {
        switch (name) {
        case "java:comp/env":
            return this;
        case Initializer.JNDI: {
            return epsgDataSource;
        }
        default:
            throw new NameNotFoundException(name);
        }
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public void unbind(String name) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        throw new NamingException("Not supported yet.");
    }

    @Override
    public void addNamingListener(String target, int scope, NamingListener listener) {
    }

    @Override
    public void removeNamingListener(NamingListener listener) {
    }

    @Override
    public boolean targetMustExist() {
        return false;
    }

    @Override
    public void close() {
    }

    /**
     * Delegates to the methods working on {@link String}.
     *
     * @param name
     *            The name to convert to string.
     * @throws NamingException
     *             if the string-based method failed.
     */
    @Override
    public void bind(Name name, Object obj) throws NamingException {
        bind(name.toString(), obj);
    }

    @Override
    public void unbind(Name name) throws NamingException {
        unbind(name.toString());
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        rebind(name.toString(), obj);
    }

    @Override
    public void rename(Name name, Name newName) throws NamingException {
        rename(name.toString(), newName.toString());
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return list(name.toString());
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return listBindings(name.toString());
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        return getNameParser(name.toString());
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        return getNameParser(name).parse(composeName(name.toString(), prefix.toString()));
    }

    @Override
    public void addNamingListener(Name target, int scope, NamingListener l) throws NamingException {
        addNamingListener(target.toString(), scope, l);
    }
}
