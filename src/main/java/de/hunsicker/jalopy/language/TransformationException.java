/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

/**
 * Indicates an error during a transformation.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public class TransformationException
    extends Exception
{
    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new TransformationException.
     *
     * @param msg the error message.
     */
    public TransformationException(String msg)
    {
        super(msg);
    }
}
