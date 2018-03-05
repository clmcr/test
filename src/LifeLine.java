
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class LifeLine
{
    private List<Bubble> lifeline = new ArrayList<>();
    private static Color[] colors = {Color.RED, Color.PINK, Color.BLUE, Color.CYAN, Color.GREEN};
    private static Color lastColor = Color.WHITE;

    public List<Bubble> getLifeline() {
        return lifeline;
    }

    public void addBubble(String data, Time startTime, Time endTime, int size){
        Color color = colors[(int)(Math.random()*colors.length)];
        while(color.equals(lastColor))
            color = colors[(int)(Math.random()*colors.length)];
        lifeline.add(new Bubble(data, startTime, endTime, size, color));
        lastColor = color;
    }

     class Bubble {
        private String data;
        private Time startTime;
        private Time endTime;
        private int size;
        private Ellipse2D circle;
        private Color color;
        private int length;
        public Bubble(String data, Time startTime, Time endTime, int size, Color color){
            setData(data);
            setStartTime(startTime);
            setEndTime(endTime);
            setSize(size);
            setColor(color);
            setEllipse2D(new Ellipse2D.Double(0,0,size,size));
            setLength(endTime.compareTo(startTime));
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public void setData(String data) {
            this.data = data;
        }

        public void setEllipse2D(Ellipse2D circle) {
            this.circle = circle;
        }

        public void setEndTime(Time endTime) {
            this.endTime = endTime;
        }

        public void setStartTime(Time startTime) {
            this.startTime = startTime;
        }

        public int getSize() {
            return size;
        }

        public Ellipse2D getEllipse2D() {
            return circle;
        }

        public String getData() {
            return data;
        }

        public Time getEndTime() {
            return endTime;
        }

        public Time getStartTime() {
            return startTime;
        }
    }
}
