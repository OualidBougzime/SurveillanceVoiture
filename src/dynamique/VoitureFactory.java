package dynamique;

import Voiture.Voiture;
import Voiture.VoitureSport;

import javax.tools.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class VoitureFactory {
    public enum ModeConstruction {INSTANCIATION, REFLEXION, META}

    public static Voiture buildVoiture(ModeConstruction mode, boolean sport, int vitesse){
        switch (mode) {
            case INSTANCIATION -> {
                if (sport)
                    return new VoitureSport();
                else
                    return new Voiture(vitesse);
            }

            case REFLEXION -> {
                try {
                    if (sport) {
                        Class classVoiture = Class.forName("Voiture.VoitureSport");
                        return (VoitureSport) classVoiture.getDeclaredConstructor().newInstance();
                    }
                    else {
                        Class classVoiture = Class.forName("Voiture.Voiture");
                        return (Voiture) classVoiture.getDeclaredConstructor(int.class).newInstance(vitesse);
                    }
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            case META -> {

                // **** ETAPE #1 : Préparation pour la compilation
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                List<ByteArrayClass> classes = new ArrayList<>();           // pour mettre les .class   (IMPORTANT)
                DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();
                JavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

                // La classe qui se charge de fournir les "conteneurs" au compilateur à la volée, sans accès au disque
                fileManager = new ForwardingJavaFileManager<JavaFileManager>(fileManager){
                    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
                                                               FileObject sibling) throws IOException {
                        if (kind == JavaFileObject.Kind.CLASS){
                            ByteArrayClass outFile = new ByteArrayClass(className);
                            classes.add(outFile);           // ICI IMPORTANT
                            return outFile;
                        }
                        else
                            return super.getJavaFileForOutput(location, className, kind, sibling);
                    }
                };
                String nomClasse;
                if (sport)
                    nomClasse = "MetaVoitureSport";
                else
                    nomClasse = "MetaVoiture";

                // **** ETAPE #2 : Génération du code source
                List<JavaFileObject> sources = List.of(
                        buildSource(nomClasse, vitesse, sport)
                );

                // **** ETAPE #3 : Compilation
                JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, collector, null,
                        null, sources);
                Boolean result = task.call();

                for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics())
                    System.out.println(d);

                try {
                    fileManager.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!result) {
                    System.out.println("ECHEC DE LA COMPILATION");
                    System.exit(1);
                }


                // **** ETAPE #4 : Instanciation
                ByteArrayClasseLoader loader = new ByteArrayClasseLoader(classes);

                try {
                    if (sport)
                        return (Voiture) Class.forName("dynamique.MetaVoitureSport", true, loader).getDeclaredConstructor().newInstance();
                    else
                        return (Voiture) Class.forName("dynamique.MetaVoiture", true, loader).getDeclaredConstructor().newInstance();
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }


    public static JavaFileObject buildSource(String nomClasse, int vitesse, boolean sport) {

        StringBuilder sb = new StringBuilder();

        sb.append("package dynamique;\n");
        if (sport)
            sb.append("public class " + nomClasse + " extends Voiture.VoitureSport implements Voiture.Surveillable{\n");
        else
            sb.append("public class " + nomClasse + " extends Voiture.Voiture implements Voiture.Surveillable{\n");
        genererAttributs(sb);
        genererConstructeurs(nomClasse, vitesse, sb);
        genererMethodes(sb);
        sb.append("}\n");

        System.out.println("LA CLASSE");
        System.out.println(sb);

        return new StringSource(nomClasse, sb.toString());
    }

    private static void genererConstructeurs(String nomClasse, int vitesse, StringBuilder sb) {

        if (nomClasse.equals("MetaVoitureSport"))
            sb.append("public " + nomClasse + "(){super();\n" +
                    "metaVitesse = 200;}\n");
        else
            sb.append("public " + nomClasse + "(){super(" + vitesse + ");\n" +
                    "metaVitesse = " + vitesse + ";}\n");
    }

    private static void genererMethodes(StringBuilder sb) {
        sb.append("@Override public int surveiller(int limite){ \n" +
                "return metaVitesse - limite;\n" +
                "}\n");
    }

    private static void genererAttributs(StringBuilder sb) {
        sb.append("private int metaVitesse;");
    }
}