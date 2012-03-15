/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.util;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * Resembles the JDK 1.4 exception chaining facility.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class ChainingRuntimeException
    extends RuntimeException
{
    //~ Instance variables ---------------------------------------------------------------

    /** Causing throwable. */
    protected Throwable cause;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ChainingRuntimeException object.
     */
    public ChainingRuntimeException()
    {
    }


    /**
     * Creates a new ChainingRuntimeException object.
     *
     * @param message error message.
     */
    public ChainingRuntimeException(String message)
    {
        super(message);
    }


    /**
     * Creates a new ChainingRuntimeException object.
     *
     * @param message error message.
     * @param cause throwable which caused the error.
     */
    public ChainingRuntimeException(
        String    message,
        Throwable cause)
    {
        super(message);
        this.cause = cause;
    }


    /**
     * Creates a new ChainingRuntimeException object.
     *
     * @param cause throwable which caused the error.
     */
    public ChainingRuntimeException(Throwable cause)
    {
        super((cause == null) ? "" /* NOI18N */
                              : cause.getLocalizedMessage());
        this.cause = cause;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the causing throwable.
     *
     * @return throwable which caused the exception.
     */
    public Throwable getCause()
    {
        return ((this.cause == null) ? this
                                     : this.cause);
    }


    /**
     * Prints this <code>Throwable</code> (the cause if available) and its backtrace to
     * the specified print writer.
     *
     * @param writer writer to use for output.
     */
    public void printStackTrace(PrintWriter writer)
    {
        if (this.cause != null)
        {
            String msg = super.getLocalizedMessage();
            writer.println((msg != null) ? msg
                                         : "Exception occured");
            writer.print("Nested Exception is: ");
            this.cause.printStackTrace(writer);
        }
        else
        {
            super.printStackTrace(writer);
        }
    }


    /**
     * Prints this <code>Throwable</code> (the cause if available) and its backtrace to
     * the specified print writer.
     *
     * @param s stream to use for output.
     */
    public void printStackTrace(PrintStream s)
    {
        printStackTrace(new PrintWriter(new OutputStreamWriter(s)));
    }


    /**
     * Prints this <code>Throwable</code> (the cause if available) and its backtrace to
     * the standard error stream.
     */
    public void printStackTrace()
    {
        printStackTrace(System.err);
    }
}
