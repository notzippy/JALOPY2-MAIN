/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;


//J-
import java.lang.ClassCastException;
//J+

/**
 * Represents a method type to distinguish between ordinary method names and those which
 * adhere to the Java Bean naming conventions.
 * 
 * <p>
 * The default order for comparing is: ordinary Java method names, mutator,
 * accessor/tester. This can be changed via {@link #setOrder}.
 * </p>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class DeclarationType
    implements Comparable, Type
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The bit value for classes. */
    public static final int CLASS_INT = 2;

    /** The bit value for constructors. */
    public static final int CTOR_INT = 4;

    /** The bit value for instance initializers. */
    public static final int INIT_INT = 16;

    /** The bit value for interfaces. */
    public static final int INTERFACE_INT = 8;

    /** The bit value for methods. */
    public static final int METHOD_INT = 32;

    /** The bit value for annotations. */
    public static final int ANNOTATION_INT = 64;

    /** The bit value for enums. */
    public static final int ENUM_INT = 128;

    /** The bit value for instance variables. */
    public static final int VARIABLE_INT = 1;

    /** The bit value for static variables/initializers. */
    public static final int STATIC_VAR_INIT_INT = 64;
    private static final String DELIMETER = "|" /* NOI18N */;

    /** A string represenation of the current sort order. */
    private static String _sortOrder;
    private static final String BUNDLE_NAME = "de.hunsicker.jalopy.language.Bundle";

    /** Holds the actual order of the method types. */
    private static final List _order = new ArrayList(7); // List of <String>

    /** Represents a class declaration. */
    public static final DeclarationType CLASS =
        new DeclarationType(
            "class" /* NOI18N */,
            ResourceBundle.getBundle(BUNDLE_NAME).getString("TYPE_CLASS" /* NOI18N */),
            CLASS_INT);

    /** Represents an ANNOTATION declaration. */
    public static final DeclarationType ANNOTATION =
        new DeclarationType(
            "annotation" /* NOI18N */,
            ResourceBundle.getBundle(BUNDLE_NAME).getString("TYPE_ANNOTATION" /* NOI18N */),
            ANNOTATION_INT);
    
    /** Represents an ENUM declaration. */
    public static final DeclarationType ENUM =
        new DeclarationType(
            "enum" /* NOI18N */,
            ResourceBundle.getBundle(BUNDLE_NAME).getString("TYPE_ENUM" /* NOI18N */),
            ENUM_INT);
    
    /** Represents an interface declaration. */
    public static final DeclarationType INTERFACE =
        new DeclarationType(
            "interface" /* NOI18N */,
            ResourceBundle.getBundle(BUNDLE_NAME).getString(
                "TYPE_INTERFACE" /* NOI18N */), INTERFACE_INT);

    /** Represents an instance variable declaration. */
    public static final DeclarationType VARIABLE =
        new DeclarationType(
            "field" /* NOI18N */,
            ResourceBundle.getBundle(BUNDLE_NAME).getString("TYPE_FIELD" /* NOI18N */),
            VARIABLE_INT);

    /** Represents an instance initializer. */
    public static final DeclarationType INIT =
        new DeclarationType(
            "initializer" /* NOI18N */,
            ResourceBundle.getBundle(BUNDLE_NAME).getString(
                "TYPE_INITIALIZER" /* NOI18N */), INIT_INT);

    /** Represents a constructor declaration. */
    public static final DeclarationType CTOR =
        new DeclarationType(
            "constructor" /* NOI18N */,
            ResourceBundle.getBundle(BUNDLE_NAME).getString(
                "TYPE_CONSTRUCTOR" /* NOI18N */), CTOR_INT);

    /** Represents a method declaration. */
    public static final DeclarationType METHOD =
        new DeclarationType(
            "method" /* NOI18N */,
            ResourceBundle.getBundle(BUNDLE_NAME).getString("TYPE_METHOD" /* NOI18N */),
            METHOD_INT);

    /** Represents a static variable declaration or initializer. */
    public static final DeclarationType STATIC_VARIABLE_INIT =
        new DeclarationType(
            "static" /* NOI18N */,
            ResourceBundle.getBundle(BUNDLE_NAME).getString("TYPE_STATIC" /* NOI18N */),
            STATIC_VAR_INIT_INT);

    static
    {
        _order.add(STATIC_VARIABLE_INIT);
        _order.add(VARIABLE);
        _order.add(INIT);
        _order.add(CTOR);
        _order.add(METHOD);
        _order.add(INTERFACE);
        _order.add(CLASS);
        _order.add(ANNOTATION);
        _order.add(ENUM);

        StringBuffer buf = new StringBuffer(100);

        for (int i = 0, size = _order.size(); i < size; i++)
        {
            buf.append(((DeclarationType) _order.get(i))._name);
            buf.append(DELIMETER);
        }

        buf.setLength(buf.length() - 1);
        _sortOrder = buf.toString();
    }

    //~ Instance variables ---------------------------------------------------------------

    /** A user-friendly string representation of the type. */
    private final String _displayName;
    private final String _name;

    /** The bit value of the method type. */
    private final int _key;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new DeclarationType object.
     *
     * @param name a string representing the declaration type.
     * @param displayName The name representing the type
     * @param key a bit value representing the declaration type.
     */
    private DeclarationType(
        String name,
        String displayName,
        int    key)
    {
        _name = name;
        _displayName = displayName;
        _key = key;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the name of this type.
     *
     * @return type name.
     */
    public String getName()
    {
        return _name;
    }


    /**
     * Sets the order to use as the natural order.
     *
     * @param str a string representing the new order. The string must consist of exactly
     *        four different, comma delimited strings which represents a method type.
     *
     * @throws IllegalArgumentException if the given string is invalid.
     */
    public static synchronized void setOrder(String str)
    {
        if ((str == null) || (str.length() == 0))
        {
            throw new IllegalArgumentException("invalid order string -- " + str);
        }

        StringTokenizer tokens = new StringTokenizer(str, DELIMETER);
        List temp = new ArrayList(_order.size());
        StringBuffer buf = new StringBuffer(20);

        while (tokens.hasMoreElements())
        {
            String token = tokens.nextToken();
            DeclarationType type = valueOf(token);

            if (temp.contains(type))
            {
                throw new IllegalArgumentException("invalid order string -- " + temp);
            }

            temp.add(type);
            buf.append(type.toString());
            buf.append(DELIMETER);
        }

        if (_order.size() != temp.size())
        {
            throw new IllegalArgumentException("invalid order string -- " + temp);
        }

        if (
            !temp.contains(CLASS) || !temp.contains(INTERFACE) || !temp.contains(CTOR)
            || !temp.contains(VARIABLE) || !temp.contains(METHOD) || !temp.contains(INIT)
            || !temp.contains(STATIC_VARIABLE_INIT))
        {
            throw new IllegalArgumentException("invalid order string -- " + temp);
        }

        _order.clear();
        _order.addAll(temp);
        buf.deleteCharAt(buf.length() - 1);
        _sortOrder = buf.toString();
    }


    /**
     * Returns a string representation of the current sort order. To encode the different
     * declaration types, use
     * <pre class="snippet">
     * Variable,Initializer,Constructor,Method,Interface,Class
     * </pre>
     * or any combination thereof.
     *
     * @return the string representation of the order.
     */
    public static synchronized String getOrder()
    {
        return _sortOrder;
    }
    public static synchronized int getOrderSize() {
        return _order.size();
    }


    /**
     * Returns the display name
     *
     * @return The display name
     */
    public String getDisplayName()
    {
        return _displayName;
    }
    /**
     * Returns the key 
     * @return The key
     */
    public int  getKey() {
        return _key;
    }


    /**
     * Compares this object with the specified object for order. Returns a negative
     * integer, zero, or a positive integer as this object is less than, equal to, or
     * greater than the specified object.
     *
     * @param other the object to be compared.
     *
     * @return An integer comparision of the 2 values
     *
     * @throws ClassCastException if the specified object's type prevents it from being
     *         compared to this object.
     */
    public int compareTo(Object other)
    {
        if (other == this)
        {
            return 0;
        }

        if (other instanceof DeclarationType)
        {
            int thisIndex = _order.indexOf(this);
            int otherIndex = _order.indexOf(other);

            if (thisIndex > otherIndex)
            {
                return 1;
            }
            else if (thisIndex < otherIndex)
            {
                return -1;
            }
            else
            {
                return 0;
            }
        }

        throw new ClassCastException(
            (other == null) ? "null"
                            : other.getClass().getName());
    }


    /**
     * Returns a string representation of this method type.
     *
     * @return a string representation of this type.
     */
    public String toString()
    {
        return _displayName;
    }


    /**
     * Returns the declaration type of the given abreviation.
     *
     * @param name a declaration identifier.
     *
     * @return the declaration type of the given name.
     *
     * @throws IllegalArgumentException if no valid declaration identifier was given.
     */
    public static DeclarationType valueOf(String name)
    {
        if ((name == null) || (name.trim().length() == 0))
        {
            throw new IllegalArgumentException("invalid declaration name -- " + name);
        }

        name = name.trim();

        if (METHOD._name.equals(name))
        {
            return METHOD;
        }
        else if (CTOR._name.equals(name))
        {
            return CTOR;
        }
        else if (VARIABLE._name.equals(name))
        {
            return VARIABLE;
        }
        else if (STATIC_VARIABLE_INIT._name.equals(name))
        {
            return STATIC_VARIABLE_INIT;
        }
        else if (INIT._name.equals(name))
        {
            return INIT;
        }
        else if (CLASS._name.equals(name))
        {
            return CLASS;
        }
        else if (INTERFACE._name.equals(name))
        {
            return INTERFACE;
        }
        else if (ANNOTATION._name.equals(name))
        {
            return ANNOTATION;
        }
        else if (ENUM._name.equals(name))
        {
            return ENUM;
        }

        return null;
    }
}
