/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.util;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * String related helper functions.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
public final class StringHelper
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Trim both leading and trailing whitespace. */
    public static final int TRIM_ALL = 1;

    /** Trim only leading whitespace. */
    public static final int TRIM_LEADING = 4;

    /** No trimming. */
    public static final int TRIM_NONE = 0;
    private static final String EMPTY_STRING = "" /* NOI18N */.intern();
    private static final String LINE_SEPARATOR = "\n" /* NOI18N */;
    private static final String SPACE = " " /* NOI18N */;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new StringHelper object.
     */
    private StringHelper()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the name part of the given qualified java class name.
     *
     * @param qualifiedName name for which the name part should be returned.
     *
     * @return name part of the given name. Returns <code>null</code> if the given name
     *         was <code>null</code> itself.
     *
     * @see #getPackageName
     */
    public static String getClassName(String qualifiedName)
    {
        int lastDot = qualifiedName.lastIndexOf('.');

        if (lastDot > 0)
        {
            return qualifiedName.substring(lastDot + 1);
        }
        return qualifiedName;
    }


    /**
     * Checks whether the given string represents a number.
     *
     * @param str string to check.
     *
     * @return <code>true</code> if <em>str</em> represents a number.
     */
    public static boolean isNumber(String str)
    {
        if (str == null)
        {
            return false;
        }

        int letters = 0;

        for (int i = 0, size = str.length(); i < size; i++)
        {
            if ((str.charAt(i) < 48) || (str.charAt(i) > 57))
            {
                letters++;
            }
        }

        if ((letters > 1) || ((letters == 1) && (str.length() == 1)))
        {
            return false;
        }

        return true;
    }


    /**
     * Returns the package name of the given Java class name.
     *
     * @param name class name for which the package name should be returned.
     *
     * @return package name of the given class name. Returns the empty string if the
     *         given name contains no package name (the default package).
     *
     * @see #getClassName
     */
    public static String getPackageName(String name)
    {
        int lastDot = name.lastIndexOf('.');

        if (lastDot > 0)
        {
            return name.substring(0, lastDot);
        }

        return EMPTY_STRING;
    }


    /**
     * Returns <code>true</code> if the given letter is uppercase.
     *
     * @param letter letter to check for capitalization.
     *
     * @return <code>true</code> if the given letter is uppercase.
     */
    public static boolean isUppercase(char letter)
    {
        if ((letter < 'A') || (letter > 'Z'))
        {
            return false;
        }

        return true;
    }


    /**
     * Returns <code>true</code> if the given string contains at least one uppercase
     * letter.
     *
     * @param str string to check for uppercase letters.
     *
     * @return <code>true</code> if the given string contains at least one uppercase
     *         letter.
     */
    public static boolean containsUppercase(String str)
    {
        if (str.length() == 0)
        {
            return false;
        }

        for (int i = 0, size = str.length(); i < size; i++)
        {
            if (isUppercase(str.charAt(i)))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Returns the index within the given string of the <em>x.</em> occurrence of the
     * specified character.
     *
     * @param character character to search.
     * @param str the string to search.
     * @param x <em>x.</em> occurrence of the character to search for.
     *
     * @return s the index within the given string of the <em>x.</em> occurrence of the
     *         given character. Returns <code>-1</code> if the specified character is
     *         not contained in the given string or it occurs less than the specified
     *         occurrence to look for.
     */
    public static int indexOf(
        char   character,
        String str,
        int    x)
    {
        for (int i = 1, pos = -1; (pos = str.indexOf(character, pos + 1)) > -1; i++)
        {
            if (i == x)
            {
                return pos;
            }
        }

        return -1;
    }


    /**
     * Returns the offset of the first non-whitespace character of the given string.
     *
     * @param str a string.
     *
     * @return the offset of the first non-whitespace character in the given string.
     *         Returns <code>-1</code> if no non-whitespace character could be found.
     */
    public static int indexOfNonWhitespace(String str)
    {
        return indexOfNonWhitespace(str, 0);
    }


    /**
     * Returns the offset of the first non-whitespace character of the given string.
     *
     * @param str a string.
     * @param beginOffset DOCUMENT ME!
     *
     * @return the offset of the first non-whitespace character in the given string.
     *         Returns <code>-1</code> if no non-whitespace character could be found.
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static int indexOfNonWhitespace(
        String str,
        int    beginOffset)
    {
        if (beginOffset < 0)
        {
            throw new IllegalArgumentException("beginOffset < 0 -- " + beginOffset);
        }

        for (int i = beginOffset, size = str.length(); i < size; i++)
        {
            switch (str.charAt(i))
            {
                case ' ' :
                case '\t' :
                    break;

                default :
                    return i;
            }
        }

        return -1;
    }


    /**
     * Left pad a String with spaces. Pad to a size of n.
     *
     * @param str String to pad out
     * @param size int size to pad to
     *
     * @return DOCUMENT ME!
     */
    public static String leftPad(
        String str,
        int    size)
    {
        return leftPad(str, size, SPACE);
    }


    /**
     * Left pad a String with a specified string. Pad to a size of n.
     *
     * @param str String to pad out
     * @param size int size to pad to
     * @param delim String to pad with
     *
     * @return DOCUMENT ME!
     */
    public static String leftPad(
        String str,
        int    size,
        String delim)
    {
        size = (size - str.length()) / delim.length();

        if (size > 0)
        {
            str = repeat(delim, size) + str;
        }

        return str;
    }


    /**
     * Returns the given string with the first letter converted to lower case.
     *
     * @param str string to transform.
     *
     * @return the transformed string. If the string already begins with a lower case
     *         letter or is the empty string, the original string is returned.
     */
    public static String lowercaseFirst(String str)
    {
        if (str.length() == 0)
        {
            return str;
        }

        if (isUppercase(str.charAt(0)))
        {
            char[] letters = str.toCharArray();
            letters[0] = (char) (letters[0] + 32);

            return new String(letters);
        }

        return str;
    }


    /**
     * Returns the number of occurrences of <em>character</em> in <em>string</em>.
     *
     * @param character character to search for.
     * @param string character array to search.
     *
     * @return number of occurrences of <em>character</em> in <em>string</em>. Returns
     *         <code>0</code> if no occurrences could be found.
     */
    public static int occurs(
        char   character,
        char[] string)
    {
        int count = 0;

        for (int i = 0; i < string.length; i++)
        {
            if (string[i] == character)
            {
                count++;
            }
        }

        return count;
    }


    /**
     * Returns the number of occurrences of <em>character</em> in <em>string</em>.
     *
     * @param character character to search for.
     * @param string string to search.
     *
     * @return number of occurrences of <em>character</em> in <em>string</em>. Returns
     *         <code>0</code> if no occurrences could be found.
     */
    public static int occurs(
        char   character,
        String string)
    {
        return occurs(character, string.toCharArray());
    }


    /**
     * Repeat a string n times to form a new string.
     *
     * @param str String to repeat
     * @param repeat int number of times to repeat
     *
     * @return String with repeated string
     */
    public static String repeat(
        String str,
        int    repeat)
    {
        StringBuffer buffer = new StringBuffer(repeat * str.length());

        for (int i = 0; i < repeat; i++)
        {
            buffer.append(str);
        }

        return buffer.toString();
    }


    /**
     * Searchs and replaces fixed string matches within the given string.
     *
     * @param original the original string.
     * @param replaceFrom the substring to be find.
     * @param replaceTo the substring to replace it with.
     *
     * @return new string with all occurrences replaced.
     */
    public static String replace(
        String original,
        String replaceFrom,
        String replaceTo)
    {
        if (EMPTY_STRING.equals(replaceFrom))
        {
            return original;
        }

        if (original.indexOf(replaceFrom) == -1)
        {
            return original;
        }

        StringBuffer buf = new StringBuffer(original.length());
        int index = 0;

        for (;;)
        {
            int pos = original.indexOf(replaceFrom, index);

            if (pos == -1)
            {
                buf.append(original.substring(index));

                return buf.toString();
            }

            buf.append(original.substring(index, pos));
            buf.append(replaceTo);
            index = pos + replaceFrom.length();

            if (index == original.length())
            {
                return buf.toString();
            }
        }
    }


    /**
     * Splits the given string into chunks.
     *
     * @param str string to split into chunks.
     * @param delim the delimeter to use for splitting.
     *
     * @return array with the individual chunks.
     *
     * @since 1.0b8
     */
    public static String[] split(
        String str,
        String delim)
    {
        int startOffset = 0;
        int endOffset = -1;
        int sepLength = delim.length();
        List lines = new ArrayList(15);

        while ((endOffset = str.indexOf(delim, startOffset)) > -1)
        {
            lines.add(str.substring(startOffset, endOffset));
            startOffset = endOffset + sepLength;
        }

        if (startOffset > 0)
        {
            lines.add(str.substring(startOffset));
        }
        else
        {
            lines.add(str);
        }

        return (String[]) lines.toArray(EMPTY_STRING_ARRAY);
    }


    /**
     * Returns <code>true</code> if the given string starts with an uppercase letter.
     *
     * @param str string to check.
     *
     * @return <code>true</code> if the given string starts with an uppercase letter.
     */
    public static boolean startsWithUppercase(String str)
    {
        if (str.length() == 0)
        {
            return false;
        }

        return isUppercase(str.charAt(0));
    }


    /**
     * Removes trailing whitespace from the given string.
     *
     * @param str the string to trim.
     *
     * @return a copy of the string with trailing whitespace removed, or the original
     *         string if no trailing whitespace could be removed.
     */
    public static String trimTrailing(String str)
    {
        int index = str.length();

        while ((index > 0) && Character.isWhitespace(str.charAt(index - 1)))
        {
            index--;
        }

        if (index != str.length())
        {
            return str.substring(0, index);
        }
        return str;
    }


    /**
     * Wraps multi-line strings.
     *
     * @param str the string to wrap.
     * @param width the maximum width of lines.
     * @param removeNewLines if <code>true</code>, any newlines in the original string
     *        are ignored.
     *
     * @return the whole string with embedded newlines.
     */
    public static String wrapString(
        String  str,
        int     width,
        boolean removeNewLines)
    {
        String[] lines =
            wrapStringToArray(str, width, LINE_SEPARATOR, removeNewLines, TRIM_ALL);
        StringBuffer buf = new StringBuffer(str.length());

        for (int i = 0; i < lines.length; i++)
        {
            buf.append(lines[i]);
            buf.append('\n');
        }

        return (buf.toString());
    }


    /**
     * Wraps multi-line strings (and returns the individual lines).
     *
     * @param str the string to wrap.
     * @param width the maximum width of each line.
     * @param lineSeparator the lineSeparator string used for newlines.
     * @param removeNewLines if <code>true</code>, any newlines in the original string
     *        are ignored.
     * @param trimPolicy trim the resulting lines according to the given policy.
     *
     * @return the lines after wrapping.
     */
    public static String[] wrapStringToArray(
        String  str,
        int     width,
        String  lineSeparator,
        boolean removeNewLines,
        int     trimPolicy)
    {
        return wrapStringToArray(
            str, width, new SpaceBreakIterator(), lineSeparator, removeNewLines,
            trimPolicy);
    }


    /**
     * Wraps multi-line strings (and get the individual lines).
     *
     * @param str the string to wrap.
     * @param width the maximum width of each line.
     * @param breakIter the iterator to use to break the string into chunks.
     * @param lineSeparator the lineSeparator string used for newlines.
     * @param removeNewLines if <code>true</code>, any newlines in the original string
     *        are ignored.
     * @param trimPolicy trim the resulting lines according to the given policy.
     *
     * @return the lines after wrapping.
     */
    public static String[] wrapStringToArray(
        String        str,
        int           width,
        BreakIterator breakIter,
        String        lineSeparator,
        boolean       removeNewLines,
        int           trimPolicy)
    {
        if (str.length() == 0)
        {
            return (new String[] { str });
        }

        String[] workingSet = null;

        if (removeNewLines)
        {
            str = str.trim();
            str = replace(str, lineSeparator, SPACE);
            workingSet = new String[] { str };
        }
        else
        {
            /**
             * @todo StringTokenizer skips empty lines!!!
             */
            StringTokenizer tokens = new StringTokenizer(str, lineSeparator);
            int len = tokens.countTokens();
            workingSet = new String[len];

            // length of the leading whitespace
            int leadingWhitespace = 0;

            for (int i = 0; i < len; i++)
            {
                String token = tokens.nextToken();

                if (trimPolicy == TRIM_LEADING)
                {
LOOP: 
                    for (int j = 0; j < token.length(); j++)
                    {
                        switch (token.charAt(j))
                        {
                            case ' ' :
                            case '\t' :
                                break;

                            default :

                                // special handling for the second line
                                if (i == 1)
                                {
                                    leadingWhitespace = j;
                                }

                                // but we have to recheck for every other line, because
                                // it could be that the following lines have a smaller
                                // indentation that the preceding, e.g.
                                //
                                // if (level > _indentLevel + 1 ||
                                //     level < _indentLevel - 1)
                                // {
                                // ...
                                // }
                                else if (j < leadingWhitespace)
                                {
                                    leadingWhitespace = j;
                                }

                                break LOOP;
                        }
                    }

                    // trim to asterix, if any
                    if (isLeadingAsterix(token))
                    {
                        token = token.trim();
                        token = ' ' + token;
                    }
                    else if (token.length() < leadingWhitespace)
                    {
                        ;
                    }
                    else if (i > 0)
                    {
                        // strip the leading whitespace
                        token = "   " + token.substring(leadingWhitespace);
                    }
                }

                workingSet[i] = token;
            }
        }

        if (width < 1)
        {
            width = 1;
        }

        if (str.length() <= width)
        {
            return workingSet;
        }


// check if all lines are within the given max width
WIDTHCHECK: 
        {
            boolean ok = true;

            for (int i = 0; i < workingSet.length; i++)
            {
                ok = ok && (workingSet[i].length() < width);

                if (!ok)
                {
                    break WIDTHCHECK;
                }
            }

            return workingSet;
        }

        List lines = new ArrayList(workingSet.length);
        int lineStart = 0;

        for (int i = 0; i < workingSet.length; i++)
        {
            if (workingSet[i].length() < width)
            {
                switch (trimPolicy)
                {
                    case TRIM_ALL :
                        lines.add(workingSet[i].trim());

                        break;

                    default :
                        lines.add(workingSet[i]);
                }
            }
            else
            {
                breakIter.setText(workingSet[i]);

                int nextStart = breakIter.next();
                int prevStart = 0;

                do
                {
                    while (
                        ((nextStart - lineStart) < width)
                        && (nextStart != BreakIterator.DONE))
                    {
                        prevStart = nextStart;
                        nextStart = breakIter.next();
                    }

                    if (prevStart == 0)
                    {
                        prevStart = nextStart;
                    }

                    if (nextStart == BreakIterator.DONE)
                    {
                        // if the text before and after the last space fits
                        // into the max width, just print it on one line
                        if (
                            ((prevStart - lineStart)
                            + (workingSet[i].length() - prevStart)) < width)
                        {
                            switch (trimPolicy)
                            {
                                case TRIM_ALL :
                                    lines.add(
                                        workingSet[i].substring(
                                            lineStart, workingSet[i].length()).trim());

                                    break;

                                default :
                                    lines.add(
                                        workingSet[i].substring(
                                            lineStart, workingSet[i].length()));
                            }
                        }
                        else
                        {
                            // otherwise use two lines
                            switch (trimPolicy)
                            {
                                case TRIM_ALL :

                                    if (prevStart > 0) // more than one line
                                    {
                                        lines.add(
                                            workingSet[i].substring(lineStart, prevStart)
                                                         .trim());
                                        lines.add(
                                            workingSet[i].substring(prevStart).trim());
                                    }
                                    else
                                    {
                                        lines.add(
                                            workingSet[i].substring(lineStart).trim());
                                    }

                                    break;

                                default :

                                    if (prevStart > 0) // more than one line
                                    {
                                        lines.add(
                                            workingSet[i].substring(lineStart, prevStart));
                                        lines.add(workingSet[i].substring(prevStart));
                                    }
                                    else
                                    {
                                        lines.add(
                                            workingSet[i].substring(lineStart).trim());
                                    }
                            }
                        }

                        prevStart = workingSet[i].length();
                    }
                    else
                    {
                        switch (trimPolicy)
                        {
                            case TRIM_ALL :
                                lines.add(
                                    workingSet[i].substring(lineStart, prevStart).trim());

                                break;

                            default :
                                lines.add(workingSet[i].substring(lineStart, prevStart));
                        }
                    }

                    lineStart = prevStart;
                    prevStart = 0;
                }
                while (lineStart < workingSet[i].length());

                lineStart = 0;
            }
        }

        String[] s = new String[lines.size()];

        return (String[]) lines.toArray(s);
    }


    /**
     * Checks whether the first non-whitespace character of the given line of text is the
     * asterix (&#042).
     *
     * @param line line of text to check.
     *
     * @return <code>true</code> if the first non-whitespace character of the given text
     *         is an asterix.
     */
    private static boolean isLeadingAsterix(String line)
    {
        if (line == null)
        {
            return false;
        }

        for (int i = 0, size = line.length(); i < size; i++)
        {
            int character = line.charAt(i);

            switch (character)
            {
                case ' ' :
                case '\t' :

                    continue;

                // skip leading whitespace
                default :

                    if (character == '*')
                    {
                        return true;
                    }
                    return false;
            }
        }

        return false;
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * A BreakIterator for space breaks. Implements only the functionality which is
     * needed by {@link #wrapStringToArray}.
     */
    private static class SpaceBreakIterator
        extends BreakIterator
    {
        static final String BR = "<br>" /* NOI18N */;
        String text;
        boolean isBreak;
        int end = -1;
        int pos = -1;

        public boolean isBreak()
        {
            return isBreak;
        }


        public void setText(String text)
        {
            this.end = -1;
            this.pos = -1;
            this.text = text;
        }


        public void setText(CharacterIterator iter)
        {
            throw new UnsupportedOperationException();
        }


        public CharacterIterator getText()
        {
            throw new UnsupportedOperationException();
        }


        public int current()
        {
            throw new UnsupportedOperationException();
        }


        public int first()
        {
            throw new UnsupportedOperationException();
        }


        public int following(int offset)
        {
            throw new UnsupportedOperationException();
        }


        public int last()
        {
            throw new UnsupportedOperationException();
        }


        public int next()
        {
            this.isBreak = false;
            this.pos = text.indexOf(' ', this.end + 1);

            int tab = text.indexOf('\t', this.end + 1);
            int br = text.indexOf(BR, this.end + 1);

            if ((this.pos > -1) && (tab > -1) && (tab < this.pos))
            {
                this.pos = tab;
            }

            if ((this.pos > -1) && (br > -1) && (br < this.pos))
            {
                this.pos = br;
                this.isBreak = true;
            }

            if (this.pos == -1)
            {
                return BreakIterator.DONE;
            }

            this.end = this.pos;

            return pos;
        }


        public int next(int offset)
        {
            throw new UnsupportedOperationException();
        }


        public int previous()
        {
            throw new UnsupportedOperationException();
        }
    }
}
