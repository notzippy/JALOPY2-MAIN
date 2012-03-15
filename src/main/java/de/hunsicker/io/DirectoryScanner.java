/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import de.hunsicker.util.ResourceBundleFactory;


/**
 * Class for scanning a directory for files/directories that match a certain filter.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 *
 * @see java.io.FilenameFilter
 * @see org.apache.oro.io.RegexFilenameFilter
 */
public class DirectoryScanner
    implements Runnable
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String BUNDLE_NAME = "de.hunsicker.io.Bundle" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    /** Used filters. */
    private Filters _filters;

    /** Object to synchronize processes on. */
    volatile Object _lock = new Object();

    /** Found files. */
    private Queue _queue;

    /** Directories to search. */
    private File[] _dirs;

    /** Are we finished? */
    private boolean _finished;

    /**
     * The directory depth we scan. Defaults to <code>0</code> (Only the current
     * directory).
     */
    private int _levels = 0;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new DirectoryScanner object. Scans the current user directory.
     */
    public DirectoryScanner()
    {
        this(System.getProperty("user.dir" /* NOI18N */), 0);
    }


    /**
     * Creates a new DirectoryScanner object.
     *
     * @param directory directory to scan.
     */
    public DirectoryScanner(String directory)
    {
        this(directory, Integer.MAX_VALUE);
    }


    /**
     * Creates a new DirectoryScanner object.
     *
     * @param directory directory to scan.
     * @param levels number of levels to scan.
     */
    public DirectoryScanner(
        String directory,
        int    levels)
    {
        this(new File(directory), levels);
    }


    /**
     * Creates a new DirectoryScanner object.
     *
     * @param directory directory to scan.
     */
    public DirectoryScanner(File directory)
    {
        this(new File(directory.getAbsolutePath()), Integer.MAX_VALUE);
    }


    /**
     * Creates a new DirectoryScanner object.
     *
     * @param directory directory to scan.
     * @param levels number of levels to scan.
     */
    public DirectoryScanner(
        File directory,
        int  levels)
    {
        this(addToList(directory), levels);
    }


    /**
     * Creates a new DirectoryScanner object.
     *
     * @param directories directories to scan.
     */
    public DirectoryScanner(Set directories)
    {
        this(directories, Integer.MAX_VALUE);
    }


    /**
     * Creates a new DirectoryScanner object.
     *
     * @param directories directories to scan.
     * @param levels number of levels to scan.
     */
    public DirectoryScanner(
        Set directories,
        int levels)
    {
        _queue = new Queue();
        setTargets(directories);
        _levels = levels;
        _filters = new Filters();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Indicates whether the queue with the found files is empty.
     *
     * @return <code>true</code> if the queue is empty.
     */
    public boolean isEmpty()
    {
        synchronized (_lock)
        {
            return _queue.isEmpty();
        }
    }


    /**
     * Returns an array with the found files.
     *
     * @return the found files.
     */
    public File[] getFiles()
    {
        synchronized (_lock)
        {
            return _queue.toArray();
        }
    }


    /**
     * Sets the filter policy to use.
     *
     * @param policy valid filter policy.
     */
    public void setFilterPolicy(int policy)
    {
        if (_filters != null)
        {
            _filters.setPolicy(policy);
        }
    }


    /**
     * Indicates whether the scan has finished or is still running.
     *
     * @return <code>true</code> if the scan has finished.
     */
    public boolean isFinished()
    {
        synchronized (_lock)
        {
            return _finished;
        }
    }


    /**
     * Sets the directory depth (number of levels) to search.
     *
     * @param level directory depth to search.
     *
     * @throws IllegalStateException if the scanner hasn't finished processing yet
     *         (<code>isFinished() == true</code>)
     *
     * @see #isFinished
     */
    public void setMaxLevels(int level)
    {
        if (isFinished())
        {
            throw new IllegalStateException(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "SCANNER_RUNNING" /* NOI18N */));
        }

        _levels = level;
    }


    /**
     * Returns the directory depth (the number of levels) to search.
     *
     * @return directory depth to search.
     */
    public int getMaxLevels()
    {
        return _levels;
    }


    /**
     * Sets the targets to search (both single files and directories are valid)
     *
     * @param targets collection with the targets to search.
     */
    public void setTargets(Collection targets)
    {
        if (targets instanceof HashSet)
        {
            setTargets((HashSet) targets);
        }
        else
        {
            setTargets(new HashSet(targets));
        }
    }


    /**
     * Sets the targets to search.
     *
     * @param targets set with the targets to search.
     *
     * @throws NullPointerException if <code>targets == null</code>
     * @throws IllegalStateException if the scanner hasn't finished processing yet
     *         (<code>isFinished() == true</code>)
     */
    public void setTargets(Set targets)
    {
        if (targets == null)
        {
            throw new NullPointerException();
        }

        if (isFinished())
        {
            throw new IllegalStateException(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "SCANNER_RUNNING" /* NOI18N */));
        }

        Set copy = new HashSet(targets);

        for (Iterator it = copy.iterator(); it.hasNext();)
        {
            Object file = it.next();

            if (file instanceof String)
            {
                targets.remove(file);
                file = new File((String) file);
                targets.add(file);
            }

            if (!((File) file).isDirectory())
            {
                _queue.push((File) file);
                targets.remove(file);
            }
        }

        _dirs = new File[targets.size()];
        targets.toArray(_dirs);
    }


    /**
     * Adds the given file filter.
     *
     * @param filter file filter to add.
     *
     * @throws IllegalStateException if the scanner hasn't finished processing yet
     *         (<code>isFinished() == true</code>)
     */
    public void addFilter(FilenameFilter filter)
    {
        if (isFinished())
        {
            throw new IllegalStateException(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "SCANNER_RUNNING" /* NOI18N */));
        }

        _filters.addFilter(filter);
    }


    /**
     * Returns an iterator over the found files.
     *
     * @return iterator over the found files.
     */
    public Iterator iterator()
    {
        synchronized (_lock)
        {
            return _queue.iterator();
        }
    }


    /**
     * Returns and removes the file at the top of the internal file queue.
     *
     * @return the file at the top of the queue.
     *
     * @see #isEmpty
     * @see #take
     */
    public File pop()
    {
        synchronized (_lock)
        {
            return _queue.pop();
        }
    }


    /**
     * Removes the given file filter.
     *
     * @param filter file filter to remove.
     *
     * @throws IllegalStateException if the scanner hasn't finished processing yet
     *         (<code>isFinished() == true</code>)
     */
    public void removeFilter(FilenameFilter filter)
    {
        if (isFinished())
        {
            throw new IllegalStateException(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "SCANNER_RUNNING" /* NOI18N */));
        }

        _filters.removeFilter(filter);
    }


    /**
     * Resets the scanner so it can be reused.
     */
    public void reset()
    {
    }


    /**
     * Starts the scanning process. All files matching the given filters will be
     * seamlessly added to the internal queue where they can be accessed via {@link
     * #pop} or {@link #take} calls.
     */
    public void run()
    {
        try
        {
            _finished = false;

            for (int i = 0; i < _dirs.length; i++)
            {
                if (_dirs[i].isDirectory())
                {
                    scanDirectory(_dirs[i]);
                }
            }
        }
        finally
        {
            synchronized (_lock)
            {
                _finished = true;
            }

            synchronized (this)
            {
                notifyAll();
            }
        }
    }


    /**
     * Returns and removes the file at the top of the internal file queue. If the queue
     * is empty, waits until one element is available, so this is in fact a blocking
     * {@link #pop}.
     *
     * @return the file at the top of the queue.
     *
     * @throws InterruptedException if the queue contains no elements.
     *
     * @see #pop
     */
    public File take()
      throws InterruptedException
    {
        synchronized (_lock)
        {
            while (_queue.isEmpty())
            {
                _lock.wait();
            }
        }

        return pop();
    }


    /**
     * Blocks until the scanner has finished processing. Returns immediately if the
     * scanner hasn't started processing already.
     */
    public void waitUntilFinished()
    {
        if (!isFinished())
        {
            try
            {
                synchronized (this)
                {
                    while (!isFinished())
                    {
                        super.wait();
                    }
                }
            }
            catch (Exception ex)
            {
                ;
            }
        }
    }


    private static Set addToList(File directory)
    {
        Set dir = new HashSet(2);
        dir.add(directory);

        return dir;
    }


    /**
     * Calls {@link #scanDirectory(File, String, int)}.
     *
     * @param dir directory to process.
     */
    private void scanDirectory(File dir)
    {
        scanDirectory(dir, dir.getPath(), 0);
    }


    /**
     * Searches recursively all directories.
     *
     * @param dir directory to search.
     * @param rootDir root directory of the search.
     * @param depth level that is currently be processed (0 based).
     */
    private void scanDirectory(
        File   dir,
        String rootDir,
        int    depth)
    {
        File[] dirEntries = null;

        if (((dirEntries = dir.listFiles(_filters)) != null) && (depth <= _levels))
        {
            for (int i = 0; i < dirEntries.length; i++)
            {
                // recursion one level up
                if (dirEntries[i].isDirectory())
                {
                    scanDirectory(dirEntries[i], rootDir, depth + 1);
                }
                else
                {
                    _queue.push(dirEntries[i]);
                }
            }
        }
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Stores all found directories/files.
     */
    private class Queue
    {
        LinkedList list;

        /**
         * Creates a new Queue object.
         */
        public Queue()
        {
            this.list = new LinkedList();
        }


        private Queue(LinkedList list)
        {
            this.list = new LinkedList(list);
        }

        public boolean isEmpty()
        {
            return this.list.isEmpty();
        }


        public Iterator iterator()
        {
            Queue stack = new Queue(this.list);

            return stack.list.iterator();
        }


        public File pop()
        {
            return (File) this.list.removeLast();
        }


        public void push(File file)
        {
            this.list.add(file);

            synchronized (_lock)
            {
                _lock.notify();
            }
        }


        public int size()
        {
            return this.list.size();
        }


        public File[] toArray()
        {
            Queue stack = new Queue(this.list);
            File[] files = new File[stack.list.size()];
            stack.list.toArray(files);

            return files;
        }
    }
}
