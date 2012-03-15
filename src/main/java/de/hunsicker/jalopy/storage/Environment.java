/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.storage;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides access to global and local environment variables (key/value pairs).
 * 
 * <p>
 * This class is thread-safe.
 * </p>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 *
 * @since 1.0b8
 */
public final class Environment
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Delimeter for the encoded variables string. */
    private static final String DELIMETER = "|";

    /** The compiler to create pattern objects. */
    //private static final PatternCompiler REGEXP_COMPILER = new Perl5Compiler();

    /** The pattern to search for variables . */
    private static Pattern _variablesPattern;

    static
    {
            _variablesPattern = 
                Pattern.compile(
                    "\\$([a-zA-Z_][a-zA-Z0-9_.]+):?(.*?)\\$");
    }

    private static final Environment INSTANCE = new Environment(true);

    //~ Instance variables ---------------------------------------------------------------

    /** The pattern matcher. */
    private final Matcher _matcher = _variablesPattern.matcher("");

    /** The current environment variables. */
    private Map _variables; // Map of <String:String>

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Environment object.
     *
     * @param initial True if initial
     */
    private Environment(boolean initial)
    {
        if (initial)
        {
            _variables = new HashMap(25);
            _variables.putAll(System.getProperties());

            Convention settings = Convention.getInstance();
            String variables =
                settings.get(ConventionKeys.ENVIRONMENT, ConventionDefaults.ENVIRONMENT);

            if (!"".equals(variables))
            {
                for (
                    StringTokenizer tokens = new StringTokenizer(variables, DELIMETER);
                    tokens.hasMoreElements();)
                {
                    String pair = tokens.nextToken();
                    int delimOffset = pair.indexOf('^');
                    String key = pair.substring(0, delimOffset);
                    String value = pair.substring(delimOffset + 1);
                    _variables.put(key, value);
                }
            }
        }
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns an instance of this class. This method is guarantueed to return a unique
     * instance for each thread that invokes it.
     *
     * @return An instance of this class.
     */
    public static Environment getInstance()
    {
        return INSTANCE;
    }


    /**
     * Returns a copy of this object.
     *
     * @return a copy of this object.
     */
    public Environment copy()
    {
        Environment environment = new Environment(false);
        environment._variables = new HashMap(INSTANCE._variables);

        return environment;
    }


    /**
     * Performs variable interpolation for the given input string. All environment
     * variable expressions (<code>\$[a-zA-Z_][a-zA-Z0-9_.]+\$</code>, e.g. $fileName$
     * or $author$) in the given string are replaced with their corresponding
     * environment value.
     *
     * @param str string to perform variable interpolation for.
     *
     * @return The input <em>str</em> with all variables interpolated.
     *
     * @see #set
     */
    public String interpolate(String str)
    {
        Map keys = new HashMap(10);
        
        _matcher.reset(str);

        // map all found variable expressions with their environment variable
        while (_matcher.find()) 
        {
            String result = _matcher.group(1);
            String pattern = _matcher.group(2);
            String value = null; 
            Variable variable = (Variable) variableMap.get(result);
            if (variable!=null) {
                value = variable.format(_variables.get(result),pattern);
            }
            else{
                value = (String) _variables.get(result);
            }
                

            // the value has to be set in order to be substituted
            if ((value != null) && (value.length() > 0))
            {
                keys.put("\\$" + result + (pattern.length() == 0 ? "" : ":" + pattern)  + "\\$", value);
            }
        }
        
        // and finally interpolate them
        for (Iterator i = keys.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry) i.next();

            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            
            str = str.replaceAll(key,value);
            
        }
        
    return str;
    }

    /**
     * Sets the given variable to the given value.
     *
     * @param variable variable name. Valid variable names have the form
     *        <code>[a-zA-Z_][a-zA-Z0-9_.]+</code>
     * @param value value to associate.
     *
     * @see #unset
     */
    public void set(
        String variable,
        Object value)
    {
        _variables.put(variable, value);
    }


    /**
     * Returns a string representation of this object. The string representation consists
     * of a list of key-value mappings in no particular order.
     *
     * @return a string representation of this object.
     */
    public String toString()
    {
        return _variables.toString();
    }


    /**
     * Unsets (removes) the given variable
     *
     * @param variable variable name.
     *
     * @see #set
     */
    public void unset(String variable)
    {
        _variables.remove(variable);
    }
    
    private static final Map variableMap = getVariableMap();
    
    private static Map getVariableMap() {
        Map vMap = new HashMap(10);
        vMap.put(Variable.FILE.getName(), Variable.FILE);
        vMap.put(Variable.FILE_NAME.getName(), Variable.FILE_NAME);
        vMap.put(Variable.FILE_FORMAT.getName(), Variable.FILE_FORMAT);
        vMap.put(Variable.TAB_SIZE.getName(), Variable.TAB_SIZE);
        vMap.put(Variable.CONVENTION.getName(), Variable.CONVENTION);
        vMap.put(Variable.PACKAGE.getName(), Variable.PACKAGE);
        vMap.put(Variable.TYPE_PARAM.getName(), Variable.TYPE_PARAM);
        vMap.put(Variable.TYPE_EXCEPTION.getName(), Variable.TYPE_EXCEPTION);
        vMap.put(Variable.TYPE_OBJECT.getName(), Variable.TYPE_OBJECT);
        vMap.put(Variable.CLASS_NAME.getName(), Variable.CLASS_NAME);
        vMap.put(Variable.DATE.getName(), Variable.DATE);
        return vMap;
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Represents a local environment variable.
     */
    public static class Variable
    {
        /** Defines the variable &quot;file&quot;. */
        public static final Variable FILE = new Variable("file");

        /** Defines the variable &quot;fileName&quot;. */
        public static final Variable FILE_NAME = new Variable("fileName");

        /** Defines the variable &quot;fileFormat&quot;. */
        public static final Variable FILE_FORMAT = new Variable("fileFormat");

        /** Defines the variable &quot;tabSize&quot;. */
        public static final Variable TAB_SIZE = new Variable("tabSize");

        /** Defines the variable &quot;convention&quot;. */
        public static final Variable CONVENTION = new Variable("convention");

        /** Defines the variable &quot;package&quot;. */
        public static final Variable PACKAGE = new Variable("package");

        /** Defines the variable &quot;paramType&quot;. */
        public static final Variable TYPE_PARAM = new Variable("paramType");

        /** Defines the variable &quot;exceptionType&quot;. */
        public static final Variable TYPE_EXCEPTION = new Variable("exceptionType");

        /** Defines the variable &quot;objectType&quot;. */
        public static final Variable TYPE_OBJECT = new Variable("objectType");
        
        /** The date variable you can specify a pattern following the date like $date:dd/mmmm$ as long as it follows the simple date format rules */
        public static final Variable DATE = new Variable("date"){
        public String format(Object value, String pattern) {
            if (pattern == null || pattern.trim().length()==0) {
                return new SimpleDateFormat().format(value);
            }
            return new SimpleDateFormat(pattern).format(value);
        }
            
        };
        
        /** The class name variable */
        public static final Variable CLASS_NAME = new Variable("className");
        String name;
        int hashCode;

        private Variable(String name)
        {
            this.name = name.intern();
            this.hashCode = this.name.hashCode();
        }

        /**
         * Returns the name of the variable.
         *
         * @return variable name.
         */
        public String getName()
        {
            return this.name;
        }


        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (o instanceof Environment.Variable)
            {
                return this.name == ((Environment.Variable) o).name;
            }

            return false;
        }
        
        public String format(Object value, String pattern) {
            return value.toString();
        }


        public int hashCode()
        {
            return this.hashCode;
        }


        /**
         * Returns a string representation of this object.
         *
         * @return A string representation of this object.
         *
         * @see #getName
         */
        public String toString()
        {
            return this.name;
        }
    }

}
