/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
/*
 * SyntaxDocument.java - Interface all colorized documents must implement
 * Copyright (C) 1999 Slava Pestov
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 */
package de.hunsicker.jalopy.swing.syntax;

import java.awt.Color;

import javax.swing.text.Document;


/**
 * The interface a document must implement to be colorizable by the
 * <code>SyntaxEditorKit</code>. It defines two methods, one that returns the
 * <code>TokenMarker</code> that will split a line into a list of tokens, and a method
 * that returns a color array that maps identification tags returned by the token marker
 * into <code>Color</code> objects. The possible token identifiers are defined as static
 * fields in the <code>Token</code> class.
 * 
 * <p></p>
 *
 * @author Slava Pestov
 * @version $Id: SyntaxDocument.java,v 1.2 2002/11/11 22:09:53 marcohu Exp $
 */
public interface SyntaxDocument
    extends Document
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the color array that maps token identifiers to <code>java.awt.Color</code>
     * ojects. May throw an exception if this is not supported for this type of
     * document.
     *
     * @param colors The new color list
     */
    public void setColors(Color[] colors);


    /**
     * Returns the color array that maps token identifiers to <code>java.awt.Color</code>
     * objects. Each index in the array is a token type.
     *
     * @return DOCUMENT ME!
     */
    public Color[] getColors();


    /**
     * Sets the token marker that is to be used to split lines of this document up into
     * tokens. May throw an exception if this is not supported for this type of
     * document.
     *
     * @param tm The new token marker
     */
    public void setTokenMarker(TokenMarker tm);


    /**
     * Returns the token marker that is to be used to split lines of this document up
     * into tokens. May return null if this document is not to be colorized.
     *
     * @return DOCUMENT ME!
     */
    public TokenMarker getTokenMarker();


    /**
     * Reparses the document, by passing all lines to the token marker. This should be
     * called after the document is first loaded.
     */
    public void tokenizeLines();


    /**
     * Reparses the document, by passing the specified lines to the token marker. This
     * should be called after a large quantity of text is first inserted.
     *
     * @param start The first line to parse
     * @param len The number of lines, after the first one to parse
     */
    public void tokenizeLines(
        int start,
        int len);
}
