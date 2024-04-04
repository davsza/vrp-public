package data;

public class Constants {

    private int PHI;
    private int CHI;
    private int PSI;
    private int OMEGA;
    private int P;
    private int P_WORST;
    private float W;
    private float C;
    private int SIGMA_1;
    private int SIGMA_2;
    private int SIGMA_3;
    private float R;
    private float ETA;
    private float ZETA;
    private String dividerString;

    public Constants() {
        this.PHI = 9;
        this.CHI = 3;
        this.PSI = 2;
        this.OMEGA = 5;
        this.P = 6;
        this.P_WORST = 3;
        this.W = (float) 0.05;
        this.C = (float) 0.99975;
        this.SIGMA_1 = 33;
        this.SIGMA_2 = 9;
        this.SIGMA_3 = 13;
        this.R = (float) 0.1;
        this.ETA = (float) 0.025;
        this.ZETA = (float) 0.4;
        this.dividerString = "============================================================";
    }

    public int getPHI() {
        return PHI;
    }


    public int getCHI() {
        return CHI;
    }

    public int getPSI() {
        return PSI;
    }

    public int getP() {
        return P;
    }

    public int getP_WORST() {
        return P_WORST;
    }

    public float getW() {
        return W;
    }

    public int getSIGMA_1() {
        return SIGMA_1;
    }

    public int getSIGMA_2() {
        return SIGMA_2;
    }

    public int getSIGMA_3() {
        return SIGMA_3;
    }

    public float getR() {
        return R;
    }

    public String getDividerString() {
        return dividerString;
    }
}
