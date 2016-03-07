// The internal format used by OpenGL for storing the texture.  Data will
// be converted if necessary into this format at image specification time.
// Each internal format has a size, performance, and quality tradeoff.  The
// application writer determines the appropriate format for their needs.
public enum TextureFormat {
    GL_RGBA,
    GL_RGBA32F
}
