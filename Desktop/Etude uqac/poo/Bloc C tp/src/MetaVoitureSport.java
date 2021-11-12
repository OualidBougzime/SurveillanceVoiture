


public class MetaVoitureSport extends VoitureSport implements Surveillable {
    private int vitesse;
    private int position;
    private int id;
    private static int _id =0;

    public void MetaVoitureSport(){
        new VoitureSport();
    }

    public int surveiller(int limite){
        if(this.vitesse>limite){
            return this.vitesse - limite;
        }
        else{
            return 0;
        }
    }



}
