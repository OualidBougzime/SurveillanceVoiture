package Voiture;

public class Voiture {
    private int vitesse;
    private int position;
    private int id;

    private static int _id = 0;



    public Voiture(int vitesse){

        this.vitesse = vitesse;
        this.position = 0;
        this.id = Voiture._id;
        Voiture._id++;
    }

    public void deplacement(){
        position += vitesse;

    }

    public int getVitesse(){
        return vitesse;

    }

    public int getPosition(){
        return position;
    }

    public int getId(){
        return id;
    }

    @Override
    public String toString(){
        return "La voiture numero : " + id + " avec la position : " + position
                + " roule avec une vitesse de "
                + vitesse + ".";
    }

}
