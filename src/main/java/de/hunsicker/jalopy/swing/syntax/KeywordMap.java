/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
/*
 * KeywordMap.java - Fast keyword->id map
 * Copyright (C) 1998, 1999 Slava Pestov
 * Copyright (C) 1999 Mike Dillon
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 */
package de.hunsicker.jalopy.swing.syntax;

import javax.swing.text.Segment;


/**
 * A <code>KeywordMap</code> is similar to a hashtable in that it maps keys to values.
 * However, the `keys' are Swing segments. This allows lookups of text substrings
 * without the overhead of creating a new string object.
 * 
 * <p>
 * This class is used by <code>CTokenMarker</code> to map keywords to ids.
 * </p>
 *
 * @author Slava Pestov, Mike Dillon
 * @version $Id: KeywordMap.java,v 1.1 2002/11/11 22:08:35 marcohu Exp $
 */
public final class KeywordMap
{
    //~ Instance variables ---------------------------------------------------------------

    // protected members

    /** DOCUMENT ME! */
    protected int mapLength;
    private Keyword[] map;
    private boolean ignoreCase;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new <code>KeywordMap</code>.
     *
     * @param ignoreCase True if keys are case insensitive
     */
    public KeywordMap(boolean ignoreCase)
    {
        this(ignoreCase, 52);
        this.ignoreCase = ignoreCase;
    }


    /**
     * Creates a new <code>KeywordMap</code>.
     *
     * @param ignoreCase True if the keys are case insensitive
     * @param mapLength The number of `buckets' to create. A value of 52 will give good
     *        performance for most maps.
     */
    public KeywordMap(
        boolean ignoreCase,
        int     mapLength)
    {
        this.mapLength = mapLength;
        this.ignoreCase = ignoreCase;
        map = new Keyword[mapLength];
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets if the keyword map should be case insensitive.
     *
     * @param ignoreCase True if the keyword map should be case insensitive, false
     *        otherwise
     */
    public void setIgnoreCase(boolean ignoreCase)
    {
        this.ignoreCase = ignoreCase;
    }


    /**
     * Returns true if the keyword map is set to be case insensitive, false otherwise.
     *
     * @return DOCUMENT ME!
     */
    public boolean getIgnoreCase()
    {
        return ignoreCase;
    }


    /**
     * Adds a key-value mapping.
     *
     * @param keyword The key
     * @param id The value
     */
    public void add(
        String keyword,
        byte   id)
    {
        int key = getStringMapKey(keyword);
        map[key] = new Keyword(keyword.toCharArray(), id, map[key]);
    }


    /**
     * Looks up a key.
     *
     * @param text The text segment
     * @param offset The offset of the substring within the text segment
     * @param length The length of the substring
     *
     * @return DOCUMENT ME!
     */
    public byte lookup(
        Segment text,
        int     offset,
        int     length)
    {
        if (length == 0)
        {
            return Token.NULL;
        }

        Keyword k = map[getSegmentMapKey(text, offset, length)];

        while (k != null)
        {
            if (length != k.keyword.length)
            {
                k = k.next;

                continue;
            }

            if (SyntaxUtilities.regionMatches(ignoreCase, text, offset, k.keyword))
            {
                return k.id;
            }

            k = k.next;
        }

        return Token.NULL;
    }


    /**
     * DOCUMENT ME!
     *
     * @param s DOCUMENT ME!
     * @param off DOCUMENT ME!
     * @param len DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected int getSegmentMapKey(
        Segment s,
        int     off,
        int     len)
    {
        return (Character.toUpperCase(s.array[off])
        + Character.toUpperCase(s.array[(off + len) - 1])) % mapLength;
    }


    /**
     * DOCUMENT ME!
     *
     * @param s DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected int getStringMapKey(String s)
    {
        return (Character.toUpperCase(s.charAt(0))
        + Character.toUpperCase(s.charAt(s.length() - 1))) % mapLength;
    }

    //~ Inner Classes --------------------------------------------------------------------

    // private members
    class Keyword
    {
        public Keyword next;
        public char[] keyword;
        public byte id;

        public Keyword(
            char[]  keyword,
            byte    id,
            Keyword next)
        {
            this.keyword = keyword;
            this.id = id;
            this.next = next;
        }
    }
}
