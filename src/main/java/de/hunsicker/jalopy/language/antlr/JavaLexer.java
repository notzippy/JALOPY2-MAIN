package de.hunsicker.jalopy.language.antlr;

// $ANTLR 2.7.4: "java15.g" -> "JavaLexer.java"$

import java.io.IOException;
import java.io.StringReader;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.hunsicker.io.FileFormat;
import de.hunsicker.jalopy.language.CompositeFactory;
import de.hunsicker.jalopy.language.JavaParser;
import de.hunsicker.jalopy.language.Lexer;
import de.hunsicker.jalopy.language.Parser;
import de.hunsicker.jalopy.language.Recognizer;
import de.hunsicker.jalopy.language.CompositeFactory.ExtendedTokenFactory;
import de.hunsicker.jalopy.language.antlr.InternalJavaLexer;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.util.StringHelper;
import antlr.InputBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.RecognitionException;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.TokenStreamIOException;

public class JavaLexer extends InternalJavaLexer implements Lexer
 {
    //begin
    /** Indicates JDK version 1.3. */
    public final static int JDK_1_3 = 13;

    /** Indicates JDK version 1.4. */
    public final static int JDK_1_4 = 14;

    private final static String SPACE = " ";

    /** The empty string array. */
    private final static String[] EMPTY_STRING_ARRAY = new String[0];

    private final static String EMPTY_STRING = "";

    /** The detected file format. */
    private FileFormat _fileFormat = FileFormat.UNKNOWN;

    /** The file separator for the file format. */
    private String _lineSeparator = System.getProperty("line.separator");

    /** The Javadoc recognizer. */
    private Recognizer _recognizer;

    /** Logging. */
    private Logger _logger = Logger.getLogger("de.hunsicker.jalopy.language.java");

    /** Should Javadoc comments be parsed or added AS IS? */
    public boolean parseJavadocComments;

    /** Specifies the Java release version to be compatible with. */
    public int sourceVersion = JDK_1_4;

    /** Should Javadoc comments be ignored? */
    public boolean removeJavadocComments;

    /** Should multi-line comments be formatted? */
    public boolean formatMLComments;

    /** Should single-line comments be ignored? */
    public boolean removeSLComments;

    /** Should multi-line comments be ignored? */
    public boolean removeMLComments;

    /** The use Java parser. */
    private JavaParser _parser;

    /** The used Javadoc parser. */
    private JavadocParser _javadocParser;

    private CompositeFactory _factory = null;;
    
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
        /**
         * Configure the tokens start line and column.
         * 
         * @param tok The token to configure
         */
        public void configureToken(ExtendedToken tok) {
            tok.setColumn(tokenStartColumn);
            tok.setLine(tokenStartLine);
        }
    }
//    protected MyLexerSharedInputState inputState;

    /*
    public JavaLexer(InputBuffer cb) { // SAS: use generic buffer
        this();
        inputState = new LexerSharedInputState(cb) ;
    }

    public JavaLexer(LexerSharedInputState sharedState) {
        this();
        inputState = sharedState;
    }
    */
    public JavaLexer(Reader in, CompositeFactory factory) {
    	this(new CharBuffer(in),factory);
    }
    public JavaLexer(InputBuffer ib, CompositeFactory factory) {
    	this(new MyLexerSharedInputState(ib),factory);
    }
    public JavaLexer(LexerSharedInputState state, CompositeFactory factory) {
    	super(state);
        this._factory = factory;
    }
    /**
     * Creates a new JavaLexer object. Use {@link #setInputBuffer(Reader)} to
     * set up the input buffer.
     */
    public JavaLexer(CompositeFactory factory)
    {
        this(new StringReader(""), factory);

        JavadocLexer lexer = new JavadocLexer(factory);
        _javadocParser = (JavadocParser)lexer.getParser();
        _recognizer = new Recognizer(_javadocParser, lexer);
        _javadocParser.setRecognizer(_recognizer);
        factory.setJavadocRecognizer(_recognizer);

        _parser = new JavaParser(this);
        _parser.setASTFactory(factory.getJavaNodeFactory());
    }

    /**
     * Returns the internal parser for Javadoc comments.
     *
     * @return the internal parser for Javadoc comments.
     */
    
    public JavadocParser getJavadocParser()
    {
        return _javadocParser;
    }
    

    /**
     * {@inheritDoc}
     */
    
    public Parser getParser()
    {
        return _parser;
    }

    /**
     * Sets whether multi-line comments should be removed during processing.
     *
     * @param remove if <code>true</code> multi-line comments will be removed during
     *        processing.
     */
    public void setRemoveMLComments(boolean remove)
    {
        this.removeMLComments = remove;
    }

    /**
     * Sets whether multi-line comments should be formatted.
     *
     * @param format if <code>true</code> multi-line comments will be formatted.
     */
    public void setFormatMLComments(boolean format)
    {
        this.formatMLComments = format;
    }

    /**
     * Indicates whether multi-line comments should be formatted.
     *
     * @return <code>true</code> if multi-line comments should be formatted.
     */
    public boolean isFormatMLComments()
    {
        return this.formatMLComments;
    }

    /**
     * Indicates whether multi-line comments should be removed during processing.
     *
     * @return <code>true</code> if multi-line comments should be removed during
     *         processing.
     */
    public boolean isRemoveMLComments()
    {
        return this.removeMLComments;
    }

    /**
     * Sets whether single-line comments should be removed during processing.
     *
     * @param remove if <code>true</code> single-line comments will be removed during
     *        processing.
     */
    public void setRemoveSLComments(boolean remove)
    {
        this.removeSLComments = remove;
    }

    /**
     * Indicates whether single-line comments should be removed during processing.
     *
     * @return <code>true</code> if single-line comments should be removed during
     *         processing.
     */
    public boolean isRemoveSLComments()
    {
        return this.removeSLComments;
    }

    /**
     * Sets whether Javadoc comments should be removed during processing.
     *
     * @param remove if <code>true</code> Javadoc comments will be removed during
     *        processing.
     */
    public void setRemoveJavadocComments(boolean remove)
    {
        this.removeJavadocComments = remove;
    }

    /**
     * Indicates whether Javadoc comments should be removed during processing.
     *
     * @return <code>true</code> if Javadoc comments should be removed during
     *         processing.
     */
    public boolean isRemoveJavadocComments()
    {
        return this.removeJavadocComments;
    }

    /**
     * Sets whether Javadoc comments should be parsed during processing.
     *
     * @param parse if <code>true</code> Javadoc comments will be parsed during
     *        processing.
     */
    public void setParseJavadocComments(boolean parse)
    {
        this.parseJavadocComments = parse;
    }

    /**
     * Indicates whether Javadoc comments will be parsed during processing.
     *
     * @return <code>true</code> if Javadoc comments will be parsed during
     *         processing.
     */
    public boolean isParseJavadocComments()
    {
        return this.parseJavadocComments;
    }

    /**
     * Sets the source compatiblity to the given release version.
     *
     * @param version Java JDK version constant.
     */
    public void setCompatibility(int version)
    {
        this.sourceVersion = version;
    }

    /**
     * Gets the current source compatiblity version.
     *
     * @return compatiblity version.
     */
    public int getCompatibility()
    {
        return this.sourceVersion;
    }

    /**
     * Test the token type against the literals table.
     *
     * @param ttype recognized token type.
     *
     * @return token type.
     */
    public int testLiteralsTable(int ttype)
    {
        this.hashString.setBuffer(text.getBuffer(), text.length());
        Integer literalsIndex = (Integer)literals.get(hashString);

        if (literalsIndex != null)
        {
            ttype = literalsIndex.intValue();
            switch (ttype)
            {
                case JavaTokenTypes.LITERAL_assert:
                    switch (this.sourceVersion)
                    {
                        case JDK_1_3:
                            ttype = JavaTokenTypes.IDENT;
                            break;
                    }
                    break;
            }
        }

        return ttype;
    }

    /**
     * Test the text passed in against the literals table.
     *
     * @param tokenText recognized token text.
     * @param ttype recognized token text type.
     *
     * @return token type.
     */
    public int testLiteralsTable(String tokenText, int ttype)
    {
        ANTLRHashString s = new ANTLRHashString(tokenText, this);
        Integer literalsIndex = (Integer)literals.get(s);

        if (literalsIndex != null)
        {
            ttype = literalsIndex.intValue();

            switch (ttype)
            {
                case JavaTokenTypes.LITERAL_assert:
                    switch (this.sourceVersion)
                    {
                        case JDK_1_3:
                            ttype = JavaTokenTypes.IDENT;
                            break;
                    }
                    break;
            }
        }

        return ttype;
    }

    public void panic()
    {
        if (this.inputState != null)
        {
            Object[] args = { getFilename(), new Integer(getLine()), new Integer(getColumn()), "JavaLexer: panic" };
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
      Object[] args = { getFilename(), new Integer(getLine()),new Integer(getColumn()), ex.getMessage() };
      _logger.l7dlog(Level.ERROR, "PARSER_ERROR", args, ex);
   }

   /**
    * Reports the given error.
    *
    * @param message error message.
    */
   public void reportError(String message)
   {
       Object[] args = { getFilename(), new Integer(getLine()), new Integer(getColumn()), message };
       _logger.l7dlog(Level.ERROR, "PARSER_ERROR", args, null);
   }

   /**
    * Reports the given warning.
    *
    * @param message warning message.
    */
   public void reportWarning(String message)
   {
       Object[] args = { getFilename(), new Integer(getLine()),new Integer(getColumn()), message };
       _logger.l7dlog(Level.WARN, "PARSER_ERROR", args, null);
   }

    /**
     * Creates a token of the given tpye.
     *
     * @param t type of the token.
     */
    protected Token makeToken(int t)
    {
        
        ExtendedToken newToken = _factory.getExtendedTokenFactory().create(t, null);
        ((MyLexerSharedInputState)this.inputState).configureToken(newToken);
        newToken.endLine=this.getLine();
        newToken.endColumn = this.getColumn();
        
        return newToken;
    }

    /**
     * Sets the class to use for tokens.
     *
     * @param clazz a qualified class name.
     *
     * @throws IllegalArgumentException if the class is not derived from
     * {@link de.hunsicker.jalopy.language.antlr.ExtendedToken}.
     */
    public void setTokenObjectClass(String clazz)
    {

        // necessary because our ctor calls this method with the default ANTLR
        // token object class during instantiation. If the ANTLR guys ever
        // change the class name, instantiating our lexer will fail until we've
        // changed our method too
        if (clazz.equals("antlr.CommonToken"))
        {
            clazz = "de.hunsicker.jalopy.language.antlr.ExtendedToken";
        }

        super.setTokenObjectClass(clazz);
        
        // TODO Technically this is not needed.

        /*
        Object instance = null;

        try
        {
            instance = this.tokenObjectClass.newInstance();
        }
        catch (Exception ex)
        {
            panic("Java Lexer.467" + ex);
            return;
        }

        if (!(instance instanceof de.hunsicker.jalopy.language.ExtendedToken))
        {
            throw new IllegalArgumentException("your TokenObject class must extend de.hunsicker.jalopy.language.ExtendedToken");
        }
        */
        
    }

    /**
     * Returns the detected file format.
     *
     * @return file format.
     */
    public FileFormat getFileFormat()
    {
        return _fileFormat;
    }

    /**
     * Sets the input buffer to use.
     * @param buf buffer.
     */
    public void setInputBuffer(InputBuffer buf)
    {
        if (this.inputState != null) {
            ((MyLexerSharedInputState)this.inputState).setInputBuffer(buf);
        }
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
     * Resets the lexer.
     *
     * <p>You have to re-initialize the input buffer before you can use the
     * lexer again.</p>
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
    }

    /**
     * Returns the index within this string of the first occurrence of any of the line
     * separator characters (quot;\nquot;, quot;\r\nquot; or quot;\rquot;).
     *
     * @param str a string.
     *
     * @return the index of the first character of a newline string; otherwise
     *         <code>-1</code> is returned.
     */
    private void getNextSeparator(SeparatorInfo result, String str)
    {
        int offset = offset = str.indexOf("\r\n" /* NOI18N */); // DOS

        if (offset > -1)
        {
            result.offset = offset;
            result.length = 2;
        }
        else
        {
            result.length = 1;

            offset = str.indexOf('\n'); // UNIX

            if (offset > -1)
            {
                result.offset = offset;
            }
            else
            {
                offset = str.indexOf('\r'); // MAC
                result.offset = offset;
            }
        }
    }

    private final static class SeparatorInfo
    {
        int length = 1;
        int offset = -1;
    }

    /**
     * Removes the leading whitespace from each line of the given multi-line comment.
     *
     * @param comment a multi-line comment.
     * @param column the column offset of the line where the comment starts.
     * @param lineSeparator the line separator.
     *
     * @return comment without leading whitespace.
     */
    private String removeLeadingWhitespace(
        String comment,
        int    column,
        String lineSeparator)
    {
        // TODO Can this be removed ???
        String[] lines = split(comment, column);
        StringBuffer buf = new StringBuffer(comment.length());

        for (int i = 0, size = lines.length; i < size; i++)
        {
            buf.append(lines[i]);
            buf.append(_lineSeparator);
        }

        buf.setLength(buf.length() - _lineSeparator.length());

        return buf.toString();
    }
    protected Token makeJavaDoc(Token node, String newText) throws TokenStreamIOException {
        node.setText(newText);
        boolean javadoc = node.getText().trim().startsWith("/**");
        
        // we found a Javadoc comment
        if (javadoc)
        {
            if (!this.removeJavadocComments)
            {
                // only parse Javadoc comments if the user likes this
                // feature as much as I do (and explictly enabled it)
                if (this.parseJavadocComments)
                {
                    String t = node.getText();

                    if (t.indexOf('\t') > -1)
                    {
                        t = StringHelper.replace(t, "\t", StringHelper.repeat(SPACE, getTabSize()));
                    }
                    // TODO Decide if this is required...
                    //t = removeLeadingWhitespace(t, node.getColumn() -1, _lineSeparator);
                    node.setText(t);
                    
                    Node comment = (Node) _factory.getJavaNodeFactory().create(JAVADOC_COMMENT);
                    comment.setText(t);
                    node = _factory.getExtendedTokenFactory().create(JavaTokenTypes.JAVADOC_COMMENT, t);
                    ((ExtendedToken)node).comment = comment;
//                    try
//                    {
//                        String t = node.getText();
//                        _recognizer.setLine(node.getLine());
//                        _recognizer.setColumn(node.getColumn());
//                        _recognizer.parse(t, getFilename());
//                        Node comment = (Node)_recognizer.getParseTree();
//
//                        // ignore empty comments
//                        if (comment != JavadocParser.EMPTY_JAVADOC_COMMENT)
//                        {
//                            node = _factory.getExtendedTokenFactory().create(JavaTokenTypes.JAVADOC_COMMENT, t);
//                            ((ExtendedToken)node).comment = comment;
//                            comment.setText(t);
//                            
//                        }
//                        else {
//                            node.setType(Token.SKIP);
//                        }
//                    }
//                    catch (IOException ex)
//                    {
//                        throw new TokenStreamIOException(ex);
//                    }
                }
                else
                {
                    // XXX only if not in tab mode
                    // replace tabs

                    String t = node.getText();

                    if (t.indexOf('\t') > -1)
                    {
                        t = StringHelper.replace(t, "\t", StringHelper.repeat(SPACE, getTabSize()));
                    }

                    t = removeLeadingWhitespace(t, node.getColumn() -1, _lineSeparator);

                    node.setText(t);
                    node.setType(JavaTokenTypes.JAVADOC_COMMENT);
                }
                
            }
            else
            {
                node.setType(Token.SKIP);
            }
        }
        else
        {
            if (!this.removeMLComments)
            {
                String t = node.getText();

                // replace tabs
                if (t.indexOf('\t') > -1)
                {
                    t = StringHelper.replace(t, "\t", StringHelper.repeat(SPACE, getTabSize()));
                }

                // in case we don't format multi-line comments, we have
                // to remove the leading whitespace for each line
                if (!this.formatMLComments)
                {
                    t = removeLeadingWhitespace(t, node.getColumn() -1, _lineSeparator);
                }

                node.setText(t);
            }
            else
                node.setType(Token.SKIP);
        }       
        return node;
    }


    /**
     * Returns the individual lines of the given multi-line comment.
     *
     * @param str a multi-line comment.
     * @param beginOffset the column offset of the line where the comment starts.
     *
     * @return the individual lines of the comment.
     */
    private String[] split(
        String str,
        int    beginOffset)
    {
        List lines = new ArrayList(15);

        SeparatorInfo info = new SeparatorInfo();

        for (getNextSeparator(info, str); info.offset > -1; getNextSeparator(info, str))
        {
            String line = str.substring(0, info.offset);
            str = str.substring(info.offset + info.length);

            int charOffset = StringHelper.indexOfNonWhitespace(line);

            if (charOffset > beginOffset)
            {
                line = line.substring(beginOffset);
            }
            else if (charOffset > -1)
            {
                line = line.substring(charOffset);
            }
            else
            {
                line = EMPTY_STRING;
            }

            lines.add(line);
        }

        int charOffset = StringHelper.indexOfNonWhitespace(str);

        if (charOffset > beginOffset)
        {
            str = str.substring(beginOffset);
        }
        else if (charOffset > -1)
        {
            str = str.substring(charOffset);
        }
        else
        {
            str = EMPTY_STRING;
        }

        lines.add(str);

        return (String[]) lines.toArray(EMPTY_STRING_ARRAY);
    }    
    // end

 }