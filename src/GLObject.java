// The base class of all OpenGL objects
public class GLObject {

    private int id;

    public GLObject(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
