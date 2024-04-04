import java.io.FileWriter;
import java.io.IOException;

/**
 * The Logger class is responsible for logging the output and every important information during the parsing or the
 * optimizing process.
 */
public class Logger {

    /**
     * Path where the log files will be created.
     */
    private String path;

    /**
     * StringBuilder object for the logger.
     */
    private final StringBuilder stringBuilder;

    public Logger() {
        this.stringBuilder = new StringBuilder();
    }

    public void log(String string) {
        this.stringBuilder.append(string).append("\n");
    }

    public void emptyLine() {
        this.stringBuilder.append("\n");
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Method to write the finished log file.
     */
    public void writeFile() {
        try {
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(stringBuilder.toString());
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
