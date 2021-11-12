import dynamique.ByteArrayClass;
import dynamique.ByteArrayClasseLoader;
import dynamique.StringSource;

import javax.tools.*;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


import java.lang.*;

public class VoitureFactory {
    public enum ModeConstruction {INSTANCIATION, REFLEXION, META};

    public static Voiture buildVoiture(ModeConstruction mode, boolean sport, int vitesse){
        if(mode == ModeConstruction.INSTANCIATION){
            if(sport == true){
                return new VoitureSport();
            }
            else{
                return new Voiture(vitesse);
            }
        }

        else if(mode == ModeConstruction.REFLEXION){
            try {
                if (sport == true) {
                    Class v = Class.forName("VoitureSport");
                    return (VoitureSport) v.getDeclaredConstructor().newInstance();

                } else {
                    Class v = Class.forName("Voiture");
                    return (Voiture) v.getDeclaredConstructor(int.class).newInstance();

                }
            }
            catch(ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e){
                e.printStackTrace();
            }
        }

        else{
            // ******** ETAPE #1 : Préparation pour la compilation
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            List<ByteArrayClass> classes = new ArrayList<>();           // pour mettre les .class   (IMPORTANT)
            DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();
            JavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

            // La classe qui se charge de fournir les "conteneurs" au compilateur à la volée, sans accès au disque
            fileManager = new ForwardingJavaFileManager<JavaFileManager>(fileManager){
                public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
                                                           FileObject sibling) throws IOException{
                    if (kind == JavaFileObject.Kind.CLASS){
                        ByteArrayClass outFile = new ByteArrayClass(className);
                        classes.add(outFile);           // ICI IMPORTANT
                        return outFile;
                    }
                    else
                        return super.getJavaFileForOutput(location, className, kind, sibling);
                }
            };
            String monClasse;
            if(sport == true){
                monClasse = "MetaVoitureSport";
            }
            else{
                monClasse = "MetaVoiture";
            }
            // ******** ETAPE #2 : Génération du code source
            List<JavaFileObject> sources = List.of(
                    buildSource(monClasse, vitesse, sport)
            );
            // ******** ETAPE #3 : Compilation
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
            // ******** ETAPE #4 : Instanciation
            ByteArrayClasseLoader loader = new ByteArrayClasseLoader(classes);
            try{
                if(sport == true){
                    return (Voiture) Class.forName("MetaVoitureSport", true, loader).getDeclaredConstructor().newInstance();
                }
                else{
                    return (Voiture) Class.forName("MetaVoiture", true, loader).getDeclaredConstructor().newInstance();
                }
            }
            catch (ClassNotFoundException | NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

            public static JavaFileObject buildSource(String monClasse, int vitesse, boolean sport) {

                StringBuilder sb = new StringBuilder();

                sb.append("package dynamique;\n");
                if(sport == true) {
                    sb.append("public class " + monClasse + " extends VoitureSport implements Surveillable{\n");
                }
                else{
                    sb.append("public class " + monClasse + " extends Voiture implements Surveillable{\n");

                }
                genererAttributs(sb);
                genererConstructeurs(monClasse, vitesse, sb);
                genererMethodes(sb);
                sb.append("}\n");

                System.out.println("LA CLASSE");
                System.out.println(sb);

                return new StringSource(monClasse, sb.toString());
            }

            private static void genererConstructeurs(String monClasse, int vitesse, StringBuilder sb) {
                if(monClasse.equals("MetaVoitureSport")) {
                    sb.append("public " + monClasse + "(){super();\n" + "metaVitesse = 200;}\n");
                }
                else{
                    sb.append("public " + monClasse + "(){super(" + vitesse + ");\n" + "metaVitesse = " + vitesse +";}\n");

                }
            }

            private static void genererMethodes(StringBuilder sb) {

                sb.append("@Override public int surveiller( int limite){ \n" +
                "return metaVitesse = limite;\n" + "}\n");
            }

            private static void genererAttributs(StringBuilder sb) {

                sb.append("private int metaVitesse;\n");
            }
    }

