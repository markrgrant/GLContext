import junit.framework.TestCase;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class TextureLoaderTest extends TestCase {

    long window;

    public void testLoad() throws Exception {
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


        TextureLoader tl = new TextureLoader();
        int textureId = tl.load("/dice.png");

        // the vertex data is modified to include, for each vertex, the
        // texture coordinates as well, where (0,0) is the bottom left
        // of the texture, and (1,1) is the top right.
        float[] vertexData = new float[]{
                -1f, 1f, 0f, 1f, 0f, 0f, // top left texture map
                1f, 1f, 0f, 1f, 1f, 0f, // top right texture map
                1f, -1f, 0f, 1f, 1f, 1f, // bottom right texture map
                -1f, -1f, 0f, 1f, 0f, 1f};// bottom left texture map
        FloatBuffer vertices = BufferUtils.createFloatBuffer(24);
        vertices.put(vertexData).flip();

        int[] elementData = new int[]{0, 1, 2, 0, 2, 3};
        IntBuffer elements = BufferUtils.createIntBuffer(6);
        elements.put(elementData).flip();

        // we're going to be mapping the texture onto two triangles.  Create
        // a vertex buffer object, and a vertex array object for this purpose.
        // create Vertex Array Object and bind it
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // create a vertex buffer object
        int vbo = glGenBuffers(); // an element buffer object
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        // copy data into video memory.  Use static mejmory
        // since the vertex data is static
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // also create an element buffer object that references
        // the vertices in the vbo
        int ebo = glGenBuffers(); // an element buffer object
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        // copy data into video memory.  Use static mejmory
        // since the vertex data is static
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elements, GL_STATIC_DRAW);

        String vShaderStr = "#version 330 core\n" +
                "in vec2 texcoord;\n" +
                "in vec4 position;\n" +
                "out vec2 Texcoord;\n" +
                "void main()\n" +
                "{gl_Position = position;Texcoord = texcoord;}";
        int vShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vShader, vShaderStr);
        glCompileShader(vShader);

        // check for compilation errors. 0 indicates an error, 1 is ok
        int vStatus = glGetShaderi(vShader, GL_COMPILE_STATUS);
        assert (vStatus == 1);

        String fShaderStr = "#version 330 core\n" +
                "in vec2 Texcoord;\n" +
                "out vec4 outColor;\n" +
                "uniform sampler2D tex;\n" +
                "void main()\n" +
                "{outColor = texture(tex, Texcoord);}";
        int fShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fShader, fShaderStr);
        glCompileShader(fShader);

        // Check shaders
        // check for compilation errors. 0 indicates an error, 1 is ok
        int fStatus = glGetShaderi(fShader, GL_COMPILE_STATUS);
        assert (fStatus == 1);

        int program = glCreateProgram();
        glAttachShader(program, vShader);
        glAttachShader(program, fShader);
        int shadersAttached = glGetProgrami(program, GL_ATTACHED_SHADERS);
        assert (shadersAttached == 2);

        // must specify the location of the fragment shader output before
        // compiling
        glBindFragDataLocation(program, 0, "outColor");

        // link the program
        glLinkProgram(program);

        // verify that linking occurred as expected
        int linkStatus = glGetProgrami(program, GL_LINK_STATUS);
        assert (linkStatus == 1);
        int numActiveAttrs = glGetProgrami(program, GL_ACTIVE_ATTRIBUTES);
        assert (numActiveAttrs == 2);

        // use the shaders in this program
        glUseProgram(program);

        // enable the position attribute in the shader
        int posAttrib = glGetAttribLocation(program, "position");
        glEnableVertexAttribArray(posAttrib);

        // tell opengl where the position data is to be found in the vertex
        // buffer object
        glVertexAttribPointer(posAttrib, 4, GL_FLOAT, false, 24, 0);

        // tell opengl where the texture coordinate data is to be found in
        // the vertex buffer object
        int texAttrib = glGetAttribLocation(program, "texcoord");
        glEnableVertexAttribArray(texAttrib);
        // texture coordinates are 2 floats, vertex data is 24 bytes in size,
        // and the texture coordinates start at byte 16
        int texAttribLength = 2;        // # of floats in a tex coord
        int texAttribType = GL_FLOAT;
        int vertexSizeInBytes = 6 * 4;    // total size of a vertex entry
        int texAttribStartInBytes = 4 * 4;// starting byte of tex coords
        glVertexAttribPointer(texAttrib, texAttribLength, GL_FLOAT, false,
                vertexSizeInBytes, texAttribStartInBytes);

        while (glfwWindowShouldClose(window) == GL_FALSE) {
            // clear the screen to black
            glClearColor(0f, 0f, 0f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);

            //draw two triangles using the element buffer of length 6, which
            //points to indices in the vertex buffer
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            // clears the frame buffer
            glfwSwapBuffers(window);
            glfwSwapInterval(1);
            glfwPollEvents();
        }
        //super.tearDown();
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}