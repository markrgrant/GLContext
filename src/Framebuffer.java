
// a framebuffer object is an OpenGL object which allows for the
// creation of user-defined framebuffers.  With them, one can render
// to non-default framebuffer locations, and thus render without
// disturbing the main screen.
public class Framebuffer extends GLObject {
    private float red;  // the color buffer of the framebuffer
    private float blue;
    private float green;
    private float alpha;
    private int stencil; // the stencil buffer of the framebuffer
    private int depth;   // the depth buffer of the framebuffer
    private FramebufferTarget target; // currently bound target

    Framebuffer(int id) {
        super(id);
        target = null;
    }

    boolean isBound() {
        return target != null;
    }

    public String toString() {
        return ""; // FIXME:
    }
}
