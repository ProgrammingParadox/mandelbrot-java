
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

public class Main extends JPanel implements Runnable, MouseListener, MouseMotionListener {
    private JFrame frame;

    Thread t;

    public int frameWidth = 800;
    public int frameHeight = 800;

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

    public String fontName = "Trebuchet MS";
    public int fontSize = 12;

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

        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);

        t = new Thread(this);
        t.start();
    }
    public Main(String name){
        frame = new JFrame(name);

        img = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);

        frame.add(this);
        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);

        t = new Thread(this);
        t.start();
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
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);

        t = new Thread(this);
        t.start();
    }

    public void run() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int scrW = (int)screenSize.getWidth();
        int scrH = (int)screenSize.getHeight();

        frameWidth = scrW;
        frameHeight = scrH;

        // frame.setSize(frameWidth, frameHeight);

        renderFractal();
        repaint();
    }

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

            System.out.println(dsx + ", " + dsy + " | " + dex + ", " + dey);
            System.out.println(start_x + ", " + start_y + " | " + end_x + ", " + end_y);

            g2d.setPaint(new Color(25, 50, 100, 255));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(dsx, dsy, dex - dsx, dey - dsy, 0, 0);
        }
    }

    private void renderFractal() {
        WritableRaster data = img.getRaster();

        int height = data.getHeight();
        int width = data.getWidth();

        double dh = 1.0 / height;
        double dw = 1.0 / width;

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                double scaledY = y_view_min + ((y_view_max - y_view_min) / height) * y;
                double scaledX = x_view_min + ((x_view_max - x_view_min) / width ) * x;

                int[] fill = renderPixel(scaledX, scaledY);

                data.setPixel(x, y, fill);
            }
        }
    }

    private double scaleX(double x){
        return x_view_min + ((x_view_max - x_view_min) / 800) * x;
    }

    private double scaleY(double y){
        return y_view_min + ((y_view_max - y_view_min) / 800) * y;
    }

    private int[] renderPixel(double scaledX, double scaledY) {
        double a = 0.0;
        double b = 0.0;
        int itr = 0;
        while(
                (a * a) + (b * b) <= 4 &&
                        itr < 1000 // (max itr)
        ){
            double temp = (a * a) - (b * b) + scaledX;
            b = 2 * a * b + scaledY;
            a = temp;

            itr++;
        }

        itr = 1000 - itr;

        int color = (int) (Math.log(itr) * Math.log(1000) * 255);

        return new int[]{color, color, color, 255};
    }

    public static void main(String[] args) {
        Main g = new Main();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

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

        return new int[]{dsx, dex, dsy, dey};
    }

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

    @Override
    public void mouseReleased(MouseEvent e) {
        end_x = e.getX();
        end_y = e.getY();
        dragging = false;

        double[] scaledBox = getScaledBox();

        x_view_min = scaledBox[0];
        y_view_min = scaledBox[1];
        x_view_max = scaledBox[2];
        y_view_max = scaledBox[3];

        this.repaint(new Rectangle(0, 0, frameWidth, frameHeight));

        renderFractal();
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