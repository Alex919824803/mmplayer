package utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class Utils {
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    public Utils(){
        mFormatBuilder=new StringBuilder();
        mFormatter=new Formatter(mFormatBuilder, Locale.getDefault());
    }

    public String stringForTime(int timesMs){
        int totalSeconds =timesMs/1000;
        int sedonds=totalSeconds%60;
        int minutes=(totalSeconds/60)%60;
        int hours=totalSeconds/3600;

        mFormatBuilder.setLength(0);
        if (hours>0){
            return mFormatter.format("%d:%02d:%02d",hours,minutes,sedonds).toString();
        }else {
            return mFormatter.format("%02d:%02d",minutes,sedonds).toString();
        }

    }
    public String getSystemTime(){
        SimpleDateFormat format=new SimpleDateFormat("HH:mm");
        String systemTime=format.format(new Date());
        return systemTime;
    }
}
