/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.storage;

import java.io.Serializable;


/**
 * Represents a project to associate specific settings with.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 *
 * @since 1.0b8
 */
public final class Project
    implements Serializable
{
    //~ Static variables/initializers ----------------------------------------------------

    static final long serialVersionUID = -4874682073931915199L;

    //~ Instance variables ---------------------------------------------------------------

    /** The project name. */
    private final String _name;

    /** The project description. */
    private String _description;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Project object.
     *
     * @param name the project name. The name must not contain one of the following
     *        characters: <code>\ / :  ? " ' &lt; &gt; |</code>.
     * @param description the project description.
     */
    public Project(
        String name,
        String description)
    {
        setDescription(description);
        validate(name, '\\');
        validate(name, '/');
        validate(name, ':');
        validate(name, '*');
        validate(name, '?');
        validate(name, '"');
        validate(name, '\'');
        validate(name, '<');
        validate(name, '>');
        validate(name, '|');
        _name = name;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the project description.
     *
     * @param description new description.
     *
     * @throws IllegalArgumentException if the given description exceeds the maximum
     *         length of 256 characters.
     */
    public void setDescription(String description)
    {
        if (description.length() > 256)
        {
            throw new IllegalArgumentException(
                "description exceeds maximum of 256 -- " + description.length());
        }

        _description = description;
    }


    /**
     * Returns the project description.
     *
     * @return project description.
     */
    public String getDescription()
    {
        return _description;
    }


    /**
     * Returns the project name.
     *
     * @return project name.
     */
    public String getName()
    {
        return _name;
    }


    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean equals(Object o)
    {
        if (o instanceof Project)
        {
            return _name.equals(((Project) o)._name);
        }

        return false;
    }


    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int hashCode()
    {
        return _name.hashCode();
    }


    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     * @param character DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private void validate(
        String name,
        char   character)
    {
        if (name.indexOf(character) > -1)
        {
            throw new IllegalArgumentException("invalid character found -- " + character);
        }
    }
}
