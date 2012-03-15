/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.util.concurrent;

import java.lang.reflect.InvocationTargetException;


/**
 * A class maintaining a single reference variable serving as the result of an operation.
 * The result cannot be accessed until it has been set.
 * 
 * <p>
 * <b>Sample Usage</b>
 * </p>
 * 
 * <p>
 * <pre class="snippet">
 * class ImageRenderer {
 *     Image render(byte[] raw);
 * }
 * class App {
 *     Executor executor = ...
 *     ImageRenderer renderer = ...
 *     void display(byte[] rawimage) {
 *         try {
 *             FutureResult futureImage = new FutureResult();
 *             Runnable command = futureImage.setter(new Callable() {
 *                 public Object call() { return renderer.render(rawImage); }
 *             });
 *             executor.execute(command);
 *             drawBorders();             // do other things while executing
 *             drawCaption();
 *             drawImage((Image)(futureImage.get())); // use future
 *         } catch (InterruptedException ex) {
 *             return;
 *         } catch (InvocationTargetException ex) {
 *             cleanup();
 *             return;
 *         }
 *     }
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * This class was taken from the util.concurrent package written by Doug Lea. See <a
 * href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html</a>
 * for an introduction to this package.
 * </p>
 *
 * @author <a href="http://gee.cs.oswego.edu/dl">Doug Lea</a>
 *
 * @see Executor
 */
public class FutureResult
{
    //~ Instance variables ---------------------------------------------------------------

    /** the exception encountered by operation producing result */
    protected InvocationTargetException exception_ = null;

    /** The result of the operation */
    protected Object value_ = null;

    /** Status -- true after first set */
    protected boolean ready_ = false;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Create an initially unset FutureResult
     */
    public FutureResult()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Set the exception field, also setting ready status.
     *
     * @param ex The exception. It will be reported out wrapped within an
     *        InvocationTargetException
     */
    public synchronized void setException(Throwable ex)
    {
        exception_ = new InvocationTargetException(ex);
        ready_ = true;
        notifyAll();
    }


    /**
     * Get the exception, or null if there isn't one (yet). This does not wait until the
     * future is ready, so should ordinarily only be called if you know it is.
     *
     * @return the exception encountered by the operation setting the future, wrapped in
     *         an InvocationTargetException
     */
    public synchronized InvocationTargetException getException()
    {
        return exception_;
    }


    /**
     * Return whether the reference or exception have been set.
     *
     * @return true if has been set. else false
     */
    public synchronized boolean isReady()
    {
        return ready_;
    }


    /**
     * Clear the value and exception and set to not-ready, allowing this FutureResult to
     * be reused. This is not particularly recommended and must be done only when you
     * know that no other object is depending on the properties of this FutureResult.
     */
    public synchronized void clear()
    {
        value_ = null;
        exception_ = null;
        ready_ = false;
    }


    /**
     * Access the reference, waiting if necessary until it is ready.
     *
     * @return current value
     *
     * @exception InterruptedException if current thread has been interrupted
     * @exception InvocationTargetException if the operation producing the value
     *            encountered an exception.
     */
    public synchronized Object get()
      throws InterruptedException, InvocationTargetException
    {
        while (!ready_)
        {
            wait();
        }

        return doGet();
    }


    /**
     * Access the reference, even if not ready
     *
     * @return current value
     */
    public synchronized Object peek()
    {
        return value_;
    }


    /**
     * Set the reference, and signal that it is ready. It is not considered an error to
     * set the value more than once, but it is not something you would normally want to
     * do.
     *
     * @param newValue The value that will be returned by a subsequent get();
     */
    public synchronized void set(Object newValue)
    {
        value_ = newValue;
        ready_ = true;
        notifyAll();
    }


    /**
     * Return a Runnable object that, when run, will set the result value.
     *
     * @param function - a Callable object whose result will be held by this
     *        FutureResult.
     *
     * @return A Runnable object that, when run, will call the function and (eventually)
     *         set the result.
     */
    public Runnable setter(final Callable function)
    {
        return new Runnable()
            {
                public void run()
                {
                    try
                    {
                        set(function.call());
                    }
                    catch (Throwable ex)
                    {
                        setException(ex);
                    }
                }
            };
    }


    /**
     * Wait at most msecs to access the reference.
     *
     * @param msecs Number of msecs
     *
     * @return current value
     *
     * @exception TimeoutException if not ready after msecs
     * @exception InterruptedException if current thread has been interrupted
     * @exception InvocationTargetException if the operation producing the value
     *            encountered an exception.
     */
    public synchronized Object timedGet(long msecs)
      throws TimeoutException, InterruptedException, InvocationTargetException
    {
        long startTime = (msecs <= 0) ? 0
                                      : System.currentTimeMillis();
        long waitTime = msecs;

        if (ready_)
        {
            return doGet();
        }
        else if (waitTime <= 0)
        {
            throw new TimeoutException(msecs);
        }
        else
        {
            for (;;)
            {
                wait(waitTime);

                if (ready_)
                {
                    return doGet();
                }
                waitTime = msecs - (System.currentTimeMillis() - startTime);

                if (waitTime <= 0)
                {
                    throw new TimeoutException(msecs);
                }
            }
        }
    }


    /**
     * internal utility: either get the value or throw the exception
     *
     * @return The object
     *
     * @throws InvocationTargetException On error
     */
    protected Object doGet()
      throws InvocationTargetException
    {
        if (exception_ != null)
        {
            throw exception_;
        }
        return value_;
    }
}
