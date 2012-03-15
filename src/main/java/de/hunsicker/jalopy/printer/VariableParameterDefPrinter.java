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
 * This class prints out an variable paramater def group
 * The format may be similar to <br>
 * <pre>
 *   public void foo(double... values) 
 * </pre>
 *  
 * @author <a href="http://jalopy.sf.net/contact.html">NotZippy</a>
 * @version $ $
 *
 * @since 1.5
 */
public class VariableParameterDefPrinter extends AbstractPrinter {
    /** Singleton. */
    private static final Printer INSTANCE = new VariableParameterDefPrinter();

    /**
     * Single instance
     */
    protected VariableParameterDefPrinter() {
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
        
        AST type = modifiers.getNextSibling();
        PrinterFactory.create(type, out).print(type, out);
        out.print(DOT,type.getType());
        out.print(DOT,type.getType());
        out.print(DOT,type.getType());
        out.print(SPACE,type.getType());
        
        for(AST child = type.getNextSibling();child!=null;child = child.getNextSibling()) {
            PrinterFactory.create(child, out).print(child, out);
        }
        
        out.state.markers.remove(marker);
    }
}
