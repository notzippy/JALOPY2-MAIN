/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A class that represents version information. Version numbering follows the &quot;Dewey
 * Decimal&quot; syntax that consists of positive decimal integers separated by periods
 * &quot;.&quot;
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public class Version
{
    //~ Instance variables ---------------------------------------------------------------

    private final String _name;
    private final boolean _beta;
    private final byte _major;
    private final byte _micro;
    private final byte _minor;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Version object.
     *
     * @param name a product version name, may be <code>null</code>
     * @param major the major version number.
     * @param minor the minor version number.
     */
    public Version(
        String name,
        int    major,
        int    minor)
    {
        this(name, major, minor, 0);
    }


    /**
     * Creates a new Version object.
     *
     * @param name a product version name, may be <code>null</code>
     * @param major the major version number.
     * @param minor the minor version number.
     * @param micro the micro version number.
     */
    public Version(
        String name,
        int    major,
        int    minor,
        int    micro)
    {
        this(name, major, minor, micro, false);
    }


    /**
     * Creates a new Version object.
     *
     * @param name a product version name, may be <code>null</code>
     * @param major the major version number.
     * @param minor the minor version number.
     * @param micro the micro version number.
     * @param beta <code>true</code> indicates that this is a beta version.
     */
    public Version(
        String  name,
        int     major,
        int     minor,
        int     micro,
        boolean beta)
    {
        _name = name;
        _major = (byte) major;
        _minor = (byte) minor;
        _micro = (byte) micro;
        _beta = beta;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Indicates whether this version represents a beta state.
     *
     * @return <code>true</code> if this version represents a beta state.
     */
    public boolean isBeta()
    {
        return _beta;
    }


    /**
     * Returns the major version number.
     *
     * @return major version number
     */
    public byte getMajorNumber()
    {
        return _major;
    }


    /**
     * Returns the micro version number.
     *
     * @return micro version number
     */
    public byte getMicroNumber()
    {
        return _micro;
    }


    /**
     * Returns the minor version number.
     *
     * @return minor version number
     */
    public byte getMinorNumber()
    {
        return _minor;
    }


    /**
     * Returns the version name.
     *
     * @return version name. Returns <code>null</code> if no product version name was
     *         specified.
     */
    public String getName()
    {
        return _name;
    }


    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (o instanceof Version)
        {
            Version other = (Version) o;

            return (_minor == other._minor) && (_major == other._major)
            && (_beta == other._beta) && (_micro == other._micro)
            && _name.equals(other._name);
        }

        return false;
    }


    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        int result = 17;
        result = (result * 37) + _major;
        result = (result * 37) + _minor;
        result = (result * 37) + _micro;
        result = (result * 37) + (_beta ? 1
                                        : 2);

        return _name.hashCode() + result;
    }


    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer(10);
        buf.append(_major);
        buf.append('.');
        buf.append(_minor);

        if (_beta)
        {
            buf.append('b');
        }
        else
        {
            buf.append('.');
        }

        buf.append(_micro);

        return buf.toString();
    }


    /**
     * Returns the version object for the given string.
     *
     * @param version a string representing version information. The string must consist
     *        of multiple (up to three) positive decimal integers separated by periods
     *        and may contain the character '<code>b</code>' instead of the last period
     *        (indicating a beta version). E.g. <code>1.0</code>, <code>1.4.1</code>,
     *        <code>1.0b5</code> are valid, <code>1</code>, <code>1b5</code>,
     *        <code>1.3.3.1</code> are not.
     *
     * @return version version object for the given string.
     *
     * @throws IllegalArgumentException if <em>version</em> represents no valid version
     *         information.
     */
    public static Version valueOf(String version)
    {
        Pattern regexp = Pattern.compile("(\\d).(\\d)(?:([.b])(\\d+))?" /* NOI18N */);
		Matcher matcher = regexp.matcher(version);
        if (matcher.matches())
        {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));

            int micro = 0;
            boolean beta = false;

            if (matcher.groupCount() == 5)
            {
                beta = matcher.group(3).indexOf('b') > -1;
                micro = Integer.parseInt(matcher.group(4));
            }

            return new Version(null, major, minor, micro, beta);
        }
        throw new IllegalArgumentException(
            "invalid version information -- " + version);
    }
}
