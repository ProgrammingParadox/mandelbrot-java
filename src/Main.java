/*

    https://blog.nobel-joergensen.com/2010/02/13/real-time-mandelbrot-in-java-part-1/
    https://blog.nobel-joergensen.com/2010/02/23/real-time-mandelbrot-in-java-%E2%80%93-part-2-jogl/

    https://github.com/SingingBush/jogl-examples/blob/master/src/main/resources/jocl/Mandelbrot.cl

    https://math.hws.edu/graphicsbook/source/jogl/TextureDemo.java

*/

import javax.swing.JFrame;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import java.util.ArrayList;

public class Main extends JPanel implements Runnable, MouseListener, MouseMotionListener {
    private final JFrame frame;

    Thread t;

    int MAX_ITERATIONS = 2000;

    public int frameWidth = 800;
    public int frameHeight = 800;

    // store previous zooms for ctrl+z
    ArrayList<Double[]> previousZooms = new ArrayList<>();
    ArrayList<BufferedImage> previousZoomImages = new ArrayList<>();

    // store undone zooms for ctrl+shift+z/ctrl+y
    ArrayList<Double[]> undoneZooms = new ArrayList<>();
    ArrayList<BufferedImage> undoneZoomImages = new ArrayList<>();


    // viewport max to hold whole fractal nicely
    double X_SCALE_MIN = -2.25;
    double X_SCALE_MAX = 0.75;

    double Y_SCALE_MIN = -1.5;
    double Y_SCALE_MAX = 1.5;

    // viewport size init to whole fractal
    double x_view_min = X_SCALE_MIN;
    double y_view_min = Y_SCALE_MIN;
    double x_view_max = X_SCALE_MAX;
    double y_view_max = Y_SCALE_MAX;

    // cached fractal image
    BufferedImage img;

    public Main(String name, int w, int h){
        frame = new JFrame(name);

        img = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);

        frameWidth = w;
        frameHeight = h;

        frame.add(this);
        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        t = new Thread(this);
        t.start();

        initShortcuts();
    }
    public Main(String name){
        frame = new JFrame(name);

        img = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);

        frame.add(this);
        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        t = new Thread(this);
        t.start();

        initShortcuts();
    }
    public Main(){
        frame = new JFrame("GUI");

        img = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);

        // init frame
        frame.add(this);
        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // events
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        t = new Thread(this);
        t.start();

        initShortcuts();
    }

    private void initShortcuts(){
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        Action undo = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(previousZooms.isEmpty()) return;

                Double[] view = previousZooms.removeLast();

                undoneZooms.add(view);

                x_view_min = view[0];
                y_view_min = view[1];
                x_view_max = view[2];
                y_view_max = view[3];

                undoneZoomImages.add(copyBuffer(img));
                img = previousZoomImages.removeLast();

                repaint(new Rectangle(0, 0, frameWidth, frameHeight));
            }
        };

        KeyStroke ctrlz = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(ctrlz, "undo");
        actionMap.put("undo", undo);

        Action redo = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(undoneZooms.isEmpty()) return;

                Double[] view = undoneZooms.removeLast();

                previousZooms.add(view);

                x_view_min = view[0];
                y_view_min = view[1];
                x_view_max = view[2];
                y_view_max = view[3];

                previousZoomImages.add(copyBuffer(img));
                img = undoneZoomImages.removeLast();

                repaint(new Rectangle(0, 0, frameWidth, frameHeight));
            }
        };

        KeyStroke ctrly = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlsz = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

        inputMap.put(ctrly, "redo");
        inputMap.put(ctrlsz, "redo");

        actionMap.put("redo", redo);
    }

    // initializes
    public void run() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int scrW = (int) screenSize.getWidth();
        int scrH = (int) screenSize.getHeight();

        frameWidth = scrW;
        frameHeight = scrH;

        renderFractal();
        repaint();
    }

    // paint a frame (happens on repaint and stuff)
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2d.setRenderingHints(rh);

        g2d.drawImage(img, 0, 0, null);

        if(dragging){
            int[] box = getBox();

            int dsx = box[0];
            int dex = box[1];
            int dsy = box[2];
            int dey = box[3];

            g2d.setPaint(new Color(25, 50, 100, 255));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(dsx, dsy, dex - dsx, dey - dsy, 0, 0);
        }
    }

    // write a fractal that spans the view coordinates
    // to the buffered image
    private void renderFractal() {
        WritableRaster data = img.getRaster();

        int height = data.getHeight();
        int width = data.getWidth();

        // division is slow, so get it out of the way
        // and make up for it with multiplication
        double dh = 1.0 / height;
        double dw = 1.0 / width;

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                double scaledY = y_view_min + ((y_view_max - y_view_min) * dh) * y;
                double scaledX = x_view_min + ((x_view_max - x_view_min) * dw) * x;

                int[] fill = renderPixel(scaledX, scaledY);

                data.setPixel(x, y, fill);
            }
        }
    }

    // map an x or y coordinate to the mandelbrot set's
    // coordinate plane
    private double scaleX(double x){
        return x_view_min + ((x_view_max - x_view_min) / 800) * x;
    }
    private double scaleY(double y){
        return y_view_min + ((y_view_max - y_view_min) / 800) * y;
    }

    // renders a pixel from the mandelbrot set (returns
    // and int array of the color data
    private int[] renderPixel(double scaledX, double scaledY) {
        double a = 0.0;
        double b = 0.0;
        int itr = 0;
        while(
                (a * a) + (b * b) <= 4 &&
                        itr < MAX_ITERATIONS
        ){
            double temp = (a * a) - (b * b) + scaledX;
            b = 2 * a * b + scaledY;
            a = temp;

            itr++;
        }

        itr = MAX_ITERATIONS - itr;

        int color = (int) (Math.log(itr) * Math.log(1000) * 255);

        return new int[]{color, (int) (color * 0.5), (int) (color * 0.25), 255};
    }

    public static void main(String[] args) {
        Main _ = new Main();
    }

    // handle all the resizing
    int start_x;
    int start_y;
    int end_x;
    int end_y;
    boolean dragging;
    @Override
    public void mousePressed(MouseEvent e) {
        start_x = e.getX();
        start_y = e.getY();
        
        dragging = true;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        end_x = e.getX();
        end_y = e.getY();

        this.repaint(new Rectangle(0, 0, frameWidth, frameHeight));
    }

    // returns a box based on the mouse positions
    // TODO: I don't think it works all the way
    int[] getBox(){
        int dsx = start_x;
        int dex = end_x;
        int dsy = start_y;
        int dey = end_y;
        if(end_x < start_x){
            dsx = end_x;
            dex = start_x;
        }
        if(end_y < start_y){
            dsy = end_y;
            dey = start_y;
        }

        int max = Math.max(dex - dsx, dey - dsy);

        dex = dsx + max;
        dey = dsy + max;

        // (0, 2), (1, 3)
        return new int[]{ dsx, dex, dsy, dey };
    }

    // returns a box in the mandelbrot set's coordinate
    // range based on the view
    double[] getScaledBox(){
        int[] box = getBox();

        int dsx = box[0];
        int dex = box[1];
        int dsy = box[2];
        int dey = box[3];

        double scaledStartX = scaleX(dsx);
        double scaledStartY = scaleY(dsy);

        double scaledEndX = scaleX(dex);
        double scaledEndY = scaleY(dey);

        return new double[]{ scaledStartX, scaledStartY, scaledEndX, scaledEndY };
    }

    public BufferedImage copyBuffer(BufferedImage h){
        ColorModel cm = h.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = h.copyData(null);

        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        end_x = e.getX();
        end_y = e.getY();
        dragging = false;

        int[] box = getBox();
        if((box[1] - box[0]) * (box[3] - box[2]) < 50){
            dragging = false;

            return;
        }

        double[] scaledBox = getScaledBox();

        // for ctrl + z
        previousZooms.add(new Double[]{x_view_min, y_view_min, x_view_max, y_view_max});
        previousZoomImages.add(copyBuffer(img));

        undoneZooms.clear();
        undoneZoomImages.clear();

        x_view_min = scaledBox[0];
        y_view_min = scaledBox[1];
        x_view_max = scaledBox[2];
        y_view_max = scaledBox[3];

        this.repaint(new Rectangle(0, 0, frameWidth, frameHeight));

        renderFractal();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}