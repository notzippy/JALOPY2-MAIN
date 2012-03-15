/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;


/**
 * Filters is used to build a filter list to apply for a search.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public class Filters
    implements FilenameFilter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** If one filter accepts a file, the file will be accepted. */
    public static final int POLICY_LAZY = 2;

    /** Only if all filters accept a file, the file will be accepted. */
    public static final int POLICY_STRICT = 1;

    //~ Instance variables ---------------------------------------------------------------

    /** Comparator for filters. */
    private FilterComparator _filterComp;

    /** Holds the specified filters. */
    private FilenameFilter[] _filters;

    /** Used filter policy. */
    private int _policy = POLICY_LAZY;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Filters object. Uses {@link #POLICY_STRICT} as its policy.
     */
    public Filters()
    {
        this(POLICY_LAZY);
    }


    /**
     * Creates a new Filters object.
     *
     * @param policy policy to use. Either {@link #POLICY_STRICT} or {@link
     *        #POLICY_LAZY}.
     */
    public Filters(int policy)
    {
        _policy = policy;
        _filters = new FilenameFilter[0];
        _filterComp = new FilterComparator();
        Arrays.sort(_filters, _filterComp);
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the filter policy to use.
     *
     * @param policy policy to use. Either {@link #POLICY_STRICT} or {@link
     *        #POLICY_LAZY}.
     */
    public void setPolicy(int policy)
    {
        _policy = policy;
    }


    /**
     * Returns the policy currently being used.
     *
     * @return used filter policy.
     *
     * @see #setPolicy
     */
    public int getPolicy()
    {
        return _policy;
    }


    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param dir the directory in which the file was found.
     * @param name the name of the file.
     *
     * @return <code>true</code> if and only if the name should be included in the file
     *         list.
     */
    public boolean accept(
        File   dir,
        String name)
    {
        if (_policy == POLICY_STRICT)
        {
            return strictAccept(dir, name);
        }

        return lazyAccept(dir, name);
    }


    /**
     * Adds the given filter to the filter list.
     *
     * @param filter filter to add.
     */
    public synchronized void addFilter(FilenameFilter filter)
    {
        if (Arrays.binarySearch(_filters, filter, _filterComp) < 0)
        {
            FilenameFilter[] tmp = new FilenameFilter[_filters.length + 1];
            System.arraycopy(_filters, 0, tmp, 0, _filters.length);
            tmp[_filters.length] = filter;
            _filters = tmp;
        }
    }


    /**
     * Indicates wheter the given filter is contained in the filter list.
     *
     * @param filter filter.
     *
     * @return <code>true</code> if the filter was registered.
     */
    public synchronized boolean contains(FilenameFilter filter)
    {
        if (Arrays.binarySearch(_filters, filter, _filterComp) > -1)
        {
            return true;
        }

        return false;
    }


    /**
     * Removes the given filter.
     *
     * @param filter filter to remove.
     */
    public synchronized void removeFilter(FilenameFilter filter)
    {
        int length = _filters.length;
        int found = Arrays.binarySearch(_filters, filter, _filterComp);

        if (found > -1)
        {
            FilenameFilter[] tmp = new FilenameFilter[length - 1];
            System.arraycopy(_filters, 0, tmp, 0, length - (length - found));
            System.arraycopy(_filters, found + 1, tmp, found, length - found - 1);
            _filters = tmp;
        }
    }


    /**
     * Gibt an, ob die angegebene Datei akzeptiert wird oder nicht. Die Datei wird dann
     * akzeptiert, wenn ein oder mehrere Filter die Datei akzeptieren.
     *
     * @param dir Verzeichnis, in dem die Datei gefunden wurde.
     * @param name Name der Datei.
     *
     * @return <code>true</code> wenn die Datei akzeptiert wird, <code>false </code> wenn
     *         nicht.
     */
    private boolean lazyAccept(
        File   dir,
        String name)
    {
        if (_filters.length == 0)
        {
            return true;
        }

        for (int i = 0; i < _filters.length; i++)
        {
            if (_filters[i].accept(dir, name))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Gibt an, ob die angegebene Datei akzeptiert wird oder nicht. Die Datei wird dann
     * akzeptiert, wenn alle Filter die Datei akzeptieren.
     *
     * @param dir Verzeichnis, in dem die Datei gefunden wurde.
     * @param name Name der Datei.
     *
     * @return <code>true</code> wenn die Datei akzeptiert wird, <code>false </code> wenn
     *         nicht.
     */
    private boolean strictAccept(
        File   dir,
        String name)
    {
        // recurse into directories
        if (new File(dir + File.separator + name).isDirectory())
        {
            return true;
        }

        for (int i = 0; i < _filters.length; i++)
        {
            if (!_filters[i].accept(dir, name))
            {
                return false;
            }
        }

        return true;
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Comparator to compare filename filters.
     */
    private static class FilterComparator
        implements Comparator
    {
        public int compare(
            Object first,
            Object second)
        {
            if (first == null)
            {
                if (second == null)
                {
                    return 0;
                }

                return -1;
            }
            else if (second == null)
            {
                return 1;
            }

            if (first.equals(second))
            {
                return 0;
            }
            return -1;
        }


        public boolean equals(Object obj)
        {
            return this.equals(obj);
        }
    }
}
