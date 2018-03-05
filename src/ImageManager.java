import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ImageManager extends Canvas implements Runnable
{
    ArrayList<CubicCurve2D> list = new ArrayList<>();
    private int hourStartDisplay = 0;
    private LifeLine lifeLine;

    private int hourEndDisplay = 5;
    Time currentTime = new Time(2018,3,5,10,0);

    private boolean isWithin(Time start, Time end, Time b, int min)
    {
        return ((start.getHour() <= b.getHour() + min/60
                ||((start.getMinute() <= b.getMinute() + min%60) && start.getHour() == b.getHour())))
                &&
                (end.getHour() >= b.getHour() + min/60 || (end.getMinute() >= b.getMinute() + min%60 && end.getHour() == b.getHour()));
    }
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

    private LifeLine line = testLifeLine();

    @Override
    public void run(){
        repaint();
        updateDirectory();
        updateImages();
    }
    public static void main(String[] args)
    {
        ImageManager im = new ImageManager();
        im.lifeLine = testLifeLine();
        im.divideCurveInit();
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
        Graphics2D graphics2D = (Graphics2D) g;

        images.values().forEach(e -> graphics2D.drawImage(e.getImage(),
                e.getLocation().x*jFrame.getSize().width/100,
                e.getLocation().y*jFrame.getSize().height/100,
                e.getSize().width*jFrame.getSize().width/100,
                e.getSize().height*jFrame.getSize().height/100, null));
        Set<LifeLine.Bubble> already = new HashSet<>();
        graphics2D.setStroke(new BasicStroke(4));
        for(int index = 0; index < list.size(); ++index) {
            final int ii = index;
            if(lifeLine.getLifeline().stream().anyMatch(e -> isWithin(e.getStartTime(), e.getEndTime(), currentTime, hourEndDisplay*60*ii/list.size())))
            {
                System.out.println(list.get(index).getX1() + " " + list.get(index).getX2());
                LifeLine.Bubble bubble = lifeLine.getLifeline().stream().filter(e -> isWithin(e.getStartTime(), e.getEndTime(), currentTime, hourEndDisplay*60*ii/list.size())).findAny().get();
                Color defC = graphics2D.getColor();
                Stroke defS = graphics2D.getStroke();
                graphics2D.setColor(bubble.getColor());
                graphics2D.draw(list.get(index));
                if(!already.contains(bubble)) {
                    Ellipse2D e = new Ellipse2D.Double((list.get(index).getX1() + list.get(index).getX2()) / 2, (list.get(index).getY1() + list.get(index).getY2()) / 2 + 10,
                            bubble.getEllipse2D().getWidth(), bubble.getEllipse2D().getWidth());
                    g.drawString(bubble.getData(), (int) (list.get(index).getX1() + list.get(index).getX2()) / 2, (int) (list.get(index).getY1() + list.get(index).getY2()) / 2 + 10);
                    graphics2D.draw(e);
                    already.add(bubble);
                }
                graphics2D.setColor(defC);
                graphics2D.setStroke(defS);
            }else
                graphics2D.draw(list.get(index));
        }

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



    public void subdivide()
    {
        ArrayList<CubicCurve2D> li = new ArrayList<>();

        for(int index = 0; index < list.size(); ++index){
            CubicCurve2D s = list.get(index);
            CubicCurve2D l = new CubicCurve2D.Float();
            CubicCurve2D r = new CubicCurve2D.Float();
            CubicCurve2D.subdivide(s, l, r);
            li.add(l);
            li.add(r);
        }
        list = li;
    }
    public void divideCurveInit(){
        CubicCurve2D q = new CubicCurve2D.Float();
        q.setCurve(10*jFrame.getSize().width/100- 10*jFrame.getSize().width/100 + 80*jFrame.getSize().width/100,
                80*jFrame.getSize().height/100 - 20*jFrame.getSize().height/100,
                15*jFrame.getSize().width/100- 10*jFrame.getSize().width/100 + 80*jFrame.getSize().width/100,
                70*jFrame.getSize().height/100 - 20*jFrame.getSize().height/100,
                25*jFrame.getSize().width/100- 10*jFrame.getSize().width/100 + 80*jFrame.getSize().width/100,
                60*jFrame.getSize().height/100 - 20*jFrame.getSize().height/100,
                30*jFrame.getSize().width/100 - 10*jFrame.getSize().width/100 + 80*jFrame.getSize().width/100,
                35*jFrame.getSize().height/100 - 20*jFrame.getSize().height/100);
        list.add(q);
        while(list.size() < hourEndDisplay*60)
            subdivide();
    }



    public static LifeLine testLifeLine(){
        LifeLine lifeLine = new LifeLine();
        lifeLine.addBubble("Hebrew", new Time(2018,3,5,10,0), new Time(2018,3,5,11, 0), 15);
        lifeLine.addBubble("CS 12b", new Time(2018,3,5,11,0), new Time(2018,3,5,12, 0), 15);
        lifeLine.addBubble("Lunch with Squad", new Time(2018,3,5,12,0), new Time(2018,3,5,12, 30), 20);
        lifeLine.addBubble("Do HW", new Time(2018,3,5,13,0), new Time(2018,3,5,14, 30), 20);
        return lifeLine;
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
