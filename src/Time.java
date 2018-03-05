import java.time.LocalDateTime;

public class Time implements Comparable<Time>{
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    public Time(int year, int month, int day, int hour, int minute){
        setHour(hour);
        setMinute(minute);
        setYear(year);
        setMonth(month);
        setDay(day);
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    @Override
    public int compareTo(Time o) {
        return (getHour()-o.getHour())*60;
}

    @Override
    public String toString() {
        return String.format("%0.2d:%0.2d %0.2d/%0.2d/%0.4d",getHour(),getMinute(),getMonth(),getDay(),getYear());
    }
}
