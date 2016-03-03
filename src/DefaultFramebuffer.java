// From the OpenGL wiki:
// The default framebuffer is the framebuffer that OpenGL is created with.
// It is created along with the OpenGL Context.  Like Framebuffer
// Objects,  the default framebuffer is a series of images. Unlike FBOs,
// one of these images usually represents what you actually see on
// some part of your screen.

// Because the default framebuffer is created at the time the OpenGL
// context is constructed, the desired properties of the default
// framebuffers are given to the context creation functions, which
// take these into consideration when creating the context.
//
// The default framebuffer contains a number of images, based on how
//  it was created.  All default framebuffer images are automatically
// resized to the size of the output window, as it is resized.
//
// The default framebuffer contains up to 4 color buffers, named
// GL_FRONT_LEFT, GL_BACK_LEFT, GL_FRONT_RIGHT, and GL_BACK_RIGHT.
// Most consumer graphics cards cannot use these.
//
// The front buffer is more or less what you see on the screen.
// The back buffer is the image that is typically rendered to.
// When the user wants the rendered image to become visible,
// he calls a platform specific buffer swapping command.

// Rendering to or reading from the front buffer is not advised.

// The default framebuffer can have a depth buffer.
//
// The default framebuffer can have a stencil buffer for doing
// stencil tests.

// The default framebuffer is owned by a resource external to OpenGL
// and it is possible that particular pixels are not owned by OpenGL
// and can't be written.  Fragments aimed at such pixels are discarded.
public class DefaultFramebuffer {

    DefaultFramebuffer() {
    }

    enum DefaultFramebufferBuffers {
        GL_FRONT_LEFT,  // always available
        GL_BACK_LEFT,   // always available

        // for stereoscopic. not supported by most cards.
        GL_FRONT_RIGHT,
        GL_BACK_RIGHT,

        // aliases for the other buffers
        GL_LEFT,        // alias for GL_FRONT_LEFT
        GL_RIGHT,       // alias for GL_FRONT_RIGHT
        GL_FRONT,       // alias for GL_FRONT_LEFT
        GL_BACK,        // alias for GL_BACK_LEFT
        GL_FRONT_AND_BACK,  // alias for GL_FRONT_LEFT

        // stencil buffer for doing stencil tests
        GL_STENCIL,

        // depth buffer for depth testing
        GL_DEPTH
    }

    public String toString() {
        return ""; // TODO
    }
}

