/*
 * Copyright (c) 2005-2006, NotZippy. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;

import antlr.collections.AST;


/**
 * This class prints out an annotation field
 * The field format may be similar to <br>
 * <pre>String outputFormBean() default "";</pre> 
 * @author <a href="http://jalopy.sf.net/contact.html">NotZippy</a>
 * @version $ $
 *
 * @since 1.5
 */
public class AnnotationFieldPrinter extends AbstractPrinter {
    /** Singleton. */
    private static final Printer INSTANCE = new AnnotationFieldPrinter();

    /**
     * Single instance usage only
     */
    protected AnnotationFieldPrinter() {
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
     * Prints the field annotation. 
     *
     * @param node
     * @param out
     * @throws IOException
     */
    public void print(AST node, NodeWriter out) throws IOException {
        
        // TODO Check to see if we should force or add comments
        printCommentsBefore(node,true,out);
        
        for(AST child = node.getFirstChild();child!=null;child=child.getNextSibling()) {
            PrinterFactory.create(child, out).print(child,out);
            // If the last child is a TYPE then add a space after it
            if (child.getType() == JavaTokenTypes.TYPE){
                out.print(SPACE,JavaTokenTypes.TYPE);
            }
            // If the last child was a RPAREN and the next sibling is not null
            // and not a SEMI print the word default
            else if (child.getType() == JavaTokenTypes.RPAREN) {
                if (child.getNextSibling()!=null && child.getNextSibling().getType()!=JavaTokenTypes.SEMI) {
                    out.print(SPACE_DEFAULT_SPACE,JavaTokenTypes.RPAREN);
                }
            }
        }
        
        printCommentsAfter(
                node, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES, out);
        // Print the comments, if none print a new line
//        if (!printCommentsAfter(node,false,true,out) && !out.newline) {
//            out.printNewline();
//        }
    }

}
