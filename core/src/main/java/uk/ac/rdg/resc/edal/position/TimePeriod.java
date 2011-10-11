package uk.ac.rdg.resc.edal.position;

public interface TimePeriod {
    public TimePeriod withSeconds(int seconds);
    public TimePeriod withMinutes(int minutes);
    public TimePeriod withHours(int hours);
    public TimePeriod withDays(int days);
    public TimePeriod withWeeks(int weeks);
    public TimePeriod withMonths(int months);
    public TimePeriod withYears(int years);
    
    public int getSeconds();
    public int getMinutes();
    public int getHours();
    public int getDays();
    public int getWeeks();
    public int getMonths();
    public int getYears();
}
