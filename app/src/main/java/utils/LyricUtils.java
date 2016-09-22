package utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
                line = analyzelLyric(line);
            }
        }
    }

    //解析重复时间点出现的歌词
    private String analyzelLyric(String line) {
        //1.得到左边第一个括号的位置和右边第一个括号的位置
        int pos1 = line.indexOf("[");//0 如果没有的话返回-1
        int pos2 = line.indexOf("]");//9 如果没有的话返回-1

        //定义long类型数组来装时间戳
        long timpPoints[] = new long[getTagCount(line)];

        if (pos1 == 0 && pos2 != -1) {

            String contentStr = line.substring(pos1 + 1, pos2);
            timpPoints[0] = timeStrToLong(contentStr);
            if (timpPoints[0] == -1) {
                return "";
            }
            String content = line;//[02:04.12][03:37.12][00:59.73] 11111111
            int i = 1;
            //当这个循环结束的时候时间戳都得到了
            while (pos1 == 0 && pos2 != -1) {

                content = content.substring(pos2 + 1);//[03:37.12][00:59.73] 11111111
                pos1 = line.indexOf("[");//0 如果没有的话返回-1
                pos2 = line.indexOf("]");//9 如果没有的话返回-1
                if (pos2 != -1) {
                    contentStr = content.substring(pos1 + 1, pos2);//03:37.12
                    timpPoints[i] = timeStrToLong(content);

                    if (timpPoints[i] == -1) {
                        return "";
                    }
                    i++;
                }
            }
            Lyric lyric = new Lyric();
            for (int j = 0; j < timpPoints.length; j++) {
                if (timpPoints[j] != 0) {
                    lyric.setContent(content);//111111
                    lyric.setTimePoint(timpPoints[j]);//时间戳
                    lyrics.add(lyric);
                    lyric = new Lyric();
                }
            }
            return content;
        }
        return "";
    }

    //把01:23.45转换成毫秒
    private long timeStrToLong(String content) {
        //把01:23.45拆成01和23.45
        //把23.45拆成23和45
        long result = 0;
        try {
            String[] s1 = content.split(":");//01和23.45
            String[] s2 = s1[1].split("\\.");//23和45
            //分
            long min = Long.valueOf(s1[0]);
            //秒
            long second = Long.valueOf(s2[0]);
            //毫秒
            long mil = Long.valueOf(s2[1]);
            result = min * 60 * 1000 + second * 1000 + mil * 10;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
        return result;
    }

    //判断有多少句歌词，至少要返回1
    private int getTagCount(String line) {
        int result = 0;
        String[] left = line.split("\\[");
        String[] right = line.split("\\]");
        if (left.length == 0 && right.length == 0) {
            result = 1;
        } else if (left.length > right.length) {
            result = left.length;
        } else {
            result = right.length;
        }
        return result;
    }
}
