package de.hunsicker.jalopy.language.antlr;


import de.hunsicker.jalopy.language.CompositeFactory;
import de.hunsicker.jalopy.language.Lexer;
import de.hunsicker.jalopy.language.Parser;
import de.hunsicker.jalopy.language.Recognizer;
import de.hunsicker.jalopy.language.antlr.*;

import java.io.Reader;
import java.io.StringReader;
import de.hunsicker.io.FileFormat;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import antlr.CharBuffer;
import antlr.CharStreamException;
import antlr.InputBuffer;
import antlr.LexerSharedInputState;
import antlr.RecognitionException;
import antlr.Token;

/**
 * TODO 
 */
public class JavadocLexer extends InternalJavadocLexer implements Lexer{
    /** Logging. */
    private Logger _logger = Logger.getLogger("de.hunsicker.jalopy.language.javadoc");

    /** Our undefined token constant. */
    final static int UNDEFINED_TOKEN = -10;

    /** Marker to track position (line/column) information. */
    private Position _mark;

    /** Current position in the stream. */
    private Position _position = new Position(1, 1);

    /** Holds the string to use as a replacement for tab characters. */
    private String _tabString;

    /** Corresponding Javadoc parser. */
    private JavadocParser _parser;

    private final CompositeFactory factory;

    static class MyLexerSharedInputState extends LexerSharedInputState  {

        /**
         * @param inbuf
         */
        public MyLexerSharedInputState(InputBuffer inbuf) {
            super(inbuf);
        }
        /**
         * 
         */
        public MyLexerSharedInputState() {
            this(null);
            // TODO Auto-generated constructor stub
        }
        public void setInputBuffer(InputBuffer inbuf) {
            this.reset();
            this.input = inbuf;            
        }
        
    }
    Recognizer recognizer = null;
    /**
     * Creates a new JavadocLexer object. Use {@link #setInputBuffer(Reader)}
     * to set up the input buffer.
     */
    public JavadocLexer(CompositeFactory factory)
    {
        this(new StringReader(""),factory);
        _parser = new JavadocParser(this, factory.getJavaNodeFactory());
        _parser.setASTFactory(factory.getNodeFactory());
        _parser.setLexer(this);
    }
    public JavadocLexer(Reader in,CompositeFactory factory) {
    	this(new CharBuffer(in),factory);
    }
    public JavadocLexer(InputBuffer ib,CompositeFactory factory) {
    	this(new MyLexerSharedInputState(ib),factory);
    }
    public JavadocLexer(LexerSharedInputState state,CompositeFactory factory) {
    	super(state);
        this.factory = factory;
    }

    public Parser getParser()
    {
        return _parser;
    }

    /**
     * Reports a fatal error.
     */
    public void panic()
    {
        if (this.inputState != null)
        {
            Object[] args = { getFilename(), new Integer(getLine()), new Integer(getColumn()), "JavadocLexer: panic" };
            _logger.l7dlog(Level.FATAL, "PARSER_ERROR", args, null);
        }
        else
        {
            if (_logger == null)
                _logger = Logger.getLogger("de.hunsicker.jalopy.language.java");

            Object[] args = { "???", new Integer(0), new Integer(0), "JavaLexer: panic" };
            _logger.l7dlog(Level.FATAL, "PARSER_ERROR", args, null);
        }

    }

   /**
    * Reports a fatal error.
    * @param message the error message.
    */
   public void panic(String message)
   {
        if (this.inputState != null)
        {
            Object[] args = { getFilename(), new Integer(getLine()), new Integer(getColumn()), message };
            _logger.l7dlog(Level.FATAL, "PARSER_ERROR", args, null);
        }
        else
        {
            if (_logger == null)
                _logger = Logger.getLogger("de.hunsicker.jalopy.language.java");

            Object[] args = { "???", new Integer(0), new Integer(0), message };
            _logger.l7dlog(Level.FATAL, "PARSER_ERROR", args, null);
        }
    }
   

   /**
    * Reports the given error.
    *
    * @param ex exception which caused the error.
    */
   public void reportError(RecognitionException ex)
   {
   	ex.printStackTrace();
       Integer line = new Integer((recognizer!=null?recognizer.getStartLine():0) +getLine());
       Integer column = new Integer((recognizer!=null?recognizer.getStartColumn():0) +getColumn());
      Object args[] = { getFilename(), line, column, ex.getMessage() };
      _logger.l7dlog(Level.ERROR, "PARSER_ERROR" , args, ex);
   }

   /**
    * Reports the given error.
    *
    * @param message error message.
    */
   public void reportError(String message)
   {
       Integer line = new Integer((recognizer!=null?recognizer.getStartLine():0) +getLine());
       Integer column = new Integer((recognizer!=null?recognizer.getStartColumn():0) +getColumn());
      Object args[] = { getFilename(), line, column, message };
      _logger.l7dlog(Level.ERROR, "PARSER_ERROR", args, null);
   }

   /**
    * Reports the given warning.
    *
    * @param message warning message.
    */
   public void reportWarning(String message)
   {
      Object args[]  = { getFilename(), new Integer(getLine()), new Integer(getColumn()), message };
      _logger.l7dlog(Level.WARN, "PARSER_ERROR", args, null);
   }

    /**
     * Returns the detected file format.
     *
     * @return The detected file format.
     */
    public FileFormat getFileFormat()
    {
        return _fileFormat;
    }

    /**
     * Sets the class to use for tokens.
     *
     * @param clazz a qualified class name.
     */
    public void setTokenObjectClass(String clazz)
    {
        super.setTokenObjectClass("de.hunsicker.jalopy.language.antlr.ExtendedToken");
    }

    /**
     * Returns the current token position.
     *
     * @return current position.
     */
    private Position getPosition()
    {
        _position.line = getLine();
        _position.column = getColumn();
        return _position;
    }

    /**
     * Sets the current token position.
     *
     * @param pos position to set.
     */
    private void setPosition(Position pos)
    {
        setColumn(pos.column);
        setLine(pos.line);
    }

    /**
     * Holds position information.
     */
    private static class Position
        implements Comparable
    {
        int line;
        int column;

        public Position(int line, int column)
        {
            this.line = line;
            this.column = column;
        }

        public int compareTo(Object o)
        {
            if (o == this)
                return 0;

            Position other = (Position)o;

            if (this.line > other.line)
                return -1;
            else if (this.line < other.line)
            {
                return 1;
            }
            else
            {
                if (this.column > other.column)
                    return -1;
                else if (this.column < other.column)
                    return 1;
            }

            return 0;
        }

        public String toString()
        {
            StringBuffer buf = new StringBuffer(20);
            buf.append("[");
            buf.append("line=");
            buf.append(this.line);
            buf.append(",col=");
            buf.append(this.column);
            buf.append("]");

            return buf.toString();
        }
    }

    /**
     * Sets the input buffer to use.
     * @param buf buffer to read from.
     */
    public void setInputBuffer(InputBuffer buf)
    {
        if (this.inputState != null)
            ((MyLexerSharedInputState)this.inputState).setInputBuffer(buf);
    }

    /**
     * Sets the input buffer to use.
     *
     * @param in reader to read from.
     */
    public void setInputBuffer(Reader in)
    {
        setInputBuffer(new CharBuffer(in));
    }

    /**
     * Resets the lexer. Remember that you have to set up the input buffer
     * before start parsing again.
     *
     * @see #setInputBuffer
     */
    public void reset()
    {
        if (this.inputState != null)
        {
            this.inputState.reset();
        }

        setFilename(Recognizer.UNKNOWN_FILE);
        _tabString = null;
        _fileFormat = FileFormat.UNKNOWN;
        _mark = null;
    }

    /**
     * Creates a token of the given tpye.
     *
     * @param t type of the token.
     */
    protected Token makeToken(int t)
    {

        // if we find a mark, we use this position as the end of the token
        // look at newline() for background information
        if (_mark != null)
        {
            Position cur = getPosition();
            setPosition(_mark);
            
            ExtendedToken newToken = factory.getExtendedTokenFactory().create(t,null);
            newToken.setLine(this.getLine());
            newToken.setColumn(this.getColumn());

            //ExtendedToken tok = new ExtendedToken(t, inputState.tokenStartLine,
            //inputState.tokenStartColumn, inputState.line, inputState.column);
            _mark = null;

            setPosition(cur);

            return newToken;
        }
            ExtendedToken newToken = factory.getExtendedTokenFactory().create(t,null);
            newToken.setLine(this.getLine());
            newToken.setColumn(this.getColumn());
            return newToken;
//                        return new ExtendedToken(t, inputState.tokenStartLine,
//                                     inputState.tokenStartColumn, inputState.line,
//                                     inputState.column);
    }

    /** Amount of spaces used to replace a tab. */
    private int _tabSize = 4;

    /**
     * Returns the current tab size.
     * @return current tab size;
     */
    public int getTabSize()
    {
        return _tabSize;
    }

    /**
     * Sets the tab size to use.
     * @param size tab size to use.
     */
    public void setTabSize(int size)
    {
        _tabSize = size;
    }

    /**
     * Replaces the tab char last read into the text buffer with an
     * equivalent number of spaces. Note that we assume you know what you do,
     * we don't check if indeed the tab char were read!
     *
     * @throws CharStreamException if an I/O error occured.
     */
    protected void replaceTab()
        throws CharStreamException
    {
        if (_tabString == null)
        {
            int tabSize = getTabSize(); // makes for faster array access
            StringBuffer indent = new StringBuffer(tabSize);

            for (int i = 0; i < tabSize; i++)
            {
                indent.append(' ');
            }

            _tabString = indent.toString();
        }

        // remove the tab char from the buffer
        this.text.setLength(text.length() - 1);

        // and insert the spaces
        this.text.append(_tabString);
    }

    /**
     * Replaces the newline chars last read into the text buffer with a
     * single space. Note that we assume you know what you do; we don't check
     * if indeed newline chars were read!
     *
     * @param length length of the newline chars (1 or 2).
     * @throws CharStreamException if an I/O error occured.
     */
    protected void replaceNewline(int length)
            throws CharStreamException
    {

        // remove the newline chars
        this.text.setLength(text.length() - length);
        newline();

        // only add a space if the next character is not already one
        if (LA(1) != ' ')
        {
            if (this.text.length() > 0)
            {
                // only add space if necessary (depending on the last char)
                switch (this.text.charAt(this.text.length() - 1))
                {
                    case ' ':
                    case '-':
                    case '(':
                    case '[':
                    case '{':
                        break;
                    default:
                        this.text.append(' ');
                }
            }
            else
            {
                this.text.append(' ');
            }
        }
    }

   /**
    * Inserts a newline. Skips all leading whitespace until the last space
    * before the first word.
    *
    * @see #makeToken
    */
    public void newline()
    {
        newline(true);
    }

    /**
     * Inserts a newline.
     *
     * @param skipAllLeadingWhitespace if <code>true</code>, all leading
     *        whitespace until the last space before the first word will be
     *        removed; if <code>false</code> only whitespace until and
     *        inclusive a leading asterix will be removed.
     *
     * @see #makeToken
     */
    public void newline(boolean skipAllLeadingWhitespace)
    {
        // because we manually advance the stream position in
        // skipLeadingSpaceAndAsterix() we store the current position in order
        // to allow our tokens to be created with the correct size
        _mark = getPosition();
        super.newline();

        try
        {
            skipLeadingSpaceAndAsterix(skipAllLeadingWhitespace);
        }
        catch (CharStreamException ignored)
        {
            // really an I/O problem, so it will show up on the next rule
        }
    }

    /**
     * Skips leading spaces and asterix.
     *
     * @param skipAllLeadingWhitespace if <code>true</code>, all leading
     *        whitespace until the last space before the first word will be
     *        removed; if <code>false</code> only whitespace until and
     *        inclusive a leading asterix will be removed.
     * @throws CharStreamException if an I/O error occured.
     *
     * @see #newline(boolean)
     */
    protected void skipLeadingSpaceAndAsterix(boolean skipAllLeadingWhitespace)
        throws CharStreamException
    {
        try
        {
            this.saveConsumedInput = false;

            int next = LA(1);

            boolean newline = false;

            while (next != EOF_CHAR)
            {
                switch (next)
                {
                    case '\n':
                    case '\r':
                        consume();

                        if (!newline)
                        {
                            setLine(getLine() + 1);
                            newline = true;
                        }

                        setColumn(1);
                        next = LA(1);
                        break;

                    case '\t':
                    case ' ':

                        // only eat up until the last space
                        switch (LA(2))
                        {
                           case ' ':
                           case '\t':
                               consume();
                           case '*':
                              break;

                           default:
                              return;
                        }

                        consume();
                        next = LA(1);
                        break;

                    case '*':

                        if (skipAllLeadingWhitespace)
                        {
                            // only allow if we don't encounter the closing delim
                            if (LA(2) != '/')
                            {
                                consume();
                                next = LA(1);
                                break;
                            }
                            else if (LA(2) == ' ')
                            {
                                consume();
                                consume();
                                return;
                            }
                            else
                                return;
                        }
                            switch (LA(2))
                            {
                                case '\r':
                                    if (LA(3) == '\n')
                                    {
                                        consume();
                                        return;
                                    }
                                case '\n':
                                case ' ':
                                    consume();
                                    consume();

                                    return;
                            }

                            // only allow if we don't encounter the closing delim
                            if (LA(2) != '/')
                            {
                                consume();
                                next = LA(1);

                                break;
                            }
                            return;
                        

                    default:
                        this.text.append(' ');
                        return;
                }
            }
        }
        finally
        {
            this.saveConsumedInput = true;
        }
    }
    public void setRecognizer(Recognizer recognizer2) {
        this.recognizer = recognizer2;
        
    }

    /**
     * Reads pending characters until whitespace is found (either '\r', '\n',
     * '\t', '\f', ' ').
     *
     * @return string of the characters read.
     */
    /*
    TODO private void consumeUntilWhitespace()
    {
        try
        {
            for (int type = LA(1);; type = LA(1))
            {
                switch (type)
                {
                    case ' ':
                    case '\r':
                    case '\n':
                    case '\t':
                    case EOF_CHAR:
                        return;
                    default:
                        consume();
                }
            }
        }
        catch (CharStreamException ignored)
        {
        }
    }
    */

}
