import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LifeLifeClientTest extends Canvas implements Runnable
{
    static int tickrate = 2000;
    static TimeUnit tickUnit = TimeUnit.MILLISECONDS;
    private ScheduledThreadPoolExecutor tick = new ScheduledThreadPoolExecutor(1);
    private LifeLine lifeLine;

    ArrayList<CubicCurve2D> list = new ArrayList<>();
    private int hourStartDisplay = 0;
    private int hourEndDisplay = 5;

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
        q.setCurve(10*jFrame.getSize().width/100, 90*jFrame.getSize().height/100,15*jFrame.getSize().width/100, 85*jFrame.getSize().height/100, 25*jFrame.getSize().width/100, 60*jFrame.getSize().height/100, 30*jFrame.getSize().width/100, 55*jFrame.getSize().height/100);
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

    public static void main(String[] args){
        LifeLifeClientTest test = new LifeLifeClientTest();
        test.lifeLine = testLifeLine();
        test.divideCurveInit();
        test.start();
        while(!test.tick.isTerminated());
    }


    @Override
    public void run() {
        repaint();

    }

    void start() {
        tick.scheduleAtFixedRate(new Thread(this), 0, tickrate, tickUnit);
    }

    Time currentTime = new Time(2018,3,5,10,0);


    @Override
    public void paint(Graphics g) {
        Graphics2D graphics2D = (Graphics2D)g;
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
                    bubble.getCircle().setCenterX((list.get(index).getX1() + list.get(index).getX2()) / 2);
                    bubble.getCircle().setCenterY((list.get(index).getY1() + list.get(index).getY2()) / 2 + 10);
                    Ellipse2D e = new Ellipse2D.Double(bubble.getCircle().getCenterX(), bubble.getCircle().getCenterY(), bubble.getCircle().getRadius() * 2, bubble.getCircle().getRadius() * 2);
                    g.drawString(bubble.getData(), (int) bubble.getCircle().getCenterX(), (int) bubble.getCircle().getCenterY());
                    graphics2D.draw(e);
                    already.add(bubble);
                }
                graphics2D.setColor(defC);
                graphics2D.setStroke(defS);
            }else
                graphics2D.draw(list.get(index));
        }
    }

    private boolean isWithin(Time start, Time end, Time b, int min)
    {
        return ((start.getHour() <= b.getHour() + min/60
            ||((start.getMinute() <= b.getMinute() + min%60) && start.getHour() == b.getHour())))
                &&
                (end.getHour() >= b.getHour() + min/60 || (end.getMinute() >= b.getMinute() + min%60 && end.getHour() == b.getHour()));
    }

}
