/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import antlr.CommonHiddenStreamToken;
import de.hunsicker.jalopy.language.CompositeFactory;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaNodeFactory;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.Environment;
import de.hunsicker.util.StringHelper;


/**
 * The writer to be used to print a Java AST. This class contains some basic support
 * methods to be used by printers.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @author <a href="mailto:david_beutel2@yahoo.com">David Beutel</a>
 * @version $Revision: 1.9 $
 *
 * @see de.hunsicker.jalopy.printer.Printer
 */
public class NodeWriter
    extends Writer
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Indicates that no indentation should be performed. */
    public static final boolean INDENT_NO = false;

    /** Indicates that indentation should be performed. */
    public static final boolean INDENT_YES = true;

    /** Print NO newline after a token. */
    public static final boolean NEWLINE_NO = false;

    /** Print a newline after a token. */
    public static final boolean NEWLINE_YES = true;

    /** Indicates that a printer is in default mode. */
    static final int MODE_DEFAULT = 1;

    /** Indicates that a printer is in testing mode. */
    static final int MODE_TEST = 2;
    private static final String LCURLY = "{" /* NOI18N */.intern();
    private static final String RCURLY = "}" /* NOI18N */.intern();
    // TODO private static final String SEMI = ";" /* NOI18N */.intern();
    private static final String TAB = "\t" /* NOI18N */.intern();
    private static final String EMPTY_STRING = "" /* NOI18N */.intern();

    //~ Instance variables ---------------------------------------------------------------

    /** The code convention settings that controls the output style. */
    protected Convention settings;

    /** The envrionment to use. */
    protected Environment environment;

    /** Used line separator. Defaults to the platform standard. */
    protected String lineSeparator;

    /** The original line separator of the file as reported by the lexer. */
    protected String originalLineSeparator;

    /** Should indenting use an added contination amount? */
    protected boolean continuation;

    /** Should a footer be inserted at the end of every file? */
    protected boolean footer;

    /** Indicates wether a trailing empty line should be inserted at the end of a file. */
    protected boolean insertTrailingEmpty;

    /** Print left curly braces on a new line? */
    protected boolean leftBraceNewline;

    /**
     * Indicates whether we're at the beginning of a new line (<code>column == 1</code>).
     */
    protected boolean newline = true;
    
    /** Indicates that the line following this is a new line
     * Used for printing commas in blocks without white space after the new line 
     */
    protected boolean nextNewline = false;

    /** Should tabs only be used to print leading indentation? */
    protected boolean useLeadingTabs;

    /** Should tabs be used to print indentation? */
    protected boolean useTabs;

    /** Current column position. */
    protected int column = 1;

    /** Number of spaces to use for continuation indentation. */
    protected int continuationIndentSize;

    /** Number of spaces to take for one indent. */
    protected int indentSize;

    /** Holds the type of the last printed token. */
    protected int last = JavaTokenTypes.BOF;

    /** Number of spaces to print before left curly braces. */
    protected int leftBraceIndent;

    /** Current line number. */
    protected int line = 1;

    /** Printing mode. */
    protected int mode = MODE_DEFAULT;
    CommonHiddenStreamToken pendingComment;

    /** The last EXPR node printed. */
    JavaNode expression;

    /** The found issues. */
    Map issues;

    /** The printing state. */
    PrinterState state;

    /** The original filename of the stream we output. */
    String filename = "<unknown>" /* NOI18N */;
    WriterCache testers;

    /** Indicates whether a tree contains annotations. */
    boolean tracking;

    //boolean groupingParentheses;

    /** The number of blank lines that were printed before the last EXPR node. */
    int blankLines;

    /** Current indent level. */
    int indentLevel;

    /** Number of spaces to use for leading indentation. */
    int leadingIndentSize;

    /** Whitespace to prepend every line. */
    private String _leadingIndentSizeString;

    /** Our right brace. */
    private String _rightBrace;

    /** Target output stream. */
    private Writer _out;

    /** Used to generate the indent string. */
    private char[] _indentChars;
    
    private CompositeFactory _factory = null;

    public int javadocIndent = 0;
    


    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new NodeWriter object with the given file output format.
     *
     * @param out the output stream to write to.
     * @param filename name of the parsed file.
     * @param issues holds the issues found during a run.
     * @param lineSeparator the lineSeparator to use.
     * @param originalLineSeparator the original line separator of the file.
     */
    public NodeWriter(
        Writer out,
        CompositeFactory factory,
        String filename,
        Map    issues,
        String lineSeparator,
        String originalLineSeparator)
    {
        this(factory);
        this.filename = filename;
        this.issues = issues;
        this.lineSeparator = lineSeparator;
        this.originalLineSeparator = originalLineSeparator;
        this.testers = new WriterCache(factory,this);
        _out = out;
    }
    
    public CompositeFactory getCompositeFactory() {
        return _factory;
    }
    public JavaNodeFactory getJavaNodeFactory() {
        return _factory.getJavaNodeFactory();
    }


    /**
     * Creates a new NodeWriter object.
     */
    protected NodeWriter(CompositeFactory factory)
    {
        this.state = new PrinterState(this);
        this._factory = factory;
        this.lineSeparator = File.separator;
        this.settings = Convention.getInstance();
        this.indentSize =
            AbstractPrinter.settings.getInt(
                ConventionKeys.INDENT_SIZE, ConventionDefaults.INDENT_SIZE);
        this.insertTrailingEmpty =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.INSERT_TRAILING_NEWLINE,
                ConventionDefaults.INSERT_TRAILING_NEWLINE);
        this.continuationIndentSize =
            AbstractPrinter.settings.getInt(
                ConventionKeys.INDENT_SIZE_CONTINUATION,
                ConventionDefaults.INDENT_SIZE_CONTINUATION);
        this.leftBraceNewline =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.BRACE_NEWLINE_LEFT, ConventionDefaults.BRACE_NEWLINE_LEFT);
        this.leftBraceIndent =
            AbstractPrinter.settings.getInt(
                ConventionKeys.INDENT_SIZE_BRACE_LEFT,
                ConventionDefaults.INDENT_SIZE_BRACE_LEFT);
        this.leadingIndentSize =
            AbstractPrinter.settings.getInt(
                ConventionKeys.INDENT_SIZE_LEADING, ConventionDefaults.INDENT_SIZE_LEADING);
        this.useTabs =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.INDENT_WITH_TABS, ConventionDefaults.INDENT_WITH_TABS);
        this.useLeadingTabs =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.INDENT_WITH_TABS_ONLY_LEADING,
                ConventionDefaults.INDENT_WITH_TABS_ONLY_LEADING);
        this.footer =
            AbstractPrinter.settings.getBoolean(ConventionKeys.FOOTER, ConventionDefaults.FOOTER);
        _indentChars = new char[150];

        for (int i = 0; i < _indentChars.length; i++)
        {
            _indentChars[i] = ' ';
        }

        if (this.leadingIndentSize > 0)
        {
            _leadingIndentSizeString = getString(this.leadingIndentSize);

            if (this.useTabs)
            {
                _leadingIndentSizeString =
                    StringHelper.replace(
                        _leadingIndentSizeString, getString(this.indentSize), TAB);
            }
        }
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the line column position of the last written character.
     *
     * @return line position of the last written character.
     */
    public int getColumn()
    {
        return this.column;
    }


    /**
     * Sets the environment to use.
     *
     * @param environment environment.
     *
     * @since 1.0b9
     */
    public void setEnvironment(Environment environment)
    {
        this.environment = environment;
    }


    /**
     * Sets the filename of the file beeing printed.
     *
     * @param filename filename of the source files beeing printed.
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }


    /**
     * Returns the name of the parsed file.
     *
     * @return filename.
     */
    public String getFilename()
    {
        return this.filename;
    }


    /**
     * Returns the length of the current indent string.
     *
     * @return indent string length.
     */
    public int getIndentLength()
    {
        return this.indentLevel * this.indentSize;
    }


    /**
     * Sets the current indent level.
     *
     * @param level new indent level.
     */
    public void setIndentLevel(int level)
    {
        this.indentLevel = level;
    }


    /**
     * Returns the current indent level.
     *
     * @return indent level.
     */
    public int getIndentLevel()
    {
        return this.indentLevel;
    }


    /**
     * Returns the number of spaces to use for indentation.
     *
     * @return the indentation size.
     *
     * @since 1.0b8
     */
    public int getIndentSize()
    {
        return this.indentSize;
    }


    /**
     * Sets the type of the token last printed.
     *
     * @param type type of the token
     */
    public void setLast(int type)
    {
        this.last = type;
    }


    /**
     * Returns the type of the token last printed.
     *
     * @return type of the token that was printed last. Returns <code>{@link
     *         JavaTokenTypes#BOF}</code> if nothing was printed yet.
     */
    public int getLast()
    {
        return this.last;
    }


    /**
     * Returns the current line number.
     *
     * @return the current line number.
     */
    public int getLine()
    {
        return this.line;
    }


    /**
     * Sets the separator string to use for newlines.
     *
     * @param lineSeparator separator string. Either &quot;\n&quot;, &quot;\r&quot; or
     *        &quot;\r\n&quot;.
     */
    public void setLineSeparator(String lineSeparator)
    {
        this.lineSeparator = lineSeparator;
    }


    /**
     * Returns the current line separator.
     *
     * @return the current line separator.
     */
    public String getLineSeparator()
    {
        return this.lineSeparator;
    }


    /**
     * Returns a string of the given length.
     *
     * @param length length of the string to return.
     *
     * @return string with the given length.
     */
    public String getString(int length)
    {
        return generateIndentString(length);
    }


    /**
     * Sets whether the tree that is to be printed contains nodes that needs their
     * positions tracked.
     *
     * @param tracking <code>true</code> to indicate that certain nodes needs their
     *        positions tracked.
     *
     * @since 1.0b9
     */
    public void setTracking(boolean tracking)
    {
        this.tracking = tracking;
    }


    /**
     * Sets the underlying writer to actually write to.
     *
     * @param out writer to write to.
     */
    public void setWriter(Writer out)
    {
        _out = out;
    }


    /**
     * Closes the stream, flushing it first.
     *
     * @throws IOException if an I/O error occured.
     */
    public void close()
      throws IOException
    {
        this.settings = null;
        this.issues = null;
        this.state.dispose();
        this.state = null;
        _out.close();
    }


    /**
     * Flushes the stream.
     *
     * @throws IOException if an I/O error occured.
     */
    public void flush()
      throws IOException
    {
        _out.flush();
    }


    /**
     * Increases the current indent level one level.
     */
    public void indent()
    {
        setIndentLevel(this.indentLevel + 1);
    }


    /**
     * Outputs the given string of the given type to the underlying writer.
     *
     * @param string string to write.
     * @param type type of the string.
     *
     * @return the column offset were the string started.
     *
     * @throws IOException if an I/O error occured.
     */
    public int print(
        String string,
        int    type)
      throws IOException
    {
        int offset = 1;

        if (this.newline)
        {
            if (leadingIndentSize > 0)
            {
                _out.write(_leadingIndentSizeString);
                this.column += leadingIndentSize;
            }

            int length = this.indentLevel * this.indentSize;

            if (continuation) // use continuation indentation
            {
                length += continuationIndentSize;
            }

            switch (type)
            {
                case JavaTokenTypes.WS :
                {
                    if (!useTabs)
                    {
                        String s = generateIndentString(length + string.length());
                        this.column += s.length();
                        _out.write(s);
                    }
                    else
                    {
                        if (!this.useLeadingTabs)
                        {
                            String s = generateIndentString(length + string.length());
                            this.column += s.length();
                            s = StringHelper.replace(
                                    s, generateIndentString(this.indentSize), TAB);
                            _out.write(s);
                        }
                        else
                        {
                            String s = generateIndentString(length);
                            this.column += length;
                            s = StringHelper.replace(
                                    s, generateIndentString(this.indentSize), TAB);
                            _out.write(s);

                            this.column += string.length();
                            _out.write(string);
                        }
                    }

                    break;
                }

                default :
                {
                    String s = generateIndentString(length);
                    offset += length;
                    this.column += (length + string.length());

                    if (this.useTabs)
                    {
                        s = StringHelper.replace(
                                s, generateIndentString(this.indentSize), TAB);
                    }

                    _out.write(s);
                    _out.write(string);

                    break;
                }
            }

            this.newline = false;
        }
        else
        {
            switch (type)
            {
                case JavaTokenTypes.WS :

                    if (
                        this.useTabs && !useLeadingTabs
                        && (string.length() > this.indentSize))
                    {
                        int tabCount = this.column / this.indentSize;
                        int spacesCount = this.column - 1 - (tabCount * this.indentSize);
                        this.column += string.length();

                        if (spacesCount == 0)
                        {
                            string =
                                StringHelper.replace(
                                    string, generateIndentString(this.indentSize), TAB);
                            _out.write(string);
                        }
                        else
                        {
                            if (spacesCount < 0)
                            {
                                _out.write(TAB);
                            }

                            _out.write(TAB);

                            string =
                                StringHelper.replace(
                                    string.substring(this.indentSize - spacesCount),
                                    generateIndentString(this.indentSize), TAB);
                            _out.write(string);
                        }

                        break;
                    }

                // fall through
                default :
                    offset = this.column;
                    this.column += string.length();
                    _out.write(string);

                    break;
            }
        }

        this.last = type;

        return offset;
    }


    /**
     * Prints the given number of blank lines.
     *
     * @param amount number of blank lines to print.
     *
     * @throws IOException if an I/O error occured.
     */
    public void printBlankLines(int amount)
      throws IOException
    {
        for (int i = 0; i < amount; i++)
        {
            printNewline();
        }
    }


    /**
     * Outputs a left curly brace.
     *
     * @return the column offset where the brace was printed.
     *
     * @throws IOException if an I/O error occured.
     */
    public int printLeftBrace()
      throws IOException
    {
        return printLeftBrace(NEWLINE_YES, NEWLINE_YES);
    }


    /**
     * Outputs a leftcurly brace.
     *
     * @param newlineBefore <code>true</code> if a newline should be printed before the
     *        brace.
     * @param newlineAfter <code>true</code> if a newline should be printed after the
     *        brace.
     *
     * @return the column offset where the brace was printed.
     *
     * @throws IOException if an I/O error occured.
     */
    public int printLeftBrace(
        boolean newlineBefore,
        boolean newlineAfter)
      throws IOException
    {
        return printLeftBrace(newlineBefore, newlineAfter, NodeWriter.INDENT_YES);
    }


    /**
     * Outputs a left curly brace.
     *
     * @param newlineBefore <code>true</code> if a newline should be printed before the
     *        brace.
     * @param newlineAfter <code>true</code> if a newline should be printed after the
     *        brace.
     * @param indent if <code>true</code> the brace will be indented relative to the
     *        current indentation level.
     *
     * @return the column offset where the brace was printed.
     *
     * @throws IOException if an I/O error occured.
     */
    public int printLeftBrace(
        boolean newlineBefore,
        boolean newlineAfter,
        boolean indent)
      throws IOException
    {
        if (newlineBefore)
        {
            printNewline();
        }

        if (indent && (this.leftBraceIndent > 0))
        {
            print(generateIndentString(this.leftBraceIndent), JavaTokenTypes.WS);
        }

        int offset = print(LCURLY, JavaTokenTypes.LCURLY);

        if (newlineAfter)
        {
            printNewline();
        }

        indent();

        return offset;
    }


    /**
     * Outputs a line break.
     *
     * @throws IOException if an I/O error occured.
     */
    public void printNewline()
      throws IOException
    {
        _out.write(this.lineSeparator);
        this.newline = true;
        this.column = 1;
        this.line++;
    }


    /**
     * Outputs a closing curly brace. Prints a newline after the brace.
     *
     * @return the column offset where the brace was printed.
     *
     * @throws IOException if an I/O error occured.
     */
    public int printRightBrace()
      throws IOException
    {
        return printRightBrace(NEWLINE_YES);
    }


    /**
     * Outputs a right curly brace.
     *
     * @param newlineAfter <code>true</code> if a newline should be printed after the
     *        brace.
     *
     * @return the column offset where the brace was printed.
     *
     * @throws IOException if an I/O error occured.
     */
    public int printRightBrace(boolean newlineAfter)
      throws IOException
    {
        return printRightBrace(JavaTokenTypes.RCURLY, newlineAfter);
    }


    /**
     * Outputs a right curly brace.
     *
     * @param type the type of the brace. Either RCURLY or OBJBLOCK.
     * @param newlineAfter if <code>true</code> a newline will be printed after the
     *        brace.
     *
     * @return the column offset where the brace was printed.
     *
     * @throws IOException if an I/O error occured.
     */
    public int printRightBrace(
        int     type,
        boolean newlineAfter)
      throws IOException
    {
        return printRightBrace(type, true, newlineAfter);
    }


    /**
     * Outputs a right curly brace.
     *
     * @param type the type of the brace. Either RCURLY or OBJBLOCK.
     * @param whitespaceBefore if <code>true</code> outputs indentation whitespace
     *        (depending on the code convention setting).
     * @param newlineAfter if <code>true</code> a newline will be printed after the
     *        brace.
     *
     * @return the column offset where the brace was printed.
     *
     * @throws IOException if an I/O error occured.
     */
    public int printRightBrace(
        int     type,
        boolean whitespaceBefore,
        boolean newlineAfter)
      throws IOException
    {
        unindent();

        int offset = 1;

        if (whitespaceBefore)
        {
            offset = print(getRightBrace(), type);
        }
        else
        {
            offset = print(RCURLY, type);
        }

        // only issue line break if not the last curly brace
        if (
            newlineAfter
            && ((this.indentLevel > 0) || (insertTrailingEmpty && !this.footer)))
        {
            printNewline();
        }

        return offset;
    }


    /**
     * Decreases the current indent level one level.
     */
    public void unindent()
    {
        setIndentLevel(this.indentLevel - 1);
    }


    /**
     * Write a portion of an array of characters.
     *
     * @param cbuf array of characters.
     * @param off offset from which to start writing characters.
     * @param len number of characters to write.
     *
     * @throws IOException if an I/O error occured.
     */
    public void write(
        char[] cbuf,
        int    off,
        int    len)
      throws IOException
    {
        _out.write(cbuf, off, len);
    }


    /**
     * Returns the closing (right) curly brace to use. The actual representation depends
     * on the code convention.
     *
     * @return the closing curly brace to use.
     */
    String getRightBrace()
    {
        if (_rightBrace == null)
        {
            StringBuffer buf = new StringBuffer(getIndentSize() + 1);
            buf.append(
                generateIndentString(
                    AbstractPrinter.settings.getInt(
                        ConventionKeys.INDENT_SIZE_BRACE_RIGHT,
                        ConventionDefaults.INDENT_SIZE_BRACE_RIGHT)));
            buf.append(RCURLY);

            _rightBrace = buf.toString();
        }

        return _rightBrace;
    }


    /**
     * Generates a string only comprimised of spaces, with the given length.
     *
     * @param length length of the string to create.
     *
     * @return a string of the given length.
     */
    private String generateIndentString(int length)
    {
        if (length == 0)
        {
            return EMPTY_STRING;
        }

        // make sure the char buffer is big enough
        if (length > _indentChars.length)
        {
            char[] buf = new char[(int) (1.4 * length)];

            for (int i = 0;; i++)
            {
                int offset = i * _indentChars.length;

                if ((offset + _indentChars.length) <= buf.length)
                {
                    System.arraycopy(_indentChars, 0, buf, offset, _indentChars.length);
                }
                else
                {
                    System.arraycopy(_indentChars, 0, buf, offset, buf.length - offset);

                    break;
                }
            }

            _indentChars = buf;
        }

        return new String(_indentChars, 0, length).intern();
    }
}
