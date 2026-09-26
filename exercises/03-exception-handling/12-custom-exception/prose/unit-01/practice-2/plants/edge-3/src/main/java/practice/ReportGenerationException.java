package practice;

public class ReportGenerationException extends Exception {

    public ReportGenerationException() {
        super();
    }

    public ReportGenerationException(String message) {
        this(message, null);
    }

    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportGenerationException(Throwable cause) {
        super(cause);
    }
}
