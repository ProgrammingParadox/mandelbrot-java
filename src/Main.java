
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

    public int frameWidth = 1000;
    public int frameHeight = 800;

    public int[] fill = new int[]{0, 0, 0, 255};
    public int[] stroke = new int[]{0, 0, 0, 255};
    public int strokeWeight = 1;

    public String fontName = "Trebuchet MS";
    public int fontSize = 12;

    BufferedImage img;

    public Main(String name, int w, int h){
        frame = new JFrame(name);

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

        frame.add(this);
        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        t = new Thread(this);
        t.start();
    }
    public Main(){
        frame = new JFrame("GUI");

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

        Graphics2D g2d = (Graphics2D)g;

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