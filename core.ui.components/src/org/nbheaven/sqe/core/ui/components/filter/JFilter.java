/* Copyright 2005,2006 Sven Reimers, Florian Vogler
 *
 * This file is part of the Software Quality Environment Project.
 *
 * The Software Quality Environment Project is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 2 of the License, or (at your option) any later version.
 *
 * The Software Quality Environment Project is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.nbheaven.sqe.core.ui.components.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * A universal filtering component heavily inspired by NetBeans
 * profiler UI
 *
 * @author Sven Reimers
 */
public class JFilter extends JPanel {
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle(
            "org.nbheaven.sqe.core.ui.components.filter.Bundle"); // NOI18N
    private static final String DEFAULT_TEXTFIELD_STRING = messages.getString(
            "FilterComponent_DefaultTextFieldString"); // NOI18N
    private static final String INVALID_REGEXP_STRING = messages.getString(
            "FilterComponent_InvalidRegExpString"); // NOI18N
    private static final String FILTER_TYPE_TOOLTIP = messages.getString(
            "FilterComponent_FilterTypeToolTip"); // NOI18N
    private static final String FILTER_VALUE_TOOLTIP = messages.getString(
            "FilterComponent_FilterValueToolTip"); // NOI18N
    private static final String SET_FILTER_BUTTON_TOOLTIP = messages.getString(
            "FilterComponent_SetFilterButtonToolTip"); // NOI18N
    private static final String CLEAR_FILTER_BUTTON_TOOLTIP = messages.getString(
            "FilterComponent_ClearFilterButtonToolTip"); // NOI18N
    private static final String FILTER_STRING_COMBO_ACCESS_NAME = messages.getString(
            "FilterComponent_FilterStringComboAccessName"); // NOI18N
    private static final String FILTER_STRING_COMBO_ACCESS_DESCR = messages.getString(
            "FilterComponent_FilterStringComboAccessDescr"); // NOI18N
    private static final String ACCESS_NAME = messages.getString("FilterComponent_AccessName"); // NOI18N
    private static final String ACCESS_DESCR = messages.getString("FilterComponent_AccessDescr"); // NOI18N
    // -----
    private static final Color DEFAULT_TEXTFIELD_FOREGROUND = Color.GRAY;
    private static final String EMPTY_STRING = ""; // NOI18N
    private final ImageIcon setFilterIcon = new ImageIcon(getClass().getResource("/org/nbheaven/sqe/core/ui/components/resources/setFilter.png")); // NOI18N
    private final Icon setFilterRolloverIcon = new ButtonBorderIcon(setFilterIcon);
    private final ImageIcon clearFilterIcon = new ImageIcon(getClass().getResource("/org/nbheaven/sqe/core/ui/components/resources/clearFilter.png")); // NOI18N
    private final Icon clearFilterRolloverIcon = new ButtonBorderIcon(clearFilterIcon);
    private Color textFieldForeground;
    private JButton clearFilterButton;
    private JButton filterTypeButton;
    private JButton setFilterButton;
    private StringComboBox filterStringCombo;
    private JLabel incorrectRegExpLabel;
    private JMenuItem activeFilterItem;
    private JPanel setClearFilterButtonsPanel;
    private JPanel textFieldRegExpWarningPanel;
    private JPopupMenu filterTypePopup;

    //private Vector tmpBufferComboItems;
    private PopupItemsListener popupItemsListener;
    private String filterString = EMPTY_STRING;
    private String textFieldEmptyText = DEFAULT_TEXTFIELD_STRING;
    private Vector<String> filterNames;

    //private Vector filterStringsBuffers; // Originally used as separate buffers for each filter type
    private Vector<String> filterStringsBuffer; // One buffer for all used filter strings
    private Vector<FilterType> filterTypes;
    private Vector<FilterChangedListener> listeners;
    private Vector<Icon> rolloverIcons;
    private Vector<Icon> standardIcons;
    private boolean internalChange = false;
    private boolean textFieldEmptyFlag = true;
    private boolean validRegExpFlag = true;
    private FilterType defaultFilterType = FilterType.NONE;
    private FilterType filterType = FilterType.NONE;
    private FilterType lastFilterType;
    private int nOwnComboItems = 0;

    /** Creates a new instance of FilterComponent */
    public JFilter() {
        super();

        listeners = new Vector<FilterChangedListener>();

        popupItemsListener = new PopupItemsListener();

        standardIcons = new Vector<Icon>();
        rolloverIcons = new Vector<Icon>();
        filterNames = new Vector<String>();
        filterTypes = new Vector<FilterType>();
        //filterStringsBuffers = new Vector();
        filterStringsBuffer = new Vector<String>();

        //tmpBufferComboItems = new Vector();
        lastFilterType = FilterType.UNDEFINED;

        initComponents();
        checkRegExp();
        updateSetClearButtons();
    }

    public FilterType getDefaultFilterType() {
        return defaultFilterType;
    }

    public void setEmptyFilterText(String text) {
        if (text.length() == 0) {
            return;
        }

        String oldTextFieldEmptyText = textFieldEmptyText;
        textFieldEmptyText = text;

        if (filterStringCombo.getText().equals(oldTextFieldEmptyText)) {
            filterStringCombo.setText(textFieldEmptyText);
        }
    }

    //--- Public methods -----
    public void setFilterString(String string) {
        setFilterString(string, true);
    }

    public String getFilterString() {
        return filterString;
    }

    public String[] getFilterStrings() {
        return getFilterStrings(filterString);
    }

    public static String[] getFilterStrings(String string) {
        if (string == null) {
            return null;
        }

        return string.trim().split(" +"); // NOI18N
    }

    public void setDefaultFilterType(FilterType filterType) {
        defaultFilterType = filterType;
        performClearFilterButtonAction();
    }

    public void setFilterType(FilterType filterType) {
        setFilterType(filterType, false);
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterValues(String string, FilterType filterType) {
        setFilterString(string, false);
        setFilterType(filterType, false);
        fireFilterChanged();
        updateSetClearButtons();
    }

    public void addFilterItem(FilterType... filterTypes) {
        for (FilterType filterType : filterTypes) {
            addFilterItem(filterType.getIcon(), filterType.getDisplayName(), filterType);
        }
    }

    public void addFilterItem(ImageIcon icon, String filterName, FilterType filterType) {
        Icon standardIcon = icon;
        Icon rolloverIcon = new ButtonBorderIcon(icon);

        standardIcons.add(standardIcon);
        rolloverIcons.add(rolloverIcon);
        filterNames.add(filterName);
        filterTypes.add(filterType);

        //filterStringsBuffers.add(new Vector());
        JMenuItem menuItem = new JMenuItem();
        menuItem.setText(filterName);
        menuItem.setIcon(standardIcon);
        menuItem.setBackground(Color.WHITE);
        menuItem.addActionListener(popupItemsListener);

        filterTypePopup.add(menuItem);
        filterTypePopup.pack();

        setFilterTypePopupItemActive(menuItem, false);
    }

    //--- FilterChangedListener interface -----
    public void addFilterListener(FilterChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public JSeparator addSeparatorItem() {
        standardIcons.add(null);
        rolloverIcons.add(null);
        filterNames.add(null);
        filterTypes.add(null);

        //filterStringsBuffers.add(null);
        JPopupMenu.Separator separator = new JPopupMenu.Separator();

        separator.setForeground(Color.BLACK);
        separator.setBackground(Color.WHITE);

        filterTypePopup.add(separator);
        filterTypePopup.pack();

        return separator;
    }

    public void removeFilterListener(FilterChangedListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    private JMenuItem getFilterMenuItemByIndex(int index) {
        if (index < filterTypePopup.getComponentCount()) {
            return (JMenuItem) filterTypePopup.getComponent(index);
        } else {
            return null;
        }
    }

    private void setFilterString(String string, boolean fireChange) {
        if (string == null) {
            return;
        }

        string = string.trim();

        if (!filterString.equals(string)) {
            filterString = string;
            filterStringCombo.setText(filterString);
            internalChange = true;
            addComboBoxItem(filterString);
            internalChange = false;

            if (fireChange) {
                boolean textFieldEmptyFlagBkp = textFieldEmptyFlag;
                textFieldEmptyFlag = false;
                fireFilterChanged();
                checkRegExp();
                updateSetClearButtons();
                textFieldEmptyFlag = textFieldEmptyFlagBkp;
            }

            if (filterString.length() > 0) {
                doFocusGained();
            } else {
                doFocusLost();
            }
        }
    }

    private void setFilterType(FilterType filterType, boolean fireChange) {
        if (this.filterType != filterType) {
            for (int i = 0; i < filterTypes.size(); i++) {
                FilterType t = filterTypes.get(i);

                if ((t != null) && (t == filterType)) {
                    setFilterTypePopupItemActive(i, fireChange);

                    return;
                }
            }
        }
    }

    private void setFilterTypePopupItemActive(JMenuItem menuItem, boolean fireChange) {
        int index = filterTypePopup.getComponentIndex(menuItem);

        if (index != -1) {
            setFilterTypePopupItemActive(index, fireChange);
        }
    }

    private void setFilterTypePopupItemActive(int index, boolean fireChange) {
        FilterType newFilterType = filterTypes.get(index);

        if (newFilterType != filterType) {
            activeFilterItem = getFilterMenuItemByIndex(index);

            filterType = newFilterType;

            Icon standardIcon = new ArrowSignIcon((ImageIcon) standardIcons.get(index));
            Icon rolloverIcon = new ButtonBorderIcon(standardIcon);
            String filterName = filterNames.get(index);

            filterTypeButton.setIcon(standardIcon);
            filterTypeButton.setRolloverIcon(rolloverIcon);
            filterTypeButton.setToolTipText(MessageFormat.format(FILTER_TYPE_TOOLTIP,
                    new Object[]{filterName}));

            checkRegExp();

            if (fireChange) {
                fireFilterChanged();
            }

            updateSetClearButtons();
            updateComboItems();
        }
    }

    private int getIndexByCurrentFilterType() {
        return getIndexByFilterType(filterType);
    }

    private int getIndexByFilterType(FilterType type) {
        for (int i = 0; i < filterTypes.size(); i++) {
            FilterType filterType = filterTypes.get(i);

            if ((filterType != null) && (filterType == type)) {
                return i;
            }
        }

        return -1;
    }

    private void addComboBoxItem(String string) {
        if ((string == null) || (string.length() == 0)) {
            return;
        }

        //Vector filterStringsBuffer = (Vector)filterStringsBuffers.get(getIndexByCurrentFilterType());
        if (filterStringsBuffer.contains(string)) {
            filterStringsBuffer.remove(string);
        }

        filterStringsBuffer.add(string);
        updateComboItems();
    }

    private void checkRegExp() {
        if ((filterType != FilterType.REGEXP) || textFieldEmptyFlag) {
            validRegExpFlag = true;
        } else {
            try {
                String[] filters = getFilterStrings(filterStringCombo.getText());

                for (String filter : filters) {
                    EMPTY_STRING.matches(filter);
                }

                validRegExpFlag = true;
            } catch (java.util.regex.PatternSyntaxException e) {
                validRegExpFlag = false;
            }
        }
    }

    private void doFocusGained() {
        textFieldEmptyFlag = false;
        filterStringCombo.comboEditor.setForeground(textFieldForeground);

        if (filterStringCombo.getText().equals(textFieldEmptyText)) {
            filterStringCombo.setText(""); // NOI18N
        }
    }

    private void doFocusLost() {
        if ((filterStringCombo.getText().length() == 0) && (filterString.length() == 0)) {
            textFieldEmptyFlag = true;
            filterStringCombo.setText(textFieldEmptyText);
            filterStringCombo.comboEditor.setForeground(DEFAULT_TEXTFIELD_FOREGROUND);
        }
    }

    private void fireFilterChanged() {
        if (validRegExpFlag) {
            for (int i = 0; i < listeners.size(); i++) {
                (listeners.elementAt(i)).filterChanged();
            }
        }

        lastFilterType = filterType;
    }

    //--- Private implementation -----
    private void initComponents() {
        Color textFieldBackground;

        ActionListener setClearButtonsActionListner = new SetClearButtonsActionListener();

        filterStringCombo = new StringComboBox();

        filterStringCombo.setEditable(true);
        filterStringCombo.setMaximumRowCount(7);
        filterStringCombo.addActionListener(new FilterStringComboActionListener());
        filterStringCombo.getAccessibleContext().setAccessibleName(FILTER_STRING_COMBO_ACCESS_NAME);
        filterStringCombo.getAccessibleContext().setAccessibleDescription(FILTER_STRING_COMBO_ACCESS_DESCR);
        filterStringCombo.setToolTipText(FILTER_VALUE_TOOLTIP);

        JTextComponent filterStringComboEditor = filterStringCombo.comboEditor;

        textFieldBackground = filterStringComboEditor.getBackground();
        textFieldForeground = filterStringComboEditor.getForeground();

        filterStringCombo.setBackground(textFieldBackground);

        filterStringComboEditor.setForeground(DEFAULT_TEXTFIELD_FOREGROUND);
        filterStringComboEditor.setText(textFieldEmptyText);
        filterStringComboEditor.getDocument().addDocumentListener(new FilterTextFieldDocumentListener());
        filterStringComboEditor.addKeyListener(new FilterTextFieldKeyListener());
        filterStringComboEditor.addFocusListener(new FilterTextFieldFocusListener());

        filterTypeButton = new JButton(""); // NOI18N
        filterTypeButton.setFocusable(false);
        filterTypeButton.setBorder(BorderFactory.createEmptyBorder());
        filterTypeButton.setBackground(textFieldBackground);
        filterTypeButton.setContentAreaFilled(false);
        filterTypeButton.addMouseListener(new FilterTypeButtonMouseListener());

        incorrectRegExpLabel = new JLabel(INVALID_REGEXP_STRING);
        incorrectRegExpLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        incorrectRegExpLabel.setBackground(textFieldBackground);
        incorrectRegExpLabel.setForeground(Color.RED);

        textFieldRegExpWarningPanel = new JPanel(new BorderLayout(0, 0));
        textFieldRegExpWarningPanel.setBackground(textFieldBackground);
        textFieldRegExpWarningPanel.add(filterStringCombo, BorderLayout.CENTER);
        textFieldRegExpWarningPanel.add(incorrectRegExpLabel, BorderLayout.EAST);

        setFilterButton = new JButton(""); // NOI18N
        setFilterButton.setIcon(setFilterIcon);
        setFilterButton.setRolloverIcon(setFilterRolloverIcon);
        setFilterButton.setFocusable(false);
        setFilterButton.setBorder(BorderFactory.createEmptyBorder());
        setFilterButton.setBackground(textFieldBackground);
        setFilterButton.setContentAreaFilled(false);
        setFilterButton.addActionListener(setClearButtonsActionListner);
        setFilterButton.setToolTipText(SET_FILTER_BUTTON_TOOLTIP);

        clearFilterButton = new JButton(""); // NOI18N
        clearFilterButton.setIcon(clearFilterIcon);
        clearFilterButton.setRolloverIcon(clearFilterRolloverIcon);
        clearFilterButton.setFocusable(false);
        clearFilterButton.setBorder(BorderFactory.createEmptyBorder());
        clearFilterButton.setBackground(textFieldBackground);
        clearFilterButton.setContentAreaFilled(false);
        clearFilterButton.addActionListener(setClearButtonsActionListner);
        clearFilterButton.setToolTipText(CLEAR_FILTER_BUTTON_TOOLTIP);

        setClearFilterButtonsPanel = new JPanel(new BorderLayout(0, 0));
        setClearFilterButtonsPanel.setBackground(textFieldBackground);
        setClearFilterButtonsPanel.add(setFilterButton, BorderLayout.WEST);
        setClearFilterButtonsPanel.add(clearFilterButton, BorderLayout.EAST);

        filterTypePopup = new JPopupMenu() {

            @Override
            public void setVisible(boolean visible) {
                super.setVisible(visible);

                if (visible) {
                    MenuElement[] me;

                    if (activeFilterItem != null) {
                        me = new MenuElement[]{this, activeFilterItem};
                    } else {
                        me = new MenuElement[]{this};
                    }

                    MenuSelectionManager.defaultManager().setSelectedPath(me);
                }
            }
        };
        filterTypePopup.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        filterTypePopup.setBackground(Color.WHITE);

        setLayout(new BorderLayout());
        setBackground(textFieldBackground);

        add(filterTypeButton, BorderLayout.WEST);
        add(textFieldRegExpWarningPanel, BorderLayout.CENTER);
        add(setClearFilterButtonsPanel, BorderLayout.EAST);

        getAccessibleContext().setAccessibleName(ACCESS_NAME);
        getAccessibleContext().setAccessibleDescription(ACCESS_DESCR);
    }

    private void performClearFilterButtonAction() {
        filterString = EMPTY_STRING;
        filterStringCombo.setText(filterString);
        setFilterType(defaultFilterType, false);
        fireFilterChanged();
        updateSetClearButtons();
    }

    private void performSetFilterButtonAction() {
        filterStringCombo.setText(filterStringCombo.getText().trim());

        String newFilterString = filterStringCombo.getText();

        if ((!newFilterString.equals(filterString)) || (lastFilterType != filterType)) {
            filterString = newFilterString;
            addComboBoxItem(filterString);
            fireFilterChanged();
            updateSetClearButtons();
        }
    }

    private void updateComboItems() {
        String currentFilterString = filterStringCombo.getText();
        filterStringCombo.removeAllItems();

        for (int i = filterStringsBuffer.size() - 1; i >= 0; i--) {
            filterStringCombo.addItem(filterStringsBuffer.get(i));
        }

        filterStringCombo.setText(currentFilterString);
    }

    private void updateSetClearButtons() {
        if (validRegExpFlag) {
            if (incorrectRegExpLabel.isVisible()) {
                incorrectRegExpLabel.setVisible(false);
            }
        } else {
            if (!incorrectRegExpLabel.isVisible()) {
                incorrectRegExpLabel.setVisible(true);
            }

            if (clearFilterButton.isVisible()) {
                clearFilterButton.setVisible(false);
            }

            if (setFilterButton.isVisible()) {
                setFilterButton.setVisible(false);
            }

            updateSetClearFilterButtonsPanelBorder();

            return;
        }

        if (textFieldEmptyFlag) {
            if (clearFilterButton.isVisible()) {
                clearFilterButton.setVisible(false);
            }

            if (setFilterButton.isVisible()) {
                setFilterButton.setVisible(false);
            }

            updateSetClearFilterButtonsPanelBorder();

            return;
        }

        // clearFilterButton
        if (filterString.length() == 0) {
            if (clearFilterButton.isVisible()) {
                clearFilterButton.setVisible(false);
            }
        } else {
            if (!clearFilterButton.isVisible()) {
                clearFilterButton.setVisible(true);
            }
        }

        // setFilterButton
        if (((filterStringCombo.getText().equals(filterString)) && (lastFilterType == filterType)) ||
                (filterStringCombo.getText().length() == 0)) {
            if (setFilterButton.isVisible()) {
                setFilterButton.setVisible(false);
            }
        } else {
            if (!setFilterButton.isVisible()) {
                if (clearFilterButton.isVisible()) {
                    ((BorderLayout) (setClearFilterButtonsPanel.getLayout())).setHgap(1);
                } else {
                    ((BorderLayout) (setClearFilterButtonsPanel.getLayout())).setHgap(0);
                }

                setFilterButton.setVisible(true);
            }
        }

        updateSetClearFilterButtonsPanelBorder();
    }

    private void updateSetClearFilterButtonsPanelBorder() {
        if ((clearFilterButton.isVisible()) || (setFilterButton.isVisible())) {
            setClearFilterButtonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        } else {
            setClearFilterButtonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }
    }

    private class FilterStringComboActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (internalChange) {
                return;
            }

            String string = (String) filterStringCombo.getSelectedItem();

            if ((string == null) || (string.equals(""))) {
                return; // NOI18N
            //if (string == filterStringCombo.comboPopupSeparatorString) return;
            }

            if ((filterStringCombo.comboPopupKeyFlag) || (!filterStringCombo.comboPopupActionFlag)) {
                return;
            }

            setFilterString(string);
            updateSetClearButtons();
        }
    }

    private class FilterTextFieldDocumentListener implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (filterStringCombo.comboPopupActionFlag) {
                return;
            }

            checkRegExp();
            updateSetClearButtons();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (filterStringCombo.comboPopupActionFlag) {
                return;
            }

            checkRegExp();
            updateSetClearButtons();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (filterStringCombo.comboPopupActionFlag) {
                return;
            }

            checkRegExp();
            updateSetClearButtons();
        }
    }

    private class FilterTextFieldFocusListener implements FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
            doFocusGained();
        }

        @Override
        public void focusLost(FocusEvent e) {
            doFocusLost();
        }
    }

    private class FilterTextFieldKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            filterStringCombo.comboPopupKeyFlag = true;

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                performSetFilterButtonAction();
                filterStringCombo.clearSelection();

                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                filterStringCombo.setText(filterString);
                updateSetClearButtons();

                return;
            }

            if (e.getModifiers() == InputEvent.SHIFT_MASK) {
                if ((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) {
                    filterTypePopup.show(JFilter.this, 0 + 1,
                            JFilter.this.getHeight() - 2);

                    return;
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            filterStringCombo.comboPopupKeyFlag = false;
        }
    }

    private class FilterTypeButtonMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            filterTypePopup.show(JFilter.this, 0 + 1, JFilter.this.getHeight() - 2);
        }
    }

    private class PopupItemsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            setFilterTypePopupItemActive((JMenuItem) e.getSource(), false);
            filterStringCombo.requestFocus();
        }
    }

    private class SetClearButtonsActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == setFilterButton) {
                performSetFilterButtonAction();
                filterStringCombo.requestFocus();

                return;
            }

            if (e.getSource() == clearFilterButton) {
                performClearFilterButtonAction();
                filterStringCombo.requestFocus();

                return;
            }
        }
    }
}
