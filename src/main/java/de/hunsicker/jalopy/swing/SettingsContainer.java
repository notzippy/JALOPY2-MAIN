/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.hunsicker.io.IoHelper;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.swing.util.PopupSupport;
import de.hunsicker.util.Helper;
import de.hunsicker.util.ResourceBundleFactory;


/**
 * The main container that provides a tree view on the left where users may choose the
 * different settings pages that are then displayed.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public final class SettingsContainer
    extends JPanel
{
    //~ Static variables/initializers ----------------------------------------------------

    /** The name for ResourceBundle lookup. */
    private static final String BUNDLE_NAME =
        "de.hunsicker.jalopy.swing.Bundle" /* NOI18N */;
    private static final String EMPTY_STRING = "" /* NOI18N */.intern();
    private static final String FILENAME_PAGE = "page.dat" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    /** Holds the currently displayed settings page. */
    AbstractSettingsPage _curSettingsPanel;

    /** The code convention settings. */
    // TODO private Convention _settings;

    /** Used to display the title of the current settings page. */
    JLabel _titleLabel;

    /** Panel to add the active settings page. */
    JPanel _settingsPanel;

    /** The used tree to select the different settings pages. */
    JTree _tree;

    /** Holds all property pages that have been displayed. */
    Map _panels = new HashMap(10); // Map of <String>:<AbstractSettingsPage>

    /** Associates preview file names and their contents. */
    private Map _previews = new HashMap(); // Map of <String>:<String>

    /** Helper class to add popup menu support for text components. */
    private PopupSupport _popupSupport;

    /** Displays a preview. */
    private PreviewFrame _previewFrame;

    /** Was this object destroyed? */
    private boolean _disposed;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new SettingsContainer object.
     *
     * @param previewFrame frame to display a preview file
     */
    public SettingsContainer(PreviewFrame previewFrame)
    {
        _previewFrame = previewFrame;
        initialize();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Clears the panel cache.
     */
    public void clearCache()
    {
        _panels.clear();
    }


    /**
     * Releases allocated resources.
     */
    public void dispose()
    {
        if (!_disposed)
        {
            trackPanel();
            clearCache();

            _popupSupport.setEnabled(false);
            _popupSupport = null;
            _disposed = true;
        }
    }


    /**
     * Stores the settings of all contained pages.
     *
     * @throws ValidationException if invalid settings were found.
     */
    public void updateSettings()
      throws ValidationException
    {
        for (Iterator i = _panels.values().iterator(); i.hasNext();)
        {
            AbstractSettingsPage page = (AbstractSettingsPage) i.next();
            page.validateSettings();
            page.updateSettings();
        }
    }


    /**
     * Creates the different nodes to choose the settings pages.
     *
     * @return the root node of the created node tree.
     */
    protected DefaultMutableTreeNode createNodes()
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT" /* NOI18N */);
        DefaultMutableTreeNode general =
            new SettingsNode(
                new SettingsNodeInfo(
                    "general" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_GENERAL" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.GeneralSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode printer =
            new SettingsNode(
                new SettingsNodeInfo(
                    "printer" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_PRINTER" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.DummyPanel" /* NOI18N */));
        DefaultMutableTreeNode braces =
            new SettingsNode(
                new SettingsNodeInfo(
                    "braces" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_BRACES" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.BracesSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode header =
            new SettingsNode(
                new SettingsNodeInfo(
                    "header" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_HEADER" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.HeaderSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode footer =
            new SettingsNode(
                new SettingsNodeInfo(
                    "footer" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_FOOTER" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.FooterSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode imports =
            new SettingsNode(
                new SettingsNodeInfo(
                    "imports" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_IMPORTS" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.ImportsSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode indentation =
            new SettingsNode(
                new SettingsNodeInfo(
                    "indentation" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_INDENTATION" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.IndentationSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode separation =
            new SettingsNode(
                new SettingsNodeInfo(
                    "separation" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_BLANK_LINES" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.BlankLinesSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode javadoc =
            new SettingsNode(
                new SettingsNodeInfo(
                    "javadoc" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_JAVADOC" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.JavadocSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode messages =
            new SettingsNode(
                new SettingsNodeInfo(
                    "messages" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_MESSAGES" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.MessagesSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode whitespace =
            new SettingsNode(
                new SettingsNodeInfo(
                    "whitespace" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_WHITESPACE" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.WhitespaceSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode wrapping =
            new SettingsNode(
                new SettingsNodeInfo(
                    "wrapping" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_WRAPPING" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.WrappingSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode comments =
            new SettingsNode(
                new SettingsNodeInfo(
                    "comments" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_COMMENTS" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.CommentsSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode sort =
            new SettingsNode(
                new SettingsNodeInfo(
                    "sorting" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_SORTING" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.SortingSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode misc =
            new SettingsNode(
                new SettingsNodeInfo(
                    "misc" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_MISC" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.MiscSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode environment =
            new SettingsNode(
                new SettingsNodeInfo(
                    "environment" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_ENVIRONMENT" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.EnvironmentSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode inspector =
            new SettingsNode(
                new SettingsNodeInfo(
                    "inspector" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_INSPECTOR" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.CodeInspectorSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode naming =
            new SettingsNode(
                new SettingsNodeInfo(
                    "naming" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_NAMING" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.NamingSettingsPage" /* NOI18N */));
        DefaultMutableTreeNode projects =
            new SettingsNode(
                new SettingsNodeInfo(
                    "projects" /* NOI18N */,
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "LBL_PROJECTS" /* NOI18N */),
                    "de.hunsicker.jalopy.swing.ProjectSettingsPage" /* NOI18N */));

        root.add(general);
        root.add(projects);

        root.add(printer);
        printer.add(braces);
        printer.add(whitespace);
        printer.add(indentation);
        printer.add(wrapping);
        printer.add(separation);
        printer.add(comments);
        printer.add(imports);
        printer.add(environment);
        printer.add(javadoc);
        printer.add(header);
        printer.add(footer);
        printer.add(sort);
        printer.add(misc);

        root.add(inspector);
        inspector.add(naming);

        root.add(messages);

        return root;
    }


    AbstractSettingsPage getCurrentPage()
    {
        return _curSettingsPanel;
    }


    /**
     * Returns a handle to the preview frame.
     *
     * @return the preview frame. Returns <code>null</code> if no preview is available.
     *
     * @since 1.0b8
     */
    PreviewFrame getPreview()
    {
        return _previewFrame;
    }


    void displayPreview()
    {
        displayPreview((SettingsNode) _tree.getLastSelectedPathComponent());
    }


    void displayPreview(SettingsNode node)
    {
        if ((_previewFrame != null) && (node != null))
        {
            if (node.toString().equalsIgnoreCase("general")) {
                _previewFrame.setCurrentPage(_curSettingsPanel);

                String text = loadPreview("general");

                if (text != null)
                {
                    _previewFrame.setText(text);

                    if (!_previewFrame.isVisible())
                    {
                        _previewFrame.setVisible(true);
                    }
                }
                else
                {
                    _previewFrame.setText(EMPTY_STRING);
                }
            }
            else 
            if (!((DefaultMutableTreeNode) node.getParent()).isRoot())
            {
                // only if the user did not choose to display a custom file, we
                // display a different file for every settings page
                if (_previewFrame.customFile)
                {
                    if (!_previewFrame.isVisible())
                    {
                        _previewFrame.setVisible(true);
                    }
                }
                else
                {
                    SettingsNode parent = (SettingsNode) node.getParent();

                    if (parent.getInfo().key.equals("printer" /* NOI18N */))
                    {
                        _previewFrame.setCurrentPage(_curSettingsPanel);

                        String text = loadPreview(_curSettingsPanel.getPreviewFileName());

                        if (text != null)
                        {
                            _previewFrame.setText(text);

                            if (!_previewFrame.isVisible())
                            {
                                _previewFrame.setVisible(true);
                            }
                        }
                        else
                        {
                            _previewFrame.setText(EMPTY_STRING);
                        }
                    }
                    else
                    {
                        //_previewFrame.setVisible(false);
                        if (!EMPTY_STRING.equals(_previewFrame.getText()))
                        {
                            _previewFrame.setText(EMPTY_STRING);
                        }
                    }
                }
            }
            else if (_previewFrame.isVisible())
            {
                if (!EMPTY_STRING.equals(_previewFrame.getText()))
                {
                    _previewFrame.setText(EMPTY_STRING);
                }

                //_previewFrame.setVisible(false);
            }
        }
    }


    /**
     * Loads the contents of the preview file with the given name.
     *
     * @param name the name of the preview file (without the file extension).
     *
     * @return the contents of the specified preview file or the empty string if no
     *         preview file with the given extension exists. Returns <code>null</code>
     *         if the user specified a custom preview file (via the file menu) and
     *         therefore loading one of the build-in preview file makes no sense.
     *
     * @since 1.0b8
     */
    String loadPreview(String name)
    {
        if (_previewFrame.customFile)
        {
            return null;
        }

        if (_previews.containsKey(name))
        {
            return (String) _previews.get(name);
        }

        Reader in = null;
        InputStream s = null;
        BufferedWriter out = null;

        try
        {
            if (name.equalsIgnoreCase("general")) {
            s = getClass().getResourceAsStream(
                    "allTest.javatpl" /* NOI18N */    );
            }
            else {
            s = getClass().getResourceAsStream(
                    "resources/" /* NOI18N */ + name + ".java.tpl" /* NOI18N */    );
            }

            if (s != null)
            {
                in = new InputStreamReader(s);

                char[] buf = new char[1024];
                CharArrayWriter buffer = new CharArrayWriter(1000);
                out = new BufferedWriter(buffer);

                for (int i = 0, len = 0; (len = in.read(buf, 0, 1024)) != -1; i++)
                {
                    out.write(buf, 0, len);
                }

                out.flush();

                String contents = buffer.toString();
                _previews.put(name, contents);

                return contents;
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                if (s != null)
                {
                    s.close();
                }
            }
            catch (IOException ignored)
            {
                ;
            }

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

            try
            {
                if (out != null)
                {
                    out.close();
                }
            }
            catch (IOException ignored)
            {
                ;
            }
        }

        return EMPTY_STRING;
    }


    private SettingsNodeInfo getLast()
    {
        File file = new File(Convention.getProjectSettingsDirectory(), FILENAME_PAGE);

        if (file.exists() && file.isFile())
        {
            try
            {
                return (SettingsNodeInfo) IoHelper.deserialize(file);
            }
            catch (Throwable ignored)
            {
            }
        }

        return new SettingsNodeInfo(
            "general" /* NOI18N */,
            ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                "LBL_GENERAL" /* NOI18N */),
            "de.hunsicker.jalopy.swing.GeneralSettingsPage" /* NOI18N */);
    }


    String getTitle(DefaultMutableTreeNode node)
    {
        TreeNode[] path = node.getPath();
        StringBuffer buf = new StringBuffer(30);

        for (int i = 0; i < path.length; i++)
        {
            if (path[i].getParent() != null)
            {
                buf.append(path[i]);
                buf.append(" \u00B7 " /* NOI18N */); // middle dot
            }
        }

        buf.setLength(buf.length() - 3);

        return buf.toString();
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        // TODO _settings = 
        Convention.getInstance();

        List supported = new ArrayList(3);
        supported.add("javax." /* NOI18N */);
        supported.add("de.hunsicker." /* NOI18N */);
        _popupSupport = new PopupSupport(supported);

        JPanel spacer = new JPanel();
        Dimension space = new Dimension(5, 5);
        spacer.setMaximumSize(space);
        spacer.setMinimumSize(space);
        spacer.setPreferredSize(space);

        SettingsNodeInfo info = getLast();
        _titleLabel = new JLabel(info.title);
        _titleLabel.setFont(
            new Font(
                _titleLabel.getFont().getName(), Font.BOLD,
                _titleLabel.getFont().getSize()));
        _titleLabel.setBackground(Color.white);
        _titleLabel.setForeground(Color.black);

        JPanel titlePanel = new JPanel();
        titlePanel.add(_titleLabel);
        titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.white);
        titlePanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white),
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(132, 130, 132))));

        _curSettingsPanel = loadPanel(info);
        _panels.put(info.key, _curSettingsPanel);
        _settingsPanel = new JPanel();
        _settingsPanel.setLayout(new BorderLayout());
        _settingsPanel.add(titlePanel, BorderLayout.NORTH);
        _settingsPanel.add(_curSettingsPanel, BorderLayout.CENTER);

        DefaultMutableTreeNode root = createNodes();
        _tree = new JTree(root);
        _tree.setRootVisible(false);
        _tree.setShowsRootHandles(true);
        _tree.putClientProperty("JTree.lineStyle" /* NOI18N */, "Angled" /* NOI18N */);
        _tree.setBorder(
            BorderFactory.createCompoundBorder(
                _tree.getBorder(), BorderFactory.createEmptyBorder(5, 0, 5, 5)));

        DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) _tree.getCellRenderer();
        r.setClosedIcon(null);
        r.setOpenIcon(null);
        r.setLeafIcon(null);

        // restore selection of the last session
        for (Enumeration childs = root.preorderEnumeration(); childs.hasMoreElements();)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) childs.nextElement();
            TreePath path = new TreePath(node.getPath());
            _tree.expandPath(path);

            if (node.getUserObject().equals(info.title))
            {
                _tree.setSelectionPath(path);
                _titleLabel.setText(getTitle(node));
            }
        }

        JScrollPane treeView = new JScrollPane();
        treeView.setPreferredSize(new Dimension(130, 400));
        treeView.getViewport().add(_tree, null);
        _tree.addTreeSelectionListener(new TreeSelectionHandler());

        JPanel propertyView = new JPanel();
        propertyView.setLayout(new BorderLayout());
        propertyView.setPreferredSize(new Dimension(430, 450));
        propertyView.add(spacer, BorderLayout.WEST);
        propertyView.add(_settingsPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(treeView, BorderLayout.WEST);
        add(propertyView, BorderLayout.CENTER);
    }


    /**
     * Loads the panel defined by the given settings info.
     *
     * @param info settings info.
     *
     * @return the indicated settings panel.
     */
    AbstractSettingsPage loadPanel(SettingsNodeInfo info)
    {
        try
        {
            Class[] params = { SettingsContainer.class };
            Constructor c = info.getPanelClass().getDeclaredConstructor(params);
            Object[] args = { this };
            AbstractSettingsPage panel = (AbstractSettingsPage) c.newInstance(args);

            panel.setTitle(info.title);
            panel.setCategory(info.key);

            return panel;
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
        }

        return null;
    }


    /**
     * Tracks the currently displayed panel.
     */
    private void trackPanel()
    {
        File directory = Convention.getProjectSettingsDirectory();
        File file = new File(directory, FILENAME_PAGE);

        if (IoHelper.ensureDirectoryExists(directory))
        {
            try
            {
                SettingsNodeInfo info =
                    new SettingsNodeInfo(
                        _curSettingsPanel.getCategory(), _curSettingsPanel.getTitle(),
                        _curSettingsPanel.getClass().getName());
                IoHelper.serialize(info, file);
            }
            catch (IOException ex)
            {
                file.delete();
            }
        }
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Our customized tree node.
     */
    private static class SettingsNode
        extends DefaultMutableTreeNode
    {
        /**
         * Creates a new SettingsNode object.
         *
         * @param userObj the node information
         */
        public SettingsNode(Object userObj)
        {
            super(userObj);
        }

        /**
         * Returns the node information.
         *
         * @return node information object.
         */
        public SettingsNodeInfo getInfo()
        {
            return (SettingsNodeInfo) getUserObject();
        }
    }


    /**
     * Helper class which provides the necessary information for every property node.
     * This is the user object for the JTree.
     */
    private static class SettingsNodeInfo
        implements Serializable
    {
        /** Use serialVersionUID for interoperability. */
        static final long serialVersionUID = 4496045479306791488L;
        transient Class panelClass;
        String className;
        String key;
        String title;
        int hashCode;

        public SettingsNodeInfo(
            String key,
            String title,
            String className)
        {
            this.key = key;
            this.className = className;
            this.title = title;
            this.hashCode = title.hashCode();
        }

        /**
         * Returns the class to load for the settings page.
         *
         * @return the class to load for the settings page.
         */
        public Class getPanelClass()
        {
            if (this.panelClass == null)
            {
                try
                {
                    this.panelClass = Helper.loadClass(this.className, this);
                }
                catch (ClassNotFoundException ex)
                {
                    this.panelClass = GeneralSettingsPage.class;
                }
            }

            return this.panelClass;
        }


        public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }

            // we only compare titles so that we can easily search the tree
            if (o instanceof String)
            {
                return this.title.equals(o);
            }

            return false;
        }


        public int hashCode()
        {
            return this.hashCode;
        }


        public String toString()
        {
            return this.title;
        }
    }


    /**
     * Handler for tree selection events. Displays the selected settings page.
     */
    private class TreeSelectionHandler
        implements TreeSelectionListener
    {
        boolean armed = true;

        public void valueChanged(TreeSelectionEvent ev)
        {
            if (this.armed)
            {
                SettingsNode node = (SettingsNode) _tree.getLastSelectedPathComponent();

                // do nothing if no node is selected
                if (node == null)
                {
                    return;
                }

                try
                {
                    _curSettingsPanel.validateSettings();
                }
                catch (ValidationException ex)
                {
                    // validation failed, so we don't want the selection to
                    // change revert the old path
                    this.armed = false;
                    _tree.setSelectionPath(ev.getOldLeadSelectionPath());

                    return;
                }

                // update the title
                String title = getTitle(node);
                _titleLabel.setText(title);
                _settingsPanel.remove(_curSettingsPanel);

                SettingsNodeInfo info = node.getInfo();

                if (_panels.containsKey(info.key))
                {
                    // load panel from cache
                    _curSettingsPanel = (AbstractSettingsPage) _panels.get(info.key);
                }
                else
                {
                    _curSettingsPanel = loadPanel(info);

                    // update cache
                    _panels.put(info.key, _curSettingsPanel);
                }

                displayPreview(node);
                _settingsPanel.add(_curSettingsPanel, BorderLayout.CENTER);
                _settingsPanel.repaint();
            }
            else
            {
                this.armed = true;
            }
        }
    }
}
