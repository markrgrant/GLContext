import java.util.HashMap;

public class Shader extends GLObject {

    private boolean deleted;

    // the shader source for easy reference
    private String source;

    private HashMap<String, Integer> attribLocations;

    private boolean linked;

    ShaderType type;

    private boolean compiled;

    Shader(ShaderType s, int id) {
        super(id);
        source = null;
        deleted = false;
        compiled = false;
        this.type = s;
        attribLocations = new HashMap<String, Integer>();
    }

    public boolean isCompiled() {
        return compiled;
    }

    public boolean isLinked() {
        return linked;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCompiled(boolean compiled) {
        this.compiled = compiled;
    }

    public void setLinked() {
        linked = true;
    }

    public void addAttrib(String s, int loc) {
        attribLocations.put(s, loc);
    }

    public boolean hasAttribute(String attrib) {
        assert isLinked();
        return attribLocations.containsKey(attrib);
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

