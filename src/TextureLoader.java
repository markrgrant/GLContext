import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;


public class TextureLoader {

    // A cache of already-loaded images that have
    // been used to create textures.  The key is the
    // filename and the value is the unique OpenGL texture
    // object name.
    private HashMap<String, Integer> cache;

    public TextureLoader() {
        cache = new HashMap<String, Integer>();
    }

    // load a texture with the given filename.
    public int load(String filename) {

        if (cache.containsKey(filename)) {
            return cache.get(filename);
        } else {
            int textureId = loadNew(filename);
            cache.put(filename, textureId);
            return textureId;
        }
    }

    private int loadNew(String filename) {

        BufferedImage img = loadBufferedImage(filename);

        ColorModel m = img.getColorModel();
        int pixelBits = m.getPixelSize();
        boolean isAlphaPremultiplied = img.isAlphaPremultiplied();
        boolean hasAlpha = m.hasAlpha();
        int imageType = img.getType();
        int numComponents = m.getNumComponents();
        int textureId = glGenTextures();

        if (imageType != BufferedImage.TYPE_4BYTE_ABGR) {
            throw new RuntimeException("Unsupported image type");
        }

        int[] data = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null,
                0, img.getWidth());

        // data is in ARGB format.  It needs to be in ABGR format.
        // Move the bytes in each int.
        for (int i = 0; i < data.length; i++) {
            int blue = (data[i] & 0x000000ff) << 16;
            int green = data[i] & 0x0000ff00;
            int red = (data[i] & 0x00ff0000) >> 16;
            int alpha = data[i] & 0xff000000;
            data[i] = red + green + blue + alpha;
        }

        IntBuffer ipixels = BufferUtils.createIntBuffer(data.length);
        ipixels.put(data).flip();

        // create the texture object
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);

        // set up texture parameters such as wrapping behavior and scaling
        glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,
                GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, img.getWidth(),
                img.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, ipixels);
        return textureId;
    }

    private BufferedImage loadBufferedImage(String filename) {
        try {
            BufferedImage img = ImageIO.read(this.getClass().getResource(filename));
            return img;
        } catch (IOException e) {
            return null;
        }

    }
}