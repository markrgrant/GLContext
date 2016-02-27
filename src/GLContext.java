import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;
import java.util.ArrayList;

// Wrap an OpenGL context, for the purpose of tracking OpenGL context state.
public class GLContext {

    public GLContext() {
        bufferTargets = new ArrayList<GLBuffer>();
        textureTargets = new ArrayList<GLTexture>();
        buffers = new ArrayList<GLBuffer>();
        textures = new ArrayList<GLTexture>();
        vertexArrays = new ArrayList<GLVertexArray>();
        GL.createCapabilities();
    }

    // reserve a new buffer object
    public GLBuffer glGenBuffers() {
        int id = GL15.glGenBuffers();
        assert buffers.get(id) == null;
        GLBuffer buffer = new GLBuffer(id);
        buffers.add(buffer);
        return buffer;
    }

    public void glDeleteBuffers(GLBuffer buffer) {
        assert buffers.contains(buffer);
        assert !buffer.isBound();
        GL15.glDeleteBuffers(buffer.getId());
    }

    // a vertex array object specifies a mapping from
    // the buffer bound to GL_VERTEX_ARRAY to the attributes
    // of the vertex buffer.
    public GLVertexArray glGenVertexArrays() {
        int id = GL30.glGenVertexArrays();
        assert vertexArrays.get(id) == null;
        GLVertexArray vertexArray = new GLVertexArray(id);
        vertexArrays.add(vertexArray);
        return vertexArray;
    }

    public GLTexture glGenTextures() {
        int id = GL11.glGenTextures();
        assert textures.get(id) == null;
        GLTexture texture = new GLTexture(id);
        textures.add(texture);
        return texture;
    }

    public void glBindBuffer(BufferTarget target, GLBuffer buffer) {
        // assert the buffer target is not already bound
        assert bufferTargets.get(target.ordinal()) == null;
        // assert that the buffer is not already bound
        assert !buffer.isBound();
        bufferTargets.add(target.ordinal(), buffer);
        buffer.bind(target);
        GL15.glBindBuffer(bufferTargetToGL(target), buffer.getId());
    }

    public void glBindTexture(TextureTarget target, GLTexture texture) {
        GL11.glBindTexture(textureTargetToGL(target), texture.getId());
    }

    // There is only a single vertex array binding point
    // in the context, but they must still be bound in
    // order to be modified.
    public void glBindVertexArray(GLVertexArray o) {
        assert vertexArrayTarget == null;
        vertexArrayTarget = o;
        GL30.glBindVertexArray(o.getId());
    }

    public void glBufferData(BufferTarget target, ByteBuffer buffer,
                             BufferUsage usage) {
        assert targetIsBound(target);
        GL15.glBufferData(bufferTargetToGL(target), buffer, usageToGL(usage));
    }

    public void glBufferSubData(BufferTarget target, long byteOffset,
                                ByteBuffer data)  {
        assert targetIsBound(target);
        GL15.glBufferSubData(bufferTargetToGL(target), byteOffset, data);
    }

    // copy data from an existing data structure by providing a pointer
    // to the data structure
    public void glMapBuffer() {
        //GL15.glMapBuffer();
        throw new NotImplementedException();
    }

    // overwrite the entire buffer with the data
    public void glClearBufferSubData() {
        //GL43.glClearBufferSubData();
        throw new NotImplementedException();
    }

    // copy data from one buffer to another.  It is recommended to use
    // the GL_COPY_READ_BUFFER target for the readtarget, and the
    // GL_COPY_WRITE_BUFFER for the write target, so that existing targets
    // don't need to be
    public void glCopyBufferSubData(GLBuffer readTarget, GLBuffer writeTarget,
                                    int readOffset, int writeoffset, int size) {
        throw new NotImplementedException();
    }

    // define a data source for a particular attribute in the vertex
    // shader.  The data won't actually be copied yet but must be
    // followed by a call to glEnableVertexAttribArray().
    public void glVertexAttribPointer(int index, int size, GLType type,
                                      boolean normalized, int stride,
                                      ByteBuffer buffer) {
    }

    // enables the automatic reading of data for a particular attribute
    // from a vertex buffer, as configured by glVertexAttribPointer.
    // A buffer must be
    private void glEnableVertexAttribArray(int index) {
        //assert index in vertexShader.attributes;
        GL20.glEnableVertexAttribArray(index);
    }

    private void glDisableVertexAttribArray(int index) {
        GL20.glDisableVertexAttribArray(index);
    }

    private boolean targetIsBound(BufferTarget t) {
        return bufferTargets.get(t.ordinal()) != null;
    }

    private boolean targetIsBound(TextureTarget t) {
        return textureTargets.get(t.ordinal()) != null;
    }

    public String toString() {
        return "";
    }

    private class GLBuffer {
        private int id;
        private BufferTarget target;
        private GLBuffer(int id) {
            this.id = id;
        }
        private int getId() {
            return id;
        }
        private void bind(BufferTarget t) {
            assert target == null;
            target = t;
        }
        private void unbind() {
            target = null;
        }
        private boolean isBound() {
            return target != null;
        }
    }

    private class GLVertexArray {
        private int id;
        private VertexArrayTarget target;

        private GLVertexArray(int id) {
            this.id = id;
        }
        private int getId() {
            return id;
        }
         private void bind(VertexArrayTarget t) {
            assert target == null;
            target = t;
        }
        private void unbind() {
            target = null;
        }
        private boolean isBound() {
            return target != null;
        }
    }

    private class GLTexture {
        private int id;
        private TextureTarget target;

        private GLTexture(int id) {
            this.id = id;
        }
        private int getId() {
            return id;
        }
         private void bind(TextureTarget t) {
            assert target == null;
            target = t;
        }
        private void unbind() {
            target = null;
        }
        private boolean isBound() {
            return target != null;
        }
    }

    private enum TextureTarget {
        GL_TEXTURE_1D, GL_TEXTURE_2D, GL_TEXTURE_3D, GL_TEXTURE_RECTANGLE,
        GL_TEXTURE_1D_ARRAY, GL_TEXTURE_2D_ARRAY, GL_TEXTURE_CUBE_MAP,
        GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_BUFFER, GL_TEXTURE_2D_MULTISAMPLE,
        GL_TEXTURE_2D_MULTISAMPLE_ARRAY
    }

    private int textureTargetToGL(TextureTarget t) {
        switch(t) {
            case GL_TEXTURE_1D: return GL11.GL_TEXTURE_1D;
            case GL_TEXTURE_2D: return GL11.GL_TEXTURE_2D;
            case GL_TEXTURE_3D: return GL11.GL_TEXTURE_2D;
            case GL_TEXTURE_RECTANGLE: return GL31.GL_TEXTURE_RECTANGLE;
            case GL_TEXTURE_1D_ARRAY: return GL30.GL_TEXTURE_1D_ARRAY;
            case GL_TEXTURE_2D_ARRAY: return GL30.GL_TEXTURE_2D_ARRAY;
            case GL_TEXTURE_CUBE_MAP: return GL13.GL_TEXTURE_CUBE_MAP;
            case GL_TEXTURE_CUBE_MAP_ARRAY: return GL40
                    .GL_TEXTURE_CUBE_MAP_ARRAY;
            case GL_TEXTURE_BUFFER: return GL31.GL_TEXTURE_BUFFER;
            case GL_TEXTURE_2D_MULTISAMPLE: return GL32
                    .GL_TEXTURE_2D_MULTISAMPLE;
            case GL_TEXTURE_2D_MULTISAMPLE_ARRAY: return GL32
                    .GL_TEXTURE_2D_MULTISAMPLE_ARRAY;
        }
        throw new IllegalArgumentException();
    }

    enum BufferTarget {
        GL_ARRAY_BUFFER, GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER
    }

    enum VertexArrayTarget {
    }

    private int bufferTargetToGL(BufferTarget t) {
        switch(t) {
            case GL_ARRAY_BUFFER:
                return GL15.GL_ARRAY_BUFFER;
            case GL_COPY_READ_BUFFER:
                return GL31.GL_COPY_READ_BUFFER;
            case GL_COPY_WRITE_BUFFER:
                return GL31.GL_COPY_WRITE_BUFFER;
        }
        throw new IllegalArgumentException();
    }

    enum BufferUsage {
        GL_STREAM_DRAW, GL_STREAM_READ, GL_STREAM_COPY, GL_STATIC_DRAW,
        GL_STATIC_READ, GL_STATIC_COPY, GL_DYNAMIC_DRAW, GL_DYNAMIC_READ,
        GL_DYNAMIC_COPY
    }

    private int usageToGL(BufferUsage u) {
        switch(u) {
            case GL_STREAM_DRAW: return GL15.GL_STREAM_DRAW;
            case GL_STREAM_READ: return GL15.GL_STREAM_READ;
            case GL_STREAM_COPY: return GL15.GL_STREAM_COPY;
            case GL_STATIC_DRAW: return GL15.GL_STATIC_DRAW;
            case GL_STATIC_READ: return GL15.GL_STATIC_READ;
            case GL_STATIC_COPY: return GL15.GL_STATIC_COPY;
            case GL_DYNAMIC_DRAW: return GL15.GL_DYNAMIC_DRAW;
            case GL_DYNAMIC_READ: return GL15.GL_DYNAMIC_READ;
            case GL_DYNAMIC_COPY: return GL15.GL_DYNAMIC_COPY;
            default: throw new IllegalArgumentException();
        }
    }

    enum GLType {
        GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, GL_UNSIGNED_SHORT, GL_INT,
        GL_UNSIGNED_INT, GL_FLOAT, GL_DOUBLE
    }

    private int typetoGL(GLType t) {
        switch(t) {
            case GL_BYTE:
                return GL11.GL_BYTE;
            case GL_UNSIGNED_BYTE:
                return GL11.GL_UNSIGNED_BYTE;
            case GL_UNSIGNED_SHORT:
                return GL11.GL_UNSIGNED_SHORT;
            case GL_INT:
                return GL11.GL_UNSIGNED_INT;
            case GL_FLOAT:
                return GL11.GL_FLOAT;
            default:
                throw new IllegalArgumentException();
        }
    }

    // for simple testing
    public static void main(String[] args) {
        glfwInit();
        long window = glfwCreateWindow(400,400,"hello", 0l, 0l);
        GLFWKeyCallback keyCallback;
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action,
                               int mods) {
                if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, GLFW_TRUE);
            }
        });

        glfwMakeContextCurrent(window);

        // there is now a current OpenGL context.  Let's track the state
        // with our wrapper context.
        GLContext context = new GLContext();
        context.glEnableVertexAttribArray(0);
        while(glfwWindowShouldClose(window) == 0) {

            glfwPollEvents();
        }
        keyCallback.release();
    }

    // bound targets
    private ArrayList<GLBuffer> bufferTargets = new ArrayList<GLBuffer>();
    private ArrayList<GLTexture> textureTargets = new
            ArrayList<GLTexture>();
    private GLVertexArray vertexArrayTarget;

    // OpenGL objects (bound or unbound)
    private ArrayList<GLBuffer> buffers;
    private ArrayList<GLTexture> textures;
    private ArrayList<GLVertexArray> vertexArrays;

}

