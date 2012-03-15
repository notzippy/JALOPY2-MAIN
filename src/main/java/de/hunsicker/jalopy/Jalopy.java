/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import de.hunsicker.io.Copy;
import de.hunsicker.io.FileBackup;
import de.hunsicker.io.FileFormat;
import de.hunsicker.io.IoHelper;
import de.hunsicker.jalopy.language.CodeInspector;
import de.hunsicker.jalopy.language.CompositeFactory;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaNodeFactory;
import de.hunsicker.jalopy.language.JavaRecognizer;
import de.hunsicker.jalopy.language.antlr.Node;
import de.hunsicker.jalopy.language.NodeFactory;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.printer.NodeWriter;
import de.hunsicker.jalopy.printer.PrinterFactory;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.Environment;
import de.hunsicker.jalopy.storage.History;
import de.hunsicker.jalopy.storage.Loggers;
import de.hunsicker.util.Version;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;


/**
 * <p>
 * The bean-like interface to Jalopy.
 * </p>
 *
 * <p>
 * <strong>Sample Usage</strong>
 * </p>
 *
 * <p>
 * <pre class="snippet">
 * // create a new Jalopy instance with the currently active code convention settings
 * Jalopy jalopy = new Jalopy();
 *
 * File file = ...;
 *
 * // specify input and output target
 * jalopy.setInput(file);
 * jalopy.setOutput(file);
 *
 * // format and overwrite the given input file
 * jalopy.format();
 *
 * if (jalopy.getState() == Jalopy.State.OK)
 *     System.out.println(file + " successfully formatted");
 * else if (jalopy.getState() == Jalopy.State.WARN)
 *     System.out.println(file + " formatted with warnings");
 * else if (jalopy.getState() == Jalopy.State.ERROR)
 *     System.out.println(file + " could not be formatted");
 *
 * // setup a destination directory
 * File destination = ...;
 *
 * jalopy.setDestination(destination);
 * jalopy.setInput(file);
 * jalopy.setOutput(file);
 *
 * // format the given input file and write the output to the given destination,
 * // the package structure will be retained automatically
 * jalopy.format();
 *
 * ...
 * </pre>
 * </p>
 *
 * <p>
 * <strong>Thread safety</strong>
 * </p>
 *
 * <p>
 * This class is <em>thread-hostile</em>, it is not safe for concurrent use by multiple
 * threads even if all method invocations are surrounded by external synchronisation.
 * You should rather create one instance of this class per thread.
 * </p>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.15 $
 */
public final class Jalopy
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The empty byte array. */
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /** Indicates a file input. */
    private static final int FILE_INPUT = 1;

    /** Indicates a file output. */
    private static final int FILE_OUTPUT = 2;

    /** Indicates the illegal mode where no processing can take place. */
    private static final int ILLEGAL = 0;

    /** Indicates a reader input. */
    private static final int READER_INPUT = 16;

    /** Indicates a string input. */
    private static final int STRING_INPUT = 4;

    /** Indicates a string output. */
    private static final int STRING_OUTPUT = 8;

    /** Indicates a writer output. */
    private static final int WRITER_OUTPUT = 32;

    /** The version information. */
    private static Version _version;

    /** Indicates file to file mode. */
    private static final int FILE_FILE = FILE_INPUT | FILE_OUTPUT;

    /** Indicates file to string mode. */
    private static final int FILE_STRING = FILE_INPUT | STRING_OUTPUT;

    /** Indicates file to writer mode. */
    private static final int FILE_WRITER = FILE_INPUT | WRITER_OUTPUT;

    /** Indicates string to file mode. */
    private static final int STRING_FILE = STRING_INPUT | FILE_OUTPUT;

    /** Indicates string to string mode. */
    private static final int STRING_STRING = STRING_INPUT | STRING_OUTPUT;

    /** Indicates string to writer mode. */
    private static final int STRING_WRITER = STRING_INPUT | WRITER_OUTPUT;

    /** Indicates reader to file mode. */
    private static final int READER_FILE = READER_INPUT | FILE_OUTPUT;

    /** Indicates reader to string mode. */
    private static final int READER_STRING = READER_INPUT | STRING_OUTPUT;

    /** Indicates reader to writer mode. */
    private static final int READER_WRITER = READER_INPUT | WRITER_OUTPUT;

    static
    {
        _version = Version.valueOf(loadVersionString());
    }

    //~ Instance variables ---------------------------------------------------------------

    private Checksum _inputFileChecksum;

    /** The code inspector. */
    private CodeInspector _inspector;

    /** Default backup directory. */
    private File _backupDir;

    /** The created backup file. */
    private File _backupFile;

    /** Destination directory to copy all formatted files into. */
    private File _destination;

    /** Input source file. */
    private File _inputFile;

    /** Output target file. */
    private File _outputFile;

    /** File format of the input stream. */
    private FileFormat _inputFileFormat;

    /** File format of the output stream. */
    private FileFormat _outputFileFormat;

    /** The last generated Java AST. */
    private JavaNode _tree;

    /** Our recognizer. */
    private final JavaRecognizer _recognizer;

    /**
     * Holds the issues found during inspection. Maps one node to either exactly one
     * issue or a list of issues.
     */
    private Map _issues; // Map of <JavaNode>:<Object>

    /** What history method should be used if file policy is enabled? */
    private History.Method _historyMethod = History.Method.TIMESTAMP;

    /** What history policy should be used? */
    private History.Policy _historyPolicy = History.Policy.DISABLED;

    /** Input source reader. */
    private Reader _inputReader;

    /** Appender which <em>spies</em> for logging events. */
    private final SpyAppender _spy;

    /** Run status. */
    State _state = State.UNDEFINED;

    /**
     * The encoding to use for formatting. If <code>null</code> the platform's default
     * encoding will be used.
     */
    private String _encoding;

    /** The contents of the input source if specified as a STRING_INPUT. */
    private String _inputString;

    /** The package name of the current input source. */
    private String _packageName;

    /** Output target string. */
    private StringBuffer _outputString;

    /** Holds the result stream if we write to STRING_OUTPUT. */
    private StringWriter _outputStringBuffer;

    /** Output writer. */
    private Writer _outputWriter;

    /** Helper array to hold parameters used to format localized messages. */
    private Object[] _args = new Object[5];

    /** Should formatting be forced for files that are up to date? */
    private boolean _force;

    /** Don't delete backup files. */
    private boolean _holdBackup;

    /** Is the code inspector enabled. */
    private boolean _inspect;

    /** Number of backups to hold. */
    private int _backupLevel;

    /** I/O mode. */
    private int _mode;

    /** Used to update the modification date of output files. */
    private long _now;

    /** Holds the number of milliseconds used for parsing. */
    private long _timeParsing;

    /** Holds the number of milliseconds used for printing. */
    private long _timePrinting;

    /** Holds the number of milliseconds used for transforming. */
    private long _timeTransforming;
    
    private CompositeFactory _factory=null;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Jalopy object.
     */
    public Jalopy()
    {
        initConventionDefaults();
        _issues = new HashMap(30);
        _factory = new CompositeFactory();
        
        _recognizer = new JavaRecognizer(_factory);
        _inspector = new CodeInspector(_issues);
        _spy = new SpyAppender();
        Loggers.ALL.addAppender(_spy);
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets the code convention to be loaded from the given file (either a qualified file
     * path or single file name).
     *
     * @param file the code convention file.
     *
     * @throws IOException if no code convention could be loaded from the given file.
     */
    public static void setConvention(File file)
      throws IOException
    {
        Convention.importSettings(file);
    }


    /**
     * Sets the code convention to be loaded from the given url.
     *
     * @param url url.
     *
     * @throws IOException if no code convention could be loaded from the given url.
     */
    public static void setConvention(URL url)
      throws IOException
    {
        Convention.importSettings(url);
    }


    /**
     * Sets the code convention to be loaded from the given location string.
     *
     * @param location location. Either a local file pathname or a pointer to a
     *        distributed resource accessible via the HTTP protocol (that is starting
     *        with &quot;<code>http://</code>&quot; or &quot;<code>www.</code>&quot;).
     *
     * @throws IOException if no code convention could be loaded from the given location.
     */
    public static void setConvention(String location)
      throws IOException
    {
        if (
            location.startsWith("http://" /* NOI18N */)
            || location.startsWith("www." /* NOI18N */))
        {
            setConvention(new URL(location));
        }
        else
        {
            setConvention(new File(location));
        }
    }


    /**
     * Sets the encoding that controls how Jalopy interprets text files containing
     * characters beyond the ASCII character set.
     *
     * @param encoding a valid encoding name. For a list of valid encoding names refer to
     *        <a
     *        href="http://java.sun.com/products/jdk/1.4/docs/guide/intl/encoding.doc.html">Supported
     *        Encodings</a>. Note that <code>null</code> is permitted and indicates the
     *        platform's default encoding.
     *
     * @throws IllegalArgumentException if an invalid encoding was specified.
     */
    public void setEncoding(String encoding)
    {
        if (encoding != null)
        {
            try
            {
                new String(EMPTY_BYTE_ARRAY, encoding);
            }
            catch (UnsupportedEncodingException ex)
            {
                throw new IllegalArgumentException(
                    "invalid encoding specified -- " + encoding);
            }
        }

        _encoding = encoding;
    }


    /**
     * Sets the file format of the output stream. The file format controls what end of
     * line character is used for the output files.
     *
     * @param format file format to use.
     */
    public void setFileFormat(FileFormat format)
    {
        _outputFileFormat = format;
    }


    /**
     * Sets the file format of the output stream. The file format controls what end of
     * line character is used for the output files.
     *
     * @param format string representation of the file format to use.
     *
     * @see de.hunsicker.io.FileFormat#valueOf
     */
    public void setFileFormat(String format)
    {
        _outputFileFormat = FileFormat.valueOf(format);
    }


    /**
     * Specifies whether all files should be formatted no matter what the state of a file
     * is.
     *
     * <p>
     * Defaults to <code>false</code>, which means that a source file will be only
     * formatted if it hasn't ever been formatted before or if it has been modified
     * since the last time it was processed.
     * </p>
     *
     * @param force if <code>true</code> all files are always formatted.
     */
    public void setForce(boolean force)
    {
        _force = force;
    }


    /**
     * Sets the history method to use.
     *
     * @param method a history method.
     *
     * @since 1.0b9
     */
    public void setHistoryMethod(History.Method method)
    {
        _historyMethod = method;
    }


    /**
     * Sets the history policy to use.
     *
     * @param policy a history policy.
     */
    public void setHistoryPolicy(History.Policy policy)
    {
        _historyPolicy = policy;
    }


    /**
     * Enables or disables the code inspector during formatting runs. You can always
     * perform inspection using <code>inspect()</code> methods.
     *
     * @param enabled if <code>true</code> the code inspector will be enabled.
     *
     * @see #inspect
     * @see #inspect(JavaNode)
     * @since 1.0b8
     */
    public void setInspect(boolean enabled)
    {
        _inspect = enabled;
    }


    /**
     * Determines wether the code inspector is enabled during formatting runs.
     *
     * @return <code>true</code> if the code inspector is enabled.
     *
     * @since 1.0b8
     */
    public boolean isInspect()
    {
        return _inspect;
    }


    /**
     * Returns the version information.
     *
     * @return the current version information.
     */
    public static Version getVersion()
    {
        return _version;
    }


    /**
     * Sets the output target to use.
     *
     * @param output writer to use as output target.
     */
    public void setOutput(Writer output)
    {
        _outputWriter = output;
        _mode += WRITER_OUTPUT;
    }


    /**
     * Returns a string with the elapsed times for the different profiling categories.
     * Purely a diagnostic method only useful during developing.
     *
     * @return string with rudimentary profiling information.
     */
    public String getProfileTimes()
    {
        long whole = _timeParsing + _timeTransforming + _timePrinting;

        if (whole > 0)
        {
            StringBuffer buf = new StringBuffer(100);
            buf.append(_timeParsing);
            buf.append('(');
            buf.append((_timeParsing * 100) / whole);
            buf.append("%) ");
            buf.append(_timeTransforming);
            buf.append('(');
            buf.append((_timeTransforming * 100) / whole);
            buf.append("%) ");
            buf.append(_timePrinting);
            buf.append('(');
            buf.append((_timePrinting * 100) / whole);
            buf.append("%)");

            return buf.toString();
        }

        return "";
    }


    /**
     * Returns the used Java recognizer.
     *
     * @return the recognizer that is used for the language processing.
     *
     * @since 1.0b9
     */
    public JavaRecognizer getRecognizer()
    {
        return _recognizer;
    }


    /**
     * Returns the current state info.
     *
     * @return The current state.
     */
    public State getState()
    {
        return _state;
    }


    /**
     * Checks whether the specification version of the given Plug-in is compatible with
     * the Jalopy Plug-in API spec version.
     *
     * @param packageName the package name of a Plug-in as specified in the Jar Manifest,
     *        e.g. &quot;de.hunsicker.jalopy.plugin.ant&quot;.
     *
     * @throws VersionMismatchException if the Plug-in with the given package name is not
     *         compatible with the Plug-in API version of the Jalopy runtime.
     *
     * @since 1.0b8
     */
    public static void checkCompatibility(String packageName)
      throws VersionMismatchException
    {
        /*// make sure the Plug-in package is known to the class loader
        // (necessary for those Plug-ins that don't make use of the
        // Plug-in API like the Ant and Console Plug-in)
        if (Package.getPackage("de.hunsicker.jalopy.plugin") == null)
        {
            try
            {
                // load a class from the Plug-in package so it becomes known
                // to the classloader
                Helper.loadClass("de.hunsicker.jalopy.plugin.StatusBar",
                                 Jalopy.class);
            }
            catch (Throwable ex)
            {
                return;
            }
        }

        Package runtimePackage = Package.getPackage(
                                         "de.hunsicker.jalopy.plugin");

        if (runtimePackage == null) // not loaded from jar
        {
            return;
        }

        Package pluginPackage = Package.getPackage(packageName);

        if (pluginPackage == null) // not loaded from jar
        {
            return;
        }

        if (!pluginPackage.isCompatibleWith(
                     runtimePackage.getSpecificationVersion()))
        {
            throw new VersionMismatchException(runtimePackage.getSpecificationVersion(),
                                               pluginPackage.getSpecificationVersion());
        }*/
    }


    /**
     * Sets whether to hold a backup copy of an input file. Defaults to
     * <code>true</code>.
     *
     * <p>
     * This switch only takes action if you specify the same file for both input and
     * output.
     * </p>
     *
     * <p>
     * Note that you can specify how many backups should be retained, in case you want a
     * history. See {@link #setBackupLevel} for further information.
     * </p>
     *
     * @param backup if <code>true</code> the backup of an input file will not be deleted
     *        after the run.
     *
     * @see #setBackupLevel
     * @see #setInput(File)
     * @see #setOutput(File)
     */
    public void setBackup(boolean backup)
    {
        _holdBackup = backup;
    }


    /**
     * Sets the directory where backup files will be stored.
     *
     * @param directory path to an existing directory.
     *
     * @throws IllegalArgumentException if the given file does not denote a valid
     *         directory.
     *
     * @see #setBackup
     */
    public void setBackupDirectory(File directory)
    {
        if (!directory.isAbsolute())
        {
            directory =
                new File(Convention.getProjectSettingsDirectory(), directory.getPath());
        }

        IoHelper.ensureDirectoryExists(directory.getAbsoluteFile());

        if (!directory.exists() || !directory.isDirectory())
        {
            throw new IllegalArgumentException("invalid directory -- " + directory);
        }

        _backupDir = directory.getAbsoluteFile();
    }


    /**
     * Sets the directory where backup files will be stored. Invokes {@link
     * #setBackupDirectory(File)} with <code>newFile(directory)</code>.
     *
     * @param directory path to an existing directory.
     *
     * @see #setBackup
     * @see #setBackupDirectory
     */
    public void setBackupDirectory(String directory)
    {
        setBackupDirectory(new File(directory));
    }


    /**
     * Returns the directory where file backups will be stored.
     *
     * @return the backup directory.
     *
     * @see #setBackup
     */
    public File getBackupDirectory()
    {
        return _backupDir;
    }


    /**
     * Sets the number of backups to hold. A value of <code>0</code> means to hold no
     * backup at all (same as {@link #setBackup setBackup(false)}). The default is
     * <code>1</code>.
     *
     * @param level number of backups to hold.
     *
     * @throws IllegalArgumentException if <code><em>level</em> &lt; 0</code>
     *
     * @see #setBackup
     */
    public void setBackupLevel(int level)
    {
        if (level < 0)
        {
            throw new IllegalArgumentException("level has to be >= 0");
        }

        _backupLevel = level;

        if (level == 0)
        {
            setBackup(false);
        }
    }


    /**
     * Sets the destination directory to create all formatting output into. This setting
     * then lasts until you either specify another directory or {@link #reset} was
     * called (which results in deleting the destination, files are overwritten now on).
     *
     * <p>
     * If the given destination does not exist, it will be created.
     * </p>
     *
     * <p>
     * Only applies if a file output target was specified.
     * </p>
     *
     * @param destination destination directory.
     *
     * @throws IllegalArgumentException if <em>destination</em> is <code>null</code> or
     *         does not denote a directory.
     * @throws RuntimeException if the destination directory could not be created.
     *
     * @see #setOutput(File)
     */
    public void setDestination(File destination)
    {
        if ((destination == null) || (destination.exists() && !destination.isDirectory()))
        {
            throw new IllegalArgumentException("no valid directory -- " + destination);
        }

        if (!destination.exists())
        {
            if (!destination.mkdirs())
            {
                throw new RuntimeException(
                    "could not create destination directory -- " + destination);
            }

            Object[] args = { destination };
            Loggers.IO.l7dlog(
                Level.INFO, "FILE_DESTINATION_CREATED" /* NOI18N */, args, null);
        }

        _destination = destination;
    }


    /**
     * Sets the input source to use.
     *
     * @param input string to use as input source.
     * @param path path of the file that is to be processed.
     *
     * @throws NullPointerException if <code><em>input</em> == null</code> or
     *         <code><em>path</em> == null</code>
     */
    public void setInput(
        String input,
        String path)
    {
        if (input == null)
        {
            throw new NullPointerException();
        }

        if (path == null)
        {
            throw new NullPointerException();
        }

        _inputFile = new File(path);
        _inputFileChecksum = null;
        _inputString = input;
        _inputReader = new BufferedReader(new StringReader(input));

        if (!hasInput())
        {
            _mode += STRING_INPUT;
        }
    }


    /**
     * Sets the input source to use.
     *
     * @param input stream to use as input source.
     * @param path path of file that is to be processed.
     *
     * @throws IllegalArgumentException if <code><em>path</em> == null</code> or if
     *         <em>path</em> does not denote a valid, i.e. existing file or the system
     *         input stream.
     */
    public void setInput(
        InputStream input,
        String      path)
    {
        // ignore empty input
        if (input == null)
        {
            /**
             * @todo Loggers.IO.l7dlog(Level.INFO, "", _args, null);
             */
            return;
        }

        if (path == null)
        {
            throw new IllegalArgumentException("no path given");
        }

        File file = new File(path);

        if ((!file.exists() || !file.isFile()) && (System.in != input))
        {
            throw new IllegalArgumentException("invalid path given -- " + path);
        }

        _inputFile = new File(path);
        _inputFileChecksum = null;
        _inputReader = new BufferedReader(new InputStreamReader(input));

        if (!hasInput())
        {
            _mode += READER_INPUT;
        }
    }


    /**
     * Sets the input source to use.
     *
     * @param input reader to use as input source.
     * @param path path of file that is to be processed.
     *
     * @throws IllegalArgumentException if <code><em>path</em> == null</code> or if
     *         <em>path</em> does not denote a valid, i.e. existing file.
     */
    public void setInput(
        Reader input,
        String path)
    {
        if (path == null)
        {
            throw new IllegalArgumentException("no path given");
        }

        // ignore empty input
        if (input == null)
        {
            /**
             * @todo Loggers.IO.l7dlog(Level.INFO, "", _args, null);
             */
            return;
        }

        File file = new File(path);

        if (!file.exists() || !file.isFile())
        {
            throw new IllegalArgumentException("invalid path given -- " + path);
        }

        _inputFile = new File(path);
        _inputFileChecksum = null;
        _inputReader = input;

        if (!hasInput())
        {
            _mode += READER_INPUT;
        }
    }


    /**
     * Sets the input source to use.
     *
     * @param input file to use as input source.
     *
     * @throws FileNotFoundException if the specified source file does not exist.
     *
     * @see #setInput(Reader, String)
     * @see #setInput(String, String)
     */
    public void setInput(File input)
      throws FileNotFoundException
    {
        _inputReader = getBufferedReader(input, _encoding);
        _inputFile = input.getAbsoluteFile();
        _inputFileChecksum = null;

        if (!hasInput())
        {
            _mode += FILE_INPUT;
        }
    }


    /**
     * Sets the output target to use.
     *
     * @param output file to use as output target.
     */
    public void setOutput(File output)
    {
        // we don't create our output writer here, because it is possible that
        // both input and output file are equal which means we have to make
        // a backup copy prior to the processing, but we create the copy
        // only after parsing was successful to avoid unnecessary I/O activity.
        // So we delegate the creation of the stream to the {@link #parse}
        // method
        _outputFile = output;
        _mode += FILE_OUTPUT;
    }


    /**
     * Sets the output target to use.
     *
     * @param output buffer to use as output target.
     */
    public void setOutput(StringBuffer output)
    {
        _outputString = output;
        _outputStringBuffer = new StringWriter();
        _outputWriter = new BufferedWriter(_outputStringBuffer);
        _mode += STRING_OUTPUT;
    }


    /**
     * Cleans up the backup directory. All empty directories will be deleted. Only takes
     * affect if no backup copies should be kept.
     *
     * @see #setBackup
     * @since 1.0b9
     */
    public void cleanupBackupDirectory()
    {
        if (!_holdBackup)
        {
            cleanupDirectory(_backupDir);
        }
    }


    /**
     * Formats the (via {@link #setInput(File)}) specified input source and writes the
     * formatted result to the specified target.
     *
     * <p>
     * Formatting a file means that {@link #parse parsing}, {@link #inspect inspecting}
     * and printing will be performed in sequence depending on the current state. Thus
     * the parsing and/or inspection phase may be skipped.
     * </p>
     *
     * <p>
     * It is safe to call this method multiple times after you've first constructed an
     * instance: just set new input/output targets and go with it. But remember that
     * this class is thread-hostile: accessing the class concurrently from multiple
     * threads will lead to unsuspected results.
     * </p>
     *
     * @return <code>true</code> if any formatting was applied.
     *
     * @throws IllegalStateException if no input source has been specified.
     *
     * @see #setInput(File)
     * @see #setOutput(File)
     * @see #parse
     * @see #inspect
     */
    public boolean format()
    {
        return format(true);
    }    
    public boolean format(boolean runCleanup)
    {
        JavaNode tree = null;
        boolean formatSuccess = false;

        if (!hasInput())
        {
            throw new IllegalStateException("no input source specified");
        }

        try
        {
            if (!isDirty()) // input source up-to-date, no formatting necessary
            {
                _args[0] = _inputFile;
                Loggers.IO.l7dlog(
                    Level.INFO, "FILE_FOUND_HISTORY" /* NOI18N */, _args, null);
                _state = State.OK;
                cleanup();

                return false;
            }

            if ((_state != State.PARSED) || (_state != State.INSPECTED))
            {
                tree = parse();

                if (_state == State.ERROR)
                {
                    cleanup();

                    return false;
                }
                    
                    /**
                     * @todo we need to reset the line info for the recognizer!!!
                     */
            }
            else
            {
                tree = _tree;
            }
            formatSuccess = format(tree, _packageName, _inputFileFormat, false);
        }
        catch (Throwable ex)
        {
            _state = State.ERROR;
            ex.printStackTrace();
            _args[0] = _inputFile;
            _args[1] =
                (ex.getMessage() == null) ? ex.getClass().getName()
                                          : ex.getMessage();
            Loggers.IO.l7dlog(Level.ERROR, "UNKNOWN_ERROR" /* NOI18N */, _args, ex);
        }
        finally {
            //todo add togglable 
            //_factory.clear();
            cleanup();
        }
        

        return formatSuccess;
    }

    /**
     * Inspects the (via {@link #setInput(File)}) specified input source for code
     * convention violations and coding weaknesses. If no parsing was performed yet, the
     * input source will be first parsed.
     *
     * @see #setInput(File)
     * @see #parse
     * @since 1.0b8
     */
    public void inspect()
    {
        JavaNode tree = null;

        if (_state != State.PARSED)
        {
            try
            {
                tree = parse();

                if (_state == State.ERROR)
                {
                    return;
                }
            }
            catch (Throwable ex)
            {
                _state = State.ERROR;
                _args[0] = _inputFile;
                _args[1] =
                    (ex.getMessage() == null) ? ex.getClass().getName()
                                              : ex.getMessage();
                Loggers.IO.l7dlog(Level.ERROR, "UNKNOWN_ERROR" /* NOI18N */, _args, ex);
            }
        }
        else
        {
            tree = _tree;
        }

        inspect(tree);
    }


    /**
     * Inspects the given Java AST for code convention violations and coding weaknesses.
     *
     * @param tree root node of the Java AST that is to be inspected.
     *
     * @throws NullPointerException if <code><em>tree</em> == null</code>
     * @throws IllegalArgumentException if <em>tree</em> is not the root node of a Java
     *         AST.
     *
     * @see #parse
     * @since 1.0b8
     */
    public void inspect(JavaNode tree)
    {
        if (tree == null)
        {
            throw new NullPointerException();
        }

        switch (tree.getType())
        {
            case JavaTokenTypes.ROOT :
                break;

            default :
                throw new IllegalArgumentException("not a root node -- " + tree);
        }

        long start = 0;

        if (Loggers.IO.isDebugEnabled())
        {
            start = System.currentTimeMillis();
            _args[0] = _inputFile;
            Loggers.IO.l7dlog(Level.DEBUG, "FILE_INSPECT" /* NOI18N */, _args, null);
        }

        _inspector.inspect(tree, (_outputFile != null) ? _outputFile
                                                       : _inputFile);

        if (Loggers.IO.isDebugEnabled())
        {
            long stop = System.currentTimeMillis();
            Loggers.IO.debug(_inputFile + ":0:0:inspecting took " + (stop - start));
        }

        if (_state != State.ERROR)
        {
            _state = State.INSPECTED;
        }
    }


    /**
     * Parses the (via {@link #setInput(File)}) specified input source. You should always
     * check the state after parsing, to be sure the input source could be successfully
     * parsed.
     *
     * @return The root node of the created Java AST. May or may not return
     *         <code>null</code> if the input source could not be successfully parsed
     *         (i.e. always use {@link #getState} to check for success).
     *
     * @throws IllegalStateException if no input source has been specified.
     *
     * @see #setInput(File)
     * @see #getState
     * @since 1.0b8
     */
    public JavaNode parse()
    {
        long start = 0;
        _state = State.RUNNING;

        if (Loggers.IO.isDebugEnabled())
        {
            start = System.currentTimeMillis();
        }

        try
        {
            switch (_mode)
            {
                case FILE_INPUT :
                case FILE_FILE :
                case FILE_STRING :
                case FILE_WRITER :
                    _args[0] = _inputFile;
                    Loggers.IO.l7dlog(Level.INFO, "FILE_PARSE" /* NOI18N */, _args, null);
                    _recognizer.parse(_inputReader, _inputFile.getAbsolutePath());

                    break;

                case STRING_INPUT :
                case READER_INPUT :
                case STRING_FILE :
                case READER_FILE :
                case STRING_STRING :
                case STRING_WRITER :
                case READER_STRING :
                case READER_WRITER :
                    _args[0] = _inputFile;
                    Loggers.IO.l7dlog(Level.INFO, "FILE_PARSE" /* NOI18N */, _args, null);
                    _recognizer.parse(_inputReader, _inputFile.getAbsolutePath());

                    break;

                default :
                    throw new IllegalStateException("no input source specified");
            }

            if (_state == State.ERROR)
            {
                return null;
            }

            if (Loggers.IO.isDebugEnabled())
            {
                long stop = System.currentTimeMillis();
                Loggers.IO.debug(
                    _inputFile.getAbsolutePath() + ":0:0:parsing took " + (stop - start));
                _timeParsing += (stop - start);
            }

            if (_state != State.ERROR)
            {
                _state = State.PARSED;
            }

            JavaNode tree = null;

            if (Loggers.IO.isDebugEnabled())
            {
                Loggers.IO.debug(
                    ((_outputFile != null) ? _outputFile
                                           : _inputFile) + ":0:0:transform");
                start = System.currentTimeMillis();
                tree = (JavaNode) _recognizer.getParseTree();

                long stop = System.currentTimeMillis();
                _timeTransforming += (stop - start);
                Loggers.IO.debug(
                    ((_outputFile != null) ? _outputFile
                                           : _inputFile) + ":0:0:transforming took "
                    + (stop - start));
            }
            else
            {
                tree = (JavaNode) _recognizer.getParseTree();
            }

            _tree = tree;
            _inputFileFormat = _recognizer.getFileFormat();
            _packageName = _recognizer.getPackageName();

            return tree;
        }
        
        finally
        {
            cleanupRecognizer();
        }
    }


    /**
     * Resets this instance.
     *
     * <p>
     * Note that this method is not meant to be invoked after every call of {@link
     * #format}, but rather serves as a way to reset this instance to exactly the state
     * directly after the object creation.
     * </p>
     */
    public void reset()
    {
        cleanup();
        initConventionDefaults();
    }


    /**
     * Resets the profiling timers.
     */
    void resetTimers()
    {
        _timeParsing = 0;
        _timePrinting = 0;
        _timeTransforming = 0;
    }


    /**
     * Cleans up the given directory. All empty subdirectories will be removed.
     *
     * @param directory directory to cleanup.
     *
     * @since 1.0b9
     */
    private void cleanupDirectory(File directory)
    {
        File[] files = directory.listFiles();

        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory())
                {
                    if (files[i].list().length > 0)
                    {
                        cleanupDirectory(files[i]);
                    }
                    else
                    {
                        if (files[i].delete())
                        {
                            File parent = directory.getParentFile();

                            if (!directory.equals(_backupDir))
                            {
                                cleanupDirectory(parent);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Formats the given Java AST and writes the result to the specified target.
     *
     * @param tree root node of the JavaAST that is to be formatted.
     * @param packageName the package name of the tree.
     * @param format the detected file format of the Java source file represented by the
     *        tree.
     * @param check if <code>true</code> the method checks if the source file of the
     *        given tree actually needs reformatting and omitts further processing if
     *        so.
     *
     * @return <code>true</code> if any formatting was applied.
     *
     * @since 1.0b8
     */
    private boolean format(
        JavaNode   tree,
        String     packageName,
        FileFormat format,
        boolean    check)
    {
        String defaultEncoding = null;

        try
        {
            _args[0] = _inputFile;

            if (check && !isDirty()) // input source up-to-date, no formatting necessary
            {
                Loggers.IO.l7dlog(
                    Level.INFO, "FILE_FOUND_HISTORY" /* NOI18N */, _args, null);
                _state = State.OK;

                return false;
            }

            if (_encoding != null)
            {
                // store the current default encoding
                defaultEncoding = System.getProperty("file.encoding" /* NOI18N */);

                // now set the encoding to use by Jalopy
                System.setProperty("file.encoding" /* NOI18N */, _encoding);
            }
            if (_inspect)
            {
                inspect(tree);
            }

            // it seems ok to print the AST
            print(tree, packageName, format);

            if (_inspect)
            {
                Object issues[] = new Object[6];
                for(Iterator i = _issues.entrySet().iterator();i.hasNext();) {
                    Entry entry = (Entry) i.next();
                    JavaNode node = (JavaNode) entry.getKey();
                    while (node!=null && node.getParent()!=null && node.newLine==0) {
                        node = node.getParent();
                    }
                    Object message = entry.getValue();
            issues[0] = _inputFile.getAbsolutePath();
            issues[1] = new Integer(node.newLine);
            issues[2] = new Integer(node.newColumn);
            issues[3] = message.toString();
            issues[4] = new Integer(node.getStartLine());
            issues[5] = node;
            Loggers.PRINTER.l7dlog(
                Level.WARN, "CODE_INSPECTOR", issues, null);
                }
                
                
            }

            if (_state == State.ERROR)
            {
                // don't forget to restore the original file, if needed
                restore(_inputFile, _backupFile);

                return false;
            }

            if (_outputStringBuffer != null)
            {
                _outputString.setLength(0);
                _outputString.append(_outputStringBuffer.toString());
            }

            if (_outputFile != null)
            {
                // we have to release the file locks prior to changing the
                // timestamp
                _inputReader.close();

                if (_outputWriter != null)
                {
                    _outputWriter.close();

                    // update the timestamp of the file with our 'magic' stamp
                    // (but only if theres a writer)
                    _outputFile.setLastModified(_now);
                }

                // update the status information if necessary
                if (
                    (_state == State.PARSED) || (_state == State.INSPECTED)
                    || (_state == State.RUNNING))
                {
                    // no error or warnings occured, all ok
                    _state = State.OK;
                }
            }

            // delete the backup if the user don't want backup copies
            if (!_holdBackup && (_backupFile != null) && _backupFile.exists())
            {
                _backupFile.delete();

                if (Loggers.IO.isDebugEnabled())
                {
                    _args[0] = _inputFile;
                    _args[1] = _backupFile;
                    Loggers.IO.l7dlog(
                        Level.DEBUG, "FILE_BACKUP_REMOVE" /* NOI18N */, _args, null);
                }
            }
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            _state = State.ERROR;
            _args[0] = _inputFile;
            _args[1] =
                (ex.getMessage() == null) ? ex.getClass().getName()
                                          : ex.getMessage();
            Loggers.IO.l7dlog(Level.ERROR, "UNKNOWN_ERROR" /* NOI18N */, _args, ex);
            restore(_inputFile, _backupFile);
        }
        finally
        {
            if (defaultEncoding != null)
            {
                // restore the default encoding
                System.setProperty("file.encoding" /* NOI18N */, defaultEncoding);
            }

            cleanup();
        }

        return _state != State.ERROR;
    }


    /**
     * Determines whether an input source was already set.
     *
     * @return <code>true</code> if an input source was already set.
     *
     * @since 1.0b8
     */
    private boolean hasInput()
    {
        switch (_mode)
        {
            case STRING_INPUT :
            case FILE_INPUT :
            case READER_INPUT :
            case FILE_FILE :
            case FILE_STRING :
            case FILE_WRITER :
            case STRING_FILE :
            case STRING_STRING :
            case STRING_WRITER :
            case READER_FILE :
            case READER_STRING :
            case READER_WRITER :
                return true;
        }

        return false;
    }


    /**
     * Determines whether an output source was already set.
     *
     * @return <code>true</code> if an output source was already set.
     *
     * @since 1.0b8
     */
    /*
    // TODO Appears unused
    private boolean hasOutput()
    {
        switch (_mode)
        {
            case STRING_OUTPUT :
            case FILE_OUTPUT :
            case WRITER_OUTPUT :
            case FILE_FILE :
            case FILE_STRING :
            case FILE_WRITER :
            case STRING_FILE :
            case STRING_STRING :
            case STRING_WRITER :
            case READER_FILE :
            case READER_STRING :
            case READER_WRITER :
                return true;
        }

        return false;
    }

*/
    /**
     * Extracts the version information out of the package manifest.
     *
     * @return The found version string.
     *
     * @since 1.0b8
     */
    private static String loadVersionString()
    {
        return "1.0b10";

        /*Package pkg = Package.getPackage("de.hunsicker.jalopy");


        if (pkg == null)
        {
            throw new RuntimeException(
                    "could not find package de.hunsicker.jalopy");
        }

        String version = pkg.getImplementationVersion();

        if (version == null)
        {
            throw new RuntimeException(
                    "no implementation version string found in package manifest");
        }

        return version;*/
    }


    /**
     * Determines whether we should checksum compare files before we print them in order
     * to determine whether two files are equal.
     *
     * @return <code>true</code> if the checksum comparison should be applied.
     *
     * @since 1.0b9
     */
    private boolean isChecksum()
    {
        boolean result =
            (_outputFile != null) && (_historyPolicy == History.Policy.FILE)
            && ((_historyMethod == History.Method.CRC32)
            || (_historyMethod == History.Method.ADLER32));

        return result;
    }


    /**
     * Returns the destination file for the given target. If the directory where the file
     * should reside does not exist it will be created.
     *
     * @param destination destination directory.
     * @param packageName package name of the file.
     * @param filename filename of the file.
     *
     * @return the destination file.
     *
     * @throws IOException if the target directory could not be created.
     */
    private File getDestinationFile(
        File   destination,
        String packageName,
        String filename)
      throws IOException
    {
        StringBuffer buf = new StringBuffer(90);
        buf.append(destination);
        buf.append(File.separator);
        buf.append(packageName.replace('.', File.separatorChar));

        File test = new File(buf.toString());

        if (!test.exists())
        {
            if (!test.mkdirs())
            {
                throw new IOException("could not create target directory -- " + buf);
            }
                if (Loggers.IO.isDebugEnabled())
                {
                    Loggers.IO.debug("directory " + test + " created");
                }
        }

        buf.append(File.separator);
        buf.append(filename);

        return new File(buf.toString());
    }


    /**
     * Indicates whether the input file is <em>dirty</em>. <em>Dirty</em> means that the
     * file needs to be formatted.
     *
     * <p>
     * Use {@link #setForce setForce(true)} to always force a formatting of the file.
     * </p>
     *
     * @return <code>true</code> if the input file is dirty. If the user specified an
     *         input stream or input string, this method always returns
     *         <code>true</code> as we cannot determine the last modification of the
     *         input source in such cases.
     *
     * @throws IOException if an I/O error occured.
     */
    private boolean isDirty()
      throws IOException
    {
        if (_force) // the user forces formatting
        {
            return true;
        }

        // no input file means we're processing a non-file input. This is
        // generally the case if we're format an editor view in a graphical
        // application. As the user may revert the formatting, we assume
        // processing is always necessary
        if ((_mode & FILE_INPUT) == 0)
        {
            // but only if the input is not empty
            return _inputReader != null;
        }

        // it doesn't make much sense to format a non-existing or empty file
        if (((_inputFile != null) && !_inputFile.exists()) || (_inputFile.length() == 0))
        {
            return false;
        }

        if (_historyPolicy == History.Policy.FILE)
        {
            History.Entry entry = History.getInstance().get(_inputFile);

            if (entry != null)
            {
                if (_historyMethod == History.Method.TIMESTAMP)
                {
                    return entry.getModification() < _inputFile.lastModified();
                }
                else if (_inputFileChecksum == null)
                {
                    if (_historyMethod == History.Method.CRC32)
                    {
                        _inputFileChecksum = new CRC32();
                    }
                    else if (_historyMethod == History.Method.ADLER32)
                    {
                        _inputFileChecksum = new Adler32();
                    }

                    InputStream in = null;

                    try
                    {
                        in = new BufferedInputStream(new FileInputStream(_inputFile));

                        byte[] buffer = new byte[8 * 1024];
                        int count = 0;

                        do
                        {
                            _inputFileChecksum.update(buffer, 0, count);
                            count = in.read(buffer, 0, buffer.length);
                        }
                        while (count != -1);
                    }
                    finally
                    {
                        if (in != null)
                        {
                            in.close();
                        }
                    }

                    return _inputFileChecksum.getValue() != entry.getModification();
                }
                else
                {
                    return _inputFileChecksum.getValue() != entry.getModification();
                }
            }

            return true;
        }
        else if (_historyPolicy == History.Policy.COMMENT)
        {
            BufferedReader in = null;

            try
            {
                in = getBufferedReader(_inputFile, _encoding);

                String line = in.readLine().trim();
                in.close();

                // we only check the very first line
                if (
                    line.startsWith("// %") && line.endsWith("%")
                    && (line.indexOf("modified") == -1))
                {
                    int start = line.indexOf('%') + 1;
                    int stop = line.indexOf(':');
                    long lastmod = Long.parseLong(line.substring(start, stop));

                    // the input file is up-to-date
                    if (lastmod >= _inputFile.lastModified())
                    {
                        if (_destination != null)
                        {
                            String packageName =
                                line.substring(stop + 1, line.length() - 1);
                            copyInputToOutput(
                                _inputFile, _destination, packageName, lastmod);
                        }

                        return false;
                    }
                }

                return true;
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }
            }
        }
        else
        {
            return true;
        }
    }


    /**
     * Returns the line separator for the given file format.
     *
     * @param fileFormat the user specified file format.
     * @param detectedFileFormat the detected file format.
     *
     * @return line separator. Either one of &quot;\n&quot;, &quot;\r\n&quot; or
     *         &quot;\r&quot;.
     */
    private String getLineSeparator(
        FileFormat fileFormat,
        FileFormat detectedFileFormat)
    {
        if (fileFormat == FileFormat.AUTO)
        {
            return detectedFileFormat.toString();
        }

        return fileFormat.getLineSeparator();
    }


    /**
     * Sets the local macro variables.
     *
     * @param environment the current environment.
     * @param file file that is to be printed.
     * @param packageName package name of the file.
     * @param fileFormat fileFormat to use.
     * @param indentSize number of spaces to use for indentation.
     *
     * @since 1.0b8
     */
    private void setLocalVariables(
        Environment environment,
        File        file,
        String      packageName,
        String      fileFormat,
        int         indentSize)
    {
        environment.set(Environment.Variable.FILE_NAME.getName(), file.getName());
        environment.set(Environment.Variable.FILE.getName(), file.getAbsolutePath());
        environment.set(
            Environment.Variable.PACKAGE.getName(),
            "".equals(packageName) ? "default package"
                                   : packageName);
        environment.set(Environment.Variable.FILE_FORMAT.getName(), fileFormat);
        environment.set(
            Environment.Variable.TAB_SIZE.getName(), String.valueOf(indentSize));
        DateFormat df = DateFormat.getDateTimeInstance();
        environment.set(Environment.Variable.DATE.getName(),new Date()); 
        String className = file.getName();
        className = className.substring(0, className.length() - 5);
        environment.set(de.hunsicker.jalopy.storage.Environment.Variable.CLASS_NAME.getName(), className);
        
    }


    /**
     * Determines whether the given file can be modified.
     *
     * @param file file to test.
     *
     * @return <code>true</code> if the given file exists and can modified or if the file
     *         does <strong>not</strong> exist.
     */
    private boolean isWritable(File file)
    {
        if (file != null)
        {
            // we cannot determine whether the file can be modified, so we
            // have to assume true
            if (!file.exists())
            {
                return true;
            }
                return file.canWrite();
        }

        return false;
    }


    /**
     * Outputs the comment history header to the given stream (if set via {@link
     * #setInput(File)}).
     *
     * @param packageName the package name of the Java source file.
     * @param out stream to write to.
     *
     * @throws IOException if the input source could not be added to the history.
     */
    private void addCommentHistoryEntry(
        String     packageName,
        NodeWriter out)
      throws IOException
    {
        if ((_historyPolicy == History.Policy.COMMENT) && (_inputFile != null))
        {
            StringBuffer buf = new StringBuffer(40);
            buf.append("// %");
            buf.append(_now);
            buf.append(':');
            buf.append(packageName);
            buf.append('%');
            out.print(buf.toString(), JavaTokenTypes.SL_COMMENT);
            out.printNewline();
        }
    }


    /**
     * Adds the last processed input source to the file history (if set via {@link
     * #setInput(File)}).
     *
     * @param packageName the package name of the Java source file.
     * @param checksumWriter the writer we use in case we perform checksum comparison.
     *
     * @throws IOException if the input source could not be added to the history.
     */
    private void addFileHistoryEntry(
        String                          packageName,
        History.ChecksumCharArrayWriter checksumWriter)
      throws IOException
    {
        if ((_historyPolicy == History.Policy.FILE) && (_inputFile != null))
        {
            if (isChecksum())
            {
                History.getInstance().add(
                    _inputFile, packageName, checksumWriter.getChecksum().getValue());
            }
            else
            {
                History.getInstance().add(_inputFile, packageName, _now);
            }
        }
    }


    /**
     * Performs needed cleanup. Resets the recognizer and frees ressources.
     */
    private void cleanup()
    {
        try
        {
            if (_inputReader != null)
            {
                _inputReader.close();
                _inputReader = null;
            }
        }
        catch (IOException ignored)
        {
            ;
        }

        try
        {
            if (_outputWriter != null)
            {
                _outputWriter.close();
                _outputWriter = null;
            }
        }
        catch (IOException ignored)
        {
            ;
        }

        _mode = ILLEGAL;
        _issues.clear();
        _inputFile = null;
        _inputString = null;
        _outputStringBuffer = null;
        _outputString = null;
        _outputFile = null;
        _backupFile = null;
        _packageName = null;
        _inputFileFormat = null;
        _tree = null;

        cleanupRecognizer();
    }


    /**
     * Resets the Java recognizer.
     *
     * @since 1.0b8
     */
    private void cleanupRecognizer()
    {
        _recognizer.reset();
    }


    /**
     * In case the given input file is up-to-date but the user specified a certain output
     * destination target, this method copies the input file into the destination
     * directory.
     *
     * @param inputFile the input file for which formatting is not necessary.
     * @param destination the directory to copy all output into.
     * @param packageName package name of the input file.
     * @param lastmod the time the input file was last formatted.
     *
     * @throws IOException if an I/O error occured.
     */
    private void copyInputToOutput(
        File   inputFile,
        File   destination,
        String packageName,
        long   lastmod)
      throws IOException
    {
        File file = getDestinationFile(destination, packageName, inputFile.getName());

        if (!file.exists() || (file.lastModified() != lastmod))
        {
            Copy.file(inputFile, file, true);
            file.setLastModified(lastmod);
            _args[0] = inputFile;
            _args[1] = file.getAbsolutePath();
            Loggers.IO.l7dlog(Level.INFO, "FILE_COPY" /* NOI18N */, _args, null);
        }
    }


    /**
     * Creates depending on the type of the input source a backup file.
     *
     * @param packageName packageName of the file currently being parsed.
     *
     * @return the backup file if both input and target source are files and equal.
     *         Returns <code>null</code> for all other cases.
     *
     * @throws IOException if an I/O error occured.
     */
    private File createBackup(String packageName)
      throws IOException
    {
        switch (_mode)
        {
            case FILE_FILE :

                if (_inputFile.equals(_outputFile))
                {
                    IoHelper.ensureDirectoryExists(_backupDir);

                    File directory =
                        new File(
                            _backupDir + File.separator
                            + packageName.replace('.', File.separatorChar));
                    File backupFile =
                        FileBackup.create(_inputFile, directory, _backupLevel);

                    if (Loggers.IO.isDebugEnabled())
                    {
                        _args[1] = backupFile;
                        Loggers.IO.l7dlog(
                            Level.DEBUG, "FILE_COPY" /* NOI18N */, _args, null);
                    }

                    return backupFile;
                }

                break;

            case STRING_STRING :
            case STRING_FILE :
            case STRING_WRITER :

                if (_inputFile.exists())
                {
                    IoHelper.ensureDirectoryExists(_backupDir);

                    File directory =
                        new File(
                            _backupDir + File.separator
                            + packageName.replace('.', File.separatorChar));
                    String filename = _inputFile.getName();
                    File backupFile =
                        FileBackup.create(
                            _inputString, filename, directory, _backupLevel);

                    if (Loggers.IO.isDebugEnabled())
                    {
                        _args[1] = backupFile;
                        Loggers.IO.l7dlog(
                            Level.DEBUG, "FILE_COPY" /* NOI18N */, _args, null);
                    }

                    return backupFile;
                }

                break;

            case READER_STRING :
            case READER_FILE :
            case READER_WRITER :

                /**
                 * @todo implement
                 */
                break;
        }

        return null;
    }


    /**
     * Initializes the default startup values.
     */
    private void initConventionDefaults()
    {
        _backupDir = Convention.getBackupDirectory();
        _backupLevel = ConventionDefaults.BACKUP_LEVEL;
        _holdBackup = false;
        _state = State.UNDEFINED;
        _outputFileFormat = FileFormat.UNKNOWN;
        _destination = null; // all files are overwritten
        _encoding = null; // use platform default encoding
    }


    /**
     * Prints the generated AST to the given output source. The tree will only be printed
     * if no errors we're found during parsing.
     *
     * @param tree root node of the Java AST.
     * @param packageName the package name of the tree.
     * @param format the detected file format of the Java source file represented by the
     *        tree.
     *
     * @throws IOException if an I/O error occured.
     * @throws IllegalStateException if either one of input source or output target was
     *         not specified.
     */
    private void print(
        JavaNode   tree,
        String     packageName,
        FileFormat format)
      throws IOException
    {
        if (_state == State.ERROR)
        {
            return;
        }

        switch (_mode)
        {
            case FILE_FILE :

                if ((_destination == null) && !isChecksum())
                {
                    _backupFile = createBackup(packageName);
                }

                // set the output file to the destination file
                if (_destination != null)
                {
                    // create the target file
                    _outputFile =
                        getDestinationFile(
                            _destination, packageName, _outputFile.getName());
                }

                if (!isWritable(_outputFile))
                {
                    _args[0] = _outputFile.getAbsolutePath();
                    _outputFile = null;
                    Loggers.IO.l7dlog(
                        Level.WARN, "FILE_NO_WRITE" /* NOI18N */, _args, null);

                    return;
                }

                if (!isChecksum())
                {
                    _outputWriter = getBufferedWriter(_outputFile, _encoding);
                }

                break;

            case FILE_STRING :
            case FILE_WRITER :
                _backupFile = createBackup(packageName);

                break;

            case STRING_FILE :
            case READER_FILE :

                if (_destination != null)
                {
                    // specify the target file
                    _outputFile =
                        getDestinationFile(
                            _destination, packageName, _outputFile.getName());
                }

                if (!isWritable(_outputFile))
                {
                    _args[0] = _outputFile.getAbsolutePath();
                    _outputFile = null;
                    Loggers.IO.l7dlog(
                        Level.WARN, "FILE_NO_WRITE" /* NOI18N */, _args, null);

                    return;
                }

                _outputWriter = getBufferedWriter(_outputFile, _encoding);

            // fall through
            case STRING_STRING :
            case STRING_WRITER :
            case READER_STRING :
            case READER_WRITER :
                _backupFile = createBackup(packageName);

                break;

            default :
                throw new IllegalStateException(
                    "both input source and output target has to be specified");
        }
        

        _now = System.currentTimeMillis();

        Writer outputWriter = null;
        History.ChecksumCharArrayWriter checksumWriter = null;

        if (isChecksum())
        {
            checksumWriter = new History.ChecksumCharArrayWriter(_historyMethod);

            // do not write the result to disk, but to a buffer
            outputWriter = new BufferedWriter(checksumWriter);
        }
        else
        {
            outputWriter = _outputWriter;
        }

        NodeWriter out =
            new NodeWriter(
                outputWriter,_factory, _inputFile.getAbsolutePath(), _issues,
                getLineSeparator(_outputFileFormat, format), format.toString());

        out.setTracking(_recognizer.hasAnnotations() || _recognizer.hasPosition());

        Environment environment = Environment.getInstance().copy();
        setLocalVariables(
            environment, _inputFile, packageName, _outputFileFormat.getName(),
            out.getIndentSize());
        out.setEnvironment(environment);

        addCommentHistoryEntry(packageName, out);

        long start = 0;

        try
        {
            if (Loggers.IO.isDebugEnabled())
            {
                Loggers.IO.debug(
                    ((_outputFile != null) ? _outputFile
                                           : _inputFile) + ":0:0:print");

                start = System.currentTimeMillis();
                PrinterFactory.create(tree, out).print(tree, out);

                if (!isChecksum())
                {
                    long stop = System.currentTimeMillis();
                    _timePrinting += (stop - start);
                    Loggers.IO.debug(
                        ((_outputFile != null) ? _outputFile
                                               : _inputFile) + ":0:0:printing took "
                        + (stop - start));
                }
            }
            else
            {
                PrinterFactory.create(tree, out).print(tree, out);
            }

            if (isChecksum())
            {
                out.flush(); // make sure the buffer is clean

                // no checksum means there was no entry for the file
                if (
                    (_inputFileChecksum == null)
                    || (_inputFileChecksum.getValue() != checksumWriter.getChecksum()
                                                                       .getValue()))
                {
                    // only create a backup if input and output file may be equal
                    if (_destination == null)
                    {
                        _backupFile = createBackup(packageName);
                    }

                    _outputWriter = getBufferedWriter(_outputFile, _encoding);

                    checksumWriter.writeTo(_outputWriter);
                }
                else
                {
                    Loggers.IO.l7dlog(
                        Level.INFO, "FILE_MODIFIED_BUT_SAME" /* NOI18N */, _args, null);
                }

                if (Loggers.IO.isDebugEnabled())
                {
                    long stop = System.currentTimeMillis();
                    _timePrinting += (stop - start);
                    Loggers.IO.debug(
                        ((_outputFile != null) ? _outputFile
                                               : _inputFile) + ":0:0:printing took "
                        + (stop - start));
                }
            }

            addFileHistoryEntry(packageName, checksumWriter);
        }
        finally
        {
            unsetLocalVariables(environment);

            if (out != null)
            {
                out.close();
            }

            if (isChecksum())
            {
                if (_outputWriter != null)
                {
                    _outputWriter.close();
                }
            }
        }
    }


    /**
     * Return a buffered reader from <code>file</code> using <code>encoding</code>. If 
     * the encoding is <code>null</code>, then the default encoding will be used.
     *
     * @param file input file
     * @param encoding file character encoding
     * @return a buffered reader 
     */
    private static BufferedReader getBufferedReader(File file, String encoding)
      throws FileNotFoundException
    {
        BufferedReader reader;

        try 
        {
            if (encoding != null) 
            {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            }
            else 
            {
                reader = new BufferedReader(new FileReader(file));
            }
        }
        catch (UnsupportedEncodingException e) {
            throw new FileNotFoundException("Unsupported encoding " + encoding);
        }
        
        return reader;
    }


    /**
     * Return a buffered writer to <code>file</code> using <code>encoding</code>. If 
     * the encoding is <code>null</code>, then the default encoding will be used.
     *
     * @param file output file
     * @param encoding file character encoding
     * @return a buffered writer 
     */
    private static BufferedWriter getBufferedWriter(File file, String encoding)
      throws IOException
    {
        BufferedWriter writer;

        if (encoding != null) 
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
        }
        else 
        {
            writer = new BufferedWriter(new FileWriter(file));
        }
        
        return writer;
    }


    /**
     * Restores the original file.
     *
     * @param original original file
     * @param backup backup of the original file.
     */
    private void restore(
        File original,
        File backup)
    {
        // check if we're in FILE_FILE mode (both source and target are files)
        if ((original != null) && (backup != null))
        {
            _args[0] = original.getAbsolutePath();
            _args[1] = backup.getAbsolutePath();
            Loggers.IO.l7dlog(Level.INFO, "FILE_RESTORE" /* NOI18N */, _args, null);

            try
            {
                Copy.file(backup, original, true);
                original.setLastModified(backup.lastModified());
                backup.delete();
            }
            catch (IOException ex)
            {
                Loggers.IO.l7dlog(
                    Level.FATAL, "FILE_RESTORE_ERROR" /* NOI18N */, _args, ex);
            }
        }
    }


    /**
     * Unsets the local macro variables.
     *
     * @param environment the current environment.
     *
     * @since 1.0b8
     */
    private void unsetLocalVariables(Environment environment)
    {
        environment.unset(Environment.Variable.FILE_NAME.getName());
        environment.unset(Environment.Variable.FILE.getName());
        environment.unset(Environment.Variable.PACKAGE.getName());
        environment.unset(Environment.Variable.FILE_FORMAT.getName());
        environment.unset(Environment.Variable.TAB_SIZE.getName());
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Represents a Jalopy run state. You may want to use {@link Jalopy#getState()} to
     * query the engine about its current state.
     *
     * @since 1.0b8
     */
    public static final class State
    {
        /** Indicates a finished run without any warnings or errors. */
        public static final State OK = new State("Jalopy.State [ok]" /* NOI18N */);

        /** Indicates a finished run which produced warnings. */
        public static final State WARN = new State("Jalopy.State [warn]" /* NOI18N */);

        /** Indicates a finished run, that failed. */
        public static final State ERROR = new State("Jalopy.State [error]" /* NOI18N */);

        /** Indicates a successful parse phase. */
        public static final State PARSED =
            new State("Jalopy.State [parsed]" /* NOI18N */);

        /** Indicates a successful inspection phase. */
        public static final State INSPECTED =
            new State("Jalopy.State [inspected]" /* NOI18N */);

        /** Indicates the running state (no phase yet finished). */
        public static final State RUNNING =
            new State("Jalopy.State [running]" /* NOI18N */);

        /** Indicates the undefined state (a run was not yet startet). */
        public static final State UNDEFINED =
            new State("Jalopy.State [undefined]" /* NOI18N */);

        /** The name of the state. */
        final String name;

        /**
         * Creates a new State object.
         *
         * @param name name of the state.
         */
        private State(String name)
        {
            this.name = name;
        }

        /**
         * Returns a string representation of this state.
         *
         * @return a string representation of this state.
         */
        public String toString()
        {
            return this.name;
        }
    }


    /**
     * Detects whether and what kind of messages were produced during a run. Updates the
     * state info accordingly.
     */
    private final class SpyAppender
        extends AppenderSkeleton
    {
        public SpyAppender()
        {
            this.name = "JalopySpyAppender" /* NOI18N */;
        }

        public void append(LoggingEvent ev)
        {
            switch (ev.getLevel().toInt())
            {
                case Priority.WARN_INT :

                    if (_state != State.ERROR)
                    {
                        _state = State.WARN;
                    }

                    break;

                case Priority.ERROR_INT :
                case Priority.FATAL_INT :
                    _state = State.ERROR;

                    break;
            }
        }


        public void close()
        {
        }


        public synchronized void doAppend(LoggingEvent ev)
        {
            append(ev);
        }


        public boolean requiresLayout()
        {
            return false;
        }


        protected boolean checkEntryConditions()
        {
            return true;
        }
    }
}

