package no.myke.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * This class is used as an example of how to load and render a 3ds model with
 * texture using jogl.
 */
public class RenderModel implements GLEventListener {

	private static File modelFile = new File("src/test/resources/fighter.3ds");
	private static File textureFile = new File("src/test/resources/fighter.png");

	private float rotateX = 0.0f;
	private float rotateY = 0.0f;
	private float rotateZ = 0.0f;
	private int modelList = 0;
	private final int swapInterval;

	private int prevMouseX;
	private int prevMouseY;

	public static void main(String[] args) {
		if (args.length == 2) {
			modelFile = new File(args[0]);
			textureFile = new File(args[1]);
		}
		System.out.println("Using model " + modelFile + " and texture " + textureFile);
		
		java.awt.Frame frame = new java.awt.Frame("Render Model");
		frame.setSize(300, 300);
		frame.setLayout(new java.awt.BorderLayout());

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			}
		});

		GLCanvas canvas = new GLCanvas();
		RenderModel modelView = new RenderModel();
		canvas.addGLEventListener(modelView);

		frame.add(canvas, java.awt.BorderLayout.CENTER);
		frame.validate();
		
		Animator animator = new Animator();
		animator.add(canvas);
		animator.start();

		frame.setVisible(true);
	}

	public RenderModel(int swapInterval) {
		this.swapInterval = swapInterval;
	}

	public RenderModel() {
		this.swapInterval = 1;
	}


	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		System.out.println("Chosen GLCapabilities: "
				+ drawable.getChosenGLCapabilities());
		System.out.println("INIT GL IS: " + gl.getClass().getName());
		System.out.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
		System.out.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
		System.out.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));

		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glEnable(GL2.GL_DEPTH_TEST);

		/* create the model */
		if (modelList <= 0) {
			modelList = gl.glGenLists(1);
			gl.glNewList(modelList, GL2.GL_COMPILE);
			createModel(gl);
			gl.glEndList();
		} else {
			System.err.println("model list reused: " + modelList);
		}

		gl.glEnable(GL2.GL_NORMALIZE);

		MouseListener modelMouse = new ModelMouseAdapter();
		KeyListener modelKeys = new ModelKeyAdapter();

		if (drawable instanceof Window) {
			Window window = (Window) drawable;
			window.addMouseListener(modelMouse);
			window.addKeyListener(modelKeys);
		} else if (GLProfile.isAWTAvailable()
				&& drawable instanceof java.awt.Component) {
			java.awt.Component comp = (java.awt.Component) drawable;
			new AWTMouseAdapter(modelMouse, drawable).addTo(comp);
			new AWTKeyAdapter(modelKeys, drawable).addTo(comp);
		}
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();
		GLU glu = GLU.createGLU(gl);
		gl.setSwapInterval(swapInterval);

		float aspect = (float) width / height;

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0f, aspect, 0.1f, 200.0f);
	    glu.gluLookAt(0.0, 0.0, 5.0, 0.0,0.0,0.0, 0.0,1.0,0.0);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -40.0f);
	}

	public void dispose(GLAutoDrawable drawable) {
		modelList = 0;
	}

	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		// Place the model and call its display list
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, 0.0f, -100.0f);
		// Rotate the entire model based on mouse drag
		gl.glRotatef(rotateX, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotateY, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotateZ, 0.0f, 0.0f, 1.0f);
		gl.glCallList(modelList);
		gl.glPopMatrix();
	}

	public void createModel(GL2 gl) {
		gl.glShadeModel(GL2.GL_FLAT);
		gl.glNormal3f(0.0f, 0.0f, 1.0f);

		Model model = loadModel();
		Texture texture = loadTexture();
	    texture.setTexParameterf(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP); // If the u,v coordinates overflow the range 0,1 the image is repeated
	    texture.setTexParameterf(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
	    texture.setTexParameterf(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		texture.setTexParameterf(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);    	   

		gl.glEnable(GL2.GL_TEXTURE_2D);
		texture.bind(gl);
		texture.enable(gl);

		for (ModelObject modelObject : model.objects) {
			short[] p = modelObject.polygons;
			Vector[] v = modelObject.vectors;
			float[] t = modelObject.textureCoordinates;

			for (int i = 0; i < p.length; i += 3) {
				int a = p[i + 0];
				int b = p[i + 1];
				int c = p[i + 2];
				int at = p[i + 0] * 2;
				int bt = p[i + 1] * 2;
				int ct = p[i + 2] * 2;
				gl.glBegin(GL2.GL_POLYGON);
				gl.glTexCoord2f(t[at], t[at + 1]);
				gl.glVertex3f(v[a].X(), v[a].Y(), v[a].Z());
				gl.glTexCoord2f(t[bt], t[bt + 1]);
				gl.glVertex3f(v[b].X(), v[b].Y(), v[b].Z());
				gl.glTexCoord2f(t[ct], t[ct + 1]);
				gl.glVertex3f(v[c].X(), v[c].Y(), v[c].Z());
				gl.glEnd();
			}
		}
	}

	private Model loadModel() {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(modelFile);
			MapReader reader = new MapReader(fis.getChannel());
			Parser parser = new Parser(reader);
			return parser.parseFile();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// nothing to do
				}
			}
		}
	}
	
	private Texture loadTexture() {
		try {
			return TextureIO.newTexture(textureFile, true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}

	class ModelKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			int kc = e.getKeyCode();
			if (KeyEvent.VK_LEFT == kc) {
				rotateY -= 1;
			} else if (KeyEvent.VK_RIGHT == kc) {
				rotateY += 1;
			} else if (KeyEvent.VK_UP == kc) {
				rotateX -= 1;
			} else if (KeyEvent.VK_DOWN == kc) {
				rotateX += 1;
			}
		}
	}

	class ModelMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			prevMouseX = e.getX();
			prevMouseY = e.getY();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			final int x = e.getX();
			final int y = e.getY();
			int width = 0, height = 0;
			Object source = e.getSource();
			if (source instanceof Window) {
				Window window = (Window) source;
				width = window.getSurfaceWidth();
				height = window.getSurfaceHeight();
			} else if (source instanceof GLAutoDrawable) {
				GLAutoDrawable glad = (GLAutoDrawable) source;
				width = glad.getSurfaceWidth();
				height = glad.getSurfaceHeight();
			} else if (GLProfile.isAWTAvailable()
					&& source instanceof java.awt.Component) {
				java.awt.Component comp = (java.awt.Component) source;
				width = comp.getWidth();
				height = comp.getHeight();
			} else {
				throw new RuntimeException("Event source neither Window nor Component: " + source);
			}
			float thetaY = 360.0f * ((float) (x - prevMouseX) / (float) width);
			float thetaX = 360.0f * ((float) (prevMouseY - y) / (float) height);

			prevMouseX = x;
			prevMouseY = y;

			rotateX += thetaX;
			rotateY += thetaY;
		}
	}
}
