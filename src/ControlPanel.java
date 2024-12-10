
import javax.swing.JFrame;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class ControlPanel {
    JFrame frame;
    JPanel panel;

    ControlPanelEventHandler eventHandler;

    JMenuBar menuBar;

    private void add(
            Component c,
            Container container,
            GridBagLayout layout,
            GridBagConstraints g,

            int gridx, int gridy, int gridwidth, int gridheight
    ){

        g.gridx = gridx;
        g.gridy = gridy;

        g.gridwidth = gridwidth;
        g.gridheight = gridheight;

        g.insets = new Insets(5,5,5,5);

        layout.setConstraints(c, g);
        container.add(c);
    }

    ControlPanel(ControlPanelEventHandler eventHandler) {
        this.eventHandler = eventHandler;

        frame = new JFrame();

        GridBagConstraints info = new GridBagConstraints();
        info.fill = GridBagConstraints.BOTH;

        GridBagLayout gridLayout = new GridBagLayout();
        //gridLayout.

        panel = new JPanel(gridLayout);

        JLabel label = new JLabel("Max Iterations:");

        SpinnerNumberModel itrModel = new SpinnerNumberModel(1000, 0, 1000000, 1);
        JSpinner spinner = new JSpinner(itrModel);

        gridLayout.setConstraints(spinner, info);

        add(label  , panel, gridLayout, info, 0, 0, 1, 1);
        add(spinner, panel, gridLayout, info, 1, 0, 1, 1);

        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                eventHandler.onMaxItrChange((Integer) spinner.getModel().getValue());
            }
        });

        menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription("File Menu");
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem(
                "Undo Control Panel Action",
                KeyEvent.VK_Z | KeyEvent.CTRL_DOWN_MASK
        );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK
        ));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "This doesn't really do anything");
        menu.add(menuItem);

        menu = new JMenu("Render");
        menu.setMnemonic(KeyEvent.VK_N);
        menu.getAccessibleContext().setAccessibleDescription(
                "This menu does nothing");
        menuBar.add(menu);

        frame.setJMenuBar(menuBar);

        frame.add(panel);
        frame.setSize(300, 100);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        ControlPanel c = new ControlPanel(new ControlPanelEventHandler() {
            @Override
            public void onMaxItrChange(int maxItr) {
                System.out.println("New maxItr: " + maxItr);
            }
        });
    }
}
