package practice;

public final class SelectBuilder {

    public SelectBuilder from(String table) {
        throw new UnsupportedOperationException("write from");
    }

    public SelectBuilder columns(String... names) {
        throw new UnsupportedOperationException("write columns");
    }

    public SelectBuilder where(String condition) {
        throw new UnsupportedOperationException("write where");
    }

    public SelectBuilder orderBy(String column) {
        throw new UnsupportedOperationException("write orderBy");
    }

    public String build() {
        throw new UnsupportedOperationException("write build");
    }
}
