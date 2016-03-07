// The texture target to which a texture object is first bound
// determines the 'type' of the texture object thereafter.

// Therefore a texture target is synonymous with a texture type.

// TODO: what does choice of target affect downstream?
enum TextureTarget {
    // behaves just like a 2D texture with a height of 1
    GL_TEXTURE_1D,

    // most common target, used for most 2d image purposes
    GL_TEXTURE_2D,

    // used for representing volumes.  Has a 3d texture coordinate.
    GL_TEXTURE_3D,

    // special case of 2D texture that has subtle difference in how it is
    // read in a shader and which parameters are supported
    GL_TEXTURE_RECTANGLE,

    // an array of 1D texture images in a single object
    GL_TEXTURE_1D_ARRAY,

    // an array of 2D texture images in a single object
    GL_TEXTURE_2D_ARRAY,

    // a collection of six square images forming a cube
    GL_TEXTURE_CUBE_MAP,

    // an array of texture cube map images
    GL_TEXTURE_CUBE_MAP_ARRAY,

    // a special type of texture much like a 1D texture except storage is
    // represented by a buffer object.  They also differ from a 1D texture
    // by having a maximum size much larger than a 1D texture, upwards of
    // several hundred Mb on most platforms. Buffer textures lack a few features
    // supported by 1D texture types such as
    GL_TEXTURE_BUFFER,

    // used for multi-sample antialiasing, a  technique for improving image
    // quality especially at the edges of lines and polygons
    GL_TEXTURE_2D_MULTISAMPLE,

    // an array of 2D milti-sample textures
    GL_TEXTURE_2D_MULTISAMPLE_ARRAY // two dimensional array multisample texture
}