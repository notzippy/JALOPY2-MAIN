/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
/*
 * JavaTokenMarker.java - C token marker
 * Copyright (C) 1998, 1999 Slava Pestov
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
 * Java token marker.
 *
 * @author Slava Pestov
 * @version $Id: JavaTokenMarker.java,v 1.4 2006/01/13 20:27:25 notzippy Exp $
 */
public final class JavaTokenMarker
    extends TokenMarker
{
    //~ Static variables/initializers ----------------------------------------------------

    // private members
    private static KeywordMap cKeywords;

    //~ Instance variables ---------------------------------------------------------------

    private KeywordMap keywords;
    private boolean cpp;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new JavaTokenMarker object.
     */
    public JavaTokenMarker()
    {
        this(true, getKeywords());
    }


    /**
     * Creates a new JavaTokenMarker object.
     *
     * @param cpp DOCUMENT ME!
     * @param keywords DOCUMENT ME!
     */
    public JavaTokenMarker(
        boolean    cpp,
        KeywordMap keywords)
    {
        this.cpp = cpp;
        this.keywords = keywords;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static KeywordMap getKeywords()
    {
        if (cKeywords == null)
        {
            cKeywords = new KeywordMap(false);
            cKeywords.add("package", Token.KEYWORD2);
            cKeywords.add("import", Token.KEYWORD2);
            cKeywords.add("class", Token.KEYWORD2);
            cKeywords.add("interface", Token.KEYWORD2);
            cKeywords.add("extends", Token.KEYWORD2);
            cKeywords.add("implements", Token.KEYWORD2);
            cKeywords.add("enum", Token.KEYWORD2);
            cKeywords.add("@interface", Token.KEYWORD2);
            cKeywords.add("@Target", Token.KEYWORD2);
            cKeywords.add("@Retention", Token.KEYWORD2);

            cKeywords.add("byte", Token.KEYWORD3);
            cKeywords.add("char", Token.KEYWORD3);
            cKeywords.add("short", Token.KEYWORD3);
            cKeywords.add("int", Token.KEYWORD3);
            cKeywords.add("long", Token.KEYWORD3);
            cKeywords.add("float", Token.KEYWORD3);
            cKeywords.add("double", Token.KEYWORD3);
            cKeywords.add("boolean", Token.KEYWORD3);
            cKeywords.add("void", Token.KEYWORD3);

            cKeywords.add("abstract", Token.KEYWORD1);
            cKeywords.add("final", Token.KEYWORD1);
            cKeywords.add("private", Token.KEYWORD1);
            cKeywords.add("protected", Token.KEYWORD1);
            cKeywords.add("public", Token.KEYWORD1);
            cKeywords.add("static", Token.KEYWORD1);
            cKeywords.add("synchronized", Token.KEYWORD1);
            cKeywords.add("volatile", Token.KEYWORD1);
            cKeywords.add("transient", Token.KEYWORD1);
            cKeywords.add("native", Token.KEYWORD1);
            cKeywords.add("strictfp", Token.KEYWORD1);
            cKeywords.add("break", Token.KEYWORD1);
            cKeywords.add("case", Token.KEYWORD1);
            cKeywords.add("continue", Token.KEYWORD1);
            cKeywords.add("default", Token.KEYWORD1);
            cKeywords.add("do", Token.KEYWORD1);
            cKeywords.add("else", Token.KEYWORD1);
            cKeywords.add("for", Token.KEYWORD1);
            cKeywords.add("if", Token.KEYWORD1);
            cKeywords.add("instanceof", Token.KEYWORD1);
            cKeywords.add("new", Token.KEYWORD1);
            cKeywords.add("return", Token.KEYWORD1);
            cKeywords.add("switch", Token.KEYWORD1);
            cKeywords.add("while", Token.KEYWORD1);
            cKeywords.add("throw", Token.KEYWORD1);
            cKeywords.add("try", Token.KEYWORD1);
            cKeywords.add("catch", Token.KEYWORD1);
            cKeywords.add("finally", Token.KEYWORD1);
            cKeywords.add("throws", Token.KEYWORD1);
            cKeywords.add("assert", Token.KEYWORD1);

            cKeywords.add("this", Token.LITERAL2);
            cKeywords.add("null", Token.LITERAL2);
            cKeywords.add("super", Token.LITERAL2);
            cKeywords.add("true", Token.LITERAL2);
            cKeywords.add("false", Token.LITERAL2);
        }

        return cKeywords;
    }


    /**
     * Marks the token 
     *
     * @param token The token to mark
     * @param line The line it is on
     * @param lineIndex The index of the line
     *
     * @return DOCUMENT ME!
     */
    public byte markTokensImpl(
        byte    token,
        Segment line,
        int     lineIndex)
    {
        char[] array = line.array;
        int offset = line.offset;
        int lastOffset = offset;
        int lastKeyword = offset;
        int newLength = line.count + offset;
        boolean backslash = false;
loop:
        for (int i = offset; i < newLength; i++)
        {
            int i1 = (i + 1);

            char c = array[i];

            switch (c)
            {
                case '\\' :
                    backslash = !backslash;

                    break;

                case '*' :

                    if (
                        ((token == Token.COMMENT1) || (token == Token.COMMENT2)
                        || (token == Token.COMMENT3)) && ((newLength - i) > 1))
                    {
                        backslash = false;

                        if (((newLength - i) > 1) && (array[i1] == '/'))
                        {
                            i++;
                            addToken((i + 1) - lastOffset, token);
                            token = Token.NULL;
                            lastOffset = i + 1;
                            lastKeyword = lastOffset;
                        }
                    }

                    break;

                case '#' :
                    backslash = false;

                    if (cpp && (token == Token.NULL))
                    {
                        token = Token.KEYWORD2;
                        addToken(i - lastOffset, Token.NULL);
                        addToken(newLength - i, Token.KEYWORD2);
                        lastOffset = newLength;

                        break loop;
                    }

                    break;

                case '/' :
                    backslash = false;

                    if ((token == Token.NULL) && ((newLength - i) > 1))
                    {
                        switch (array[i1])
                        {
                            case '*' :
                                addToken(i - lastOffset, token);
                                lastOffset = i;
                                lastKeyword = lastOffset;

                                if (((newLength - i) > 2) && (array[i + 2] == '*'))
                                {
                                    token = Token.COMMENT2;
                                }
                                else if (((newLength - i) > 2) && (array[i + 2] == '#'))
                                {
                                    token = Token.COMMENT3;
                                }
                                else
                                {
                                    token = Token.COMMENT1;
                                }

                                break;

                            case '/' :
                                addToken(i - lastOffset, token);
                                addToken(newLength - i, Token.COMMENT1);
                                lastOffset = newLength;
                                lastKeyword = lastOffset;

                                break loop;
                        }
                    }

                    break;

                case '"' :

                    if (backslash)
                    {
                        backslash = false;
                    }
                    else if (token == Token.NULL)
                    {
                        token = Token.LITERAL1;
                        addToken(i - lastOffset, Token.NULL);
                        lastOffset = i;
                        lastKeyword = lastOffset;
                    }
                    else if (token == Token.LITERAL1)
                    {
                        token = Token.NULL;
                        addToken(i1 - lastOffset, Token.LITERAL1);
                        lastOffset = i1;
                        lastKeyword = lastOffset;
                    }

                    break;

                case '\'' :

                    if (backslash)
                    {
                        backslash = false;
                    }
                    else if (token == Token.NULL)
                    {
                        token = Token.LITERAL2;
                        addToken(i - lastOffset, Token.NULL);
                        lastOffset = i;
                        lastKeyword = lastOffset;
                    }
                    else if (token == Token.LITERAL2)
                    {
                        token = Token.NULL;
                        addToken(i1 - lastOffset, Token.LITERAL1);
                        lastOffset = i1;
                        lastKeyword = lastOffset;
                    }

                    break;

                case ':' :

                    if ((token == Token.NULL) && (lastKeyword == offset))
                    {
                        backslash = false;
                        addToken(i1 - lastOffset, Token.LABEL);
                        lastOffset = i1;
                        lastKeyword = lastOffset;

                        break;
                    }

                default :
                    backslash = false;

                    if ((token == Token.NULL) && (c != '_') && !(Character.isLetter(c) || c == '@'))
                    {
                        int len = i - lastKeyword;
                        byte id = keywords.lookup(line, lastKeyword, len);

                        if (id != Token.NULL)
                        {
                            if (lastKeyword != lastOffset)
                            {
                                addToken(lastKeyword - lastOffset, Token.NULL);
                            }

                            addToken(len, id);
                            lastOffset = i;
                        }

                        lastKeyword = i1;
                    }

                    break;
            }
        }

        if (token == Token.NULL)
        {
            int len = newLength - lastKeyword;
            byte id = keywords.lookup(line, lastKeyword, len);

            if (id != Token.NULL)
            {
                if (lastKeyword != lastOffset)
                {
                    addToken(lastKeyword - lastOffset, Token.NULL);
                }

                addToken(len, id);
                lastOffset = newLength;
            }
        }

        if (lastOffset != newLength)
        {
            if ((token == Token.LITERAL1) || (token == Token.LITERAL2))
            {
                addToken(newLength - lastOffset, Token.INVALID);
                token = Token.NULL;
            }
            else
            {
                addToken(newLength - lastOffset, token);
            }
        }

        if ((token == Token.KEYWORD2) && !backslash)
        {
            token = Token.NULL;
        }

        return token;
    }
}
