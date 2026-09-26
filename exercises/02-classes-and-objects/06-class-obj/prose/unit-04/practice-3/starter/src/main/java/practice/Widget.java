package practice;

public class Widget {

    public Widget(String id) {
        throw new UnsupportedOperationException("write the constructor");
    }

    public String getId() {
        throw new UnsupportedOperationException("write getId");
    }

    public String label() {
        throw new UnsupportedOperationException("write label");
    }

    public String summary() {
        throw new UnsupportedOperationException("write summary");
    }

    public static class Button extends Widget {

        public Button(String id, String text) {
            super(id);
        }

        public void setText(String text) {
            throw new UnsupportedOperationException("write setText");
        }
    }
}
