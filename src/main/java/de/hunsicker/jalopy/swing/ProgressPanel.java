/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.swing;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.hunsicker.jalopy.storage.Loggers;
import de.hunsicker.swing.util.SwingHelper;
import de.hunsicker.util.ResourceBundleFactory;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;


/**
 * A component that can be used to display progress information to the user.
 * 
 * <p>
 * The panel displays the number of errors, warnings and processed files, along with a
 * short text message and an optional progress bar.
 * </p>
 * <pre>
 * +---------------------------------------------------------------&#043;
 * | Warnings: 2  Errors: 0  Files: 10                             |
 * |                                                               |
 * | Processing ./src/java/de/hunsicker/jalopy/JalopyShell.java    |
 * |                                                               |
 * |  |XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX                   |  |
 * +---------------------------------------------------------------&#043;
 * </pre>
 * 
 * <p>
 * The panel reflects the state of all logger channels as it increases its counters as
 * warnings or errors occurs.
 * </p>
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public class ProgressPanel
    extends JPanel
{
    //~ Instance variables ---------------------------------------------------------------

    /** Appender which listenes on the logger chain to update the progress panel. */
    private transient Appender _progressAppender;

    /** Label to display the errors count. */
    CountLabel _errors;

    /** Label to display the files count. */
    private CountLabel _files;

    /** Label to display the warnings count. */
    CountLabel _warnings;

    /** The cancel button to interrupt a time-consuming operation. */
    private JButton _stopButton;

    /** The current message text. */
    private JLabel _text;

    /** Progress bar to use. */
    private JProgressBar _progressBar;

    /** Did the user pressed the stop button? */
    boolean _isStopped;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ProgressPanel object.
     */
    public ProgressPanel()
    {
        initialize();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Sets whether a stop button should be displayed.
     *
     * @param visible if <code>true</code> a stop button will be displayed.
     */
    public void setCancelButtonVisible(boolean visible)
    {
        _stopButton.setVisible(visible);
    }


    /**
     * Indicates whether the was stopped by the user.
     *
     * @return <code>true</code> if the user pressed the stop button.
     */
    public boolean isCanceled()
    {
        return _isStopped;
    }


    /**
     * Sets the maximum value of the progress indicator.
     *
     * @param value integer &gt; 0
     */
    public void setMaximum(int value)
    {
        _progressBar.setMaximum(value);
    }


    /**
     * Returns the indicators's maximum value.
     *
     * @return the indicators's maximum.
     */
    public int getMaximum()
    {
        return _progressBar.getMaximum();
    }


    /**
     * Returns the indicators's minimum value.
     *
     * @return the indicators's minimum.
     */
    public int getMinimum()
    {
        return _progressBar.getMinimum();
    }


    /**
     * Sets whether a progress bar should be displayed.
     *
     * @param visible if <code>true</code> a progress bar will be displayed.
     */
    public void setProgressBarVisible(boolean visible)
    {
        _progressBar.setVisible(visible);
    }


    /**
     * Sets the text to display.
     *
     * @param text text.
     */
    public void setText(String text)
    {
        _text.setText(text);
    }


    /**
     * Sets the current value of the progress indicator.
     *
     * @param value current value of the progress, integer between 0 and the set maximum.
     */
    public void setValue(int value)
    {
        if (_progressBar != null)
        {
            _progressBar.setValue(value);
        }
    }


    /**
     * Housekeeping method. Call this method if you're sure you don't want or need the
     * instance anymore and want to release the allocated system resources. The object
     * will not be usable hereafter.
     */
    public void dispose()
    {
        Loggers.ALL.removeAppender(String.valueOf(hashCode()));
        _progressAppender = null;
        _progressBar = null;
        _stopButton = null;
        _errors = null;
        _warnings = null;
        _files = null;
        _text = null;
    }


    /**
     * Increases the error count.
     */
    public void increaseErrors()
    {
        _errors.increase();
    }


    /**
     * Increases the file count.
     */
    public void increaseFiles()
    {
        _files.increase();
    }


    /**
     * Increases the warnings count.
     */
    public void increaseWarnings()
    {
        _warnings.increase();
    }


    /**
     * Resets all counters and the progress bar.
     */
    public void reset()
    {
        _errors.reset();
        _warnings.reset();
        _files.reset();
        _progressBar.setValue(0);
        _progressBar.setMaximum(0);
        _isStopped = false;
    }


    /**
     * Initializes the UI.
     */
    private void initialize()
    {
        ResourceBundle bundle =
            ResourceBundleFactory.getBundle(
                "de.hunsicker.jalopy.swing.Bundle" /* NOI18N */);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(layout);

        _errors = new CountLabel(bundle.getString("LBL_ERRORS" /* NOI18N */));
        c.insets.top = 10;
        c.insets.left = 10;
        SwingHelper.setConstraints(
            c, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(_errors, c);
        add(_errors);

        _warnings = new CountLabel(bundle.getString("LBL_WARNINGS" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(_warnings, c);
        add(_warnings);

        _files = new CountLabel(bundle.getString("LBL_FILES" /* NOI18N */));
        SwingHelper.setConstraints(
            c, 2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(_files, c);
        add(_files);

        _stopButton = SwingHelper.createButton(bundle.getString("BTN_STOP" /* NOI18N */));
        _stopButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    _isStopped = true;
                }
            });
        c.insets.right = 10;
        SwingHelper.setConstraints(
            c, 3, 0, 4, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(_stopButton, c);
        add(_stopButton);

        _text = new JLabel();
        c.insets.top = 15;
        SwingHelper.setConstraints(
            c, 0, 1, 7, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(_text, c);
        add(_text);

        JPanel glue = new JPanel();
        c.insets.top = 20;
        c.insets.bottom = 10;
        SwingHelper.setConstraints(
            c, 0, 2, 7, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
            c.insets, 0, 0);
        layout.setConstraints(glue, c);
        add(glue);

        _progressBar = new JProgressBar();
        c.insets.top = 0;
        SwingHelper.setConstraints(
            c, 0, 3, 7, 1, 1.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
        layout.setConstraints(_progressBar, c);
        add(_progressBar);

        Dimension size = new Dimension(400, 115);
        setMinimumSize(size);
        setPreferredSize(size);

        _progressAppender = new ProgressAppender();
        Loggers.ALL.addAppender(_progressAppender);
    }

    //~ Inner Classes --------------------------------------------------------------------

    /**
     * Increases the warning/error count as these messages occur.
     */
    private final class ProgressAppender
        extends AppenderSkeleton
    {
        public ProgressAppender()
        {
            // the appender depends on the enclosing class
            this.name = String.valueOf(ProgressPanel.this.hashCode());
        }

        public void append(LoggingEvent ev)
        {
            switch (ev.getLevel().toInt())
            {
                case Priority.WARN_INT :
                    _warnings.increase();

                    break;

                case Priority.ERROR_INT :
                case Priority.FATAL_INT :
                    _errors.increase();

                    break;
            }
        }


        public void close()
        {
            Loggers.ALL.removeAppender(this);
        }


        public boolean requiresLayout()
        {
            return false;
        }
    }
}
