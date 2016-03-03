import org.lwjgl.opengl.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GLUtil.checkGLError;

// Wrap an OpenGL context, for the purpose of tracking OpenGL context state.
public class GLContext {

    public GLContext() {
        bufferTargets = new HashMap<BufferTarget, Buffer>();
        for (BufferTarget target : BufferTarget.values()) {
            bufferTargets.put(target, null);
        }
        textureTargets = new HashMap<TextureTarget, Texture>();
        for (TextureTarget target : TextureTarget.values()) {
            textureTargets.put(target, null);
        }
        framebufferTargets = new HashMap<FramebufferTarget, Framebuffer>();
        for (FramebufferTarget t : FramebufferTarget.values()) {
            framebufferTargets.put(t, null);
        }
        framebuffers = new HashMap<Integer, Framebuffer>();
        buffers = new HashMap<Integer, Buffer>();
        textures = new HashMap<Integer, Texture>();
        vertexArrays = new HashMap<Integer, VertexArray>();
        programs = new HashMap<Integer, Program>();
        shaders = new HashMap<Integer, Shader>();
        bitplane = new Bitplane();
        defaultFramebuffer = new DefaultFramebuffer();
    }

    // reserve a new buffer object
    public Buffer glGenBuffers() {
        int id = GL15.glGenBuffers();
        checkGLError();
        assert !buffers.containsKey(id);
        Buffer buffer = new Buffer(id);
        buffers.put(id, buffer);
        return buffer;
    }

    public Program glCreateProgram() {
        int programId = GL20.glCreateProgram();
        checkGLError();
        Program p = new Program(programId);
        this.programs.put(p.getId(), p);
        return p;
    }

    public Shader glCreateShader(ShaderType t) {
        int shaderId = GL20.glCreateShader(shaderTypeToGL(t));
        checkGLError();
        Shader s = new Shader(t, shaderId);
        shaders.put(s.getId(), s);
        return s;
    }

    public Framebuffer glGenFramebuffer() {
        int framebufferId = GL30.glGenFramebuffers();
        return new Framebuffer(framebufferId);
    }

    public void glUseProgram(Program p) {
        if (p == null) {
            GL20.glUseProgram(0);
            checkGLError();
            this.program = null;
            return;
        }
        assert p.isLinked();
        this.program = p;
        GL20.glUseProgram(p.getId());
        checkGLError();
    }

    public void glShaderSource(Shader shader, String str) {
        GL20.glShaderSource(shader.getId(), str);
        checkGLError();
        shader.source = str;
    }

    public void glCompileShader(Shader s) {
        assert !s.isCompiled;
        assert s.source != null;
        GL20.glCompileShader(s.getId());
        s.isCompiled = glGetShaderi(s.getId(), GL_COMPILE_STATUS) == 1;
        if (!s.isCompiled) {
            String strInfoLog = glGetShaderInfoLog(s.getId());
            String strShaderType = "";
            switch (s.type) {
                case GL_VERTEX_SHADER:
                    strShaderType = "vertex";
                    break;
                case GL_FRAGMENT_SHADER:
                    strShaderType = "fragment";
                    break;
            }
            String msg = "Compile failure in " + strShaderType +
                    " shader:\n" + strInfoLog + "\n";
            throw new RuntimeException(msg);
        }
    }

    // draw vertices with data obtained from the current vertex array object
    // bound to the vertex array target.
    // s - the DrawMode (such as GL_POINTS, GL_TRIANGLES, etc.)
    // first - the starting index in the vertex array data
    // count - the number of vertex data points to read
    public void glDrawArrays(DrawMode s, int first, int count) {
        // a vertex array object must be bound, it specifies
        // where the vertex data comes from when this function is
        // called.
        assert vertexArrayTarget != null;
        assert vertexArrayTarget.hasPointers();
        GL11.glDrawArrays(drawModeToGL(s), first, count);
        checkGLError();
    }

    public void glClearColor(float r, float g, float b, float a) {
        bitplane.setColor(r, g, b, a);
        GL11.glClearColor(r, g, b, a);
        checkGLError();
    }

    public void glClearBufferfv(FramebufferBuffer buffer, int drawbuffer,
                                float[] color) {
        GL30.glClearBufferfv(fbbToGL(buffer), drawbuffer, FloatBuffer.wrap
                (color));
    }

    public void glLinkProgram(Program p) {
        GL20.glLinkProgram(p.getId());
        boolean isLinked = GL20.glGetProgrami(p.getId(), GL20
                .GL_LINK_STATUS) == 1;
        if (!isLinked) {
            String strInfoLog = GL20.glGetProgramInfoLog(p.getId());
            throw new RuntimeException("Linker failure: " + strInfoLog +
                    "\n");
        }
        p.link();
    }

    public float getPointSize() {
        return pointSize;
    }

    public void glPointSize(float p) {
        assert p >= 0; // TODO: can point size be zero?
        pointSize = p;
        GL11.glPointSize(p);
        checkGLError();
    }

    public void glAttachShader(Program p, Shader s) {
        p.attach(s);
        GL20.glAttachShader(p.getId(), s.getId());
        checkGLError();
    }

    public void glClearDepth(double depth) {
        bitplane.setDepth(depth);
        GL11.glClearDepth(depth);
        checkGLError();
    }

    public void glClearStencil(int stencil) {
        GL11.glClearStencil(stencil);
        checkGLError();
        bitplane.setStencil(stencil);
    }

    // not sure what this does exactly.
    public void glClear(BufferBit[] bufferBits) {
        int total = 0;
        for (BufferBit b : bufferBits) {
            total = total | bufferBitToGL(b);
        }
        GL11.glClear(total);
        checkGLError();
    }

    public void glClear(BufferBit b) {
        GL11.glClear(bufferBitToGL(b));
        checkGLError();
    }

    public void glDeleteBuffers(Buffer buffer) {
        assert buffers.containsKey(buffer.getId());
        assert buffer.getBindings().size() == 0;
        GL15.glDeleteBuffers(buffer.getId());
        checkGLError();
        buffers.remove(buffer.getId());
        buffer.delete();
    }

    // a vertex array object contains mappings from
    // the buffer bound to GL_VERTEX_ARRAY to the attributes
    // of the vertex buffer. After generating a vertex array,
    // it needs to be bound to the vertex array target
    // and populated.
    public VertexArray glGenVertexArrays() {
        int id = GL30.glGenVertexArrays();
        checkGLError();
        assert vertexArrays.get(id) == null;
        VertexArray vertexArray = new VertexArray(id);
        vertexArrays.put(id, vertexArray);
        return vertexArray;
    }

    public void glDeleteVertexArrays(VertexArray a) {
        // OpenGL allows a vertex array to be deleted if it is bound.
        // In that case, the vertex array target is set unbound (to 0).
        // However, to avoid accidents this API requires that the
        // vertex array being deleted already be unbound.
        assert !a.isBound();
        GL30.glDeleteVertexArrays(a.getId());
    }

    public Texture glGenTextures() {
        int id = GL11.glGenTextures();
        checkGLError();
        assert textures.get(id) == null;
        Texture texture = new Texture(id);
        textures.put(id, texture);
        return texture;
    }

    // get the program in use
    public Program getProgram() {
        return this.program;
    }

    public void glBindBuffer(BufferTarget target, Buffer buffer) {
        // assert the buffer target is not already bound.
        if (buffer == null) {
            // binding to null is how unbinding is accomplished in the
            // OpenGL API
            unbindBuffer(target);
            return;
        }
        assert bufferTargets.get(target) == null;
        bufferTargets.put(target, buffer);
        buffer.bind(target);
        GL15.glBindBuffer(bufferTargetToGL(target), buffer.getId());
    }

    private void glBindFramebuffer(FramebufferTarget t, Framebuffer fb) {
        assert !fb.isBound();
        assert framebufferTargets.get(t) == null;
        GL30.glBindFramebuffer(fbtToGL(t), fb.getId());
    }

    private void unbindBuffer(BufferTarget t) {
        Buffer b = bufferTargets.get(t);
        assert b != null;
        bufferTargets.put(t, null);
        b.unbind(t);
    }

    public void glBindTexture(TextureTarget target, Texture texture) {
        GL11.glBindTexture(textureTargetToGL(target), texture.getId());
        checkGLError();
    }

    public int numBuffers() {
        return buffers.size();
    }
    // There is only a single vertex array binding point
    // in the context.  A vertex array object must be bound to this
    // target in order to be modified.
    public void glBindVertexArray(VertexArray o) {
        assert vertexArrayTarget == null;
        vertexArrayTarget = o;
        GL30.glBindVertexArray(o.getId());
        checkGLError();
    }


    public void glBufferData(BufferTarget target, FloatBuffer buffer,
                             BufferUsage usage) {
        assert isBound(target);
        Buffer b = bufferTargets.get(target);
        assert !b.hasData();
        b.addData(buffer.array().length, usage);
        GL15.glBufferData(bufferTargetToGL(target), buffer, usageToGL(usage));
        checkGLError();
    }

    public void glBufferSubData(BufferTarget target, long byteOffset,
                                ByteBuffer data)  {
        assert isBound(target);
        GL15.glBufferSubData(bufferTargetToGL(target), byteOffset, data);
        checkGLError();
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
    public void glCopyBufferSubData(Buffer readTarget, Buffer writeTarget,
                                    int readOffset, int writeoffset, int size) {
        throw new NotImplementedException();
    }

    // define a data source for a particular attribute in the vertex
    // shader.  The data won't actually be copied yet but must be
    // accompanied by a call to glEnableVertexAttribArray().
    // index  - an attribute location assigned by the vertex shader as
    //          obtained by a call to glGetAttribLocation.
    // size   - the size of the attribute.  For example, if the attribute type
    //          is float, and the shader expects 4 floats for position,
    //          then the size would be 4.
    // type   - the data type of the values of the attribute, such as GL_FLOAT.
    // normalized - whether the values have been scaled via w.
    // stride - # of bytes between each attribute value in the data
    // offset - the index of the starting byte of the attribute
    public void glVertexAttribPointer(int index, int size, GLType type,
                                      boolean normalized, int stride,
                                      int offset) {
        // make sure there's a vertex array object that is bound.
        // We want to save this pointer information.
        assert vertexArrayTarget != null;
        vertexArrayTarget.addPointer(index, size, type, normalized, stride,
                offset);
        GL20.glVertexAttribPointer(index, size, typetoGL(type), normalized,
                stride, offset);
        checkGLError();
    }

    // enables the automatic reading of data for a particular attribute
    // from a vertex buffer, as configured by glVertexAttribPointer.
    // A buffer must be
    public void glEnableVertexAttribArray(int index) {

        // A vertex array object must exist and be bound.
        assert vertexArrayTarget != null;

        //assert index in vertexShader.attributes;
        GL20.glEnableVertexAttribArray(index);
        checkGLError();
    }

    private void glDisableVertexAttribArray(int index) {
        GL20.glDisableVertexAttribArray(index);
        checkGLError();
    }

    public boolean isBound(BufferTarget t) {
        return bufferTargets.get(t) != null;
    }

    public boolean isBoundTo(BufferTarget t, Buffer b) {
        return bufferTargets.get(t) == b;
    }

    public boolean isBound(Buffer b) {
        return b.getBindings().size() > 0;
    }

    public boolean isBound(Texture t) {
        return t.isBound();
    }

    public boolean isBound(VertexArray v) {
        return v.isBound();
    }

    public boolean isBound(TextureTarget t) {
        return textureTargets.get(t) != null;
    }

    public Set<BufferTarget> getBufferBindings(Buffer b) {
        return b.getBindings();
    }

    public boolean isDeleted(Buffer b) {
        return b.isDeleted();
    }

    public String toString() {
        String s = "Buffer Targets:\n";
        // include buffer target binding status
        for (BufferTarget t : bufferTargets.keySet()) {
            Buffer b = bufferTargets.get(t);
            String bstr = b == null ? "None" : Integer.toString(b.getId());
            s = s.concat(t + "=" + bstr + "\n");
        }
        s = s.concat("\nTexture Targets:\n");
        // include texture target binding status
        for (TextureTarget t : textureTargets.keySet()) {
            Texture tex = textureTargets.get(t);
            String tstr = tex == null ? "None" : Integer.toString(tex.getId());
            s = s.concat(t + "=" + tstr + "\n");
        }
        s = s.concat("\nFramebuffer Targets:\n");
        for (FramebufferTarget fbt : framebufferTargets.keySet()) {
            Framebuffer fb = framebufferTargets.get(fbt);
            String fbstr = fb == null ? "None" : Integer.toString(fb.getId());
            s = s.concat(fbt + "=" + fbstr + "\n");
        }
        s = s.concat("\nVertex Array Target:\n");
        // include vertex array target binding status
        String vastr = vertexArrayTarget == null ? "None" : Integer.toString
                (vertexArrayTarget.getId());
        s = s.concat(vastr + "\n");
        // include existing buffers
        s = s.concat("\nBuffers:\n" + mapToString(buffers));
        // include existing textures
        s = s.concat("\nTextures:\n" + mapToString(textures));
        // include existing vertex arrays
        s = s.concat("\nVertex Arrays:\n" + mapToString(vertexArrays));
        // includeexisting framebuffers
        s = s.concat("\nFramebuffers:\n" + mapToString(framebuffers));
        // the default framebuffer state
        s = s.concat("\nDefault Framebuffer:\n" + defaultFramebuffer);

        s = s.concat("\nPoint Size:\n" + pointSize + "\n");
        // the program in use
        s = s.concat("\nProgram:\n");
        if (program == null) {
            s = s.concat("None");
        } else {
            s += program.toString();
        }
        return s;
    }

    private String mapToString(HashMap<?, ?> map) {
        String s = "";
        if (map.isEmpty()) return s.concat("None\n");
        else {
            for (Object o : map.values()) {
                s = s.concat(o + "\n");
            }
        }
        return s;
    }

    public int glGetAttribLocation(Program p, CharSequence attrib) {
        int loc = GL20.glGetAttribLocation(p.getId(), attrib);
        checkGLError();
        return loc;
    }

    public void glBindFragDataLocation(Program p, int location, String name) {
        GL30.glBindFragDataLocation(p.getId(), location, name);
    }



    private class Bitplane {
        private float red;
        private float green;
        private float blue;
        private float alpha;
        private double depth;
        private int stencil;

        private Bitplane() {
        }

        private void setColor(float r, float g, float b, float a) {
            if (r < 0 || r > 1) throw new IllegalArgumentException();
            if (g < 0 || g > 1) throw new IllegalArgumentException();
            if (b < 0 || b > 1) throw new IllegalArgumentException();
            if (a < 0 || a > 1) throw new IllegalArgumentException();
            red = r;
            green = g;
            blue = b;
            alpha = a;
        }

        private void setDepth(double d) {
            depth = d;
        }

        private void setStencil(int s) {
            stencil = s;
        }

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

    private static int bufferTargetToGL(BufferTarget t) {
        switch(t) {
            case GL_ARRAY_BUFFER:
                return GL15.GL_ARRAY_BUFFER;
            case GL_COPY_READ_BUFFER:
                return GL31.GL_COPY_READ_BUFFER;
            case GL_COPY_WRITE_BUFFER:
                return GL31.GL_COPY_WRITE_BUFFER;
            case GL_ELEMENT_ARRAY_BUFFER:
                return GL15.GL_ELEMENT_ARRAY_BUFFER;
        }
        throw new IllegalArgumentException();
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

    private enum FramebufferBuffer {
        GL_COLOR, GL_STENCIL, GL_DEPTH
    }

    private int fbtToGL(FramebufferTarget t) {
        switch (t) {
            case GL_FRAMEBUFFER:
                return GL30.GL_FRAMEBUFFER;
            case GL_READ_FRAMEBUFFER:
                return GL30.GL_READ_FRAMEBUFFER;
            case GL_DRAW_FRAMEBUFFER:
                return GL30.GL_DRAW_FRAMEBUFFER;
            default:
                throw new IllegalArgumentException();
        }
    }

    private int fbbToGL(FramebufferBuffer b) {
        switch (b) {
            case GL_COLOR:
                return GL11.GL_COLOR;
            case GL_STENCIL:
                return GL11.GL_STENCIL;
            case GL_DEPTH:
                return GL11.GL_DEPTH;
            default:
                throw new IllegalArgumentException();
        }
    }

    private int bufferBitToGL(BufferBit b) {
        switch (b) {
            case GL_COLOR_BUFFER_BIT:
                return GL11.GL_COLOR_BUFFER_BIT;
            case GL_DEPTH_BUFFER_BIT:
                return GL11.GL_DEPTH_BUFFER_BIT;
            case GL_STENCIL_BUFFER_BIT:
                return GL11.GL_STENCIL_BUFFER_BIT;
            default:
                throw new IllegalArgumentException();
        }
    }

    private int drawModeToGL(DrawMode m) {
        switch (m) {
            case GL_POINTS:
                return GL11.GL_POINTS;
            case GL_TRIANGLES:
                return GL11.GL_TRIANGLES;
            case GL_TRIANGLE_FAN:
                return GL11.GL_TRIANGLE_FAN;
            case GL_TRIANGLE_STRIP:
                return GL11.GL_TRIANGLE_STRIP;
            case GL_QUADS:
                return GL11.GL_QUADS;
            case GL_LINES:
                return GL11.GL_LINES;
            case GL_LINE_LOOP:
                return GL11.GL_LINE_LOOP;
            case GL_LINE_STRIP:
                return GL11.GL_LINE_STRIP;
            case GL_QUAD_STRIP:
                return GL11.GL_QUAD_STRIP;
            case GL_POLYGON:
                return GL11.GL_POLYGON;
            default:
                throw new IllegalArgumentException();
        }
    }

    int shaderTypeToGL(ShaderType t) {
        switch (t) {
            case GL_FRAGMENT_SHADER:
                return GL20.GL_FRAGMENT_SHADER;
            case GL_VERTEX_SHADER:
                return GL20.GL_VERTEX_SHADER;
            case GL_GEOMETRY_SHADER:
                return GL32.GL_GEOMETRY_SHADER;
            default:
                throw new IllegalArgumentException();
        }
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

    // bound targets
    private Map<BufferTarget, Buffer> bufferTargets;
    private Map<TextureTarget, Texture> textureTargets;
    private Map<FramebufferTarget, Framebuffer> framebufferTargets;
    private VertexArray vertexArrayTarget;

    // the current bit plane
    private Bitplane bitplane;

    // the currently bound draw framebuffer
    private Framebuffer framebuffer;

    // the point size
    private float pointSize;

    // the program currently in use
    Program program;

    // the default framebuffer
    DefaultFramebuffer defaultFramebuffer;

    // OpenGL objects (bound or unbound)
    private HashMap<Integer, Buffer> buffers;
    private HashMap<Integer, Texture> textures;
    private HashMap<Integer, VertexArray> vertexArrays;
    private HashMap<Integer, Program> programs;
    private HashMap<Integer, Shader> shaders;
    private HashMap<Integer, Framebuffer> framebuffers;
}
