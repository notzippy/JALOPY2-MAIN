/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language.antlr;

import antlr.CommonHiddenStreamToken;


/**
 * An extended token. Stores information about the token's extent.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public abstract class ExtendedToken
    extends CommonHiddenStreamToken
{
    //~ Instance variables ---------------------------------------------------------------

    /** Ending column. */
    int endColumn;

    /** Ending line. */
    int endLine;

    /** The associated Javadoc comment. */
    Node comment;

    /** Token text. */
    String text;

    public int nlAfter = 0;
    /** True if attached to a node */
    public boolean attached = false;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ExtentedToken object.
     */
    public ExtendedToken()
    {
        this.endLine = 0;
        this.endColumn = 0;
    }


    /**
     * Creates a new ExtentedToken object.
     *
     * @param type type of the token.
     * @param text text of the token.
     */
    public ExtendedToken(
        int    type,
        String text)
    {
        this();
        this.text = text;
        setType(type);
    }


    /**
     * Creates a new ExtendedToken object.
     *
     * @param type
     * @param text
     * @param startLine
     * @param startColumn
     * @param endLine
     * @param endColumn
     */
    public ExtendedToken(
        int    type,
        String text,
        int    startLine,
        int    startColumn,
        int    endLine,
        int    endColumn)
    {
        this.text = text;
        this.line = startLine;
        this.col = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        setType(type);
    }


    /**
     * Creates a new ExtendedToken object.
     *
     * @param type
     * @param startLine
     * @param startColumn
     * @param endLine
     * @param endColumn
     */
    public ExtendedToken(
        int type,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn)
    {
        this.line = startLine;
        this.col = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        setType(type);
    }


    /**
     * Creates a new ExtentedToken object.
     *
     * @param text the text of the token
     */
    public ExtendedToken(String text)
    {
        this();
        this.text = text;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the starting column of the token.
     *
     * @return token starting column.
     */
    public int getColumn()
    {
        return this.col;
    }


    /**
     * Sets the comment for this token.
     *
     * @param comment the comment to attach to this token.
     */
    public void setComment(Node comment)
    {
        this.comment = comment;
    }


    /**
     * Returns the comment that is attached to this token.
     *
     * @return The attached comment of this token. Returns <code>null</code> if no
     *         comment is attached.
     */
    public Node getComment()
    {
        return this.comment;
    }


    /**
     * Returns the token's start column
     *
     * @return the column where the token ends.
     */
    public int getEndColumn()
    {
        return this.endColumn;
    }


    /**
     * Returns the token's end line.
     *
     * @return the line where the token ends.
     */
    public int getEndLine()
    {
        return this.endLine;
    }


    /**
     * Returns the starting line of the token.
     *
     * @return token starting line.
     */
    public int getLine()
    {
        return this.line;
    }


    /**
     * Sets the text of the token.
     *
     * @param text text of the token.
     */
    public void setText(String text)
    {
        this.text = text;
    }


    /**
     * Returns the text of the token.
     *
     * @return token text.
     */
    public String getText()
    {
        return this.text;
    }


    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer(30);
        buf.append('[');
        buf.append('"');
        buf.append(this.text);
        buf.append('"');
        buf.append(',');
        buf.append('<');
        buf.append(getType());
        buf.append('>');
        buf.append(' ');
        buf.append('[');
        buf.append(this.line);
        buf.append(':');
        buf.append(this.col);
        buf.append('-');
        buf.append(this.endLine);
        buf.append(':');
        buf.append(this.endColumn);
        buf.append(']');
        buf.append(']');

        return buf.toString();
    }
    public void setHiddenAfter(CommonHiddenStreamToken t) {
        hiddenAfter = t;
    }

    public void setHiddenBefore(CommonHiddenStreamToken t) {
        hiddenBefore = t;
    }
    public void clear() {
        this.endColumn = -1;
        this.endLine = -1;
        this.comment=null;
        this.text = null;
        this.attached = false;
        this.hiddenBefore = null;
        this.hiddenAfter = null;
        this.nlAfter = 0;
        this.line = -1;
        this.col = -1;
    }
    
}
