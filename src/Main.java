import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Main
 */
public class Main {

	private GLFWErrorCallback errorCallback;
	private GLFWKeyCallback keyCallback;

	private int uniModel;
	private float angle = 0f;
	private float anglePerSecond = 50f;
	private int vao;
	private int vbo;
	private int vertexShader;
	private int fragmentShader;
	private int shaderProgram;
	private int posAttrib;
	private int colorAttrib;
	private int uniView;
	private int uniProjection;

	public Main(){
	}

	public Main init(){
		errorCallback = GLFWErrorCallback.createPrint(System.err);
		glfwSetErrorCallback(errorCallback);
		if(glfwInit() != GLFW_TRUE){
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
					glfwSetWindowShouldClose(window, GLFW_TRUE);
				}
			}
		};
		return this;
	}

	public Main run(){
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

		long window = glfwCreateWindow(640, 480, "Simple example", NULL, NULL);
		if (window == NULL) {
			glfwTerminate();
			throw new RuntimeException("Failed to create the GLFW window");
		}
		glfwSetKeyCallback(window, keyCallback);
		glfwMakeContextCurrent(window);
		GL.createCapabilities();

		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		FloatBuffer vertices = BufferUtils.createFloatBuffer(3 * 6);
		vertices.put(-0.6f).put(-0.4f).put(0f).put(1f).put(0f).put(0f)
				.put(0.6f).put(-0.4f).put(0f).put(0f).put(1f).put(0f)
				.put(0f).put(0.6f).put(0f).put(0f).put(0f).put(1f);
		vertices.flip();

		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

		vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, FileReaderUtil.readAllLines("src/vertex.glsl"));
		glCompileShader(vertexShader);

		int status = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
		if(status != GL_TRUE){
			throw new RuntimeException(glGetShaderInfoLog(vertexShader));
		}

		fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, FileReaderUtil.readAllLines("src/fragment.glsl"));
		glCompileShader(fragmentShader);

		status = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
		if(status != GL_TRUE){
			throw new RuntimeException(glGetShaderInfoLog(vertexShader));
		}

		shaderProgram = glCreateProgram();
		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		glBindFragDataLocation(shaderProgram, 0, "fragColor");
		glLinkProgram(shaderProgram);

		status = glGetProgrami(shaderProgram, GL_LINK_STATUS);
		if (status != GL_TRUE) {
			throw new RuntimeException(glGetProgramInfoLog(shaderProgram));
		}

		glUseProgram(shaderProgram);

		int floatSize = 4;

		posAttrib = glGetAttribLocation(shaderProgram, "position");
		glEnableVertexAttribArray(posAttrib);
		glVertexAttribPointer(posAttrib, 3, GL_FLOAT, false, 6 * floatSize, 0);

		colorAttrib = glGetAttribLocation(shaderProgram, "color");
		glEnableVertexAttribArray(colorAttrib);
		glVertexAttribPointer(colorAttrib, 3, GL_FLOAT, false, 6 * floatSize, 3 * floatSize);

		uniModel = glGetUniformLocation(shaderProgram, "model");
		Matrix4f model = new Matrix4f();
		glUniformMatrix4fv(uniModel, false, model.getBuffer());

		uniView = glGetUniformLocation(shaderProgram, "view");
		Matrix4f view = new Matrix4f();
		glUniformMatrix4fv(uniView, false, view.getBuffer());

		uniProjection = glGetUniformLocation(shaderProgram, "projection");
		float ratio = 640f/ 480f;
		Matrix4f projection = Matrix4f.orthographic(-ratio, ratio, -1f, 1f, -1f, 1f);
		glUniformMatrix4fv(uniProjection, false, projection.getBuffer());

		mainLoop(window);

		glfwDestroyWindow(window);
		return this;
	}

	private void mainLoop(long window) {
		double time = glfwGetTime();
		long sleepTime = 1000L / 60L;
		while (glfwWindowShouldClose(window) != GLFW_TRUE) {
			glfwSwapBuffers(window);
			glfwPollEvents();

			update(glfwGetTime() - time);
			time = glfwGetTime();

			glClear(GL_COLOR_BUFFER_BIT);

			Matrix4f model = Matrix4f.rotate(angle, 0, 0, 1f);
			glUniformMatrix4fv(uniModel, false, model.getBuffer());

			glDrawArrays(GL_TRIANGLES, 0, 3);

			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	private void update(double delta){
		angle += delta * anglePerSecond;
	}

	public Main destroy(){
		glDeleteVertexArrays(vao);
		glDeleteBuffers(vbo);
		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
		glDeleteProgram(shaderProgram);

		keyCallback.release();
		glfwTerminate();
		errorCallback.release();

		return this;
	}

	public static void main(String[] args){
		new Main().init().run().destroy();
	}
}
