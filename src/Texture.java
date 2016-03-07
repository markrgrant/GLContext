public class Texture extends GLObject {

    private TextureTarget target;

    Texture(int id) {
        super(id);
        target = null;
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

    public TextureTarget getTarget() {
        return target;
    }

    public void setTarget(TextureTarget target) {
        // a texture target should only be set once. It determines the
        // type of the texture thereafter.
        assert this.target == null;
        this.target = target;
    }
}
