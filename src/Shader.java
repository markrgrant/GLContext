public class Shader extends GLObject {

    private boolean isDestroyed;

    // the shader source for easy reference
    String source;

    // either GL_TRUE or GL_FALSE dependeing on
    // the success of or failure of computation
    boolean isCompiled;

    ShaderType type;

    Shader(ShaderType s, int id) {
        super(id);
        source = null;
        isDestroyed = false;
        isCompiled = false;
        this.type = s;
    }

    void destroy() {
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

}

