import junit.framework.TestCase;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;

import static java.lang.Thread.sleep;
import static org.lwjgl.glfw.GLFW.*;

public class GLContextTest extends TestCase {

    long window;

    public void testGenBuffers() throws Exception {
        GLContext c = new GLContext();
        Buffer b = c.glGenBuffers();
        assert !c.isBound(b);
    }

    public void testClearBufferfv() {
        GLContext c = new GLContext();
        //c.glClearBufferfv(GL_COLOR, 0, red);
    }

    // a higher level test where point position and color is specified in a
    // buffer, mapped to a vertex shader, which forwards color information
    // to a fragment shader.
    public void testDrawPoint() throws Exception {
        GLContext c = new GLContext();

        // create the data
        Buffer b = c.glGenBuffers();
        c.glBindBuffer(BufferTarget.GL_ARRAY_BUFFER, b);
        float[] points = new float[]{
                0f, 0f, 0f, 1f,
                0f, 0f, 1f, 1f
        };
        FloatBuffer data = BufferUtils.createFloatBuffer(8);
        data.put(points).flip();
        c.glBufferData(BufferTarget.GL_ARRAY_BUFFER, data,
                BufferUsage.GL_STATIC_DRAW);

        // create the shader
        Shader vShader = c.glCreateShader(ShaderType
                .GL_VERTEX_SHADER);
        String vSource = "#version 330 core\n" +
                "in vec4 position;\n" +
                "in vec4 color;\n" +
                "out vec4 Color;\n" +
                "void main() {\n" +
                "gl_Position = position;\n" +
                "Color = color;}";
        c.glShaderSource(vShader, vSource);
        c.glCompileShader(vShader);
        Shader fShader = c.glCreateShader(ShaderType
                .GL_FRAGMENT_SHADER);
        String fSource = "#version 330 core\n" +
                "in vec4 Color;\n" +
                "out vec4 color;\n" +
                "void main() {\n" +
                "color = Color;}";
        c.glShaderSource(fShader, fSource);
        c.glCompileShader(fShader);
        Program p = c.glCreateProgram();
        c.glAttachShader(p, vShader);
        c.glAttachShader(p, fShader);
        c.glLinkProgram(p);
        c.glUseProgram(p);

        int pAttrib = c.glGetAttribLocation(p, "position");
        int cAttrib = c.glGetAttribLocation(p, "color");

        GLContext.VertexArray vao = c.glGenVertexArrays();
        c.glBindVertexArray(vao);

        // specify that the values for the vertex shader attributes
        // should come from the vertex array object.
        c.glEnableVertexAttribArray(pAttrib);
        c.glEnableVertexAttribArray(cAttrib);
        // specify how the data in the buffer attached to
        // GL_VERTEX_BUFFER should be used.
        c.glVertexAttribPointer(pAttrib, 4, GLType.GL_FLOAT, false,
                32, 0);
        c.glVertexAttribPointer(cAttrib, 4, GLType.GL_FLOAT, false, 32,
                16);
        c.glPointSize(50f);

        // the drawing loop, but we just sleep here to verify
        // the image drawn
        c.glClearColor(1f, 0f, 0f, 1f);
        c.glClear(BufferBit.GL_COLOR_BUFFER_BIT);
        c.glDrawArrays(DrawMode.GL_POINTS, 0, 1);
        glfwSwapBuffers(window);
        glfwPollEvents();
        sleep(5000);
        c.glUseProgram(null);
    }

    public void testClearColor() throws Exception {
        GLContext c = new GLContext();
        c.glClearColor(1f, 0f, 0f, 1f);
        c.glClear(BufferBit.GL_COLOR_BUFFER_BIT);
        glfwSwapBuffers(window);
        glfwPollEvents();
        c.glUseProgram(null);
    }

    // it should be possible to bind a buffer to multiple targets
    public void testBindBufferMultipleTargets() {
        GLContext c = new GLContext();
        Buffer b = c.glGenBuffers();
        BufferTarget t1 = BufferTarget.GL_ARRAY_BUFFER;
        BufferTarget t2 = BufferTarget.GL_ELEMENT_ARRAY_BUFFER;
        c.glBindBuffer(t1, b);
        assert c.isBoundTo(t1, b);
        c.glBindBuffer(t2, b);
        assert c.isBoundTo(t2, b);
    }

    public void testDeleteBuffers() throws Exception {
        GLContext c = new GLContext();
        Buffer b = c.glGenBuffers();
        BufferTarget t = BufferTarget.GL_ARRAY_BUFFER;
        c.glBindBuffer(t, b);
        assert c.isBoundTo(t, b);
        c.glBindBuffer(BufferTarget.GL_ARRAY_BUFFER, null);
        assert !c.isBoundTo(t, b);
        c.glDeleteBuffers(b);
        assert c.isDeleted(b);
    }

    public void testCreateProgram() throws Exception {
        GLContext c = new GLContext();
        Program p = c.glCreateProgram();
    }

    public void testGenVertexArrays() throws Exception {

    }

    public void testGenTextures() throws Exception {

    }

    public void testBindBuffer() throws Exception {

    }

    public void testBindTexture() throws Exception {

    }

    public void testBindVertexArray() throws Exception {

    }

    public void testBufferData() throws Exception {

    }

    public void testPointSize() throws Exception {
        GLContext c = new GLContext();
        c.glPointSize(0.5f);
        assert c.getPointSize() == 0.5f;
    }

    public void testBufferSubData() throws Exception {

    }

    public void testMapBuffer() throws Exception {

    }

    public void testClearBufferSubData() throws Exception {

    }

    public void testCopyBufferSubData() throws Exception {

    }

    public void testVertexAttribPointer() throws Exception {

    }

    public void testToString() throws Exception {
        GLContext c = new GLContext();
        Buffer b = c.glGenBuffers();
        BufferTarget t1 = BufferTarget.GL_ARRAY_BUFFER;
        BufferTarget t2 = BufferTarget.GL_ELEMENT_ARRAY_BUFFER;
        c.glBindBuffer(t1, b);
        assert c.isBoundTo(t1, b);
        c.glBindBuffer(t2, b);
        assert c.isBoundTo(t2, b);
        System.out.println(c.toString());
    }

    @Override
    public void setUp() throws Exception {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, 1);
        glfwWindowHint(GLFW_RESIZABLE, 0);

        window = glfwCreateWindow(200, 200, "", 0, 0);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwSetWindowPos(window, 200, 200);
        GL.createCapabilities();
    }

    @Override
    public void tearDown() throws Exception {
        //super.tearDown();
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}