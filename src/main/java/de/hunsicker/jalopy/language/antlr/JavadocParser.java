package de.hunsicker.jalopy.language.antlr;

import java.util.Collection;

import de.hunsicker.jalopy.language.Parser;
import de.hunsicker.jalopy.language.Recognizer;
import de.hunsicker.jalopy.language.antlr.*;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import antlr.ASTPair;
import antlr.MismatchedTokenException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.AST;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import de.hunsicker.util.Lcs;
import java.io.FileInputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;


/**
 * Parser for Javadoc comments.
 *
 * <p><strong>Sample Usage:</strong></p>
 * <pre>
 * <blockquote style="background:lightgrey">
 *  // an input source
 *  Reader in = new BufferedReader(new FileReader(new File(argv[0])));
 *
 *  // create a lexer
 *  Lexer lexer = new JavadocLexer();
 *
 *  // set up the lexer to read from the input source
 *  lexer.setInputBuffer(in);
 *
 *  // get the corresponding parser
 *  Parser parser = lexer.getParser();
 *
 *  // and start the parsing process
 *  parser.parse();
 * </blockquote>
 * </pre>
 *
 * <p>This is an <a href="http://www.antlr.org">ANTLR</a> automated generated
 * file. <strong>DO NOT EDIT</strong> but rather change the associated grammar
 * (<code>java.doc.g</code>) and rebuild.</p>
 *
 * @version 1.0
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 *
 * @see de.hunsicker.jalopy.language.antlr.JavadocLexer
 * @see de.hunsicker.jalopy.language.Recognizer
 */
public class JavadocParser extends InternalJavadocParser implements Parser{

    /** Logging. */
    private final Logger _logger = Logger.getLogger("de.hunsicker.jalopy.language.javadoc");

    /** Holds all valid tag names. */
    private Set _standardTags = new HashSet(); // Set of <String>

    /** Holds all valid inline tag names. */
    private Set _inlineTags = new HashSet(); // Set of <String>

    /** The token types for the parser/lexer. */
    private Map _tokenTypes; // Map of <String>

    /** The corresponding lexer. */
    private JavadocLexer _lexer;

    /** Starting line of the comment in the source file. */
    private int _startLine;

    /** Starting column of the comment in the source file. */
    private int _startColumn;

    /** The empty Javadoc comment. */
    public final static Node EMPTY_JAVADOC_COMMENT = new Node(JavadocTokenTypes.JAVADOC_COMMENT, "<JAVADOC_COMMENT>"){};

    /** File to load the standard Javadoc tag definitions from. */
    //private final static File STANDARD_TAGS = new File(System.getProperty("user.home")+ File.separator + ".jalopy" + File.separator + "standard.tags");

    /** File to load the inline Javadoc tag definitions from. */
    //private final static File INLINE_TAGS = new File(System.getProperty("user.home")+ File.separator + ".jalopy" + File.separator + "inline.tags");
    private JavaNodeFactory _factory = null;

    private  Recognizer recognizer = null;;
    {
        loadTokenTypeInfo();
        loadTagInfo(true);
    }
    /**
     * Creates a new JavadocParser object.
     *
     * @param tokenBuf DOCUMENT ME!
     */
    public JavadocParser(TokenBuffer tokenBuf, JavaNodeFactory factory)
    {
        this(tokenBuf, 1,factory);
    }


    /**
     * Creates a new JavadocParser object.
     *
     * @param lexer DOCUMENT ME!
     */
    public JavadocParser(TokenStream lexer, JavaNodeFactory factory)
    {
        this(lexer, 1, factory);
    }


    /**
     * Creates a new JavadocParser object.
     *
     * @param state DOCUMENT ME!
     */
    public JavadocParser(ParserSharedInputState state)
    {
        super(state);
    }


    /**
     * Creates a new JavadocParser object.
     *
     * @param tokenBuf DOCUMENT ME!
     * @param k DOCUMENT ME!
     */
    protected JavadocParser(
        TokenBuffer tokenBuf,
        int         k, JavaNodeFactory factory)
    {
        super(tokenBuf, k);
        _factory = factory;
        
    }


    /**
     * Creates a new JavadocParser object.
     *
     * @param lexer DOCUMENT ME!
     * @param k DOCUMENT ME!
     */
    protected JavadocParser(
        TokenStream lexer,
        int         k, JavaNodeFactory factory)
    {
        super(lexer, k);
        _factory = factory;
    }

    /**
     * Sets the corresponding Javadoc lexer for the parser.
     * @param lexer corresponding Javadoc lexer.
     */
    public void setLexer(JavadocLexer lexer)
    {
        _lexer = lexer;
    }

    /**
     * Loads the token type mapping table.
     */
    private void loadTokenTypeInfo()
    {
        try
        {
            Properties props = new Properties();
            props.load(JavadocLexer.class.getResourceAsStream("JavadocTokenTypes.txt"));
            _tokenTypes = new HashMap((int)(props.size() * 1.3));
            _tokenTypes.putAll(props);
        }
        catch (Exception ex)
        {
        	
        	ex.printStackTrace();
            throw new RuntimeException("failed loading token types file -- JavadocTokenTypes.txt");
        }
    }

    /**
     * Loads the properties from the given file.
     * @param file property file.
     */
    private Properties loadProperties(File file)
        throws IOException
    {
        Properties props = new Properties();

        if (file.exists())
        {
            InputStream in  = null;

            try
            {
                in = new BufferedInputStream(new FileInputStream(file));
                props.load(in);
            }
            finally
            {
                in.close();
            }
        }

        return props;
    }

    /**
     * Loads the custom tag info from the given tag definition. The found tags
     * will be added to the given set.
     *
     * @param file the tag definition file.
     * @param tags set to add the tags to.
     */
    private void loadCustomTagInfo(File file, Set tags)
    {
        try
        {
            Properties props = loadProperties(file);
            tags.addAll(props.keySet());
        }
        catch (Exception ex)
        {
            throw new RuntimeException("failed loading tag definition file -- " + file);
        }
    }

    /**
     * Loads the Javadoc standard tag info.
     *
     * @param force if <code>true</code> forces a loading of the tag info.
     */
    private void loadStandardTagInfo(boolean force)
    {
        //if (force || STANDARD_TAGS.lastModified() > _standardStamp)
        if (force)
        {
            _standardTags.clear();
            _standardTags.add("@author");
            _standardTags.add("@deprecated");
            _standardTags.add("@exception");
            _standardTags.add("@param");
            _standardTags.add("@return");
            _standardTags.add("@see");
            _standardTags.add("@serial");
            _standardTags.add("@serialData");
            _standardTags.add("@serialField");
            _standardTags.add("@since");
            _standardTags.add("@throws");
            _standardTags.add("@todo");
            _standardTags.add("@version");
            //loadCustomTagInfo(STANDARD_TAGS, _standardTags);
            //_standardStamp = STANDARD_TAGS.lastModified();
        }
    }

    /**
     * Loads the Javadoc inline tag info.
     *
     * @param force if <code>true</code> forces a loading of the tag info.
     */
    private void loadInlineTagInfo(boolean force)
    {
        //if (force || INLINE_TAGS.lastModified() > _inlineStamp)
        if (force)
        {
            _inlineTags.clear();
            _inlineTags.add("@docRoot");
            _inlineTags.add("@inheritDoc");
            _inlineTags.add("@link");
            _inlineTags.add("@linkPlain");
            _inlineTags.add("@value");
            _inlineTags.add("@code");
            _inlineTags.add("@literal");
            //loadCustomTagInfo(INLINE_TAGS, _inlineTags);
            //_inlineStamp = INLINE_TAGS.lastModified();
        }
    }

    /**
     * Sets the custom Javadoc standard tags to recognize.
     *
     * @param tags tags.
     */
    public void setCustomStandardTags(Collection tags)
    {
        _standardTags.addAll(tags);
    }

    /**
     * Sets the custom Javadoc in-line tags to recognize.
     *
     * @param tags tags.
     */
    public void setCustomInlineTags(Collection tags)
    {
        _inlineTags.addAll(tags);
    }

    /**
     * Loads the Javadoc tag info.
     *
     * @param force if <code>true</code> forces a loading of the tag info.
     */
    private void loadTagInfo(boolean force)
    {
        if (force)
        {
            _standardTags = new HashSet(15);
            _inlineTags = new HashSet(8);
        }

        loadStandardTagInfo(force);
        loadInlineTagInfo(force);
    }

    /**
     * Tries to determine and set the correct type for the given tag node.
     * @param tag tag AST to set the correct type for.
     * @param type the type of the tag. Either {@link #TYPE_STANDARD} or
     *        {@link #TYPE_INLINE}
     */
    protected void setTagType(AST tag, String type)
    {
        String text = tag.getText();
        String name = getTag(text, type);

        if (name == null) // invalid tag
        {
            Object[] args = { getFilename(), new Integer(_lexer.getLine()), new Integer(_lexer.getColumn()), text };
            _logger.l7dlog(Level.ERROR, "TAG_INVALID", args, null);
        }
        else
        {
            if (name != text) // mispelled tag
            {
                // correct the tag name
                tag.setText(name);

                Object[] args = { getFilename(), new Integer(_lexer.getLine()), new Integer(_lexer.getColumn()), text, name};
                _logger.l7dlog(Level.WARN, "TAG_MISSPELLED_NAME", args, null);
            }

            String t = (String)_tokenTypes.get(type +
                                               name.substring(1).toUpperCase());

            // not found in the mapping table means custom tag
            if (t == null)
            {
                if (type == TYPE_STANDARD)
                    tag.setType(JavadocTokenTypes.TAG_CUSTOM);
                else
                    tag.setType(JavadocTokenTypes.TAG_INLINE_CUSTOM);
            }
            else
            {
                try
                {
                    int result = Integer.parseInt(t);
                    tag.setType(result);
                }
                catch (NumberFormatException neverOccurs)
                {
                    ;
                }
            }
        }
    }

    /**
     * Returns the matching tag for the given text, if any.
     *
     * @param text text to match against.
     * @param type the type of the tag. Either {@link #TYPE_STANDARD} or
     *        {@link #TYPE_INLINE}
     *
     * @return matching Javadoc tag. Returns <code>null</code> to indicate that
     * no valid tag could be found for the given text. If the given text
     * represents a valid tag the given text reference will be returned.
     */
    String getTag(String text, String type)
    {
        Set tags = null;

        if (type == TYPE_STANDARD)
            tags = _standardTags;
        else
            tags = _inlineTags;

        if (tags.contains(text))
        {
            return text;
        }

        Lcs lcs = new Lcs();

        for (Iterator i = tags.iterator(); i.hasNext();)
        {
            String tag = (String)i.next();

            lcs.init(text, tag);

            double similarity = lcs.getPercentage();

            // XXX evaluate whether this is appropriate
            if (similarity > 70.0)
            {
                // mispelled tag found
                return tag;
            }
        }

        // no match found means invalid tag
        return null;
    }

    /**
     * Resets the parser.
     */
    public void reset()
    {
        if (this.inputState != null)
        {
            this.inputState.reset();
        }

        setFilename(Recognizer.UNKNOWN_FILE);
        this.returnAST = null;
    }

    public AST getParseTree()
    {
        // can be null for empty comments. The empty node indicates that this
        // comment can be savely ignored. It is up to the caller to handle
        // it appropriate
        if (this.returnAST == null)
        {
          return EMPTY_JAVADOC_COMMENT;
        }

        Node current = (Node)this.returnAST;
        Node root = _factory.create(_startLine, _startColumn,
                                           current.endLine, current.endColumn);
        root.setType(JavadocTokenTypes.JAVADOC_COMMENT);
        root.setText(JAVADOC_COMMENT);
        root.setFirstChild(this.returnAST);

        return root;
    }

    private final static String JAVADOC_COMMENT = "JAVADOC_COMMENT";

   /**
    * Reports the given error.
    *
    * @param ex encountered exception.
    */
   public void reportError(RecognitionException ex)
   {
       Integer line = new Integer((recognizer!=null?recognizer.getStartLine():0) +ex.getLine());
       Integer column = new Integer((recognizer!=null?recognizer.getStartColumn():0) +ex.getColumn());
      Object args[] = { getFilename(), line, column, ex.getMessage() };
      _logger.l7dlog(Level.ERROR, "PARSER_ERROR", args, ex);
   }

   /**
    * Reports the given error.
    *
    * @param message error message.
    */
   public void reportError(String message)
   {
      Object args[]  = { getFilename(), new Integer(_lexer.getLine()), new Integer(_lexer.getColumn()), message };
      _logger.l7dlog(Level.ERROR, "PARSER_ERROR", args, null);
   }

   /**
    * Reports the given warning.
    *
    * @param message warning message.
    */
   public void reportWarning(String message)
   {
      Object args[]  = { getFilename(), new Integer(_lexer.getLine()), new Integer(_lexer.getColumn()), message };
      _logger.l7dlog(Level.WARN, message, args, null);
   }

    /**
     * Handler for recoverable errors. If the error can't be handled it will be
     * rethrown.
     *
     * @param ex the input violation exception.
     */
    public void handleRecoverableError(RecognitionException ex)
    {
        if (ex instanceof antlr.MismatchedTokenException)
        {
            MismatchedTokenException mtex = (MismatchedTokenException)ex;

            if (mtex.token != null)
            {
                switch (mtex.expecting)
                {
                    // missing closing </p> tag
                    case JavadocTokenTypes.CPARA:

                        System.err.println("[WARN] ambigious missing </p> tag around line " + mtex.token.getLine() + " ");

                        // we add the found token manually
                        ASTPair currentAST = new ASTPair();
                        AST tmp2_AST = this.astFactory.create(mtex.token);
                        this.astFactory.makeASTRoot(currentAST, tmp2_AST);
                        this.astFactory.addASTChild(currentAST, this.returnAST);

                        // we also have to increase the line counter
                        _lexer.setLine(_lexer.getLine() + 1);

                        // now the parsing can go on
                        break;

                    default:
                        // we can't handle the error situation
                        reportError(ex);
                }
            }
            else
            {
                // we can't handle the error situation
                reportError(ex);
            }
        }
        else
        {
            // we can't handle the error situation
            reportError(ex);
        }
    }
public void parse() throws RecognitionException, TokenStreamException {
	if ( inputState.guessing==0 ) {
		
		// Uncomment to check the tag definition files for update
		//loadTagInfo(false);
	    
		_startLine = _lexer.getLine();
		_startColumn = _lexer.getColumn();
		
	}
	internalParse();
}


public void setRecognizer(Recognizer recognizer2) {
    this.recognizer = recognizer2;
    _lexer.setRecognizer(recognizer2);
}

}
