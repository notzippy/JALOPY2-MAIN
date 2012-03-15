/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

/**
 * Indicates that some user specified input violates some data constraint.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public class ValidationException
    extends Exception
{
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ValidationException object.
     */
    public ValidationException()
    {
        super();
    }


    /**
     * Creates a new ValidationException object.
     *
     * @param message the error message.
     */
    public ValidationException(String message)
    {
        super(message);
    }
}
