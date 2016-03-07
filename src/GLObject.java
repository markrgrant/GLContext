// The base class of all OpenGL objects
public class GLObject {

    private int id;
    private boolean deleted;

    public GLObject(int id) {
        this.id = id;
        deleted = false;
    }

    int getId() {
        return id;
    }

    boolean isDeleted() {
        return deleted;
    }

    void delete() {
        deleted = true;
    }
}
