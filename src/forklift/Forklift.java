package forklift;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;



/**
 * Trabalho2.java <BR>
 * author: 
 * 
 * Bruno Angeli Calza
 * Fernando Dalcin Gobbo
 * José A. Daidone Neto
 *
 * This version is equal to Brian Paul's version 1.2 1999/10/21
 */
public class Forklift implements GLEventListener, KeyListener {

    private GL gl;
    private GLU glu;
    private float forkliftPositionX;
    private float forkliftPositionZ;
    private float forkliftRotationY;
    private float boxPositionY;
    private float finalBoxPositionX;
    private float finalBoxPositionY;
    private float finalBoxPositionZ;
    private float finalBoxRotationY;
    private float forkPositionY;
    private Texture texture[];
    private boolean keys[];
    
    private GLModel forkLift = null;
    private GLModel road = null;
    private GLModel fork = null;
    
    private int state;
    
    public Forklift(){
        glu = new GLU();
        
        forkliftPositionX = 0;
        forkliftPositionZ = 0;
        forkliftRotationY = 0;
        boxPositionY = 0;
        forkPositionY = 0;
        
        texture = new Texture[5];
        keys = new boolean[7];
        
        state = 0;
    }
    
    public static void main(String[] args) {
        Frame frame = new Frame("Forklift");
        GLCanvas canvas = new GLCanvas();

        canvas.addGLEventListener(new Forklift());
        frame.add(canvas);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH); 
        final Animator animator = new Animator(canvas);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                // Run this on another thread than the AWT event queue to
                // make sure the call to Animator.stop() completes before
                // exiting
                new Thread(new Runnable() {

                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }).start();
            }
        });
      
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        animator.start();
    }

    public void init(GLAutoDrawable drawable) {
        // Use debug pipeline
        // drawable.setGL(new DebugGL(drawable.getGL()));

        drawable.addKeyListener(this);
        
        gl = drawable.getGL();
        System.err.println("INIT GL IS: " + gl.getClass().getName());

        // Enable VSync
        gl.setSwapInterval(1);

        // Setup the drawing area and shading mode
        gl.glClearColor(0,0,0,0);
        gl.glShadeModel(GL.GL_SMOOTH); // try setting this to GL_FLAT and see what happens.
        
        setupIlumination();
        setupTextures();
       
        if (false == loadModels(gl)) {
                System.exit(1);
        }
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        if (height <= 0) { // avoid a divide by zero error!
            height = 1;
        }
        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(65.0f, h, 1.0, 2000.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void display(GLAutoDrawable drawable) {

        keyOperations();
        // Clear the drawing area
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL.GL_TEXTURE_2D);
        
        gl.glPushMatrix();
            
            glu.gluLookAt(0,200,800,0,0,0,0,1,0);
 
            drawRoad();
          
            gl.glPushMatrix();
                drawForkLift();
        
                if (state == 1)
                    drawInMovementBox();
    
            gl.glPopMatrix();
            
                if (state == 0){
                    gl.glPushMatrix();
                        drawInitialBox();
                    gl.glPopMatrix();
                }
                    
                if (state == 2){
                    gl.glPushMatrix();
                        drawFinalBox();
                    gl.glPopMatrix();
                }
                
        gl.glPopMatrix();   
       
        // Flush all drawing operations to the graphics card
        gl.glFlush();
    }

    private Boolean loadModels(GL gl) {
		forkLift = ModelLoaderOBJ.LoadModel("./models/forklift.obj",
				"./models/forklift.mtl", gl);
                fork = ModelLoaderOBJ.LoadModel("./models/fork.obj",
				"./models/fork.mtl", gl);
                road = ModelLoaderOBJ.LoadModel("./models/road.obj",
				"./models/road.mtl", gl);
                
        
                
		if (forkLift == null || road == null) {
			return false;
		}
		return true;
    }
    
    private void setupIlumination(){
       
        //Luz do Ambiente
        float luzAmbiente[]  = {0.20f, 0.20f, 0.20f, 2.0f};
        //Luz que é refletida do material em todas as direções
        float luzDifusa[]    = {0.5f, 0.5f, 0.5f, 1.0f};  
        //Luz que é refletida do material em uma única direção
        float luzEspecular[] = {0.8f, 0.8f, 0.8f, 1.0f};  
        //Posição da origem da Luz
        float posicaoLuz[]   = {0.0f, 1000.0f, 0.0f, 1.0f};
       
        //Capacidade de brilho dos materiais
        float especularidade[] = {0.8f, 0.8f, 0.8f, 1.0f};
        int especMaterial = 70;      
       
        //Refletancia do Material
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK,GL.GL_SPECULAR,especularidade,0);
        //Concentração do Brilho
        gl.glMateriali(GL.GL_FRONT_AND_BACK,GL.GL_SHININESS,especMaterial);
       
        // Ativa o uso da luz ambiente
        gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT,luzAmbiente,0);
 
        // Define os parâmetros da luz de número 0
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT,  luzAmbiente,  0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE,  luzDifusa,    0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, luzEspecular, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, posicaoLuz,   0);      
       
        //Habilita o uso da cor natural do objeto
        gl.glEnable(GL.GL_COLOR_MATERIAL);
        //Habilita o uso de iluminação
        gl.glEnable(GL.GL_LIGHTING);  
        // Habilita a luz de número 0
        gl.glEnable(GL.GL_LIGHT0);
        // Habilita o depth-buffering
        gl.glEnable(GL.GL_DEPTH_TEST);
    }
    
    private void setupTextures(){
        
        BufferedImage img = null;
        File file;
        
        for (int i =0; i < 5; i++){
            file = new File("texture/"+i+".jpg");
            try {
                img = ImageIO.read(file);
            } catch (IOException ex) {
                Logger.getLogger(Forklift.class.getName()).log(Level.SEVERE, null, ex);
            }
            texture[i] = TextureIO.newTexture(img,true);
        }
        
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
    }
    
    private void drawRoad(){
        gl.glTranslated(0, -100, -150);
        texture[0].bind();
        road.opengldraw(gl);  
    }
    
    private void drawForkLift(){
        texture[3].bind();
        texture[2].bind();
        texture[1].bind();
        gl.glTranslatef(forkliftPositionX,85,forkliftPositionZ);
        gl.glRotatef(forkliftRotationY,0,1,0);
        forkLift.opengldraw(gl);
        gl.glTranslatef(140,boxPositionY-35 , 0);
        fork.opengldraw(gl);
    }
    
    private void drawCube(double x, double y, double z){
 
            gl.glBegin(GL.GL_QUADS);            // Face posterior
                    gl.glNormal3d(0.0, 0.0, 1.0);       // Normal da face
                    gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3d( x, y, z);
                    gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3d(-x, y, z);
                    gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3d(-x,-y, z);
                    gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3d( x,-y, z);
            gl.glEnd();
            gl.glBegin(GL.GL_QUADS);                    // Face frontal
                    gl.glNormal3d(0.0, 0.0, -1.0);      // Normal da face
                    gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3d( x, y,-z);
                    gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3d( x,-y,-z);
                    gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3d(-x,-y,-z);
                    gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3d(-x, y,-z);
            gl.glEnd();
            gl.glBegin(GL.GL_QUADS);                    // Face lateral esquerda
                    gl.glNormal3d(-1.0, 0.0, 0.0);      // Normal da face
                    gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3d(-x, y, z);
                    gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3d(-x, y,-z);
                    gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3d(-x,-y,-z);
                    gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3d(-x,-y, z);
            gl.glEnd();
            gl.glBegin(GL.GL_QUADS);                    // Face lateral direita
                    gl.glNormal3d(1.0, 0.0, 0.0);       // Normal da face
                    gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3d(x, y, z);
                    gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3d(x,-y, z);
                    gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3d(x,-y,-z);
                    gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3d(x, y,-z);
            gl.glEnd();
            gl.glBegin(GL.GL_QUADS);                    // Face superior
                    gl.glNormal3d(0.0, 1.0, 0.0);       // Normal da face
                    gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3d(-x, y,-z);
                    gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3d(-x, y, z);
                    gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3d( x, y, z);
                    gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3d( x, y,-z);
            gl.glEnd();
            gl.glBegin(GL.GL_QUADS);                    // Face inferior
                    gl.glNormal3d(0.0, -1.0, 0.0);      // Normal da face
                    gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3d(-x,-y,-z);
                    gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3d( x,-y,-z);
                    gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3d( x,-y, z);
                    gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3d(-x,-y, z);
            gl.glEnd();
       
    }
    
    private void drawInMovementBox(){
        gl.glTranslatef(5,15,0);
        texture[4].bind();
        drawCube(40, 40, 40);
    }
    
    private void drawInitialBox(){
        texture[4].bind();
        gl.glTranslatef(-350,65 , 400);
        drawCube(40, 40, 40);
    }
    
    private void drawFinalBox(){
        texture[4].bind();
        gl.glTranslatef(finalBoxPositionX,85,finalBoxPositionZ);
        gl.glRotatef(finalBoxRotationY,0,1,0);
        gl.glTranslatef(140,finalBoxPositionY-35 , 0);
        gl.glTranslatef(5,15 , 0);
        drawCube(40, 40, 40);
    }
        
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }
    
    public void keyTyped(KeyEvent e) {
    
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch(key){
            //esq
            case 37:
                keys[0] = true;
                break;
            //frente 
            case 38:
                keys[1] = true;     
                break;
            //dir    
            case 39:
                keys[2] = true;
                break;
            //tras    
            case 40:  
                keys[3] = true;
                break;
            case 49:  
                keys[4] = true;
                break;
            case 50:  
                keys[5] = true;
                break;
            case 51:  {
                if (state == 2)
                    return;
                state = 2;
                finalBoxPositionX = forkliftPositionX;
                finalBoxPositionY = boxPositionY;
                finalBoxPositionZ = forkliftPositionZ;
                finalBoxRotationY = forkliftRotationY;
            }
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        switch(key){
            //esq
            case 37:
                keys[0] = false;
                break;
            //frente 
            case 38:
                keys[1] = false;     
                break;
            //dir    
            case 39:
                keys[2] = false;
                break;
            //tras    
            case 40:  
                keys[3] = false;
                break;
            case 49:  
                keys[4] = false;
                break;
            case 50:  
                keys[5] = false;
                break;
        }
    }
    
    private void keyOperations(){
      float tempX, tempZ;
        
      //esq
      if (keys[0])
            forkliftRotationY = (forkliftRotationY + .35f)%360;
      //frente
      if (keys[1]){
            int statePosition = 0;
            tempX = (float) (forkliftPositionX + Math.cos(forkliftRotationY*2*Math.PI/360));
            tempZ = (float) (forkliftPositionZ - Math.sin(forkliftRotationY*2*Math.PI/360)); 
            if (tempZ > 290 || tempZ < -290)
               statePosition = 1;
            if (tempZ > 390 || tempZ < -390)
                statePosition = 2;
            if ((statePosition == 1 || statePosition == 2) && ((tempX > 260 || tempX < -260)))
                    return ;
            if (statePosition == 2)
                return;
            if (tempX > 290 || tempX < -290)
               statePosition = 3;
            if (tempX > 390 || tempX < -390)
                statePosition = 4;
            if ((statePosition == 3 || statePosition == 4) && ((tempZ > 180 || tempZ < -180)))
                    return ;
            if (statePosition == 4)
                return;
            
            //a empílhadeira chegou na posição correta
            if (tempX <=-200 && tempX >=-203&& tempZ <=395 && tempZ >=380)
                state = 1;
            
           
            forkliftPositionX = tempX;
            forkliftPositionZ = tempZ;
            
      }
      //dir    
      if (keys[2])
            forkliftRotationY = (forkliftRotationY - 0.35f)%360;
      //ré    
      if (keys[3]){
            int statePosition = 0;
            tempX = (float) (forkliftPositionX - Math.cos(forkliftRotationY*2*Math.PI/360));
            tempZ = (float) (forkliftPositionZ + Math.sin(forkliftRotationY*2*Math.PI/360)); 
            if (tempZ > 290 || tempZ < -290)
               statePosition = 1;
            if (tempZ > 390 || tempZ < -390)
                statePosition = 2;
            if ((statePosition == 1 || statePosition == 2) && ((tempX > 260 || tempX < -260)))
                    return ;
            if (statePosition == 2)
                return;
            if (tempX > 290 || tempX < -290)
               statePosition = 3;
            if (tempX > 390 || tempX < -390)
                statePosition = 4;
            if ((statePosition == 3 || statePosition == 4) && ((tempZ > 180 || tempZ < -180)))
                    return ;
            if (statePosition == 4)
                return;
            
            forkliftPositionX = tempX;
            forkliftPositionZ = tempZ;
            
      }
      if (keys[4]){
          float temp;
          temp = boxPositionY +.1f;
          if (temp > 105)
              return;
          forkPositionY = boxPositionY = temp;
          
        
      }
      if (keys[5]){
          float temp;
          temp = boxPositionY -.1f;
          if (temp < -30)
              return;
          forkPositionY = boxPositionY = temp;
      }
    }
}

