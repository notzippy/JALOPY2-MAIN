/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.storage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * A key for storing a value in a code convention.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 *
 * @see de.hunsicker.jalopy.storage.ConventionKeys
 * @since 1.0b8
 * @deprecated Replaced by {@link de.hunsicker.jalopy.storage.Convention.Key}. Only
 *             provided for backwards compatibility with earlier versions. Will be
 *             removed with a future release.
 */
public final class Key
    implements Serializable
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Use serialVersionUID for interoperability. */
    static final long serialVersionUID = -7320495354745545260L;

    //~ Instance variables ---------------------------------------------------------------

    /** Our name. */
    private transient String _name;

    /** Pre-computed hash code value. */
    private transient int _hashCode;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Key object.
     *
     * @param name the name of the key.
     */
    Key(String name)
    {
        _name = name.intern();
        _hashCode = _name.hashCode();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Indicates whether some other object is &quot;equal to&quot; this one.
     *
     * @param o the reference object with which to compare.
     *
     * @return <code>true</code> if this object is the same as the obj argument.
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        return _name == ((Key) o)._name;
    }


    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code value for this object.
     */
    public int hashCode()
    {
        return _hashCode;
    }


    /**
     * Returns a string representation of this object.
     *
     * @return A string representation of this object.
     */
    public String toString()
    {
        return _name;
    }


    /**
     * Deserializes a key from the given stream.
     *
     * @param in stream to read the object from.
     *
     * @throws IOException if an I/O error occured.
     * @throws ClassNotFoundException if a class that should be read could not be found
     *         (Should never happen actually).
     */
    private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        // that's why we have to provide custom serialization: we want to be
        // able to compare two keys by identity
        _name = ((String) in.readObject()).intern();
        _hashCode = in.readInt();
    }


    /**
     * Serializes this instance.
     *
     * @param out stream to write the object to.
     *
     * @throws IOException if an I/O error occured.
     *
     * @serialData Emits the name of the key, followed by its pre-computed hash code
     *             value.
     */
    private void writeObject(ObjectOutputStream out)
      throws IOException
    {
        out.defaultWriteObject();
        out.writeObject(_name);
        out.writeInt(_hashCode);
    }
}
