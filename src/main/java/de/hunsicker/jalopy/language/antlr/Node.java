/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language.antlr;

import antlr.CommonASTWithHiddenTokens;
import antlr.CommonHiddenStreamToken;
import antlr.Token;
import antlr.collections.AST;


//J-
import java.lang.ClassCastException;

//J+

/**
 * A node which stores information about its span.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public abstract class Node
    extends CommonASTWithHiddenTokens
    implements Comparable
{
    //~ Instance variables ---------------------------------------------------------------

    /** Node text. */
    protected String text;
    public int nlAfter = 0;

    /** Column number where this node ends. */
    protected int endColumn;

    /** Line number where this node ends. */
    protected int endLine;

    /** Column number where this node starts. */
    protected int startColumn;

    /** Line number where this node starts. */
    protected int startLine;

    /** Node type. */
    protected int type;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Node object.
     */
    public Node()
    {
    }


    /**
     * Creates a new Node object.
     *
     * @param type node type
     * @param text node text.
     * @param startLine line number where this node starts.
     * @param startColumn column number where this node starts.
     * @param endLine line number where this node ends.
     * @param endColumn column number where this node ends.
     */
    public Node(
        int    type,
        String text,
        int    startLine,
        int    startColumn,
        int    endLine,
        int    endColumn)
    {
        this.type = type;
        this.text = text;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }


    /**
     * Creates a new Node object.
     *
     * @param startLine line number where this node starts.
     * @param startColumn column number where this node starts.
     * @param endLine line number where this node ends.
     * @param endColumn column number where this node ends.
     */
    public Node(
        int startLine,
        int startColumn,
        int endLine,
        int endColumn)
    {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }


    /**
     * Creates a new Node object.
     *
     * @param type node type
     * @param text node text.
     */
    public Node(
        int    type,
        String text)
    {
        this.type = type;
        this.text = text;
    }


    /**
     * Creates a new Node object from the given token.
     *
     * @param tok token to initialize this node with.
     */
    public Node(Token tok)
    {
        initialize(tok);
    }


    /**
     * Creates a new Node object.
     *
     * @param text text to add.
     */
    public Node(String text)
    {
        this.text = text;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the end column value.
     *
     * @param column new end column value.
     */
    public void setEndColumn(int column)
    {
        this.endColumn = column;
    }


    /**
     * Returns the column number where this node ends.
     *
     * @return ending column number of this node.
     */
    public int getEndColumn()
    {
        return this.endColumn;
    }


    /**
     * Sets the end line value.
     *
     * @param line new end line value.
     */
    public void setEndLine(int line)
    {
        this.endLine = line;
    }


    /**
     * Returns the line number where this node ends.
     *
     * @return ending line number of this node.
     */
    public int getEndLine()
    {
        return this.endLine;
    }


    /**
     * Sets the first hidden token that appears after this node.
     *
     * @param token a hidden token.
     */
    public void setHiddenAfter(CommonHiddenStreamToken token)
    {
        this.hiddenAfter = token;
    }


    /**
     * Sets the first hidden token that appears before this node.
     *
     * @param token a hidden token.
     */
    public void setHiddenBefore(CommonHiddenStreamToken token)
    {
        this.hiddenBefore = token;
    }


    /**
     * Indicates whether this node has its location information set.
     *
     * @return <code>true</code> if this node has its location information set.
     */
    public boolean isPositionKnown()
    {
        return (this.startLine + this.endLine) > 0;
    }


    /**
     * Sets the column where this node starts.
     *
     * @param column the column where this node starts.
     */
    public void setStartColumn(int column)
    {
        this.startColumn = column;
    }


    /**
     * Returns the column number where this node starts.
     *
     * @return starting column number of this node.
     */
    public int getStartColumn()
    {
        return this.startColumn;
    }


    /**
     * Sets the line where this node starts.
     *
     * @param line the line where this node starts.
     */
    public void setStartLine(int line)
    {
        this.startLine = line;
    }


    /**
     * Returns the line number where this node starts.
     *
     * @return starting line number of this node.
     */
    public int getStartLine()
    {
        return this.startLine;
    }


    /**
     * Sets the text of this node.
     *
     * @param text text to set.
     */
    public void setText(String text)
    {
        this.text = text;
    }


    /**
     * Get the token text for this node
     *
     * @return the text of this node.
     */
    public String getText()
    {
        return this.text;
    }


    /**
     * Sets the type of this node.
     *
     * @param type type to set.
     */
    public void setType(int type)
    {
        this.type = type;
    }


    /**
     * Get the token type for this node
     *
     * @return the type of this node.
     */
    public int getType()
    {
        return this.type;
    }


    /**
     * Compares this object with the specified object for order. Returns a negative
     * integer, zero, or a positive integer as this object is less than, equal to, or
     * greater than the specified object.
     *
     * @param o the Object to be compared
     *
     * @return a negative integer, zero, or a positive integer as this node's extent
     *         starts before, with or after the extent of the specified node.
     *
     * @throws NullPointerException if <code>object == null</code>
     * @throws ClassCastException if the specified object's type prevents it from being
     *         compared to this object.
     */
    public int compareTo(Object o)
    {
        if (o == this)
        {
            return 0;
        }

        if (o == null)
        {
            throw new NullPointerException("o == null");
        }

        if (!(o instanceof Node))
        {
            throw new ClassCastException(
                o.getClass() + " not of type de.hunsicker.jalopy.language.Node");
        }

        Node other = (Node) o;

        if (startsBefore(other))
        {
            return -1;
        }
        else if (endsAfter(other))
        {
            return 1;
        }

        return 0;
    }


    /**
     * Checks whether this node contains the given node.
     *
     * @param node to check.
     *
     * @return <code>true</code> if the given node is contained in this node.
     */
    public boolean contains(Node node)
    {
        return contains(node.startLine, node.startColumn)
        && contains(node.endLine, node.endColumn);
    }


    /**
     * Checks whether this node contains the given position.
     *
     * @param aStartLine start line to check.
     * @param aStartColumn start column to check.
     * @param aEndLine end line to check.
     * @param aEndColumn end column to check.
     *
     * @return <code>true</code> if the given position is contained in this span.
     */
    public boolean contains(
        int aStartLine,
        int aStartColumn,
        int aEndLine,
        int aEndColumn)
    {
        return contains(aStartLine, aStartColumn) && contains(aEndLine, aEndColumn);
    }


    /**
     * Checks whether this node contains the given position.
     *
     * @param line line to check.
     * @param column column to check.
     *
     * @return <code>true</code> if the given position is contained in this span.
     */
    public boolean contains(
        int line,
        int column)
    {
        boolean afterStart = false;
        boolean beforeEnd = false;

        if (this.startLine < line)
        {
            afterStart = true;
        }
        else if ((this.startLine == line) && (this.startColumn <= column))
        {
            afterStart = true;
        }

        if (this.endLine > line)
        {
            beforeEnd = true;
        }
        else if ((this.endLine == line) && (this.endColumn >= column))
        {
            beforeEnd = true;
        }

        return (afterStart && beforeEnd);
    }


    /**
     * Determines whether this node starts after another one.
     *
     * @param other node to check for.
     *
     * @return <code>true</code> if this node ends after the other node.
     */
    public boolean endsAfter(Node other)
    {
        boolean result = false;

        if (this.endLine > other.endLine)
        {
            result = true;
        }
        else if ((this.endLine == other.endLine) && (this.endColumn >= other.endColumn))
        {
            result = true;
        }

        return result;
    }
/*
    public int hashCode()
    {
       // TODO This really buggers up the tree view !!  
        return this.text.hashCode() + this.type;
    }
    */

    /**
     * Compares the specified object with this object for equality. Returns
     * <code>true</code> if and only if the specified object is also an Node and both
     * nodes have the same text and type. So it does actually the same job as {@link
     * de.hunsicker.antlr.BaseAST#equals(AST)}.
     *
     * @param o the object to be compared for equality with this node.
     *
     * @return <code>true</code> if the specified object is equal to this object.
     */
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        /*

        if (o instanceof Node)
        {
            Node node = (Node) o;
            if (this.text==null || node.text == null) {
                return false;
            }
            
            return this.text.equals(node.text) && (this.type == node.type) && 
            this.startColumn == node.startColumn && this.endColumn == node.endColumn 
            && this.startLine == node.startLine  && this.endLine = node.endLine;
        }
         */
        
        return false;
    }


    /**
     * Initializes this node with information from the given node.
     *
     * @param node node to setup this node with.
     */
    public void initialize(AST node)
    {
        Node n = (Node) node;
        this.text = n.text;
        this.type = n.getType();
        this.startLine = n.startLine;
        this.endLine = n.endLine;
        this.startColumn = n.startColumn;
        this.endColumn = n.endColumn;
        this.hiddenBefore = n.getHiddenBefore();
        this.hiddenAfter = n.getHiddenAfter();
    }


    /**
     * Initializes this node with the given information.
     *
     * @param newType type to set.
     * @param newText text to set.
     */
    public void initialize(
        int    newType,
        String newText)
    {
        this.type = newType;
        this.text = newText;
    }


    /**
     * Initializes this node with information from the given token.
     *
     * @param tok token to setup this node with.
     */
    public void initialize(Token tok)
    {
        ExtendedToken token = (ExtendedToken) tok;

        this.text = token.getText();
        this.type = token.getType();
        this.startLine = token.getLine();
        this.endLine = token.endLine;
        this.startColumn = token.getColumn();
        this.endColumn = token.endColumn;
        this.hiddenBefore = token.getHiddenBefore();
        this.hiddenAfter = token.getHiddenAfter();
        this.nlAfter = token.nlAfter;
    }


    /**
     * Determines whether this node starts before another one.
     *
     * @param other node to check for.
     *
     * @return <code>true</code> if this node starts before the other one.
     */
    public boolean startsBefore(Node other)
    {
        boolean result = false;

        if (this.startLine < other.startLine)
        {
            result = true;
        }
        else if (
            (this.startLine == other.startLine)
            && (this.startColumn <= other.startColumn))
        {
            result = true;
        }

        return result;
    }


    /**
     * Returns a string representation of this node.
     *
     * @return a string representation of this node.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer(50);
        buf.append(this.text);
        buf.append(' ');
        buf.append(this.startLine);
        buf.append(':');
        buf.append(this.startColumn);
        buf.append(' ');
        buf.append(this.endLine);
        buf.append(':');
        buf.append(this.endColumn);

        return buf.toString();
    }
/**
 * Method to prevent references from sticking around
 * 
 */
    public void clear() {
        this.down = null;
        this.right = null;
        this.hiddenAfter = null;
        this.hiddenBefore = null;     
        this.text = null;
        this.startColumn = -1;
        this.endColumn = -1;
        this.startLine = -1;
        this.endLine = -1;
        this.type = -1;
        this.nlAfter = 0;
        
    }
}
