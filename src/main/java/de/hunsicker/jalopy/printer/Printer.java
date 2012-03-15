/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import antlr.collections.AST;


/**
 * Common Interface for printing nodes.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public interface Printer
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Outputs the given node.
     *
     * @param node node to output.
     * @param out output stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    public void print(
        AST        node,
        NodeWriter out)
      throws IOException;
}
