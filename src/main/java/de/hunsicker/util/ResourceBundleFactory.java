/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.util;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Serves as the central facility to aquire <code>ResourceBundle</code> objects.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public final class ResourceBundleFactory
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The current locale. */
    private static Locale _locale = Locale.getDefault();

    //~ Methods --------------------------------------------------------------------------

    /**
     * Get the appropriate resource bundle for the given name.
     *
     * @param bundleName the family name of the resource bundle that contains the object
     *        in question.
     *
     * @return the appropriate bundle for the given name.
     *
     * @see java.util.ResourceBundle#getBundle(String)
     */
    public static ResourceBundle getBundle(String bundleName)
    {
        return ResourceBundle.getBundle(bundleName, _locale);
    }


    /**
     * Sets the current locale of the factory.
     *
     * @param locale a locale.
     */
    public static void setLocale(Locale locale)
    {
        _locale = locale;
    }


    /**
     * Returns the current locale of the factory.
     *
     * @return the current locale.
     */
    public static Locale getLocale()
    {
        return _locale;
    }
}
