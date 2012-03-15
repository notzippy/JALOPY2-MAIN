/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.plugin;

import java.util.Collection;


/**
 * Provides access to the Java source files that make up a user's project.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.2 $
 */
public interface Project
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the currently active Java source file. <em>Active</em> means that the file
     * has an {@link Editor} opened and this editor has the focus.
     *
     * @return currently active file. Returns <code>null</code> if there is no active
     *         file or if the active file is no Java source file.
     */
    public ProjectFile getActiveFile();


    /**
     * Returns all Java source files that make up a project.
     *
     * @return the files of the project (of type {@link
     *         de.hunsicker.jalopy.plugin.ProjectFile &lt;ProjectFile&gt;}). Returns an
     *         empty list if no files exist.
     */
    public Collection getAllFiles();


    /**
     * Returns the Java source files that are currently opened (i.e. <code>{@link
     * ProjectFile#getEditor()} != null</code>).
     *
     * @return currently opened files (of type {@link
     *         de.hunsicker.jalopy.plugin.ProjectFile &lt;ProjectFile&gt;}). Returns an
     *         empty list if no files are opened.
     */
    public Collection getOpenedFiles();


    /**
     * Returns the Java source files that are currently selected.
     *
     * @return currently selected files (of type {@link
     *         de.hunsicker.jalopy.plugin.ProjectFile &lt;ProjectFile&gt;}). Returns an
     *         empty list if no files are selected.
     */
    public Collection getSelectedFiles();
}
