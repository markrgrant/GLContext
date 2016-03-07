import java.util.HashMap;

public class Program extends GLObject {

    private HashMap<Integer, Shader> shaders;
    private int activeAttributes;
    private boolean linked;
    private boolean deleted;

    // create a program object from an array of shader source code strings
    Program(int id) {
        super(id);
        shaders = new HashMap<Integer, Shader>();
        linked = false;
    }

    boolean isLinked() {
        return linked;
    }

    void link() {
        linked = true;
    }

    void attach(Shader s) {
        shaders.put(s.getId(), s);
    }

    public String toString() {
        return "";
    }

    public boolean isDeleted() {
        return deleted;
    }

    void setLinked(boolean linked) {
        // verify every shader has been compiled
        for (Shader s : shaders.values()) {
            assert s.isCompiled();
            s.setLinked();
        }
        this.linked = linked;
    }

    public void delete() {
        deleted = true;
    }

    boolean shadersReady() {
        if (shaders.size() == 0) return false;
        for (Shader s : shaders.values()) {
            if (!s.isCompiled()) return false;
        }
        return true;
    }

    boolean hasAttribute(CharSequence attrib) {
        for (Shader s : shaders.values()) {
            if (s.hasAttribute(attrib.toString())) return true;
        }
        return false;
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
