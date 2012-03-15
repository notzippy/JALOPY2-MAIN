/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import antlr.collections.AST;


/**
 * Command interface for applying tree transformations.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public interface Transformation
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Performs a transformation of the given tree.
     *
     * @param tree the root node of a parse tree to apply a transformation to.
     *
     * @throws TransformationException if the transformation failed.
     */
    public void apply(AST tree)
      throws TransformationException;
}
