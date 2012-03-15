/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.storage;

import java.io.BufferedOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import de.hunsicker.io.IoHelper;


/**
 * History serves as a tracker for file modifications.
 * 
 * <p>
 * The {@link #flush} method may be used to synchronously force updates to the backing
 * store. Normal termination of the Java Virtual Machine will <em>not</em> result in the
 * loss of pending updates - an explicit flushing is <em>not</em> required upon
 * termination to ensure that pending updates are made persistent.
 * </p>
 * 
 * <p>
 * This class is thread-safe.
 * </p>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public final class History
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final History INSTANCE = new History();

    /** Holds the history entries. */
    private static Map _history; // Map of <String>:<History.Entry>

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new History object.
     */
    private History()
    {
        initialize();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the sole instance of this class.
     *
     * @return class instance.
     */
    public static History getInstance()
    {
        return INSTANCE;
    }


    /**
     * Adds the given file to the history. It will only be added, if it exists and indeed
     * denotes a file (not a directory).
     *
     * @param file file to add.
     * @param packageName the package name of the file to add.
     * @param modification the time the file given was last processed.
     *
     * @throws IOException if an I/O error occured, which is possible because a canonical
     *         pathname will be constructed.
     */
    public synchronized void add(
        File   file,
        String packageName,
        long   modification)
      throws IOException
    {
        if (file.exists() && file.isFile())
        {
            _history.put(file.getCanonicalPath(), new Entry(packageName, modification));
        }
    }


    /**
     * Clears the history.
     */
    public synchronized void clear()
    {
        _history.clear();
    }


    /**
     * Stores the history to the backing store.
     *
     * @throws IOException if an I/O error occured.
     */
    public synchronized void flush()
      throws IOException
    {
        File file = Convention.getHistoryFile();
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        ObjectOutputStream p = new ObjectOutputStream(out);

        try
        {
            p.writeObject(_history);
        }
        finally
        {
            if (p != null)
            {
                p.close();
            }
        }
    }


    /**
     * Returns the history entry for the given file.
     *
     * @param file the file to get the corresponding history entry for.
     *
     * @return the history entry for the given file or <code>null</code> if no entry for
     *         the given file exists.
     *
     * @throws IOException if an I/O error occured, which is possible because a canonical
     *         pathname will be constructed.
     */
    public synchronized Entry get(File file)
      throws IOException
    {
        return (Entry) _history.get(file.getCanonicalPath());
    }


    /**
     * Removes the given file from the history.
     *
     * @param file file to remove.
     *
     * @throws IOException if an I/O error occured, which is possible because a canonical
     *         pathname will be constructed.
     */
    public synchronized void remove(File file)
      throws IOException
    {
        _history.remove(file.getCanonicalPath());
    }


    /**
     * Initialization. Loads the history from the backing store.
     */
    private synchronized void initialize()
    {
        try
        {
            File file = Convention.getHistoryFile();

            if (file.exists())
            {
                _history = (Map) IoHelper.deserialize(file);
            }
            else
            {
                _history = new HashMap();
            }
        }
        catch (Throwable ex)
        {
            _history = new HashMap();
        }

        Runtime.getRuntime().addShutdownHook(new TerminationHandler());
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * A writer that calculates a checkum during the writing process.
     *
     * @author <a href="http://jalopy.sf.net/contact.html">Michael Callum</a>
     */
    public static final class ChecksumCharArrayWriter
        extends CharArrayWriter
    {
        private Checksum _checksum;

        public ChecksumCharArrayWriter(Method method)
        {
            if (method == History.Method.ADLER32)
            {
                _checksum = new Adler32();
            }
            else if (method == History.Method.CRC32)
            {
                _checksum = new CRC32();
            }
            else
            {
                throw new IllegalArgumentException(
                    "invalid check sum history method -- " + method.toString());
            }
        }

        public Checksum getChecksum()
        {
            return _checksum;
        }


        public void write(
            char[] c,
            int    off,
            int    len)
        {
            super.write(c, off, len);

            String string = new String(c, off, len);
            byte[] bytes = string.getBytes();
            _checksum.update(bytes, 0, bytes.length);
        }
    }


    /**
     * Represents a history entry.
     */
    public static final class Entry
        implements Serializable
    {
        /** Use serialVersionUID for interoperability. */
        static final long serialVersionUID = 7661537884776942605L;

        /** The package name of the entry. */
        String packageName;

        /** The time this entry was last processed. */
        long lastmod;

        /**
         * Creates a new entry object.
         *
         * @param packageName the package name of the entry.
         * @param modification the value calculated for the last processing of this
         *        entry.
         */
        public Entry(
            String packageName,
            long   modification)
        {
            this.packageName = packageName;
            this.lastmod = modification;
        }

        /**
         * Returns the last modification value (could be timestamp or crc).
         *
         * @return last modification value.
         */
        public long getModification()
        {
            return this.lastmod;
        }


        /**
         * Returns the package name of the entry.
         *
         * @return the package name.
         */
        public String getPackageName()
        {
            return this.packageName;
        }


        /**
         * Returns a string representation of this entry.
         *
         * @return a string representation of this entry.
         */
        public String toString()
        {
            StringBuffer buf = new StringBuffer(30);
            buf.append('%');
            buf.append(this.lastmod);
            buf.append(':');
            buf.append(this.packageName);
            buf.append('%');

            return buf.toString();
        }
    }


    /**
     * Represents the method used to identify dirty files and changed files.
     *
     * @author <a href="http://jalopy.sf.net/contact.html">Michael Callum</a>
     *
     * @since 1.0b9
     */
    public static final class Method
    {
        /** Use simple, last modified timestamp. */
        public static final Method TIMESTAMP =
            new Method("timestamp" /* NOI18N */, "Timestamp");

        /** Use CRC32 checksum. */
        public static final Method CRC32 =
            new Method("crc32" /* NOI18N */, "CRC32 Checksum");

        /** Use Adler32 checksum. */
        public static final Method ADLER32 =
            new Method("adler32" /* NOI18N */, "Adler32 Checksum");
        String displayName;
        String name;

        /**
         * Creates a new Method object.
         *
         * @param name the name of the method.
         * @param displayName a possibly localized, descriptive name suitable for display
         *        presentation purposes.
         */
        private Method(
            String name,
            String displayName)
        {
            this.name = name.intern();
            this.displayName = displayName;
        }

        /**
         * Returns the history method for the given name.
         *
         * @param methodName a valid method name. Either &quot;timestamp&quot;,
         *        &quot;crc32&quot; or &quot;adler32&quot; (case-sensitive).
         *
         * @return The policy for the given name.
         *
         * @throws IllegalArgumentException if an invalid name specified.
         */
        public static Method valueOf(String methodName)
        {
            methodName = methodName.intern();

            if (methodName == TIMESTAMP.name)
            {
                return TIMESTAMP;
            }
            else if (methodName == CRC32.name)
            {
                return CRC32;
            }
            else if (methodName == ADLER32.name)
            {
                return ADLER32;
            }
            else
            {
                throw new IllegalArgumentException(
                    "no valid history method name -- " + methodName);
            }
        }


        /**
         * Returns the name of this method.
         *
         * @return the name of this method.
         */
        public String getName()
        {
            return this.name;
        }


        /**
         * Returns a string representation of this method.
         *
         * @return a string representation of this method.
         */
        public String toString()
        {
            return this.displayName;
        }
    }


    /**
     * Represents a history policy.
     *
     * @since 1.0b8
     */
    public static final class Policy
    {
        /** Don't use the history. */
        public static final Policy DISABLED =
            new Policy("disabled", "History.Policy [disabled]");

        /** Insert a single line comment header at the top of every formatted file. */
        public static final Policy COMMENT =
            new Policy("comment", "History.Policy [comment]");

        /**
         * Track file modifications in a binary file stored in the Jalopy settings
         * directory.
         */
        public static final Policy FILE = new Policy("file", "History.Policy [file]");

        /** The display name of the history policy. */
        final String displayName;

        /** The name of the history policy. */
        final String name;

        private Policy(
            String name,
            String displayName)
        {
            this.name = name.intern();
            this.displayName = displayName;
        }

        /**
         * Returns the policy for the given name.
         *
         * @param name a valid policy name. Either &quot;disabled&quot;, &quot;file&quot;
         *        or &quot;comment&quot; (case-sensitive).
         *
         * @return The policy for the given name.
         *
         * @throws IllegalArgumentException if an invalid name specified.
         */
        public static Policy valueOf(String name)
        {
            String n = name.intern();

            if (FILE.name == n)
            {
                return FILE;
            }
            else if (COMMENT.name == n)
            {
                return COMMENT;
            }
            else if (DISABLED.name == n)
            {
                return DISABLED;
            }

            throw new IllegalArgumentException("no valid history policy name -- " + name);
        }


        /**
         * Returns the name of this policy.
         *
         * @return the name of this policy.
         */
        public String getName()
        {
            return this.name;
        }


        /**
         * Returns a string representation of this object.
         *
         * @return A string representation of this object.
         */
        public String toString()
        {
            return this.displayName;
        }
    }


    /**
     * Executed before the JVM terminates. Flushes the history to disk.
     */
    private final class TerminationHandler
        extends Thread
    {
        public synchronized void run()
        {
            try
            {
                if (_history != null)
                {
                    flush();
                }
            }
            catch (IOException ex)
            {
                /**
                 * @todo log error message
                 */
            }
        }
    }
}
