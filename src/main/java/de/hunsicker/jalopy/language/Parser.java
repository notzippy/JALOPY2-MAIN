/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.collections.AST;


/**
 * Common interface for ANTLR parsers.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public interface Parser
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the factory used to create tree nodes.
     *
     * @param factory factory to use.
     */
    public void setASTFactory(ASTFactory factory);


    /**
     * Sets the factory used to create the nodes of the parse tree.
     *
     * @return the used node factory.
     */
    public ASTFactory getASTFactory();


    /**
     * Sets the filename we parse.
     *
     * @param filename filename to parse.
     */
    public void setFilename(String filename);


    /**
     * Returns the name of the file.
     *
     * @return The currently processed filename.
     */
    public String getFilename();


    /**
     * Returns the root node of the generated parse tree.
     *
     * @return root node of the generated parse tree.
     */
    public AST getParseTree();


    /**
     * Sets the token buffer of the parser.
     *
     * @param buffer buffer to use.
     */
    public void setTokenBuffer(TokenBuffer buffer);


    /**
     * Returns the token names of the parser.
     *
     * @return The token names of the parser.
     */
    public String[] getTokenNames();


    /**
     * Start parsing.
     *
     * @throws RecognitionException if a problem with the input occured.
     * @throws TokenStreamException if something went wrong while generating the stream
     *         of tokens.
     */
    public void parse()
      throws RecognitionException, TokenStreamException;


    /**
     * Resets the parser state.
     */
    public void reset();
}
