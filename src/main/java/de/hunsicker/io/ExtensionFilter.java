/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.io;

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;

import de.hunsicker.util.ResourceBundleFactory;


/**
 * ExtensionFilter implements a FilenameFilter driven by a file extension.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public final class ExtensionFilter
    implements FilenameFilter
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String BUNDLE_NAME = "de.hunsicker.io.Bundle" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    /** The extension we use. */
    private String _ext;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Extension filter object.
     *
     * @param ext the extension string we filter.
     *
     * @throws IllegalArgumentException if <code>ext == null</code> or <code>ext.length()
     *         == 0</code>
     */
    public ExtensionFilter(String ext)
    {
        if ((ext == null) || (ext.length() == 0))
        {
            Object[] args = { ext };
            throw new IllegalArgumentException(
                MessageFormat.format(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "INVALID_EXTENSION" /* NOI18N */), args));
        }

        _ext = ext.trim();

        if (!_ext.startsWith("." /* NOI18N */))
        {
            _ext = '.' + _ext;
        }
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param dir the directory in which the file was found.
     * @param name the name of the file.
     *
     * @return <code>true</code> if the given file was accepted.
     */
    public boolean accept(
        File   dir,
        String name)
    {
        File file = new File(dir, name);

        if (file.isDirectory() || name.endsWith(_ext))
        {
            return true;
        }

        return false;
    }
}
