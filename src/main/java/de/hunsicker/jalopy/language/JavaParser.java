package de.hunsicker.jalopy.language;
// $ANTLR 2.7.4: "java15.g" -> "JavaParser.java"$

import antlr.ANTLRStringBuffer;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.ParserSharedInputState;
import antlr.collections.AST;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.hunsicker.jalopy.language.antlr.InternalJavaParser;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;

/** Java 1.5 Recognizer
 *
 * Run 'java Main [-showtree] directory-full-of-java-files'
 *
 * [The -showtree option pops up a Swing frame that shows
 *  the JavaNode constructed from the parser.]
 *
 * Run 'java Main <directory full of java files>'
 *
 * Contributing authors:
 *		John Mitchell		johnm@non.net
 *		Terence Parr		parrt@magelang.com
 *		John Lilley		jlilley@empathy.com
 *		Scott Stanchfield	thetick@magelang.com
 *		Markus Mohnen		mohnen@informatik.rwth-aachen.de
 *		Peter Williams		pete.williams@sun.com
 *		Allan Jacobs		Allan.Jacobs@eng.sun.com
 *		Steve Messick		messick@redhills.com
 *		John Pybus		john@pybus.org
 *
 * Version 1.00 December 9, 1997 -- initial release
 * Version 1.01 December 10, 1997
 *		fixed bug in octal def (0..7 not 0..8)
 * Version 1.10 August 1998 (parrt)
 *		added tree construction
 *		fixed definition of WS,comments for mac,pc,unix newlines
 *		added unary plus
 * Version 1.11 (Nov 20, 1998)
 *		Added "shutup" option to turn off last ambig warning.
 *		Fixed inner class def to allow named class defs as statements
 *		synchronized requires compound not simple statement
 *		add [] after builtInType DOT class in primaryExpression
 *		"const" is reserved but not valid..removed from modifiers
 * Version 1.12 (Feb 2, 1999)
 *		Changed LITERAL_xxx to xxx in tree grammar.
 *		Updated java.g to use tokens {...} now for 2.6.0 (new feature).
 *
 * Version 1.13 (Apr 23, 1999)
 *		Didn't have (stat)? for else clause in tree parser.
 *		Didn't gen ASTs for interface extends.  Updated tree parser too.
 *		Updated to 2.6.0.
 * Version 1.14 (Jun 20, 1999)
 *		Allowed final/abstract on local classes.
 *		Removed local interfaces from methods
 *		Put instanceof precedence where it belongs...in relationalExpr
 *			It also had expr not type as arg; fixed it.
 *		Missing ! on SEMI in classBlock
 *		fixed: (expr) + "string" was parsed incorrectly (+ as unary plus).
 *		fixed: didn't like Object[].class in parser or tree parser
 * Version 1.15 (Jun 26, 1999)
 *		Screwed up rule with instanceof in it. :(  Fixed.
 *		Tree parser didn't like (expr).something; fixed.
 *		Allowed multiple inheritance in tree grammar. oops.
 * Version 1.16 (August 22, 1999)
 *		Extending an interface built a wacky tree: had extra EXTENDS.
 *		Tree grammar didn't allow multiple superinterfaces.
 *		Tree grammar didn't allow empty var initializer: {}
 * Version 1.17 (October 12, 1999)
 *		ESC lexer rule allowed 399 max not 377 max.
 *		java.tree.g didn't handle the expression of synchronized
 *		statements.
 * Version 1.18 (August 12, 2001)
 *	  	Terence updated to Java 2 Version 1.3 by
 *		observing/combining work of Allan Jacobs and Steve
 *		Messick.  Handles 1.3 src.  Summary:
 *		o  primary didn't include boolean.class kind of thing
 *	  	o  constructor calls parsed explicitly now:
 * 		   see explicitConstructorInvocation
 *		o  add strictfp modifier
 *	  	o  missing objBlock after new expression in tree grammar
 *		o  merged local class definition alternatives, moved after declaration
 *		o  fixed problem with ClassName.super.field
 *	  	o  reordered some alternatives to make things more efficient
 *		o  long and double constants were not differentiated from int/float
 *		o  whitespace rule was inefficient: matched only one char
 *		o  add an examples directory with some nasty 1.3 cases
 *		o  made Main.java use buffered IO and a Reader for Unicode support
 *		o  supports UNICODE?
 *		   Using Unicode charVocabulay makes code file big, but only
 *		   in the bitsets at the end. I need to make ANTLR generate
 *		   unicode bitsets more efficiently.
 * Version 1.19 (April 25, 2002)
 *		Terence added in nice fixes by John Pybus concerning floating
 *		constants and problems with super() calls.  John did a nice
 *		reorg of the primary/postfix expression stuff to read better
 *		and makes f.g.super() parse properly (it was METHOD_CALL not
 *		a SUPER_CTOR_CALL).  Also:
 *
 *		o  "finally" clause was a root...made it a child of "try"
 *		o  Added stuff for asserts too for Java 1.4, but *commented out*
 *		   as it is not backward compatible.
 *
 * Version 1.20 (October 27, 2002)
 *
 *	  Terence ended up reorging John Pybus' stuff to
 *	  remove some nondeterminisms and some syntactic predicates.
 *	  Note that the grammar is stricter now; e.g., this(...) must
 *	be the first statement.
 *
 *	  Trinary ?: operator wasn't working as array name:
 *		  (isBig ? bigDigits : digits)[i];
 *
 *	  Checked parser/tree parser on source for
 *		  Resin-2.0.5, jive-2.1.1, jdk 1.3.1, Lucene, antlr 2.7.2a4,
 *		and the 110k-line jGuru server source.
 *
 * Version 1.21 (October 17, 2003)
 *  Fixed lots of problems including:
 *  Ray Waldin: add typeDefinition to interfaceBlock in java.tree.g
 *  He found a problem/fix with floating point that start with 0
 *  Ray also fixed problem that (int.class) was not recognized.
 *  Thorsten van Ellen noticed that \n are allowed incorrectly in strings.
 *  TJP fixed CHAR_LITERAL analogously.
 *
 * Version 1.21.2 (March, 2003)
 *	  Changes by Matt Quail to support generics (as per JDK1.5/JSR14)
 *	  Notes:
 *	  o We only allow the "extends" keyword and not the "implements"
 *		keyword, since thats what JSR14 seems to imply.
 *	  o Thanks to Monty Zukowski for his help on the antlr-interest
 *		mail list.
 *	  o Thanks to Alan Eliasen for testing the grammar over his
 *		Fink source base
 *
 * Version 1.22 (July, 2004)
 *	  Changes by Michael Studman to support Java 1.5 language extensions
 *	  Notes:
 *	  o Added support for annotations types
 *	  o Finished off Matt Quail's generics enhancements to support bound type arguments
 *	  o Added support for new for statement syntax
 *	  o Added support for static import syntax
 *	  o Added support for enum types
 *	  o Tested against JDK 1.5 source base and source base of jdigraph project
 *	  o Thanks to Matt Quail for doing the hard part by doing most of the generics work
 *
 * Version 1.22.1 (July 28, 2004)
 *	  Bug/omission fixes for Java 1.5 language support
 *	  o Fixed tree structure bug with classOrInterface - thanks to Pieter Vangorpto for
 *		spotting this
 *	  o Fixed bug where incorrect handling of SR and BSR tokens would cause type
 *		parameters to be recognised as type arguments.
 *	  o Enabled type parameters on constructors, annotations on enum constants
 *		and package definitions
 *	  o Fixed problems when parsing if ((char.class.equals(c))) {} - solution by Matt Quail at Cenqua
 *
 * Version 1.22.2 (July 28, 2004)
 *	  Slight refactoring of Java 1.5 language support
 *	  o Refactored for/"foreach" productions so that original literal "for" literal
 *	    is still used but the for sub-clauses vary by token type
 *	  o Fixed bug where type parameter was not included in generic constructor's branch of AST
 *
 * Version 1.22.3 (August 26, 2004)
 *	  Bug fixes as identified by Michael Stahl; clean up of tabs/spaces
 *        and other refactorings
 *	  o Fixed typeParameters omission in identPrimary and newStatement
 *	  o Replaced GT reconcilliation code with simple semantic predicate
 *	  o Adapted enum/assert keyword checking support from Michael Stahl's java15 grammar
 *	  o Refactored typeDefinition production and field productions to reduce duplication
 *
 * Version 1.22.4 (October 21, 2004)
 *    Small bux fixes
 *    o Added typeArguments to explicitConstructorInvocation, e.g. new <String>MyParameterised()
 *    o Added typeArguments to postfixExpression productions for anonymous inner class super
 *      constructor invocation, e.g. new Outer().<String>super()
 *    o Fixed bug in array declarations identified by Geoff Roy
 *
 * This grammar is in the PUBLIC DOMAIN
 */
public class JavaParser extends InternalJavaParser       implements Parser
 {

    /** Indicates JDK version 1.3. */
    public final static int JDK_1_3 = 13;

    /** Indicates JDK version 1.4. */
    public final static int JDK_1_4 = 14;

    /** Indicates JDK version 1.5. */
    public final static int JDK_1_5 = 15;

protected JavaParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
}

public JavaParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected JavaParser(TokenStream lexer, int k) {
  super(lexer,k);
}

public JavaParser(TokenStream lexer) {
  this(lexer,2);
}

public JavaParser(ParserSharedInputState state) {
  super(state);
}
final static String STR_TYPE = "TYPE";
final static String STR_MODIFIERS= "MODIFIERS";
final static String STR_OBJBLOCK = "OBJBLOCK";
final static String STR_EXTENDS_CLAUSE = "EXTENDS_CLAUSE";
final static String STR_INSTANCE_INIT = "INSTANCE_INIT";
final static String STR_PARAMETERS = "PARAMETERS";
final static String STR_CASESLIST = "CASESLIST";
final static String STR_EXPR = "EXPR";
final static String STR_ELIST = "ELIST";

/** Reported identifiers. */
private List _unqualIdents = new NoList(100); // List of <String>

/** Reported identifiers. */
private List _qualIdents = new NoList(30); // List of <String>

/** Used to build qualified identifiers. */
private ANTLRStringBuffer _buf = new ANTLRStringBuffer(100);

/** Used to build identifiers. */
private List _buildList = new ArrayList(10); // List of <String>

private References _references = new References();

/**
 * Were the import nodes of the identifiers we stripped qualification yet
 * added to the tree?
 */
private boolean _insert = true;

/** Holds the imports of the identifiers we stripped. */
private List _strippedImports = new ArrayList(); // List of <JavaNode>

/** Qualified imports. */
private Set _qualImports = new HashSet(20); // Set of <String>

/** Unqualified (wildcard) imports. */
private Set _unqualImports = new HashSet(10); // Set of <String>

/** Logging. */
private final Logger _logger = Logger.getLogger("de.hunsicker.jalopy.language.java");

/** The package name of the parsed source file. */
private String _packageName = "";

/** Strip qualification for qualified identifiers? */
boolean stripQualification;

/**
 * Sets whether qualification of qualified identifiers should be stripped.
 * (not implemented yet)
 * @param strip if <code>true</code> qualification will be stripped.
 */
public void setStripQualification(boolean strip)
{
    // XXX currently disabled as it don't work without accessing the
    // class repository
    //this.stripQualification = strip;
}

/**
 * Indicates whether the qualification stripping is enabled.
 * @return <code>true</code> if the qualification stripping is enabled.
 */
public boolean isStripQualifation()
{
    return this.stripQualification;
}

/**
 * Adds the given identifier to the identifier storage.
 * @param ident identifier.
 */
/*
TODO private void addIdentifier(String ident)
{
    if (ident.indexOf('.') > -1)
    {
        // XXX implement feature: resolve qualified identifiers
        // make them unqualified
        _qualIdents.add(ident);
    }
    else
    {
        _unqualIdents.add(ident);
    }
}
*/
/**
 * {@inheritDoc}
 */
public AST getParseTree()
{
    // insert the import nodes of the stripped identifiers to the tree
    // if not already added
    if (this.stripQualification && _insert)
    {
        if (this.returnAST == null)
            return null;

        AST top = this.returnAST.getFirstChild();

        switch (top.getType())
        {
            case JavaTokenTypes.PACKAGE_DEF:
            case JavaTokenTypes.IMPORT:
                for (int i = 0, size = _strippedImports.size(); i < size; i++)
                {
                    AST tmp = top.getNextSibling();
                    JavaNode imp = (JavaNode)_strippedImports.get(i);
                    imp.setNextSibling(tmp);
                    top.setNextSibling(imp);
                }

                break;

            case JavaTokenTypes.CLASS_DEF:
                break;
        }

        _insert = false;
    }

    return this.returnAST;
}

/**
* Reports the given error.
* @param ex encountered exception.
*/
public void reportError(RecognitionException ex)
{
  Object[] args = { getFilename(), new Integer(ex.line), new Integer(ex.column), ex.getMessage() };
  _logger.l7dlog(Level.ERROR, "PARSER_ERROR", args, ex);
}

private final static Integer UNKNOWN_POSITION = new Integer(0);

/**
* Reports the given error.
* @param message error message.
*/
public void reportError(String message)
{
  Object[] args = { getFilename(), UNKNOWN_POSITION, UNKNOWN_POSITION, message };
  _logger.l7dlog(Level.ERROR, "PARSER_ERROR", args, null);
}

/**
* Reports the given warning.
* @param message warning message.
*/
public void reportWarning(String message)
{
  Object[] args = { getFilename(), UNKNOWN_POSITION,UNKNOWN_POSITION, message };
  _logger.l7dlog(Level.WARN, "PARSER_ERROR", args, null);
}

/**
 * Returns the package name of the parsed source file.
 * @return the package name of the parsed source file. Returns the empty
 *         String if the source file contains no package information.
 */
public String getPackageName()
{
    return _packageName;
}

/**
 * Attaches the hidden tokens from the specified compound statement to its
 * imaginary node.
 *
 * @param node a INSTANCE_INIT node.
 * @param statement a SLIST node.
 */
private void attachStuffBeforeCompoundStatement(JavaNode node, JavaNode statement)
{
    node.setHiddenBefore(statement.getHiddenBefore());
    statement.setHiddenBefore(null);
}

/**
 * Attaches the hidden tokens associated to either the modifiers or keyword to the imaginary node.
 *
 * @param node a CTOR_DEF node.
 * @param modifiers a MODIFIRES node.
 * @param keyword a IDENT node.
 */
private void attachStuffBeforeCtor(JavaNode node, JavaNode modifiers, JavaNode keyword)
{
    JavaNode modifier = getFirstCommentNode(modifiers);

    if (modifier != null)
    {
        node.setHiddenBefore(modifier.getHiddenBefore());
        modifier.setHiddenBefore(null);
    }
    else
    {
        if (keyword.getHiddenBefore() != null)
        {
            node.setHiddenBefore(keyword.getHiddenBefore());
            keyword.setHiddenBefore(null);
        }
    }
}
/**
 * Special node for determing the correct node to move up the comments from. Because
 * a child of the modifier may contain both an anotation node and a modifier node.
 * 
 * @param modifiers
 * @return
 */
private JavaNode getFirstCommentNode(JavaNode modifiers) {
    JavaNode fc = (JavaNode)modifiers.getFirstChild();
    if (fc!=null) {
        if (fc.getType() == JavaTokenTypes.ANNOTATION) {
            fc = (JavaNode) fc.getFirstChild();
            if (fc.getHiddenBefore()==null) {
                fc = (JavaNode) fc.getParent().getNextSibling();
            }
        }
    }
    
    return fc;
}

/**
 * Attaches the hidden tokens associated to either the modifiers or type to the imaginary node.
 *
 * @param node a METHOD_DEF or VARIABLE_DEF node.
 * @param modifiers a MODIFIERS node.
 * @param type a TYPE node.
 */
private void attachStuffBefore(JavaNode node, JavaNode modifiers, JavaNode type)
{
    JavaNode modifier = getFirstCommentNode(modifiers);

    if (modifier != null)
    {
        node.setHiddenBefore(modifier.getHiddenBefore());
        modifier.setHiddenBefore(null);
    }
    else
    {
        for (AST child = type; child != null; child = child.getFirstChild())
        {
            if (child.getFirstChild() == null)
            {
                JavaNode t = (JavaNode)child;

                if (t.getHiddenBefore() != null)
                {
                    node.setHiddenBefore(t.getHiddenBefore());
                    t.setHiddenBefore(null);
                }

                break;
            }
        }
    }
}

/**
 * Returns all unqualified Java identifiers referenced in the file.
 *
 * @return unqualified identifiers. Returns an empty array if no
 *         unqualified identifiers could be found.
 */
public List getUnqualifiedIdents()
{
    return _unqualIdents;
}

/**
 * Returns all qualified Java identifiers referenced in the file.
 *
 * @return qualified identifiers. Returns an empty array if no
 *         qualified identifiers could be found.
 */
public List getQualifiedIdents()
{
    return _qualIdents;
}

/**
 * {@inheritDoc}
 */
public void reset()
{
    _buildList.clear();
    _qualIdents.clear();
    _qualImports.clear();
    _strippedImports.clear();
    _references.reset();
    _unqualIdents.clear();
    _unqualImports.clear();
    _insert = true;
    _packageName = "";
    _buf.setLength(0);

    if (this.inputState != null)
        this.inputState.reset();

    setFilename(Recognizer.UNKNOWN_FILE);
    this.returnAST = null;
}
/**
 * Attaches the items to the correct nodes
 * 
 * @param nodes
 */
protected void attachStuff(JavaNode[] nodes) {
    JavaNode node = nodes[0];
        switch (node.getType()) {
            case JavaTokenTypes.CLASS_DEF:
            case JavaTokenTypes.INTERFACE_DEF:
            case JavaTokenTypes.VARIABLE_DEF:
            case JavaTokenTypes.ENUM_DEF:
            case JavaTokenTypes.ANNOTATION_DEF:
                attachStuffBefore(node,nodes[1],nodes[2]);
            break;
            case JavaTokenTypes.ENUM_CONSTANT_DEF:
                JavaNode t = (JavaNode)node.getFirstChild();
                if (t!=null) {
                    if (t.getHiddenBefore() != null)
                    {
                        node.setHiddenBefore(t.getHiddenBefore());
                        t.setHiddenBefore(null);
                    }
                    t = (JavaNode) t.getNextSibling();
                    if (t!=null) {
                        if (t.getHiddenBefore() != null)
                        {
                            node.setHiddenBefore(t.getHiddenBefore());
                            t.setHiddenBefore(null);
                        }
                    }
                }
                
                
                
            break;
            case JavaTokenTypes.METHOD_DEF:
                attachStuffBefore(node,nodes[1],nodes[2]);
            break;
            case JavaTokenTypes.CTOR_DEF :
                attachStuffBeforeCtor(nodes[0],nodes[1],nodes[2]);
            break;
            case JavaTokenTypes.INSTANCE_INIT :
                attachStuffBeforeCompoundStatement(nodes[0],nodes[1]);
            break;
            	
        }    
}
/**
 * Random access list that prohibits duplicates or null-values.
 */
private final static class NoList
    extends ArrayList
{
    public NoList(int initialSize)
    {
        super(initialSize);
    }

    public boolean add(Object element)
    {
        if (element == null)
            return false;

        if (contains(element))
            return false;

        return super.add(element);
    }

    public void add(int index, Object element)
    {
        if (element == null)
            return;

        if (contains(element))
            return;

        super.add(index, element);
    }

    public Object set(int index, Object element)
    {
        if (element == null)
            return element;

        if (contains(element))
            return element;

        return super.set(index, element);
    }

    // XXX implement addAll
}

 }
