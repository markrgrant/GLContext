import java.util.HashMap;
import java.util.Set;

// A buffer is a linear allocation of memory that can be used for
// a number of purposes.  It is represented by a unique 'name'.
// The memory allocated for a buffer is called its 'data store'.
// OpenGL does not assign types to buffers - a buffer can be used
// for any purpose at any time.
//
// It is common to place vertex data (position, normal vector, color, etc.)
// to the video device for non-immediate-mode rendering.  VBOs offer
// substantial performance gains over immmediate mode rendering because the
// data resides in video device memory rather than system memory and can be
// rendered directly by the video device.
public class Buffer extends GLObject {

    // targets to which this buffer is currently bound
    private HashMap<BufferTarget, Buffer> bindings;
    private boolean isDeleted;
    // the data associated with this buffer.  It is
    // populated by a calling glBufferData on a target
    // to which this buffer is bound.  It may be null.
    private BufferData data;

    // this buffer
    Buffer(int id) {
        super(id);
        bindings = new HashMap<BufferTarget, Buffer>();
        isDeleted = false;
        data = null;
    }

    boolean isDeleted() {
        return isDeleted;
    }

    boolean hasData() {
        return data != null;
    }

    void addData(int dataSize, BufferUsage u) {
        // don't allow accidental overwriting of existing data
        assert !this.hasData();
        this.data = new BufferData(dataSize, u);
    }

    void bind(BufferTarget t) {
        // don't allow overwriting of an existing binding
        assert bindings.get(t) == null;
        bindings.put(t, this);
    }

    void unbind(BufferTarget t) {
        // verify this buffer is bound to the target in question
        assert bindings.get(t) == this;
        bindings.remove(t);
    }

    void delete() {
        // require that all existing bindings have
        // been removed before deleting
        assert bindings.size() == 0;
        isDeleted = true;
    }

    Set<BufferTarget> getBindings() {
        return bindings.keySet();
    }

    public String toString() {
        String s = "(id=" + getId() + ", ";
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

    // Represents the memory bound to a buffer.
    // Any given buffer may or may not have an
    // associated block of memory,
    private class BufferData {
        private int sizeInBytes;
        private BufferUsage usage;

        public BufferData(int sizeInBytes, BufferUsage usage) {
            assert sizeInBytes >= 0;
            this.sizeInBytes = sizeInBytes;
            this.usage = usage;
        }
    }
}
