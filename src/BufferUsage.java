enum BufferUsage {
    // Buffer contents will be set once by the application and used infrequently
    // for drawing
    GL_STREAM_DRAW,
    // Buffer contents will be set once by an OpenGL command and used
    // infrequently for drawing
    GL_STREAM_READ,
    // Buffer contents will be set once as output from an OpenGL command and
    // used infrequently for drawing or copying to other images.
    GL_STREAM_COPY,
    // Buffer contents will be set once by the application and used frequently
    // for drawing or copying to other images
    GL_STATIC_DRAW,
    // Buffer contents will be set once as output from an OpenGL command and
    // queried many times by the application
    GL_STATIC_READ,
    // Buffer contents will be set once as ouptut  from an OpenGL command and
    // used frequently for drawing or copying to other images
    GL_STATIC_COPY,
    // Buffer contents will be updated frequently by the applicatoin and used
    // frequently for drawing or copying to other images
    GL_DYNAMIC_DRAW,
    // Buffer contents will be updated frequently as output from OpenGL
    // commands and queried many times by the application
    GL_DYNAMIC_READ,
    // Buffer contents will be updated frequently as output from OpenGL
    // commands and used frequently for drawing or copying to other images
    GL_DYNAMIC_COPY
}
