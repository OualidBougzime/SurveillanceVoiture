



public class MetaVoiture extends Voiture implements Surveillable {
    private int metavitesse;
    private int position;
    private int id;
    private static int _id =0;

    public MetaVoiture(int vitesse){
        super(vitesse);
        this.metavitesse = vitesse;
    }
    public int surveiller(int limite){
        if(this.metavitesse>limite){
            return this.metavitesse - limite;
        }
        else{
            return 0;
        }
    }
}
