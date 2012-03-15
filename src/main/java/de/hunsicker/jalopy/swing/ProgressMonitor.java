/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

/**
 * Common interface for objects that monitor the progress of an operation.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public interface ProgressMonitor
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the canceled state to the given value.
     *
     * @param state <code>true</code> to request a cancelation of the monitored
     *        operation.
     */
    public void setCanceled(boolean state);


    /**
     * Indicates whether the user canceled the monitored operation.
     *
     * @return <code>true</code> if the user canceled the monitored operation.
     */
    public boolean isCanceled();


    /**
     * Sets amount of work units done to <em>units</em>.
     *
     * @param units the amount of work units done.
     */
    public void setProgress(int units);


    /**
     * Returns the amount of work units done.
     *
     * @return the amount of work units done.
     */
    public int getProgress();


    /**
     * Sets the description to be displayed.
     *
     * @param text description to be displayed.
     */
    public void setText(String text);


    /**
     * Begins the monitoring of an operation.
     *
     * @param text description to be displayed.
     * @param units amount of work units to be done.
     */
    public void begin(
        String text,
        int    units);


    /**
     * Notifies that the worker is done. Indicates that either the operation is completed
     * or the user canceled it.
     *
     * @see #setCanceled
     */
    public void done();
}
