/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.util.concurrent;

/**
 * Interface describing any class that can generate new Thread objects. Using
 * ThreadFactories removes hardwiring of calls to <code>new Thread</code>, enabling
 * applications to use special thread subclasses, default prioritization settings, etc.
 * 
 * <p>
 * This class was taken from the util.concurrent package written by Doug Lea. See <a
 * href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html</a>
 * for an introduction to this package.
 * </p>
 *
 * @author <a href="http://gee.cs.oswego.edu/dl">Doug Lea</a>
 */
public interface ThreadFactory
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Create a new thread that will run the given command when started
     *
     * @param command DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Thread newThread(Runnable command);
}
