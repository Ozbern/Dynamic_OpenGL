package com.example.ozbern.gltest;


import android.opengl.Matrix;
import android.os.SystemClock;

import java.util.Random;

import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by Ozbern on 09.09.2016.
 */
public class TBall {
    private final TBall[] Balls;
    private final float Mass;
    float[] Pos=new float[3];
    float[] SpeedP=new float[3];
    int CountIndex;
    float Rad;
    float[] Color=new float[3];
    float[] rotateBall=new float[4];
    long LastTime;
    final Random random = new Random();
    float[] ballModelMatrix = new float[16];


    public TBall(int fCountIndex, TBall[] balls) {
        CountIndex = fCountIndex;
        Balls=balls;

        Pos[0]=random.nextFloat()*1.5f-0.75f;
        Pos[1]=random.nextFloat()*1.5f-0.75f;
        Pos[2]=random.nextFloat()*1.5f-0.75f;
        rotateBall[0]=random.nextFloat()*360f;//Здесь угол поворота
        rotateBall[1]=random.nextFloat();//здесь пошли оси
        rotateBall[2]=random.nextFloat();
        rotateBall[2]=random.nextFloat();
        Rad=0.2f;//(random.nextFloat()*0.15f+0.1f);
        Mass=(random.nextFloat()*0.35f+0.1f);
        //Цвет зависит от массы. Чем тяжелее, тем краснее
        Color[0]=(Mass-0.1f)*2.85f;
        Color[1]=((float)Math.sqrt(Math.sqrt(1f/Mass))-1.22f)*1.791218f;
        Color[2]=0f;
        LastTime=SystemClock.uptimeMillis();
        SpeedP[0]=(random.nextFloat()+5f)*0.15f-random.nextFloat();
        SpeedP[1]=(random.nextFloat()+5f)*0.15f-random.nextFloat();
        SpeedP[2]=(random.nextFloat()+5f)*0.15f-random.nextFloat();

        Matrix.setIdentityM(ballModelMatrix, 0);
        Matrix.translateM(ballModelMatrix, 0, Pos[0], Pos[1], Pos[2]);
        Matrix.scaleM(ballModelMatrix, 0, Rad, Rad, Rad);
        Matrix.rotateM(ballModelMatrix, 0, rotateBall[0], rotateBall[1], rotateBall[2], rotateBall[3]);

    }
    
    public void Draw(){
        //Зададим матрицу модели для шара
        glUniformMatrix4fv(OpenGLRenderer.uModelMatrix, 1, false, ballModelMatrix, 0);

        for (int i = 0; i < OpenGLRenderer.sphereAlphaSteps; i++) {
            if (i%2==1){
                glUniform4f(OpenGLRenderer.uColorLocation, Color[0], Color[1], Color[2], 1f);
            } else {
                glUniform4f(OpenGLRenderer.uColorLocation, 0f, 0f, 0f, 1f);
            }
            glDrawArrays(GL_TRIANGLE_STRIP, i * OpenGLRenderer.sphereBetaSteps * 2, OpenGLRenderer.sphereBetaSteps * 2);
        }


    }

    public void Move(long time){
        final float Edge=0.999f;
        double Dif;

        Dif=(double)(time-LastTime)/1000.0;
        LastTime=time;

        for (int i = 0; i < 3; i++) {
            Pos[i] = Pos[i] + (float)(Dif * SpeedP[i]);

            if ((Pos[i] + Rad) >= Edge) {
                Pos[i] = Edge - Rad;
                SpeedP[i] = -SpeedP[i];
            } else if (Pos[i] - Rad <= -Edge) {
                Pos[i] = -Edge + Rad;
                SpeedP[i] = -SpeedP[i];
            }
        }
        Matrix.setIdentityM(ballModelMatrix, 0);
        Matrix.translateM(ballModelMatrix, 0, Pos[0], Pos[1], Pos[2]);
        Matrix.scaleM(ballModelMatrix, 0, Rad, Rad, Rad);
        Matrix.rotateM(ballModelMatrix, 0, rotateBall[0], rotateBall[1], rotateBall[2], rotateBall[3]);

    }

    public boolean CheckDist(){
        float dist,u,znamen;
        dvuh newcoord=new dvuh();
        dvuh koef=new dvuh();
        odno speed1=new odno();
        odno speed2=new odno();
        odno pro1=new odno();
        odno pro2=new odno();
        boolean returnResult=false;

        for (int j=0; j<OpenGLRenderer.NumBalls;j++) {
            //Просматриваем все соседние мячи, кроме себя
            if (Balls[j] == this) {
                continue;
            }
            //Определяем расстояние между шарами
            dist = (float) Math.sqrt(
                    Math.pow(Pos[0] - Balls[j].Pos[0], 2.0) +
                            Math.pow(Pos[1] - Balls[j].Pos[1], 2.0) +
                            Math.pow(Pos[2] - Balls[j].Pos[2], 2.0)) - Rad - Balls[j].Rad;
            if (dist <= 0) {
                //Зафиксировали пересечение
                //Теперь необходимо проверить, соответствуют ли скорости пересеченных мечей
                //условию столкновения
                //Для этого необходимо проанализировать направление скоростей и их величину
                //При ударе поперечная составляющая скорости первого мяча ВСЕГДА МЕНЬШЕ
                //поперечной составляющей скорости второго мяча


                //Построим новую систему координат, у которой ось Х проходит через центры шаров
                //Вектор, соединяющий центры шаров av
                newcoord.set(1, 1, Balls[j].Pos[0] - Pos[0]);
                newcoord.set(2, 1, Balls[j].Pos[1] - Pos[1]);
                newcoord.set(3, 1, Balls[j].Pos[2] - Pos[2]);

                //Второй вектор новой системы координат bv
                newcoord.set(1, 2, 0.0f);
                newcoord.set(2, 2, 0.0f);
                newcoord.set(3, 2, 5.0f);
                if (newcoord.get(1, 1) != 0) {
                    newcoord.set(1, 2, -newcoord.get(3, 1) * newcoord.get(3, 2) / newcoord.get(1, 1));
                } else if (newcoord.get(2, 1) != 0) {
                    newcoord.set(2, 2, -newcoord.get(3, 1) * newcoord.get(3, 2) / newcoord.get(2, 1));
                }

                //Третий вектор новой системы координат cv находим векторным умножением первых двух
                newcoord.set(1, 3, newcoord.get(2, 1) * newcoord.get(3, 2) - newcoord.get(3, 1) * newcoord.get(2, 2));
                newcoord.set(2, 3, -(newcoord.get(1, 1) * newcoord.get(3, 2) - newcoord.get(3, 1) * newcoord.get(1, 2)));
                newcoord.set(3, 3, newcoord.get(1, 1) * newcoord.get(2, 2) - newcoord.get(2, 1) * newcoord.get(1, 2));

                //Теперь спроецируем вектора скорости двух наших шаров на новую систему координат
                //Построим матрицу 3 на 3 коэфициентов наших уравнений
                for (int k = 1; k <= 3; k++) {
                    znamen = (float) Math.sqrt(newcoord.get(1, k) * newcoord.get(1, k) +
                            newcoord.get(2, k) * newcoord.get(2, k) +
                            newcoord.get(3, k) * newcoord.get(3, k));
                    for (int l = 1; l <= 3; l++) {
                        koef.set(k, l, newcoord.get(l, k) / znamen);
                    }

                }
                //Теперь имея коэффициенты находим проекции векторов скоростей на новую систему координат
                speed1.set(1, SpeedP[0]);
                speed1.set(2, SpeedP[1]);
                speed1.set(3, SpeedP[2]);

                speed2.set(1, Balls[j].SpeedP[0]);
                speed2.set(2, Balls[j].SpeedP[1]);
                speed2.set(3, Balls[j].SpeedP[2]);

                //и посчитаем проекции
                for (int k = 1; k <= 3; k++) {
                    pro1.set(k, 0.0f);
                    pro2.set(k, 0.0f);
                    for (int l = 1; l <= 3; l++) {
                        pro1.set(k, pro1.get(k) + speed1.get(l) * koef.get(k, l));
                        pro2.set(k, pro2.get(k) + speed2.get(l) * koef.get(k, l));
                    }

                }

                //Теперь можно проверять условие столкновения и пересчитывать скорости
                if (pro1.get(1) > pro2.get(1)) {
                    returnResult = true;//Сообщаем наверх, что на этой итерации был удар,
                    //Удар
                    //Изменяем скорость обоих мячей
                    u = (Mass * pro1.get(1) + Balls[j].Mass * pro2.get(1)) / (Mass + Balls[j].Mass);
                    pro1.set(1, 2 * u - pro1.get(1));
                    pro2.set(1, 2 * u - pro2.get(1));

                    speed1.setWholeArray(solveOpr(koef, pro1));;
                    speed2.setWholeArray(solveOpr(koef, pro2));

                    SpeedP[0] = speed1.get(1);
                    SpeedP[1] = speed1.get(2);
                    SpeedP[2] = speed1.get(3);

                    Balls[j].SpeedP[0] = speed2.get(1);
                    Balls[j].SpeedP[1] = speed2.get(2);
                    Balls[j].SpeedP[2] = speed2.get(3);
                }
            }
        }
        return returnResult;
    }

    public odno solveOpr(dvuh koefs,odno svob){
        dvuh writekoefs=new dvuh();
        odno returnArray=new odno();
        double opr;
        opr=koefs.get(1,1)*koefs.get(2,2)*koefs.get(3,3)+
                koefs.get(2,1)*koefs.get(3,2)*koefs.get(1,3)+
                koefs.get(1,2)*koefs.get(2,3)*koefs.get(3,1)-
                koefs.get(1,3)*koefs.get(2,2)*koefs.get(3,1)-
                koefs.get(1,2)*koefs.get(2,1)*koefs.get(3,3)-
                koefs.get(2,3)*koefs.get(3,2)*koefs.get(1,1);
        for (int m = 1; m <= 3; m++) {
            writekoefs.setWholeArray(koefs);
            for (int g = 1; g <= 3; g++) {
                writekoefs.set(g,m,svob.get(g));
            }
            returnArray.set(m,
                    (float)((
                            writekoefs.get(1,1)*writekoefs.get(2,2)*writekoefs.get(3,3)+
                                    writekoefs.get(2,1)*writekoefs.get(3,2)*writekoefs.get(1,3)+
                                    writekoefs.get(1,2)*writekoefs.get(2,3)*writekoefs.get(3,1)-
                                    writekoefs.get(1,3)*writekoefs.get(2,2)*writekoefs.get(3,1)-
                                    writekoefs.get(1,2)*writekoefs.get(2,1)*writekoefs.get(3,3)-
                                    writekoefs.get(2,3)*writekoefs.get(3,2)*writekoefs.get(1,1)
                    )/opr));
        }
        return returnArray;
    }

    class odno{
        float[] fArray=new float [3];
        public float get(int i){
            return fArray[i-1];
        }
        public void set(int i,float item){
            fArray[i-1]=item;
        }
        public void setWholeArray(odno koefs) {
            System.arraycopy(koefs.getfArray(),0,fArray,0,3);
        }
        public float[] getfArray(){
            return fArray;
        }
    }

    class dvuh{
        float[][] fArray=new float [3][3];
        public float get(int i,int j){
            return fArray[i-1][j-1];
        }
        public float[][] getfArray(){
            return fArray;
        }
        public void set(int i,int j,float item){
            fArray[i-1][j-1]=item;
        }

        public void setWholeArray(dvuh koefs) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    fArray[i][j]=koefs.getfArray()[i][j];
                }
            }
        }
    }


}
