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
 * This class prints out an annotation group
 * The format may be similar to <br>
 * <pre>
 *  public @interface License
 *   {
 *      String name();
 *
 *      String notice(); 
 *
 *      boolean redistributable();
 *
 *     Trademark[] trademarks();
 *
 *  }
 *  </pre>
 *  
 * @author <a href="http://jalopy.sf.net/contact.html">NotZippy</a>
 * @version $ $
 *
 * @since 1.5
 */
public class AnnotationDefPrinter extends AbstractPrinter {
    /** Singleton. */
    private static final Printer INSTANCE = new AnnotationDefPrinter();

    /**
     * Single instance
     */
    protected AnnotationDefPrinter() {
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
     * Adds a marker 
     *
     * @param node
     * @param out
     * @throws IOException
     */
    public void print(AST node, NodeWriter out) throws IOException {
//        out.indent();
        
        Marker marker = out.state.markers.add(out.line, out.indentSize );
        out.state.markers.add(
            out.line,
            out.state.markers.getLast().column + out.indentSize);
        // TODO Complete annotations implementation
        out.state.innerClass = (out.getIndentLength() > 0);

        // TODO addCommentIfNeeded(node, out);

        printCommentsBefore(node,true, out);

        // print the modifiers
        AST modifiers = node.getFirstChild();
        PrinterFactory.create(modifiers, out).print(modifiers, out);
        
        out.print(AT_INTERFACE_SPACE, JavaTokenTypes.AT);
        
        // Print out the annotation field def(s)
        
        for(AST child = modifiers.getNextSibling();child!=null;child = child.getNextSibling()) {
            if (child.getType() == JavaTokenTypes.OBJBLOCK) {
                out.state.extendsWrappedBefore = false;
                out.last = JavaTokenTypes.AT;                
            }
            PrinterFactory.create(child, out).print(child, out);
        }
        
        out.state.innerClass = false;
        out.last = JavaTokenTypes.AT;
        out.state.markers.remove(marker);
//        out.unindent();

    }

}
