package minesweeper.menu;

import minesweeper.difficulty.CustomDifficulty;
import minesweeper.game.Dimensions;
import minesweeper.game.MinesweeperGameManager;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.text.NumberFormatter;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;

import static minesweeper.utility.Icon.ICON;

public class CustomDifficultyMenuItem extends JMenuItem {

    private static final int ROWS_MIN = 5;
    private static final int ROWS_MAX = 25;
    private static final int COLS_MIN = 5;
    private static final int COLS_MAX = 45;
    private static final int BOMB_THRESHOLD = 9;

    private static final NumberFormat format = NumberFormat.getInstance();
    private NumberFormatter rowsFormatter;
    private NumberFormatter colsFormatter;
    private NumberFormatter bombsFormatter;

    public CustomDifficultyMenuItem(Container parentFrame, Runnable onValidCustomDifficultySelection) {
        super("Custom..");

        initFormatters();

        setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.SHIFT_DOWN_MASK));
        addActionListener(ae -> {
            JFormattedTextField rowsField = new JFormattedTextField(rowsFormatter);
            JFormattedTextField colsField = new JFormattedTextField(colsFormatter);
            JFormattedTextField bombsField = new JFormattedTextField(bombsFormatter);

            JLabel errorInfoLabel = new JLabel();
            errorInfoLabel.setForeground(Color.RED);

            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new GridLayout(4, 2));
            inputPanel.add(new JLabel("Rows:"));
            inputPanel.add(rowsField);
            rowsField.setToolTipText("Value must be between 5 and 25");
            inputPanel.add(new JLabel("Columns:"));
            inputPanel.add(colsField);
            colsField.setToolTipText("Value must be between 5 and 45");
            inputPanel.add(new JLabel("Mines:"));
            inputPanel.add(bombsField);
            bombsField.setToolTipText("Value must be less than (Rows X Cols) - 9");
            inputPanel.add(errorInfoLabel);

            // MessageDialog Buttons' Text
            Object[] options = {"Submit", "Cancel"};
            final JOptionPane optionPane = new JOptionPane(inputPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options);
            final javax.swing.JDialog dialog = new javax.swing.JDialog();
            dialog.setTitle("Customize Difficulty");
            dialog.setContentPane(optionPane);

            // Listener for the MessageDialog's Buttons
            optionPane.addPropertyChangeListener(e -> {
                if (JOptionPane.VALUE_PROPERTY.equals(e.getPropertyName())) {
                    // Wait until the user clicks on a button
                    if (optionPane.getValue() == JOptionPane.UNINITIALIZED_VALUE) return;

                    // Clicked on submit
                    if (optionPane.getValue().equals(options[0])) {
                        try {
                            rowsField.commitEdit();
                            colsField.commitEdit();
                            bombsField.commitEdit();

                            MinesweeperGameManager.getInstance().setDifficulty(new CustomDifficulty(
                                    new Dimensions((Integer) rowsField.getValue(), (Integer) colsField.getValue()),
                                    (Integer) bombsField.getValue())
                            );
                            dialog.dispose();
                            onValidCustomDifficultySelection.run();
                        } catch (ParseException ignored) {
                            // If the user presses the same button next time, without this being reset, no property change event will be fired
                            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                            errorInfoLabel.setText("Invalid Input..");
                        }
                    } else dialog.dispose();
                }
            });
            dialog.pack();
            dialog.setIconImage(ICON.getImage());
            dialog.setVisible(true);
            dialog.setLocationRelativeTo(parentFrame);
        });
    }

    private void initFormatters() {
        rowsFormatter = new NumberFormatter(format);
        rowsFormatter.setValueClass(Integer.class);
        rowsFormatter.setMinimum(ROWS_MIN);
        rowsFormatter.setMaximum(ROWS_MAX);

        colsFormatter = new NumberFormatter(format);
        colsFormatter.setValueClass(Integer.class);
        colsFormatter.setMinimum(COLS_MIN);
        colsFormatter.setMaximum(COLS_MAX);

        bombsFormatter = new NumberFormatter(format);
        bombsFormatter.setValueClass(Integer.class);
        bombsFormatter.setMinimum(0);
        bombsFormatter.setMaximum(ROWS_MAX * COLS_MAX - BOMB_THRESHOLD);
    }
}
