/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import de.hunsicker.util.ResourceBundleFactory;


/**
 * Helper routines for copying files or directories.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
public class Copy
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String BUNDLE_NAME = "de.hunsicker.io.Bundle" /* NOI18N */;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Copy object.
     */
    private Copy()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Copies the given directory to the new location.
     *
     * @param source the directory to copy.
     * @param destination the destination directory.
     *
     * @return <code>true</code> if the operation ended upon success.
     *
     * @throws IOException if an I/O error occured.
     * @throws NullPointerException if <code>source == null</code>
     * @throws IllegalArgumentException if <em>source</em> does not exist or does not
     *         denote a directory.
     */
    public static boolean directory(
        File source,
        File destination)
      throws IOException
    {
        if (source == null)
        {
            throw new NullPointerException();
        }

        if (!source.exists())
        {
            Object[] args = { source };

            throw new IllegalArgumentException(
                MessageFormat.format(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "NOT_FOUND" /* NOI18N */), args));
        }

        if (!source.isDirectory())
        {
            Object[] args = { source };
            throw new IllegalArgumentException(
                MessageFormat.format(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "NOT_DIRECTORY" /* NOI18N */), args));
        }

        File[] files = source.listFiles();
        boolean success = true;

        for (int i = 0; i < files.length; i++)
        {

            if (files[i].isFile())
            {
                success = file(files[i], new File(destination, files[i].getName()));
            }
            else
            {
                success = directory(files[i], new File(destination, files[i].getName()));
            }

            if (!success)
            {
                return false;
            }
        }

        return success;
    }


    /**
     * Copies the given file to <em>dest</em>. Calls {@link #file(File,File,boolean)}.
     *
     * @param source source file.
     * @param destination destination file.
     *
     * @return Returns <code>true</code> if the file was sucessfully copied.
     *
     * @throws IOException if an I/O error occured.
     */
    public static boolean file(
        String source,
        String destination)
      throws IOException
    {
        return file(new File(source), new File(destination), false);
    }


    /**
     * Copies the given file to <em>dest</em>. Calls {@link #file(File,File,boolean)}.
     *
     * @param source source file.
     * @param destination destination file.
     *
     * @return Returns <code>true</code> if the file was sucessfully copied
     *
     * @throws IOException if an I/O error occured.
     */
    public static boolean file(
        File source,
        File destination)
      throws IOException
    {
        return file(source, destination, false);
    }


    /**
     * Copies the given file to <em>dest</em>.
     *
     * @param source source file.
     * @param destination destination file.
     * @param overwrite if <code>true</code> the destination file will be always
     *        overwritten if it already exists; if <code>false</code> it will only be
     *        overwritten if the source file is newer than the destination file.
     *
     * @return Returns <code>true</code> if the file was sucessfully copied
     *
     * @throws IOException if an I/O error occured.
     * @throws NullPointerException if <code>source == null</code>
     * @throws IllegalArgumentException if <em>source</em> does not exist or does not
     *         denote a file.
     */
    public static boolean file(
        File    source,
        File    destination,
        boolean overwrite)
      throws IOException
    {
        if (source == null)
        {
            throw new NullPointerException();
        }

        if (!source.exists())
        {
            Object[] args = { source };
            throw new IllegalArgumentException(
                MessageFormat.format(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "NOT_FOUND" /* NOI18N */), args));
        }

        if (!source.isFile())
        {
            Object[] args = { source };
            throw new IllegalArgumentException(
                MessageFormat.format(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "NOT_FILE" /* NOI18N */), args));
        }

        if (overwrite || (destination.lastModified() < source.lastModified()))
        {
            // ensure that parent directory of destination file exists
            File parent = destination.getAbsoluteFile().getParentFile();

            if ((parent != null) && !parent.exists())
            {
                parent.mkdirs();
            }

            InputStream in = new BufferedInputStream(new FileInputStream(source));
            OutputStream out =
                new BufferedOutputStream(new FileOutputStream(destination));
            byte[] buffer = new byte[8 * 1024];
            int count = 0;

            try
            {
                do
                {
                    out.write(buffer, 0, count);
                    count = in.read(buffer, 0, buffer.length);
                }
                while (count != -1);
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }

                if (out != null)
                {
                    out.close();
                }
            }

            return true;
        }

        return false;
    }
}
