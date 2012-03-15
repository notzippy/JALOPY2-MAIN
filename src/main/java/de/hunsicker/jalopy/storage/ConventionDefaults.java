/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.storage;

import de.hunsicker.jalopy.language.JavaParser;

import org.apache.log4j.Level;


/**
 * Holds the default code convention values (Sun Java Coding Style).
 * 
 * <p>
 * Use this class in conjunction with {@link ConventionKeys} to access the convention
 * settings:
 * </p>
 * <pre class="snippet">
 * {@link Convention} settings = {@link Convention}.getInstance();
 * int numThreads = settings.getInt({@link ConventionKeys}.THREAD_COUNT,
 *                                  {@link ConventionDefaults}.THREAD_COUNT));
 * </pre>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 *
 * @see de.hunsicker.jalopy.storage.Convention
 * @see de.hunsicker.jalopy.storage.ConventionKeys
 */
public final class ConventionDefaults
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String EMPTY_STRING = "" /* NOI18N */.intern();

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean ALIGN_PARAMS_METHOD_DEF = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean ALIGN_TERNARY_OPERATOR = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean ALIGN_TERNARY_EXPRESSION = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean ALIGN_TERNARY_VALUES = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean ALIGN_VAR_ASSIGNS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean ALIGN_VAR_IDENTS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean ALIGN_METHOD_CALL_CHAINS = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean ARRAY_BRACKETS_AFTER_IDENT = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BACKUP_LEVEL = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_AFTER_BLOCK = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_AFTER_BRACE_LEFT = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String ENVIRONMENT = EMPTY_STRING;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_AFTER_CLASS = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_AFTER_DECLARATION = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_AFTER_FOOTER = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_AFTER_HEADER = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_AFTER_IMPORT = 2;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_AFTER_INTERFACE = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_AFTER_METHOD = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_AFTER_PACKAGE = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_BEFORE_BLOCK = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_BEFORE_BRACE_RIGHT = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_BEFORE_CASE_BLOCK = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_BEFORE_COMMENT_JAVADOC = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_BEFORE_COMMENT_MULTI_LINE = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_BEFORE_CONTROL = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_BEFORE_DECLARATION = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_BEFORE_FOOTER = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_BEFORE_HEADER = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int BLANK_LINES_KEEP_UP_TO = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_EMPTY_CUDDLE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_EMPTY_INSERT_STATEMENT = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_INSERT_DO_WHILE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_INSERT_FOR = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_INSERT_IF_ELSE = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_INSERT_WHILE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_NEWLINE_LEFT = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_TREAT_DIFFERENT_IF_WRAPPED = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INSERT_TRAILING_NEWLINE = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_NEWLINE_RIGHT = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_REMOVE_BLOCK = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_REMOVE_DO_WHILE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_REMOVE_FOR = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_REMOVE_IF_ELSE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_REMOVE_WHILE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean BRACE_TREAT_DIFFERENT = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean CHUNKS_BY_BLANK_LINES = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int HEADER_SMART_MODE_LINES = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean CHUNKS_BY_COMMENTS = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_FORMAT_MULTI_LINE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_INSERT_SEPARATOR = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_INSERT_SEPARATOR_RECURSIVE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_JAVADOC_FIELDS_SHORT = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_JAVADOC_CHECK_TAGS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_JAVADOC_CHECK_TAGS_THROWS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int COMMENT_JAVADOC_CLASS_MASK = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int COMMENT_JAVADOC_CTOR_MASK = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_JAVADOC_INNER_CLASS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean DONT_COMMENT_JAVADOC_WHEN_ML = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int COMMENT_JAVADOC_METHOD_MASK = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int COMMENT_JAVADOC_VARIABLE_MASK = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_JAVADOC_PARSE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_JAVADOC_REMOVE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_JAVADOC_TRANSFORM = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_INTERFACE =
        "/**| * DOCUMENT ME!| *| * @author $author$| * @version \u0024Revision\u0024| */" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_CLASS =
        "/**| * DOCUMENT ME!| *| * @author $author$| * @version \u0024Revision\u0024| */" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_VARIABLE =
        "/**| * DOCUMENT ME!| */" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_METHOD_TOP =
        "/**| * DOCUMENT ME!" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_METHOD_SEPARATOR =
        " *" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM =
        " * @param $paramType$ DOCUMENT ME!" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_METHOD_EXCEPTION =
        " * @throws $exceptionType$ DOCUMENT ME!" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_METHOD_RETURN =
        " * @return DOCUMENT ME!" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_METHOD_BOTTOM =
        " */" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_CTOR_TOP =
        "/**| * Creates a new $objectType$ object." /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_CTOR_PARAM =
        " * @param $paramType$ DOCUMENT ME!" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_CTOR_EXCEPTION =
        " * @throws $exceptionType$ DOCUMENT ME!" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TEMPLATE_CTOR_BOTTOM = " */" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_REMOVE_MULTI_LINE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean COMMENT_REMOVE_SINGLE_LINE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int SOURCE_VERSION = JavaParser.JDK_1_5;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean FOOTER = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean FORCE_FORMATTING = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INSERT_FINAL_MODIFIER_FOR_PARAMETERS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INSERT_FINAL_MODIFIER_FOR_METHOD_PARAMETERS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean HEADER = false;

    /** The default value for the key with the same name. */
    public static final String HISTORY_POLICY = History.Policy.DISABLED.getName();

    /** The default value for the key with the same name. */
    public static final String HISTORY_METHOD = History.Method.TIMESTAMP.getName();

    /** The default value for the key with the same name. */
    public static final String IMPORT_POLICY = ImportPolicy.DISABLED.getName();

    /** The default value for the key with the same name ("{@value}"). */
    public static final String IMPORT_GROUPING = "*:0|gnu:2|java:2|javax:2" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int IMPORT_GROUPING_DEPTH = 3;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean IMPORT_SORT = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INDENT_CASE_FROM_SWITCH = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INDENT_CONTINUATION_BLOCK = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INDENT_CONTINUATION_OPERATOR = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INDENT_FIRST_COLUMN_COMMENT = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INDENT_LABEL = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INDENT_DEEP = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE = 4;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_BRACE_CUDDLED = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_BRACE_LEFT = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_BRACE_RIGHT = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_BRACE_RIGHT_AFTER = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_COMMENT_ENDLINE = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_CONTINUATION = 4;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_DEEP = 55;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_EXTENDS = -1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_IMPLEMENTS = -1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_LEADING = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_TABS = 8;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int INDENT_SIZE_THROWS = -1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INDENT_WITH_TABS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INDENT_WITH_TABS_ONLY_LEADING = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INSERT_EXPRESSION_PARENTHESIS = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INSERT_LOGGING_CONDITIONAL = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INSERT_SERIAL_UID = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String LANGUAGE = "en" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COUNTRY = "US" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TAGS_INLINE = EMPTY_STRING;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String COMMENT_JAVADOC_TAGS_STANDARD = EMPTY_STRING;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int LINE_LENGTH = 80;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_PARAMS_EXCEED = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_PARAMS_HARD = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_PARAMS_DEEP = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_PAREN_GROUPING = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_CHAINED_METHOD_CALL = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_ASSIGN = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_LABEL = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_PARAMS_METHOD_CALL = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_PARAMS_METHOD_CALL_IF_NESTED = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_PARAMS_METHOD_DEF = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_TYPES_EXTENDS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_TYPES_EXTENDS_EXCEED = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_TYPES_IMPLEMENTS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_TYPES_IMPLEMENTS_EXCEED = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_TYPES_THROWS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_TYPES_THROWS_EXCEED = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int LINE_WRAP_ARRAY_ELEMENTS = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_BEFORE_ARRAY_INIT = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_BEFORE_EXTENDS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_BEFORE_IMPLEMENTS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_BEFORE_OPERATOR = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_BEFORE_THROWS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_AFTER_LEFT_PAREN = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean LINE_WRAP_BEFORE_RIGHT_PAREN = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean ENUM_LCURLY_NO_NEW_LINE = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int ENUM_ALIGN_VALUES_WHEN_EXCEEDS = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean ANON_LCURLY_NO_NEW_LINE = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int ANON_ALIGN_VALUES_WHEN_EXCEEDS = 0;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean ANON_DEF_LCURLY_NO_NEW_LINE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int ANON_DEF_ALIGN_VALUES_WHEN_EXCEEDS = 0;

    /** The default value for the key with the same name. */
    public static final int MSG_PRIORITY_IO = Level.WARN.toInt();

    /** The default value for the key with the same name. */
    public static final int MSG_PRIORITY_PARSER = Level.WARN.toInt();

    /** The default value for the key with the same name. */
    public static final int MSG_PRIORITY_PARSER_JAVADOC = Level.WARN.toInt();

    /** The default value for the key with the same name. */
    public static final int MSG_PRIORITY_PRINTER = Level.WARN.toInt();

    /** The default value for the key with the same name. */
    public static final int MSG_PRIORITY_PRINTER_JAVADOC = Level.WARN.toInt();

    /** The default value for the key with the same name. */
    public static final int MSG_PRIORITY_TRANSFORM = Level.WARN.toInt();

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean MSG_SHOW_ERROR_STACKTRACE = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean PADDING_ASSIGNMENT_OPERATORS = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean PADDING_BITWISE_OPERATORS = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean PADDING_BRACES = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean PADDING_BRACKETS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean PADDING_CAST = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean PADDING_LOGICAL_OPERATORS = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean PADDING_MATH_OPERATORS = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean PADDING_PAREN = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean PADDING_RELATIONAL_OPERATORS = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean PADDING_SHIFT_OPERATORS = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SORT = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SORT_CLASS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SORT_ANNOTATION = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SORT_ENUM = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SORT_CTOR = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SORT_INTERFACE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SORT_METHOD = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SORT_METHOD_BEAN = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SORT_MODIFIERS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SORT_VARIABLE = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SPACE_AFTER_CAST = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SPACE_AFTER_COMMA = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SPACE_AFTER_SEMICOLON = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SPACE_BEFORE_BRACES = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SPACE_BEFORE_BRACKETS = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SPACE_BEFORE_BRACKETS_TYPES = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SPACE_BEFORE_CASE_COLON = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SPACE_BEFORE_LOGICAL_NOT = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SPACE_BEFORE_METHOD_CALL_PAREN = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SPACE_BEFORE_METHOD_DEF_PAREN = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean SPACE_BEFORE_STATEMENT_PAREN = true;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean STRIP_QUALIFICATION = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String CONVENTION_DESCRIPTION =
        "Sun Java Coding Convention" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String CONVENTION_NAME = "Sun" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final int THREAD_COUNT = 1;

    /** The default value for the key with the same name ("{@value}"). */
    public static final boolean INSPECTOR = false;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String REGEXP_PACKAGE = "[a-z]+(?:\\.[a-z]+)*" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String REGEXP_CLASS = "[A-Z][a-zA-Z0-9]+" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String REGEXP_CLASS_ABSTRACT = "[A-Z][a-zA-Z0-9]+" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String REGEXP_INTERFACE = "[A-Z][a-zA-Z0-9]+" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String REGEXP_LABEL = "\\w+" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String REGEXP_FIELD = "[a-z][\\w]+" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String REGEXP_FIELD_STATIC_FINAL = "[a-zA-Z][\\w]+" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String REGEXP_METHOD = "[a-z][\\w]+" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String REGEXP_LOCAL_VARIABLE = "[a-z][\\w]*" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String REGEXP_PARAM = "[a-z][\\w]+" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String SEPARATOR_FILL_CHARACTER = "-" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String SEPARATOR_STATIC_VAR_INIT =
        "Static fields/initializers" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String SEPARATOR_INSTANCE_VAR = "Instance fields" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String SEPARATOR_INSTANCE_INIT =
        "Instance initializers" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String SEPARATOR_CTOR = "Constructors" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String SEPARATOR_METHOD = "Methods" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String SEPARATOR_INTERFACE = "Inner Interfaces" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String SEPARATOR_ENUM_INIT = "Enumerations" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String SEPARATOR_ANNOTATION_INIT = "Annotations" /* NOI18N */;

    /** The default value for the key with the same name ("{@value}"). */
    public static final String SEPARATOR_CLASS = "Inner Classes" /* NOI18N */;

    /** Add comments after closing braces */
    public static final boolean BRACE_ADD_COMMENT = false;

    public static final boolean JAVADOC_PARSE_DESCRIPTION = true;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ConventionDefaults object.
     */
    private ConventionDefaults()
    {
    }
}
