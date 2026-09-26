package practice;

public class Widget {
    private final String id;

    public Widget(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String label() {
        return "Widget";
    }

    public String summary() {
        return label() + "#" + id;
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
