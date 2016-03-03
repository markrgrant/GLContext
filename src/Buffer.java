import java.util.HashMap;
import java.util.Set;

// A buffer is a linear allocation of memory that can be used for
// a number of purposes.  It is represented by a unique 'name'.
// The memory allocated for a buffer is called its 'data store'.
// OpenGL does not assign types to buffers - a buffer can be used
// for any purpose at any time.
public class Buffer {

    // the buffer 'name'
    private int id;
    // targets to which this buffer is currently bound
    private HashMap<BufferTarget, Buffer> bindings;
    private boolean isDeleted;
    // the data associated with this buffer.  It is
    // populated by a calling glBufferData on a target
    // to which this buffer is bound.  It may be null.
    private BufferData data;

    // this buffer
    public Buffer(int id) {
        this.id = id;
        bindings = new HashMap<BufferTarget, Buffer>();
        isDeleted = false;
        data = null;
    }

    boolean isDeleted() {
        return isDeleted;
    }

    public boolean hasData() {
        return data != null;
    }

    public void addData(int dataSize, BufferUsage u) {
        assert this.data == null;
        this.data = new BufferData(dataSize, u);
    }

    public int getId() {
        return id;
    }

    public void bind(BufferTarget t) {
        bindings.put(t, this);
    }

    public void unbind(BufferTarget t) {
        bindings.remove(t);
    }

    public void delete() {
        assert bindings.size() == 0;
        isDeleted = true;
    }

    public Set<BufferTarget> getBindings() {
        return bindings.keySet();
    }

    public String toString() {
        String s = "(id=" + id + ", ";
        String sb = "";
        if (bindings.isEmpty()) sb = "None";
        else {
            for (BufferTarget b : bindings.keySet()) {
                sb = sb.concat(b.toString() + " ");
            }
        }
        s = s.concat("bindings=" + sb + ")\n");
        return s;
    }

    public class BufferData {
        private int dataSize;
        private BufferUsage usage;

        public BufferData(int dataSize, BufferUsage usage) {
            this.dataSize = dataSize;
            this.usage = usage;
        }
    }
}
