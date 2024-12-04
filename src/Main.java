
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

public class Main extends JPanel implements Runnable, MouseListener {
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

        t = new Thread(this);
        t.start();
    }
    public Main(){
        frame = new JFrame("GUI");

        img = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);

        frame.add(this);
        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

        WritableRaster data = img.getRaster();

        int height = data.getHeight();
        int width = data.getWidth();

        double dh = 1.0 / height;
        double dw = 1.0 / width;

        double ww = (Math.abs(X_SCALE_MAX) + Math.abs(X_SCALE_MIN));
        double hh = (Math.abs(Y_SCALE_MAX) + Math.abs(Y_SCALE_MIN));
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){

                double scaledX = ((x * dw) * ww) + X_SCALE_MIN;
                double scaledY = ((y * dh) * hh) + Y_SCALE_MIN;

                //double scaledX = 0;
                //double scaledY = 0;

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

                int[] fill = new int[]{color, color, color, 255};

                data.setPixel(x, y, fill);
            }
        }


        // draw
//        g2d.setPaint(new Color(50, 100, 200, 255));
//        g2d.fillRoundRect(0, 0, 20, 20, 4, 4);
//
//        g2d.setPaint(new Color(25, 50, 100, 255));
//        g2d.setStroke(new BasicStroke(4));
//        g2d.drawRoundRect(0, 0, 20, 20, 4, 4);

        g2d.drawImage(img, 0, 0, null);
    }

    public static void main(String[] args) {
        Main g = new Main();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}