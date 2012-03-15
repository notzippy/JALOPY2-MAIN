/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.storage;

import java.lang.reflect.Field;
import java.util.Locale;

import de.hunsicker.jalopy.storage.Convention.Key;


/**
 * Provides the valid keys for accessing the values in a code convention.
 * 
 * <p>
 * Use this class in conjunction with {@link ConventionDefaults} to access the convention
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
 * @see de.hunsicker.jalopy.storage.ConventionDefaults
 */
public final class ConventionKeys
{
    private static class BasicKey {
        public Convention.Key key = null;
        public Object defaultValue = null;
        private Convention convention = Convention.getInstance();
        
        public BasicKey(final String name, final Object defaulValue) {
            key = new Convention.Key(name);
            this.defaultValue = defaulValue;
        }
        public void set(String value) {
            convention.put(key,value);
        }
        public String get() {
            return convention.get(key,(String)defaultValue);
        }
        public void setBoolean(Boolean newValue) {
            convention.putBoolean(key,newValue.booleanValue());
        }
        public boolean getBoolean() {
            return convention.getBoolean(key,((Boolean)defaultValue).booleanValue());
        }
        public void setInt(Integer intValue) {
            convention.putInt(key,intValue.intValue());
        }
        public int getInt() {
            return convention.getInt(key,((Integer)defaultValue).intValue());
        }
    }
    //~ Static variables/initializers ----------------------------------------------------
    
    private static class General extends BasicKey {
        private static class Compliance extends General{
            public static Version VERSION = new Version();
            private static class Version extends Compliance {
                public Version(){
                    super("version","1.5");
                }
            }
            public Compliance(String name, Object defaultValue) {
                super("compliance/" + name,defaultValue);
            }
        }
        public General(String name,Object defaultValue) {
            super("general/" + name,defaultValue);
        }
        private static class Locale extends General{
            public static Country COUNTRY = new Country();
            public static Language LANGUAGE = new Language();
            private static class Language extends Locale {
                public Language() {
                    super("language" , java.util.Locale.US.toString());
                }
            }
            
            private static class Country extends Locale {
                public Country() {
                    super("country" , java.util.Locale.US.toString());
                }
            }
            public Locale(String name,Object value) {
                super("locale/" + name, value);
            }
        }
        private static class access extends BasicKey{
            public access(String name, Object defaulValue) {
                super(name, defaulValue);
                // TODO Auto-generated constructor stub
            }

            public BasicKey PUBLIC = new BasicKey(key.toString() + "/public",Boolean.FALSE);
            
        }
        private static class style extends General {

            private class description extends style {

                public description() {
                    super("description" , "");
                }
                
            }
            public style(String name, Object defaultValue) {
                super("style/" + name, defaultValue);
            }
        }
    }
    
    /**
     * JDK source compatibility version (<em>String</em>).
     * TODO 
     * @since 1.0b8
     */
    public static final Convention.Key SOURCE_VERSION =
        //General.Compliance.VERSION.key;
        new Convention.Key("general/compliance/version");

    /**
     * Uppercase two-letter ISO-3166 code (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key COUNTRY =
        //General.Locale.COUNTRY.key;
        new Convention.Key("general/locale/country");

    /**
     * Lowercase two-letter ISO-639 code (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key LANGUAGE =
        //General.Locale.LANGUAGE.key;
        new Convention.Key("general/locale/lanuguage");

    /** Description of the code convention (<em>String</em>). */
    public static final Convention.Key CONVENTION_DESCRIPTION =
        new Convention.Key("general/style/description");

    /** Name of the code convention (<em>String</em>). */
    public static final Convention.Key CONVENTION_NAME =
        new Convention.Key("general/style/name");

    /** The location where to load the code convention from (String). */
    public static final Convention.Key STYLE_LOCATION =
        new Convention.Key("general/style/location");

    /** Enable the code inspector? (<em>boolean</em>) */
    public static final Convention.Key INSPECTOR = new Convention.Key("inspector/enable");

    /**
     * Regexp for package names (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_PACKAGE =
        new Convention.Key("inspector/naming/packages");

    /**
     * Regexp for class names (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_CLASS =
        new Convention.Key("inspector/naming/classes/general");

    /**
     * Regexp for abstract class names (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_CLASS_ABSTRACT =
        new Convention.Key("inspector/naming/classes/abstract");

    /**
     * Regexp for interface names (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_INTERFACE =
        new Convention.Key("inspector/naming/interfaces");

    /**
     * Regexp for local variables names (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_LOCAL_VARIABLE =
        new Convention.Key("inspector/naming/variables");

    /**
     * Regexp for method/ctor parameter names (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_PARAM =
        new Convention.Key("inspector/naming/parameters/default");

    /**
     * Regexp for final method/ctor parameter names (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_PARAM_FINAL =
        new Convention.Key("inspector/naming/parameters/final");

    /**
     * Regexp for labels (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_LABEL =
        new Convention.Key("inspector/naming/labels");

    /**
     * Regexp for public fields  (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_PUBLIC =
        new Convention.Key("inspector/naming/fields/public");

    /**
     * Regexp for protected fields (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_PROTECTED =
        new Convention.Key("inspector/naming/fields/protected");

    /**
     * Regexp for package protected (default access) fields (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_DEFAULT =
        new Convention.Key("inspector/naming/fields/default");

    /**
     * Regexp for private fields (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_PRIVATE =
        new Convention.Key("inspector/naming/fields/private");

    /**
     * Regexp for public static fields (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_PUBLIC_STATIC =
        new Convention.Key("inspector/naming/fields/publicStatic");

    /**
     * Regexp for protected static  (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_PROTECTED_STATIC =
        new Convention.Key("inspector/naming/fields/protectedStatic");

    /**
     * Regexp for package protected (default access) static fields (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_DEFAULT_STATIC =
        new Convention.Key("inspector/naming/fields/defaultStatic");

    /**
     * Regexp for private static fields (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_PRIVATE_STATIC =
        new Convention.Key("inspector/naming/fields/privateStatic");

    /**
     * Regexp for public static final fields (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_PUBLIC_STATIC_FINAL =
        new Convention.Key("inspector/naming/fields/publicStaticFinal");

    /**
     * Regexp for protected static final fields (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_PROTECTED_STATIC_FINAL =
        new Convention.Key("inspector/naming/fields/protectedStaticFinal");

    /**
     * Regexp for package protected (default access) static final fields
     * (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_DEFAULT_STATIC_FINAL =
        new Convention.Key("inspector/naming/fields/defaultStaticFinal");

    /**
     * Regexp for private static final fields (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key REGEXP_FIELD_PRIVATE_STATIC_FINAL =
        new Convention.Key("inspector/naming/fields/privateStaticFinal");

    /**
     * Regexp for public methods  (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_PUBLIC =
        new Convention.Key("inspector/naming/methods/public");

    /**
     * Regexp for protected methods (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_PROTECTED =
        new Convention.Key("inspector/naming/methods/protected");

    /**
     * Regexp for package protected (default access) methods (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_DEFAULT =
        new Convention.Key("inspector/naming/methods/default");

    /**
     * Regexp for private methods (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_PRIVATE =
        new Convention.Key("inspector/naming/methods/private");

    /**
     * Regexp for public static methods (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_PUBLIC_STATIC =
        new Convention.Key("inspector/naming/methods/publicStatic");

    /**
     * Regexp for protected static  (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_PROTECTED_STATIC =
        new Convention.Key("inspector/naming/methods/protectedStatic");

    /**
     * Regexp for package protected (default access) static methods (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_DEFAULT_STATIC =
        new Convention.Key("inspector/naming/methods/defaultStatic");

    /**
     * Regexp for private static methods (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_PRIVATE_STATIC =
        new Convention.Key("inspector/naming/methods/privateStatic");

    /**
     * Regexp for public static final methods (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_PUBLIC_STATIC_FINAL =
        new Convention.Key("inspector/naming/methods/publicStaticFinal");

    /**
     * Regexp for protected static final methods (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_PROTECTED_STATIC_FINAL =
        new Convention.Key("inspector/naming/methods/protectedStaticFinal");

    /**
     * Regexp for package protected (default access) static final methods
     * (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_DEFAULT_STATIC_FINAL =
        new Convention.Key("inspector/naming/methods/defaultStaticFinal");

    /**
     * Regexp for private static final methods (<em>String</em>).
     *
     * @since 1.0b9
     */
    public static final Convention.Key REGEXP_METHOD_PRIVATE_STATIC_FINAL =
        new Convention.Key("inspector/naming/methods/privateStaticFinal");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key TIP_STRING_LITERAL_I18N =
        new Convention.Key("inspector/tips/stringLiterallI18n");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_DONT_SUBSTITUTE_OBJECT_EQUALS =
        new Convention.Key("inspector/tips/dontSubstituteObjectEquals");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_OBEY_CONTRACT_EQUALS =
        new Convention.Key("inspector/tips/obeyContractEquals");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_OVERRIDE_HASHCODE =
        new Convention.Key("inspector/tips/alwaysOverrideHashCode");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_OVERRIDE_EQUALS =
        new Convention.Key("inspector/tips/alwaysOverrideEquals");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_OVERRIDE_TO_STRING =
        new Convention.Key("inspector/tips/overrideToString");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_INTERFACE_ONLY_FOR_TYPE =
        new Convention.Key("inspector/tips/useInterfaceOnlyForTypes");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_REPLACE_STRUCTURE_WITH_CLASS =
        new Convention.Key("inspector/tips/replaceStructureWithClass");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_RETURN_ZERO_ARRAY =
        new Convention.Key("inspector/tips/neverReturnZeroArrays");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_REFER_BY_INTERFACE =
        new Convention.Key("inspector/tips/referToObjectsByInterface");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_ADHERE_TO_NAMING_CONVENTION =
        new Convention.Key("inspector/tips/adhereToNamingConvention");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_NEVER_THROW_EXCEPTION =
        new Convention.Key("inspector/tips/neverDeclareException");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_NEVER_THROW_THROWABLE =
        new Convention.Key("inspector/tips/neverDeclareThrowable");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_DONT_IGNORE_EXCEPTION =
        new Convention.Key("inspector/tips/dontIgnoreExceptions");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_NEVER_WAIT_OUTSIDE_LOOP =
        new Convention.Key("inspector/tips/neverInvokeWaitOutsideLoop");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_AVOID_THREAD_GROUPS =
        new Convention.Key("inspector/tips/avoidThreadGroups");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_VARIABLE_SHADOW =
        new Convention.Key("inspector/tips/avoidVariableShadowing");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_DECLARE_COLLECTION_VARIABLE_COMMENT =
        new Convention.Key("inspector/tips/addCommentForCollections");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_EMPTY_FINALLY =
        new Convention.Key("inspector/tips/neverUseEmptyFinally");

    /**
     * Perform this code inspection? (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_DECLARE_COLLECTION_COMMENT =
        new Convention.Key("inspector/tips/declareCollectionComment");

    /**
     * Perform this code inspection (<em>boolean</em>)
     *
     * @since 1.0b8
     */
    public static final Convention.Key TIP_WRONG_COLLECTION_COMMENT =
        new Convention.Key("inspector/tips/wrongCollectionComment");

    /** The version number of the current code convention. For internal use only. */
    public static final Convention.Key INTERNAL_VERSION =
        new Convention.Key("internal/version");

    /** Level of the {@link Loggers#IO} (<em>int</em>). */
    public static final Convention.Key MSG_PRIORITY_IO =
        new Convention.Key("messages/priority/general");

    /** Level of the {@link Loggers#PARSER} (<em>int</em>). */
    public static final Convention.Key MSG_PRIORITY_PARSER =
        new Convention.Key("messages/priority/parser");

    /** Level of the {@link Loggers#PARSER_JAVADOC} (<em>int</em>). */
    public static final Convention.Key MSG_PRIORITY_PARSER_JAVADOC =
        new Convention.Key("messages/priority/parserJavadoc");

    /** Level of the {@link Loggers#PRINTER} (<em>int</em>). */
    public static final Convention.Key MSG_PRIORITY_PRINTER =
        new Convention.Key("messages/priority/printer");

    /** Level of the {@link Loggers#PRINTER_JAVADOC} (<em>int</em>). */
    public static final Convention.Key MSG_PRIORITY_PRINTER_JAVADOC =
        new Convention.Key("messages/priority/printerJavadoc");

    /** Level of the {@link Loggers#TRANSFORM} (<em>int</em>). */
    public static final Convention.Key MSG_PRIORITY_TRANSFORM =
        new Convention.Key("messages/priority/transform");

    /** Display stacktrace for errors? (<em>boolean</em>) */
    public static final Convention.Key MSG_SHOW_ERROR_STACKTRACE =
        new Convention.Key("messages/showErrorStackTrace");

    /** Number of processing threads to use (<em>int</em>). */
    public static final Convention.Key THREAD_COUNT =
        new Convention.Key("misc/threadCount");

    /**
     * Specifies whether method declaration parameters should be aligned
     * (<em>boolean</em>).
     */
    public static final Convention.Key ALIGN_PARAMS_METHOD_DEF =
        new Convention.Key("printer/alignment/parameterMethodDeclaration");

    /**
     * Align the indiviual parts of the ternary operator? (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key ALIGN_TERNARY_OPERATOR =
        new Convention.Key("printer/alignment/ternaryOperator");

    /**
     * Force alignment of indiviual method call chains? (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key ALIGN_METHOD_CALL_CHAINS =
        new Convention.Key("printer/alignment/methodCallChain");

    /** Specifies whether variable assignments should be aligned (<em>boolean</em>). */
    public static final Convention.Key ALIGN_VAR_ASSIGNS =
        new Convention.Key("printer/alignment/variableAssignment");

    /** Specifies whether variable deceleration assignments should be aligned (<em>boolean</em>). */
    public static final Convention.Key ALIGN_VAR_DECL_ASSIGNS =
        new Convention.Key("printer/alignment/variableDeclAssignment");

    /** Specifies whether variable identifiers should be aligned (<em>boolean</em>). */
    public static final Convention.Key ALIGN_VAR_IDENTS =
        new Convention.Key("printer/alignment/variableIdentifier");

    /** The directory where backup files are to be stored (<em>String</em>). */
    public static final Convention.Key BACKUP_DIRECTORY =
        new Convention.Key("printer/backup/directory");

    /** Number of backup files to hold (<em>int</em>). */
    public static final Convention.Key BACKUP_LEVEL =
        new Convention.Key("printer/backup/level");

    /** Number of blank lines after a block (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_AFTER_BLOCK =
        new Convention.Key("printer/blanklines/after/block");

    /** Force the given number of blank lines after left curly braces (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_AFTER_BRACE_LEFT =
        new Convention.Key("printer/blanklines/after/braceLeft");

    /** Number of blank lines after classes (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_AFTER_CLASS =
        new Convention.Key("printer/blanklines/after/class");

    /** Number of blank lines after declarations (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_AFTER_DECLARATION =
        new Convention.Key("printer/blanklines/after/declaration");

    /** Number of blank lines after the footer (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_AFTER_FOOTER =
        new Convention.Key("printer/blanklines/after/footer");

    /** Number of blank lines after the header (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_AFTER_HEADER =
        new Convention.Key("printer/blanklines/after/header");

    /** Number of blank lines after the last import statement (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_AFTER_IMPORT =
        new Convention.Key("printer/blanklines/after/lastImport");

    /** Number of blank lines after interfaces (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_AFTER_INTERFACE =
        new Convention.Key("printer/blanklines/after/interface");

    /** Number of blank lines after methods (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_AFTER_METHOD =
        new Convention.Key("printer/blanklines/after/method");

    /** Number of blank lines after the package statement (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_AFTER_PACKAGE =
        new Convention.Key("printer/blanklines/after/package");

    /** Number of blank lines before a block (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_BEFORE_BLOCK =
        new Convention.Key("printer/blanklines/before/block");

    /** Force the given number of blank lines before right curly braces (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_BEFORE_BRACE_RIGHT =
        new Convention.Key("printer/blanklines/before/braceRight");

    /** Number of blank lines before a case block (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_BEFORE_CASE_BLOCK =
        new Convention.Key("printer/blanklines/before/caseBlock");

    /** Number of blank lines before Javadoc comments (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_BEFORE_COMMENT_JAVADOC =
        new Convention.Key("printer/blanklines/before/comment/javadoc");

    /** Number of blank lines before multi-line comments (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_BEFORE_COMMENT_MULTI_LINE =
        new Convention.Key("printer/blanklines/before/comment/multiline");

    /** Number of blank lines before single-line commenents (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE =
        new Convention.Key("printer/blanklines/before/comment/singleline");

    /** Number of blank lines before a flow control statement (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_BEFORE_CONTROL =
        new Convention.Key("printer/blanklines/before/controlStatement");

    /** Number of blank lines before declarations (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_BEFORE_DECLARATION =
        new Convention.Key("printer/blanklines/before/declaration");

    /** Number of blank lines before the footer (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_BEFORE_FOOTER =
        new Convention.Key("printer/blanklines/before/footer");

    /** Number of blank lines before the header (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_BEFORE_HEADER =
        new Convention.Key("printer/blanklines/before/header");

    /** Number of blank lines to keep up to (<em>int</em>). */
    public static final Convention.Key BLANK_LINES_KEEP_UP_TO =
        new Convention.Key("printer/blanklines/keepUpTo");

    /** Cuddle empty braces? (<em>boolean</em>) */
    public static final Convention.Key BRACE_EMPTY_CUDDLE =
        new Convention.Key("printer/braces/empty/cuddle");

    /** Insert an empty statement into empty braces? (<em>boolean</em>) */
    public static final Convention.Key BRACE_EMPTY_INSERT_STATEMENT =
        new Convention.Key("printer/braces/empty/insertStatement");

    /** Insert braces around single if-else statements? (<em>boolean</em>) */
    public static final Convention.Key BRACE_INSERT_IF_ELSE =
        new Convention.Key("printer/braces/insert/ifelse");

    /** Insert unneccessary  braces around single for statements? (<em>boolean</em>) */
    public static final Convention.Key BRACE_INSERT_FOR =
        new Convention.Key("printer/braces/insert/for");

    /** Insert braces around single while statements? (<em>boolean</em>) */
    public static final Convention.Key BRACE_INSERT_WHILE =
        new Convention.Key("printer/braces/insert/while");

    /** Insert braces around single do-while statements? (<em>boolean</em>) */
    public static final Convention.Key BRACE_INSERT_DO_WHILE =
        new Convention.Key("printer/braces/insert/dowhile");

    /** Remove unneccessary braces around single if-else statements? (<em>boolean</em>) */
    public static final Convention.Key BRACE_REMOVE_IF_ELSE =
        new Convention.Key("printer/braces/remove/ifelse");

    /** Remove unneccessary  braces around single for statements? (<em>boolean</em>) */
    public static final Convention.Key BRACE_REMOVE_FOR =
        new Convention.Key("printer/braces/remove/for");

    /** Remove braces around single while statements? (<em>boolean</em>) */
    public static final Convention.Key BRACE_REMOVE_WHILE =
        new Convention.Key("printer/braces/remove/while");

    /** Remove braces around single do-while statements? (<em>boolean</em>) */
    public static final Convention.Key BRACE_REMOVE_DO_WHILE =
        new Convention.Key("printer/braces/remove/dowhile");

    /** Remove unneccessary braces for blocks? (<em>boolean</em>) */
    public static final Convention.Key BRACE_REMOVE_BLOCK =
        new Convention.Key("printer/braces/remove/block");

    /** Print a newline after the last curly brace? (<em>boolean</em>) */
    public static final Convention.Key INSERT_TRAILING_NEWLINE =
        new Convention.Key("printer/misc/insertTrailingNewline");

    /** Should class and method blocks be treated different? (<em>boolean</em>) */
    public static final Convention.Key BRACE_TREAT_DIFFERENT =
        new Convention.Key("printer/braces/treatDifferent/methodClass");

    /**
     * Print left braces of class/interface/ctor/method declarations on a new line if
     * either the parameter list or extends, implements or throws clause is wrapped?
     * (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key BRACE_TREAT_DIFFERENT_IF_WRAPPED =
        new Convention.Key("printer/braces/treatDifferent/methodClassIfWrapped");

    /**
     * Should the brackets for array types be printed after the type or after the
     * identifier? (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key ARRAY_BRACKETS_AFTER_IDENT =
        new Convention.Key("printer/misc/arrayBracketsAfterIdent");

    /** Enable chunk detection by blank lines? (<em>boolean</em>) */
    public static final Convention.Key CHUNKS_BY_BLANK_LINES =
        new Convention.Key("printer/chunks/blanklines");

    /** Enable chunk detection by comments? (<em>boolean</em>) */
    public static final Convention.Key CHUNKS_BY_COMMENTS =
        new Convention.Key("printer/chunks/comments");

    /** Format multi-line comments? (<em>boolean</em>) */
    public static final Convention.Key COMMENT_FORMAT_MULTI_LINE =
        new Convention.Key("printer/comments/format/multiline");

    /** Check Javadoc standard tags? (<em>boolean</em>) */
    public static final Convention.Key COMMENT_JAVADOC_CHECK_TAGS =
        new Convention.Key("printer/comments/javadoc/check/tags");

    /**
     * Should Javadoc &064;throws tags be checked or not? (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key COMMENT_JAVADOC_CHECK_TAGS_THROWS =
        new Convention.Key("printer/comments/javadoc/check/throwsTags");

    /**
     * Print Javadoc comments for fields in one line if possible? (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key COMMENT_JAVADOC_FIELDS_SHORT =
        new Convention.Key("printer/comments/javadoc/fieldsShort");

    /**
     * Javadoc template for interfaces (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_INTERFACE =
        new Convention.Key("printer/comments/javadoc/templates/interface");

    /**
     * Javadoc template for classes (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_CLASS =
        new Convention.Key("printer/comments/javadoc/templates/class");

    /**
     * Javadoc template for variables (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_VARIABLE =
        new Convention.Key("printer/comments/javadoc/templates/variable");

    /**
     * Javadoc template for methods, top part (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_METHOD_TOP =
        new Convention.Key("printer/comments/javadoc/templates/method/top");

    /**
     * Javadoc template for methods, parameter part (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM =
        new Convention.Key("printer/comments/javadoc/templates/method/param");

    /**
     * Javadoc template for methods, exceptions part (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_METHOD_EXCEPTION =
        new Convention.Key("printer/comments/javadoc/templates/method/exception");

    /**
     * Javadoc template for methods, return part (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_METHOD_RETURN =
        new Convention.Key("printer/comments/javadoc/templates/method/return");

    /**
     * Javadoc template for methods, bottom part (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_METHOD_BOTTOM =
        new Convention.Key("printer/comments/javadoc/templates/method/bottom");

    /**
     * Javadoc template for constructors, top part (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_CTOR_TOP =
        new Convention.Key("printer/comments/javadoc/templates/constructor/top");

    /**
     * Javadoc template for constructors, exceptions part (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_CTOR_EXCEPTION =
        new Convention.Key("printer/comments/javadoc/templates/constructor/exception");

    /**
     * Javadoc template for constructors, parameters part (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_CTOR_PARAM =
        new Convention.Key("printer/comments/javadoc/templates/constructor/param");

    /**
     * Javadoc template for constructors, bottom part (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key COMMENT_JAVADOC_TEMPLATE_CTOR_BOTTOM =
        new Convention.Key("printer/comments/javadoc/templates/constructor/bottom");

    /** Insert missing Javadoc comment for classes/interfaces? (<em>int</em>) */
    public static final Convention.Key COMMENT_JAVADOC_CLASS_MASK =
        new Convention.Key("printer/comments/javadoc/generate/class");

    /** Insert missing Javadoc comment for constructors? (<em>int</em>) */
    public static final Convention.Key COMMENT_JAVADOC_CTOR_MASK =
        new Convention.Key("printer/comments/javadoc/generate/constructor");

    /** Insert missing Javadoc comment for variables? (<em>int</em>) */
    public static final Convention.Key COMMENT_JAVADOC_VARIABLE_MASK =
        new Convention.Key("printer/comments/javadoc/generate/field");

    /** Insert missing Javadoc comment for methods? (<em>int</em>) */
    public static final Convention.Key COMMENT_JAVADOC_METHOD_MASK =
        new Convention.Key("printer/comments/javadoc/generate/method");

    /** Insert missing Javadoc comments for inner classes too? (<em>boolean</em>) */
    public static final Convention.Key COMMENT_JAVADOC_INNER_CLASS =
        new Convention.Key("printer/comments/javadoc/check/innerclass");

    /** Dont insert missing Javadoc comments if multiline comment exists (<em>boolean</em>) */
    public static final Convention.Key DONT_COMMENT_JAVADOC_WHEN_ML =
        new Convention.Key("printer/comments/javadoc/skip/ifml");

    /** Parse Javadoc comments or add AS IS? (<em>boolean</em>) */
    public static final Convention.Key COMMENT_JAVADOC_PARSE =
        new Convention.Key("printer/comments/javadoc/parseComments");

    /** Remove Javadoc comments? (<em>boolean</em>) */
    public static final Convention.Key COMMENT_JAVADOC_REMOVE =
        new Convention.Key("printer/comments/remove/javadoc");

    /** Transform non-Javadoc comments to Javadoc comments? (<em>boolean</em>) */
    public static final Convention.Key COMMENT_JAVADOC_TRANSFORM =
        new Convention.Key("printer/comments/javadoc/transform");

    /** Remove multi-line comments? (<em>boolean</em>) */
    public static final Convention.Key COMMENT_REMOVE_MULTI_LINE =
        new Convention.Key("printer/comments/remove/multiline");

    /** Remove single-line comments? (<em>boolean</em>) */
    public static final Convention.Key COMMENT_REMOVE_SINGLE_LINE =
        new Convention.Key("printer/comments/remove/singleline");

    /** Insert a footer? (<em>boolean</em>) */
    public static final Convention.Key FOOTER = new Convention.Key("printer/footer/use");

    /** Ignore footer if it exists? (<em>boolean</em>) */
    public static final Convention.Key FOOTER_IGNORE_IF_EXISTS = new Convention.Key("printer/footer/ignoreIfExists");

    /** Identify keys of the footers that are to be deleted (<em>String</em>). */
    public static final Convention.Key FOOTER_KEYS =
        new Convention.Key("printer/footer/keys");

    /** The footer text (<em>String</em>). */
    public static final Convention.Key FOOTER_TEXT =
        new Convention.Key("printer/footer/text");

    /**
     * Should the processing of a source file be forced although the file hasn't changed?
     * (<em>boolean</em>)
     */
    public static final Convention.Key FORCE_FORMATTING =
        new Convention.Key("printer/misc/forceFormatting");

    /**
     * Should the "final" modifier be added for method parameters?
     * (<em>boolean</em>)
     */
    public static final Convention.Key INSERT_FINAL_MODIFIER_FOR_METHOD_PARAMETERS =
        new Convention.Key("printer/misc/method/forceFinalModifier");

    /**
     * Should the "final" modifier be added for method parameters?
     * (<em>boolean</em>)
     */
    public static final Convention.Key INSERT_FINAL_MODIFIER_FOR_PARAMETERS =
        new Convention.Key("printer/misc/forceFinalModifier");

    /** Insert a header? (<em>boolean</em>) */
    public static final Convention.Key HEADER = new Convention.Key("printer/header/use");

    /** Ignore header if it exists? (<em>boolean</em>) */
    public static final Convention.Key HEADER_IGNORE_IF_EXISTS = new Convention.Key("printer/header/ignoreIfExists");

    /**
     * Number of comments before the first node (an opening curly brace) that should be
     * treated as header comments (<em>int</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key HEADER_SMART_MODE_LINES =
        new Convention.Key("printer/header/smartMode");

    /**
     * Number of comments after the last node (a closing curly brace) that should be
     * treated as footer comments (<em>int</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key FOOTER_SMART_MODE_LINES =
        new Convention.Key("printer/footer/smartMode");

    /** Identify keys of the headers that are to be deleted (<em>String</em>). */
    public static final Convention.Key HEADER_KEYS =
        new Convention.Key("printer/header/keys");

    /** The header text (<em>String</em>). */
    public static final Convention.Key HEADER_TEXT =
        new Convention.Key("printer/header/text");

    /**
     * The history policy to use (<em>String</em>). Either &quot;none&quot;,
     * &quot;file&quot; or &quot;comment&quot;.
     */
    public static final Convention.Key HISTORY_POLICY =
        new Convention.Key("printer/history/policy");

    /**
     * The method used to identify changed files.
     *
     * @since 1.0b9
     */
    public static final Convention.Key HISTORY_METHOD =
        new Convention.Key("printer/history/method");

    /** Directory to store class repository files (<em>String</em>). */
    public static final Convention.Key CLASS_REPOSITORY_DIRECTORY =
        new Convention.Key("printer/imports/classRepositoryDirectory");

    /** Specifies the import optimization policy (<em>String</em>). */
    public static final Convention.Key IMPORT_POLICY =
        new Convention.Key("printer/imports/policy");

    /** Holds grouping info for distinct package names (<em>String</em>). */
    public static final Convention.Key IMPORT_GROUPING =
        new Convention.Key("printer/imports/grouping/packages");

    /** Default import grouping depth (<em>int</em>). */
    public static final Convention.Key IMPORT_GROUPING_DEPTH =
        new Convention.Key("printer/imports/grouping/defaultDepth");

    /** Sort import statements? (<em>boolean</em>) */
    public static final Convention.Key IMPORT_SORT =
        new Convention.Key("printer/imports/sort");

    /** Indent case block in switch statements? (<em>boolean</em>) */
    public static final Convention.Key INDENT_CASE_FROM_SWITCH =
        new Convention.Key("printer/indentation/caseFromSwitch");

    /**
     * Should standard indentation be used to indent wrapped lines, or rather the deep
     * indentation policy? (<em>boolean</em>).
     */
    public static final Convention.Key INDENT_DEEP =
        new Convention.Key("printer/indentation/policy/deep");

    /** Should continuation indentation be used for operators? (<em>boolean</em>). */
    public static final Convention.Key INDENT_CONTINUATION_OPERATOR =
        new Convention.Key("printer/indentation/continuation/operator");

    /** Continuation indent size (<em>int</em>). */
    public static final Convention.Key INDENT_SIZE_CONTINUATION =
        new Convention.Key("printer/indentation/sizes/continuation");

    /**
     * Should continuation indentation be used for statement blocks? (<em>boolean</em>).
     */
    public static final Convention.Key INDENT_CONTINUATION_BLOCK =
        new Convention.Key("printer/indentation/continuation/block");

    /** Indent first column comments? (<em>boolean</em>) */
    public static final Convention.Key INDENT_FIRST_COLUMN_COMMENT =
        new Convention.Key("printer/indentation/firstColumnComments");

    /** Indent labels? (<em>boolean</em>) */
    public static final Convention.Key INDENT_LABEL =
        new Convention.Key("printer/indentation/label");

    /** Amount of space to use for indentation (<em>int</em>). */
    public static final Convention.Key INDENT_SIZE =
        new Convention.Key("printer/indentation/sizes/general");

    /** Indentation before cuddled braces (<em>int</em>). */
    public static final Convention.Key INDENT_SIZE_BRACE_CUDDLED =
        new Convention.Key("printer/indentation/sizes/braceCuddled");

    /** Indentation before a left curly brace (<em>int</em>). */
    public static final Convention.Key INDENT_SIZE_BRACE_LEFT =
        new Convention.Key("printer/indentation/sizes/braceLeft");

    /** Indentation before a right curly brace (<em>int</em>). */
    public static final Convention.Key INDENT_SIZE_BRACE_RIGHT =
        new Convention.Key("printer/indentation/sizes/braceRight");

    /** Indentation after a right curly brace (<em>int</em>). */
    public static final Convention.Key INDENT_SIZE_BRACE_RIGHT_AFTER =
        new Convention.Key("printer/indentation/sizes/braceRightAfter");

    /** Indentation before an endline comment (<em>int</em>). */
    public static final Convention.Key INDENT_SIZE_COMMENT_ENDLINE =
        new Convention.Key("printer/indentation/sizes/trailingComment");

    /** Maximal amount of spaces for wrapping should be forced (<em>int</em>). */
    public static final Convention.Key INDENT_SIZE_DEEP =
        new Convention.Key("printer/indentation/sizes/deep");

    /**
     * Indentation for extends types (<em>int</em>).
     *
     * @since 1.0b7
     */
    public static final Convention.Key INDENT_SIZE_EXTENDS =
        new Convention.Key("printer/indentation/sizes/extends");

    /**
     * Indentation for implements types (<em>int</em>).
     *
     * @since 1.0b7
     */
    public static final Convention.Key INDENT_SIZE_IMPLEMENTS =
        new Convention.Key("printer/indentation/sizes/implements");

    /** Indentation before every line (<em>int</em>). */
    public static final Convention.Key INDENT_SIZE_LEADING =
        new Convention.Key("printer/indentation/sizes/leading");

    /**
     * Indentation for throws types (<em>int</em>).
     *
     * @since 1.0b7
     */
    public static final Convention.Key INDENT_SIZE_THROWS =
        new Convention.Key("printer/indentation/sizes/throws");

    /** Number of spaces to assume for tabs (<em>int</em>). */
    public static final Convention.Key INDENT_SIZE_TABS =
        new Convention.Key("printer/indentation/sizes/tabs");

    /** Fill gaps with tabs? (<em>boolean</em>) */
    public static final Convention.Key INDENT_WITH_TABS =
        new Convention.Key("printer/indentation/tabs/enable");

    /** Use spaces for continuation after tabs? (<em>boolean</em>) */
    public static final Convention.Key INDENT_WITH_TABS_ONLY_LEADING =
        new Convention.Key("printer/indentation/tabs/onlyLeading");

    /**
     * Insert parenthesis around expressions to make precedence clear? (<em>boolean</em>)
     */
    public static final Convention.Key INSERT_EXPRESSION_PARENTHESIS =
        new Convention.Key("printer/misc/insertExpressionParentheses");

    /** Insert conditional expresssion for debug logging calls? (<em>boolean</em>) */
    public static final Convention.Key INSERT_LOGGING_CONDITIONAL =
        new Convention.Key("printer/misc/insertLoggingConditional");

    /** Insert serial version UID for serializable classes? (<em>boolean</em>) */
    public static final Convention.Key INSERT_SERIAL_UID =
        new Convention.Key("printer/misc/insertUID");

    /**
     * Custom Javadoc in-line tags definitions (<em>String</em>).
     *
     * @since 1.0b7
     */
    public static final Convention.Key COMMENT_JAVADOC_TAGS_INLINE =
        new Convention.Key("printer/comments/javadoc/tags/in-line");

    /**
     * Custom Javadoc standard tags definitions (<em>String</em>).
     *
     * @since 1.0b7
     */
    public static final Convention.Key COMMENT_JAVADOC_TAGS_STANDARD =
        new Convention.Key("printer/comments/javadoc/tags/standard");

    /** Pad assignment operators? (<em>boolean</em>) */
    public static final Convention.Key PADDING_ASSIGNMENT_OPERATORS =
        new Convention.Key("printer/whitespace/padding/operator/assignment");

    /** Pad bitwise operators? (<em>boolean</em>) */
    public static final Convention.Key PADDING_BITWISE_OPERATORS =
        new Convention.Key("printer/whitespace/padding/operator/bitwise");

    /**
     * Insert spaces after the left, and before the right curly brace for array
     * initializations? (<em>boolean</em>)
     */
    public static final Convention.Key PADDING_BRACES =
        new Convention.Key("printer/whitespace/padding/braces");

    /** Insert spaces after the left, and before the right bracket? (<em>boolean</em>) */
    public static final Convention.Key PADDING_BRACKETS =
        new Convention.Key("printer/whitespace/padding/brackets");

    /**
     * Insert spaces after the left, and before the right type cast parenthesis?
     * (<em>boolean</em>)
     */
    public static final Convention.Key PADDING_CAST =
        new Convention.Key("printer/whitespace/padding/typeCast");

    /** Pad logical operators? (<em>boolean</em>) */
    public static final Convention.Key PADDING_LOGICAL_OPERATORS =
        new Convention.Key("printer/whitespace/padding/operator/logical");

    /** Pad mathematical operators? (<em>boolean</em>) */
    public static final Convention.Key PADDING_MATH_OPERATORS =
        new Convention.Key("printer/whitespace/padding/operator/mathematical");

    /**
     * Insert spaces after the left, and before the right parenthesis? (<em>boolean</em>)
     */
    public static final Convention.Key PADDING_PAREN =
        new Convention.Key("printer/whitespace/padding/parenthesis");

    /** Pad relational operators? (<em>boolean</em>) */
    public static final Convention.Key PADDING_RELATIONAL_OPERATORS =
        new Convention.Key("printer/whitespace/padding/operator/relational");

    /** Pad shift operators? (<em>boolean</em>) */
    public static final Convention.Key PADDING_SHIFT_OPERATORS =
        new Convention.Key("printer/whitespace/padding/operator/shift");

    /** Sort the different elements of a Java source file? (<em>boolean</em>) */
    public static final Convention.Key SORT =
        new Convention.Key("printer/sorting/declaration/enable");

    /** Sort classes declarations? (<em>boolean</em>) */
    public static final Convention.Key SORT_CLASS =
        new Convention.Key("printer/sorting/declaration/class");

    /** Sort annotation declarations? (<em>boolean</em>) */
    public static final Convention.Key SORT_ANNOTATION =
        new Convention.Key("printer/sorting/declaration/annotation");

    /** Sort enum declarations? (<em>boolean</em>) */
    public static final Convention.Key SORT_ENUM =
        new Convention.Key("printer/sorting/declaration/enum");

    /** Sort constructors declarations? (<em>boolean</em>) */
    public static final Convention.Key SORT_CTOR =
        new Convention.Key("printer/sorting/declaration/constructor");

    /** Sort interfaces declarations? (<em>boolean</em>) */
    public static final Convention.Key SORT_INTERFACE =
        new Convention.Key("printer/sorting/declaration/interface");

    /** Sort methods declarations? (<em>boolean</em>) */
    public static final Convention.Key SORT_METHOD =
        new Convention.Key("printer/sorting/declaration/method");
    
    /** Sort method bean declarations? (<em>boolean</em>) */
    public static final Convention.Key SORT_METHOD_BEAN =
        new Convention.Key("printer/sorting/declaration/method/bean");

    /** Sort modifers? (<em>boolean</em>) */
    public static final Convention.Key SORT_MODIFIERS =
        new Convention.Key("printer/sorting/modifier/enable");

    /** String indicating the declaration sort order (<em>String</em>). */
    public static final Convention.Key SORT_ORDER =
        new Convention.Key("printer/sorting/declaration/order");

    /** String indicating the modifiers sort order (<em>String</em>). */
    public static final Convention.Key SORT_ORDER_MODIFIERS =
        new Convention.Key("printer/sorting/modifier/order");

    /** Sort variable declarations? (<em>boolean</em>) */
    public static final Convention.Key SORT_VARIABLE =
        new Convention.Key("printer/sorting/declaration/variable");

    /**
     * String encoded environment variables (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key ENVIRONMENT =
        new Convention.Key("printer/environment");

    /** Print a space after type casting? (<em>boolean</em>) */
    public static final Convention.Key SPACE_AFTER_CAST =
        new Convention.Key("printer/whitespace/after/typeCast");

    /** Print a space after a comma? (<em>boolean</em>) */
    public static final Convention.Key SPACE_AFTER_COMMA =
        new Convention.Key("printer/whitespace/after/comma");

    /** Print a space after a semicolon? (<em>boolean</em>) */
    public static final Convention.Key SPACE_AFTER_SEMICOLON =
        new Convention.Key("printer/whitespace/after/semicolon");

    /** Print a space before braces (of arrays)? (<em>boolean</em>) */
    public static final Convention.Key SPACE_BEFORE_BRACES =
        new Convention.Key("printer/whitespace/before/braces");

    /** Print a space before brackets? (<em>boolean</em>) */
    public static final Convention.Key SPACE_BEFORE_BRACKETS =
        new Convention.Key("printer/whitespace/before/brackets");

    /** Print a space before types with brackets (<em>int</em>). */
    public static final Convention.Key SPACE_BEFORE_BRACKETS_TYPES =
        new Convention.Key("printer/whitespace/before/bracketsTypes");

    /** Print a space before the colon of a case block? (<em>boolean</em>) */
    public static final Convention.Key SPACE_BEFORE_CASE_COLON =
        new Convention.Key("printer/whitespace/before/caseColon");

    /** Print a space before the negation of boolean expressions? (<em>boolean</em>) */
    public static final Convention.Key SPACE_BEFORE_LOGICAL_NOT =
        new Convention.Key("printer/whitespace/before/operator/not");

    /** Print a space before method call parenthesis? (<em>boolean</em>) */
    public static final Convention.Key SPACE_BEFORE_METHOD_CALL_PAREN =
        new Convention.Key("printer/whitespace/before/parentheses/methodCall");

    /** Print a space before method definition parenthesis? (<em>boolean</em>) */
    public static final Convention.Key SPACE_BEFORE_METHOD_DEF_PAREN =
        new Convention.Key("printer/whitespace/before/parentheses/methodDeclaration");

    /** Print a space before java statement parenthesis? (<em>boolean</em>) */
    public static final Convention.Key SPACE_BEFORE_STATEMENT_PAREN =
        new Convention.Key("printer/whitespace/before/parentheses/statement");

    /** Strip qualification for identifiers? (<em>boolean</em>) */
    public static final Convention.Key STRIP_QUALIFICATION =
        new Convention.Key("printer/parser/stripQualification");

    /**
     * The fill character to use for the separator comments (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key SEPARATOR_FILL_CHARACTER =
        new Convention.Key("printer/comments/separator/fillCharacter");

    /**
     * Insert separator comments between the different tree portions (
     * class/interface/variable/method/constructor/initialiation declarations)?
     * (<em>boolean</em>)
     */
    public static final Convention.Key COMMENT_INSERT_SEPARATOR =
        new Convention.Key("printer/comments/separator/insert");

    /**
     * Insert separator comments between the different tree portions of inner
     * classes/interfaces (a.k.a recursively)? (<em>boolean</em>)
     */
    public static final Convention.Key COMMENT_INSERT_SEPARATOR_RECURSIVE =
        new Convention.Key("printer/comments/separator/insertRecursive");

    /**
     * Separator text for the static variables and initalizers section (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key SEPARATOR_STATIC_VAR_INIT =
        new Convention.Key("printer/comments/separator/text/static");

    /**
     * Separator text for the instance variables section (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key SEPARATOR_INSTANCE_VAR =
        new Convention.Key("printer/comments/separator/text/field");

    /**
     * Separator text for the instance initializers section (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key SEPARATOR_INSTANCE_INIT =
        new Convention.Key("printer/comments/separator/text/initializer");

    /**
     * Separator text for the annotation section (<em>String</em>).
     *
     * @since 1.5b1
     */
    public static final Convention.Key SEPARATOR_ANNOTATION_INIT =
        new Convention.Key("printer/comments/separator/text/annotation");

    /**
     * Separator text for the enumeration section (<em>String</em>).
     *
     * @since 1.5b1
     */
    public static final Convention.Key SEPARATOR_ENUM_INIT =
        new Convention.Key("printer/comments/separator/text/enum");

    /**
     * Separator text for the enumeration constant section (<em>String</em>).
     *
     * @since 1.5b4
     */
    public static final Convention.Key SEPARATOR_ENUM_CONSTANT_INIT =
        new Convention.Key("printer/comments/separator/text/enum/constant");

    /**
     * Separator text for the constructors section (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key SEPARATOR_CTOR =
        new Convention.Key("printer/comments/separator/text/constructor");

    /**
     * Separator text for the methods section (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key SEPARATOR_METHOD =
        new Convention.Key("printer/comments/separator/text/method");

    /**
     * Separator text for the interfaces section (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key SEPARATOR_INTERFACE =
        new Convention.Key("printer/comments/separator/text/interface");

    /**
     * Separator text for the inner classes section (<em>String</em>).
     *
     * @since 1.0b8
     */
    public static final Convention.Key SEPARATOR_CLASS =
        new Convention.Key("printer/comments/separator/text/class");

    /** Number of characters in each line(<em>int</em>). */
    public static final Convention.Key LINE_LENGTH =
        new Convention.Key("printer/wrapping/general/lineLength");

    /** Use line wrapping? (<em>boolean</em>) */
    public static final Convention.Key LINE_WRAP =
        new Convention.Key("printer/wrapping/general/enable");

    /**
     * Prefer wrapping after assignments? (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key LINE_WRAP_AFTER_ASSIGN =
        new Convention.Key("printer/wrapping/ondemand/after/assignment");

    /**
     * Wrap and indent expressions in grouping parentheses ? (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key LINE_WRAP_PAREN_GROUPING =
        new Convention.Key("printer/wrapping/ondemand/groupingParentheses");

    /**
     * Prefer line wrapping after the left parentheses of parameter/expression lists?
     * (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key LINE_WRAP_AFTER_LEFT_PAREN =
        new Convention.Key("printer/wrapping/ondemand/after/leftParenthesis");

    /**
     * Insert a newline before the right parentheses of parameter/epxression lists?
     * (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key LINE_WRAP_BEFORE_RIGHT_PAREN =
        new Convention.Key("printer/wrapping/ondemand/before/rightParenthesis");

    /** Should line wrapping be performed before or after operators? (<em>boolean</em>) */
    public static final Convention.Key LINE_WRAP_BEFORE_OPERATOR =
        new Convention.Key("printer/wrapping/general/beforeOperator");

    /** Force wrapping/alignment after parameters for method calls? (<em>boolean</em>) */
    public static final Convention.Key LINE_WRAP_AFTER_PARAMS_METHOD_CALL =
        new Convention.Key("printer/wrapping/always/parameter/methodCall");

    /**
     * Force wrapping/alignment of chained method? (<em>boolean</em>)
     *
     * @since 1.0b7
     */
    public static final Convention.Key LINE_WRAP_AFTER_CHAINED_METHOD_CALL =
        new Convention.Key("printer/wrapping/always/after/methodCallChained");

    /**
     * Force wrapping/alignment after parameters for method calls if they contain at
     * least one other method call? (<em>boolean</em>)
     */
    public static final Convention.Key LINE_WRAP_AFTER_PARAMS_METHOD_CALL_IF_NESTED =
        new Convention.Key("printer/wrapping/always/parameter/methodCallNested");

    /**
     * Force wrapping/alignment of parameters for method/constructor declarations?
     * (<em>boolean</em>)
     */
    public static final Convention.Key LINE_WRAP_AFTER_PARAMS_METHOD_DEF =
        new Convention.Key("printer/wrapping/always/parameter/methodDeclaration");

    /** Force wrapping after the first ternary operand? (<em>boolean</em>) */
    public static final Convention.Key ALIGN_TERNARY_EXPRESSION =
        new Convention.Key("printer/wrapping/always/after/ternaryOperator/first");

    /** Force wrapping after the second ternary operand? (<em>boolean</em>) */
    public static final Convention.Key ALIGN_TERNARY_VALUES =
        new Convention.Key("printer/wrapping/always/after/ternaryOperator/second");

    /** Print newline after labels? (<em>boolean</em>) */
    public static final Convention.Key LINE_WRAP_AFTER_LABEL =
        new Convention.Key("printer/wrapping/always/after/label");

    /** Print newline before left braces? (<em>boolean</em>) */
    public static final Convention.Key BRACE_NEWLINE_LEFT =
        new Convention.Key("printer/wrapping/always/before/braceLeft");

    /** Print newline after right braces? (<em>boolean</em>) */
    public static final Convention.Key BRACE_NEWLINE_RIGHT =
        new Convention.Key("printer/wrapping/always/after/braceRight");

    /**
     * Force alignment of extends types for class/interface declarations?
     * (<em>boolean</em>)
     */
    public static final Convention.Key LINE_WRAP_AFTER_TYPES_EXTENDS =
        new Convention.Key("printer/wrapping/always/after/extendsTypes");

    /**
     * Force alignment of implements types for class/interface declarations?
     * (<em>boolean</em>)
     */
    public static final Convention.Key LINE_WRAP_AFTER_TYPES_IMPLEMENTS =
        new Convention.Key("printer/wrapping/always/after/implementsTypes");

    /**
     * Force wrapping/alignment of exception types for method/ctor declarations?
     * (<em>boolean</em>)
     */
    public static final Convention.Key LINE_WRAP_AFTER_TYPES_THROWS =
        new Convention.Key("printer/wrapping/always/after/throwsTypes");

    /* Force line wrapping before array initialization? (<em>boolean</em>)
    public static final Convention.Key LINE_WRAP_BEFORE_ARRAY_INIT =
        new Convention.Key("printer/wrapping/before/arrayInit");*/

    /** Force wrapping/alignment after given number of array elements (<em>int</em>). */
    public static final Convention.Key LINE_WRAP_ARRAY_ELEMENTS =
        new Convention.Key("printer/wrapping/always/after/arrayElement");
    /**
     * Enumeration LCURLY statrts new line ? (<em>boolean</em>)
     *
     * @since 1.5
     */
    public static final Convention.Key ENUM_LCURLY_NO_NEW_LINE =
        new Convention.Key("printer/wrapping/enum/lcurly/nonewline");
    
    /**
     * Force alignment of wrapping ENUM values after a certain amount ? (<em>int</em>)
     *
     * @since 1.5
     */
    public static final Convention.Key ENUM_ALIGN_VALUES_WHEN_EXCEEDS =
        new Convention.Key("printer/wrapping/enum/align/after");
    
    /**
     * Annotation LCURLY statrts new line ? (<em>boolean</em>)
     *
     * @since 1.5
     */
    public static final Convention.Key ANON_LCURLY_NO_NEW_LINE =
        new Convention.Key("printer/wrapping/anon/lcurly/newline");
    
    /**
     * Annotation Def LCURLY statrts new line ? (<em>boolean</em>)
     *
     * @since 1.5
     */
    public static final Convention.Key ANON_DEF_LCURLY_NO_NEW_LINE =
        new Convention.Key("printer/wrapping/anondef/lcurly/nonewline");
    
    /**
     * Force alignment of wrapping ANON values after a certain amount ? (<em>int</em>)
     *
     * @since 1.5
     */
    public static final Convention.Key ANON_ALIGN_VALUES_WHEN_EXCEEDS =
        new Convention.Key("printer/wrapping/anon/align/after");
    
    /**
     * Force alignment of wrapping ANON DEF values after a certain amount ? (<em>int</em>)
     *
     * @since 1.5
     */
    public static final Convention.Key ANON_DEF_ALIGN_VALUES_WHEN_EXCEEDS =
        new Convention.Key("printer/wrapping/anondef/align/after");
    
    /** Force line wrapping before implements? (<em>boolean</em>) */
    public static final Convention.Key LINE_WRAP_BEFORE_IMPLEMENTS =
        new Convention.Key("printer/wrapping/always/before/implements");

    /** Force line wrapping before extends? (<em>boolean</em>) */
    public static final Convention.Key LINE_WRAP_BEFORE_EXTENDS =
        new Convention.Key("printer/wrapping/always/before/extends");

    /**
     * Force line wrapping before throws? (<em>boolean</em>)
     *
     * @since 1.0b7
     */
    public static final Convention.Key LINE_WRAP_BEFORE_THROWS =
        new Convention.Key("printer/wrapping/always/before/throws");

    /**
     * Force alignment of extends types for class/interface declarations?
     * (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key LINE_WRAP_AFTER_TYPES_EXTENDS_EXCEED =
        new Convention.Key("printer/wrapping/ondemand/after/types/extends");

    /**
     * Force alignment of implements types for class/interface declarations?
     * (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key LINE_WRAP_AFTER_TYPES_IMPLEMENTS_EXCEED =
        new Convention.Key("printer/wrapping/ondemand/after/types/implements");

    /**
     * Force wrapping/alignment of successive parameters/expression if the first
     * parameter/expression was wrapped ? (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key LINE_WRAP_PARAMS_EXCEED =
        new Convention.Key("printer/wrapping/ondemand/after/parameter");

    /**
     *  If it is better to wrap the parameters deeply then do so 
     *  (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key LINE_WRAP_PARAMS_HARD =
        new Convention.Key("printer/wrapping/ondemand/hard/parameter");

    /**
     *  When wrapping parameters always do it deeply 
     *  (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key LINE_WRAP_PARAMS_DEEP =
        new Convention.Key("printer/wrapping/ondemand/deep/parameter");

    /**
     * Force alignment of throws types for method/ctor declarations? (<em>boolean</em>)
     *
     * @since 1.0b9
     */
    public static final Convention.Key LINE_WRAP_AFTER_TYPES_THROWS_EXCEED =
        new Convention.Key("printer/wrapping/ondemand/after/types/throws");

    /** Add comments like end if and end switch after brace blocks */
    public static final Key BRACE_ADD_COMMENT = 
        new Convention.Key("printer/brace/comment");

    /** */
    public static final Key COMMENT_JAVADOC_PARSE_DESCRIPTION = 
        new Convention.Key("printer/comments/javadoc/parseDescription");

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ConventionConvention.Keys object.
     */
    private ConventionKeys()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Determines whether the given key is valid.
     *
     * @param key a code convention key.
     *
     * @return <code>true</code> if the given key is valid.
     *
     * @since 1.0b9
     */
    public static boolean isValid(Convention.Key key)
    {
        Field[] fields = ConventionKeys.class.getDeclaredFields();

        ConventionKeys keys = new ConventionKeys();

        try
        {
            for (int i = 0; i < fields.length; i++)
            {
                Object k = fields[i].get(keys);

                if (key.toString().equals(k.toString()))
                {
                    return true;
                }
            }
        }
        catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
        }

        return false;
    }


    /**
     * Called to dump the keys
     *
     * @param args Args ignored
     */
    public static void main(String[] args)
    {
        try
        {
            dump();
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
        }
    }


    private static void dump()
      throws IllegalAccessException
    {
        Field[] fields = ConventionKeys.class.getDeclaredFields();

        ConventionKeys keys = new ConventionKeys();

        java.util.Map m = new java.util.TreeMap();

        for (int i = 0; i < fields.length; i++)
        {
            java.lang.reflect.Field field = fields[i];
            Object k = field.get(keys);

            if (k instanceof Convention.Key)
            {
                m.put(k.toString(), field.getName());
            }
        }

        for (java.util.Iterator i = m.keySet().iterator(); i.hasNext();)
        {
            Object k = i.next();
            System.out.println(
                "renameKey(settings, \"" + k + "\" /* NOI18N */ , ConventionKeys."
                + m.get(k) + ");");
        }
    }
}
