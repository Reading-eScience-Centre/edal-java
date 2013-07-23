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
 * Defines a partial unary function, which explicitly advertises the values of A
 * for which the function is defined.
 * </p>
 * <p>
 * <i>Note: this approach is borrowed from the Scala language.</i>
 * </p>
 * 
 * @param <A>
 *            The type of the function input
 * @param <B>
 *            The type of the function output
 * @author Jon Blower
 */
public interface PartialFunction<A, B> extends Function<A, B> {

    /**
     * @return the set of positions for which the partial function is defined.
     */
    public Domain<A> getDomain();

    /**
     * Returns true if the function is defined at the given input value. If the
     * function is defined at this value (i.e. the domain contains the value)
     * then {@link #evaluate(java.lang.Object) evaluate()} will return a
     * non-null value.
     * 
     * @param val
     *            The input value to test
     * @return true if the function is defined at the given input value, false
     *         otherwise.
     */
    public boolean isDefinedAt(A val);

}
