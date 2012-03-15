/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.language;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.hunsicker.io.DirectoryScanner;
import de.hunsicker.io.ExtensionFilter;
import de.hunsicker.io.IoHelper;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.Loggers;
import de.hunsicker.util.ChainingRuntimeException;
import de.hunsicker.util.StringHelper;

import org.apache.log4j.Level;


/**
 * Stores type names for Java packages. This information is needed in order to be able to
 * switch between <em>single-type-import declarations</em> and <em>type-import-on-demand
 * declarations</em>.
 * 
 * <p>
 * This class is thread-safe.
 * </p>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public class ClassRepository
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The file extension for repository entry files. */
    static final String EXT_REPOSITORY = ".repository";

    /** The empty ClassRepositoryEntry.Info array. */
    private static final ClassRepositoryEntry.Info[] EMPTY_INFO_ARRAY =
        new ClassRepositoryEntry.Info[0];
    private static final String EMPTY_STRING = "".intern() /* NOI18N */;

    /** The empty string array. */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String EXT_CLASS = ".class" /* NOI18N */;
    private static final String EXT_JAR = ".jar" /* NOI18N */;
    private static final String EXT_ZIP = ".zip" /* NOI18N */;

    /** Sole instance. */
    private static final ClassRepository INSTANCE = new ClassRepository();

    //~ Instance variables ---------------------------------------------------------------

    /** The current code convention. */
    private Convention _settings;

    /** Our working directory. */
    private File _repositoryDirectory;

    /** Holds information about the currently active repository entries. */
    private List _infos = new ArrayList(); // List of <ClassRepositoryEntry.Info>

    /** The current contents. */
    private String[] _content = EMPTY_STRING_ARRAY;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ClassRepository object.
     *
     * @throws RuntimeException if the repository directory does not exist and could not
     *         be created.
     */
    private ClassRepository()
    {
        _settings = Convention.getInstance();

        File directory = getWorkingDir();

        if (!directory.exists())
        {
            if (!directory.mkdirs())
            {
                throw new RuntimeException(
                    "could not create the repository directory -- " + directory);
            }
        }
        else
        {
            preload();
        }
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the sole instance of this class.
     *
     * @return class instance.
     */
    public static ClassRepository getInstance()
    {
        return INSTANCE;
    }


    /**
     * Returns the current contents of the repository.
     *
     * @return active contents. The repository can be empty in which case an empty array
     *         will be returned. This method never returns <code>null</code>.
     */
    public synchronized String[] getContent()
    {
        return _content;
    }


    /**
     * Indicates whether the repository is currently empty.
     *
     * @return <code>true</code> if the repository is currently empty.
     */
    public synchronized boolean isEmpty()
    {
        return _content.length == 0;
    }


    /**
     * Returns the repository info (info about all registered entries).
     *
     * @return repository information.
     */
    public synchronized ClassRepositoryEntry.Info[] getInfo()
    {
        List infos = new ArrayList(_infos);

        return (ClassRepositoryEntry.Info[]) infos.toArray(EMPTY_INFO_ARRAY);
    }


    /**
     * Returns the current size of the repository.
     *
     * @return the size of the repository.
     *
     * @since 1.0b7
     */
    public int getSize()
    {
        return _content.length;
    }


    /**
     * Creates a repository entry for the given repository info.
     *
     * @param info the information to create an entry upon.
     *
     * @return the generated repository entry. Returns <code>null</code> if the location
     *         info is no package root.
     *
     * @throws IOException if an I/O error occurred.
     * @throws IllegalArgumentException if the location specified in the info object does
     *         not denote an existing file or directory.
     */
    public static ClassRepositoryEntry createEntry(ClassRepositoryEntry.Info info)
      throws IOException
    {
        File location = info.getLocation();

        if (!location.exists())
        {
            throw new IllegalArgumentException(
                " no valid file or directory -- " + location);
        }

        Set types = new HashSet(20);

        if (location.isDirectory())
        {
            DirectoryScanner scanner = new DirectoryScanner(location);
            scanner.addFilter(new ExtensionFilter(EXT_CLASS));
            scanner.run();

            int length = location.toString().length() + 1;
            File[] files = scanner.getFiles();
            boolean verify = true;

            for (int i = 0, size = files.length; i < size; i++)
            {
                String path = files[i].toString().substring(length);
                path = path.replace(File.separatorChar, '.');

                if (!createEntryImpl(location, path, types, verify))
                {
                    // verification failed, location is no package root
                    throw new IOException("no package root directory -- " + location);
                }
                verify = false;
            }
        }
        else
        {
            if (
                location.getName().endsWith(EXT_JAR)
                || location.getName().endsWith(EXT_ZIP))
            {
                JarFile archive = new JarFile(location);

                for (Enumeration enumerator = archive.entries(); enumerator.hasMoreElements();)
                {
                    JarEntry entry = (JarEntry) enumerator.nextElement();
                    String path = entry.getName();

                    if (path.endsWith(EXT_CLASS))
                    {
                        path = path.replace('/', '.');
                        createEntryImpl(location, path, types, false);
                    }
                }
            }
            else
            {
                throw new IllegalArgumentException(
                    "no valid Java archive -- " + location);
            }
        }

        return new ClassRepositoryEntry(info, types);
    }


    /**
     * Returns the repository info for the given location.
     *
     * @param location location to return the repository info for.
     *
     * @return the corresponding repository info or <code>null</code> if no repository
     *         file for the given location exists (i.e. the given location was not
     *         registered yet).
     */
    public synchronized ClassRepositoryEntry.Info get(File location)
    {
        for (int i = 0, size = _infos.size(); i < size; i++)
        {
            ClassRepositoryEntry.Info info = (ClassRepositoryEntry.Info) _infos.get(i);

            if (info.getLocation().equals(location))
            {
                return info;
            }
        }

        return null;
    }


    /**
     * Loads the contents of the given location into memory.
     * 
     * <p>
     * If the given location is already registered, both persistent storage and memory
     * will be updated. If the location denotes a Java archive (JAR) an update will only
     * be performed if the archive actually changed.
     * </p>
     * 
     * <p>
     * Directories will always be updated as there is no easy way to detect changes in
     * such a case. You should perform logic to avoid unnecessary loads in the Plug-in
     * code.
     * </p>
     * 
     * <p>
     * If no entry exits for the location, a new entry will be generated and its contents
     * loaded into memory.
     * </p>
     *
     * @param location location to add.
     *
     * @throws IOException if an I/O error occured.
     */
    public synchronized void load(File location)
      throws IOException
    {
        ClassRepositoryEntry entry = loadEntry(location);

        if (entry == null)
        {
            return;
        }

        Set data = entry.getData();
        Set temp = new HashSet(_content.length + data.size());
        temp.addAll(Arrays.asList(_content));
        temp.addAll(data);
        _content = (String[]) temp.toArray(EMPTY_STRING_ARRAY);
        Arrays.sort(_content);

        if (Loggers.IO.isDebugEnabled())
        {
            Loggers.IO.debug(
                "ClassRepository: Loaded " + data.size() + " classes from " + location);
        }
    }


    /**
     * Loads the contents of the given locations into memory.
     *
     * @param locations locations to add (of type {@link java.io.File &lt;File&gt;}).
     *
     * @throws IOException if an I/O error occurred.
     */
    public synchronized void loadAll(List locations)
      throws IOException
    {
        if (locations == null)
        {
            return;
        }

        Set data = new HashSet(1000);

        for (Iterator i = locations.iterator(); i.hasNext();)
        {
            File location = (File) i.next();
            ClassRepositoryEntry entry = loadEntry(location);

            if (entry != null)
            {
                data.addAll(entry.getData());

                if (Loggers.IO.isDebugEnabled())
                {
                    Loggers.IO.debug(
                        "ClassRepository: Loaded " + data.size() + " classes from "
                        + location);
                }
            }
        }

        if (!data.isEmpty())
        {
            data.addAll(Arrays.asList(_content));
            _content = (String[]) data.toArray(EMPTY_STRING_ARRAY);
            Arrays.sort(_content);
        }
    }


    /**
     * Loads the information from the given repository entry file.
     *
     * @param file the repository entry file.
     *
     * @return the entry info.
     *
     * @throws IOException if an I/O error occurred.
     */
    public synchronized ClassRepositoryEntry.Info loadInfo(File file)
      throws IOException
    {
        return (ClassRepositoryEntry.Info) IoHelper.deserialize(file);
    }


    /**
     * Unloads the given file from memory. If the given location was not registered, the
     * call we be safely ignored.
     *
     * @param location location to unload.
     *
     * @throws IOException if an I/O error occurred.
     *
     * @see ClassRepositoryEntry.Info#getLocation
     */
    public synchronized void unload(File location)
      throws IOException
    {
        ClassRepositoryEntry.Info template = new ClassRepositoryEntry.Info(location);

        if (!_infos.contains(template))
        {
            return;
        }

        ClassRepositoryEntry entry = createEntry(template);
        Set data = entry.getData();
        Set temp = new HashSet(Arrays.asList(_content));
        temp.removeAll(data);
        _content = (String[]) temp.toArray(EMPTY_STRING_ARRAY);
        Arrays.sort(_content);

        // update the info
        ClassRepositoryEntry.Info info =
            (ClassRepositoryEntry.Info) _infos.get(_infos.indexOf(template));
        info.setLoaded(false);

        if (Loggers.IO.isDebugEnabled())
        {
            Loggers.IO.debug(
                "ClassRepository: Unloaded " + data.size() + " entries for " + location);
        }
    }


    /**
     * Unloads the given locations from memory. If one of the given locations was not
     * registered, it will be ignored.
     *
     * @param locations locations to unload (of type {@link java.io.File &lt;File&gt;}).
     *
     * @throws IOException if an I/O error occurred.
     *
     * @see ClassRepositoryEntry.Info#getLocation
     */
    public synchronized void unloadAll(List locations)
      throws IOException
    {
        Set data = new HashSet(1000);

        for (Iterator i = locations.iterator(); i.hasNext();)
        {
            File location = (File) i.next();

            if (location.exists())
            {
                ClassRepositoryEntry entry = null;
                ClassRepositoryEntry.Info template = null;

                try
                {
                    template = new ClassRepositoryEntry.Info(location);

                    if (!_infos.contains(template))
                    {
                        continue;
                    }

                    entry = createEntry(template);
                    data.addAll(entry.getData());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();

                    continue;
                }

                // update the info
                ClassRepositoryEntry.Info info =
                    (ClassRepositoryEntry.Info) _infos.get(_infos.indexOf(template));
                info.setLoaded(false);

                if (Loggers.IO.isDebugEnabled())
                {
                    Loggers.IO.debug(
                        "ClassRepository: Unloaded " + entry.getData().size()
                        + " entries for " + location);
                }
            }
        }

        if (!data.isEmpty())
        {
            Set temp = new HashSet(Arrays.asList(_content));
            temp.removeAll(data);
            _content = (String[]) temp.toArray(EMPTY_STRING_ARRAY);
            Arrays.sort(_content);
        }
    }


    /**
     * Returns the current working directory where all repository files are stored.
     *
     * @return working directory.
     */
    private File getWorkingDir()
    {
        if (_repositoryDirectory == null)
        {
            /**
             * @todo make this user configurable
             */
            _repositoryDirectory =
                new File(
                    _settings.get(
                        ConventionKeys.CLASS_REPOSITORY_DIRECTORY,
                        Convention.getRepositoryDirectory().getAbsolutePath()));
        }

        return _repositoryDirectory;
    }


    /**
     * Creates a new entry.
     *
     * @param location location of the package to add.
     * @param path path to add.
     * @param types the package types.
     * @param verify if <code>true</code> the package root check will be performed.
     *
     * @return if <code>verify == true</code> returns <code>true</code> when the given
     *         location could be verified, <code>false</code> if the given location is
     *         no package root; otherwise always returns <code>true</code>.
     */
    private static boolean createEntryImpl(
        File      location,
        String    path,
        final Set types,
        boolean   verify)
    {
        // strip extension
        path = path.substring(0, path.lastIndexOf('.'));

        int pos = path.lastIndexOf('$');

        // is this an inner class?
        if (pos > -1)
        {
            String className = path.substring(pos + 1);

            // skip anonymous inner classes
            if (StringHelper.isNumber(className))
            {
                return true;
            }

            path = path.replace('$', '.');
        }
        else if (verify)
        {
            try
            {
                // we setup a classloader with the given location and check
                // whether the location is indeed a package root
                URL[] url = new URL[] { location.toURL() };
                ClassLoader loader =
                    new URLClassLoader(url, ClassRepository.class.getClassLoader());
                loader.loadClass(path.replace(File.separatorChar, '.'));
            }
            catch (ClassNotFoundException ex)
            {
                Object[] args = { path, location };
                Loggers.IO.l7dlog(Level.WARN, "REPOSITORY_NOT_PACKAGE_ROOT", args, null);

                return false;
            }
            catch (Throwable ex)
            {
                return false;
            }
        }

        String typeName = StringHelper.getClassName(path);

        // HACK skip obfuscated classes
        // this is necessary to make our import declaration expanding working
        // as it could easily be, that an identifier reported by the parser
        // would be wrongly taken as a type name:
        //
        //      Database d = new Database();
        //      d.shutdown();
        //      ^
        // 'd' would be reported as an identifier which is perfect but
        // lead to wrong results as there could be a class 'd.class' for
        // obfuscated librarys
        if ((typeName.length() == 1) && Character.isLowerCase(typeName.charAt(0)))
        {
            return true;
        }

        String packageName = StringHelper.getPackageName(path);

        // we place this marker in front of every subpackage so we know
        // where a package starts (purely for the searching facility in
        // ImportTransformation.java)
        if (!EMPTY_STRING.equals(packageName))
        {
            types.add(packageName + '#');
        }

        types.add(path);

        return true;
    }


    /**
     * Loads the entry from the given location.
     *
     * @param location location where the entry is stored.
     *
     * @return the entry. Returns <code>null</code> if the entry is up-to-date.
     *
     * @throws IOException if the entry could not be loaded.
     */
    private ClassRepositoryEntry loadEntry(File location)
      throws IOException
    {
        // either the user has deleted/renamed the .jar since the first
        // initialization or the project was newly created
        if (!location.exists())
        {
            if (
                location.getName().endsWith(".jar")
                || location.getName().endsWith(".zip"))
            {
                // for .jars this always means an error
                throw new IOException("File not found -- " + location);
            }
            
            if (!location.mkdirs())
            {
                throw new IOException("Directory not found -- " + location);
            }
            
            if (Loggers.IO.isDebugEnabled())
            {
                Loggers.IO.debug(
                    "ClassRepository: Created new directory: " + location);
            }
        }

        ClassRepositoryEntry.Info template = new ClassRepositoryEntry.Info(location);
        ClassRepositoryEntry.Info info = template;
        ClassRepositoryEntry entry = null;

        // location already registered
        if (_infos.contains(template))
        {
            info = (ClassRepositoryEntry.Info) _infos.get(_infos.indexOf(template));

            if (Loggers.IO.isDebugEnabled())
            {
                Loggers.IO.debug("ClassRepository: Already registered: " + info);
            }

            // already loaded in memory
            if (info.isLoaded())
            {
                if (Loggers.IO.isDebugEnabled())
                {
                    Loggers.IO.debug("ClassRepository: Alread loaded: " + info);
                }

                // always rescan directories as the contents might have changed
                if (location.isDirectory())
                {
                    if (Loggers.IO.isDebugEnabled())
                    {
                        Loggers.IO.debug("ClassRepository: Create new: " + info);
                        Loggers.IO.debug("ClassRepository: Save to disk: " + info);
                    }

                    entry = createEntry(info);

                    saveToDisk(entry);
                }
                else
                {
                    if (location.lastModified() == info.getLocation().lastModified())
                    {
                        if (Loggers.IO.isDebugEnabled())
                        {
                            Loggers.IO.debug("ClassRepository: Up to date: " + info);
                        }

                        // no changes detected, nothing to do
                        return entry;
                    }

                    if (Loggers.IO.isDebugEnabled())
                    {
                        Loggers.IO.debug("ClassRepository: Create new: " + info);
                        Loggers.IO.debug("ClassRepository: Save to disk: " + info);
                    }

                    entry = createEntry(info);

                    saveToDisk(entry);
                }
            }
            else
            {
                // always rescan directories as the contents might have changed
                if (location.isDirectory())
                {
                    if (Loggers.IO.isDebugEnabled())
                    {
                        Loggers.IO.debug("ClassRepository: Create new: " + info);
                        Loggers.IO.debug("ClassRepository: Save to disk: " + info);
                    }

                    entry = createEntry(info);

                    saveToDisk(entry);
                }
                else
                {
                    if (Loggers.IO.isDebugEnabled())
                    {
                        Loggers.IO.debug("ClassRepository: Load from disk: " + info);
                    }

                    // load from persistent storage
                    entry =
                        loadFromDisk(
                            new File(
                                getWorkingDir() + File.separator + info.getFilename()));
                }
            }
        }
        else
        {
            _infos.add(template);

            if (Loggers.IO.isDebugEnabled())
            {
                Loggers.IO.debug("ClassRepository: Create new: " + info);
                Loggers.IO.debug("ClassRepository: Save to disk: " + info);
            }

            entry = createEntry(template);

            saveToDisk(entry);
        }

        info = (ClassRepositoryEntry.Info) _infos.get(_infos.indexOf(template));
        info.setLoaded(true);

        return entry;
    }


    /**
     * Loads the entry stored in the given file.
     *
     * @param file the file where the entry is stored in.
     *
     * @return the loaded entry.
     *
     * @throws IOException if an I/O error occured.
     * @throws ChainingRuntimeException If an error occurs
     */
    private ClassRepositoryEntry loadFromDisk(File file)
      throws IOException
    {
        ObjectInputStream in =
            new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

        try
        {
            ClassRepositoryEntry entry = new ClassRepositoryEntry();
            entry.info = (ClassRepositoryEntry.Info) in.readObject();
            entry.data = (Set) in.readObject();
            in.close(); // so we can update the timestamp

            // we use this timestamp as the last access mark
            file.setLastModified(System.currentTimeMillis());

            return entry;
        }
        catch (ClassNotFoundException ex)
        {
            throw new ChainingRuntimeException(ex);
        }
        finally
        {
            in.close();
        }
    }


    /**
     * Loads all existing repository infos from persisent storage.
     */
    private void preload()
    {
        try
        {
            File[] files = getWorkingDir().listFiles(new ExtensionFilter(EXT_REPOSITORY));
            long now = System.currentTimeMillis();

            /**
             * @todo make user configurable
             */
            long delta = 1000 * 60 * 60 * 24 * 15;

            for (int i = 0; i < files.length; i++)
            {
                ClassRepositoryEntry.Info info = loadInfo(files[i]);
                _infos.add(info);

                if ((files[i].lastModified() + delta) < now)
                {
                    // remove repository file not used for a long time
                    if (!files[i].delete())
                    {
                        Object[] args = { files[i] };
                        Loggers.IO.l7dlog(
                            Level.INFO, "IMPORT_DELETE_UNUSED_ERR", args, null);
                    }
                }
            }
        }
        catch (Throwable ex)
        {
            _infos.clear();
            Loggers.IO.warn(
                "Error preloading the class repository, no import optimizaton available",
                ex);
        }
    }


    /**
     * Saves the given entry.
     *
     * @param entry the entry to save.
     *
     * @throws IOException if an I/O error occured.
     */
    private void saveToDisk(ClassRepositoryEntry entry)
      throws IOException
    {
        ClassRepositoryEntry.Info info = entry.getInfo();
        ObjectOutputStream out =
            new ObjectOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(
                        getWorkingDir() + File.separator + entry.getInfo().getFilename())));

        try
        {
            // first the metadata
            out.writeObject(info);
            out.writeObject(entry.getData());
        }
        finally
        {
            out.close();
        }
    }
}
