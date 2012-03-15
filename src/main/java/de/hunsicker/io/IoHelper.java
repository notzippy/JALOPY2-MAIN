/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import de.hunsicker.util.ResourceBundleFactory;


/**
 * Some I/O helper routines.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 */
public final class IoHelper
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String BUNDLE_NAME = "de.hunsicker.io.Bundle" /* NOI18N */;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new IoHelper object.
     */
    private IoHelper()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Deletes the given file or directory.
     *
     * @param file a file or directory.
     * @param recursive if <code>true</code> directories will be deleted recursively.
     *
     * @return <code>true</code> if the file or directory could be deleted successfully.
     */
    public static boolean delete(
        File    file,
        boolean recursive)
    {
        if (file.exists())
        {
            if (file.isFile())
            {
                return file.delete();
            }
            if (recursive)
            {
                File[] files = file.listFiles();
                boolean success = false;

                for (int i = 0; i < files.length; i++)
                {
                    if (files[i].isDirectory() && (files[i].list().length != 0))
                    {
                        success = delete(files[i], true);
                    }
                    else
                    {
                        success = files[i].delete();
                    }

                    if (!success)
                    {
                        return false;
                    }
                }

                return file.delete();
            }
            return file.delete();
        }
        return false;
    }


    /**
     * Deserializes an object previously written using an ObjectOutputStream.
     *
     * @param data binary array.
     *
     * @return deserialized object.
     *
     * @throws IOException if an I/O error occured.
     *
     * @see #serialize
     */
    public static Object deserialize(byte[] data)
      throws IOException
    {
        if (data.length == 0)
        {
            return null;
        }

        return deserialize(new BufferedInputStream(new ByteArrayInputStream(data)));
    }


    /**
     * Deserializes the object stored in the given stream.
     *
     * @param in an input stream.
     *
     * @return the deserialized object.
     *
     * @throws IOException if an I/O exception occured.
     */
    public static Object deserialize(InputStream in)
      throws IOException
    {
        ObjectInputStream oin = new ObjectInputStream(in);

        try
        {
            return oin.readObject();
        }
        catch (ClassNotFoundException ex)
        {
            /**
             * @todo once we only support JDK 1.4, add chained exception
             */
            throw new IOException(ex.getMessage());
        }
        finally
        {
            if (oin != null)
            {
                oin.close();
            }
        }
    }


    /**
     * Deserializes the object stored in the given file.
     *
     * @param file a file.
     *
     * @return the deserialized object.
     *
     * @throws IOException if an I/O exception occured.
     */
    public static final Object deserialize(File file)
      throws IOException
    {
        return deserialize(new BufferedInputStream(new FileInputStream(file)));
    }


    /**
     * Verifies the existence of the given directory and if that directory does not yet
     * exist, tries to create it.
     *
     * @param directory directory to check for existence.
     *
     * @return <code>true</code> if the directory already exists or was successfully
     *         created.
     *
     * @throws IllegalArgumentException if <em>directory</em> does exist but does not
     *         denote a valid directory.
     */
    public static boolean ensureDirectoryExists(File directory)
    {
        if (!directory.exists())
        {
            return directory.mkdirs();
        }
        else if (!directory.isDirectory())
        {
            Object[] args = { directory };
            throw new IllegalArgumentException(
                MessageFormat.format(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "NOT_DIRECTORY" /* NOI18N */), args));
        }

        return true;
    }


    /**
     * Returns the contents of the given text file.
     *
     * @param file text file to be read.
     *
     * @return contents of the give file.
     *
     * @throws IOException if an I/O error occured.
     */
    public static String readTextFile(File file)
      throws IOException
    {
        BufferedReader in = null;

        try
        {
            if (!file.exists())
            {
                Object[] args = { file };
                throw new IOException(
                    MessageFormat.format(
                        ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                            "FILE_DOES_NOT_EXIST" /* NOI18N */), args));
            }

            in = new BufferedReader(
                    new InputStreamReader(
                        new FileInputStream(file), "UTF-8" /* NOI18N */));

            int fileSize = (int) file.length();

            char[] buf = new char[fileSize];
            in.read(buf, 0, fileSize);

            return new String(buf);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ignored)
                {
                    ;
                }
            }
        }
    }


    /**
     * Serializes the given object to the given output stream.
     *
     * @param o a serializable object.
     * @param out the stream to write the object to.
     *
     * @throws IOException if an I/O exception occured.
     */
    public static void serialize(
        Object       o,
        OutputStream out)
      throws IOException
    {
        ObjectOutputStream oout = new ObjectOutputStream(out);

        try
        {
            oout.writeObject(o);
        }
        finally
        {
            if (oout != null)
            {
                oout.close();
            }
        }
    }


    /**
     * Serializes the given object to the given file.
     *
     * @param o a serializable object.
     * @param file the file to write the object to.
     *
     * @throws IOException if an I/O exception occured.
     */
    public static void serialize(
        Object o,
        File   file)
      throws IOException
    {
        serialize(o, new BufferedOutputStream(new FileOutputStream(file)));
    }
}
