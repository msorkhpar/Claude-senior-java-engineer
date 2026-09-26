package practice;

public class Widget {
    private final String id;
    private final String summary;

    public Widget(String id) {
        this.id = id;
        this.summary = label() + "#" + id;
    }

    public String getId() {
        return id;
    }

    public String label() {
        return "Widget";
    }

    public String summary() {
        return summary;
    }

    public static class Button extends Widget {
        private String text;

        public Button(String id, String text) {
            super(id);
            this.text = text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String label() {
            return "Button:" + text;
        }
    }
}
