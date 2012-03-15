/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.util.concurrent;

/**
 * Base class for Executors and related classes that rely on thread factories. Generally
 * intended to be used as a mixin-style abstract class, but can also be used
 * stand-alone.
 * 
 * <p>
 * This class was taken from the util.concurrent package written by Doug Lea. See <a
 * href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html</a>
 * for an introduction to this package.
 * </p>
 *
 * @author <a href="http://gee.cs.oswego.edu/dl">Doug Lea</a>
 */
public class ThreadFactoryUser
{
    //~ Instance variables ---------------------------------------------------------------

    /** DOCUMENT ME! */
    protected ThreadFactory threadFactory_ = new DefaultThreadFactory();

    //~ Methods --------------------------------------------------------------------------

    /**
     * Set the factory for creating new threads. By default, new threads are created
     * without any special priority, threadgroup, or status parameters. You can use a
     * different factory to change the kind of Thread class used or its construction
     * parameters.
     *
     * @param factory the factory to use
     *
     * @return the previous factory
     */
    public synchronized ThreadFactory setThreadFactory(ThreadFactory factory)
    {
        ThreadFactory old = threadFactory_;
        threadFactory_ = factory;

        return old;
    }


    /**
     * Get the factory for creating new threads.
     *
     * @return DOCUMENT ME!
     */
    public synchronized ThreadFactory getThreadFactory()
    {
        return threadFactory_;
    }

    //~ Inner Classes --------------------------------------------------------------------

    protected static class DefaultThreadFactory
        implements ThreadFactory
    {
        public Thread newThread(Runnable command)
        {
            return new Thread(command);
        }
    }
}
