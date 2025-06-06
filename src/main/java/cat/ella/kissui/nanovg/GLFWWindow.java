package cat.ella.kissui.nanovg;

import cat.ella.kissui.KissUI;
import cat.ella.kissui.render.Window;
import cat.ella.kissui.unit.Vector2;
import cat.ella.kissui.util.MathHelper;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLFWWindow extends Window {

    public Long handle;
    public Double fpsCap = 0.0;
    public float height;
    public float width;

    public GLFWWindow(String title, int height, int width) {
        this(title, height, width, false, true, true);
    }

    public GLFWWindow(String title, int height, int width, boolean openGL12, boolean resizable, boolean decorated) {
        super(height, width);
        this.height = height;
        this.width = width;

        GLFWErrorCallback errorCallback = GLFWErrorCallback.create((code, description) -> {
            String errorName = org.lwjgl.glfw.GLFW.class.getName();
            String stackTrace = Arrays.stream(Thread.currentThread().getStackTrace())
                    .skip(4)
                    .map(StackTraceElement::toString)
                    .reduce("", (a, b) -> a + "\n\t at " + b);
            NanoVGManager.LOGGER.error(errorName + " (" + code + "): " + description + "\nStack: " + stackTrace);
        });

        glfwSetErrorCallback(errorCallback);

        if (!glfwInit()) {
            throw new RuntimeException("Failed to init GLFW");
        }

        if (openGL12) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
        } else {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        }

        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
        if (!decorated) glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);

        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the window.");
        }

        glfwMakeContextCurrent(handle);
        GL.createCapabilities();
        System.out.println("System Information:");
        System.out.println("\tGPU: " + glGetString(GL_RENDERER) + "; " + glGetString(GL_VENDOR));
        System.out.println("\tDriver version: " + glGetString(GL_VERSION));
        System.out.println("\tOS: " + System.getProperty("os.name") + " v" + System.getProperty("os.version") + "; " + System.getProperty("os.arch"));
        System.out.println("\tJava version: " + System.getProperty("java.version") + "; " + System.getProperty("java.vm.name") + " from " + System.getProperty("java.vendor") + " (" + System.getProperty("java.vendor.url") + ")");
    }

    @Override
    public Window open(KissUI UI) {
        UI.setWindow(this);
        glfwSetTime(0.0);
        glfwSwapInterval(1);

        createCallbacks(UI);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer wbuf = stack.mallocInt(1);
            IntBuffer hbuf = stack.mallocInt(1);
            FloatBuffer sxbuf = stack.mallocFloat(1);
            FloatBuffer sybuf = stack.mallocFloat(1);
            glfwGetWindowContentScale(handle, sxbuf, sybuf);
            glfwGetFramebufferSize(handle, wbuf, hbuf);
            float sx = sxbuf.get(0);
            float sy = sybuf.get(0);
            int w = wbuf.get(0);
            int h = hbuf.get(0);

            float pixelRatio = Math.max(sx, sy);
            if (true) NanoVGManager.LOGGER.info("Pixel ratio: " + pixelRatio);
            UI.resize(w / sx, h / sy);

            this.width = w;
            this.height = h;
        }

        Vector2 min = new Vector2(100F, 100F);
        Vector2 max = new Vector2(-1F, -1F);
        glfwSetWindowSizeLimits(handle, MathHelper.fti(min.x()), MathHelper.fti(min.y()), MathHelper.fti(max.x()), MathHelper.fti(max.y()));

        Vector2 aspectRatio = new Vector2(-1F, -1F);
        if (aspectRatio.x() == 0f || aspectRatio.y() == 0f) {
            Vector2 ratio = new Vector2(width, height);
            NanoVGManager.LOGGER.info("Inferred aspect ratio: " + ratio.x() + ":" + ratio.y());
            aspectRatio = ratio;
        }
        glfwSetWindowAspectRatio(handle, MathHelper.fti(aspectRatio.x()), MathHelper.fti(aspectRatio.y()));

        double[] time = {glfwGetTime()};
        fpsCap = 30.0;
        while (!glfwWindowShouldClose(handle)) {
            Vector2 size = UI.size;
            int offset = 0;
            int pixelRatio = 1;

            int height = (int) (size.y() * pixelRatio);
            GL20C.glViewport(0, MathHelper.fti(offset + (this.height - height)), (int) (size.x() * pixelRatio), height);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glClearColor(0f, 0f, 0f, 0f);

            UI.render();

            if (fpsCap != 0.0) {
                double delta = glfwGetTime() - time[0];
                if (delta < 1/fpsCap) {
                    try {
                        Thread.sleep((long) ((1/fpsCap - delta) * 1000));
                    } catch (InterruptedException ignored) {}
                }
                time[0] = glfwGetTime();
            }

            glfwSwapBuffers(handle);
            glfwPollEvents();
        }

        UI.cleanup();
        GL.setCapabilities(null);
        Callbacks.glfwFreeCallbacks(handle);
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) callback.free();
        return this;
    }

    @Override
    public void close() {
        glfwSetWindowShouldClose(handle, true);
    }

    @Override
    public void preRender() {
        // Called before rendering frame
    }

    @Override
    public void postRender() {
        // Called after rendering frame
    }

    @Override
    public Optional<String> getClipboard() {
        return Optional.ofNullable(glfwGetClipboardString(handle));
    }

    @Override
    public void setClipboard(String text) {
        glfwSetClipboardString(handle, text);
    }

    @Override
    public String getKeyName(int key) {
        return glfwGetKeyName(key, 0);
    }

    public void createCallbacks(KissUI UI) {
        glfwSetFramebufferSizeCallback(handle, (window, w, h) -> {
            this.width = w;
            this.height = h;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer sxbuf = stack.mallocFloat(1);
                FloatBuffer sybuf = stack.mallocFloat(1);
                glfwGetWindowContentScale(window, sxbuf, sybuf);
                float sx = sxbuf.get(0);
                float sy = sybuf.get(0);
                UI.resize(w / sx, h / sy);
            }
        });
    }

    public void fullscreen() {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = glfwGetVideoMode(monitor);
        glfwSetWindowMonitor(handle, monitor, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
    }
}