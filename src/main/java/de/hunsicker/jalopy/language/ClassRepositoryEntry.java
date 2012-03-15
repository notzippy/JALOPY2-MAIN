/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Represents a repository entry for a given Java library. An entry consists of meta
 * information and the actual data stored in a set.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 *
 * @see ClassRepository
 */
public class ClassRepositoryEntry
{
    //~ Instance variables ---------------------------------------------------------------

    /** The entry info. */
    Info info;

    /** The actual data. */
    Set data = Collections.EMPTY_SET; // Set of <String>

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ClassRepositoryEntry object.
     *
     * @param info the entry information.
     * @param data set with the actual data.
     */
    public ClassRepositoryEntry(
        Info info,
        Set  data)
    {
        this.data = data;
        this.info = info;
    }


    /**
     * Creates a new ClassRepositoryEntry object.
     *
     * @param location the location of the original source.
     * @param lastModified
     * @param data contents.
     */
    public ClassRepositoryEntry(
        File location,
        long lastModified,
        Set  data)
    {
        this(new Info(location), data);
    }


    /**
     * Sole constructor.
     */
    ClassRepositoryEntry()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the entry information for the given repository entry file.
     *
     * @param file repository entry file (those ending with <code>.jdb</code>).
     *
     * @return entry information.
     *
     * @throws IOException if an I/O error occured.
     */
    public static Info getInfo(File file)
      throws IOException
    {
        ObjectInputStream in = null;

        try
        {
            in = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(file)));

            return (Info) in.readObject();
        }
        catch (ClassNotFoundException neverOccurs)
        {
            return null;
        }
        finally
        {
            in.close();
        }
    }


    /**
     * Sets the data of the entry.
     *
     * @param data data (of type {@link java.lang.String &lt;String&gt;}).
     */
    public void setData(Set data)
    {
        this.data = data;
    }


    /**
     * Returns the current data.
     *
     * @return data (of type {@link java.lang.String &lt;String&gt;}).
     */
    public Set getData()
    {
        return this.data;
    }


    /**
     * Returns the entry information.
     *
     * @return entry information.
     */
    public Info getInfo()
    {
        return this.info;
    }


    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof ClassRepositoryEntry))
        {
            return false;
        }

        return this.data.equals(((ClassRepositoryEntry) o).data);
    }


    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return this.data.hashCode();
    }


    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "ClassRepositoryEntry: " + this.info.getLocation();
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Provides information about a repository entry.
     */
    public static final class Info
        implements Serializable, Comparable
    {
        /** Use serialVersionUID for interoperability. */
        static final long serialVersionUID = 6093443653626639672L;

        /** The filename of the entry. */
        transient String filename;

        /** The location of the original source. */
        transient String location;

        /** Indicates whether the entry was loaded into memory. */
        transient boolean loaded;

        /**
         * Creates a new Info object.
         *
         * @param location location of the source for the entry (either file or directory
         *        are valid).
         *
         * @throws IllegalArgumentException if <em>location</em> does not denote an
         *         existing archive or directory.
         */
        public Info(File location)
        {
            if (!location.exists())
            {
                throw new IllegalArgumentException(
                    "location does not exist -- " + location);
            }

            this.location = location.getAbsolutePath();

            if (
                !location.isDirectory() && !this.location.endsWith(".jar")
                && !this.location.endsWith(".zip"))
            {
                throw new IllegalArgumentException(
                    location + " does denote archive or directory");
            }

            ClassRepositoryEntry.Info info = ClassRepository.getInstance().get(location);

            if (info == null)
            {
                if (location.isDirectory())
                {
                    this.filename =
                        String.valueOf(
                            System.currentTimeMillis() + ClassRepository.EXT_REPOSITORY);
                }
                else
                {
                    this.filename =
                        genFilename(
                            location.getName().substring(
                                0, location.getName().lastIndexOf('.'))
                            + ClassRepository.EXT_REPOSITORY);
                }
            }
            else
            {
                this.filename = info.getFilename();
            }
        }

        /**
         * Returns the filename under which this entry is stored.
         *
         * @return the filename of the entry.
         */
        public String getFilename()
        {
            return this.filename;
        }


        public void setLoaded(boolean loaded)
        {
            this.loaded = loaded;
        }


        public boolean isLoaded()
        {
            return this.loaded;
        }


        /**
         * Returns the original location of the entry's data.
         *
         * @return the original location.
         */
        public File getLocation()
        {
            return new File(this.location);
        }


        /**
         * Determines whether this entry can be refreshed.
         *
         * @return <code>true</code> if the entry can be refreshed.
         */
        public boolean isRefreshable()
        {
            return new File(this.location).exists();
        }


        /**
         * Compares this object with the specified object for order.
         *
         * @param o the object to be compared.
         *
         * @return a negative integer, zero, or a positive integer as this object is less
         *         than, equal to, or greater than the specified object.
         *
         * @throws ClassCastException if the specified object's type prevents it from
         *         being compared to this object.
         */
        public int compareTo(Object o)
        {
            /**
             * @todo maybe we should do locale dependent sorting here
             */
            if (o instanceof Info)
            {
                return this.location.compareToIgnoreCase(((Info) o).location);
            }
            else if (o instanceof String)
            {
                return this.location.compareToIgnoreCase((String) o);
            }

            throw new ClassCastException(o.getClass().getName());
        }


        public boolean equals(Object o)
        {
            if (o instanceof Info)
            {
                return this.location.equals(((Info) o).location);
            }
            else if (o instanceof String)
            {
                return this.location.equals(o);
            }

            return false;
        }


        /**
         * {@inheritDoc}
         */
        public int hashCode()
        {
            return this.location.hashCode();
        }


        /**
         * {@inheritDoc}
         */
        public String toString()
        {
            return this.location + " [" + this.filename + "]";
        }


        private String genFilename(String newFilename)
        {
            File file =
                new File(
                    Convention.getInstance().get(
                        ConventionKeys.CLASS_REPOSITORY_DIRECTORY,
                        Convention.getRepositoryDirectory().getAbsolutePath()) + newFilename);

            if (file.exists())
            {
                int paren = newFilename.indexOf('(');

                if (paren > -1)
                {
                    String number =
                        newFilename.substring(paren + 1, newFilename.lastIndexOf(')'));

                    try
                    {
                        int n = Integer.parseInt(number);
                        newFilename =
                            newFilename.substring(0, newFilename.lastIndexOf('(')) + "("
                            + (++n) + ")" + ClassRepository.EXT_REPOSITORY;
                    }
                    catch (Exception ex)
                    {
                        throw new RuntimeException(
                            "error creating filename for " + newFilename);
                    }
                }
                else
                {
                    newFilename =
                        newFilename.substring(0, newFilename.lastIndexOf('.')) + "(1)"
                        + ClassRepository.EXT_REPOSITORY;
                }

                newFilename = genFilename(newFilename);
            }

            return newFilename;
        }


        private void readObject(ObjectInputStream in)
          throws IOException, ClassNotFoundException
        {
            in.defaultReadObject();
            this.location = (String) in.readObject();
            this.filename = (String) in.readObject();
        }


        /**
         * Writes the object to the stream
         *
         * @param out The output stream
         *
         * @throws IOException If an IO Error occur
         *
         * @todo document serial data
         */
        private void writeObject(ObjectOutputStream out)
          throws IOException
        {
            out.defaultWriteObject();
            out.writeObject(this.location);
            out.writeObject(this.filename);
        }
    }
}
