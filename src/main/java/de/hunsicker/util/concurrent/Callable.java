/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.util.concurrent;

/**
 * Interface for runnable actions that bear results and/or throw Exceptions. This
 * interface is designed to provide a common protocol for result-bearing actions that
 * can be run independently in threads, in which case they are ordinarily used as the
 * bases of Runnables that set FutureResults
 * 
 * <p>
 * This class was taken from the util.concurrent package written by Doug Lea. See <a
 * href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html</a>
 * for an introduction to this package.
 * </p>
 *
 * @author <a href="http://gee.cs.oswego.edu/dl">Doug Lea</a>
 *
 * @see FutureResult
 */
public interface Callable
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Perform some action that returns a result or throws an exception.
     *
     * @return the result.
     *
     * @throws Exception if something goes wrong.
     */
    Object call()
      throws Exception;
}
