import java.util.HashMap;

public class Program extends GLObject {

    private HashMap<Integer, Shader> shaders;
    private int activeAttributes;
    private boolean isLinked;

    // create a program object from an array of shader source code strings
    public Program(int id) {
        super(id);
        shaders = new HashMap<Integer, Shader>();
        isLinked = false;
    }

    public boolean isLinked() {
        return isLinked;
    }

    public void link() {
        isLinked = true;
    }

    public void attach(Shader s) {
        shaders.put(s.getId(), s);
    }

    public String toString() {
        return "";
    }

        /*
        public Program(CharSequence[] vertexShaders,
                       CharSequence[] fragmentShaders) {
            isLinked = false;
            id = glCreateProgram();
            shaders = new Shader[vertexShaders.length +
                    fragmentShaders.length];
            int i = 0;
            for (CharSequence vs : vertexShaders) {
                Shader s = new VertexShader(vs);
                shaders[i++] = s;
            }

            for (CharSequence fs : fragmentShaders) {
                Shader s = new FragmentShader(fs);
                shaders[i++] = s;
            }

            for(Shader s : shaders) {
                glAttachShader(id, s.id);
            }

            glLinkProgram(id);

            isLinked = glGetProgrami(id, GL_LINK_STATUS) == 1 ? true : false;
            if(!isLinked) {
                String strInfoLog = glGetProgramInfoLog(id);
                throw new RuntimeException("Linker failure: " + strInfoLog + "\n");
            }
            glUseProgram(id);
            activeAttributes = glGetProgrami(id, GL_ACTIVE_ATTRIBUTES);
        }
        */
}
