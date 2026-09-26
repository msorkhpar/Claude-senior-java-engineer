package practice;

public class ReportGenerationException extends Exception {

    public ReportGenerationException() {
        super("Report generation failed");
    }

    public ReportGenerationException(String message) {
        super(message);
    }

    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportGenerationException(Throwable cause) {
        super(cause);
    }
}
