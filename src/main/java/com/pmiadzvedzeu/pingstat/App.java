package src.main.java.com.pmiadzvedzeu.pingstat;

import java.io.*;
import java.util.Date;

public class App {
    public static void main(String[] args) {
        String inFilename = "";
        String outFileName = "javaPingOut";
        int delay = 0;
        if (args.length > 0) {
            String USAGE =
                    "USAGE:\n" +
                            "java -jar pingStat.jar [OPTIONS]\n" +
                            "OPTIONS:\n" +
                            "\t-i, --input [filename]:\t\tfile with ping logs (with timestamps)\n" +
                            "\t-o, --output [filename]:\tfile to store new logs (./javaPingOut by default)\n" +
                            "\t-d, --delay [delay]:\t\tminimum delay to show/log int ms\n";

            for (int i = 0; i < args.length; i++) {
                if ((args[i].equals("-i") || args[i].equals("--input")) && args.length > i+1) {
                    inFilename = args[++i];
                } else if ((args[i].equals("-o") || args[i].equals("--output")) && args.length > i+1) {
                    outFileName = args[++i];
                } else if ((args[i].equals("-d") || args[i].equals("--delay")) && args.length > i+1) {
                    delay = Integer.parseInt(args[++i]);
                } else {
                    System.out.println(USAGE);
                    System.exit(31);
                }
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName))) {
            if (inFilename.isBlank()) {
                new App().showRealtimeStat(delay, writer);
            } else {
                new App().showStatFromFile(inFilename, delay, writer);
            }
        } catch (IOException e) {
            System.out.println("IOException in main");
            System.exit(30);
        }
    }

    void showStatFromFile(String fileName, int minDelay, BufferedWriter writer) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            writeAndPrint(br, minDelay, writer);
        } catch (IOException e) {
            System.out.println("IOException in showStatFromFile()");
            System.exit(30);
        }
    }

    void showRealtimeStat(int minDelay, BufferedWriter writer) {
        ProcessBuilder pb = new ProcessBuilder("ping", "-D", "google.com");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(pb.start().getInputStream()))) {
            writeAndPrint(br, minDelay, writer);
        } catch (IOException e) {
            System.out.println("IOException in showRealtimeStat()");
            System.exit(26);
        }
    }

    void writeAndPrint(BufferedReader reader, int minDelay, BufferedWriter writer) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                PingLine pingLine = new PingLine(line);
                String out;
                if ((out = pingLine.drawMoreThan(minDelay)) != null) {
                    System.out.print(out);
                    writer.append(out);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException in show()");
            System.exit(30);
        }
    }
}

class PingLine {
    private Date date;
    private double ms;

    public PingLine(String line) {
        if (line.contains("[") && line.indexOf(".") >= line.indexOf("[")) {
            date = new Date(1000 * Long.parseLong(line.substring(
                    line.indexOf("[") + 1,
                    line.indexOf(".")
            )));
            try {
                ms = Double.parseDouble(line.substring(line.indexOf("time=") + 5, line.indexOf(" ms")));
            } catch (StringIndexOutOfBoundsException e) {
                System.out.println(line);
                ms = 0;
            }
        }
    }

    public String draw() {
        String bar = "";
        for (int i = 0; i < (int)ms/4 && i < 100; i++) {
            bar += "|";
        }
        return date!=null ? String.format("%-30s %-100s %-10s\n", date, bar, ms + " ms") : null;
    }

    public String drawMoreThan(int x) {
        if (ms >= x) {
            return draw();
        }
        return null;
    }
}
