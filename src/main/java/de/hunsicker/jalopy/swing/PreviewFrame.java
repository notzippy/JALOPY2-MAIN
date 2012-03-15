/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.tree.TreePath;

import de.hunsicker.io.IoHelper;
import de.hunsicker.jalopy.Jalopy;
import de.hunsicker.jalopy.printer.PrinterFactory;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.Loggers;
import de.hunsicker.jalopy.swing.syntax.DefaultSyntaxDocument;
import de.hunsicker.jalopy.swing.syntax.SyntaxEditorKit;
import de.hunsicker.swing.util.SwingHelper;
import de.hunsicker.util.ResourceBundleFactory;

import org.apache.log4j.Level;

import antlr.CommonASTWithHiddenTokens;
import antlr.collections.AST;
import antlr.debug.misc.JTreeASTModel;
import antlr.debug.misc.JTreeASTPanel;


/**
 * Provides a floating preview that can be used to display a Java source file.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 *
 * @since 1.0b8
 */
final class PreviewFrame
    extends JDialog
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String EXT_JAVA = ".java" /* NOI18N */;
    static final String EMPTY_STRING = "" /* NOI18N */.intern();

    //~ Instance variables ---------------------------------------------------------------

    /** Indicates whether the user opened a custom preview file. */
    boolean customFile;

    /** The currently displayed settings page. */
    AbstractSettingsPage _page;

    /** Action to close the active file. */
    final Action ACTION_FILE_CLOSE = new FileCloseAction();

    /** Action to choose and open a new Java source file. */
    private final Action ACTION_FILE_OPEN = new FileOpenAction();
    
    private final Action TREE_VIEW_ACTION = new TreeViewAction();

    /** Our text area. */
    JEditorPane _textArea;

    /** The Jalopy instance to format the preview files. */
    Jalopy _jalopy = new Jalopy();

    /** The list of the currently running threads. */
    List _threads = new ArrayList(3); // List of <Thread>
    ResourceBundle bundle;

    /** The associated settings dialog. */
    private Window _dialog;

    /** Was the frame ever made visible? */
    private boolean _wasVisible;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new PreviewFrame object.
     *
     * @param owner the parent dialog.
     */
    protected PreviewFrame(Dialog owner)
    {
        super(owner);
        initialize();

        setTitle(this.bundle.getString("TLE_PREVIEW" /* NOI18N */));
    }


    /**
     * Creates a new PreviewFrame object.
     *
     * @param owner the parent frame.
     */
    protected PreviewFrame(Frame owner)
    {
        super(owner);
        initialize();

        setTitle(this.bundle.getString("TLE_PREVIEW" /* NOI18N */));
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Creates a new preview frame with the window owner
     *
     * @param owner The window
     *
     * @return The preview frame instance
     */
    public static PreviewFrame create(Window owner)
    {
        return create(owner, owner);
    }


    /**
     * Sets the settings page that is currently displayed in the settings dialog.
     *
     * @param page the currently displayed settings page.
     */
    public void setCurrentPage(AbstractSettingsPage page)
    {
        _page = page;
    }


    /**
     * Returns the currently active settings page.
     *
     * @return currently displayed settings page.
     */
    public AbstractSettingsPage getCurrentPage()
    {
        return _page;
    }


    /**
     * Sets the contents of the preview.
     *
     * @param text contents of a valid Java source file.
     */
    public synchronized void setText(String text)
    {
        if (text == null)
        {
            text = _textOriginal.getText();
        }

        synchronized (_threads)
        {
            if (_threads.size() > 0)
            {
                try
                {
                    FormatThread thread = (FormatThread) _threads.remove(0);
                    thread.interrupt();

                    if (thread.isAlive())
                    {
                        thread.join(50);
                    }
                }
                catch (InterruptedException ignored)
                {
                    ;
                }

                _jalopy.reset();
            }

            Thread thread = new FormatThread(text);
            _textOriginal.setText(text);
            ((DefaultSyntaxDocument)_textOriginal.getDocument()).tokenizeLines();
            _threads.add(thread);
            thread.start();
        }
    }


    /**
     * Returns the contents of the text area.
     *
     * @return contents of the text area.
     *
     * @since 1.0b9
     */
    public String getText()
    {
        return _textOriginal.getText();
    }


    /**
     * Makes the dialog visible.
     */
    public void show()
    {
        if (!_wasVisible)
        {
            Dimension screen = getToolkit().getScreenSize();

            int screenWidth = screen.width;
            int screenHeight = screen.height;
            int dialogWidth = _dialog.getWidth();
//            int dialogHeight = _dialog.getHeight();
//            int dialogX = _dialog.getX();
            int dialogY = _dialog.getY();
            int frameWidth = 600;

            if (screenWidth > (dialogWidth + frameWidth))
            {
                _dialog.setLocation(
                    (screenWidth - frameWidth - dialogWidth) / 2, dialogY);
                setSize(frameWidth, screenHeight - 30);
                setLocation(_dialog.getX() + dialogWidth, 1);
            }
            else
            {
                _dialog.setLocation(1, dialogY);
                setSize(screenWidth - dialogWidth - 2, screenHeight - 30);
                setLocation(_dialog.getX() + dialogWidth, 1);
            }

            _wasVisible = true;
        }

        super.show();
        _dialog.toFront();
    }


    /**
     * Issues an update of the preview.
     *
     * @since 1.0b9
     */
    public void update()
    {
        setText(_textOriginal.getText());
    }


    /**
     * Creates a new PreviewFrame object.
     *
     * @param owner the window from which the frame is displayed.
     * @param target DOCUMENT ME!
     *
     * @return a new preview frame.
     *
     * @since 1.0b9
     */
    static PreviewFrame create(
        Window owner,
        Window target)
    {
        PreviewFrame frame = null;

        if (owner instanceof Frame)
        {
            frame = new PreviewFrame((Frame) owner);
        }
        else
        {
            frame = new PreviewFrame((Dialog) owner);
        }

        frame._dialog = target;

        return frame;
    }


    private void initialize()
    {
        this.bundle =
            ResourceBundleFactory.getBundle(
                "de.hunsicker.jalopy.swing.Bundle" /* NOI18N */);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu();
        SwingHelper.setMenuText(
            fileMenu, this.bundle.getString("MNE_FILE" /* NOI18N */), true);
        menuBar.add(fileMenu);

        JMenuItem openFileMenuItem = new JMenuItem(ACTION_FILE_OPEN);
        SwingHelper.setMenuText(
            openFileMenuItem, this.bundle.getString("MNE_OPEN" /* NOI18N */), true);
        fileMenu.add(openFileMenuItem);

        JMenuItem closeFileMenuItem = new JMenuItem(ACTION_FILE_CLOSE);
        SwingHelper.setMenuText(
            closeFileMenuItem, this.bundle.getString("MNE_CLOSE" /* NOI18N */), true);
        fileMenu.add(closeFileMenuItem);
        
        JMenuItem treeViewItem = new JMenuItem(TREE_VIEW_ACTION);
        SwingHelper.setMenuText(treeViewItem, "Tree view", true);
        fileMenu.add(treeViewItem);

        setJMenuBar(menuBar);

        SyntaxEditorKit kit = new SyntaxEditorKit();
        DefaultSyntaxDocument doc = new DefaultSyntaxDocument();
        doc.setAsynchronousLoadPriority(-1);

        _textArea = new JEditorPane();
        _textArea.setDocument(doc);
        _textArea.setFont(new Font("Monospaced" /* NOI18N */, Font.PLAIN, 12));
        _textArea.setEditable(false);
        _textArea.setCaretPosition(0);
        _textArea.setMargin(new Insets(2, 2, 2, 2));
        _textArea.setOpaque(true);
        _textArea.setEditorKit(kit);

        JScrollPane scrollPane = new JScrollPane(_textArea);
        JTabbedPane mainTabbedPane = new JTabbedPane();
        doc = new DefaultSyntaxDocument();
        doc.setAsynchronousLoadPriority(-1);
        _textOriginal = new JEditorPane();
        _textOriginal.setDocument(doc);
        _textOriginal.setFont(new Font("Monospaced" /* NOI18N */, Font.PLAIN, 12));
        _textOriginal.setEditable(false);
        _textOriginal.setCaretPosition(0);
        _textOriginal.setMargin(new Insets(2, 2, 2, 2));
        _textOriginal.setOpaque(true);
        _textOriginal.setEditorKit(new SyntaxEditorKit());
        
        mainTabbedPane.add("Original",new JScrollPane(_textOriginal));
        mainTabbedPane.add("Formatted",scrollPane);
        
        
        getContentPane().add(mainTabbedPane);
    }
    JEditorPane _textOriginal = null;
    //~ Inner Classes --------------------------------------------------------------------

    private final class FileCloseAction
        extends AbstractAction
    {
        public FileCloseAction()
        {
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent ev)
        {
            PreviewFrame.this.customFile = false;

            String filename = getCurrentPage().getPreviewFileName();
            setText(getCurrentPage().getContainer().loadPreview(filename));

            setEnabled(false);
        }
    }


    private final class TreeViewAction extends AbstractAction {
        public void actionPerformed(ActionEvent ev)
        {
            AST root = _jalopy.getRecognizer().getRoot();
            
            
			final JFrame frame = new JFrame("Java AST");
			frame.setVisible(true);
			frame.addWindowListener(
				new WindowAdapter() {
                   public void windowClosing (WindowEvent e) {
                       frame.setVisible(false); // hide the Frame
                       frame.dispose();
                   }
		        }
			);
			
			JTreeASTPanel panel = new JTreeASTPanel(new JTreeASTModel(root){
			    /*
			    public Object getChild(Object parent, int index) {
			        if (parent == null) {
			            return null;
			        }
			        AST p = (AST)parent;
			        AST c = p.getFirstChild();
			        if (c == null) {
			            throw new ArrayIndexOutOfBoundsException("node has no children");
			        }
			        int i = 0;
			        while (c != null && i < index) {
			            c = c.getNextSibling();
			            i++;
			        }
			        System.out.println("Getting child for " + parent +" child num " +i+"Child" +c);
			        return c;
			    }
			    
			    public int getChildCount(Object parent) {
			        if (parent == null) {
			            throw new IllegalArgumentException("root is null");
			        }
			        AST p = (AST)parent;
			        AST c = p.getFirstChild();
			        int i = 0;
			        while (c != null) {
			            c = c.getNextSibling();
			            i++;
			        }
			        System.out.println("Child count for " +p+","+i);
			        return i;
			    }
			    */
			    
			}, new TreeSelectionListener() {
		        public void valueChanged(TreeSelectionEvent event) {
		            
		            TreePath path = event.getPath();
		            System.out.println("Selected: " +"," +
		                               ((CommonASTWithHiddenTokens) path.getLastPathComponent()).getLine() + ","+
		                               ((CommonASTWithHiddenTokens) path.getLastPathComponent()).getColumn()
		                               +"\n\r"+
		            ((CommonASTWithHiddenTokens) path.getLastPathComponent()).getHiddenBefore()+"\n\r"+
		            ((CommonASTWithHiddenTokens) path.getLastPathComponent()).getHiddenAfter());
		            Object elements[] = path.getPath();
		            for (int i = 0; i < elements.length; i++) {
		                System.out.print("->" + elements[i].getClass());
		                
		            }
		            System.out.println();
		            
		        }});
			frame.getContentPane().add(panel);

			// System.out.println(t.toStringList());
	            
        }
        
    }
    private final class FileOpenAction
        extends AbstractAction
    {
        public FileOpenAction()
        {
        }

        public void actionPerformed(ActionEvent ev)
        {
            LocationDialog dialog =
                LocationDialog.create(
                    PreviewFrame.this,
                    PreviewFrame.this.bundle.getString("TLE_OPEN_JAVA_FILE" /* NOI18N */),
                    "XXX", EMPTY_STRING);
            dialog.addFilter(new JavaFilter(), true);
            dialog.setVisible(true);

            switch (dialog.getOption())
            {
                case JOptionPane.OK_OPTION :

                    try
                    {
                        String location = (String) dialog.getSelectedLocation();

                        if ((location == null) || (location.trim().length() == 0))
                        {
                            /**
                             * @todo show error dialog;
                             */
                            return;
                        }

                        File file = new File(location);

                        String contents = IoHelper.readTextFile(file);
                        setText(contents);

                        PreviewFrame.this.customFile = true;
                        ACTION_FILE_CLOSE.setEnabled(true);
                    }
                    catch (Throwable ex)
                    {
                        ex.printStackTrace();
                    }

                    break;
            }
        }
    }


    private class FormatThread
        extends Thread
    {
        String text;

        public FormatThread(String text)
        {
            this.text = text;
        }

        public void run()
        {
            Level ioLevel = null;
            Level parserLevel = null;
            Level parserJavadocLevel = null;
            Level printerLevel = null;
            Level printerJavadocLevel = null;

            try
            {
                // the thread may be interrupted, but I don't want to introduce
                // inconsistencies
            }
            finally
            {
                ioLevel = Loggers.IO.getLevel();
                parserLevel = Loggers.PARSER.getLevel();
                parserJavadocLevel = Loggers.PARSER_JAVADOC.getLevel();
                printerLevel = Loggers.PRINTER.getLevel();
                printerJavadocLevel = Loggers.PRINTER_JAVADOC.getLevel();
                _page.settings.snapshot();
            }

            try
            {
                _page.updateSettings();

                // disable logging so no messages appear and may confuse the
                // users
                Loggers.IO.setLevel(Level.FATAL);
                Loggers.PARSER.setLevel(Level.FATAL);
                Loggers.PARSER_JAVADOC.setLevel(Level.FATAL);
                Loggers.PRINTER.setLevel(Level.WARN);
                Loggers.PRINTER_JAVADOC.setLevel(Level.FATAL);

                if (!PreviewFrame.this.customFile)
                {
                    // enable Header/Footer template only for the Header/Footer page
                    if (_page.getCategory().equals("header" /* NOI18N */))
                    {
                        _page.settings.putBoolean(ConventionKeys.FOOTER, false);
                    }
                    else if (_page.getCategory().equals("footer" /* NOI18N */))
                    {
                        _page.settings.putBoolean(ConventionKeys.HEADER, false);
                    }
                    else
                    {
                        _page.settings.putBoolean(ConventionKeys.FOOTER, false);
                        _page.settings.putBoolean(ConventionKeys.HEADER, false);

                        if (_page.getCategory().equals("indentation" /* NOI18N */))
                        {
                            _page.settings.putBoolean(
                                ConventionKeys.LINE_WRAP_BEFORE_EXTENDS, true);
                            _page.settings.putBoolean(
                                ConventionKeys.LINE_WRAP_BEFORE_IMPLEMENTS, true);
                            _page.settings.putBoolean(
                                ConventionKeys.LINE_WRAP_BEFORE_THROWS, true);
                            _page.settings.putBoolean(
                                ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_DEF, true);
                            _page.settings.putBoolean(
                                ConventionKeys.ALIGN_TERNARY_EXPRESSION, true);
                            _page.settings.putBoolean(
                                ConventionKeys.ALIGN_TERNARY_VALUES, true);
                        }
                    }

                    // enable Javadoc template insertion only for Javadoc page
                    if (!_page.getCategory().equals("javadoc" /* NOI18N */))
                    {
                        /*
                        TODO REMOVE COMMENTS
                        _page.settings.putInt(
                            ConventionKeys.COMMENT_JAVADOC_CLASS_MASK, 0);
                        _page.settings.putInt(
                            ConventionKeys.COMMENT_JAVADOC_CTOR_MASK, 0);
                        _page.settings.putInt(
                            ConventionKeys.COMMENT_JAVADOC_METHOD_MASK, 0);
                        _page.settings.putInt(
                            ConventionKeys.COMMENT_JAVADOC_VARIABLE_MASK, 0);
                            */
                    }

                    // enable separation comments only for Separation page
                    if (
                        !_page.getPreviewFileName().equals(
                            "separationcomments" /* NOI18N */))
                    {
                        //_page.settings.putBoolean(
                        //    ConventionKeys.COMMENT_INSERT_SEPARATOR, false);
                    }
                }

                /*
                int wrapGuideColumn =
                    _page.settings.getInt(
                        ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH);

                if (wrapGuideColumn != _textArea.getWrapGuideColumn())
                {
                    _textArea.setWrapGuideColumn(wrapGuideColumn);
                }
                */
                _textArea.getDocument().putProperty(
                    PlainDocument.tabSizeAttribute,
                    new Integer(
                        _page.settings.get(
                            ConventionKeys.INDENT_SIZE,
                            String.valueOf(ConventionDefaults.INDENT_SIZE))));

                _jalopy.setForce(true);
                
                if (this.text.length() > 0)
                {
                    _jalopy.setInput(this.text, _page.getCategory() + EXT_JAVA);

                    final StringBuffer buf = new StringBuffer(this.text.length());
                    _jalopy.setOutput(buf);
                    _jalopy.setInspect(true);
                    _jalopy.format();
                    
                    SwingUtilities.invokeLater(
                        new Runnable()
                        {
                            public void run()
                            {
                                int offset = _textArea.getCaretPosition();
                                synchronized (_textArea) {
                                    _textArea.setText(buf.toString());
                                    ((DefaultSyntaxDocument)_textArea.getDocument()).tokenizeLines();
                                }

                                if (_textArea.getDocument().getLength() > offset)
                                {
                                    _textArea.setCaretPosition(offset);
                                }
                            }
                        });
                }
                else
                {
                    _textArea.setText(this.text);
                }
            }
            catch (Throwable ignored)
            {
                ignored.printStackTrace();
            }
            finally
            {
                // restore the current active settings because we want the user
                // to explicitly enable the changes (either by pressing 'OK'
                // or 'Apply')
                _page.settings.revert();

                if (EMPTY_STRING.equals(_textArea.getText()))
                {
                    _textArea.setText(this.text);
                }

                _jalopy.reset();

                Loggers.IO.setLevel(ioLevel);
                Loggers.PARSER.setLevel(parserLevel);
                Loggers.PARSER_JAVADOC.setLevel(parserJavadocLevel);
                Loggers.PRINTER.setLevel(printerLevel);
                Loggers.PRINTER_JAVADOC.setLevel(printerJavadocLevel);

                synchronized (_threads)
                {
                    _threads.remove(this);
                }
            }
        }
    }


    /**
     * JFileChooser filter for Java source files (.java).
     */
    private class JavaFilter
        extends FileFilter
    {
        public String getDescription()
        {
            return PreviewFrame.this.bundle.getString(
                "LBL_JAVA_SOURCE_FILES" /* NOI18N */);
        }


        public boolean accept(File f)
        {
            if (f == null)
            {
                return false;
            }

            if (f.isDirectory())
            {
                return true;
            }

            if (f.getName().endsWith(EXT_JAVA))
            {
                return true;
            }

            return false;
        }
    }
}
