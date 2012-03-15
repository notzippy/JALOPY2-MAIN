/*
 * Copyright (c) 2005-2006, NotZippy. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import antlr.collections.AST;


/**
 * An annotation line is usually ing the format of <p>
 * <pre>@Target(ANNOTATION_TYPE)
 * @Retention(RUNTIME)</pre></p>
 *
 *
 * @author <a href="http://jalopy.sf.net/contact.html">NotZippy</a>
 * @version $ $
 *
 * @since 1.5
 */
public class AnnotatePrinter extends AbstractPrinter {
    /** Singleton. */
    private static final Printer INSTANCE = new AnnotatePrinter();

    /**
     * Single instance usage only
     */
    protected AnnotatePrinter() {
        super();
    }
    
    /**
     * Returns the sole instance of this class.
     *
     * @return the sole instance of this class.
     */
    public static final Printer getInstance()
    {
        return INSTANCE;
    }

    /** 
     * Prints the annotation as defined in the modifier
     *
     * @param node
     * @param out
     * @throws IOException
     */
    public void print(AST node, NodeWriter out) throws IOException {
        // Print the comments
        printCommentsBefore(node,false,out);
        
        // Add a marker
        Marker marker = out.state.markers.add(out.line, out.indentSize * out.indentLevel);
        out.state.markers.add(
            out.line,
            out.state.markers.getLast().column + out.indentSize);
        
        printCommentsBefore(node,true, out);
        // Print @ symbol
//        out.print(AT,node.getType());
        
        // Print All sub elements
        printChildren(node,out);
        
        // Add a new line 
        if (!printCommentsAfter(node,false,true,out)) {
            out.printNewline();
        }
        
        // Remove the marker
        out.state.markers.remove(marker);
    }
}
