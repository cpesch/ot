package slash.ot.example;

import slash.ot.Delete;
import slash.ot.Insert;
import slash.ot.Operation;
import slash.ot.client.Client;
import slash.ot.client.ClientListener;
import slash.ot.client.ProxyContent;
import slash.ot.server.Server;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class TextEditor {
    private JFrame frame;
    private JPanel panel;
    private JTextField userField;
    private JButton connectButton;
    private JFormattedTextField intervalField;
    private JComboBox contentsComboBox;
    private JButton createButton;
    private JTextPane textPane;
    private JLabel versionLabel;

    private Client client = new Client();
    private Timer updateTimer;
    private Server server;

    public void setServer(Server server) {
        this.server = server;
    }

    public void show() {
        frame = new JFrame("TextEditor");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                System.exit(0);
            }
        });
        frame.getContentPane().add(panel);
        frame.setSize(400, 500);
        frame.setVisible(true);

        userField.setText(System.getProperty("user.name", "user") + new Random().nextInt());
        intervalField.setValue(5000);
        initializeUIControls();

        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });

        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                create();
            }
        });

        contentsComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() != ItemEvent.SELECTED)
                    return;

                Long contentId = (Long) e.getItem();
                final ProxyContent content = client.getContent(contentId);
                versionLabel.setText(Long.toString(content.getVersion()));
                // get out of the AWT Eventqueue
                new Thread(new Runnable() {
                    public void run() {
                        textPane.setText(content.getText());
                    }
                }).start();
                startTimer();
            }
        });

        client.addClientListener(new ClientListener() {
            public void contentsAdded(Collection<Long> contentIds) {
                DefaultComboBoxModel model = (DefaultComboBoxModel) contentsComboBox.getModel();
                for (Long contentId : contentIds) {
                    model.addElement(contentId);
                }
            }

            public void contentUpdated(long contentId, final long version, List<Operation> operations) {
                assert !SwingUtilities.isEventDispatchThread();

                Long selectedContentId = (Long) contentsComboBox.getSelectedItem();
                if (contentId != selectedContentId)
                    return;

                for (Operation operation : operations) {
                    process(operation);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        versionLabel.setText(Long.toString(version));
                    }
                });
            }

            private void process(Operation operation) {
                System.out.println(System.currentTimeMillis() + " client " + client.getClientId() + " processing " + operation); // TODO
                if (operation instanceof Insert) {
                    Insert insert = (Insert) operation;
                    try {
                        textPane.getDocument().insertString(insert.getStartIndex(), insert.getDelta(), null);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }

                } else if (operation instanceof Delete) {
                    Delete delete = (Delete) operation;
                    try {
                        textPane.getDocument().remove(delete.getStartIndex(), delete.getLength());
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }

                } else
                    throw new UnsupportedOperationException("Operation " + operation + " is not supported");
            }
        });

        textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                if (!SwingUtilities.isEventDispatchThread())
                    return;

                try {
                    Long contentId = (Long) contentsComboBox.getSelectedItem();
                    client.insert(contentId, textPane.getDocument().getText(e.getOffset(), e.getLength()), e.getOffset());
                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }

            public void removeUpdate(DocumentEvent e) {
                if (!SwingUtilities.isEventDispatchThread())
                    return;

                Long contentId = (Long) contentsComboBox.getSelectedItem();
                client.delete(contentId, e.getOffset(), e.getOffset() + e.getLength());
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    void startTimer() {
        stopTimer();

        Integer updateInterval = (Integer) intervalField.getValue();
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            public void run() {
                assert !SwingUtilities.isEventDispatchThread();

                if (client.isConnected())
                    client.update();
            }
        }, 0, updateInterval);
    }

    private void stopTimer() {
        if (updateTimer != null)
            updateTimer.cancel();
        updateTimer = null;
    }

    private void connect() {
        if (client.isConnected()) {
            initializeUIControls();
            intervalField.setEditable(true);
            client.disconnect();
            stopTimer();
            connectButton.setText("Connect");
        } else {
            textPane.setEditable(true);
            contentsComboBox.setEnabled(true);
            createButton.setEnabled(true);
            intervalField.setEditable(false);
            create();
            client.connect(server, userField.getText().hashCode());
            startTimer();
            connectButton.setText("Disconnect");
        }
    }

    private void create() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) contentsComboBox.getModel();
        long contentId = new Random().nextLong();
        model.addElement(contentId);
        model.setSelectedItem(contentId);
    }

    private void initializeUIControls() {
        versionLabel.setText("?");
        contentsComboBox.setEnabled(false);
        contentsComboBox.setModel(new DefaultComboBoxModel());
        createButton.setEnabled(false);
        textPane.setText("");
        textPane.setEditable(false);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("User:");
        panel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userField = new JTextField();
        panel.add(userField, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textPane = new JTextPane();
        textPane.setEditable(false);
        scrollPane1.setViewportView(textPane);
        final JLabel label2 = new JLabel();
        label2.setText("Text:");
        panel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Interval:");
        panel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Version:");
        panel.add(label4, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        versionLabel = new JLabel();
        versionLabel.setText("");
        panel.add(versionLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        connectButton = new JButton();
        connectButton.setText("Connect");
        panel.add(connectButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        intervalField = new JFormattedTextField();
        panel.add(intervalField, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Content:");
        panel.add(label5, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        contentsComboBox = new JComboBox();
        panel.add(contentsComboBox, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createButton = new JButton();
        createButton.setText("Create");
        panel.add(createButton, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }
}
