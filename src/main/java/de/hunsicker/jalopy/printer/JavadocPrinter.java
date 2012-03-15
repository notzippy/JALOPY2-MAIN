/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;

import antlr.CommonAST;
import antlr.Token;
import antlr.collections.AST;

import de.hunsicker.jalopy.language.JavaNodeHelper;
import de.hunsicker.jalopy.language.Recognizer;
import de.hunsicker.jalopy.language.TreeWalker;
import de.hunsicker.jalopy.language.antlr.ExtendedToken;
import de.hunsicker.jalopy.language.antlr.JavaLexer;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.language.antlr.JavadocParser;
import de.hunsicker.jalopy.language.antlr.JavadocTokenTypes;
import de.hunsicker.jalopy.language.antlr.Node;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.Environment;
import de.hunsicker.jalopy.storage.Loggers;
import de.hunsicker.util.Lcs;
import de.hunsicker.util.StringHelper;

/**
 * Printer for Javadoc comments.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.19 $
 */
final class JavadocPrinter extends AbstractPrinter {
    /** Singleton. */
    private static final Printer INSTANCE = new JavadocPrinter();

    /** The delimeter we use to separate token chunks of strings. */
    private static final String DELIMETER = "|" /* NOI18N */;

    /** The empty node. */
    private static final AST EMPTY_NODE = new CommonAST();

    /** Indicates that no tag or description was printed yet. */
    private static final int NONE = 0;

    /** Indicates that the description section was printed last. */
    private static final int DESCRIPTION = 1;

    /** The empty String array. */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /** The remove tag obsolete key */
    private static final String KEY_TAG_REMOVE_OBSOLETE = "TAG_REMOVE_OBSOLETE" /* NOI18N */;

    /** The add tag missing key */
    private static final String KEY_TAG_ADD_MISSING = "TAG_ADD_MISSING" /* NOI18N */;

    /** The tag misspelled key  */
    private static final String KEY_TAG_MISSPELLED_NAME = "TAG_MISSPELLED_NAME" /* NOI18N */;

    /** The geenrate comment key */
    private static final String KEY_GENERATE_COMMENT = "GENERATE_COMMENT" /* NOI18N */;

    // TODO private static final String TAG_OPARA = "<p>" /* NOI18N */;
    /** The close paragraph key */
    private static final String TAG_CPARA = "</p>" /* NOI18N */;

    /** The pattern for keys */
    protected static Pattern _pattern = Pattern.compile(
        "(?: )*([a-zA-z0-9_.]*)\\s*(.*)" /* NOI18N */);

    /** The break iterator to use for realigning the comment texts. */
    private ThreadLocal _stringBreaker = new ThreadLocal() {
        protected Object initialValue() {
            return new BreakIterator();
        } // end initialValue()
    } // end new
    ;

    /**
     * The break iterator
     *
     * @version 1.0
     */
    private static class BreakIterator {
        /** TODO DOCUMENT ME! */
        private static final int WHITESPACE = 1;

        /** TODO DOCUMENT ME! */
        private static final int BREAK = 2;

        /** TODO DOCUMENT ME! */
        public static final int DONE = -10;

        /** TODO DOCUMENT ME! */
        private static final String TAG_BREAK = "<br>" /* NOI18N */;

        /** TODO DOCUMENT ME! */
        private static final String TAG_BREAK_WELL = "<br/>" /* NOI18N */;

        /** TODO DOCUMENT ME! */
        int _type;

        /** TODO DOCUMENT ME! */
        private String _text;

        /** TODO DOCUMENT ME! */
        private int _end = -1;

        /** TODO DOCUMENT ME! */
        private int _pos = -1;

        /**
         * TODO Creates a new BreakIterator object.
         */
        public BreakIterator() {}

        /**
         * Returns the boundary following the current boundary.
         *
         * @return the character index of the next text boundary or {@link #DONE} if all
         *         boundaries have been returned.
         */
        public int getBreakType() {
            return _type;
        } // end getBreakType()

        /**
         * TODO DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int next() {
            _type = WHITESPACE;
            _pos = _text.indexOf(' ', _end + 1);

            if (_pos > -1) {
                int tab = _text.indexOf('\t', _end + 1);

                if ((tab > -1) && (tab < _pos)) {
                    _pos = tab;
                } // end if

                int br = _text.indexOf(TAG_BREAK, _end + 1);

                if (br == -1) {
                    br = _text.indexOf(TAG_BREAK_WELL, _end + 1);
                } // end if

                if ((br > -1) && (br < _pos)) {
                    _pos = br;
                    _type = BREAK;
                } // end if
            } // end if

            if (_pos == -1) {
                return DONE;
            } // end if

            _end = _pos;

            return _pos;
        } // end next()

        /**
         * TODO DOCUMENT ME!
         */
        public void reset() {
            _text = null;
            _end = -1;
            _pos = -1;
        } // end reset()

        /**
         * TODO DOCUMENT ME!
         *
         * @param text DOCUMENT ME!
         */
        public void setText(String text) {
            _text = text;
        } // end setText()
    } // end BreakIterator

    /**
     * Creates a new JavadocPrinter object.
     */
    private JavadocPrinter() {}

    /**
     * Returns the sole instance of this class.
     *
     * @return sole instance of this class.
     */
    public static final Printer getInstance() {
        return INSTANCE;
    } // end getInstance()

    /**
     * This method is <strong>NOT</strong> implemented. Use {@link
     * #print(AST,AST,NodeWriter)} instead.
     *
     * @param node node to print.
     * @param out stream to print to.
     *
     * @throws UnsupportedOperationException as this method is not supported.
     */
    public void print(AST        node,
                      NodeWriter out) {
        throw new UnsupportedOperationException("use print(AST, AST, NodeWriter) instead");
    } // end print()

    /**
     * Prints the given Javadoc comment.
     *
     * @param node node the comment belongs to.
     * @param comment the comment to print.
     * @param out to output to.
     *
     * @throws IOException if an I/O error occured.
     */
    public void print(AST        node,
                      AST        comment,
                      NodeWriter out)
               throws IOException {
        // Javadoc comments always start on a new line
        if (!out.newline) {
            out.printNewline();
        } // end if
        out.javadocIndent = 0;

        JavaNode parentParent = null;

        parentParent = ((JavaNode)node).getParent();
        if (parentParent != null) {
            parentParent = parentParent.getParent();
        } // end if

        boolean reformatComment = (node.getType() == JavaTokenTypes.CLASS_DEF) ||
                                (node.getType()==JavaTokenTypes.ENUM_CONSTANT_DEF) ||
                                (node.getType()==JavaTokenTypes.ENUM_DEF) ||
                                  ((node.getType() == JavaTokenTypes.VARIABLE_DEF) &&
                                  ((parentParent.getType() == JavaTokenTypes.CLASS_DEF) ||
                                  (parentParent.getType() == JavaTokenTypes.INTERFACE_DEF) ||
                                  (parentParent.getType() == JavaTokenTypes.ENUM_DEF))) ||
                                  (node.getType() == JavaTokenTypes.METHOD_DEF) ||
                                  (node.getType() == JavaTokenTypes.CTOR_DEF);

        boolean formatJavadoc = AbstractPrinter.settings.getBoolean(
            ConventionKeys.COMMENT_JAVADOC_PARSE,
            ConventionDefaults.COMMENT_JAVADOC_PARSE);

        // output an auto-generated comment
        if (BasicDeclarationPrinter.GENERATED_COMMENT.equals(comment.getText())) {
            String[] lines = StringHelper.split(comment.getFirstChild().getText(), DELIMETER);

            if (lines.length > 0) {
                out.state.args[0] = out.getFilename();
                out.state.args[1] = new Integer(out.line);
                out.state.args[2] = new Integer(out.getIndentLength() + 1);

                for (int i = 0, size = lines.length - 1; i < size; i++) {
                    out.print(lines[i], JavadocTokenTypes.JAVADOC_COMMENT);
                    out.printNewline();
                } // end for

                out.print(lines[lines.length - 1], JavadocTokenTypes.JAVADOC_COMMENT);

                Loggers.PRINTER_JAVADOC.l7dlog(
                    Level.INFO,
                    KEY_GENERATE_COMMENT,
                    out.state.args,
                    null);
            } // end if
        } // end if
        else if (!reformatComment) {
            int currentIndent = out.indentLevel;

            out.indentLevel = 0;
            out.print(comment.getText(), comment.getType());
            out.indentLevel = currentIndent;
        } // end else if

        // output Javadoc comment as multi-comment
        else if (!formatJavadoc) {
            String[] lines = StringHelper.split(comment.getText(), out.originalLineSeparator);

            for (int i = 0; i < lines.length; i++) {
                if (lines[i].trim().startsWith("*")) {
                    out.print(" " + lines[i].trim(), JavadocTokenTypes.JAVADOC_COMMENT);
                } // end if
                else {
                    out.print(lines[i], JavadocTokenTypes.JAVADOC_COMMENT);
                } // end else
                if ((i + 1) != lines.length) {
                    out.printNewline();
                }
            } // end for
        } // end else if
        else {
            // The javadoc hasnt been parsed so...
            Recognizer _recognizer = out.getCompositeFactory().getRecognizer();
            String     t           = comment.getText();

            _recognizer.setLine(node.getLine());
            _recognizer.setColumn(node.getColumn());
            _recognizer.parse(t, out.filename);
            comment = _recognizer.getParseTree();

//                        // ignore empty comments
//                        if (comment != JavadocParser.EMPTY_JAVADOC_COMMENT)
//                        {
//                            node = _factory.getExtendedTokenFactory().create(JavaTokenTypes.JAVADOC_COMMENT, t);
//                            ((ExtendedToken)node).comment = comment;
//                            comment.setText(t);
//                            
//                        }
//                        else {
//                            return; //node.setType(Token.SKIP);
//                        }
            out.print(getTopString(node.getType()), JavadocTokenTypes.JAVADOC_COMMENT);

            String bottomText = getBottomString(node.getType());
            String asterix    = bottomText.substring(0, bottomText.indexOf('*') + 1);

            asterix = getAsterix();

            AST    firstTag   = null;
            String commentText = t;

            if (!AbstractPrinter.settings.getBoolean(
                ConventionKeys.COMMENT_JAVADOC_PARSE_DESCRIPTION,
                ConventionDefaults.JAVADOC_PARSE_DESCRIPTION)) {
                TestNodeWriter dummy = out.testers.get();

                firstTag = printDescriptionSection(node, comment, asterix, dummy);
                boolean hasFirstTag = firstTag != EMPTY_NODE; 
                if (hasFirstTag) {
                    commentText = commentText.substring(
                        0,
                        commentText.indexOf(firstTag.getText()));
                } // end if
                out.testers.release(dummy);

                String[] lines = StringHelper.split(commentText, out.originalLineSeparator);
                
                if (lines.length == 1) {
                    lines[0] = lines[0].substring(3, lines[0].length() - 2);
                    out.print(lines[0], JavadocTokenTypes.JAVADOC_COMMENT);
                } // end if
                else if (lines.length > 0) {
                    lines[0] = lines[0].substring(3);
                    int i=0;
                    if (!out.newline) {
                        out.printNewline();
                    }
                    for (int size = lines.length - 1; (i < size ) ; i++) {
                        String newline = " " + lines[i].trim();
                        
                        if (size-1==i && newline.endsWith("*") || newline.length()==1)
                            continue;
                        out.print(newline, JavadocTokenTypes.JAVADOC_COMMENT);
                        out.printNewline();
                    } // end for
                } // end else if
            } // end if
            else {
                firstTag = printDescriptionSection(node, comment, asterix, out);
            } // end else

            // any tags to print or check needed?
            if ((firstTag != EMPTY_NODE) ||
                AbstractPrinter.settings.getBoolean(
                ConventionKeys.COMMENT_JAVADOC_CHECK_TAGS,
                ConventionDefaults.COMMENT_JAVADOC_CHECK_TAGS)) {
                printTagSection(node, comment, firstTag, asterix, out);
            } // end if

            out.print(bottomText, JavadocTokenTypes.JAVADOC_COMMENT);
        } // end else
    } // end print()

    /**
     * Returns all valid type names for the given node found as a sibling of the given
     * node (i.e. all exception or parameter types depending on the node).
     *
     * @param node node to search. Either of type METHOD_DEF or CTOR_DEF.
     * @param type type of the node to return the identifiers for. Either PARAMETERS or
     *        LITERAL_throws.
     *
     * @return the valid types names. Returns an empty list if no names were found for the
     *         given type.
     */
    static List getValidTypeNames(AST node,
                                  int type) {
        // TODO Correct for generics
        switch (type) {
            case JavaTokenTypes.PARAMETERS: {
                switch (node.getType()) {
                    case JavaTokenTypes.METHOD_DEF:
                    case JavaTokenTypes.CTOR_DEF:
                        break;
                    case JavaTokenTypes.INTERFACE_DEF:
                    case JavaTokenTypes.CLASS_DEF:
                        return appendTypeNames(new ArrayList(4), node);
                    default:
                        return Collections.EMPTY_LIST;
                } // end switch

                List names = new ArrayList(4);

                appendTypeNames(names, node);

                for (AST child = JavaNodeHelper.getFirstChild(node, type).getFirstChild();
                     child != null; child = child.getNextSibling()) {
                    switch (child.getType()) {
                        case JavaTokenTypes.PARAMETER_DEF:
                        case JavaTokenTypes.VARIABLE_PARAMETER_DEF:
                            names.add(
                                JavaNodeHelper.getFirstChild(child, JavaTokenTypes.IDENT)
                                              .getText());
                            break;
                    } // end switch
                } // end for

                return names;
            } // end case
            case JavaTokenTypes.LITERAL_throws: {
                final List names      = new ArrayList(3);
                AST        exceptions = JavaNodeHelper.getFirstChild(node, type);

                if (exceptions != null) {
                    // add all clauses of the exception specification
                    for (AST child = exceptions.getFirstChild(); child != null;
                         child = child.getNextSibling()) {
                        switch (child.getType()) {
                            case JavaTokenTypes.IDENT:
                                names.add(child.getText());
                                break;
                        } // end switch
                    } // end for
                } // end if

/**
                 * @todo make this user configurable
                 */

                // add all exceptions actually thrown within the method body
                TreeWalker walker = new TreeWalker() {
                    public void visit(AST aNode) {
                        switch (aNode.getType()) {
                            case JavaTokenTypes.LITERAL_throw:
                                switch (aNode.getFirstChild().getFirstChild().getType()) {
                                    case JavaTokenTypes.LITERAL_new:

                                        String name = aNode.getFirstChild().getFirstChild()
                                                           .getFirstChild().getText();

                                        //JavaNode slist =
                                        //    ((JavaNode) aNode).getParent();

                                        // only add if the exception is not
                                        // enclosed within a try/catch block
                                        if (isEnclosedWithTry((JavaNode)aNode)) {
                                            break;
                                        } // end if

                                        if (!names.contains(name)) {
                                            names.add(name);
                                        } // end if

                                        break;
                                } // end switch
                                break;
                        } // end switch
                    } // end visit()
                } // end new
                ;

                walker.walk(node);

                return names;
            } // end case
        } // end switch

        return Collections.EMPTY_LIST;
    } // end getValidTypeNames()

    /**
     * Determines whether the given node is enclosed with a try/catch block.
     *
     * @param node a LITERAL_throw node.
     *
     * @return <code>true</code> if the node is enclosed with a try/catch block.
     *
     * @since 1.0b9
     */
    static boolean isEnclosedWithTry(JavaNode node) {
        JavaNode parent = node.getParent();

        switch (parent.getType()) {
            case JavaTokenTypes.METHOD_DEF:
            case JavaTokenTypes.CTOR_DEF:
                return false;
            case JavaTokenTypes.LITERAL_try:

                AST next = parent.getFirstChild().getNextSibling();

                if (next != null) {
                    switch (next.getType()) {
                        case JavaTokenTypes.LITERAL_catch:
                            return true;
                    } // end switch
                } // end if

                return false;
            default:
                switch (parent.getType()) {
                    case JavaTokenTypes.LITERAL_catch:
                    case JavaTokenTypes.LITERAL_finally:
                        return isEnclosedWithTry(parent.getParent());
                    default:
                        return isEnclosedWithTry(parent);
                } // end switch
        } // end switch
    } // end isEnclosedWithTry()

    /**
     * Appends valid parameter type names to the list
     *
     * @param names The list to append to
     * @param node The node we are working with
     *
     * @return
     */
    private static List appendTypeNames(List names,
                                        AST  node) {
        if (JavaNodeHelper.getFirstChild(node, JavaTokenTypes.TYPE_PARAMETERS) != null) {
            for (AST child = JavaNodeHelper.getFirstChild(
                node,
                JavaTokenTypes.TYPE_PARAMETERS).getFirstChild(); child != null;
                 child = child.getNextSibling()) {
                if (child.getType() == JavaTokenTypes.TYPE_PARAMETER) {
                    names.add(
                        "<" +
                        JavaNodeHelper.getFirstChild(child, JavaTokenTypes.IDENT).getText() +
                        ">");
                } // end if
            } // end for
        } // end if
        return names;
    } // end appendTypeNames()

    /**
     * Checks whether the given METHOD_DEF node contains a return tag or not and adds
     * one if necessary.
     *
     * @param node a METHOD_DEF node.
     * @param returnNode the found returnNode, may be <code>null</code>.
     * @param out current writer.
     *
     * @return the return tag, returns <code>null</code> if the method does not need a return
     *         tag.
     */
    private AST checkReturnTag(AST        node,
                               AST        returnNode,
                               NodeWriter out) {
        boolean needTag = false; // need @return tag?

LOOP: 
        for (AST child = node.getFirstChild(); child != null;
             child = child.getNextSibling()) {
            switch (child.getType()) {
                case JavaTokenTypes.TYPE:
                    if (child.getFirstChild().getType() != JavaTokenTypes.LITERAL_void) {
                        needTag = true;

                        break LOOP;
                    } // end if
                    break;
            } // end switch
        } // end for

        if (returnNode != null) {
            if (!needTag) {
                out.state.args[0] = out.getFilename();
                out.state.args[1] = new Integer(out.line);
                out.state.args[2] = new Integer(out.column);
                out.state.args[3] = "@return" /* NOI18N */;
                out.state.args[4] = new Integer(((Node)returnNode).getStartLine());
                returnNode = null;
                Loggers.PRINTER_JAVADOC.l7dlog(
                    Level.WARN,
                    KEY_TAG_REMOVE_OBSOLETE,
                    out.state.args,
                    null);
            } // end if
        } // end if
        else {
            if (needTag) {
                returnNode = createTag(
                    node,
                    JavadocTokenTypes.TAG_RETURN,
                    null,
                    out.environment,
                    out);
            } // end if
        } // end else

        return returnNode;
    } // end checkReturnTag()

    /**
     * Makes sure that the tag names matches the parameter names of the node. Updates
     * the given list as it adds missing or removes obsolete tags.
     *
     * @param node node the comment belongs to.
     * @param tags tags to print.
     * @param type tag type. Either LITERAL_throws or PARAMETERS.
     * @param asterix string to use as leading asterix.
     * @param last type of the tag that was printed last.
     * @param out stream to write to.
     */
    private void checkTags(AST        node,
                           List       tags,
                           int        type,
                           String     asterix,
                           int        last,
                           NodeWriter out) {
        switch (type) {
            case JavaTokenTypes.LITERAL_throws:
                if (!AbstractPrinter.settings.getBoolean(
                    ConventionKeys.COMMENT_JAVADOC_CHECK_TAGS_THROWS,
                    ConventionDefaults.COMMENT_JAVADOC_CHECK_TAGS_THROWS)) {
                    return;
                } // end if
                break;
        } // end switch

        // get the actual names of the parameters or exceptions
        List validNames     = getValidTypeNames(node, type);
        List validNamesCopy = new ArrayList(validNames);

        int capacity = (int)(tags.size() * 1.3);

        // will contain the correct tags
        Map correct = new HashMap(capacity);

        // will contain misspelled, obsolete or mispositioned tags
        List wrongOrObsolete = new ArrayList(capacity);

        // split the tag list in correct tags and wrong/obsolete ones
        for (int i = 0, size = tags.size(); i < size; i++) {
            AST tag = (AST)tags.get(i);

            if (tag.getFirstChild() != null) {
                String description = tag.getFirstChild().getText().trim();
                String name        = null;
                int    offset      = -1;

                // determine the first word of the description: the parameter name
                if ((offset = description.indexOf(' ')) > -1) {
                    name = description.substring(0, offset);
                } // end if
                else {
                    name = description;
                } // end else

                if (validNamesCopy.contains(name)) {
                    correct.put(name, tag);
                    validNamesCopy.remove(name);
                } // end if
                else {
                    wrongOrObsolete.add(tag);
                } // end else
            } // end if
            else {
                switch (tag.getType()) {
                    case JavadocTokenTypes.TAG_PARAM:
                    case JavadocTokenTypes.TAG_SEE:
                    case JavadocTokenTypes.TAG_THROWS:
                    case JavadocTokenTypes.TAG_EXCEPTION:
                        wrongOrObsolete.add(tag);
                        break;
                } // end switch
            } // end else
        } // end for

        // create an empty list with as many empty slots as needed
        List result = new ArrayList(validNames);

        Collections.fill(result, null);

        // add all correct tags at the correct position
        for (Iterator i = correct.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();

            result.set(validNames.indexOf(entry.getKey()), entry.getValue());
        } // end for

        // either we have too many or too less tags
        if (validNames.size() != tags.size()) {
/**
             * @todo the situation here can be ambigious if we have an obsolete tag AND a
             *       misspelled or missing name in which case we could end up renaming an
             *       obsolete tag name but deleting the misspelled or missing name tag Maybe
             *       we should change this routine to only add missing params and spit out
             *       warnings if we possibly found obsolete tags or add a switch to disable
             *       the removal
             */

            // fill gaps with wrong or obsolete tags until no more slots left
            for (int i = 0, size = wrongOrObsolete.size(); i < size; i++) {
                int next = getNextEmptySlot(result);

                // no more empty slots, spit out warnings for all tags which
                // are skipped, e.g. essentially removed
                if (next == -1) {
                    for (int j = i, s = wrongOrObsolete.size(); j < s; j++) {
                        AST tag = (AST)wrongOrObsolete.get(j);

                        out.state.args[0] = out.getFilename();
                        out.state.args[1] = new Integer(out.line);
                        out.state.args[2] = new Integer(out.column);
                        out.state.args[3] = tag.getText();
                        out.state.args[4] = new Integer(((Node)tag).getStartLine());
                        out.state.args[5] = tag;

                        Loggers.PRINTER_JAVADOC.l7dlog(
                            Level.WARN,
                            KEY_TAG_REMOVE_OBSOLETE,
                            out.state.args,
                            null);
                    } // end for

                    break;
                } // end if

                AST tag = (AST)wrongOrObsolete.get(i);

                // if the tag name was mispelled, it has been corrected, so add
                // it to the list
                correctTagName(tag, validNames, next, asterix, last, out);
                result.set(next, tag);
            } // end for

            int emptySlots = validNames.size() - getEmptySlotCount(result);

            if (emptySlots < validNames.size()) {
                // create missing tags
                for (int i = emptySlots, size = validNames.size(); i < size; i++) {
                    int    next    = getNextEmptySlot(result);
                    String name    = (String)validNames.get(next);
                    AST    tag     = null;
                    String tagName = null;

                    switch (type) {
                        case JavaTokenTypes.PARAMETERS:
                            tag = createTag(
                                node,
                                JavadocTokenTypes.TAG_PARAM,
                                name,
                                out.environment,
                                out);
                            result.set(next, tag);
                            tagName = "@param" /* NOI18N */;

                            break;
                        case JavaTokenTypes.LITERAL_throws:
                            result.set(
                                next,
                                tag = createTag(
                                    node,
                                    JavadocTokenTypes.TAG_EXCEPTION,
                                    name,
                                    out.environment,
                                    out));
                            tagName = "@throws" /* NOI18N */;

                            break;
                    } // end switch

                    out.state.args[0] = out.getFilename();
                    out.state.args[1] = new Integer(
                        out.line + next + (shouldHaveNewlineBefore(tag, last) ? 1 : 0));
                    out.state.args[2] = new Integer(
                        out.getIndentLength() + asterix.length() + 1);
                    out.state.args[3] = tagName;
                    out.state.args[4] = name;
                    Loggers.PRINTER_JAVADOC.l7dlog(
                        Level.WARN,
                        KEY_TAG_ADD_MISSING,
                        out.state.args,
                        null);
                } // end for
            } // end if

            // update the tag list
            tags.clear();
            tags.addAll(result);
        } // end if

        // we have the right number of tags, but do they have the correct names
        // and positions?
        else {
            // TODO List c = new ArrayList(correct.values());

            /*for (int i = 0, size = tags.size(); i < size; i++)
               {
               AST tag = (AST)tags.get(i);
               // we're only interested in the missing/wrong tags
               if (c.contains(tag))
               {
               System.err.println("correct " + tag);
               //result.set(i, tag);
               System.err.println(i + " " + c.indexOf(tag));
               continue;
               }
               int next = getNextEmptySlot(result);
               result.set(next, tags.get(i));
               }*/
            for (int i = 0, size = result.size(); i < size; i++) {
                AST tag = (AST)result.get(i);

                // missing or mispelled tag
                if ((tag == null) || (tag.getFirstChild() == null)) {
                    AST wrongTag = (AST)wrongOrObsolete.remove(0);

                    correctTagName(wrongTag, validNames, i, asterix, last, out);
                    tag = wrongTag;
                } // end if

                // make sure the tag is at the correct position
                tags.set(i, tag);

                /*
                   AST child = tag.getFirstChild();
                   if (child != null)
                   {
                   String text = child.getText().trim();
                   String name = null;
                   int offset = -1;
                   // determine the first word of the description: the
                   // parameter name
                   if ((offset = text.indexOf(' ')) > -1)
                   {
                   name = text.substring(0, offset);
                   }
                   else
                   {
                   name = text;
                   offset = text.length();
                   }
                   int pos = validNames.indexOf(name);
                   // if we can't find the name in our list or if it does not
                   // appear at the correct position, we rename it
                   if ((pos == -1) || (pos != i))
                   {
                   String validName = (String)validNames.get(i);
                   out.state.args[0] = out.getFilename();
                   out.state.args[1] = new Integer(out.line + i);
                   out.state.args[2] = new Integer(out.getIndentLength()
                                  + asterix.length() + 1);
                   out.state.args[3] = name;
                   out.state.args[4] = validName;
                   Loggers.PRINTER_JAVADOC.l7dlog(Level.WARN,
                                          KEY_TAG_MISSPELLED_NAME,
                                          out.state.args, null);
                   child.setText(SPACE + validName + text.substring(offset));
                   }
                   }*/
            } // end for
        } // end else
    } // end checkTags()

    /**
     * Corrects the tag name of the given Javadoc standard tag.
     *
     * @param wrongTag the tag node to correct.
     * @param validNames list with all valid tag names for the method/ctor.
     * @param index current index in the list of valid names.
     * @param asterix string to use as leading asterix.
     * @param last type of tag that was printed last.
     * @param out stream to write to.
     *
     * @return index of the corrected tag in the list with valid names.
     */
    private int correctTagName(AST        wrongTag,
                               List       validNames,
                               int        index,
                               String     asterix,
                               int        last,
                               NodeWriter out) {
        AST child = wrongTag.getFirstChild();

        if (child != null) {
            // get the whole description text
            String text    = child.getText().trim();
            String oldName = null;
            int    offset  = -1;

            // determine the first word of the text: the parameter name
            if ((offset = text.indexOf(' ')) > -1) {
                oldName = text.substring(0, offset);
            } // end if
            else {
                oldName = text;
                offset = text.length();
            } // end else

            String match   = getMatch(oldName, validNames);
            String newName = null;

            if (match != null) {
                newName = match;
                index = validNames.indexOf(match);
            } // end if
            else {
                newName = (String)validNames.get(index);
            } // end else

            out.state.args[0] = out.getFilename();

            out.state.args[1] = new Integer(
                out.line + index + (shouldHaveNewlineBefore(wrongTag, last) ? 1 : 0));
            out.state.args[2] = new Integer(out.getIndentLength() + asterix.length() + 1);
            out.state.args[3] = oldName;
            out.state.args[4] = newName;

            Loggers.PRINTER_JAVADOC.l7dlog(
                Level.WARN,
                KEY_TAG_MISSPELLED_NAME,
                out.state.args,
                null);

            text = SPACE + newName + text.substring(offset);
            child.setText(text);
        } // end if
        else {
            String newName = (String)validNames.get(index);
            String text    = SPACE + newName;
            Node   c       = (Node)out.getJavaNodeFactory()
                                      .create(JavadocTokenTypes.PCDATA, text);

            wrongTag.setFirstChild(c);
        } // end else

        return index;
    } // end correctTagName()

    /**
     * Creates a standard tag of the given type.
     *
     * @param node node to create the tag for.
     * @param type type of the tag.
     * @param typeName name of the type.
     * @param environment the environment.
     * @param out DOCUMENT ME!
     *
     * @return the created standard tag.
     */
    private AST createTag(AST         node,
                          int         type,
                          String      typeName,
                          Environment environment,
                          NodeWriter  out) {
        AST tag = out.getJavaNodeFactory().create(type, EMPTY_STRING);

        if (typeName != null) {
            AST para = out.getJavaNodeFactory().create(
                JavadocTokenTypes.PCDATA,
                getTagTemplateText(node, typeName, type, environment));

            tag.setFirstChild(para);
        } // end if
        else {
            AST description = out.getJavaNodeFactory().create(
                JavadocTokenTypes.PCDATA,
                getTagTemplateText(node, null, type, environment));

            tag.setFirstChild(description);
        } // end else

        return tag;
    } // end createTag()

    /**
     * Performs a general print things like when to put new lines in is decided here
     *
     * @param out The output writer
     * @param nodeType The node type
     * @param text The text
     * @param asterix The asteric string
     *
     * @throws IOException If an error occurs
     */
    private void generalPrint(NodeWriter out,
                              int        nodeType,
                              String     text,
                              String     asterix)
                       throws IOException {
        generalPrint(out, null, nodeType, text, asterix, -1);
    } // end generalPrint()

    /**
     * Performs a general print things like when to put new lines in is decided here
     *
     * @param out The output writer
     * @param node The node
     * @param text The text
     * @param asterix The asteric string
     *
     * @throws IOException If an error occurs
     */
    private void generalPrint(NodeWriter out,
                              AST        node,
                              String     text,
                              String     asterix)
                       throws IOException {
        generalPrint(out, node, -1, text, asterix, -1);
    } // end generalPrint()

    /**
     * Performs a general print things like when to put new lines in is decided here
     *
     * @param out The output writer
     * @param node The node
     * @param nodeType The node type
     * @param text The text
     * @param asterix The asteric string
     * @param length The length to autoindent
     *
     * @throws IOException If an error occurs
     */
    private void generalPrint(NodeWriter out,
                              AST        node,
                              int        nodeType,
                              String     text,
                              String     asterix,
                              int        length)
                       throws IOException {
        boolean newLine      = false;
        boolean newLineAfter = false;

        if (length > 0) {
            length += (out.javadocIndent * out.indentSize);
        } // end if
        else {
            length = out.javadocIndent * out.indentSize;
        } // end else
        if (nodeType < 0) {
            nodeType = node.getType();
        } // end if
        switch (nodeType) {
            case JavadocTokenTypes.OTD:
            case JavadocTokenTypes.OTH:
                if (!out.newline) {
                    newLine = true;
                } // end if
                out.javadocIndent++;
                length += out.indentSize;
                break;
            case JavadocTokenTypes.HR:
                newLineAfter = true;
            case JavadocTokenTypes.OH1:
            case JavadocTokenTypes.OH2:
            case JavadocTokenTypes.OH3:
            case JavadocTokenTypes.OH4:
            case JavadocTokenTypes.OH5:
            case JavadocTokenTypes.OH6:
            case JavadocTokenTypes.OLITEM:
            case JavadocTokenTypes.O_TR:
                if (!out.newline) {
                    newLine = true;
                } // end if
                break;
            case JavadocTokenTypes.OTABLE:
            case JavadocTokenTypes.OOLIST:
            case JavadocTokenTypes.OULIST:
                out.javadocIndent++;
                if (!out.newline) {
                    newLine = true;
                } // end if
                break;
            case JavadocTokenTypes.COLIST:
            case JavadocTokenTypes.CULIST:
                newLine = newLineAfter = true;
            case JavadocTokenTypes.CTABLE:
                if (!out.newline) {
                    newLine = true;
                } // end if
                out.javadocIndent--;
                length -= out.indentSize;
                newLineAfter = true;
                break;
            case JavadocTokenTypes.CTD:
            case JavadocTokenTypes.CTH:
                length -= out.indentSize;
                out.javadocIndent--;
                break;
            case JavadocTokenTypes.C_TR:
                if (!out.newline) {
                    newLine = true;
                } // end if
                break;
        } // end switch

        int maxwidth = AbstractPrinter.settings.getInt(
            ConventionKeys.LINE_LENGTH,
            ConventionDefaults.LINE_LENGTH) - 3 - out.getIndentLength() - length;

        if (out.newline) {
            out.print(asterix, JavadocTokenTypes.JAVADOC_COMMENT);
            if (length > -1) {
                out.print(out.getString(length + 1), JavaTokenTypes.WS);
            } // end if
        } // end if

        String[] lines = split(text, maxwidth, out.column, true);

        // Print the forced new line
        if (newLine) {
            out.printNewline();
            out.print(asterix, JavadocTokenTypes.JAVADOC_COMMENT);
            if (length > -1) {
                out.print(out.getString(length + 1), JavaTokenTypes.WS);
            } // end if
        } // end if

        if (lines.length > 1) {
            for (int x = 0; x < lines.length; x++) {
                out.print(lines[x], nodeType);
                if ((x + 1) < lines.length) {
                    out.printNewline();
                    out.print(asterix, JavadocTokenTypes.JAVADOC_COMMENT);
                    if (length > 0) {
                        out.print(out.getString(length + 1), JavaTokenTypes.WS);
                    } // end if
                } // end if
            } // end for
        } // end if
        else if (lines.length == 1) {
            out.print(lines[0], nodeType);
        } // end else if

        if (newLineAfter) {
            out.printNewline();
        } // end if
    } // end generalPrint()

    /**
     * Returns the string to start successive Javadoc comment lines with.
     *
     * @return the string to start successive Javadoc comment lines with.
     *
     * @since 1.0b8
     */
    private String getAsterix() {
        String text        = AbstractPrinter.settings.get(
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM,
            ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM);
        int    asterix     = text.indexOf('*');
        int    description = StringHelper.indexOfNonWhitespace(text, asterix + 1);

        if (description > -1) {
            return text.substring(0, description);
        } // end if
        else if (asterix > -1) {
            return text.substring(0, asterix + 1);
        } // end else if
        else {
            return EMPTY_STRING;
        } // end else
    } // end getAsterix()

    /**
     * Returns the string to end a Javadoc comment with.
     *
     * @param type type of the node to get the ending comment string for.
     *
     * @return the string to end a Javadoc comment with (usually <code> &#42;/</code>).
     *
     * @since 1.0b8
     */
    private String getBottomString(int type) {
        switch (type) {
            case JavaTokenTypes.METHOD_DEF:
                return AbstractPrinter.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_BOTTOM,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_METHOD_BOTTOM);
            case JavaTokenTypes.CTOR_DEF:
                return AbstractPrinter.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_BOTTOM,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_CTOR_BOTTOM);
            case JavaTokenTypes.VARIABLE_DEF: {
                String text = AbstractPrinter.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_VARIABLE,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_VARIABLE).trim();

                int    offset = text.lastIndexOf(DELIMETER);

                if (offset > -1) {
                    return text.substring(offset + 1);
                } // end if
                return " */";
            } // end case
            case JavaTokenTypes.CLASS_DEF: {
                String text = AbstractPrinter.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CLASS,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_CLASS).trim();

                int    offset = text.lastIndexOf(DELIMETER);

                if (offset > -1) {
                    return text.substring(offset + 1);
                } // end if
                return " */";
            } // end case
            case JavaTokenTypes.INTERFACE_DEF: {
                String text   = AbstractPrinter.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_INTERFACE,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_INTERFACE).trim();
                int    offset = text.lastIndexOf(DELIMETER);

                if (offset > -1) {
                    return text.substring(offset + 1);
                } // end if
                return " */";
            } // end case
            default:
                return " */";
        } // end switch
    } // end getBottomString()

    /**
     * Returns the number of empty slots in the given list.
     *
     * @param list list to search.
     *
     * @return the number of empty slots. Returns <code>0</code> if no empty slots were found.
     */
    private int getEmptySlotCount(List list) {
        int empty = 0;

        for (int i = 0, size = list.size(); i < size; i++) {
            if (list.get(i) == null) {
                empty++;
            } // end if
        } // end for

        return empty;
    } // end getEmptySlotCount()

    /**
     * Searchs the given list for a string similiar to the given string. The match is
     * kind of 'fuzzy' as the strings must not be exactly similar.
     *
     * @param string the string to match.
     * @param list list with strings to match against.
     *
     * @return Returns <code>null</code> if no match could be found.
     */
    private String getMatch(String string,
                            List   list) {
        if (string == null) {
            return null;
        } // end if

        if (list.contains(string)) {
            return string;
        } // end if

        Lcs lcs = new Lcs();

        for (int i = 0, size = list.size(); i < size; i++) {
            String tag = (String)list.get(i);

            lcs.init(string, tag);

            double similarity = lcs.getPercentage();

/**
             * @todo evaluate whether this is appropriate
             */
            if (similarity > 75.0) {
                return tag;
            } // end if
        } // end for

        return null;
    } // end getMatch()

    /**
     * Returns the index of the next empty slot in the given list. An empty slot has a
     * value of <code>null</code>.
     *
     * @param list list to search.
     *
     * @return index position of the next empty slot. Returns <code>-1</code> if the list
     *         contains no empty slots.
     */
    private int getNextEmptySlot(List list) {
        int result = -1;

        for (int i = 0, size = list.size(); i < size; i++) {
            if (list.get(i) == null) {
                return i;
            } // end if
        } // end for

        return result;
    } // end getNextEmptySlot()

    /**
     * Returns the parameter count of the given METHOD_DEF or CTOR_DEF node.
     *
     * @param node either a METHOD_DEF or CTOR_DEF node.
     *
     * @return the number of parameters in the parameter list of the given node.
     */
    private int getParamCount(AST node) {
        int count = 0;

        if (JavaNodeHelper.getFirstChild(node, JavaTokenTypes.PARAMETERS) != null) {
            for (AST param = JavaNodeHelper.getFirstChild(node, JavaTokenTypes.PARAMETERS)
                                           .getFirstChild(); param != null;
                 param = param.getNextSibling()) {
                count++;
            } // end for
        } // end if
        if (JavaNodeHelper.getFirstChild(node, JavaTokenTypes.TYPE_PARAMETERS) != null) {
            for (AST child = JavaNodeHelper.getFirstChild(
                node,
                JavaTokenTypes.TYPE_PARAMETERS).getFirstChild(); child != null;
                 child = child.getNextSibling()) {
                if (child.getType() == JavaTokenTypes.TYPE_PARAMETER) {
                    count++;
                } // end if
            } // end for
        } // end if

        return count;
    } // end getParamCount()

    /**
     * Returns the template text for the given parameter type.
     *
     * @param node the node to return the template text for.
     * @param typeName the type name of the node. Given the TAG_PARAM type, this is the type
     *        name of the parameter. For TAG_EXCEPTION/TAG_THROWS, this is the type name of
     *        the exception. May be <code>null</code> for TAG_RETURN.
     * @param type parameter type. Either TAG_PARAM, TAG_RETURN or TAG_EXCEPTION/TAG_THROWS.
     * @param environment the environment.
     *
     * @return template text for the given param type.
     *
     * @throws IllegalArgumentException if <em>node</em> is no valid node to add a tag of type
     *         <em>type</em> to.
     */
    private String getTagTemplateText(AST         node,
                                      String      typeName,
                                      int         type,
                                      Environment environment) {
        switch (type) {
            case JavadocTokenTypes.TAG_PARAM:
                switch (node.getType()) {
                    case JavaTokenTypes.METHOD_DEF:

                        String text = AbstractPrinter.settings.get(
                            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM,
                            ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM);

                    // fall through
                    case JavaTokenTypes.CTOR_DEF:
                    case JavaTokenTypes.CLASS_DEF: // TODO Update template form for class definition
                    case JavaTokenTypes.INTERFACE_DEF: // TODO Update template form for class definition
                        text = AbstractPrinter.settings.get(
                            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_PARAM,
                            ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_CTOR_PARAM);

                        int offset = text.indexOf('*');

                        environment.set(Environment.Variable.TYPE_PARAM.getName(), typeName);

                        if (offset > -1) {
                            text = text.substring(offset + 1).trim();
                        } // end if

                        text = environment.interpolate(text);
                        environment.unset(Environment.Variable.TYPE_PARAM.getName());

                        return text;
                    default:
                        throw new IllegalArgumentException(
                            "invalid node type to add @param tag -- " + node);
                } // end switch
            case JavadocTokenTypes.TAG_THROWS:
            case JavadocTokenTypes.TAG_EXCEPTION:
                switch (node.getType()) {
                    case JavaTokenTypes.METHOD_DEF:

                        String text = AbstractPrinter.settings.get(
                            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_EXCEPTION,
                            ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_METHOD_EXCEPTION);

                    // fall through
                    case JavaTokenTypes.CTOR_DEF:
                        text = AbstractPrinter.settings.get(
                            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_EXCEPTION,
                            ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_CTOR_EXCEPTION);

                        int offset = text.indexOf('*');

                        environment.set(
                            Environment.Variable.TYPE_EXCEPTION.getName(),
                            typeName);

                        if (offset > -1) {
                            text = text.substring(offset + 1).trim();
                        } // end if

                        text = environment.interpolate(text);
                        environment.unset(Environment.Variable.TYPE_EXCEPTION.getName());

                        return text;
                    default:
                        throw new IllegalArgumentException(
                            "invalid node type to add @throws tag -- " + node);
                } // end switch
            case JavadocTokenTypes.TAG_RETURN:
                switch (node.getType()) {
                    case JavaTokenTypes.METHOD_DEF:

                        String text   = AbstractPrinter.settings.get(
                            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_RETURN,
                            ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_METHOD_RETURN);
                        int    offset = text.indexOf('*');

                        if (offset > -1) {
                            text = text.substring(offset + 1).trim();
                        } // end if

                        return text;
                    default:
                        throw new IllegalArgumentException(
                            "invalid node type to add @return tag -- " + node);
                } // end switch
            default:
                return EMPTY_STRING;
        } // end switch
    } // end getTagTemplateText()

    /**
     * Returns the string to start a Javadoc comment with.
     *
     * @param type type of the node to get the starting comment string for.
     *
     * @return the string to start a Javadoc comment with (usually <code> /&#42;&#42;</code>).
     *
     * @since 1.0b8
     */
    private String getTopString(int type) {
        switch (type) {
            case JavaTokenTypes.METHOD_DEF: {
                String text   = AbstractPrinter.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_TOP,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_METHOD_TOP);
                int    offset = text.indexOf(DELIMETER);

                if (offset > -1) {
                    return text.substring(0, offset);
                } // end if
                return text;
            } // end case
            case JavaTokenTypes.CTOR_DEF: {
                String text   = AbstractPrinter.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_TOP,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_CTOR_TOP);
                int    offset = text.indexOf(DELIMETER);

                if (offset > -1) {
                    return text.substring(0, offset);
                } // end if
                return text;
            } // end case
            case JavaTokenTypes.VARIABLE_DEF: {
                String text   = AbstractPrinter.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_VARIABLE,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_VARIABLE).trim();
                int    offset = text.indexOf(DELIMETER);

                if (offset > -1) {
                    return text.substring(0, offset);
                } // end if
                return "/**";
            } // end case
            case JavaTokenTypes.CLASS_DEF: {
                String text   = AbstractPrinter.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CLASS,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_CLASS).trim();
                int    offset = text.indexOf(DELIMETER);

                if (offset > -1) {
                    return text.substring(0, offset);
                } // end if
                return "/**";
            } // end case
            case JavaTokenTypes.INTERFACE_DEF: {
                String text   = AbstractPrinter.settings.get(
                    ConventionKeys.COMMENT_JAVADOC_TEMPLATE_INTERFACE,
                    ConventionDefaults.COMMENT_JAVADOC_TEMPLATE_INTERFACE).trim();
                int    offset = text.indexOf(DELIMETER);

                if (offset > -1) {
                    return text.substring(0, offset);
                } // end if
                return "/**";
            } // end case
            default:
                return "/**";
        } // end switch
    } // end getTopString()

    /**
     * Determines whether the description for the given Javadoc comment starts with
     * the inheritDoc in-line tag.
     *
     * @param comment a Javadoc comment.
     *
     * @return <code>true</code> if the inheritDoc tag could be found.
     */
    private boolean hasInheritDoc(AST comment) {
        for (AST child = comment.getFirstChild(); child != null;
             child = child.getNextSibling()) {
            switch (child.getType()) {
                case JavadocTokenTypes.TAG_INLINE_INHERITDOC:
                    return true;
                default:
                    return false;
            } // end switch
        } // end for

        return false;
    } // end hasInheritDoc()

    /**
     * Determines whether the given node is a valid Javadoc node.
     *
     * @param node a node.
     *
     * @return <code>true</code> if the given node can have a Javadoc comment as per the
     *         Javadoc specification. These are CLASS_DEF, INTERFACE_DEF, CTOR_DEF,
     *         METHOD_DEF and VARIABLE_DEF nodes.
     *
     * @since 1.0b8
     */
    private boolean isValidNode(AST node) {
        switch (node.getType()) {
            case JavaTokenTypes.METHOD_DEF:
            case JavaTokenTypes.CTOR_DEF:
            case JavaTokenTypes.CLASS_DEF:
            case JavaTokenTypes.INTERFACE_DEF:
                return true;
            case JavaTokenTypes.VARIABLE_DEF:
                return !JavaNodeHelper.isLocalVariable(node);
            default:
                return false;
        } // end switch
    } // end isValidNode()

    /**
     * Returns the text of the node and all siblings as one string.
     *
     * @param node node to merge its text for.
     * @param newLineString The new line char code
     * @param asterix DOCUMENT ME!
     *
     * @return string with the textual content of the node and all siblings.
     *
     * @throws IllegalStateException if a &lt;pre&gt; tag was found in the description.
     */
    private String mergeChildren(AST    node,
                                 String newLineString,
                                 String asterix) {
        StringBuffer buf = new StringBuffer(150);

        for (AST child = node; child != null; child = child.getNextSibling()) {
            switch (child.getType()) {
                case JavadocTokenTypes.OCODE:
                case JavadocTokenTypes.OTTYPE:
                case JavadocTokenTypes.OANCHOR:
                case JavadocTokenTypes.OEM:
                case JavadocTokenTypes.OSTRONG:
                case JavadocTokenTypes.OITALIC:
                case JavadocTokenTypes.OBOLD:
                case JavadocTokenTypes.OUNDER:
                case JavadocTokenTypes.OSTRIKE:
                case JavadocTokenTypes.OBIG:
                case JavadocTokenTypes.OSMALL:
                case JavadocTokenTypes.OSUB:
                case JavadocTokenTypes.OSUP:
                case JavadocTokenTypes.ODFN:
                case JavadocTokenTypes.OSAMP:
                case JavadocTokenTypes.OKBD:
                case JavadocTokenTypes.OVAR:
                case JavadocTokenTypes.OCITE:
                case JavadocTokenTypes.OACRO:
                case JavadocTokenTypes.OFONT:
                case JavadocTokenTypes.OBQUOTE:
                case JavadocTokenTypes.OULIST:
                case JavadocTokenTypes.ODLIST:
                case JavadocTokenTypes.OOLIST:
                case JavadocTokenTypes.OTABLE:
                case JavadocTokenTypes.O_TR:
                case JavadocTokenTypes.OTD:
                case JavadocTokenTypes.OTH:
                case JavadocTokenTypes.OH1:
                case JavadocTokenTypes.OH2:
                case JavadocTokenTypes.OH3:
                case JavadocTokenTypes.OH4:
                case JavadocTokenTypes.OH5:
                case JavadocTokenTypes.OH6:
                    buf.append(child.getText());
                    buf.append(mergeChildren(child.getFirstChild(), newLineString, asterix));

                    break;
                case JavadocTokenTypes.OLITEM:
                case JavadocTokenTypes.ODTERM:
                case JavadocTokenTypes.ODDEF:
                    buf.append(child.getText());
                    buf.append(mergeChildren(child.getFirstChild(), newLineString, asterix));
                    buf.append(SPACE);

                    break;
                case JavadocTokenTypes.TAG_INLINE_LINK:
                case JavadocTokenTypes.TAG_INLINE_LINKPLAIN:
                case JavadocTokenTypes.TAG_INLINE_INHERITDOC:
                case JavadocTokenTypes.TAG_INLINE_DOCROOT:
                case JavadocTokenTypes.TAG_INLINE_VALUE:
                case JavadocTokenTypes.TAG_INLINE_CUSTOM:
                    buf.append(LCURLY);
                    buf.append(child.getText());
                    buf.append(mergeChildren(child.getFirstChild(), newLineString, asterix));
                    buf.append(RCURLY);

                    break;
                case JavadocTokenTypes.PRE:
                    throw new IllegalStateException(
                        "<pre> tag not supported within tag description");
                case JavadocTokenTypes.BR:
                    buf.append(child.getText());
                    buf.append(newLineString);
                    buf.append(asterix);
                    break;
                default:
                    buf.append(child.getText());
            } // end switch
        } // end for

        return buf.toString();
    } // end mergeChildren()

    /**
     * Prints the given blockquote.
     *
     * @param node root node of the blockquote.
     * @param asterix leading asterix.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printBlockquote(AST        node,
                                 String     asterix,
                                 NodeWriter out)
                          throws IOException {
        generalPrint(out, node, node.getText(), asterix);
        node = printContent(node.getFirstChild(), asterix, out);
        generalPrint(out, node, node.getText(), asterix);
    } // end printBlockquote()

    /**
     * Prints the given comment.
     *
     * @param node root node of the comment.
     * @param asterix the leading asterix.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printComment(AST        node,
                              String     asterix,
                              NodeWriter out)
                       throws IOException {
        generalPrint(out, node, node.getText(), asterix);
    } // end printComment()

    /**
     * Prints the given lines. Prepends a leading asterix in front of each line.
     *
     * @param lines lines to print.
     * @param asterix the leading asterix.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printCommentLines(String[]   lines,
                                   String     asterix,
                                   NodeWriter out)
                            throws IOException {
        printCommentLines(lines, asterix, out, false);
    } // end printCommentLines()

    /**
     * Prints the individual comment lines.
     *
     * @param lines the comment lines.
     * @param asterix asterix to prepend before each line.
     * @param out stream to write to.
     * @param trim if <code>true</code> each line will be trimmed.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printCommentLines(String[]   lines,
                                   String     asterix,
                                   NodeWriter out,
                                   boolean    trim)
                            throws IOException {
        int maxColumn = AbstractPrinter.settings.getInt(
            ConventionKeys.LINE_LENGTH,
            ConventionDefaults.LINE_LENGTH);

        if (trim) {
            for (int i = 0; i < lines.length; i++) {
                if ((asterix != null) && ((out.column + lines[i].length()) > maxColumn)) {
                    out.printNewline();
                    out.print(asterix, JavadocTokenTypes.PCDATA);
                } // end if

                out.print(lines[i].trim(), JavadocTokenTypes.PCDATA);
            } // end for
        } // end if
        else {
            for (int i = 0; i < lines.length; i++) {
                if ((asterix != null) && ((out.column + lines[i].length()) > maxColumn)) {
                    out.printNewline();
                    out.print(asterix, JavadocTokenTypes.PCDATA);
                } // end if

                out.print(lines[i], JavadocTokenTypes.PCDATA);
            } // end for
        } // end else
    } // end printCommentLines()

    /**
     * Prints the content of the description section.
     *
     * @param node the first node of the description section.
     * @param asterix leading asterix.
     * @param out stream to write to.
     *
     * @return the next node to print. Either a node representing a standard Javadoc tag or
     *         the {@link #EMPTY_NODE} to indicate that no standard tags are available.
     *
     * @throws IOException if an I/O error occured.
     */
    private AST printContent(AST        node,
                             String     asterix,
                             NodeWriter out)
                      throws IOException {
        AST next = EMPTY_NODE;

ITERATION: 
        for (AST child = node; child != null; child = child.getNextSibling()) {
SELECTION: 
            for (;;) {
                switch (child.getType()) {
                    case JavadocTokenTypes.COMMENT:
                        printComment(child, asterix, out);
                        break SELECTION;
                    case JavadocTokenTypes.PCDATA:
                    case JavadocTokenTypes.RCURLY:
                    case JavadocTokenTypes.LCURLY:
                    case JavadocTokenTypes.AT:
                    case JavadocTokenTypes.OTTYPE:
                    case JavadocTokenTypes.OITALIC:
                    case JavadocTokenTypes.OBOLD:
                    case JavadocTokenTypes.OUNDER:
                    case JavadocTokenTypes.OSTRIKE:
                    case JavadocTokenTypes.OBIG:
                    case JavadocTokenTypes.OSMALL:
                    case JavadocTokenTypes.OSUB:
                    case JavadocTokenTypes.OSUP:
                    case JavadocTokenTypes.OEM:
                    case JavadocTokenTypes.OSTRONG:
                    case JavadocTokenTypes.ODFN:
                    case JavadocTokenTypes.OCODE:
                    case JavadocTokenTypes.OSAMP:
                    case JavadocTokenTypes.OKBD:
                    case JavadocTokenTypes.OVAR:
                    case JavadocTokenTypes.OCITE:
                    case JavadocTokenTypes.OACRO:
                    case JavadocTokenTypes.OANCHOR:
                    case JavadocTokenTypes.IMG:
                    case JavadocTokenTypes.OFONT:
                    case JavadocTokenTypes.BR:
                    case JavadocTokenTypes.TYPEDCLASS:
                        child = printText(child, asterix, out);
                        continue SELECTION;
                    case JavadocTokenTypes.TAG_INLINE_DOCROOT:
                    case JavadocTokenTypes.TAG_INLINE_LINK:
                    case JavadocTokenTypes.TAG_INLINE_LINKPLAIN:
                    case JavadocTokenTypes.TAG_INLINE_INHERITDOC:
                    case JavadocTokenTypes.TAG_INLINE_VALUE:
                    case JavadocTokenTypes.TAG_INLINE_CUSTOM:
                        child = printText(child, asterix, out);
                        continue SELECTION;
                    case JavadocTokenTypes.OPARA:
                        printParagraph(child, asterix, out);
                        break SELECTION;
                    case JavadocTokenTypes.OBQUOTE:
                        printBlockquote(child, asterix, out);
                        break SELECTION;
                    case JavadocTokenTypes.HR:
                        generalPrint(out, child, child.getText(), asterix);
                        break SELECTION;
                    case JavadocTokenTypes.OH1:
                    case JavadocTokenTypes.OH2:
                    case JavadocTokenTypes.OH3:
                    case JavadocTokenTypes.OH4:
                    case JavadocTokenTypes.OH5:
                    case JavadocTokenTypes.OH6:
                        printHeading(child, asterix, out);
                        break SELECTION;
                    case JavadocTokenTypes.OTABLE:
                        printTable(child, asterix, out);
                        break SELECTION;
                    case JavadocTokenTypes.PRE:
                        printPreformatted(child, asterix, out);
                        break SELECTION;
                    case JavadocTokenTypes.OULIST:
                    case JavadocTokenTypes.OOLIST:
                    case JavadocTokenTypes.ODLIST:
                        printList(child, asterix, out);
                        break SELECTION;

/**
                     * @todo center and div
                     */
                    default:
                        next = child;
                        break ITERATION;
                } // end switch
            } // end for
        } // end for

        return next;
    } // end printContent()

    /**
     * Print the comment's leading description.
     *
     * @param node owner of the comment.
     * @param comment the first node of the description section.
     * @param asterix the leading asterix.
     * @param out stream to write to.
     *
     * @return the first node of the tag section or the {@link #EMPTY_NODE} if no tag nodes
     *         exist.
     *
     * @throws IOException if an I/O error occured.
     */
    private AST printDescriptionSection(AST        node,
                                        AST        comment,
                                        String     asterix,
                                        NodeWriter out)
                                 throws IOException {
        // check if we only have a description that fits in one line
        switch (node.getType()) {
            case JavaTokenTypes.VARIABLE_DEF:
            case JavaTokenTypes.ENUM_CONSTANT_DEF:
                if (AbstractPrinter.settings.getBoolean(
                    ConventionKeys.COMMENT_JAVADOC_FIELDS_SHORT,
                    ConventionDefaults.COMMENT_JAVADOC_FIELDS_SHORT)) {
                    if (printSingleLineDescription(node, comment, out)) {
                        // no standard tags found if we print in one line,
                        // return the EMPTY_NODE to indicate this
                        return EMPTY_NODE;
                    } // end if
                } // end if
                break;
        } // end switch

        out.printNewline();
        out.print(asterix, JavadocTokenTypes.JAVADOC_COMMENT);

        AST result = printContent(comment.getFirstChild(), asterix, out);

        if (!out.newline) {
            out.printNewline();
        }

        return result;
    } // end printDescriptionSection()

    /**
     * Prints the given heading.
     *
     * @param node root node of the heading.
     * @param asterix the leading asterix.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printHeading(AST        node,
                              String     asterix,
                              NodeWriter out)
                       throws IOException {
        generalPrint(out, node, node.getText(), asterix);
        if (node.getFirstChild() != null) {
            node = printText(node.getFirstChild(), asterix, out);
            if (node != EMPTY_NODE) {
                generalPrint(out, node, node.getText(), asterix);
            } // end if
        } // end if
        out.last = JavadocTokenTypes.CH1;
    } // end printHeading()

    /**
     * Prints out the given list (either a definition, ordered or unordered list).
     *
     * @param node node to print
     * @param asterix leading asterix.
     * @param out stream to print to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printList(AST        node,
                           String     asterix,
                           NodeWriter out)
                    throws IOException {
        generalPrint(out, node, node.getText(), asterix);

        AST child = null;

        for (child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            printListItem(child, asterix, out);
        } // end for
    } // end printList()

    /**
     * Prints out the given list item (either a list item, definition term or
     * definition).
     *
     * @param node node to print
     * @param asterix leading asterix.
     * @param out stream to print to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printListItem(AST        node,
                               String     asterix,
                               NodeWriter out)
                        throws IOException {
        generalPrint(out, node, node.getText(), asterix);

        AST newnode = printContent(node.getFirstChild(), asterix, out);

        if (newnode != EMPTY_NODE) {
            generalPrint(out, newnode, newnode.getText(), asterix);
        } // end if
    } // end printListItem()

    /**
     * Prints an empty line before the given tag, if necessary.
     *
     * @param tag a Javadoc Standard tag.
     * @param last the type of the last tag printed.
     * @param asterix string to print as leading asterix.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b9
     */
    private void printNewlineBefore(AST        tag,
                                    int        last,
                                    String     asterix,
                                    NodeWriter out)
                             throws IOException {
        if (shouldHaveNewlineBefore(tag, last)) {
            if (!out.newline) {
                out.printNewline();
            } // end if
            out.print(StringHelper.trimTrailing(asterix), JavadocTokenTypes.PCDATA);
            out.printNewline();
        } // end if

        /*switch (last)
           {
           case DESCRIPTION:
           out.print(asterix, JavadocTokenTypes.PCDATA);
           out.printNewline();
           break;
           case NONE:
           break;
           default:
           switch (tag.getType())
           {
               case JavadocTokenTypes.TAG_EXCEPTION:
               case JavadocTokenTypes.TAG_THROWS:
                   switch (last)
                   {
                       case JavadocTokenTypes.TAG_EXCEPTION:
                       case JavadocTokenTypes.TAG_THROWS:
                           break;
                       default:
                           out.print(asterix, JavadocTokenTypes.PCDATA);
                           out.printNewline();
                           break;
                   }
                   break;
               case JavadocTokenTypes.TAG_VERSION:
               break;
               case JavadocTokenTypes.TAG_PARAM:
               case JavadocTokenTypes.TAG_CUSTOM:
               case JavadocTokenTypes.TAG_AUTHOR:
                   if (last != tag.getType())
                   {
                       out.print(asterix, JavadocTokenTypes.PCDATA);
                       out.printNewline();
                   }
                   break;
               case JavadocTokenTypes.TAG_RETURN:
                       out.print(asterix, JavadocTokenTypes.PCDATA);
                       out.printNewline();
                   break;
               case JavadocTokenTypes.TAG_SEE:
                   if (last != tag.getType())
                   {
                       out.print(asterix, JavadocTokenTypes.PCDATA);
                       out.printNewline();
                   }
                   break;
               default:
               switch (last)
               {
                   case JavadocTokenTypes.TAG_PARAM:
                   case JavadocTokenTypes.TAG_RETURN:
                   case JavadocTokenTypes.TAG_THROWS:
                   case JavadocTokenTypes.TAG_EXCEPTION:
                   case JavadocTokenTypes.TAG_AUTHOR:
                   case JavadocTokenTypes.TAG_VERSION:
                       out.print(asterix, JavadocTokenTypes.PCDATA);
                       out.printNewline();
                       break;
               }
                   break;
           }
           break;
           }*/
    } // end printNewlineBefore()

    /**
     * Prints the given paragraph.
     *
     * @param node root node of the paragraph.
     * @param asterix leading asterix.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printParagraph(AST        node,
                                String     asterix,
                                NodeWriter out)
                         throws IOException {
//        switch (out.last)
//        {
//            case JavadocTokenTypes.CPARA :
//            case JavadocTokenTypes.CBQUOTE :
//            case JavadocTokenTypes.HR :
//            case JavadocTokenTypes.CH1 :
//            case JavadocTokenTypes.CTABLE :
//            case JavadocTokenTypes.PRE :
//            case JavadocTokenTypes.CULIST :
//            case JavadocTokenTypes.COLIST :
//            case JavadocTokenTypes.CDLIST :
//            case JavadocTokenTypes.PCDATA :
//                if (!out.newline)
//                    out.printNewline();
//                out.print(asterix, JavadocTokenTypes.PCDATA);
//                out.printNewline();
//        }
//
//        out.print(asterix, JavadocTokenTypes.PCDATA);
        generalPrint(out, node, node.getText(), asterix);
//        out.print(node.getText(), JavadocTokenTypes.OPARA);
        if (node.getFirstChild() != null) {
//            out.printNewline();
ITERATION: 
            for (AST child = node.getFirstChild(); child != null;
                 child = child.getNextSibling()) {
SELECTION: 
                for (;;) {
                    switch (child.getType()) {
                        case JavadocTokenTypes.PCDATA:
                        case JavadocTokenTypes.AT:
                        case JavadocTokenTypes.RCURLY:
                        case JavadocTokenTypes.LCURLY:
                        case JavadocTokenTypes.IMG:
                        case JavadocTokenTypes.BR:
                        case JavadocTokenTypes.OTTYPE:
                        case JavadocTokenTypes.OITALIC:
                        case JavadocTokenTypes.OBOLD:
                        case JavadocTokenTypes.OANCHOR:
                        case JavadocTokenTypes.OUNDER:
                        case JavadocTokenTypes.OSTRIKE:
                        case JavadocTokenTypes.OBIG:
                        case JavadocTokenTypes.OSMALL:
                        case JavadocTokenTypes.OSUB:
                        case JavadocTokenTypes.OSUP:
                        case JavadocTokenTypes.OEM:
                        case JavadocTokenTypes.OSTRONG:
                        case JavadocTokenTypes.ODFN:
                        case JavadocTokenTypes.OCODE:
                        case JavadocTokenTypes.OSAMP:
                        case JavadocTokenTypes.OKBD:
                        case JavadocTokenTypes.OVAR:
                        case JavadocTokenTypes.OCITE:
                        case JavadocTokenTypes.OACRO:
                        case JavadocTokenTypes.OFONT:
                            child = printText(child, asterix, out);
                            continue SELECTION;
                        case JavadocTokenTypes.TAG_INLINE_DOCROOT:
                        case JavadocTokenTypes.TAG_INLINE_LINK:
                        case JavadocTokenTypes.TAG_INLINE_LINKPLAIN:
                        case JavadocTokenTypes.TAG_INLINE_INHERITDOC:
                        case JavadocTokenTypes.TAG_INLINE_VALUE:
                        case JavadocTokenTypes.TAG_INLINE_CUSTOM:
                            out.print(LCURLY, JavadocTokenTypes.LCURLY);
                            child = printText(child, asterix, out);
                            out.print(RCURLY, JavadocTokenTypes.RCURLY);

                            continue SELECTION;
                        case JavadocTokenTypes.OBQUOTE:
                            printBlockquote(child, asterix, out);
                            break SELECTION;
                        case JavadocTokenTypes.PRE:
                            printPreformatted(child, asterix, out);
                            break SELECTION;
                        case JavadocTokenTypes.OULIST:
                        case JavadocTokenTypes.OOLIST:
                        case JavadocTokenTypes.ODLIST:
                            printList(child, asterix, out);
                            break SELECTION;
                        default:
                            break ITERATION;
                    } // end switch
                } // end for
            } // end for

            generalPrint(out, JavadocTokenTypes.CPARA, TAG_CPARA, asterix);
        } // end if
        else {
            generalPrint(out, JavadocTokenTypes.CPARA, TAG_CPARA, asterix);
        } // end else

        if (!out.newline) {
            out.printNewline();
        } // end if
    } // end printParagraph()

    /**
     * Prints the given &lt;pre&gt; tag.
     *
     * @param node a PRE node.
     * @param asterix asterix to prepend before each line of the tag text.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printPreformatted(AST        node,
                                   String     asterix,
                                   NodeWriter out)
                            throws IOException {
        String[] lines = split(node.getText(), out.originalLineSeparator, '*');

        printCommentLines(lines, asterix, out);
        out.last = JavadocTokenTypes.PRE;
    } // end printPreformatted()

    /**
     * Prints the given return tag.
     *
     * @param tag tag to print.
     * @param asterix the leading asterix.
     * @param maxWidth maximal width one line can consume.
     * @param added if <code>true</code> a warning message will be added that the tag was
     *        added.
     * @param last the type of the last printed tag.
     * @param out stream to write to.
     *
     * @return the type of the type printed.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b8
     */
    private int printReturnTag(AST        tag,
                               String     asterix,
                               int        maxWidth,
                               boolean    added,
                               int        last,
                               NodeWriter out)
                        throws IOException {
        if (tag != null) {
            if (added) {
                out.state.args[0] = out.getFilename();
                out.state.args[1] = new Integer(
                    out.line + (shouldHaveNewlineBefore(tag, last) ? 1 : 0));
                out.state.args[2] = new Integer(out.getIndentLength() + asterix.length() + 1);
                out.state.args[3] = "@return" /* NOI18N */;
                out.state.args[4] = EMPTY_STRING;
                Loggers.PRINTER_JAVADOC.l7dlog(
                    Level.WARN,
                    KEY_TAG_ADD_MISSING,
                    out.state.args,
                    null);
            } // end if

            return printTag(tag, asterix, maxWidth, last, out);
        } // end if
        return last;
    } // end printReturnTag()

    /**
     * Attempts to print the given node in one line.
     *
     * @param node owner of the comment.
     * @param comment comment to print.
     * @param out stream to write to.
     *
     * @return <code>true</code> if the node could be printed in one line.
     *
     * @throws IOException if an I/O error occured.
     */
    private boolean printSingleLineDescription(AST        node,
                                               AST        comment,
                                               NodeWriter out)
                                        throws IOException {
        StringBuffer buf      = new StringBuffer();
        int          maxwidth = AbstractPrinter.settings.getInt(
            ConventionKeys.LINE_LENGTH,
            ConventionDefaults.LINE_LENGTH) - 3 - out.getIndentLength();

        for (AST child = comment.getFirstChild(); child != null;
             child = child.getNextSibling()) {
            switch (child.getType()) {
                // never print these in one line
                case JavadocTokenTypes.OPARA:
                case JavadocTokenTypes.TAG_SERIAL:
                case JavadocTokenTypes.TAG_SERIAL_DATA:
                case JavadocTokenTypes.TAG_SERIAL_FIELD:
                case JavadocTokenTypes.TAG_PARAM:
                case JavadocTokenTypes.TAG_RETURN:
                case JavadocTokenTypes.TAG_THROWS:
                case JavadocTokenTypes.TAG_EXCEPTION:
                case JavadocTokenTypes.TAG_CUSTOM:
                case JavadocTokenTypes.TAG_TODO:
                case JavadocTokenTypes.TAG_AUTHOR:
                case JavadocTokenTypes.TAG_SEE:
                case JavadocTokenTypes.TAG_VERSION:
                case JavadocTokenTypes.TAG_SINCE:
                case JavadocTokenTypes.TAG_DEPRECATED:
                    return false;
                case JavadocTokenTypes.PCDATA:
                case JavadocTokenTypes.AT:
                case JavadocTokenTypes.RCURLY:
                case JavadocTokenTypes.LCURLY:
                case JavadocTokenTypes.IMG:
                case JavadocTokenTypes.BR:
                    buf.append(child.getText());
                    break;

                // physical text elements, shouldn't be used anymore
                case JavadocTokenTypes.OTTYPE:
                case JavadocTokenTypes.OITALIC:
                case JavadocTokenTypes.OBOLD:
                case JavadocTokenTypes.OANCHOR:
                case JavadocTokenTypes.OUNDER:
                case JavadocTokenTypes.OSTRIKE:
                case JavadocTokenTypes.OBIG:
                case JavadocTokenTypes.OSMALL:
                case JavadocTokenTypes.OSUB:
                case JavadocTokenTypes.OSUP:

                // logical text elements
                case JavadocTokenTypes.OEM:
                case JavadocTokenTypes.OSTRONG:
                case JavadocTokenTypes.ODFN:
                case JavadocTokenTypes.OCODE:
                case JavadocTokenTypes.OSAMP:
                case JavadocTokenTypes.OKBD:
                case JavadocTokenTypes.OVAR:
                case JavadocTokenTypes.OCITE:
                case JavadocTokenTypes.OACRO:

                // special text elements
                case JavadocTokenTypes.OFONT:
                    buf.append(child.getText());
                    buf.append(mergeChildren(child.getFirstChild(), "", ""));

                    break;

                // inline tags
                case JavadocTokenTypes.TAG_INLINE_DOCROOT:
                case JavadocTokenTypes.TAG_INLINE_LINK:
                case JavadocTokenTypes.TAG_INLINE_LINKPLAIN:
                case JavadocTokenTypes.TAG_INLINE_INHERITDOC:
                case JavadocTokenTypes.TAG_INLINE_VALUE:
                case JavadocTokenTypes.TAG_INLINE_CUSTOM:
                    buf.append(LCURLY);
                    buf.append(child.getText());

                    for (AST part = child.getFirstChild(); part != null;
                         part = part.getNextSibling()) {
                        buf.append(part.getText());
                    } // end for

                    buf.append(RCURLY);

                    break;
            } // end switch

            if (buf.length() > maxwidth) {
                return false;
            } // end if
        } // end for

        if (buf.length() < maxwidth) {
            out.print(SPACE, JavadocTokenTypes.JAVADOC_COMMENT);
            out.print(buf.toString().trim(), JavadocTokenTypes.JAVADOC_COMMENT);

            return true;
        } // end if

        return false;
    } // end printSingleLineDescription()

    /**
     * Prints the given table.
     *
     * @param node root node of the table.
     * @param asterix leading asterix.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printTable(AST        node,
                            String     asterix,
                            NodeWriter out)
                     throws IOException {
        generalPrint(out, node, node.getText(), asterix);

        for (AST row = node.getFirstChild(); row != null; row = row.getNextSibling()) {
            printTableRow(row, asterix, out);
        } // end for
    } // end printTable()

    /**
     * Prints the given table data.
     *
     * @param node root node of the table data.
     * @param asterix the leading asterix.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printTableData(AST        node,
                                String     asterix,
                                NodeWriter out)
                         throws IOException {
        generalPrint(out, node, node.getText(), asterix);

        if (node.getFirstChild() != null) {
            AST nextNode = printContent(node.getFirstChild(), asterix, out);

            generalPrint(out, nextNode, nextNode.getText(), asterix);
        } // end if
    } // end printTableData()

    /**
     * Prints the given table row.
     *
     * @param node root node of the table row.
     * @param asterix the leading asterix.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     */
    private void printTableRow(AST        node,
                               String     asterix,
                               NodeWriter out)
                        throws IOException {
        generalPrint(out, node, node.getText(), asterix);

        for (AST cell = node.getFirstChild(); cell != null; cell = cell.getNextSibling()) {
            printTableData(cell, asterix, out);
        } // end for
    } // end printTableRow()

    /**
     * Prints the given tag.
     *
     * @param tag tag to print.
     * @param asterix the leading asterix.
     * @param maxwidth maximal width one line can consume.
     * @param last the type of the last printed tag.
     * @param out stream to write to.
     *
     * @return the type of the tag printed.
     *
     * @throws IOException if an I/O error occured.
     */
    private int printTag(AST        tag,
                         String     asterix,
                         int        maxwidth,
                         int        last,
                         NodeWriter out)
                  throws IOException {
        if (tag != null) {
            printNewlineBefore(tag, last, asterix, out);

            if (out.newline) {
                out.print(asterix, JavaTokenTypes.JAVADOC_COMMENT);
            }

            String ident = tag.getText();

            out.print(ident, JavaTokenTypes.JAVADOC_COMMENT);

            switch (tag.getType()) {
                // special case for @version tag as these may contain CVS
                // version info strings that should never be wrapped
                case JavadocTokenTypes.TAG_VERSION: {
                    AST child = tag.getFirstChild();

                    if (child != null) {
                        String description = mergeChildren(child, out.lineSeparator, asterix);

                        // we trim the text so we have to take care to print a
                        // blank between tag name and description
                        switch (description.charAt(0)) {
                            case ' ':
                            case '<':
                            case '{':
                                out.print(SPACE, JavadocTokenTypes.JAVADOC_COMMENT);
                                break;
                        } // end switch

                        out.print(description.trim(), JavadocTokenTypes.JAVADOC_COMMENT);
                    } // end if

                    break;
                } // end case

                // @tag name type description
                case JavadocTokenTypes.TAG_SERIAL_FIELD:
                // @tag name description
                case JavadocTokenTypes.TAG_PARAM:
                case JavadocTokenTypes.TAG_THROWS:
                case JavadocTokenTypes.TAG_EXCEPTION:
                    printTagDescription(
                        tag.getFirstChild(),
                        ident,
                        asterix,
                        maxwidth,
                        true,
                        out);
                    break;

                // @tag description
                case JavadocTokenTypes.TAG_CUSTOM:
                case JavadocTokenTypes.TAG_TODO:default:
                    printTagDescription(
                        tag.getFirstChild(),
                        ident,
                        asterix,
                        maxwidth,
                        false,
                        out);
                    break;
            } // end switch

            out.printNewline();

            return tag.getType();
        } // end if

        return last;
    } // end printTag()

    /**
     * Prints the description of the given tag, if any.
     *
     * @param child the first child of the tag, starting the description.
     * @param name the tag name.
     * @param asterix string to print as leading asterix.
     * @param maxwidth maximal width one line can consume.
     * @param normalize if <code>true</code> the method tries to strip any whitespace between
     *        the first word and second word of the description.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b9
     */
    private void printTagDescription(AST        child,
                                     String     name,
                                     String     asterix,
                                     int        maxwidth,
                                     boolean    normalize,
                                     NodeWriter out)
                              throws IOException {
        if (child != null) {
            String description = mergeChildren(child, out.lineSeparator, asterix);

            // we trim the text so we have to take care to print a
            // blank between tag name and description
            switch (description.charAt(0)) {
                case ' ':
                case '<':
                case '{':
                    out.print(SPACE, JavadocTokenTypes.JAVADOC_COMMENT);
                    break;
            } // end switch

            // normalize the description if this is not an auto-generated tag
            if (normalize && (description.charAt(0) != '@')) {
                Matcher matcher = _pattern.matcher(description);

                if (matcher.matches()) {
                    if (matcher.group(1) != null) {
                        StringBuffer buf = new StringBuffer(description.length());

                        buf.append(matcher.group(1));
                        buf.append(SPACE);
                        buf.append(matcher.group(2));

                        description = buf.toString();
                    } // end if
                } // end if
            } // end if

            int      length = name.length();
            String[] lines  = split(description.trim(), maxwidth - length - 1, 0, true);

            for (int i = 0, size = lines.length - 1; i < size; i++) {
                out.print(lines[i], JavadocTokenTypes.JAVADOC_COMMENT);
                out.printNewline();
                out.print(asterix, JavadocTokenTypes.JAVADOC_COMMENT);
                out.print(out.getString(length + 1), JavaTokenTypes.WS);
            } // end for

            out.print(lines[lines.length - 1], JavadocTokenTypes.JAVADOC_COMMENT);
        } // end if

/**
         * @todo add semantic check
         */
    } // end printTagDescription()

    /**
     * Prints all tags of the given comment.
     *
     * @param node owner of the comment.
     * @param comment the comment.
     * @param firstTag first tag to print.
     * @param asterix the leading asterix.
     * @param out stream to write to.
     *
     * @throws IOException if an I/O error occurred.
     */
    private void printTagSection(AST        node,
                                 AST        comment,
                                 AST        firstTag,
                                 String     asterix,
                                 NodeWriter out)
                          throws IOException {
        List parameterTags    = Collections.EMPTY_LIST;
        List annotationTags   = Collections.EMPTY_LIST;
        AST  serialTag        = null;
        AST  serialDataTag    = null;
        List serialFieldsTags = Collections.EMPTY_LIST;
        AST  sinceTag         = null;
        List seesTags         = Collections.EMPTY_LIST;
        AST  versionTag       = null;
        List customTags       = Collections.EMPTY_LIST;
        List authorTags       = Collections.EMPTY_LIST;
        AST  deprecatedTag    = null;
        AST  returnTag        = null;
        List exceptionTags    = Collections.EMPTY_LIST;

        boolean checkTags = AbstractPrinter.settings.getBoolean(
            ConventionKeys.COMMENT_JAVADOC_CHECK_TAGS,
            ConventionDefaults.COMMENT_JAVADOC_CHECK_TAGS);

        if (checkTags) {
            checkTags = shouldCheckTags(node, comment);
        } // end if

        for (AST tag = firstTag; tag != null; tag = tag.getNextSibling()) {
            switch (tag.getType()) {
                case JavadocTokenTypes.TAG_PARAM:
                    if (parameterTags.isEmpty()) {
                        parameterTags = new ArrayList(4);
                    } // end if
                    parameterTags.add(tag);

                    break;
                case JavadocTokenTypes.TAG_SERIAL:
                    serialTag = tag;
                    break;
                case JavadocTokenTypes.TAG_SERIAL_DATA:
                    serialDataTag = tag;
                    break;
                case JavadocTokenTypes.TAG_SERIAL_FIELD:
                    if (serialFieldsTags.isEmpty()) {
                        serialFieldsTags = new ArrayList(4);
                    } // end if
                    serialFieldsTags.add(tag);

                    break;
                case JavadocTokenTypes.TAG_SINCE:
                    sinceTag = tag;
                    break;
                case JavadocTokenTypes.TAG_SEE:
                    if (seesTags.isEmpty()) {
                        seesTags = new ArrayList(4);
                    } // end if
                    seesTags.add(tag);

                    if (checkTags && (tag.getNextSibling() == null) && (tag == firstTag)) {
                        // checking only needed if no single @see tag found
                        // in description
                        checkTags = false;
                    } // end if

                    break;
                case JavadocTokenTypes.TAG_VERSION:
                    versionTag = tag;
                    break;
                case JavadocTokenTypes.TAG_DEPRECATED:
                    deprecatedTag = tag;
                    break;
                case JavadocTokenTypes.TAG_AUTHOR:
                    if (authorTags.isEmpty()) {
                        authorTags = new ArrayList(4);
                    } // end if
                    authorTags.add(tag);

                    break;
                case JavadocTokenTypes.TAG_RETURN:
                    returnTag = tag;
                    break;
                case JavadocTokenTypes.TAG:
                    if (annotationTags.isEmpty()) {
                        annotationTags = new ArrayList(4);
                    } // end if
                    annotationTags.add(tag);

                    break;
                case JavadocTokenTypes.TAG_THROWS:
                case JavadocTokenTypes.TAG_EXCEPTION:
                    if (exceptionTags.isEmpty()) {
                        exceptionTags = new ArrayList(4);
                    } // end if
                    exceptionTags.add(tag);

                    break;
                case JavadocTokenTypes.TAG_CUSTOM:
                case JavadocTokenTypes.TAG_TODO:
                    if (customTags.isEmpty()) {
                        customTags = new ArrayList(5);
                    } // end if
                    customTags.add(tag);

                    break;
            } // end switch
        } // end for

        int maxwidth = AbstractPrinter.settings.getInt(
            ConventionKeys.LINE_LENGTH,
            ConventionDefaults.LINE_LENGTH) - out.getIndentLength() - 3;

        int last = NONE;

        if (comment.getFirstChild() != firstTag) {
            last = DESCRIPTION;
        } // end if

        // insert description if missing
        else if (checkTags) {
/**
             * @todo log info/warning
             */

            // no description found, add missing
//            out.print(asterix, JavadocTokenTypes.PCDATA);
/**
             * @todo parse user specified Javadoc template
             */
            out.print("DOCUMENT ME!", JavadocTokenTypes.PCDATA);
            out.printNewline();

            last = DESCRIPTION;
        } // end else if

        boolean returnTagAdded     = false;
        boolean checkParameterTags = false;
        boolean checkThrowsTags    = false;

        // not all tags are valid for every node, so we only print the tags
        // that are valid
        switch (node.getType()) {
            case JavaTokenTypes.VARIABLE_DEF:
                last = printTag(serialTag, asterix, maxwidth, last, out);
                last = printTags(serialFieldsTags, asterix, maxwidth, last, out);

                break;
            case JavaTokenTypes.METHOD_DEF:
                if (checkTags) {
                    boolean tagPresent = returnTag != null;

                    returnTag = checkReturnTag(node, returnTag, out);
                    returnTagAdded = !tagPresent && (returnTag != null);
                } // end if

            // fall through
            case JavaTokenTypes.CTOR_DEF:
                last = printTag(serialDataTag, asterix, maxwidth, last, out);

            // Fall through
            case JavaTokenTypes.CLASS_DEF:
            case JavaTokenTypes.INTERFACE_DEF: {
                if ((node.getType() == JavaTokenTypes.CLASS_DEF) ||
                    (node.getType() == JavaTokenTypes.INTERFACE_DEF)) {
                    last = printTags(authorTags, asterix, maxwidth, last, out);
                    last = printTag(versionTag, asterix, maxwidth, last, out);
                    last = printTag(serialTag, asterix, maxwidth, last, out);
                } // end if

                if (getParamCount(node) > 0) {
                    if (checkTags) {
                        if (parameterTags.isEmpty()) {
                            parameterTags = new ArrayList(5);
                        } // end if

                        checkParameterTags = true;
                    } // end if

                    if (checkParameterTags) {
                        last = printTags(
                            parameterTags,
                            asterix,
                            maxwidth,
                            node,
                            JavaTokenTypes.PARAMETERS,
                            last,
                            out);
                    } // end if
                    else {
                        last = printTags(parameterTags, asterix, maxwidth, last, out);
                    } // end else
                } // end if

                switch (node.getType()) {
                    case JavaTokenTypes.METHOD_DEF:
                        last = printReturnTag(
                            returnTag,
                            asterix,
                            maxwidth,
                            returnTagAdded,
                            last,
                            out);

                    // Fall through
                    case JavaTokenTypes.CTOR_DEF:
                        if (checkTags) {
                            if (exceptionTags.isEmpty()) {
                                exceptionTags = new ArrayList();
                            } // end if

                            checkThrowsTags = true;
                        } // end if
                        if (checkThrowsTags) {
                            last = printTags(
                                exceptionTags,
                                asterix,
                                maxwidth,
                                node,
                                JavaTokenTypes.LITERAL_throws,
                                last,
                                out);
                        } // end if
                        else {
                            last = printTags(exceptionTags, asterix, maxwidth, last, out);
                        } // end else
                        break;
                } // end switch

                break;
            } // end case
        } // end switch

        last = printTags(customTags, asterix, maxwidth, last, out);

        // print the tags that can be used everywhere
        last = printTags(seesTags, asterix, maxwidth, last, out);
        last = printTag(sinceTag, asterix, maxwidth, last, out);
        last = printTag(deprecatedTag, asterix, maxwidth, last, out);
        last = printTags(annotationTags, asterix, maxwidth, last, out);
    } // end printTagSection()

    /**
     * Prints the given tags and checks for missing/obsolete ones.
     *
     * @param tags tags to print.
     * @param asterix string to print as leading asterix.
     * @param maxwidth maximal width one line can consume.
     * @param node associated node.
     * @param tagType the token type of the tags.
     * @param last if <code>true</code> an empty line will be printed before the tags.
     * @param out stream to write to.
     *
     * @return the type of the last tag printed.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b8
     */
    private int printTags(List       tags,
                          String     asterix,
                          int        maxwidth,
                          AST        node,
                          int        tagType,
                          int        last,
                          NodeWriter out)
                   throws IOException {
        if ((tagType != -1)) {
            checkTags(node, tags, tagType, asterix, last, out);
        } // end if

        return printTags(tags, asterix, maxwidth, last, out);
    } // end printTags()

    /**
     * Prints the given tags.
     *
     * @param tags tags to print.
     * @param asterix leading asterix.
     * @param maxwidth maximal width one line can consume.
     * @param last the type of the tag printed last.
     * @param out stream to write to.
     *
     * @return the type of the last printed tag.
     *
     * @throws IOException if an I/O error occured.
     */
    private int printTags(List       tags,
                          String     asterix,
                          int        maxwidth,
                          int        last,
                          NodeWriter out)
                   throws IOException {
        for (int i = 0, size = tags.size(); i < size; i++) {
            last = printTag((AST)tags.get(i), asterix, maxwidth, last, out);
        } // end for

        return last;
    } // end printTags()

    /**
     * Prints out all text and text level tags (&lt;tt&gt;, &lt;strong&gt; etc.)
     *
     * @param node first node to print
     * @param asterix the leading asterix.
     * @param out stream to print to.
     *
     * @return the first non-text level node found (i.e the first node not printed) or the
     *         {@link #EMPTY_NODE} if all or none nodes have been printed.
     *
     * @throws IOException if an I/O error occured.
     */
    private AST printText(AST        node,
                          String     asterix,
                          NodeWriter out)
                   throws IOException {
        StringBuffer buf  = new StringBuffer(200);
        AST          next = EMPTY_NODE;

LOOP: 
        for (AST child = node; child != null; child = child.getNextSibling()) {
            switch (child.getType()) {
                case JavadocTokenTypes.COMMENT:
                    printComment(child, asterix, out);
                    break;

                // text
                case JavadocTokenTypes.PCDATA:
                // special characters
                case JavadocTokenTypes.AT:
                case JavadocTokenTypes.RCURLY:
                case JavadocTokenTypes.LCURLY:
                case JavadocTokenTypes.IMG:
                case JavadocTokenTypes.BR:
                case JavadocTokenTypes.TYPEDCLASS:
                    buf.append(child.getText());
                    break;

                // physical text elements, shouldn't be used anymore
                case JavadocTokenTypes.OTTYPE:
                case JavadocTokenTypes.OITALIC:
                case JavadocTokenTypes.OBOLD:
                case JavadocTokenTypes.OANCHOR:
                case JavadocTokenTypes.OUNDER:
                case JavadocTokenTypes.OSTRIKE:
                case JavadocTokenTypes.OBIG:
                case JavadocTokenTypes.OSMALL:
                case JavadocTokenTypes.OSUB:
                case JavadocTokenTypes.OSUP:

                // logical text elements
                case JavadocTokenTypes.OEM:
                case JavadocTokenTypes.OSTRONG:
                case JavadocTokenTypes.ODFN:
                case JavadocTokenTypes.OCODE:
                case JavadocTokenTypes.OSAMP:
                case JavadocTokenTypes.OKBD:
                case JavadocTokenTypes.OVAR:
                case JavadocTokenTypes.OCITE:
                case JavadocTokenTypes.OACRO:

                // special text elements
                case JavadocTokenTypes.OFONT:
                    buf.append(child.getText());
                    buf.append(
                        mergeChildren(child.getFirstChild(), out.lineSeparator, asterix));

                    break;

                // inline tags
                case JavadocTokenTypes.TAG_INLINE_DOCROOT:
                case JavadocTokenTypes.TAG_INLINE_LINK:
                case JavadocTokenTypes.TAG_INLINE_LINKPLAIN:
                case JavadocTokenTypes.TAG_INLINE_INHERITDOC:
                case JavadocTokenTypes.TAG_INLINE_VALUE:
                case JavadocTokenTypes.TAG_INLINE_CUSTOM:
                    buf.append(LCURLY);
                    buf.append(child.getText());

                    for (AST part = child.getFirstChild(); part != null;
                         part = part.getNextSibling()) {
                        // BUGFIX #581299
                        switch (part.getType()) {
                            case JavadocTokenTypes.OANCHOR:
                                buf.append(' ');
                                break;
                        } // end switch

                        buf.append(part.getText());
                    } // end for

                    buf.append(RCURLY);

                    break;

                // non text level element
                default:
                    next = child;
                    // we only want text elements, so we quit here
                    break LOOP;
            } // end switch
        } // end for

        if (buf.length() > 0) {
            generalPrint(out, node, buf.toString().trim(), asterix);
        } // end if

        return next;
    } // end printText()

    /**
     * Indicates whether Javadoc tags should be checked.
     *
     * @param node the node that the Javadoc comment belongs to.
     * @param comment the Javadoc comment.
     *
     * @return <code>true</code> if <em>node</em> is a valid Javadoc node and the comment does
     *         start with the inheritDoc in-line tag.
     *
     * @since 1.0b8
     */
    private boolean shouldCheckTags(AST node,
                                    AST comment) {
        return isValidNode(node) && !hasInheritDoc(comment);
    } // end shouldCheckTags()

    /**
     * Determines whether the given tag should have an empty line before.
     *
     * @param tag a Javadoc tag.
     * @param last the type of the tag last printed.
     *
     * @return <code>true</code> if an empty should be printed.
     *
     * @since 1.0b9
     */
    private boolean shouldHaveNewlineBefore(AST tag,
                                            int last) {
        boolean result = false;

        switch (last) {
            case DESCRIPTION:
                result = true;
                break;
            case NONE:
                break;
            default:
                switch (tag.getType()) {
                    case JavadocTokenTypes.TAG_EXCEPTION:
                    case JavadocTokenTypes.TAG_THROWS:
                        switch (last) {
                            case JavadocTokenTypes.TAG_EXCEPTION:
                            case JavadocTokenTypes.TAG_THROWS:
                                break;
                            default:
                                result = true;
                                break;
                        } // end switch
                        break;
                    case JavadocTokenTypes.TAG_VERSION:
                        break;
                    case JavadocTokenTypes.TAG_PARAM:
                    case JavadocTokenTypes.TAG_CUSTOM:
                    case JavadocTokenTypes.TAG_TODO:
                    case JavadocTokenTypes.TAG_AUTHOR:
                        if (last != tag.getType()) {
                            result = true;
                        } // end if
                        break;
                    case JavadocTokenTypes.TAG_RETURN:
                        result = true;
                        break;
                    case JavadocTokenTypes.TAG_SEE:
                        if (last != tag.getType()) {
                            result = true;
                        } // end if
                        break;
                    default:
                        switch (last) {
                            case JavadocTokenTypes.TAG_PARAM:
                            case JavadocTokenTypes.TAG_RETURN:
                            case JavadocTokenTypes.TAG_THROWS:
                            case JavadocTokenTypes.TAG_EXCEPTION:
                            case JavadocTokenTypes.TAG_AUTHOR:
                            case JavadocTokenTypes.TAG_VERSION:
                                result = true;
                                break;
                        } // end switch
                        break;
                } // end switch
                break;
        } // end switch

        return result;
    } // end shouldHaveNewlineBefore()

    /**
     * Splits the given string into tokens.
     *
     * @param str string to split into tokens.
     * @param delim the delimeter to use for splitting.
     * @param character if <code>character > -1</code> all leading whitespace before the
     *        character will be removed.
     *
     * @return 1.0b8
     */
    private String[] split(String str,
                           String delim,
                           char   character) {
        if (character > -1) {
            int  startOffset = 0;
            int  endOffset   = -1;
            int  sepLength   = delim.length();
            List lines       = new ArrayList(15);

            while ((endOffset = str.indexOf(delim, startOffset)) > -1) {
                String line = str.substring(startOffset, endOffset);

                lines.add(trimLeadingWhitespace(line, character));
                startOffset = endOffset + sepLength;
            } // end while

            if (startOffset > 0) {
                String line = trimLeadingWhitespace(str.substring(startOffset), character);

                lines.add(line);
            } // end if
            else {
                lines.add(trimLeadingWhitespace(str, character));
            } // end else

            return (String[])lines.toArray(EMPTY_STRING_ARRAY);
        } // end if
        return StringHelper.split(str, delim);
    } // end split()

    /**
     * Splits the given string into multiple lines.
     *
     * @param str the string to split.
     * @param width the maximum width of each line.
     * @param columnStart DOCUMENT ME!
     * @param trim if <code>true</code> the individual lines will be trimmed.
     *
     * @return array with the splitted lines.
     *
     * @since 1.0b8
     */
    private String[] split(String  str,
                           int     width,
                           int     columnStart,
                           boolean trim) {
        List lines = new ArrayList();

        if (trim) {
            str = str.trim();
        } // end if

        if ((str.length() + columnStart) < width) {
            lines.add(str);
        } // end if
        else {
            BreakIterator iterator = (BreakIterator)_stringBreaker.get();

            try {
                iterator.setText(str);

                int lineStart = 0;
                int nextStart = iterator.next();
                int prevStart = 0;

                do {
MOVE_FORWARD: 
                    while (((nextStart - lineStart + columnStart) < width) &&
                           (nextStart != BreakIterator.DONE)) {
                        prevStart = nextStart;
                        nextStart = iterator.next();

                        switch (iterator._type) {
                            case BreakIterator.BREAK:
                                prevStart = nextStart + 4;
                                break MOVE_FORWARD;
                        } // end switch
                    } // end while

                    if (prevStart == 0) {
                        prevStart = nextStart;
                    } // end if

                    if (nextStart == BreakIterator.DONE) {
                        // if the text before and after the last space fits
                        // into the max width, just print it on one line
                        if (((prevStart - lineStart) + (str.length() - prevStart) +
                            columnStart) < width) {
                            lines.add(str.substring(lineStart, str.length()).trim());
                        } // end if
                        else {
                            if ((prevStart > 0) && (prevStart != BreakIterator.DONE)) {
                                if (trim) {
                                    lines.add(str.substring(lineStart, prevStart).trim());
                                    lines.add(str.substring(prevStart).trim());
                                } // end if
                                else {
                                    lines.add(str.substring(lineStart, prevStart));
                                    lines.add(str.substring(prevStart));
                                } // end else
                            } // end if
                            else {
                                if (trim) {
                                    lines.add(str.substring(lineStart).trim());
                                } // end if
                                else {
                                    lines.add(str.substring(lineStart));
                                } // end else
                            } // end else
                        } // end else

                        prevStart = str.length();
                    } // end if
                    else {
                        if (trim) {
                            lines.add(str.substring(lineStart, prevStart).trim());
                        } // end if
                        else {
                            lines.add(str.substring(lineStart, prevStart));
                        } // end else
                    } // end else

                    lineStart = prevStart;
                    prevStart = 0;
                    columnStart = 0;
                } // end do
                while (lineStart < str.length());
            } // end try
            finally {
                iterator.reset();
            } // end finally
        } // end else

        return (String[])lines.toArray(EMPTY_STRING_ARRAY);
    } // end split()

    /**
     * TODO DOCUMENT ME!
     *
     * @param str DOCUMENT ME!
     * @param character DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String trimLeadingWhitespace(String str,
                                         char   character) {
        int off = StringHelper.indexOfNonWhitespace(str);

        if ((off > -1) && (str.charAt(off) == character)) {
            return str.substring(off + 1);
        } // end if

        return str;
    } // end trimLeadingWhitespace()
} // end JavadocPrinter
