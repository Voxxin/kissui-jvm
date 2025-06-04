package cat.ella.kissui.render;

import cat.ella.kissui.data.FrameBuffer;

public interface FrameBufferController {
    FrameBuffer create(int width, int height);

    void bind(FrameBuffer buffer);

    void unbind();

    void draw(
            FrameBuffer buffer,
            float x, float y,
            float width, float height
    );

    void delete(FrameBuffer buffer);
}
