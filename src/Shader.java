import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public class Shader {
    // the shader id
    int id;

    private boolean isDestroyed;

    // the shader source for easy reference
    String source;

    // either GL_TRUE or GL_FALSE dependeing on
    // the success of or failure of computation
    boolean isCompiled;

    ShaderType type;

    public Shader(ShaderType s, int shaderId) {
        this.id = shaderId;
        source = null;
        isDestroyed = false;
        isCompiled = false;
        this.type = s;
    }

    public int getId() {
        return id;
    }

    public void destroy() {
        isDestroyed = true;
    }

        /*
        // create a shader object and compile the shader.
        Shader(int eShaderType, CharSequence shaderSource) {

            this.type = eShaderType;

            // create a shader opengl object and save its id
            id = glCreateShader(eShaderType);

            // save the shader source for access.
            source = shaderSource;

            // set the shader source
            glShaderSource(id, source);

            // compile the shader
            glCompileShader(id);

            // check for errors and print them to stderr
            isCompiled = glGetShaderi(id, GL_COMPILE_STATUS) == 1 ?
                    true : false;
            if(!isCompiled) {
                String strInfoLog = glGetShaderInfoLog(id);
                String strShaderType = "";
                switch(eShaderType) {
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
        */

    public static int shaderTypeToGL(ShaderType t) {
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
}

