package utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import domain.Lyric;

public class LyricUtils {
    //歌词集合
    private ArrayList<Lyric> lyrics;

    //根据传入的文件解析歌词
    public void readLyricFile(File file) throws IOException {
        if (file == null || !file.exists()) {
            //歌词文件不存在
        } else {
            //解析歌词
            lyrics = new ArrayList<Lyric>();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(bis, "GBK"));
            String line;
            while ((line = br.readLine()) != null) {
                analyzelLyric(line);
            }
        }
    }

    //解析重复时间点出现的歌词
    private void analyzelLyric(String line) {
        //1.得到左边第一个括号的位置和右边第一个括号的位置
        int pos1 = line.indexOf("[");//0 如果没有的话返回-1
        int pos2 = line.indexOf("[");//9 如果没有的话返回-1
        if (pos1 == 0 && pos2 != -1) {
            //定义long类型数组来装时间戳
            long timpPoints[] = new long[getTagCount(line)];
            String content=line.substring(pos1+1,pos2);
            timpPoints[0]=timeStrToLong(content);
        }
    }

    private long timeStrToLong(String content) {
        return 0;
    }

    //判断有多少句歌词，至少要返回1
    private int getTagCount(String line) {
        int result=0;
        String[] left=line.split("\\[");
        String[] right=line.split("\\]");
        if (left.length==0&&right.length==0){
            result=1;
        }else if (left.length>right.length){
            result=left.length;
        }else {
            result=right.length;
        }
        return result;
    }
}
