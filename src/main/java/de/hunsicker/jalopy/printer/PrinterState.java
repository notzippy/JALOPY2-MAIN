/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Holds some state values during the printing process (mostly used to implement line
 * wrapping and aligning).
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class PrinterState
{
    //~ Instance variables ---------------------------------------------------------------

    LinkedList parenScope;

    /**
     * Holds the right parentheses of parentheses pairs that should be wrapped and
     * indented.
     */
    List parentheses;

    /** An array we use to hold the arguments for String formatting. */
    final Object[] args = new Object[6];

    /** Our markers. */
    Markers markers;

    /** Indicates that the printer currently prints an anonymous inner class. */
    boolean anonymousInnerClass;

    /**
     * Indicates that we're currently printing the expressions of an expression list (for
     * <code>if</code>, <code>while</code>, <code>do-while</code> blocks).
     */
    boolean expressionList;

    /**
     * Indicates wether a newline was printed before the last <code>extends</code>
     * keyword.
     */
    boolean extendsWrappedBefore;

    /** Indicates that the printer currently prints an inner class. */
    boolean innerClass;

    /**
     * Indicates whether we should print the left curly brace of method/ctor declarations
     * in C-style.
     */
    boolean newlineBeforeLeftBrace;

    /**
     * Indicates that we're currently printing the parameters of a parameter list (for
     * method calls or creators).
     */
    boolean paramList;

    /**
     * Indicates whether the parameter list of a method or ctor declaration was wrapped.
     */
    boolean parametersWrapped;

    /**
     * Indicates whether operators should be wrapped as needed, or wrapping should be
     * forced after each operator.
     */
    boolean wrap;

    /** Holds the current level of array brackets. */
    int arrayBrackets;

    /** Holds the column offset of the rightmost assignment for assignment aligning. */
    int assignOffset = AssignmentPrinter.OFFSET_NONE;

    /** Stores the nesting level for parameter/expression lists. */
    int paramLevel;

    /** Holds the column offset of the rightmost identifier for parameter aligning. */
    int paramOffset = ParametersPrinter.OFFSET_NONE;

    /**
     * Holds the column offset of the rightmost variable definition for variable
     * aligning.
     */
    int variableOffset = VariableDeclarationPrinter.OFFSET_NONE;
    boolean smallIndent = false;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new PrinterState object.
     *
     * @param writer the NodeWriter to associate.
     */
    PrinterState(NodeWriter writer)
    {
        if (writer.mode == NodeWriter.MODE_DEFAULT)
        {
            this.markers = new Markers(writer);
            this.parenScope = new LinkedList();
            this.parentheses = new ArrayList(5);
            this.parenScope.addFirst(new ParenthesesScope(0));
        }
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void dispose()
    {
        if (this.parenScope != null)
        {
            this.parenScope.clear();
            this.markers = null;
        }
    }
    
    public void reset() {
        if (this.parenScope!=null){
            //while(this.parenScope.size()>1)
            //    this.parenScope.removeLast();
            this.parenScope.clear();
            this.parenScope.addFirst(new ParenthesesScope(0));
        }
        if (this.parentheses!=null){
            this.parentheses.clear();
        }
        if (this.markers!=null){
            this.markers.reset();
        }
        this.wrap=false;
        this.anonymousInnerClass=false;
        this.expressionList = false;
        this.extendsWrappedBefore=false;
        this.innerClass=false;
        this.newlineBeforeLeftBrace=false;
        this.paramList=false;
        this.parametersWrapped=false;
        this.wrap=false;
        this.arrayBrackets=0;
        this.assignOffset = AssignmentPrinter.OFFSET_NONE;
        this.paramLevel = 0;
        this.paramOffset = ParametersPrinter.OFFSET_NONE;
        this.variableOffset = VariableDeclarationPrinter.OFFSET_NONE;        
        smallIndent=false;

    }
    public void reset(PrinterState state) {
        this.anonymousInnerClass = state.anonymousInnerClass;
        this.innerClass = state.innerClass;
        this.newlineBeforeLeftBrace = state.newlineBeforeLeftBrace;
        this.parametersWrapped=state.parametersWrapped;
        this.wrap=state.wrap;
        smallIndent=false;
    }
}
