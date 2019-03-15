package com.example.ozbern.gltest;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BACK;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_FRONT;
import static android.opengl.GLES20.GL_LINE_STRIP;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCullFace;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class OpenGLRenderer implements Renderer {
    private Context context;
    private int programId;
    private FloatBuffer vertexData;
    private FloatBuffer linesVertexBuffer;
    public static int uColorLocation;
    private int aPositionLocation;
    private int uMatrixLocation;
    public static int uModelMatrix;
    private float[] mProjectionMatrix=new float[16];
    private float[] mViewMatrix=new float[16];
    private float[] mTotalMatrix=new float[16];
    public static float[] mCageMatrixArray =new float[16];
    private float[] mCurrentRotation=new float[16];
    private float[] mAccumulatedRotation = new float[16];
    private float[] mTemporaryMatrix = new float[16];
    public static int sphereAlphaSteps;
    public static int sphereBetaSteps;
    private int uDisableLight;
    public float mTouchDeltaX;
    public float mTouchDeltaY;

    public static final int NumBalls=4;
    public static TBall[] Balls=new TBall[NumBalls];



    private void bindMatrix(int width, int height) {
        float top;
        float bottom;
        float left;
        float right;

        if (width >= height) {
            top = 0.8f;
            bottom = -0.8f;
            left = -0.8f*width/height;
            right = 0.8f*width/height;
        } else {
            top = 0.8f*height/width;
            bottom = -0.8f*height/width;
            left = -0.8f;
            right = 0.8f;

        }
        float near = 1.5f;
        float far = 15.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        Matrix.setLookAtM(mViewMatrix, 0, 2.8f, 1.5f, 2.8f, 0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.setIdentityM(mAccumulatedRotation, 0);



    }

    public OpenGLRenderer(Context context) {
        this.context = context;
        prepareData();
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        glClearColor(0f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);
        glEnable (GL_BLEND);
        glEnable (GL_CULL_FACE);
        glCullFace(GL_BACK);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        int vertexShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader);
        int fragmentShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader);
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        glUseProgram(programId);
        bindData();
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        bindMatrix(width, height);
    }

    private void prepareData() {
        double radius=1.0f;
        double alphaBegin=0.0f;
        double betaBegin=(float)(Math.PI/2.0);
        double betaEnd=(float)(-Math.PI/2.0);
        double alphaEnd=(float)(Math.PI*2.0);
        final Random random = new Random();

        //Устанавливаем детализацию сферы
        sphereAlphaSteps = 20;
        sphereBetaSteps = 20;

        double alphaDif=(alphaEnd-alphaBegin)/sphereAlphaSteps;
        double betaDif=(betaEnd-betaBegin)/sphereBetaSteps;

        double alpha;
        double beta;

        double rad1;
        float[] oneVertex=new float[3];

        //Расчитаем координаты сферы. Заполним буфер
        //Выделяем по 3 float на каждую вершину. По 4 байта на float
        vertexData = ByteBuffer.allocateDirect(sphereAlphaSteps*sphereBetaSteps * 2 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 1; i <= sphereAlphaSteps; i++) {
            for (int j = 1; j <= (sphereBetaSteps+1); j++) {
                //За один цикл добавляем 2 вершины из текущего и следующего i
                for (int k = 1; k >= 0; k--) {
                    alpha=alphaBegin+(i+k-1)*alphaDif;
                    //Первая вершина
                    beta=betaBegin+(j-1)*betaDif;
                    rad1 = (float)(Math.cos(beta)*radius);
                    //x
                    oneVertex[0] = (float) (Math.cos(alpha) * rad1);
                    //y
                    oneVertex[1] = (float) (Math.sin(beta)*radius);
                    //z
                    oneVertex[2] = (float) (Math.sin(alpha)*rad1);
                    vertexData.put(oneVertex);

                    //На вервом и последнем проходе цикла j добавляем по одной вершине, т.к. это верхушки сектора сферы
                    if ((j==1)||(j==(sphereBetaSteps+1))){
                        //Прерываем внутренний цикл k
                        break;
                    }

                }
            }
            
        }
        
        //Заполним массив вершин для сетки
        float[] lineVertexArray = new float[3];
        int numLines=10;
        linesVertexBuffer = ByteBuffer.allocateDirect(13*3*4*10).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i=1;i<=numLines;i++){
            //Первая группа линий
            lineVertexArray[0]=-1f;
            lineVertexArray[1]=-1f;
            lineVertexArray[2]=(float)(2.0 / (numLines + 1) * i - 1);
            linesVertexBuffer.put(lineVertexArray);

            lineVertexArray[0]=-1f;
            lineVertexArray[1]=1f;
            linesVertexBuffer.put(lineVertexArray);

            lineVertexArray[0]=1f;
            lineVertexArray[1]=1f;
            linesVertexBuffer.put(lineVertexArray);

            lineVertexArray[0]=1f;
            lineVertexArray[1]=-1f;
            linesVertexBuffer.put(lineVertexArray);

            lineVertexArray[0]=-1f;
            lineVertexArray[1]=-1f;
            linesVertexBuffer.put(lineVertexArray);

            //Вторая группа линий
            lineVertexArray[0]=-1f;
            lineVertexArray[1]=(float)(2.0 / (numLines + 1) * i - 1);
            lineVertexArray[2]=(float)(1.0 - 2.0 / (numLines + 1));
            linesVertexBuffer.put(lineVertexArray);

            lineVertexArray[0]=-1f;
            lineVertexArray[2]=1f;
            linesVertexBuffer.put(lineVertexArray);

            lineVertexArray[0]=1f;
            lineVertexArray[2]=1f;
            linesVertexBuffer.put(lineVertexArray);

            lineVertexArray[0]=1f;
            lineVertexArray[2]=(float)(1.0 - 2.0 / (numLines + 1));
            linesVertexBuffer.put(lineVertexArray);

            //Третья группа линий
            lineVertexArray[0]=1f;
            lineVertexArray[1]=(float)(2.0 / (numLines + 1) * i - 1);
            lineVertexArray[2]=(float)(-1.0 + 2.0 / (numLines + 1));
            linesVertexBuffer.put(lineVertexArray);

            lineVertexArray[0]=1f;
            lineVertexArray[2]=-1f;
            linesVertexBuffer.put(lineVertexArray);

            lineVertexArray[0]=-1f;
            lineVertexArray[2]=-1f;
            linesVertexBuffer.put(lineVertexArray);

            lineVertexArray[0]=-1f;
            lineVertexArray[2]=(float)(-1.0 + 2.0 / (numLines + 1));
            linesVertexBuffer.put(lineVertexArray);

        }

        //Создадим массив шаров TBall
        for (int i=0;i<NumBalls;i++){
            Balls[i]=new TBall(i,Balls);
            Balls[i].CountIndex=i;
        }

        //Очистим матрицу модели для клетки
        Matrix.setIdentityM(mCageMatrixArray, 0);


    }

    private void bindData() {
        //матрица перспективы
        uMatrixLocation = glGetUniformLocation(programId, "u_Matrix");

        //матрица модели
        uModelMatrix = glGetUniformLocation(programId, "u_ModelMatrix");


        //координаты
        aPositionLocation = glGetAttribLocation(programId, "a_Position");

        //цвет
        uColorLocation = glGetUniformLocation(programId, "u_Color");

        //выключатель освещенности
        uDisableLight = glGetUniformLocation(programId, "u_DisableLight");

    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //Повернем матрицу текущего поворота при касании
        Matrix.setIdentityM(mCurrentRotation, 0);
        Matrix.rotateM(mCurrentRotation, 0, mTouchDeltaX, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, mTouchDeltaY, 1.0f, 0.0f, 0.0f);
        mTouchDeltaX = 0.0f;
        mTouchDeltaY = 0.0f;

        //Перенесем текущий поворот в накопительный поворот
        Matrix.multiplyMM(mTemporaryMatrix, 0, mCurrentRotation, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mAccumulatedRotation, 0, 16);

        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 3.15f, 0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.multiplyMM(mTemporaryMatrix, 0, mViewMatrix, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mViewMatrix, 0, 16);


        Matrix.multiplyMM(mTotalMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        glUniformMatrix4fv(uMatrixLocation, 1, false, mTotalMatrix, 0);

        //Рисуем шары
        //Подключим массив треугольников для сферы перед рисованием
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT, false, 12, vertexData);
        glEnableVertexAttribArray(aPositionLocation);
        glUniform1f(uDisableLight, 0.0f); //включим модель освещения в шейдере
        long time = SystemClock.uptimeMillis();
        for (int i = 0; i < NumBalls; i++) {

            Balls[i].Move(time);
        }
        //В этом цикле добиваемся, чтобы проверка на столкновение не выдала ни одного удара
        //Повторяем до тех пор, пока не пересчитаем все скорости шаров так, чтобы столкновений не осталось
        boolean goOn;
        do {
            goOn=false;//Сначала предполагаем, что мячи сейчас не соприкасаются и повторной проверки не надо
            for (int i = 0; i < NumBalls; i++) {
                goOn=(goOn || Balls[i].CheckDist());
                //Если был удар, то CheckDist вернет true и цикл repeat повториться
                //А если ни одного удара не произошло, то в goOn останется false и цикл не повториться
            }
        } while (true==false);

        for (int i = 0; i < NumBalls; i++) {
            Balls[i].Draw();
        }


        //Рисуем клетку из линий
        //подключим массив координат линий
        linesVertexBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT, false, 12, linesVertexBuffer);
        glEnableVertexAttribArray(aPositionLocation);

        glUniform4f(uColorLocation, 0f, 1f, 1f, 0.2f);
        glUniformMatrix4fv(uModelMatrix, 1, false, mCageMatrixArray, 0);

        //Отключим модель освещения в шейдере
        glUniform1f(uDisableLight, 1.0f);
        for (int i= 1; i <= 10; i++) {
            glDrawArrays(GL_LINE_STRIP, (i-1)*13, 5);
            glDrawArrays(GL_LINE_STRIP, 5+(i-1)*13, 4);
            glDrawArrays(GL_LINE_STRIP, 9+(i-1)*13, 4);
        }
    }

    public void identityAccumulationRotation() {
        Matrix.setIdentityM(mAccumulatedRotation, 0);
    }
}