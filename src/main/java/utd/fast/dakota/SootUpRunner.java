package utd.fast.dakota;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.Project;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

public class SootUpRunner 
{
    @Option(name="-t",usage="Path to Target (Java) (Binary)",required=true)
    private String targetPath;

    @Option(name="-cga",usage="Callgraph Algorithm to use, CHA or RTA",required = true)
    private String cga;

    public static void main( String[] args )
    {
       new SootUpRunner().nonStaticMain(args);
    }

    public void nonStaticMain(String[] args){
        CmdLineParser parser = new CmdLineParser(this);
        try{
            parser.parseArgument(args);
            runSootUp();
        }catch(CmdLineException e){
            e.printStackTrace();
            return;
        }

    }

    private void runSootUp() {
        //Method that runs soot up on targetPath
        //from sootup tutorial...
        Path resolvedTargetPath = Path.of(targetPath);
        JavaLanguage language = new JavaLanguage(8);
        AnalysisInputLocation<JavaSootClass> inLoc = new PathBasedAnalysisInputLocation(resolvedTargetPath, SourceType.Application);
        AnalysisInputLocation<JavaSootClass> rtLoc = new JavaClassPathAnalysisInputLocation(
            System.getProperty("java.home")+"/lib/jrt-fs.jar");
        //AnalysisInputLocation<JavaSootClass> rtLoc = new JavaClassPathAnalysisInputLocation(
        //    "/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar");
        JavaProject project = JavaProject.builder(language)
            .addInputLocation(inLoc)
            .addInputLocation(rtLoc)
            .build();



        //create analysis view

        JavaView view = (JavaView) project.createFullView();
        
        ClassType classType = project.getIdentifierFactory().getClassType("cs.utd.soles.App");

        SootClass<JavaSootClassSource> mainClass = (SootClass<JavaSootClassSource>) view.getClass(classType).get();
        

        System.out.println("");
        //MethodSubSignature entrySubSignature = JavaIdentifierFactory.getInstance()
        //    .getMethodSubSignature("main", VoidType.getInstance(), String.type));
        MethodSignature entryMethodSignature = JavaIdentifierFactory.getInstance()
            .getMethodSignature(classType, "main", "void", Collections.singletonList("java.lang.String[]"));
            //.getMethodSignature("main","void",classtype,Collections.singletonList("java.lang.String[]"));
        view.getMethod(entryMethodSignature);



        TypeHierarchy th = view.getTypeHierarchy();

        CallGraphAlgorithm cga_builder = null;
    
        if(cga.equals("RTA")){
            cga_builder = new RapidTypeAnalysisAlgorithm(view,th);
        }else if(cga.equals("CHA")){
            cga_builder = new ClassHierarchyAnalysisAlgorithm(view, th);
        }
        //CallGraphAlgorithm cha = new RapidTypeAnalysisAlgorithm(view, th);
        CallGraph cg = cga_builder.initialize(Collections.singletonList(entryMethodSignature));
        //System.out.println(cg);
        //convert callgraph into closures + dot files
        Set<MethodSignature> mss = cg.getMethodSignatures();

        String dotstring = "digraph callgraph {\n";
        for(MethodSignature ms: mss){
            if(!ms.toString().contains("cs.utd.soles"))
                continue;
            Set<MethodSignature> outCalls= cg.callsFrom(ms);
            for(MethodSignature outCall: outCalls){
                if(!outCall.toString().contains("cs.utd.soles"))
                    continue;
                dotstring+="\""+ms.toString()+"\" -> \""+outCall.toString()+"\";\n";
                
            }
        }
        dotstring+="\n}";
        System.out.println(dotstring);

    }
}
