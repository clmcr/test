import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ImageManager extends Canvas implements Runnable
{
    static final String INFILE_COMMENTS = "#";
    static int tickrate = 2000;
    static TimeUnit tickUnit = TimeUnit.MILLISECONDS;
    private ScheduledThreadPoolExecutor tick = new ScheduledThreadPoolExecutor(1);
    private JFrame jFrame = new JFrame();
    {
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                jFrame.dispose();
            }
        });
        jFrame.add(this);
        jFrame.setSize(1920, 1080);
        jFrame.setVisible(true);
    }

    @Override
    public void run(){
        repaint();
        updateDirectory();
        updateImages();
    }
    public static void main(String[] args)
    {
        ImageManager im = new ImageManager();
        im.start();
        while (!im.tick.isTerminated()) ;
    }
    void start() {
        tick.scheduleAtFixedRate(new Thread(this), 0, tickrate, tickUnit);
    }

    private static final String directoryFileName = "fileDirectory.txt";
    private static final String imgCfgFileName = "imgCfg.txt";
    private static volatile Map<String, File> directory = new HashMap<>();
    private static volatile Map<String, ImageWrapper> images = new HashMap<>();
    static {
        updateDirectory();
        updateImages();
    }
    @Override
    public void paint(Graphics g) {
        Graphics2D graphics2d = (Graphics2D) g;
        images.values().forEach(e -> graphics2d.drawImage(e.getImage(),
                e.getLocation().x*jFrame.getSize().width/100,
                e.getLocation().y*jFrame.getSize().height/100,
                e.getSize().width*jFrame.getSize().width/100,
                e.getSize().height*jFrame.getSize().height/100, null));
    }

    public static void updateImages(){
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(new File(imgCfgFileName)));
            for(String curLine = in.readLine(); curLine != null; curLine = in.readLine()) {
                if(curLine.startsWith(INFILE_COMMENTS))
                    continue;
                String[] lineContents = curLine.split(" ");
                images.put(lineContents[0], new ImageWrapper(ImageIO.read(directory.get(lineContents[0])),
                        new Dimension(Integer.parseInt(lineContents[1]), Integer.parseInt(lineContents[2])),
                        new Point(Integer.parseInt(lineContents[3]), Integer.parseInt(lineContents[4]))));
            }
        }catch (IOException e){
            System.err.println(e.getMessage());
        }finally {
            try{
                in.close();
            }catch (Exception e){}
        }
    }
    public static void updateDirectory(){
        BufferedReader in = null;
        try{
            in = new BufferedReader(new FileReader(new File(directoryFileName)));
            in.lines().filter(e -> !e.startsWith(INFILE_COMMENTS)).map(e -> e.split(" ")).forEach(e -> directory.put(e[0], new File(e[1])));
        }catch (IOException e){
            System.err.println(e.getMessage());
        }finally {
            try{
                in.close();
            }catch (Exception e){}
        }
    }
    private static class ImageWrapper {
        private BufferedImage image;
        private Dimension size;
        private Point location;
        ImageWrapper(BufferedImage image, Dimension size, Point location){
            setImage(image);
            setSize(size);
            setLocation(location);
        }
        public void setLocation(Point location) {
            this.location = location;
        }
        public void setSize(Dimension size) {
            this.size = size;
        }
        public void setImage(BufferedImage image) {
            this.image = image;
        }
        public Point getLocation() {
            return location;
        }
        public BufferedImage getImage() {
            return image;
        }
        public Dimension getSize() {
            return size;
        }
    }
}
