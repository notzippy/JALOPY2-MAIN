/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.util;

/**
 * A few asorted helper routines.
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public final class Helper
{
    //~ Constructors ---------------------------------------------------------------------

    private Helper()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Loads the class with the specified name. This method first attempts  to load the
     * class with the current context classloader and only if the search failed, it
     * tries to load the class with the class loader of the given object.
     *
     * @param name the name of the class.
     * @param o the object which classloader should be used.
     *
     * @return the result {@link java.lang.Class} object.
     *
     * @throws ClassNotFoundException if the class was not found.
     */
    public static Class loadClass(
        final String name,
        final Object o)
      throws ClassNotFoundException
    {
        return loadClass(name, o.getClass().getClassLoader());
    }


    /**
     * Loads the class with the specified name. This method first attempts  to load the
     * class with the current context classloader and only if the search failed, it
     * tries to load the class with the given class loader.
     *
     * @param name the name of the class.
     * @param loader the classloader to load the class.
     *
     * @return the result {@link java.lang.Class} object.
     *
     * @throws ClassNotFoundException if the class was not found.
     */
    private static Class loadClass(
        final String      name,
        final ClassLoader loader)
      throws ClassNotFoundException
    {
        ClassLoader l = Thread.currentThread().getContextClassLoader();

        if (l != null)
        {
            try
            {
                return l.loadClass(name);
            }
            catch (ClassNotFoundException ignored)
            {
                // fall back to the given classloader
            }
        }

        return loader.loadClass(name);
    }
}
