/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.util.ArrayList;
import java.util.List;
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
public class MethodType
    implements Comparable, Type
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The bit value for getters. */
    static final int GETTER_INT = 2;

    /** The bit value for ordinary methods. */
    static final int OTHER_INT = 8;

    /** The bit value for setters. */
    static final int SETTER_INT = 1;

    /** The bit value for testers. */
    static final int TESTER_INT = 4;
    private static final String PREF_GET = "get";
    private static final String PREF_IS = "is";
    private static final String PREF_SET = "set";

    /** A string represenation of the current sort order. */
    private static String _sortOrder;

    /** Holds the actual order of the method types. */
    private static final List order = new ArrayList(5); // List of <String>

    /** Represents a Java Bean accessor. */
    public static final MethodType GETTER = new MethodType("getFoo", GETTER_INT);

    /** Represents a Java Bean mutator. */
    public static final MethodType SETTER = new MethodType("setFoo", SETTER_INT);

    /** Represents a Java Bean accessor/tester. */
    public static final MethodType TESTER = new MethodType("isFoo", TESTER_INT);

    /** Represents an ordinary Java method. */
    public static final MethodType OTHER = new MethodType("foo", OTHER_INT);

    static
    {
        order.add(SETTER);
        order.add(GETTER);
        order.add(TESTER);
        order.add(OTHER);

        StringBuffer buf = new StringBuffer(10);

        for (int i = 0, size = order.size(); i < size; i++)
        {
            buf.append(((MethodType) order.get(i))._name);
            buf.append(',');
        }

        buf.setLength(buf.length() - 1);
        _sortOrder = buf.toString();
    }

    //~ Instance variables ---------------------------------------------------------------

    /** A user-friendly string representation of the type. */
    private final String _name;

    /** The bit value of the method type. */
    //TODO private final int _key;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new MethodType object.
     *
     * @param name a string representing the method type.
     * @param key a bit value representing the method type.
     */
    private MethodType(
        String name,
        int    key)
    {
        _name = name;
        // TODO _key = key;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the order to use as the natural order.
     *
     * @param str a string representing the new order. The string must consist of exactly
     *        four different, comma delimited strings which represents a method type
     *        (e.g. <code>isFoo,getFoo,setFoo,foo</code>)
     *
     * @throws IllegalArgumentException if the given string is invalid.
     */
    public static synchronized void setOrder(String str)
    {
        if ((str == null) || (str.length() == 0))
        {
            throw new IllegalArgumentException("order == " + str);
        }

        StringTokenizer tokens = new StringTokenizer(str, ",");
        List temp = new ArrayList(order.size());
        StringBuffer buf = new StringBuffer(20);

        while (tokens.hasMoreElements())
        {
            String token = tokens.nextToken();
            MethodType type = valueOf(token);

            if (temp.contains(type))
            {
                throw new IllegalArgumentException("invalid order string " + order);
            }

            temp.add(type);
            buf.append(type.toString());
            buf.append(',');
        }

        if (order.size() != temp.size())
        {
            throw new IllegalArgumentException("invalid order string " + order);
        }

        if (
            !temp.contains(GETTER) || !temp.contains(SETTER) || !temp.contains(SETTER)
            || !temp.contains(OTHER))
        {
            throw new IllegalArgumentException("invalid order string " + order);
        }

        order.clear();
        order.addAll(temp);
        buf.deleteCharAt(buf.length() - 1);
        _sortOrder = buf.toString();
    }


    /**
     * Returns a string representation of the current sort order. To encode the different
     * method types, the common foo abreviation is used:
     * <pre>
     *      <blockquote style="background:lightgrey">
     *       isFoo,getFoo,setFoo,foo  or
     *       foo,getFoo,isFoo,setFoo  or
     *       ...
     *      </blockquote>
     *      </pre>
     *
     * @return a string representation of the current sort order.
     */
    public static synchronized String getOrder()
    {
        return _sortOrder;
    }


    /**
     * Compares this object with the specified object for order. Returns a negative
     * integer, zero, or a positive integer as this object is less than, equal to, or
     * greater than the specified object.
     *
     * @param other the object to be compared.
     *
     * @return DOCUMENT ME!
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

        if (other instanceof MethodType)
        {
            int thisIndex = MethodType.order.indexOf(this);
            int otherIndex = MethodType.order.indexOf(other);

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
        return _name;
    }


    /**
     * Returns the method type of the given method name.
     *
     * @param name a method name.
     *
     * @return the method type of the given name.
     *
     * @throws IllegalArgumentException if no valid method name is given.
     */
    public static MethodType valueOf(String name)
    {
        if ((name == null) || (name.trim().length() == 0))
        {
            throw new IllegalArgumentException("invalid method name " + name);
        }

        name = name.trim();

        if (isGetter(name))
        {
            return GETTER;
        }
        else if (isSetter(name))
        {
            return SETTER;
        }
        else if (isTester(name))
        {
            return TESTER;
        }
        else
        {
            return OTHER;
        }
    }


    /**
     * Indicates whether this type represents a Java Bean method.
     *
     * @return <code>true</code> if this method type adheres to the Java Bean naming
     *         conventions.
     */
    public boolean isBean()
    {
        return (this == GETTER) || (this == SETTER) || (this == TESTER);
    }


    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static boolean isGetter(String name)
    {
        int length = name.length();

        if (length > 3)
        {
            if (name.startsWith(PREF_GET) && Character.isUpperCase(name.charAt(3)))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static boolean isSetter(String name)
    {
        int length = name.length();

        if (length > 3)
        {
            if (name.startsWith(PREF_SET) && Character.isUpperCase(name.charAt(3)))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static boolean isTester(String name)
    {
        int length = name.length();

        if (length > 2)
        {
            if (name.startsWith(PREF_IS) && Character.isUpperCase(name.charAt(2)))
            {
                return true;
            }
        }

        return false;
    }
}
