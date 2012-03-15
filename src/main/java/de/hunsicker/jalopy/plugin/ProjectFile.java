/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.plugin;

import java.io.File;


/**
 * Represents a Java source file that is part of a project.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public interface ProjectFile
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns an editor view to modify the file. One may check if the file is actually
     * opened in the editor prior to call this method:
     * <pre class="snippet">
     * if (projectFile.isOpened())
     * {
     *     return projectFile.getEditor();
     * }
     * else
     * {
     *     // do whatever you want ...
     * }
     * </pre>
     *
     * @return the editor to modify the contents of the file. Returns <code>null</code>
     *         if the file is currently closed.
     *
     * @see #isOpened
     */
    public Editor getEditor();


    /**
     * Returns the encoding used to read and write this file.
     *
     * @return A Java encoding name. May be <code>null</code> to indicate the platform's
     *         default encoding.
     */
    public String getEncoding();


    /**
     * Returns the underlying physical file. Note that if the application uses virtual
     * files this method should create an intermediate representation but never return
     * <code>null</code>.
     *
     * @return the physical file.
     */
    public File getFile();


    /**
     * Returns the name of the file.
     *
     * @return the file name.
     */
    public String getName();


    /**
     * Determines whether the file is currently opened. That means an editor view exists.
     *
     * @return <code>true</code> if the file is currently opened, i.e. has an editor to
     *         modify its contents.
     *
     * @see #getEditor
     */
    public boolean isOpened();


    /**
     * Returns the project this file is attached to.
     *
     * @return the containing project.
     */
    public Project getProject();


    /**
     * Determines whether the file can be changed.
     *
     * @return <code>true</code> if the file can be changed by the user.
     */
    public boolean isReadOnly();
}
