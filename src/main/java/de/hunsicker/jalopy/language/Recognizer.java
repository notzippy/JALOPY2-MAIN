/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import antlr.RecognitionException;
import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamRecognitionException;
import antlr.collections.AST;
import de.hunsicker.io.FileFormat;
import de.hunsicker.util.ChainingRuntimeException;


/**
 * Recognizer acts as a helper class to bundle both an ANTLR parser and lexer for the
 * task of language recognition.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 */
public class Recognizer
{
    //~ Static variables/initializers ----------------------------------------------------

    /**
     * Represents an unknown filename that may be used for the <em>filename</em> argument
     * with {@link #parse(Reader, String)} or {@link #parse(String, String)}.
     */
    public static final String UNKNOWN_FILE = "<unknown>" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    /** The used lexer. */
    protected Lexer lexer;

    /** The used parser. */
    protected Parser parser;

    /** Indicates that formatting finished. */
    boolean finished;

    /** Indicates that formatting currently takes place. */
    boolean running;

    private int _startColumn;

    private int _startLine;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Recognizer object.
     *
     * @param parser the parser to use.
     * @param lexer the lexer to use.
     */
    public Recognizer(
        Parser parser,
        Lexer  lexer)
    {
        this.parser = parser;
        this.lexer = lexer;
    }


    /**
     * Creates a new Recognizer object.
     */
    protected Recognizer()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the current column of the lexer.
     *
     * @param column current column information.
     */
    public void setColumn(int column)
    {
        this.lexer.setColumn(column);
    }


    /**
     * Returns the current column of the lexer.
     *
     * @return current column offset.
     */
    public int getColumn()
    {
        return this.lexer.getColumn();
    }


    /**
     * Gets the file format of the parsed file as reported by the lexer
     *
     * @return The file format.
     *
     * @throws IllegalStateException if nothing has been parsed yet.
     */
    public FileFormat getFileFormat()
    {
        if (!this.finished)
        {
            throw new IllegalStateException("nothing parsed yet");
        }

        return this.lexer.getFileFormat();
    }


    /**
     * Indicates whether the recognizer is currently running.
     *
     * @return <code>true</code> if the recognizer is currently running.
     */
    public boolean isFinished()
    {
        return this.finished;
    }


    /**
     * Returns the used lexer.
     *
     * @return lexer.
     */
    public Lexer getLexer()
    {
        return this.lexer;
    }


    /**
     * Sets the current line of the lexer.
     *
     * @param line current line information.
     */
    public void setLine(int line)
    {
        this.lexer.setLine(line);
    }


    /**
     * Returns the current line of the lexer.
     *
     * @return current line number of the lexer
     */
    public int getLine()
    {
        return this.lexer.getLine();
    }


    /**
     * Returns the root node of the generated parse tree.
     *
     * @return root node of the generated parse tree.
     */
    public AST getParseTree()
    {
        return this.parser.getParseTree();
    }


    /**
     * Returns the used parser.
     *
     * @return parser.
     */
    public Parser getParser()
    {
        return this.parser;
    }


    /**
     * Indicates whether the recognizer is currently running.
     *
     * @return <code>true</code> if the recognizer is currently running.
     */
    public boolean isRunning()
    {
        return this.running;
    }


    /**
     * Parses the given stream.
     *
     * @param in stream we read from.
     * @param filename name of the file we parse.
     *
     * @throws IllegalStateException if the parser is currently running.
     * @throws ParseException if an unexpected error occured.
     */
    public void parse(
        Reader in,
        String filename)
    {
        if (this.running)
        {
            throw new IllegalStateException("parser already running");
        }

        this.finished = false;
        this.running = true;
        _startLine = this.lexer.getLine();
        _startColumn = this.lexer.getColumn();
        this.lexer.setInputBuffer(in);
        this.lexer.setFilename(filename);
        this.parser.setTokenBuffer(new TokenBuffer(this.lexer));
        this.parser.setFilename(filename);

        try
        {
            this.parser.parse();
        }

        // the parser/lexer should never throw any checked exception as we
        // intercept them and print logging messages prior to attempt
        // further parsing; so simply wrap all checked exceptions for the
        // case one changes the error handling in the grammar...
        catch (RecognitionException ex)
        {
            throw new ParseException(ex);
        }
        catch (TokenStreamRecognitionException ex)
        {
            throw new ParseException(ex);
        }
        catch (TokenStreamException ex)
        {
            throw new ParseException(ex);
        }
        finally
        {
            this.finished = true;
            this.running = false;
        }
    }


    /**
     * Parses the given file.
     *
     * @param file file to parse.
     */
    public void parse(File file)
    {
        if (file.exists() && file.isFile())
        {
            BufferedReader in = null;

            try
            {
                in = new BufferedReader(new FileReader(file));
                parse(in, file.getAbsolutePath());
            }
            catch (FileNotFoundException neverOccurs)
            {
                ;
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException ignored)
                    {
                        ;
                    }
                }
            }
        }
    }


    /**
     * Parses the given string.
     *
     * @param str to parse.
     * @param filename name of the file we parse.
     *
     * @throws IOException if an I/O error occured.
     */
    public void parse(
        String str,
        String filename)
      throws IOException
    {
        BufferedReader in = new BufferedReader(new StringReader(str));
        parse(in, filename);
        in.close();
    }


    /**
     * Resets both the parser and lexer.
     *
     * @see Parser#reset
     * @see Lexer#reset
     */
    public void reset()
    {
        this.running = false;
        this.finished = false;
        this.lexer.reset();
        this.parser.reset();
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Indicates an unexpected error during the parsing of an input file or stream.
     */
    public static final class ParseException
        extends ChainingRuntimeException
    {
        /**
         * Creates a new ParseException.
         *
         * @param cause throwable which caused the error.
         */
        public ParseException(Throwable cause)
        {
            super(cause);
        }
    }

    
    public int getStartColumn() {
    
        return _startColumn;
    }



    
    public int getStartLine() {
    
        return _startLine;
    }


    
    public void set_startLine(int line) {
    
        _startLine = line;
    
    }
}
