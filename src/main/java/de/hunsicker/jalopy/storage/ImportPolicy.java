/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.storage;

/**
 * Represents an import policy.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public final class ImportPolicy
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Apply no import optimization. */
    public static final ImportPolicy DISABLED =
        new ImportPolicy("disabled" /* NOI18N */, "ImportPolicy [disabled]");

    /** Expand on-demand import statements. */
    public static final ImportPolicy EXPAND =
        new ImportPolicy("expand" /* NOI18N */, "ImportPolicy [expand]");

    /** Collapse single-type import statements. */
    public static final ImportPolicy COLLAPSE =
        new ImportPolicy("collapse" /* NOI18N */, "ImportPolicy [collapse]");

    //~ Instance variables ---------------------------------------------------------------

    private String _displayName;

    /** The unique policy name. */
    private String _name;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ImportPolicy object.
     *
     * @param name name of the policy.
     * @param name a name suitable for displaying to users.
     */
    private ImportPolicy(
        String name,
        String displayName)
    {
        _name = name.intern();
        _displayName = displayName;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the ImportPolicy for the given name.
     *
     * @param name a valid policy name. Either &quot;expand&quot;, &quot;collapse&quot;
     *        or &quot;disabled&quot;.
     *
     * @return the corresponding policy for the given name.
     *
     * @throws IllegalArgumentException if <em>name</em> is no valid policy name.
     */
    public static ImportPolicy valueOf(String name)
    {
        String n = name.intern();

        if (n == EXPAND._name)
        {
            return EXPAND;
        }
        else if (n == COLLAPSE._name)
        {
            return COLLAPSE;
        }
        else if (n == DISABLED._name)
        {
            return DISABLED;
        }

        throw new IllegalArgumentException("invalid policy name -- " + name);
    }


    /**
     * Returns the unique name of this policy.
     *
     * @return the unique name of this policy.
     */
    public String getName()
    {
        return _name;
    }


    /**
     * Returns a string representation of this object.
     *
     * @return A string representation of this object.
     */
    public String toString()
    {
        return _displayName;
    }
}
