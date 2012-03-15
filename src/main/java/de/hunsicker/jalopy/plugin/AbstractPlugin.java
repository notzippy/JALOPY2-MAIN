/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.plugin;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import de.hunsicker.io.FileFormat;
import de.hunsicker.jalopy.Jalopy;
import de.hunsicker.jalopy.language.Position;
import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;
import de.hunsicker.jalopy.storage.History;
import de.hunsicker.jalopy.storage.Loggers;
import de.hunsicker.jalopy.swing.ProgressMonitor;
import de.hunsicker.jalopy.swing.ProgressPanel;
import de.hunsicker.swing.ErrorDialog;
import de.hunsicker.swing.util.SwingWorker;
import de.hunsicker.util.ChainingRuntimeException;
import de.hunsicker.util.ResourceBundleFactory;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;


//J- needed only as a workaround for a Javadoc bug
import java.lang.System;
//J+

/**
 * Skeleton implementation of a Jalopy Plug-in for integrated development environments.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.10 $
 */
public abstract class AbstractPlugin
{
    //~ Static variables/initializers ----------------------------------------------------

    private static final String EMPTY_STRING = "" /* NOI18N */.intern();

    /** The default status bar does nothing. */
    private static final StatusBar DEFAULT_STATUS_BAR = new DummyStatusBar();

    /** Cursor to display whilst long-running operations. */
    static final Cursor WAIT_CURSOR =
        Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

    /** The name for ResourceBundle lookup. */
    private static final String BUNDLE_NAME =
        "de.hunsicker.jalopy.plugin.Bundle" /* NOI18N */;

    //~ Instance variables ---------------------------------------------------------------

    /** The main Jalopy instance. */
    protected Jalopy jalopy;

    /** Appender to write messages to. */
    protected SwingAppender appender;
    int offset = 0;

    /** The action that was last performed. */
    Action _lastAction;

    /** Holds the currently running action worker. */
    ActionWorker _worker;

    /** Stores the original glass pane. */
    Component _oldGlassPane;

    /** Pane to display on top of all other components. */
    GlassPane _glassPane;

    /** Used as a monitor to synchronize processes. */
    final Object _lock = new Object();

    /** Progress monitor for long running operations. */
    private ProgressMonitor _progressMonitor;
    final Object[] _args = new Object[3];

    /** Number of running formatting threads. */
    int _threadCount;

    /** When did the worker thread start? */
    long _start;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new AbstractPlugin object. Uses a default appender which outputs all
     * messages to <code>System.out</code>.
     */
    public AbstractPlugin()
    {
        this(new DefaultAppender());
    }


    /**
     * Creates a new AbstractPlugin object.
     *
     * @param appender appender to use for logging; if <code>null</code> all logging
     *        output goes to <code>System.out</code>.
     */
    public AbstractPlugin(SwingAppender appender)
    {
        this.appender = appender;
        initLogging();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the currently active project.
     *
     * @return the active user project.
     */
    public abstract Project getActiveProject();


    /**
     * Returns the main window of the application.
     *
     * @return the main application window.
     */
    public abstract Frame getMainWindow();


    /**
     * Returns the elapsed execution time of the run.
     *
     * @return the elapsed execution time.
     */
    public long getElapsed()
    {
        return System.currentTimeMillis() - _start;
    }


    /**
     * Returns the action that was performed last.
     *
     * @return Last performed action. Returns <code>null</code> if no action was ever
     *         performed.
     */
    public final Action getLastAction()
    {
        return _lastAction;
    }


    /**
     * Returns the state info of Plug-in. Use this method to query the state after a run
     * finished.
     *
     * @return The run state.
     *
     * @see de.hunsicker.jalopy.Jalopy#getState
     */
    public final synchronized Jalopy.State getState()
    {
        if (this.jalopy == null)
        {
            return Jalopy.State.UNDEFINED;
        }

        return this.jalopy.getState();
    }


    /**
     * Determines whether the Plug-in currently processes a request.
     *
     * @return <code>true</code> if an action is currently being performed.
     *
     * @see #performAction
     * @see #interrupt
     * @since 1.0b8
     */
    public synchronized boolean isRunning()
    {
        return _worker != null;
    }


    /**
     * Returns the active status bar. Override to provide access to the status bar of the
     * used application.
     *
     * @return the active status bar. The default implementation returns a dummy.
     */
    public StatusBar getStatusBar()
    {
        return DEFAULT_STATUS_BAR;
    }


    /**
     * Called on the event dispatching thread after an action was performed.
     *
     * <p>
     * Override this method to perform any custom work after the formatting process
     * finished.
     * </p>
     */
    public void afterEnd()
    {
    }


    /**
     * Called on the event dispatching thread before an action will be started.
     *
     * <p>
     * Override this method to perform any custom work before the formatting process
     * starts.
     * </p>
     */
    public void beforeStart()
    {
    }


    /**
     * Interrupts the currently performed action, if any.
     *
     * @since 1.0b8
     */
    public final synchronized void interrupt()
    {
        if (_worker != null)
        {
            _worker.interrupt();
        }
    }


    /**
     * Performs the given action.
     *
     * @param action action to perform.
     */
    public final synchronized void performAction(final AbstractPlugin.Action action)
    {
        // clear the message window
        this.appender.clear();

        _worker = new ActionWorker(action);
        _worker.start();

        _lastAction = action;
    }


    /**
     * Returns the file format to use for writing Java source files.
     *
     * @return the file format to use.
     */
    protected abstract FileFormat getFileFormat();


    /**
     * Returns a Jalopy instance. The instance will be configured according to the
     * current code convention.
     *
     * @return a Jalopy instance.
     *
     * @since 1.0b8
     */
    public final Jalopy getEngine()
    {
        if (this.jalopy == null)
        {
            if (_progressMonitor != null)
            {
                _progressMonitor.setText(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "MSG_INITIALIZATION" /* NOI18N */));
            }

            getStatusBar().setText(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "MSG_INITIALIZATION" /* NOI18N */));
            this.jalopy = new Jalopy();
        }

        configureJalopy(this.jalopy);

        return this.jalopy;
    }


    /**
     * Creates a monitor to be used for long-running operations.
     *
     * @return the progress monitor to use for long-running operations.
     */
    protected ProgressMonitor createProgressMonitor()
    {
        return new ProgressMonitorImpl();
    }


    /**
     * Displays an error dialog.
     *
     * @param error the throwable which caused the error.
     * @param parent parent frame of the dialog (used to position the dialog).
     */
    protected void displayError(
        Throwable error,
        Frame     parent)
    {
        ErrorDialog d = ErrorDialog.create(getMainWindow(), error);
        d.setVisible(true);
        d.dispose();
    }


    /**
     * Executes the given runnable asynchronously on the AWT event dispatching thread.
     *
     * @param operation runnable to be invoked asynchronously on the AWT event
     *        dispatching thread.
     *
     * @see javax.swing.SwingUtilities#invokeLater
     */
    protected void executeAsynchron(Runnable operation)
    {
        EventQueue.invokeLater(operation);
    }


    /**
     * Executes the given runnable synchronously on the AWT event dispatching thread.
     *
     * @param operation runnable to be invoked synchronously on the AWT event dispatching
     *        thread.
     *
     * @throws InterruptedException if another thread has interrupted this thread.
     * @throws InvocationTargetException if an exception is thrown when running runnable.
     *
     * @see javax.swing.SwingUtilities#invokeAndWait
     */
    protected void executeSynchron(Runnable operation)
      throws InterruptedException, InvocationTargetException
    {
        EventQueue.invokeAndWait(operation);
    }


    /**
     * Hides the wait cursor.
     */
    protected void hideWaitCursor()
    {
        if ((_glassPane != null) && _glassPane.isVisible())
        {
            // hiding the glass pane restores the normal cursor too
            _glassPane.setVisible(false);

            Frame window = getMainWindow();

            if ((window != null) && window instanceof JFrame)
            {
                JFrame w = (JFrame) window;

                // restore the original glass pane
                w.getRootPane().setGlassPane(_oldGlassPane);
            }
        }
    }


    /**
     * Shows the wait cursor to indicate a long-running operation. Keyboard and mouse
     * input will be blocked.
     */
    protected void showWaitCursor()
    {
        final Frame window = getMainWindow();

        if ((window != null) && window instanceof JFrame)
        {
            if (_glassPane == null)
            {
                _glassPane = new GlassPane();
            }

            _glassPane.setThread(Thread.currentThread());

            try
            {
                executeSynchron(
                    new Runnable()
                    {
                        public void run()
                        {
                            // make the glass pane visible so that all mouse and key
                            // actions will be blocked and a wait cursor displayed;
                            // the pane will be made hidden in the afterEnd() method that
                            // is always called after the run has finished
                            JFrame w = (JFrame) window;
                            _oldGlassPane = w.getRootPane().getGlassPane();
                            w.getRootPane().setGlassPane(_glassPane);
                            _glassPane.setCursor(WAIT_CURSOR);
                            _glassPane.setVisible(true);
                        }
                    });
            }
            catch (Throwable ignored)
            {
                hideWaitCursor();
            }
        }
    }


    /**
     * Configures the given Jalopy instance to meet the current code convention.
     *
     * @param newJalopy Jalopy instance to configure.
     */
    private void configureJalopy(Jalopy newJalopy)
    {
        Convention settings = Convention.getInstance();
        int backupLevel =
            settings.getInt(ConventionKeys.BACKUP_LEVEL, ConventionDefaults.BACKUP_LEVEL);
        newJalopy.setBackup(backupLevel > 0);
        newJalopy.setBackupDirectory(
            settings.get(
                ConventionKeys.BACKUP_DIRECTORY,
                Convention.getBackupDirectory().getAbsolutePath()));
        newJalopy.setHistoryPolicy(
            History.Policy.valueOf(
                settings.get(
                    ConventionKeys.HISTORY_POLICY, ConventionDefaults.HISTORY_POLICY)));
        newJalopy.setHistoryMethod(
            History.Method.valueOf(
                settings.get(
                    ConventionKeys.HISTORY_METHOD, ConventionDefaults.HISTORY_METHOD)));
        newJalopy.setInspect(
            settings.getBoolean(ConventionKeys.INSPECTOR, ConventionDefaults.INSPECTOR));
        newJalopy.setBackupLevel(backupLevel);
        newJalopy.setFileFormat(getFileFormat());
        newJalopy.setForce(
            settings.getBoolean(
                ConventionKeys.FORCE_FORMATTING, ConventionDefaults.FORCE_FORMATTING));
    }


    /**
     * Formats the given file. This method performs the actual work.
     *
     * @param file Java source file.
     * @param newJalopy Jalopy instance to use.
     *
     * @throws IOException if an I/O error occured.
     * @throws InvocationTargetException if the updating of an editor window failed.
     */
    void format(
        ProjectFile  file,
        final Jalopy newJalopy)
      throws IOException, InvocationTargetException
    {
        newJalopy.setEncoding(file.getEncoding());

        if (_progressMonitor != null)
        {
            _args[0] = file.getName();

            _progressMonitor.setText(
                MessageFormat.format(
                    ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                        "MSG_FORMATTING_FILE" /* NOI18N */), _args));

            if (_progressMonitor instanceof ProgressMonitorImpl)
            {
                ((ProgressMonitorImpl) _progressMonitor).progressPanel.increaseFiles();
            }
        }

        // only update the file if available
        if (!file.isReadOnly())
        {
            getStatusBar().setText(
                ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                    "MSG_FORMATTING" /* NOI18N */));

            if (file.isOpened()) // we're interacting with an editor view
            {
                final Editor editor = file.getEditor();

                String content = editor.getText();

                if ((content != null) && (content.length() > 0))
                {
                    newJalopy.setInput(content, file.getFile().getAbsolutePath());

                    List annotations = editor.detachAnnotations();
                    newJalopy.getRecognizer().attachAnnotations(annotations);

                    try
                    {
                        executeSynchron(
                            new Runnable()
                            {
                                public void run()
                                {
                                    newJalopy.getRecognizer().setPosition(
                                        editor.getLine(), editor.getColumn());
                                }
                            });
                    }
                    catch (InterruptedException ex)
                    {
                        ;
                    }

                    final StringBuffer textBuf = new StringBuffer(content.length());
                    newJalopy.setOutput(textBuf);
                    newJalopy.format();

                    if ((_progressMonitor != null) && _progressMonitor.isCanceled())
                    {
                        newJalopy.getRecognizer().detachAnnotations();

                        return;
                    }

                    // only update the editor view if no errors showed up
                    // and the content has changed
                    if (getState() != Jalopy.State.ERROR && !content.equals(textBuf.toString()))
                    {
                        try
                        {
                            executeSynchron(
                                new Runnable()
                                {
                                    public void run()
                                    {
                                        editor.setText(textBuf.toString());
                                        editor.attachAnnotations(
                                            newJalopy.getRecognizer().detachAnnotations());

                                        Position position =
                                            newJalopy.getRecognizer().getPosition();
                                        editor.setCaretPosition(
                                            position.getLine(), position.getColumn());
                                    }
                                });
                        }
                        catch (InterruptedException ex)
                        {
                            ;
                        }
                    }
                }
            }
            else // update the physical file
            {
                File f = file.getFile();
                newJalopy.setInput(f);
                newJalopy.setOutput(f);
                newJalopy.format();
            }
        }
        else
        {
            Object[] args = { file };
            Loggers.IO.l7dlog(Level.INFO, "FILE_READ_ONLY" /* NOI18N */, args, null);
        }
    }


    /**
     * Formats the given files.
     *
     * @param newJalopy the Jalopy instance to use for formatting.
     * @param files list with the files to format.
     *
     * @throws IOException if an I/O error occured.
     * @throws InvocationTargetException if the updating of an editor window failed.
     */
    void formatSeveral(
        Jalopy     newJalopy,
        Collection files)
      throws IOException, InvocationTargetException
    {
        formatSeveral(newJalopy, files, true);
    }


    /**
     * Formats the given files.
     *
     * @param newJalopy the Jalopy instance to use for formatting.
     * @param files list with the files to format.
     * @param checkThreading if <code>true</code> checks whether the user enabled the
     *        multi-threaded execution and if so, uses multiple threads to perform the
     *        operation.
     *
     * @throws IOException if an I/O error occured.
     * @throws InvocationTargetException if the updating of an editor window failed.
     */
    void formatSeveral(
        final Jalopy     newJalopy,
        final Collection files,
        final boolean    checkThreading)
      throws IOException, InvocationTargetException
    {
        final int size = files.size();

        if (size > 0)
        {
            synchronized (this)
            {
                if (_progressMonitor == null)
                {
                    _progressMonitor = createProgressMonitor();

                    _progressMonitor.begin(
                        ((newJalopy == null)
                        ? ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                            "MSG_INITIALIZATION" /* NOI18N */)
                        : EMPTY_STRING), files.size());
                }
            }

            int numThreads =
                Convention.getInstance().getInt(
                    ConventionKeys.THREAD_COUNT, ConventionDefaults.THREAD_COUNT);

            if (!checkThreading || (numThreads == 1) || (size == 1))
            {
                for (Iterator i = files.iterator(); i.hasNext();)
                {
                    // the user canceled the program execution
                    if (_progressMonitor.isCanceled())
                    {
                        return;
                    }

                    ProjectFile file = (ProjectFile) i.next();
                    format(file, newJalopy);

                    synchronized (_progressMonitor)
                    {
                        _progressMonitor.setProgress(_progressMonitor.getProgress() + 1);
                    }
                }
            }
            else
            {
                List workList =
                    (files instanceof List) ? (List) files
                                            : new ArrayList(files);
                int amount = 1;

                if (numThreads < size)
                {
                    amount = size / numThreads;
                }
                else
                {
                    numThreads = size;
                }

                // the number of threads to span
                _threadCount = numThreads - 1;

                for (int i = 0, threshold = numThreads - 1; i < threshold; i++)
                {
                    Jalopy j = new Jalopy();
                    configureJalopy(j);

                    Collection part = workList.subList(i * amount, (i + 1) * amount);
                    new FormatThread(j, part).start();
                }

                Collection rest = workList.subList((numThreads - 1) * amount, size);
                formatSeveral(newJalopy, rest, false);

                try
                {
                    synchronized (_lock)
                    {
                        while (_threadCount > 0)
                        {
                            _lock.wait();
                        }
                    }
                }
                catch (InterruptedException ignored)
                {
                    ;
                }
            }
        }
    }


    synchronized void hideProgressMonitor()
    {
        if (_progressMonitor != null)
        {
            _progressMonitor.done();
            _progressMonitor = null;
        }
    }


    /**
     * Initializes the logging system. This method is called upon the creation of the
     * object.
     */
    private void initLogging()
    {
        // if no appender was specified, we output all messages to System.out
        if (this.appender == null)
        {
            this.appender = new DefaultAppender();
        }

        Loggers.initialize(this.appender);
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Represents an action that can be performed.
     *
     * @see AbstractPlugin#performAction
     * @since 1.0b8
     */
    public static final class Action
    {
        /** Indicates that no action was ever performed. */
        public static final Action UNDEFINED = new Action("undefined" /* NOI18N */);

        /** Format the currently active (opened) file. */
        public static final Action FORMAT_ACTIVE =
            new Action("format_active" /* NOI18N */);

        /** Format all Java Source files of the currently active project. */
        public static final Action FORMAT_ALL = new Action("format_all" /* NOI18N */);

        /** Format all currently opened Java source files. */
        public static final Action FORMAT_OPEN = new Action("format_open" /* NOI18N */);

        /** Format the selected Java source file(s). */
        public static final Action FORMAT_SELECTED =
            new Action("format_selected" /* NOI18N */);

        /** Parse the currently active (opened) file. */
        public static final Action PARSE_ACTIVE = new Action("parse_active" /* NOI18N */);

        /** Parse all Java Source files of the currently active project. */
        public static final Action PARSE_ALL = new Action("parse_all" /* NOI18N */);

        /** Parse all currently opened Java source files. */
        public static final Action PARSE_OPEN = new Action("parse_open" /* NOI18N */);

        /** Parse the selected Java source file(s). */
        public static final Action PARSE_SELECTED =
            new Action("parse_selected" /* NOI18N */);

        /** Inspect the currently active (opened) file. */
        public static final Action INSPECT_ACTIVE =
            new Action("inspect_active" /* NOI18N */);

        /** Inspect all Java Source files of the currently active project. */
        public static final Action INSPECT_ALL = new Action("inspect_all" /* NOI18N */);

        /** Inspect all currently opened Java source files. */
        public static final Action INSPECT_OPEN = new Action("inspect_open" /* NOI18N */);

        /** Inspect the selected Java source file(s). */
        public static final Action INSPECT_SELECTED =
            new Action("inspect_selected" /* NOI18N */);
        final String name;

        private Action(String name)
        {
            this.name = name.intern();
        }
    }


    /**
     * Default appender to use if no custom appender was specified. All output goes to
     * System.out.
     */
    private static class DefaultAppender
        extends ConsoleAppender
        implements SwingAppender
    {
        /**
         * Default appender for plufins
         *
         * @todo overide format() to add stacktraces
         */
        public DefaultAppender()
        {
            super(new PatternLayout("[%p] %m\n" /* NOI18N */), "System.out" /* NOI18N */);
        }

        public void clear()
        {
        }


        public void done()
        {
        }
    }


    private static final class DummyStatusBar
        implements StatusBar
    {
        public void setText(String text)
        {
        }
    }


    /**
     * Worker to perform our actions in a dedicated thread.
     */
    private class ActionWorker
        extends SwingWorker
    {
        /** The action to perform. */
        Action action;

        /**
         * Stores the initial position of the caret in case we're formatting an opened
         * file.
         */
        int offset;

        public ActionWorker(Action action)
        {
            this.action = action;
        }

        public Object construct()
        {
            _start = System.currentTimeMillis();

            try
            {
                if (this.action == Action.FORMAT_ACTIVE)
                {
                    // wait cursor indicates running operation
                    showWaitCursor();
                    beforeStart();

                    ProjectFile activeFile = getActiveProject().getActiveFile();

                    //final Editor editor = activeFile.getEditor();
                    // store the current offset to reposition the caret
                    // (synchronization needed to make Eclipse happy)

                    /*executeSynchron(
                        new Runnable()
                        {
                            public void run()
                            {
                                offset = editor.getCaretPosition();
                            }
                        });*/
                    Jalopy newJalopy = getEngine();
                    format(activeFile, newJalopy);

                    // only change if no errors showed up
                    /*if (getState() != Jalopy.State.ERROR)
                    {
                        // move the cursor

                        **
                         * @todo this could be improved. Determine the location prior
                         *       formatting (relative to the next known node) and set
                         *       the cursor to that position after formatting; quite
                         *       involved, but possible (and only necessary if in
                         *       sorting mode)
                         *
                        executeSynchron(
                            new Runnable()
                            {
                                public void run()
                                {
                                    editor.requestFocus();

                                    if (editor.getLength() > offset)
                                    {
                                        editor.setCaretPosition(offset);
                                    }
                                }
                            });
                    }*/
                    newJalopy.cleanupBackupDirectory();
                }
                else if (this.action == Action.FORMAT_ALL)
                {
                    beforeStart();

                    Jalopy newJalopy = getEngine();
                    formatSeveral(newJalopy, getActiveProject().getAllFiles());
                    newJalopy.cleanupBackupDirectory();
                }
                else if (this.action == Action.FORMAT_SELECTED)
                {
                    beforeStart();

                    Jalopy newJalopy = getEngine();
                    formatSeveral(newJalopy, getActiveProject().getSelectedFiles());
                    newJalopy.cleanupBackupDirectory();
                }
                else if (this.action == Action.FORMAT_OPEN)
                {
                    beforeStart();

                    Jalopy newJalopy = getEngine();
                    formatSeveral(newJalopy, getActiveProject().getOpenedFiles());
                    newJalopy.cleanupBackupDirectory();
                }
            }

            /*catch (InterruptedException ex)
            {
                hideProgressMonitor();
                notifyAll();
            }*/
            catch (Throwable ex)
            {
                hideProgressMonitor();
                displayError(ex, getMainWindow());
                notifyAll();
            }

            return null;
        }


        /**
         * Updates the status bar after a run has finished.
         *
         * @see #getStatusBar
         */
        public void finished()
        {
            try
            {
                if (_lastAction == Action.FORMAT_ACTIVE)
                {
                    hideWaitCursor();
                }
                else
                {
                    hideProgressMonitor();
                }

                StatusBar statusBar = getStatusBar();

                if (statusBar != null)
                {
                    _args[0] =
                        ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                            (getState() == Jalopy.State.ERROR)
                            ? "MSG_FORMAT_FAILED" /* NOI18N */
                            : "MSG_FORMAT_SUCCEEDED" /* NOI18N */);

                    long time = getElapsed();

                    if (time > 999)
                    {
                        time /= 1000;
                        _args[2] =
                            ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                                (time == 1) ? "MSG_SECOND" /* NOI18N */
                                            : "MSG_SECONDS" /* NOI18N */);
                    }
                    else
                    {
                        _args[2] =
                            ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                                "MSG_MILLI_SECONDS" /* NOI18N */);
                    }

                    _args[1] = String.valueOf(time);

                    statusBar.setText(
                        MessageFormat.format(
                            ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                                "MSG_FORMAT_FINISHED" /* NOI18N */), _args));
                }

                AbstractPlugin.this.appender.done();

                // call the user hook
                AbstractPlugin.this.afterEnd();
            }
            finally
            {
                _worker = null;
            }
        }
    }


    /**
     * Helper to format several files in a dedicated thread.
     */
    private class FormatThread
        extends Thread
    {
        Collection files; // Collection of <ProjectFile>
        Jalopy jalopy;

        public FormatThread(
            Jalopy     jalopy,
            Collection files)
        {
            this.files = files;
            this.jalopy = jalopy;
        }

        public void run()
        {
            try
            {
                formatSeveral(jalopy, files, false);
            }
            catch (Exception ex)
            {
                throw new ChainingRuntimeException(ex);
            }
            finally
            {
                synchronized (_lock)
                {
                    _threadCount--;
                    _lock.notify();
                }
            }
        }
    }


    /**
     * GlassPane used to block input whilst formatting the currently active file.
     */
    private class GlassPane
        extends JComponent
        implements AWTEventListener
    {
        Thread thread;

        public void setThread(Thread thread)
        {
            this.thread = thread;
        }


        public void setVisible(boolean visible)
        {
            super.setVisible(visible);

            if (visible)
            {
                Toolkit.getDefaultToolkit().addAWTEventListener(
                    this,
                    AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK
                    | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.TEXT_EVENT_MASK
                    | AWTEvent.INPUT_METHOD_EVENT_MASK);
            }
            else
            {
                Toolkit.getDefaultToolkit().removeAWTEventListener(this);
            }
        }


        public void eventDispatched(AWTEvent ev)
        {
            if (ev instanceof KeyEvent)
            {
                KeyEvent e = (KeyEvent) ev;

                switch (e.getKeyCode())
                {
                    case KeyEvent.VK_ESCAPE :
                        interrupt();

                        break;

                    case KeyEvent.VK_C :

                        if (e.isControlDown())
                        {
                            interrupt();
                        }

                        break;

                    default :
                        e.consume();

                        break;
                }
            }
        }
    }


    /**
     * A concrete progress monitor implemenation for Swing-based applications.
     */
    private final class ProgressMonitorImpl
        implements ProgressMonitor
    {
        JDialog dialog;
        ProgressPanel progressPanel;
        boolean running;
        int progress;

        public ProgressMonitorImpl()
        {
            this.progressPanel = new ProgressPanel();
            this.progressPanel.setProgressBarVisible(true);
        }

        public synchronized void setCanceled(boolean state)
        {
        }


        public synchronized boolean isCanceled()
        {
            return this.progressPanel.isCanceled();
        }


        public synchronized void setProgress(int units)
        {
            if (this.running)
            {
                this.progress = units;
                this.progressPanel.setValue(units);
            }
        }


        public synchronized int getProgress()
        {
            return this.progress;
        }


        public synchronized void setText(String text)
        {
            if (this.running)
            {
                this.progressPanel.setText(text);
            }
        }


        public synchronized void begin(
            String text,
            int    units)
        {
            if (!this.running)
            {
                dialog =
                    new JDialog(
                        getMainWindow(),
                        ResourceBundleFactory.getBundle(BUNDLE_NAME).getString(
                            "TLE_FORMAT_PROGRESS" /* NOI18N */), true);
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.getContentPane().add(this.progressPanel, BorderLayout.CENTER);
                dialog.pack();
                dialog.setLocationRelativeTo(getMainWindow());

                this.progressPanel.setText(text);
                this.progressPanel.setMaximum(units);

                executeAsynchron(
                    new Runnable()
                    {
                        public void run()
                        {
                            running = true;
                            dialog.setVisible(true);
                        }
                    });
            }
        }


        public synchronized void done()
        {
            if (this.running)
            {
                try
                {
                    executeSynchron(
                        new Runnable()
                        {
                            public void run()
                            {
                                progressPanel.setValue(progressPanel.getMaximum());
                            }
                        });
                }
                catch (Throwable ignored)
                {
                    ;
                }

                this.dialog.setVisible(false);
                this.running = false;
                this.progressPanel.dispose();
                this.dialog.dispose();
                this.progressPanel = null;
            }
        }
    }
}
