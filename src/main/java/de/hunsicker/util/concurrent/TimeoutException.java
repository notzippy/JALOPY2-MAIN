/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.util.concurrent;

/**
 * Thrown by synchronization classes that report timeouts via exceptions. The exception
 * is treated as a form (subclass) of InterruptedException. This both simplifies
 * handling, and conceptually reflects the fact that timed-out operations are
 * artificially interrupted by timers.
 * 
 * <p>
 * This class was taken from the util.concurrent package written by Doug Lea. See <a
 * href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html</a>
 * for an introduction to this package.
 * </p>
 *
 * @author <a href="http://gee.cs.oswego.edu/dl">Doug Lea</a>
 */
public class TimeoutException
    extends InterruptedException
{
    //~ Instance variables ---------------------------------------------------------------

    /**
     * The approximate time that the operation lasted before this timeout exception was
     * thrown.
     */
    public final long duration;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Constructs a TimeoutException with given duration value.
     *
     * @param time DOCUMENT ME!
     */
    public TimeoutException(long time)
    {
        duration = time;
    }


    /**
     * Constructs a TimeoutException with the specified duration value and detail
     * message.
     *
     * @param time DOCUMENT ME!
     * @param message DOCUMENT ME!
     */
    public TimeoutException(
        long   time,
        String message)
    {
        super(message);
        duration = time;
    }
}
