import junit.framework.TestCase;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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

    public void testDrawTexture() throws Exception {
        GLContext c = new GLContext();

        // create the texture object
        Texture t = c.glGenTextures();
        // bind the texture to a target in the context
        TextureTarget tgt = TextureTarget.GL_TEXTURE_2D;
        c.glBindTexture(tgt, t);
        // a 256 by 256 image with four channels, each stored as
        // a float
        float[] data = new float[256 * 256 * 4];
        for (int i = 0; i < data.length; i += 4) {
            data[i] = 1f;  // red
            data[i + 1] = 0f;  // green
            data[i + 2] = 0f;  // blue
            data[i + 3] = 1f;  // alpha
        }
        FloatBuffer buff = BufferUtils.createFloatBuffer(data.length);
        buff.put(data);
        buff.flip();
        c.glTexImage2D(tgt, 0, TextureFormat.GL_RGBA, 256, 256,
                TextureFormat.GL_RGBA, GLType.GL_FLOAT, buff);

        // create the vertex object that will contain the triangles into
        // which the texture will be drawn
        // our image data. It's 2 pixels by 2 pixels in a checkerboard pattern.
        // Also included are the texture coordinates for each vertex, where
        // (0,0) is the bottom left of the texture and (1,1) is the top right
        float vertexData[] = {
                // vertex       // texture
                0f, 1f, 0f, 1f, 0f, 1f, // top left texture map
                1f, 1f, 0f, 1f, 1f, 1f, // top right texture map
                1f, 0f, 0f, 1f, 1f, 0f, // bottom right texture map
                0f, 0f, 0f, 1f, 0f, 0f};// bottom left texture map
        FloatBuffer vertices = BufferUtils.createFloatBuffer(16);
        vertices.put(vertexData).flip();

        // create Vertex Array Object and bind it
        VertexArray vao = c.glGenVertexArrays();
        c.glBindVertexArray(vao);

        // create a vertex buffer object
        Buffer vbo = c.glGenBuffers(); // an element buffer object
        c.glBindBuffer(BufferTarget.GL_ARRAY_BUFFER, vbo);
        // copy data into video memory.  Use static mejmory
        // since the vertex data is static
        c.glBufferData(BufferTarget.GL_ARRAY_BUFFER, vertices,
                BufferUsage.GL_STATIC_DRAW);

        // also create an element buffer object that references
        // the vertices in the vbo
        Buffer ebo = c.glGenBuffers(); // an element buffer object
        c.glBindBuffer(BufferTarget.GL_ELEMENT_ARRAY_BUFFER, ebo);
        // copy data into video memory.  Use static mejmory
        // since the vertex data is static

        int[] elementData = new int[]{0, 1, 2, 0, 2, 3};
        IntBuffer elements = BufferUtils.createIntBuffer(6);
        elements.put(elementData).flip();

        c.glBufferData(BufferTarget.GL_ELEMENT_ARRAY_BUFFER, elements,
                BufferUsage.GL_STATIC_DRAW);

        String vShaderStr = "#version 330 core\n" +
                "in vec2 texcoord;\n" +
                "in vec4 position;\n" +
                "out vec2 Texcoord;\n" +
                "void main()\n" +
                "{gl_Position = position;Texcoord = texcoord;}";

        Shader vShader = c.glCreateShader(ShaderType.GL_VERTEX_SHADER);
        c.glShaderSource(vShader, vShaderStr);
        c.glCompileShader(vShader);

        String fShaderStr = "#version 330 core\n" +
                "in vec2 Texcoord;\n" +
                "out vec4 outColor;\n" +
                "uniform sampler2D tex;\n" +
                "void main()\n" +
                "{outColor = texture(tex, Texcoord);}";
        Shader fShader = c.glCreateShader(ShaderType.GL_FRAGMENT_SHADER);
        c.glShaderSource(fShader, fShaderStr);
        c.glCompileShader(fShader);

        Program program = c.glCreateProgram();
        c.glAttachShader(program, vShader);
        c.glAttachShader(program, fShader);

        // must specify the location of the fragment shader output before
        // compiling
        c.glBindFragDataLocation(program, 0, "outColor");

        // link the program
        c.glLinkProgram(program);

        // use the shaders in this program
        c.glUseProgram(program);

        // specify layout of the vertex data
        int posAttrib = c.glGetAttribLocation(program, "position");
        c.glEnableVertexAttribArray(posAttrib);
        // the posAttrib is 4 bytes long, data type is float, the data is
        // already scaled, and the stride is 0.
        c.glVertexAttribPointer(posAttrib, 4, GLType.GL_FLOAT, false, 24, 0);

        int texAttrib = c.glGetAttribLocation(program, "texcoord");
        c.glEnableVertexAttribArray(texAttrib);

        // texture coordinates are 2 floats, vertex data is 24 bytes in size,
        // and the texture coordinates start at byte 16
        int texAttribLength = 2;        // # of floats in a tex coord
        int vertexSizeInBytes = 6 * 4;    // total size of a vertex entry
        int texAttribStartInBytes = 4 * 4;// starting byte of tex coords
        c.glVertexAttribPointer(texAttrib, texAttribLength, GLType.GL_FLOAT,
                false, vertexSizeInBytes, texAttribStartInBytes);

        //while(glfwWindowShouldClose(window) == GL_FALSE) {
        // clear the screen to black
        //c.glClearColor(0f, 0f, 0f, 1f);
        //c.glClear(GL_COLOR_BUFFER_BIT);
        //draw two triangles using the element buffer of length 6, which
        //points to indices in the vertex buffer
        c.glDrawElements(DrawMode.GL_TRIANGLES, 6, GLType.GL_UNSIGNED_INT, 0);
        // clears the frame buffer
        glfwSwapBuffers(window);
        glfwSwapInterval(1);
        glfwPollEvents();
        //}
    }


    // a higher level test where point position and color is specified in a
    // buffer, mapped to a vertex shader, which forwards color information
    // to a fragment shader.
    public void testDrawPoint() throws Exception {
        GLContext c = new GLContext();

        // create the data
        Buffer b = c.glGenBuffers();
        c.glBindBuffer(BufferTarget.GL_ARRAY_BUFFER, b);

        // data for a single point
        float[] points = new float[]{
                0f, 0f, 0f, 1f,  // location
                0f, 0f, 1f, 1f   // color
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

        VertexArray vao = c.glGenVertexArrays();
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
        GLContext c = new GLContext();
        Texture texture = c.glGenTextures();
        assertFalse(texture.isBound());
        assertNull(texture.getTarget());
    }

    public void testBindTexture() throws Exception {
        GLContext c = new GLContext();
        Texture texture = c.glGenTextures();
        c.glBindTexture(TextureTarget.GL_TEXTURE_2D, texture);
        assertTrue(texture.isBound());
        assertTrue(c.isBound(TextureTarget.GL_TEXTURE_2D));
        assertEquals(texture.getTarget(), TextureTarget.GL_TEXTURE_2D);

        // test unbinding of the currently bound texture object
        c.glBindTexture(TextureTarget.GL_TEXTURE_2D, null);
        assertFalse(texture.isBound());
        assertFalse(c.isBound(TextureTarget.GL_TEXTURE_2D));
        assertNull(texture.getTarget());
    }

    public void testTexStorage2D() throws Exception {
        GLContext c = new GLContext();
        Texture texture = c.glGenTextures();
        c.glBindTexture(TextureTarget.GL_TEXTURE_2D, texture);
        c.glTexStorage2D(TextureTarget.GL_TEXTURE_2D, 0, TextureFormat
                .GL_RGBA32F, 256, 256);
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