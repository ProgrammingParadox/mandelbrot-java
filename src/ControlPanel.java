
import javax.swing.JFrame;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ControlPanel {
    JFrame frame;
    JPanel panel;

    ControlPanelEventHandler eventHandler;

    double x_view_min;
    double y_view_min;
    double x_view_max;
    double y_view_max;

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

        //g.gridx = GridBagConstraints.RELATIVE;
        //g.gridy = GridBagConstraints.RELATIVE;

        g.gridwidth = gridwidth;
        g.gridheight = gridheight;

        g.insets = new Insets(5,5,5,5);

        layout.setConstraints(c, g);
        container.add(c);
    }

    private void spinner(
            String labelText,

            double initVal, double minVal, double maxVal, double stepSize,

            int gx, int gy, int gw, int gh,

            ChangeListener l
    ) {
        GridBagConstraints info = new GridBagConstraints();
        info.fill = GridBagConstraints.BOTH;

        GridBagLayout gridLayout = new GridBagLayout();

        JLabel label = new JLabel(labelText);

        SpinnerNumberModel itrModel = new SpinnerNumberModel(initVal, minVal, maxVal, stepSize);
        JSpinner itrSpinner = new JSpinner(itrModel);

        gridLayout.setConstraints(itrSpinner, info);

        add(label     , panel, gridLayout, info, gx, gy, gw, gh);
        add(itrSpinner, panel, gridLayout, info, gx + 1, gy, gw, gh);

        itrSpinner.addChangeListener(l);
    }

    ControlPanel(ControlPanelEventHandler eventHandler, Explorer explorer) {
        this.eventHandler = eventHandler;

        frame = new JFrame();

        GridBagConstraints info = new GridBagConstraints();
        info.fill = GridBagConstraints.BOTH;

        GridBagLayout gridLayout = new GridBagLayout();
        //gridLayout.

        panel = new JPanel(gridLayout);

        // max iterations
        spinner(
                "Max Iterations: ",

                1000, 0, 1000000, 1,

                0, 0, 1, 1,

                e -> eventHandler.onMaxItrChange((int) (double) ((JSpinner) e.getSource()).getModel().getValue())
        );

//        // viewport
//        spinner(
//                "X min",
//
//                explorer.x_view_min, explorer.X_SCALE_MIN, explorer.X_SCALE_MAX, 0.0001,
//
//                0, 1, 2, 1,
//
//                e -> eventHandler.onViewChanged(
//                        (Double) ((JSpinner) e.getSource()).getModel().getValue(),
//
//                        explorer.y_view_min,
//                        explorer.x_view_max,
//                        explorer.y_view_max
//                )
//        );


        explorer.setExploreEventHandler(new ExploreEventHandler() {
            @Override
            public void onViewChanged(double sx, double sy, double ex, double ey) {
                x_view_min = sx;
                y_view_min = sy;
                x_view_max = ex;
                y_view_max = ey;
            }
        });

        /*
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
        */

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

            @Override
            public void onViewChanged(double sx, double sy, double ex, double ey) {

            }
        }, new Explorer());
    }
}
