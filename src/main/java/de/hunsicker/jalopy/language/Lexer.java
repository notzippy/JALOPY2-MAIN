/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import antlr.TokenStream;

import de.hunsicker.io.FileFormat;

import java.io.Reader;

/**
 * Common interface for ANTLR lexers.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.3 $
 */
public interface Lexer extends TokenStream
{
   /**
    * Sets the start column of the position where parsing starts.
    *
    * @param column start column.
    */
   public void setColumn(int column);

   /**
    * Returns the current column.
    *
    * @return current column offset.
    */
   public int getColumn();

   /**
    * Returns the file format of the input stream.
    *
    * @return The detected file format of the input stream.
    */
   public FileFormat getFileFormat();

   /**
    * Sets the filename we parse.
    *
    * @param file filename to parse.
    */
   public void setFilename(String file);

   /**
    * Returns the name of the file.
    *
    * @return filename.
    */
   public String getFilename();

   /**
    * Sets the input source to use.
    *
    * @param in input source to use.
    */
   public void setInputBuffer(Reader in);

   /**
    * Sets the line number of the position where parsing starts.
    *
    * @param line line number.
    */
   public void setLine(int line);

   /**
    * Returns the current line.
    *
    * @return current line number.
    */
   public int getLine();

   /**
    * Returns the corresponding parser for this lexer.
    *
    * @return corresponding parser.
    */
   public Parser getParser();

   /**
    * Resets the lexer state.
    */
   public void reset();
}