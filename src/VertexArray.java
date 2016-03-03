import java.util.HashSet;
import java.util.Set;

// A vertex array object contains a configuration for the loading of data
// from a buffer object into the attributes of a vertex shader. By binding a
// vertex array object to the current context, its configuration will be used
// for data loading (provided the vertex shader inputs have been enabled).

// A vertex object must be created if shader input is being provided from
// buffer object(s).
public class VertexArray extends GLObject {

    private VertexArrayTarget target;
    private Set<Pointer> pointers;

    VertexArray(int id) {
        super(id);
        this.pointers = new HashSet<Pointer>();
    }

    void bind(VertexArrayTarget t) {
        assert target == null;
        target = t;
    }

    void unbind() {
        target = null;
    }

    boolean isBound() {
        return target != null;
    }

    void addPointer(int index, int size, GLType type,
                    boolean normalized, int stride, int offset) {
        pointers.add(new Pointer(index, size, type, normalized, stride,
                offset));
    }

    boolean hasPointers() {
        return pointers.size() > 0;
    }

    private class Pointer {
        int index;
        int size;
        GLType type;
        boolean normalized;
        int stride;
        int offset;

        private Pointer(int index, int size, GLType type, boolean
                normalized, int stride, int offset) {
            this.index = index;
            this.size = size;
            this.type = type;
            this.normalized = normalized;
            this.stride = stride;
            this
                    .offset = offset;
        }
    }

    enum VertexArrayTarget {}

}