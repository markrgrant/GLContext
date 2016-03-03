public class Texture {
    private int id;
    private TextureTarget target;

    Texture(int id) {
        this.id = id;
    }

    int getId() {
        return id;
    }

    void bind(TextureTarget t) {
        assert target == null;
        target = t;
    }

    void unbind() {
        target = null;
    }

    boolean isBound() {
        return target != null;
    }
}
