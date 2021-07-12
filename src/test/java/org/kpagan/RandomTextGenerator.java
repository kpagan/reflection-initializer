package org.kpagan;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomTextGenerator {

    private static List<String> lines = new ArrayList<>();
    private static Random random = new Random();

    static {
        try {
            lines = Files.readAllLines(new File("src/test/resources/LoremIpsum.txt").toPath(), Charset.defaultCharset() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRandomText(int linesNo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < linesNo; i++) {
            sb.append(lines.get(random.nextInt(lines.size()))).append("\n");
        }
        return sb.toString();
    }
}
