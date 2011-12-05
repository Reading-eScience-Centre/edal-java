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
package uk.ac.rdg.resc.edal.cdm.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.oro.io.GlobFilenameFilter;

public class FileUtils {
    /**
     * Expands a glob expression to give a List of absolute paths to files. This
     * method recursively searches directories, allowing for glob expressions
     * like {@code "c:\\data\\200[6-7]\\*\\1*\\A*.nc"}.
     * 
     * @return a a List of absolute paths to files matching the given glob
     *         expression
     * @throws IllegalArgumentException
     *             if the glob expression does not represent an absolute path
     *             (according to {@code new File(globExpression).isAbsolute()}).
     * @author Mike Grant, Plymouth Marine Labs; Jon Blower
     */
    public static List<File> expandGlobExpression(String globExpression) {
        // Check that the glob expression represents an absolute path. Relative
        // paths would cause unpredictable and platform-dependent behaviour so
        // we disallow them.
        File globFile = new File(globExpression);
        if (!globFile.isAbsolute()) {
            throw new IllegalArgumentException("Location must be an absolute path");
        }

        // Break glob pattern into path components. To do this in a reliable
        // and platform-independent way we use methods of the File class, rather
        // than String.split().
        List<String> pathComponents = new ArrayList<String>();
        while (globFile != null) {
            // We "pop off" the last component of the glob pattern and place
            // it in the first component of the pathComponents List. We
            // therefore
            // ensure that the pathComponents end up in the right order.
            File parent = globFile.getParentFile();
            // For a top-level directory, getName() returns an empty string,
            // hence we use getPath() in this case
            String pathComponent = parent == null ? globFile.getPath() : globFile.getName();
            pathComponents.add(0, pathComponent);
            globFile = parent;
        }

        // We must have at least two path components: one directory and one
        // filename or glob expression
        List<File> searchPaths = new ArrayList<File>();
        searchPaths.add(new File(pathComponents.get(0)));
        int i = 1; // Index of the glob path component

        while (i < pathComponents.size()) {
            FilenameFilter globFilter = new GlobFilenameFilter(pathComponents.get(i));
            List<File> newSearchPaths = new ArrayList<File>();
            // Look for matches in all the current search paths
            for (File dir : searchPaths) {
                if (dir.isDirectory()) {
                    // Workaround for automounters that don't make filesystems
                    // appear unless they're poked
                    // do a listing on searchpath/pathcomponent whether or not
                    // it exists, then discard the results
                    new File(dir, pathComponents.get(i)).list();

                    for (File match : dir.listFiles(globFilter)) {
                        newSearchPaths.add(match);
                    }
                }
            }
            // Next time we'll search based on these new matches and will use
            // the next globComponent
            searchPaths = newSearchPaths;
            i++;
        }

        // Now we've done all our searching, we'll only retain the files from
        // the list of search paths
        List<File> files = new ArrayList<File>();
        for (File path : searchPaths) {
            if (path.isFile())
                files.add(path);
        }
        return files;
    }
}
