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
 * This class prints out an annotation member value pair
 * The format may be similar to <br>
 * <pre>@Trademark(<b>description = "abcd"</b>...;</pre> 
 * @author <a href="http://jalopy.sf.net/contact.html">NotZippy</a>
 * @version $ $
 *
 * @since 1.5
 */
public class AnnotationMemberValuePairPrinter extends AbstractPrinter {
    /** Singleton. */
    private static final Printer INSTANCE = new AnnotationMemberValuePairPrinter();

    /**
     * Single instance model
     */
    protected AnnotationMemberValuePairPrinter() {
        super();
        // TODO Auto-generated constructor stub
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
     * Prints the member value pairs 
     *
     * @param node
     * @param out
     * @throws IOException
     */
    public void print(AST node, NodeWriter out) throws IOException {
        // Add comments
        printCommentsBefore(node,out);
        
        // Fetch and print the name
        AST child = node.getFirstChild();
        PrinterFactory.create(child, out).print(child,out);
        
        // Add the assign
        out.print(ASSIGN_PADDED,node.getType());
        
        // Fetch and print the value
        PrinterFactory.create(child.getNextSibling(), out).print(child.getNextSibling(),out);
        
        // Print the rest
        printCommentsAfter(node,out);
    }
}
