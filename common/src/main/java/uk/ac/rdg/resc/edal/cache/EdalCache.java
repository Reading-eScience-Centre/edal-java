/*******************************************************************************
 * Copyright (c) 2018 The University of Reading
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

package uk.ac.rdg.resc.edal.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;

public class EdalCache {
    private static final String CACHE_MANAGER = "EDAL-CacheManager";
    private static final int MAX_CACHE_DEPTH = 4_000_000;

    /*
     * We are using an in-memory cache with a configured memory size (as opposed
     * to a configured number of items in memory). This has the advantage that
     * we will get a hard limit on the amount of memory the cache consumes. The
     * disadvantage is that the size of each object needs to be calculated prior
     * to inserting it into the cache.
     * 
     * The maxDepth property specified the maximum number of object references
     * to count before a warning is given.
     * 
     * Now, we are generally caching 2 things:
     * 
     * 1) Gridded map features which will generally have 256*256 ~= 65,000
     * values, but could easily be bigger
     * 
     * 2) Collections of point features. A year's worth of EN3 data could
     * typically contain >15,000 features, each with a number of properties
     * 
     * These can need to count a very large number of object references.
     * However, this calculation is actually pretty quick. Setting the max depth
     * to 4,000,000 seems to suppress the vast majority of warnings, and doesn't
     * impact performance noticeably.
     * 
     * Cache configuration specified in resources/ehcache.xml
     */
    public static final CacheManager cacheManager = CacheManager
            .newInstance(new Configuration().name(CACHE_MANAGER)
                    .sizeOfPolicy(new SizeOfPolicyConfiguration().maxDepth(MAX_CACHE_DEPTH)));
}
