/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import de.hunsicker.io.Copy;
import de.hunsicker.io.ExtensionFilter;
import de.hunsicker.io.IoHelper;
import de.hunsicker.jalopy.language.DeclarationType;
import de.hunsicker.jalopy.language.ModifierType;
import de.hunsicker.util.ChainingRuntimeException;
import de.hunsicker.util.StringHelper;

import org.apache.log4j.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


//J- needed only as a workaround for a Javadoc bug
import java.lang.NullPointerException;
import java.lang.Object;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
//J+

/**
 * Represents a code convention: the settings that describe the desired coding style for
 * Java source files.
 * 
 * <p>
 * To ensure type-safety, valid key access, two accompanying classes are provided:
 * </p>
 * <pre class="snippet">
 * {@link Convention} settings = {@link Convention}.getInstance();
 * int numThreads = settings.getInt({@link ConventionKeys}.THREAD_COUNT,
 *                                  {@link ConventionDefaults}.THREAD_COUNT));
 * </pre>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @author <a href="http://jalopy.sf.net/contact.html">Roman Sarychev</a>
 * @version $Revision: 1.6 $
 *
 * @see de.hunsicker.jalopy.storage.ConventionKeys
 * @see de.hunsicker.jalopy.storage.ConventionDefaults
 */
public final class Convention
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The file extension for Jalopy binary code convention files (&quot;.jal&quot;). */
    public static final String EXTENSION_JAL = ".jal" /* NOI18N */;

    /** The file extension for Jalopy XML code convention files (&quot;.xml&quot;). */
    public static final String EXTENSION_XML = ".xml" /* NOI18N */;

    /**
     * The file extension for Jalopy local binary code convention files
     * (&quot;.dat&quot;).
     */
    public static final String EXTENSION_DAT = ".dat" /* NOI18N */;

    /** The filename of project files. */
    private static final String FILENAME_PROJECT = "project.dat" /* NOI18N */;

    /** The filename of the code convention files. */
    private static final String FILENAME_PREFERENCES = "preferences.dat" /* NOI18N */;

    /** The filename of the code convention settings files. */
    private static final String FILENAME_SETTINGS = "settings.xml" /* NOI18N */;

    /** The name of the repository directories. */
    private static final String NAME_REPOSITORY = "repository" /* NOI18N */;

    /** The filename of the history files. */
    private static final String FILENAME_HISTORY = "history.dat" /* NOI18N */;

    /** The name of the backup directories. */
    private static final String NAME_BACKUP = "bak" /* NOI18N */;

    /** The empty map, effectively means the default values. */
    private static final Map EMPTY_MAP = new HashMap(); // Map of <Convention.Key>:<String>

    /** The current version number. */
    private static final String VERSION = "6" /* NOI18N */;

    /** Object to synchronize processes on. */
    private static final Object _lock = new Object();

    /** The sole instance of this class. */
    private static Convention INSTANCE;

    /**
     * The empty code convention, used if no code convention could be loaded from
     * persistent storage. This either means no code convention were ever stored or
     * something went wrong during the loading process. In either way the build-in
     * defaults will be used.
     */
    private static final Convention EMPTY_PREFERENCES = new Convention(EMPTY_MAP);

    /** Our default project. */
    private static final Project DEFAULT_PROJECT =
        new Project(
            "default" /* NOI18N */, "The Jalopy default project space." /* NOI18N */);

    /** The default project. */
    private static Project _project = DEFAULT_PROJECT;

    /** Base settings directory. */
    private static File _settingsDirectory;

    /** Bbackup directory. */
    private static File _backupDirectory;

    /** Class type repository directory. */
    private static File _repositoryDirectory;

    /** The local code convention file. */
    private static File _settingsFile;

    /** The active project settings directory. */
    private static File _projectSettingsDirectory;

    /** The active project history file. */
    private static File _historyFile;

    static
    {
        // TODO Change to dynamiclly specify
        _settingsDirectory =
            new File(
                System.getProperty("user.home" /* NOI18N */) + File.separator
                + ".jalopy.15" /* NOI18N */    );

        Project project = loadProject();
        _project = project;
        setDirectories(project);

        File settingsFile = null;
        InputStream in = null;

        try
        {
            settingsFile = getSettingsFile();

            // first load the system code convention file
            if (settingsFile.exists())
            {
                in = new FileInputStream(settingsFile);
                INSTANCE = readFromStream(in);
            }
            else
            {
                INSTANCE = EMPTY_PREFERENCES;
            }

            String location =
                INSTANCE.get(ConventionKeys.STYLE_LOCATION, "" /* NOI18N */);

            // if the user specified a distributed location to load
            // code convention from, try to sync
            if (location.startsWith("http" /* NOI18N */))
            {
                try
                {
                    importSettings(new URL(location));

                    // update the location, so we keep synchronizing further on
                    INSTANCE.put(ConventionKeys.STYLE_LOCATION, location);
                    INSTANCE.flush();
                }
                catch (IOException ex)
                {
                    Object[] args = { location };
                    Loggers.IO.l7dlog(
                        Level.WARN, "PREF_COULD_NOT_CONNECT" /* NOI18N */, args, null);
                }
            }
            else
            {
                synchronize(INSTANCE);
            }
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();

            Object[] args = { settingsFile };
            Loggers.IO.l7dlog(Level.WARN, "PREF_ERROR_LOADING" /* NOI18N */, args, ex);

            // actually means the build-in defaults will be used
            INSTANCE = EMPTY_PREFERENCES;
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ignored)
                {
                    ;
                }
            }
        }

        if (project.getName().equals(DEFAULT_PROJECT.getName()))
        {
            // update to our new
            if (
                !_projectSettingsDirectory.exists()
                || new File(_settingsDirectory, "preferences.jal" /* NOI18N */).exists())
            {
                if (IoHelper.ensureDirectoryExists(_projectSettingsDirectory))
                {
                    File[] files = _settingsDirectory.listFiles();

                    for (int i = 0; i < files.length; i++)
                    {
                        if (files[i].getName().endsWith("preferences.jal" /* NOI18N */))
                        {
                            try
                            {
                                Copy.file(
                                    files[i],
                                    new File(
                                        _projectSettingsDirectory, FILENAME_PREFERENCES));
                                files[i].delete();
                            }
                            catch (IOException ex)
                            {
                                ;
                            }
                        }
                        else if (!files[i].getName().equals(FILENAME_PROJECT))
                        {
                            if (
                                files[i].isDirectory()
                                && (files[i].getName().equals(NAME_BACKUP)
                                || files[i].getName().equals(NAME_REPOSITORY)))
                            {
                                IoHelper.delete(files[i], true);
                            }
                        }
                    }

                    try
                    {
                        IoHelper.serialize(
                            project, new File(
                                _projectSettingsDirectory, FILENAME_PROJECT));
                    }
                    catch (IOException ex)
                    {
                        ;
                    }
                }
            }
        }
    }

    //~ Instance variables ---------------------------------------------------------------

    private Locale _locale;

    /** Holds the last snapshot. */
    private Map _snapshot; // Map of <Convention.Key>:<String>

    /** The map which holds the actual values. */
    private Map _values = EMPTY_MAP; // Map of <Convention.Key>:<String>

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new code convention object.
     *
     * @param values the actual code convention setttings.
     */
    private Convention(Map values)
    {
        if (!values.isEmpty())
        {
            Iterator keys = values.keySet().iterator();

            Object key = keys.next();

            if (key instanceof de.hunsicker.jalopy.prefs.Key)
            {
                Map t = new HashMap(values.size());

                for (Iterator i = values.entrySet().iterator(); i.hasNext();)
                {
                    Map.Entry entry = (Map.Entry) i.next();
                    t.put(new Key(entry.getKey().toString()), entry.getValue());
                }

                values = t;
            }
        }

        _values = values;
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the current project's backup directory path.
     *
     * @return backup directory.
     *
     * @since 1.0b8
     */
    public static File getBackupDirectory()
    {
        return _backupDirectory;
    }


    /**
     * Returns the default project.
     *
     * @return the default project.
     *
     * @since 1.0b8
     */
    public static Project getDefaultProject()
    {
        return DEFAULT_PROJECT;
    }


    /**
     * Returns the history backing store file.
     *
     * @return history backing store file.
     */
    public static File getHistoryFile()
    {
        return _historyFile;
    }


    /**
     * Returns the sole instance of this object.
     *
     * @return the sole instance of this object.
     */
    public static Convention getInstance()
    {
        return INSTANCE;
    }


    /**
     * Returns the preferred locale.
     *
     * @return the preferred locale.
     *
     * @since 1.0b9
     */
    public Locale getLocale()
    {
        return _locale;
    }


    /**
     * Sets the currently active project.
     *
     * @param project the new active project.
     *
     * @return <code>true</code> if setting the project was successful.
     *
     * @since 1.0b8
     */
    public static boolean setProject(Project project)
    {
        synchronized (_lock)
        {
            try
            {
                INSTANCE.snapshot();

                File activeFile = getSettingsFile();

                // change all directories to point to the new project directory
                setDirectories(project);
                storeProject(project);

                File file = getSettingsFile();

                if (file.exists())
                {
                    importSettings(file);
                }
                else if (activeFile.exists())
                {
                    importSettings(activeFile);
                }

                _project = project;

                return true;
            }
            catch (IOException ex)
            {
                _project = DEFAULT_PROJECT;
                setDirectories(_project);
                INSTANCE.revert();

                return false;
            }
        }
    }


    /**
     * Returns the current project settings directory.
     *
     * @return settings directory.
     *
     * @since 1.0b8
     */
    public static File getProjectSettingsDirectory()
    {
        return _projectSettingsDirectory;
    }


    /**
     * Returns the current project's class repository directory.
     *
     * @return class repository directory.
     *
     * @since 1.0b8
     */
    public static File getRepositoryDirectory()
    {
        return _repositoryDirectory;
    }


    /**
     * Returns the base settings directory.
     *
     * @return settings directory.
     *
     * @since 1.0b8
     */
    public static File getSettingsDirectory()
    {
        return _settingsDirectory;
    }


    /**
     * Returns the local code convention file.
     *
     * @return local code convention file.
     *
     * @since 1.0b8
     */
    public static File getSettingsFile()
    {
        return _settingsFile;
    }


    /**
     * Adds a new project. Adding a project means that the settings of the currently
     * active project will be duplicated to the new project settings directory.
     *
     * @param project the project information.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b8
     */
    public static void addProject(Project project)
      throws IOException
    {
        synchronized (_lock)
        {
            File projectDirectory = new File(getSettingsDirectory(), project.getName());
            File activeProjectDirectory =
                new File(getSettingsDirectory(), _project.getName());

            try
            {
                if (activeProjectDirectory.exists())
                {
                    File[] files =
                        activeProjectDirectory.listFiles(
                            new ExtensionFilter(EXTENSION_DAT));

                    // copy the settings files from the active project directory
                    // into the new one
                    for (int i = 0; i < files.length; i++)
                    {
                        if (files[i].isFile())
                        {
                            Copy.file(
                                files[i], new File(projectDirectory, files[i].getName()));
                        }
                    }
                }

                if (IoHelper.ensureDirectoryExists(projectDirectory))
                {
                    IoHelper.serialize(
                        project, new File(projectDirectory, FILENAME_PROJECT));
                }
            }
            catch (IOException ex)
            {
                IoHelper.delete(projectDirectory, true);
                throw ex;
            }
        }
    }


    /**
     * Imports the code convention from the specified input stream.
     *
     * @param in the input stream from which to read the code convention.
     * @param extension file extension indicating the format of the saved code
     *        convention.
     *
     * @throws IOException if an I/O error occured.
     * @throws IllegalArgumentException if an invalid extension was specified.
     */
    public static void importSettings(
        InputStream in,
        String      extension)
      throws IOException
    {
        if (EXTENSION_DAT.equals(extension) || EXTENSION_JAL.equals(extension))
        {
            INSTANCE._values = (Map) IoHelper.deserialize(in);
            synchronize(INSTANCE);
        }
        else if (EXTENSION_XML.equals(extension))
        {
        	BufferedInputStream isr = null;

            try
            {
                isr = 
                        new BufferedInputStream(in);

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                Document doc = dbf.newDocumentBuilder().parse(isr);
                //SAXBuilder builder = new SAXBuilder();
                //Document document = builder.build(isr);
                INSTANCE._values = new HashMap();
                convertXmlToMap(INSTANCE._values, doc.getDocumentElement());
                synchronize(INSTANCE);
            }
            catch (SAXException ex)
            {
                throw new IOException(ex.getMessage());
            } 
            catch (ParserConfigurationException ex) {
                throw new IOException(ex.getMessage());
			}
            finally
            {
                if (isr != null)
                {
                    try
                    {
                        isr.close();
                    }
                    catch (IOException ignored)
                    {
                        ;
                    }
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("invalid extension -- " + extension);
        }
    }


    /**
     * Imports the code convention from the specified url.
     *
     * @param url url to import the code convention from.
     *
     * @throws IOException an I/O error occured.
     * @throws ChainingRuntimeException If an error occurs
     */
    public static void importSettings(URL url)
      throws IOException
    {
        InputStream in = null;

        try
        {
            in = url.openStream();
            importSettings(in, getExtension(url));
        }
        catch (MalformedURLException ex)
        {
            throw new ChainingRuntimeException(
                "Could not load code convention from the given url -- " + url, ex);
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException ignored)
            {
                ;
            }
        }
    }


    /**
     * Imports the code convention from the given file.
     *
     * @param file code convention file.
     *
     * @throws IOException if an I/O error occured.
     * @throws FileNotFoundException File not found
     * @throws IllegalArgumentException Illegal arg
     */
    public static void importSettings(File file)
      throws IOException
    {
        InputStream in = null;

        try
        {
            if (!file.exists())
            {
                // if no explicit path was given
                if (file.getAbsolutePath().indexOf(File.separatorChar) < 0)
                {
                    file =
                        new File(
                            System.getProperty("user.dir" /* NOI18N */) + File.separator
                            + file);

                    if (file.exists()) // first search the current directory
                    {
                        in = new FileInputStream(file);
                    }
                    else // then the user's home directory
                    {
                        file =
                            new File(
                                System.getProperty("user.home" /* NOI18N */)
                                + File.separator + file);

                        if (file.exists())
                        {
                            in = new FileInputStream(file);
                        }
                        else // and finally the Jalopy .jar
                        {
                            in = Convention.class.getResourceAsStream(
                                    file.getAbsolutePath());

                            if (in == null)
                            {
                                throw new FileNotFoundException(
                                    "file not found -- " + file.getAbsolutePath());
                            }
                        }
                    }
                }
                else
                {
                    throw new FileNotFoundException(file.getAbsolutePath());
                }
            }
            else if (file.isFile())
            {
                in = new FileInputStream(file);
            }
            else
            {
                throw new IllegalArgumentException(
                    "no valid file -- " + file.getAbsolutePath());
            }

            importSettings(in, getExtension(file));
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException ignored)
            {
                ;
            }
        }
    }


    /**
     * Removes the given project.
     *
     * @param project a project.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b8
     */
    public static void removeProject(Project project)
      throws IOException
    {
        synchronized (_lock)
        {
            File projectDirectory = new File(getSettingsDirectory(), project.getName());
            IoHelper.delete(projectDirectory, true);
        }
    }


    /**
     * Returns the boolean value associated with the given key.
     * 
     * <p>
     * This implementation invokes {@link #get(Convention.Key,String) <tt>get(key,
     * null)</tt>}. If the return value is non-null, it is compared with <tt>"true"</tt>
     * using {@link String#equalsIgnoreCase(String)}. If the comparison returns
     * <tt>true</tt>, this invocation returns <tt>true</tt>. Otherwise, the original
     * return value is compared with <tt>"false"</tt>, again using {@link
     * String#equalsIgnoreCase(String)}. If the comparison returns <tt>true</tt>, this
     * invocation returns <tt>false</tt>. Otherwise, this invocation returns
     * <tt>def</tt>.
     * </p>
     *
     * @param key key whose associated value is to be returned as a boolean.
     * @param def the value to be returned in the event that this preference node has no
     *        value associated with <tt>key</tt> or the associated value cannot be
     *        interpreted as a boolean.
     *
     * @return the boolean value represented by the string associated with <tt>key</tt>
     *         in this preference node, or <tt>def</tt> if the associated value does not
     *         exist or cannot be interpreted as a boolean.
     */
    public boolean getBoolean(
        Key     key,
        boolean def)
    {
        boolean result = def;
        String value = get(key, null);

        if (value != null)
        {
            if (value.equalsIgnoreCase("true" /* NOI18N */))
            {
                result = true;
            }
            else if (value.equalsIgnoreCase("false" /* NOI18N */))
            {
                result = false;
            }
        }

        return result;
    }


    /**
     * Returns the int value represented by the string associated with the specified key
     * in this preference node. The string is converted to an integer as by {@link
     * Integer#parseInt(String)}. Returns the specified default if there is no value
     * associated with the key, the backing store is inaccessible, or if {@link
     * Integer#parseInt(String)} would throw a {@link NumberFormatException} if the
     * associated value were passed. This method is intended for use in conjunction with
     * {@link #putInt}.
     *
     * @param key key whose associated value is to be returned as an int.
     * @param def the value to be returned in the event that this preference node has no
     *        value associated with <tt>key</tt> or the associated value cannot be
     *        interpreted as an int, or the backing store is inaccessible.
     *
     * @return the int value represented by the string associated with <tt>key</tt> in
     *         this preference node, or <tt>def</tt> if the associated value does not
     *         exist or cannot be interpreted as an int.
     *
     * @see #putInt(Convention.Key,int)
     * @see #get(Convention.Key,String)
     */
    public int getInt(
        Key key,
        int def)
    {
        int result = def;

        try
        {
            String value = get(key, null);

            if (value != null)
            {
                result = Integer.parseInt(value);
            }
        }
        catch (NumberFormatException ex)
        {
            ;
        }

        return result;
    }


    /*public boolean equals(Object obj)
       {
           if (obj instanceof Convention)
           {
               return _values.equals((Convention)obj);
           }

           return false;
       }*/

    /**
     * Exports the code convention to the given file. The file extension determines the
     * format in which the code convention will be written.
     *
     * @param file file to export the code convention to.
     *
     * @throws IOException if writing to the specified output stream failed.
     */
    public void exportSettings(File file)
      throws IOException
    {
        exportSettings(new FileOutputStream(file), getExtension(file));
    }


    /**
     * Emits the code convention in a format indicated by the given extension. If no
     * extension is given, the default format will be used (the binary <code>.jal</code>
     * format).
     *
     * @param out the output stream on which to emit the code convention.
     * @param extension output format to use. Either {@link #EXTENSION_JAL} or {@link
     *        #EXTENSION_XML}.
     *
     * @throws IOException if an I/O error occured.
     * @throws IllegalArgumentException if <em>extension</em> is no valid file extension.
     */
    public void exportSettings(
        OutputStream out,
        String       extension)
      throws IOException
    {
        _values.put(ConventionKeys.INTERNAL_VERSION, VERSION);

        if (extension == null)
        {
            extension = EXTENSION_JAL;
        }

        if (EXTENSION_DAT.equals(extension) || EXTENSION_JAL.equals(extension))
        {
            IoHelper.serialize(_values, new BufferedOutputStream(out));
        }
        else if (EXTENSION_XML.equals(extension))
        {
            try
            {
                //XMLOutputter outputter = new XMLOutputter("    " /* NOI18N */, true);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                Document doc = dbf.newDocumentBuilder().newDocument();
                convertMapToXml(_values,doc);
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform(new DOMSource(doc),new StreamResult(out));
                
                //Document document = new Document(convertMapToXml(_values));
                //outputter.output(document, out);
                // TODO dbf.
            } catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            finally
            {
                out.close();
            }
        }
        else
        {
            throw new IllegalArgumentException("invalid file extension -- " + extension);
        }
    }


    /**
     * Flushes the code convention to the persistent store.
     *
     * @throws IOException if an I/O error occured.
     */
    public void flush()
      throws IOException
    {
        File directory = getProjectSettingsDirectory();

        if (!IoHelper.ensureDirectoryExists(directory))
        {
            throw new IOException("could not create settings directory -- " + directory);
        }

        _values.put(ConventionKeys.INTERNAL_VERSION, VERSION);

        // write the values to disk
        IoHelper.serialize(_values, getSettingsFile());

        // update the project file in the current project directory
        storeProject(_project);

        //Writer writer = new BufferedWriter(new FileWriter("c:/test.xml"));

        /*ObjOut out = new ObjOut(writer, false, new Config().aliasID(false));
        out.writeObject(_values);
        writer.close();*/
    }


    /**
     * Returns the value associated with the given key.
     * 
     * <p>
     * This implementation first checks to see if <tt>key</tt> is <tt>null</tt> throwing
     * a <tt>NullPointerException</tt> if this is the case.
     * </p>
     *
     * @param key key whose associated value is to be returned.
     * @param def the value to be returned in the event that this preference node has no
     *        value associated with <tt>key</tt>.
     *
     * @return the value associated with <tt>key</tt>, or <tt>def</tt> if no value is
     *         associated with <tt>key</tt>.
     *
     * @throws NullPointerException if key is <tt>null</tt>. (A <tt>null</tt> default
     *         <i>is</i> permitted.)
     */
    public String get(
        Key    key,
        String def)
    {
        if (key == null)
        {
            throw new NullPointerException("null no valid key");
        }

        String result = null;

        try
        {
            result = (String) _values.get(key);
        }
        catch (Exception ignored)
        {
            ;
        }

        return ((result == null) ? def
                                 : result);
    }


    /**
     * Implements the <tt>put</tt> method as per the specification in {@link
     * Convention#put(Convention.Key,String)}.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     *
     * @throws NullPointerException if key or value is <tt>null</tt>.
     */
    public void put(
        Key    key,
        String value)
    {
        if ((key == null) || (value == null))
        {
            throw new NullPointerException(key + ", " + value);
        }
        if (key._name.equals(ConventionKeys.SORT_ORDER._name)) {
            key = key;
            value = value.trim();
        }

        _values.put(key, value);
    }


    /**
     * Implements the <tt>putBoolean</tt> method as per the specification in {@link
     * Convention#putBoolean(Convention.Key,boolean)}.
     * 
     * <p>
     * This implementation translates <tt>value</tt> to a string with {@link
     * String#valueOf(boolean)} and invokes {@link #put(Convention.Key,String)} on the
     * result.
     * </p>
     *
     * @param key key with which the string form of value is to be associated.
     * @param value value whose string form is to be associated with key.
     */
    public void putBoolean(
        Key     key,
        boolean value)
    {
        put(key, String.valueOf(value));
    }


    /**
     * Implements the <tt>putInt</tt> method as per the specification in {@link
     * Convention#putInt(Convention.Key,int)}.
     * 
     * <p>
     * This implementation translates <tt>value</tt> to a string with {@link
     * Integer#toString(int)} and invokes {@link #put(Convention.Key,String)} on the
     * result.
     * </p>
     *
     * @param key key with which the string form of value is to be associated.
     * @param value value whose string form is to be associated with key.
     */
    public void putInt(
        Key key,
        int value)
    {
        put(key, Integer.toString(value));
    }


    /**
     * Reverts the code convention to the state of the last snapshot. If no snapshots
     * exists, the call will be ignored.
     *
     * @see #snapshot
     * @since 1.0b8
     */
    public void revert()
    {
        synchronized (_lock)
        {
            if (_snapshot != null)
            {
                _values.clear();
                _values.putAll(_snapshot);
                _snapshot = null;
            }
        }
    }


    /**
     * Creates an internal snapshot of the current code convention.  You can then use
     * {@link #revert} at any time to revert the code convention to the state of the
     * last snapshot.
     *
     * @see #revert
     * @since 1.0b8
     */
    public void snapshot()
    {
        synchronized (_lock)
        {
            _snapshot = new HashMap(_values);
        }
    }


    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object.
     */
    public String toString()
    {
        return _values.toString();
    }


    /**
     * Sets the directory for the project
     *
     * @param project The project
     */
    private static void setDirectories(Project project)
    {
        _projectSettingsDirectory = new File(_settingsDirectory, project.getName());
        _backupDirectory = new File(_projectSettingsDirectory, NAME_BACKUP);
        _repositoryDirectory = new File(_projectSettingsDirectory, NAME_REPOSITORY);
        _settingsFile = new File(_projectSettingsDirectory, FILENAME_PREFERENCES);
        _historyFile = new File(_projectSettingsDirectory, FILENAME_HISTORY);
    }


    /**
     * Returns the file extension of the given string, denoting a file path.
     *
     * @param location string denoting a file path.
     *
     * @return file extension of the given file.
     *
     * @throws IOException if the given location does not denote a valid code convention
     *         file.
     * @throws IllegalArgumentException if <em>location</em> does not have a valid
     *         extension (Either {@link #EXTENSION_JAL} or {@link #EXTENSION_XML}).
     */
    private static String getExtension(String location)
      throws IOException
    {
        int offset = location.lastIndexOf('.');

        if (offset > -1)
        {
            String extension = location.substring(offset);

            if (
                (extension == null) && !EXTENSION_JAL.equals(extension)
                && EXTENSION_DAT.equals(extension) && !EXTENSION_XML.equals(extension))
            {
                throw new IOException("no valid location given -- " + location);
            }

            return extension;
        }

        throw new IllegalArgumentException("invalid file extension -- " + location);
    }


    /**
     * Returns the file extension of the file.
     *
     * @param file a file.
     *
     * @return file extension of the given file.
     *
     * @throws IOException if the given file does not denote a valid code convention
     *         file.
     */
    private static String getExtension(File file)
      throws IOException
    {
        return getExtension(file.getName());
    }


    /**
     * Returns the file extension of the url.
     *
     * @param url an url.
     *
     * @return file extension of the given url.
     *
     * @throws IOException if the given url does not denote a valid code convention file.
     */
    private static String getExtension(URL url)
      throws IOException
    {
        return getExtension(url.getFile());
    }


    /**
     * Returns the history policy for the given string.
     *
     * @param policy string indidcating the old integer-based history.
     *
     * @return History policy for the given string.
     *
     * @since 1.0b8
     */
    private static History.Policy getHistoryPolicy(String policy)
    {
        if ("1" /* NOI18N */.equals(policy))
        {
            return History.Policy.COMMENT;
        }
        else if ("2" /* NOI18N */.equals(policy))
        {
            return History.Policy.FILE;
        }
        else
        {
            return History.Policy.DISABLED;
        }
    }


    /**
     * Converts a JDOM tree to a Map represenation.
     *
     * @param map the map to hold the values.
     * @param element root element of the JDOM tree.
     */
    private static void convertXmlToMap(
        Map     map,
        Element element)
    {
        NodeList children = element.getChildNodes();

        if (children.getLength() == 0)
        {
            StringBuffer path = new StringBuffer();
            String value = "";//;element.getTagName();

            while (true)
            {
                if (path.length() > 0)
                {
                    path.insert(0, '/');
                }

                path.insert(0, element.getTagName());
                element = (Element) element.getParentNode();

                if ((element == null) || element.getTagName().equals("jalopy" /* NOI18N */))
                {
                    break;
                }
            }

            map.put(new Key(new String(path)), value);

            return;
        }

        for (int i = 0, i_len = children.getLength(); i < i_len; i++)
        {
            if (children instanceof Element && children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);
                convertXmlToMap(map, childElement);
                
            }
            
            else if (i_len == 1){
                StringBuffer path = new StringBuffer();
                String value = element.getFirstChild().getNodeValue();
    
                while (true)
                {
                    if (path.length() > 0)
                    {
                        path.insert(0, '/');
                    }
    
                    path.insert(0, element.getTagName());
                    if (element.getParentNode() instanceof Element) {
                    element = (Element) element.getParentNode();
                    }
                    else {
                        element = null;
                    }
    
                    if ((element == null) || element.getTagName().equals("jalopy" /* NOI18N */))
                    {
                        break;
                    }
                }
                
    
                map.put(new Key(new String(path)), value);
    
                return;
            }
            else {
                // Skip format node
            }
        }
    }


    /**
     * Loads the active project from persistent storage.
     *
     * @return the currently active project.
     *
     * @since 1.0b8
     */
    private static Project loadProject()
    {
        try
        {
            File file = new File(getSettingsDirectory(), FILENAME_PROJECT);

            if (file.exists())
            {
                Project project = (Project) IoHelper.deserialize(file);

                return project;
            }
            else
            {
                return DEFAULT_PROJECT;
            }
        }
        catch (Throwable ex)
        {
            return DEFAULT_PROJECT;
        }
    }


    /**
     * Reads the code convention from the given stream.
     *
     * @param in stream to read the code convention from.
     *
     * @return the code convention just read.
     *
     * @throws IOException if an I/O error occured.
     * @throws ClassNotFoundException if a class could not be found.
     */
    private static Convention readFromStream(InputStream in)
      throws IOException, ClassNotFoundException
    {
        return new Convention((Map) IoHelper.deserialize(new BufferedInputStream(in)));
    }


    /**
     * Renames the given code convention key.
     *
     * @param settings the convention that holds the key.
     * @param oldName the old key name.
     * @param newKey the new key name.
     *
     * @since 1.0b6
     */
    private static void renameKey(
        Convention settings,
        String     oldName,
        Key        newKey)
    {
        Object value = settings._values.remove(new Convention.Key(oldName));

        if (value != null)
        {
            settings._values.put(newKey, value);
        }
        else
        {
            /**
             * @todo use logger
             */

            //System.err.println("[WARN] could not found key -- " + oldName);
        }
    }


    /**
     * Stores the given project (it will be serialized to disk).
     *
     * @param project project to made persistent.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b8
     */
    private static void storeProject(Project project)
      throws IOException
    {
        File file = new File(getProjectSettingsDirectory(), FILENAME_PROJECT);
        IoHelper.serialize(project, file);

        if (!project.getName().equals(DEFAULT_PROJECT.getName()))
        {
            // don't forget to keep track of the active project
            IoHelper.serialize(
                project, new File(getSettingsDirectory(), FILENAME_PROJECT));
        }
        else
        {
            File f = new File(getSettingsDirectory(), FILENAME_PROJECT);
            f.delete();
        }
    }


    /**
     * Changes the code convention settings format from an old, unsupported format (prior
     * to 1.0b6) to the new format (all values will be lost).
     *
     * @param settings code convention settings to update.
     *
     * @since 1.0b6
     */
    private static void sync0To1(Convention settings)
    {
        INSTANCE = EMPTY_PREFERENCES;
        INSTANCE._values = EMPTY_MAP;
    }


    /**
     * Changes the code convention settings format from version 1 to version 2.
     *
     * @param settings code convention settings to update.
     *
     * @since 1.0b7
     */
    private static void sync1To2(Convention settings)
    {
        renameKey(
            settings, "printer/alignment/throwsTypes" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_TYPES_THROWS);
        renameKey(
            settings, "printer/alignment/implementsTypes" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_TYPES_IMPLEMENTS);
        renameKey(
            settings, "printer/alignment/extendsTypes" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_TYPES_EXTENDS);

        Object collapse = INSTANCE._values.get("transform/import/collapse" /* NOI18N */);

        if (collapse != null)
        {
            INSTANCE._values.remove("transform/import/collapse" /* NOI18N */);

            if ("true" /* NOI18N */.equals(collapse))
            {
                INSTANCE.putInt(ConventionKeys.IMPORT_POLICY, 2);
            }
        }

        Object expand = INSTANCE._values.get("transform/import/expand" /* NOI18N */);

        if (expand != null)
        {
            INSTANCE._values.remove("transform/import/expand" /* NOI18N */);

            if ("true" /* NOI18N */.equals(expand))
            {
                INSTANCE.putInt(ConventionKeys.IMPORT_POLICY, 1);
            }
        }
    }


    /**
     * Changes the code convention settings format from version 2 to version 3.
     *
     * @param settings code convention settings to update.
     *
     * @since 1.0b8
     */
    private static void sync2To3(Convention settings)
    {
        int historyPolicy = INSTANCE.getInt(ConventionKeys.HISTORY_POLICY, 0);

        switch (historyPolicy)
        {
            case -1 :
                INSTANCE.putInt(ConventionKeys.HISTORY_POLICY, 0);

                break;
        }
    }


    /**
     * Changes the code convention settings format from version 3 to version 4.
     *
     * @param settings code convention settings to update.
     *
     * @since 1.0b8
     */
    private static void sync3To4(Convention settings)
    {
        Map values = new HashMap(settings._values.size());

        // make sure we don't use String based keys anymore
        for (Iterator i = settings._values.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry) i.next();

            if (entry.getKey() instanceof String)
            {
                Key key = new Key((String) entry.getKey());
                values.put(key, entry.getValue());
            }
            else
            {
                values.put(entry.getKey(), entry.getValue());
            }
        }

        settings._values = values;
        settings._values.remove(
            new Key("printer/comments/javadoc/templateDescription" /* NOI18N */));
        settings._values.remove(
            new Key("printer/comments/javadoc/templateParam" /* NOI18N */));
        settings._values.remove(
            new Key("printer/comments/javadoc/templateReturn" /* NOI18N */));
        settings._values.remove(
            new Key("printer/comments/javadoc/templateThrows" /* NOI18N */));
        settings._values.remove(new Key("internal/urls/import" /* NOI18N */));
        settings._values.remove(new Key("internal/urls/backup" /* NOI18N */));
        settings._values.remove(new Key("internal/urls/export" /* NOI18N */));
        settings._values.remove(new Key("internal/styleLastPanel" /* NOI18N */));
        settings._values.remove(new Key("internal/styleLastPanelClass" /* NOI18N */));
        settings._values.remove(new Key("internal/styleLastPanelTitle" /* NOI18N */));
        settings._values.remove(new Key("printer/comments/removeSeparator" /* NOI18N */));
        settings._values.remove(new Key("messages/showIoMsg" /* NOI18N */));
        settings._values.remove(new Key("messages/showParserMsg" /* NOI18N */));
        settings._values.remove(new Key("messages/showParserJavadocMsg" /* NOI18N */));
        settings._values.remove(new Key("messages/showTransformMsg" /* NOI18N */));
        settings._values.remove(new Key("messages/showPrinterJavadocMsg" /* NOI18N */));
        settings._values.remove(new Key("messages/showPrinterMsg" /* NOI18N */));

        String sortOrder =
            settings.get(ConventionKeys.SORT_ORDER, DeclarationType.getOrder());

        StringBuffer newSortOrder = new StringBuffer(150);

        for (
            StringTokenizer tokens = new StringTokenizer(sortOrder, "," /* NOI18N */);
            tokens.hasMoreElements();)
        {
            String value = tokens.nextToken();

            if ("Variable" /* NOI18N */.equals(value))
            {
                newSortOrder.append("Static Variables/Initializers," /* NOI18N */);
            }
            else if ("Initializer" /* NOI18N */.equals(value))
            {
                newSortOrder.append(
                    "Instance Variables,Instance Initializers," /* NOI18N */);
            }
            else if ("Constructor" /* NOI18N */.equals(value))
            {
                newSortOrder.append("Constructors," /* NOI18N */);
            }
            else if ("Method" /* NOI18N */.equals(value))
            {
                newSortOrder.append("Methods," /* NOI18N */);
            }
            else if ("Interface" /* NOI18N */.equals(value))
            {
                newSortOrder.append("Interfaces," /* NOI18N */);
            }
            else if ("Class" /* NOI18N */.equals(value))
            {
                newSortOrder.append("Classes," /* NOI18N */);
            }
        }

        // remove the last comma in case we performed substitution
        if (',' == newSortOrder.charAt(newSortOrder.length() - 1))
        {
            newSortOrder = newSortOrder.deleteCharAt(newSortOrder.length() - 1);
        }

        settings._values.put(ConventionKeys.SORT_ORDER, newSortOrder.toString());
        settings._values.put(
            ConventionKeys.HISTORY_POLICY,
            getHistoryPolicy(
                settings.get(ConventionKeys.HISTORY_POLICY, "0" /* NOI18N */)).toString());

        int importPolicy = settings.getInt(ConventionKeys.IMPORT_POLICY, 0);

        switch (importPolicy)
        {
            case 1 : // EXPAND
                settings._values.put(
                    ConventionKeys.IMPORT_POLICY, ImportPolicy.EXPAND.toString());

                break;

            case 2 : // COLLAPSE
                settings._values.put(
                    ConventionKeys.IMPORT_POLICY, ImportPolicy.COLLAPSE.toString());

                break;

            case 0 : // DISABLED
            // fall through
            default :
                settings._values.put(
                    ConventionKeys.IMPORT_POLICY, ImportPolicy.DISABLED.toString());

                break;
        }

        String backupDirectory =
            settings.get(ConventionKeys.BACKUP_DIRECTORY, "" /* NOI18N */).trim();

        // make the backup directory a relative path
        if (
            backupDirectory.endsWith(".jalopy/bak" /* NOI18N */)
            || backupDirectory.endsWith(".jalopy\\bak" /* NOI18N */))
        {
            settings._values.put(ConventionKeys.BACKUP_DIRECTORY, NAME_BACKUP);
        }
    }


    /**
     * Changes the code convention settings format from version 4 to version 5.
     *
     * @param settings code convention settings to update.
     *
     * @since 1.0b9
     */
    private static void sync4To5(Convention settings)
    {
        String header = settings.get(ConventionKeys.HEADER_TEXT, "" /* NOI18N */).trim();
        String[] lines = StringHelper.split(header, "\n" /* NOI18N */);

        StringBuffer buf = new StringBuffer(header.length());

        for (int i = 0; i < lines.length; i++)
        {
            buf.append(StringHelper.trimTrailing(lines[i]));
            buf.append('|');
        }

        if (lines.length > 0)
        {
            buf.deleteCharAt(buf.length() - 1);
        }

        settings.put(ConventionKeys.HEADER_TEXT, buf.toString());

        String footer = settings.get(ConventionKeys.FOOTER_TEXT, "" /* NOI18N */).trim();
        lines = StringHelper.split(footer, "\n" /* NOI18N */);

        buf = new StringBuffer(footer.length());

        for (int i = 0; i < lines.length; i++)
        {
            buf.append(StringHelper.trimTrailing(lines[i]));
            buf.append('|');
        }

        if (lines.length > 0)
        {
            buf.deleteCharAt(buf.length() - 1);
        }

        settings.put(ConventionKeys.FOOTER_TEXT, buf.toString());
    }


    /**
     * Changes the code convention settings format from version 5 to version 6.
     *
     * @param settings code convention settings to update.
     *
     * @since 1.0b9
     */
    private static void sync5To6(Convention settings)
    {
        renameKey(
            settings, "printer/braces/insertBracesIfElse" /* NOI18N */,
            ConventionKeys.BRACE_INSERT_IF_ELSE);
        renameKey(
            settings, "printer/braces/insertBracesFor" /* NOI18N */,
            ConventionKeys.BRACE_INSERT_FOR);
        renameKey(
            settings, "printer/braces/insertBracesWhile" /* NOI18N */,
            ConventionKeys.BRACE_INSERT_WHILE);
        renameKey(
            settings, "printer/braces/insertBracesDoWhile" /* NOI18N */,
            ConventionKeys.BRACE_INSERT_DO_WHILE);
        renameKey(
            settings, "printer/braces/removeBracesIfElse" /* NOI18N */,
            ConventionKeys.BRACE_REMOVE_IF_ELSE);
        renameKey(
            settings, "printer/braces/removeBracesFor" /* NOI18N */,
            ConventionKeys.BRACE_REMOVE_FOR);
        renameKey(
            settings, "printer/braces/removeBracesWhile" /* NOI18N */,
            ConventionKeys.BRACE_REMOVE_WHILE);
        renameKey(
            settings, "printer/braces/removeBracesDoWhile" /* NOI18N */,
            ConventionKeys.BRACE_REMOVE_DO_WHILE);
        renameKey(
            settings, "printer/braces/removeBracesBlock" /* NOI18N */,
            ConventionKeys.BRACE_REMOVE_BLOCK);
        renameKey(
            settings, "printer/braces/treatMethodClassDifferent" /* NOI18N */,
            ConventionKeys.BRACE_TREAT_DIFFERENT);
        renameKey(
            settings, "printer/braces/treatMethodClassDifferentIfWrapped" /* NOI18N */,
            ConventionKeys.BRACE_TREAT_DIFFERENT_IF_WRAPPED);
        renameKey(
            settings, "printer/braces/emptyCuddle" /* NOI18N */,
            ConventionKeys.BRACE_EMPTY_CUDDLE);
        renameKey(
            settings, "printer/braces/emptyInsertStatement" /* NOI18N */,
            ConventionKeys.BRACE_EMPTY_INSERT_STATEMENT);
        renameKey(
            settings, "printer/braces/rightBraceNewLine" /* NOI18N */,
            ConventionKeys.BRACE_NEWLINE_RIGHT);
        renameKey(
            settings, "printer/braces/leftBraceNewLine" /* NOI18N */,
            ConventionKeys.BRACE_NEWLINE_LEFT);

        renameKey(
            settings, "messages/ioMsgPrio" /* NOI18N */, ConventionKeys.MSG_PRIORITY_IO);
        renameKey(
            settings, "messages/parserMsgPrio" /* NOI18N */,
            ConventionKeys.MSG_PRIORITY_PARSER);
        renameKey(
            settings, "messages/parserJavadocMsgPrio" /* NOI18N */,
            ConventionKeys.MSG_PRIORITY_PARSER_JAVADOC);
        renameKey(
            settings, "messages/printerMsgPrio" /* NOI18N */,
            ConventionKeys.MSG_PRIORITY_PRINTER);
        renameKey(
            settings, "messages/printerJavadocMsgPrio" /* NOI18N */,
            ConventionKeys.MSG_PRIORITY_PRINTER_JAVADOC);
        renameKey(
            settings, "messages/transformMsgPrio" /* NOI18N */,
            ConventionKeys.MSG_PRIORITY_TRANSFORM);

        renameKey(
            settings, "printer/whitespace/paddingAssignmentOperators" /* NOI18N */,
            ConventionKeys.PADDING_ASSIGNMENT_OPERATORS);
        renameKey(
            settings, "printer/whitespace/paddingBitwiseOperators" /* NOI18N */,
            ConventionKeys.PADDING_BITWISE_OPERATORS);
        renameKey(
            settings, "printer/whitespace/padddingBraces" /* NOI18N */,
            ConventionKeys.PADDING_BRACES);
        renameKey(
            settings, "printer/whitespace/padddingBrackets" /* NOI18N */,
            ConventionKeys.PADDING_BRACKETS);
        renameKey(
            settings, "printer/whitespace/padddingTypeCast" /* NOI18N */,
            ConventionKeys.PADDING_CAST);
        renameKey(
            settings, "printer/whitespace/paddingLogicalOperators" /* NOI18N */,
            ConventionKeys.PADDING_LOGICAL_OPERATORS);
        renameKey(
            settings, "printer/whitespace/paddingMathematicalOperators" /* NOI18N */,
            ConventionKeys.PADDING_MATH_OPERATORS);
        renameKey(
            settings, "printer/whitespace/padddingParenthesis" /* NOI18N */,
            ConventionKeys.PADDING_PAREN);
        renameKey(
            settings, "printer/whitespace/paddingRelationalOperators" /* NOI18N */,
            ConventionKeys.PADDING_RELATIONAL_OPERATORS);
        renameKey(
            settings, "printer/whitespace/paddingShiftOperators" /* NOI18N */,
            ConventionKeys.PADDING_SHIFT_OPERATORS);
        renameKey(
            settings, "printer/whitespace/afterCastingParenthesis" /* NOI18N */,
            ConventionKeys.SPACE_AFTER_CAST);
        renameKey(
            settings, "printer/whitespace/afterComma" /* NOI18N */,
            ConventionKeys.SPACE_AFTER_COMMA);
        renameKey(
            settings, "printer/whitespace/afterSemiColon" /* NOI18N */,
            ConventionKeys.SPACE_AFTER_SEMICOLON);
        renameKey(
            settings, "printer/whitespace/beforeBraces" /* NOI18N */,
            ConventionKeys.SPACE_BEFORE_BRACES);
        renameKey(
            settings, "printer/whitespace/beforeBrackets" /* NOI18N */,
            ConventionKeys.SPACE_BEFORE_BRACKETS);
        renameKey(
            settings, "printer/whitespace/beforeBracketsTypes" /* NOI18N */,
            ConventionKeys.SPACE_BEFORE_BRACKETS_TYPES);
        renameKey(
            settings, "printer/whitespace/beforeCaseColon" /* NOI18N */,
            ConventionKeys.SPACE_BEFORE_CASE_COLON);
        renameKey(
            settings, "printer/whitespace/beforeLogicalNot" /* NOI18N */,
            ConventionKeys.SPACE_BEFORE_LOGICAL_NOT);
        renameKey(
            settings, "printer/whitespace/beforeMethodCallParenthesis" /* NOI18N */,
            ConventionKeys.SPACE_BEFORE_METHOD_CALL_PAREN);
        renameKey(
            settings, "printer/whitespace/beforeMethodDeclarationParenthesis" /* NOI18N */,
            ConventionKeys.SPACE_BEFORE_METHOD_DEF_PAREN);
        renameKey(
            settings, "printer/whitespace/beforeStatementParenthesis" /* NOI18N */,
            ConventionKeys.SPACE_BEFORE_STATEMENT_PAREN);

        renameKey(
            settings, "inspector/naming/label" /* NOI18N */, ConventionKeys.REGEXP_LABEL);
        renameKey(
            settings, "inspector/naming/localVariable" /* NOI18N */,
            ConventionKeys.REGEXP_LABEL);
        renameKey(
            settings, "inspector/naming/fieldFriendly" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_DEFAULT);
        renameKey(
            settings, "inspector/naming/fieldFriendlyStatic" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_DEFAULT_STATIC);
        renameKey(
            settings, "inspector/naming/fieldFriendlyStaticFinal" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_DEFAULT_STATIC_FINAL);
        renameKey(
            settings, "inspector/naming/fieldPrivate" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_PRIVATE);
        renameKey(
            settings, "inspector/naming/fieldPrivateStatic" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_PRIVATE_STATIC);
        renameKey(
            settings, "inspector/naming/fieldPrivateStaticFinal" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_PRIVATE_STATIC_FINAL);
        renameKey(
            settings, "inspector/naming/fieldProtected" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_PROTECTED);
        renameKey(
            settings, "inspector/naming/fieldProtectedStatic" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_PROTECTED_STATIC);
        renameKey(
            settings, "inspector/naming/fieldProtectedStaticFinal" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_PROTECTED_STATIC_FINAL);
        renameKey(
            settings, "inspector/naming/fieldPublic" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_PUBLIC);
        renameKey(
            settings, "inspector/naming/fieldPublicStatic" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_PUBLIC_STATIC);
        renameKey(
            settings, "inspector/naming/fieldPublicStaticFinal" /* NOI18N */,
            ConventionKeys.REGEXP_FIELD_PUBLIC_STATIC_FINAL);
        renameKey(
            settings, "inspector/naming/methodFriendly" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_DEFAULT);
        renameKey(
            settings, "inspector/naming/methodFriendlyStatic" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_DEFAULT_STATIC);
        renameKey(
            settings, "inspector/naming/methodFriendlyStaticFinal" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_DEFAULT_STATIC_FINAL);
        renameKey(
            settings, "inspector/naming/methodPrivate" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_PRIVATE);
        renameKey(
            settings, "inspector/naming/methodPrivateStatic" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_PRIVATE_STATIC);
        renameKey(
            settings, "inspector/naming/methodPrivateStaticFinal" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_PRIVATE_STATIC_FINAL);
        renameKey(
            settings, "inspector/naming/methodProtected" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_PROTECTED);
        renameKey(
            settings, "inspector/naming/methodProtectedStatic" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_PROTECTED_STATIC);
        renameKey(
            settings, "inspector/naming/methodProtectedStaticFinal" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_PROTECTED_STATIC_FINAL);
        renameKey(
            settings, "inspector/naming/methodPublic" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_PUBLIC);
        renameKey(
            settings, "inspector/naming/methodPublicStatic" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_PUBLIC_STATIC);
        renameKey(
            settings, "inspector/naming/methodPublicStaticFinal" /* NOI18N */,
            ConventionKeys.REGEXP_METHOD_PUBLIC_STATIC_FINAL);
        renameKey(
            settings, "inspector/naming/param" /* NOI18N */, ConventionKeys.REGEXP_PARAM);
        renameKey(
            settings, "inspector/naming/paramFinal" /* NOI18N */,
            ConventionKeys.REGEXP_PARAM_FINAL);
        renameKey(
            settings, "inspector/naming/abstractClasses" /* NOI18N */,
            ConventionKeys.REGEXP_CLASS_ABSTRACT);
        renameKey(
            settings, "inspector/naming/classes" /* NOI18N */, ConventionKeys.REGEXP_CLASS);

        renameKey(
            settings, "general/backupDirectory" /* NOI18N */,
            ConventionKeys.BACKUP_DIRECTORY);
        renameKey(
            settings, "general/backupLevel" /* NOI18N */, ConventionKeys.BACKUP_LEVEL);
        renameKey(
            settings, "general/threadCount" /* NOI18N */, ConventionKeys.THREAD_COUNT);
        renameKey(
            settings, "general/forceFormatting" /* NOI18N */,
            ConventionKeys.FORCE_FORMATTING);

        renameKey(
            settings, "transform/import/grouping" /* NOI18N */,
            ConventionKeys.IMPORT_GROUPING);
        renameKey(
            settings, "transform/import/groupingDepth" /* NOI18N */,
            ConventionKeys.IMPORT_GROUPING_DEPTH);
        renameKey(
            settings, "transform/import/policy" /* NOI18N */, ConventionKeys.IMPORT_POLICY);
        renameKey(
            settings, "transform/import/sort" /* NOI18N */, ConventionKeys.IMPORT_SORT);
        renameKey(
            settings, "transform/misc/insertExpressionParenthesis" /* NOI18N */,
            ConventionKeys.INSERT_EXPRESSION_PARENTHESIS);
        renameKey(
            settings, "transform/misc/insertLoggingConditional" /* NOI18N */,
            ConventionKeys.INSERT_LOGGING_CONDITIONAL);
        renameKey(
            settings, "transform/misc/insertUID" /* NOI18N */,
            ConventionKeys.INSERT_SERIAL_UID);

        renameKey(
            settings, "general/sourceVersion" /* NOI18N */, ConventionKeys.SOURCE_VERSION);
        renameKey(
            settings, "general/styleDescription" /* NOI18N */,
            ConventionKeys.CONVENTION_DESCRIPTION);
        renameKey(
            settings, "general/styleName" /* NOI18N */, ConventionKeys.CONVENTION_NAME);

        renameKey(
            settings, "printer/wrapping/afterAssign" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_ASSIGN);
        renameKey(
            settings, "printer/wrapping/afterChainedMethodCall" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_CHAINED_METHOD_CALL);
        renameKey(
            settings, "printer/wrapping/afterExtendsTypes" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_TYPES_EXTENDS);
        renameKey(
            settings, "printer/wrapping/afterExtendsTypesIfExceeded" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_TYPES_EXTENDS_EXCEED);
        renameKey(
            settings, "printer/wrapping/afterImplementsTypes" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_TYPES_IMPLEMENTS);
        renameKey(
            settings, "printer/wrapping/afterImplementsTypesIfExceeded" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_TYPES_IMPLEMENTS_EXCEED);
        renameKey(
            settings, "printer/wrapping/afterLabel" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_LABEL);
        renameKey(
            settings, "printer/wrapping/afterLeftParen" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_LEFT_PAREN);
        renameKey(
            settings, "printer/wrapping/afterThrowsTypes" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_TYPES_THROWS);
        renameKey(
            settings, "printer/wrapping/afterThrowsTypesIfExceeded" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_TYPES_THROWS_EXCEED);
        renameKey(
            settings, "printer/wrapping/arrayElements" /* NOI18N */,
            ConventionKeys.LINE_WRAP_ARRAY_ELEMENTS);
        renameKey(
            settings, "printer/wrapping/beforeExtends" /* NOI18N */,
            ConventionKeys.LINE_WRAP_BEFORE_EXTENDS);
        renameKey(
            settings, "printer/wrapping/beforeImplements" /* NOI18N */,
            ConventionKeys.LINE_WRAP_BEFORE_IMPLEMENTS);
        renameKey(
            settings, "printer/wrapping/beforeOperator" /* NOI18N */,
            ConventionKeys.LINE_WRAP_BEFORE_OPERATOR);
        renameKey(
            settings, "printer/wrapping/beforeRightParen" /* NOI18N */,
            ConventionKeys.LINE_WRAP_BEFORE_RIGHT_PAREN);
        renameKey(
            settings, "printer/wrapping/beforeThrows" /* NOI18N */,
            ConventionKeys.LINE_WRAP_BEFORE_THROWS);
        renameKey(
            settings, "printer/wrapping/ifFirst" /* NOI18N */,
            ConventionKeys.LINE_WRAP_PARAMS_EXCEED);
        renameKey(
            settings, "printer/wrapping/lineLength" /* NOI18N */,
            ConventionKeys.LINE_LENGTH);
        renameKey(
            settings, "printer/wrapping/paramsMethodCall" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_CALL);
        renameKey(
            settings, "printer/wrapping/paramsMethodCallIfCall" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_CALL_IF_NESTED);
        renameKey(
            settings, "printer/wrapping/paramsMethodDef" /* NOI18N */,
            ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_DEF);
        renameKey(
            settings, "printer/wrapping/parenGrouping" /* NOI18N */,
            ConventionKeys.LINE_WRAP_PAREN_GROUPING);
        renameKey(
            settings, "printer/wrapping/use" /* NOI18N */, ConventionKeys.LINE_WRAP);
        renameKey(
            settings, "printer/comments/separator/class" /* NOI18N */,
            ConventionKeys.SEPARATOR_CLASS);
        renameKey(
            settings, "printer/comments/separator/ctor" /* NOI18N */,
            ConventionKeys.SEPARATOR_CTOR);
        renameKey(
            settings, "printer/comments/separator/instanceInit" /* NOI18N */,
            ConventionKeys.SEPARATOR_INSTANCE_INIT);
        renameKey(
            settings, "printer/comments/separator/instanceVariable" /* NOI18N */,
            ConventionKeys.SEPARATOR_INSTANCE_VAR);
        renameKey(
            settings, "printer/comments/separator/interface" /* NOI18N */,
            ConventionKeys.SEPARATOR_INTERFACE);
        renameKey(
            settings, "printer/comments/separator/method" /* NOI18N */,
            ConventionKeys.SEPARATOR_METHOD);
        renameKey(
            settings, "printer/comments/separator/staticVariableInit" /* NOI18N */,
            ConventionKeys.SEPARATOR_STATIC_VAR_INIT);
        renameKey(
            settings, "printer/comments/insertSeparator" /* NOI18N */,
            ConventionKeys.COMMENT_INSERT_SEPARATOR);
        renameKey(
            settings, "printer/comments/insertSeparatorRecursive" /* NOI18N */,
            ConventionKeys.COMMENT_INSERT_SEPARATOR_RECURSIVE);
        renameKey(
            settings, "printer/comments/formatMultiLine" /* NOI18N */,
            ConventionKeys.COMMENT_FORMAT_MULTI_LINE);
        renameKey(
            settings, "printer/comments/removeMultiLine" /* NOI18N */,
            ConventionKeys.COMMENT_REMOVE_MULTI_LINE);
        renameKey(
            settings, "printer/comments/removeSingleLine" /* NOI18N */,
            ConventionKeys.COMMENT_REMOVE_SINGLE_LINE);
        renameKey(
            settings, "printer/comments/javadoc/remove" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_REMOVE);
        renameKey(
            settings, "printer/comments/javadoc/addClass" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_CLASS_MASK);
        renameKey(
            settings, "printer/comments/javadoc/addCtor" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_CTOR_MASK);
        renameKey(
            settings, "printer/comments/javadoc/addField" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_VARIABLE_MASK);
        renameKey(
            settings, "printer/comments/javadoc/addMethod" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_METHOD_MASK);
        renameKey(
            settings, "printer/comments/javadoc/templates/classes" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CLASS);
        renameKey(
            settings,
            "printer/comments/javadoc/templates/constructors/bottom" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_BOTTOM);
        renameKey(
            settings,
            "printer/comments/javadoc/templates/constructors/exception" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_EXCEPTION);
        renameKey(
            settings, "printer/comments/javadoc/templates/constructors/param" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_PARAM);
        renameKey(
            settings, "printer/comments/javadoc/templates/constructors/top" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_CTOR_TOP);
        renameKey(
            settings, "printer/comments/javadoc/templates/interface" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_INTERFACE);
        renameKey(
            settings, "printer/comments/javadoc/templates/methods/bottom" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_BOTTOM);
        renameKey(
            settings, "printer/comments/javadoc/templates/methods/exception" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_EXCEPTION);
        renameKey(
            settings, "printer/comments/javadoc/templates/methods/param" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM);
        renameKey(
            settings, "printer/comments/javadoc/templates/methods/return" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_RETURN);
        renameKey(
            settings, "printer/comments/javadoc/templates/methods/top" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_METHOD_TOP);
        renameKey(
            settings, "printer/comments/javadoc/templates/variables" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_TEMPLATE_VARIABLE);
        renameKey(
            settings, "printer/comments/javadoc/checkInnerClass" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_INNER_CLASS);
        renameKey(
            settings, "printer/comments/javadoc/checkTags" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_CHECK_TAGS);
        renameKey(
            settings, "printer/comments/javadoc/checkThrowsTags" /* NOI18N */,
            ConventionKeys.COMMENT_JAVADOC_CHECK_TAGS_THROWS);

        renameKey(
            settings, "printer/sorting/class" /* NOI18N */, ConventionKeys.SORT_CLASS);
        renameKey(
            settings, "printer/sorting/constructor" /* NOI18N */, ConventionKeys.SORT_CTOR);
        renameKey(
            settings, "printer/sorting/interface" /* NOI18N */,
            ConventionKeys.SORT_INTERFACE);
        renameKey(
            settings, "printer/sorting/method" /* NOI18N */, ConventionKeys.SORT_METHOD);
        renameKey(
            settings, "printer/sorting/modifiers/use" /* NOI18N */,
            ConventionKeys.SORT_MODIFIERS);
        renameKey(
            settings, "printer/sorting/order" /* NOI18N */, ConventionKeys.SORT_ORDER);
        renameKey(
            settings, "printer/sorting/orderModifiers" /* NOI18N */,
            ConventionKeys.SORT_ORDER_MODIFIERS);
        renameKey(settings, "printer/sorting/use" /* NOI18N */, ConventionKeys.SORT);
        renameKey(
            settings, "printer/sorting/variable" /* NOI18N */,
            ConventionKeys.SORT_VARIABLE);

        renameKey(
            settings, "printer/alignment/ParamsMethodDef" /* NOI18N */,
            ConventionKeys.ALIGN_PARAMS_METHOD_DEF);
        renameKey(
            settings, "printer/alignment/ternaryExpresssion" /* NOI18N */,
            ConventionKeys.ALIGN_TERNARY_EXPRESSION);
        renameKey(
            settings, "printer/alignment/ternaryOperator" /* NOI18N */,
            ConventionKeys.ALIGN_TERNARY_OPERATOR);
        renameKey(
            settings, "printer/alignment/ternaryValue" /* NOI18N */,
            ConventionKeys.ALIGN_TERNARY_VALUES);
        renameKey(
            settings, "printer/alignment/varAssigns" /* NOI18N */,
            ConventionKeys.ALIGN_VAR_ASSIGNS);
        renameKey(
            settings, "printer/alignment/varIdents" /* NOI18N */,
            ConventionKeys.ALIGN_VAR_IDENTS);
        renameKey(
            settings, "printer/alignment/methodCallChains" /* NOI18N */,
            ConventionKeys.ALIGN_METHOD_CALL_CHAINS);

        renameKey(
            settings, "printer/blankLines/afterBlock" /* NOI18N */,
            ConventionKeys.BLANK_LINES_AFTER_BLOCK);
        renameKey(
            settings, "printer/blankLines/afterBraceLeft" /* NOI18N */,
            ConventionKeys.BLANK_LINES_AFTER_BRACE_LEFT);
        renameKey(
            settings, "printer/blankLines/afterClass" /* NOI18N */,
            ConventionKeys.BLANK_LINES_AFTER_CLASS);
        renameKey(
            settings, "printer/blankLines/afterDeclaration" /* NOI18N */,
            ConventionKeys.BLANK_LINES_AFTER_DECLARATION);
        renameKey(
            settings, "printer/blankLines/afterFooter" /* NOI18N */,
            ConventionKeys.BLANK_LINES_AFTER_FOOTER);
        renameKey(
            settings, "printer/blankLines/afterHeader" /* NOI18N */,
            ConventionKeys.BLANK_LINES_AFTER_HEADER);
        renameKey(
            settings, "printer/blankLines/afterInterface" /* NOI18N */,
            ConventionKeys.BLANK_LINES_AFTER_INTERFACE);
        renameKey(
            settings, "printer/blankLines/afterLastImport" /* NOI18N */,
            ConventionKeys.BLANK_LINES_AFTER_IMPORT);
        renameKey(
            settings, "printer/blankLines/afterMethod" /* NOI18N */,
            ConventionKeys.BLANK_LINES_AFTER_METHOD);
        renameKey(
            settings, "printer/blankLines/afterPackage" /* NOI18N */,
            ConventionKeys.BLANK_LINES_AFTER_PACKAGE);
        renameKey(
            settings, "printer/blankLines/beforeBlock" /* NOI18N */,
            ConventionKeys.BLANK_LINES_BEFORE_BLOCK);
        renameKey(
            settings, "printer/blankLines/beforeBraceRight" /* NOI18N */,
            ConventionKeys.BLANK_LINES_BEFORE_BRACE_RIGHT);
        renameKey(
            settings, "printer/blankLines/beforeCaseBlock" /* NOI18N */,
            ConventionKeys.BLANK_LINES_BEFORE_CASE_BLOCK);
        renameKey(
            settings, "printer/blankLines/beforeCommentMultiLine" /* NOI18N */,
            ConventionKeys.BLANK_LINES_BEFORE_COMMENT_MULTI_LINE);
        renameKey(
            settings, "printer/blankLines/beforeCommentSingleLine" /* NOI18N */,
            ConventionKeys.BLANK_LINES_BEFORE_COMMENT_SINGLE_LINE);
        renameKey(
            settings, "printer/blankLines/beforeControl" /* NOI18N */,
            ConventionKeys.BLANK_LINES_BEFORE_CONTROL);
        renameKey(
            settings, "printer/blankLines/beforeDeclaration" /* NOI18N */,
            ConventionKeys.BLANK_LINES_BEFORE_DECLARATION);
        renameKey(
            settings, "printer/blankLines/beforeFooter" /* NOI18N */,
            ConventionKeys.BLANK_LINES_BEFORE_FOOTER);
        renameKey(
            settings, "printer/blankLines/beforeHeader" /* NOI18N */,
            ConventionKeys.BLANK_LINES_BEFORE_HEADER);
        renameKey(
            settings, "printer/blankLines/beforeJavadoc" /* NOI18N */,
            ConventionKeys.BLANK_LINES_BEFORE_COMMENT_JAVADOC);
        renameKey(
            settings, "printer/blankLines/keepUpTo" /* NOI18N */,
            ConventionKeys.BLANK_LINES_KEEP_UP_TO);

        renameKey(
            settings, "printer/chunks/byBlankLines" /* NOI18N */,
            ConventionKeys.CHUNKS_BY_BLANK_LINES);
        renameKey(
            settings, "printer/chunks/byComments" /* NOI18N */,
            ConventionKeys.CHUNKS_BY_COMMENTS);

        renameKey(
            settings, "printer/footer/smartModeLines" /* NOI18N */,
            ConventionKeys.FOOTER_SMART_MODE_LINES);
        renameKey(
            settings, "printer/header/smartModeLines" /* NOI18N */,
            ConventionKeys.HEADER_SMART_MODE_LINES);
        settings._values.remove(
            new Convention.Key("printer/indentation/continationIfTernary"));

        renameKey(
            settings, "printer/indentation/continationIf" /* NOI18N */,
            ConventionKeys.INDENT_CONTINUATION_BLOCK);
        renameKey(
            settings, "printer/indentation/continationOperator" /* NOI18N */,
            ConventionKeys.INDENT_CONTINUATION_OPERATOR);
        renameKey(
            settings, "printer/indentation/continuation" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_CONTINUATION);
        renameKey(
            settings, "printer/indentation/braceCuddled" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_BRACE_CUDDLED);
        renameKey(
            settings, "printer/indentation/caseFromSwitch" /* NOI18N */,
            ConventionKeys.INDENT_CASE_FROM_SWITCH);
        renameKey(
            settings, "printer/indentation/commentEndline" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_COMMENT_ENDLINE);
        renameKey(
            settings, "printer/indentation/braceLeft" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_BRACE_LEFT);
        renameKey(
            settings, "printer/indentation/braceRight" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_BRACE_RIGHT);
        renameKey(
            settings, "printer/indentation/braceRightAfter" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_BRACE_RIGHT_AFTER);
        renameKey(
            settings, "printer/indentation/deep" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_DEEP);
        renameKey(
            settings, "printer/indentation/extends" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_EXTENDS);
        renameKey(
            settings, "printer/indentation/general" /* NOI18N */,
            ConventionKeys.INDENT_SIZE);
        renameKey(
            settings, "printer/indentation/leading" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_LEADING);
        renameKey(
            settings, "printer/indentation/throws" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_THROWS);
        renameKey(
            settings, "printer/indentation/implements" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_IMPLEMENTS);
        renameKey(
            settings, "printer/indentation/policyDeep" /* NOI18N */,
            ConventionKeys.INDENT_DEEP);
        renameKey(
            settings, "printer/indentation/firstColumnComments" /* NOI18N */,
            ConventionKeys.INDENT_FIRST_COLUMN_COMMENT);
        renameKey(
            settings, "printer/indentation/label" /* NOI18N */,
            ConventionKeys.INDENT_LABEL);
        renameKey(
            settings, "printer/indentation/tabs/size" /* NOI18N */,
            ConventionKeys.INDENT_SIZE_TABS);
        renameKey(
            settings, "printer/indentation/tabs/use" /* NOI18N */,
            ConventionKeys.INDENT_WITH_TABS);
        renameKey(
            settings, "printer/indentation/tabs/useOnlyLeading" /* NOI18N */,
            ConventionKeys.INDENT_WITH_TABS_ONLY_LEADING);

        String historyMethod =
            settings.get(
                ConventionKeys.HISTORY_METHOD, ConventionDefaults.HISTORY_METHOD);

        if (historyMethod.equals("History.Method [adler32]" /* NOI18N */))
        {
            settings.put(ConventionKeys.HISTORY_METHOD, "adler32" /* NOI18N */);
        }
        else if (historyMethod.equals("History.Method [crc32]" /* NOI18N */))
        {
            settings.put(ConventionKeys.HISTORY_METHOD, "crc32" /* NOI18N */);
        }
        else
        {
            settings.put(ConventionKeys.HISTORY_METHOD, "timestamp" /* NOI18N */);
        }

        String historyPolicy =
            settings.get(
                ConventionKeys.HISTORY_POLICY, ConventionDefaults.HISTORY_POLICY);

        if (historyPolicy.equals("History.Policy [file]" /* NOI18N */))
        {
            settings.put(ConventionKeys.HISTORY_POLICY, "file" /* NOI18N */);
        }
        else if (historyPolicy.equals("History.Policy [comment]" /* NOI18N */))
        {
            settings.put(ConventionKeys.HISTORY_POLICY, "comment" /* NOI18N */);
        }
        else
        {
            settings.put(ConventionKeys.HISTORY_POLICY, "disabled" /* NOI18N */);
        }

        String importPolicy =
            settings.get(ConventionKeys.IMPORT_POLICY, ConventionDefaults.IMPORT_POLICY);

        if (importPolicy.equals("ImportPolicy [expand]" /* NOI18N */))
        {
            settings.put(ConventionKeys.IMPORT_POLICY, "expand" /* NOI18N */);
        }
        else if (importPolicy.equals("ImportPolicy [collapse]" /* NOI18N */))
        {
            settings.put(ConventionKeys.IMPORT_POLICY, "collapse" /* NOI18N */);
        }
        else
        {
            settings.put(ConventionKeys.IMPORT_POLICY, "disabled" /* NOI18N */);
        }

        String sortOrderDeclarations =
            settings.get(ConventionKeys.SORT_ORDER, DeclarationType.getOrder());
        sortOrderDeclarations =
            StringHelper.replace(
                sortOrderDeclarations, "," /* NOI18N */, "|" /* NOI18N */);
        sortOrderDeclarations =
            StringHelper.replace(
                sortOrderDeclarations, "Classes" /* NOI18N */, "class" /* NOI18N */);
        sortOrderDeclarations =
            StringHelper.replace(
                sortOrderDeclarations, "Constructors" /* NOI18N */,
                "constructor" /* NOI18N */);
        sortOrderDeclarations =
            StringHelper.replace(
                sortOrderDeclarations, "Instance Initializers" /* NOI18N */,
                "initializer" /* NOI18N */);
        sortOrderDeclarations =
            StringHelper.replace(
                sortOrderDeclarations, "Instance Variables" /* NOI18N */,
                "field" /* NOI18N */);
        sortOrderDeclarations =
            StringHelper.replace(
                sortOrderDeclarations, "Interfaces" /* NOI18N */, "interface" /* NOI18N */);
        sortOrderDeclarations =
            StringHelper.replace(
                sortOrderDeclarations, "Methods" /* NOI18N */, "method" /* NOI18N */);
        sortOrderDeclarations =
            StringHelper.replace(
                sortOrderDeclarations, "Static Variables/Initializers" /* NOI18N */,
                "static" /* NOI18N */);
        settings.put(ConventionKeys.SORT_ORDER, sortOrderDeclarations);

        String sortOrderModifiers =
            settings.get(ConventionKeys.SORT_ORDER_MODIFIERS, ModifierType.getOrder());
        sortOrderModifiers =
            StringHelper.replace(sortOrderModifiers, "," /* NOI18N */, "|" /* NOI18N */);
        settings.put(ConventionKeys.SORT_ORDER_MODIFIERS, sortOrderModifiers);
    }


    /**
     * Updates the given code convention to the current format.
     *
     * @param settings code convention settings to synchronize.
     * @param version version number of the given code convention settings.
     *
     * @throws IllegalArgumentException if the <em>version</em> is greater than the
     *         current version number of this code convention.
     *
     * @since 1.0b8
     */
    private static void synchronize(
        Convention settings,
        int        version)
    {
        int curVersion = Integer.parseInt(VERSION);

        if (version > curVersion)
        {
            throw new IllegalArgumentException(
                "invalid code convention version detected, was " + version
                + ", needed <= " + curVersion);
        }
        else if (version != curVersion)
        {
            switch (version)
            {
                case -1 : // before 1.0b6
                    sync0To1(settings);

                    break;

                case 1 : // 1.0b6
                    sync1To2(settings);
                    synchronize(settings, 2);

                    break;

                case 2 : // 1.0b7
                    sync2To3(settings);
                    synchronize(settings, 3);

                    break;

                case 3 : // 1.0b8
                    sync3To4(settings);
                    synchronize(settings, 4);

                    break;

                case 4 : // 1.0b9
                    sync4To5(settings);
                    synchronize(settings, 5);

                    break;

                case 5 : // 1.0b9
                    sync5To6(settings);

                    break;
            }
            
            for (
                Iterator i = new HashMap(settings._values).keySet().iterator();
                i.hasNext();)
            {
                Convention.Key key = (Convention.Key) i.next();

                if (!ConventionKeys.isValid(key))
                {
                    /**
                     * @todo use logger
                     */

                    //System.err.println("[WARN] remove invalid key -- " + key);
                    settings._values.remove(key);
                }
            }
        }
        
        // Temporary modify one of the keys
        // TODO Update to add in the order, this requires a version change !
        String s=settings.get(ConventionKeys.SORT_ORDER,DeclarationType.getOrder());
        StringTokenizer sortOrder = new StringTokenizer(s,"|");
        int t = sortOrder.countTokens();

        int z= DeclarationType.getOrderSize();
        if (t!=z) {
            settings.put(ConventionKeys.SORT_ORDER,DeclarationType.getOrder());
        }
        
    }


    /**
     * Updates the given code convention settings to the current code convention format.
     *
     * @param settings code convention settings to synchronize against the latest
     *        version.
     *
     * @since 1.0b6
     */
    private static void synchronize(Convention settings)
    {
        synchronize(settings, settings.getInt(ConventionKeys.INTERNAL_VERSION, -1));

        INSTANCE._locale =
            new Locale(
                settings.get(ConventionKeys.LANGUAGE, ConventionDefaults.LANGUAGE),
                settings.get(ConventionKeys.COUNTRY, ConventionDefaults.COUNTRY));

        INSTANCE._values = settings._values;

        INSTANCE.put(ConventionKeys.INTERNAL_VERSION, VERSION);
    }


    /**
     * Converts the given map into an XML representation.
     *
     * @param map map to convert.
     *
     * @return root element of the resulting XML document.
     */
    private Element convertMapToXml(Map map,Document doc)
    {
        map = new java.util.TreeMap(map);

        //doc.createElement("jalopy" /* NOI18N */);
        
        Element root = doc.createElement("jalopy" /* NOI18N */);
        doc.appendChild(root);

        for (Iterator it = map.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            Key key = (Key) entry.getKey();
            Object value = entry.getValue();
            List pathList = splitPath(key.toString());
            Element go = root;

            for (int i = 0, size = pathList.size(); i < size; i++)
            {
                String elName = (String) pathList.get(i);
                NodeList children = go.getElementsByTagName(elName);
                
                Element child = null;
                
                if (children.getLength() == 0)
                {
                    child = doc.createElement(elName);
                    go.appendChild(child);
                }
                else {
                    for(int x=0;x<children.getLength();x++) {
                        child =(Element) children.item(x);
                        if (child.getParentNode() == go) {
                            break;
                        }
                        child = null;
                    }
                    if (child == null) {
                        child = doc.createElement(elName);
                        go.appendChild(child);
                    }
                }

                go = child;
            }
            go.appendChild(doc.createTextNode(value.toString()));
        }

        return root;
    }


    /**
     * Splits the given XPath fragment into several parts.
     *
     * @param strXPath XPath fragment (e.g printer/indentation/general)
     *
     * @return list with splitted parts of the given path.
     */
    private List splitPath(String strXPath)
    {
        strXPath = "/" /* NOI18N */ + strXPath;

        List result = new ArrayList();

        for (int i = 0, i_len = strXPath.length(); i < (i_len - 1); i++)
        {
            char ch = strXPath.charAt(i);

            if (ch == '/')
            {
                StringBuffer sb = new StringBuffer();

                for (int j = i + 1; j < i_len; j++)
                {
                    char varCh = strXPath.charAt(j);

                    if (varCh != '/')
                    {
                        sb.append(varCh);

                        continue;
                    }

                    break;
                }

                if (sb.length() == 0)
                {
                    continue;
                }

                result.add(new String(sb));
            }
        }

        return result;
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * A key for storing a value in a code convention.
     *
     * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
     * @version $Revision: 1.6 $
     *
     * @see de.hunsicker.jalopy.storage.ConventionKeys
     * @since 1.0b9
     */
    public static class Key
        implements Serializable, Comparable
    {
        /** Use serialVersionUID for interoperability. */
        static final long serialVersionUID = -7320495354745545260L;

        /** Our name. */
        private transient String _name;

        /** Pre-computed hash code value. */
        private transient int _hashCode;

        /**
         * Creates a new Key object.
         *
         * @param name the name of the key.
         */
        Key(String name)
        {
            _name = name.intern();
            _hashCode = _name.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(Object o)
        {
            if (o instanceof Convention.Key)
            {
                return (_name.compareTo(((Convention.Key) o)._name));
            }

            return 0;
        }


        /**
         * {@inheritDoc}
         */
        public boolean equals(Object o)
        {
            // note that we only have to override the equals method because we want to be
            // able to compare objects between several virtual machine instances (we use
            // Java object serialization as our persistence mechanism)
            if (this == o)
            {
                return true;
            }

            return _name == ((Key) o)._name;
        }


        /**
         * {@inheritDoc}
         */
        public int hashCode()
        {
            return _hashCode;
        }


        /**
         * {@inheritDoc}
         */
        public String toString()
        {
            return _name;
        }


        /**
         * Deserializes a key from the given stream.
         *
         * @param in stream to read the object from.
         *
         * @throws IOException if an I/O error occured.
         * @throws ClassNotFoundException if a class that should be read could not be
         *         found (Should never happen actually).
         */
        private void readObject(ObjectInputStream in)
          throws IOException, ClassNotFoundException
        {
            in.defaultReadObject();

            // that's why we have to provide custom serialization: we want to be
            // able to compare two keys by identity
            _name = ((String) in.readObject()).intern();
            _hashCode = in.readInt();
        }


        /**
         * Serializes this instance.
         *
         * @param out stream to write the object to.
         *
         * @throws IOException if an I/O error occured.
         *
         * @serialData Emits the name of the key, followed by its pre-computed hash code
         *             value.
         */
        private void writeObject(ObjectOutputStream out)
          throws IOException
        {
            out.defaultWriteObject();
            out.writeObject(_name);
            out.writeInt(_hashCode);
        }
    }
}
