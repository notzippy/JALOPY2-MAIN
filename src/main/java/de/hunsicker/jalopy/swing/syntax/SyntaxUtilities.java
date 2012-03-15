/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
/*
* SyntaxUtilities.java - Utility functions used by syntax colorizing
* Copyright (C) 1999 Slava Pestov
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

import java.awt.Color;

import javax.swing.text.Segment;


/**
 * Class with segment and bracket matching functions used by the syntax colorizing
 * subsystem. It also provides a way to get the default color table.
 *
 * @author Slava Pestov
 * @version $Id: SyntaxUtilities.java,v 1.2 2006/01/13 20:27:25 notzippy Exp $
 */
public class SyntaxUtilities
{
    //~ Static variables/initializers ----------------------------------------------------

    // TODO private static Color[] COLORS;

    //~ Constructors ---------------------------------------------------------------------

    private SyntaxUtilities()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the default color table. This can be passed to the
     * <code>setColors()</code> method of <code>SyntaxDocument</code> to use the default
     * syntax colors.
     *
     * @return the default colors.
     */
    public static Color[] getDefaultSyntaxColors()
    {
        Color[] colors = new Color[Token.ID_COUNT];

        colors[Token.COMMENT1] = new Color(0, 128, 0); // private comment
        colors[Token.COMMENT2] = colors[Token.COMMENT1]; // Javadoc comment
        colors[Token.COMMENT3] = colors[Token.COMMENT1]; // stand-out comment
        colors[Token.KEYWORD1] = Color.blue; // red(ish)
        colors[Token.KEYWORD2] = Color.blue;
        colors[Token.KEYWORD3] = Color.blue;
        colors[Token.LITERAL1] = Color.gray; // string
        colors[Token.LITERAL2] = Color.blue;
        colors[Token.LABEL] = new Color(255, 128, 0);
        colors[Token.OPERATOR] = Color.blue;
        colors[Token.INVALID] = Color.red;

        return colors;
    }


    /**
     * Checks if a subregion of a <code>Segment</code> is equal to a character array.
     *
     * @param ignoreCase True if case should be ignored, false otherwise
     * @param text The segment
     * @param offset The offset into the segment
     * @param match The character array to match
     *
     * @return <code>true</code> if the given Segment is equal to the character array.
     */
    public static boolean regionMatches(
        boolean ignoreCase,
        Segment text,
        int     offset,
        char[]  match)
    {
        int length = offset + match.length;
        char[] textArray = text.array;

        if (length > textArray.length)
        {
            return false;
        }

        for (int i = offset, j = 0; i < length; i++, j++)
        {
            char c1 = textArray[i];
            char c2 = match[j];

            if (ignoreCase)
            {
                c1 = Character.toUpperCase(c1);
                c2 = Character.toUpperCase(c2);
            }

            if (c1 != c2)
            {
                return false;
            }
        }

        return true;
    }
}
