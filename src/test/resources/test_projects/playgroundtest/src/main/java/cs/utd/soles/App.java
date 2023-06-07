package cs.utd.soles;
import java.lang.reflect.*;

public class App {
    public static void main(String[] args) {
        

        if(args[0].equals("server")){
            bar();
        }
        try{
        Method reflectEdge = App.class.getMethod("bad_foo",int.class);
        reflectEdge.invoke(0);
        }catch(Exception e){
            e.printStackTrace();
        }
        thirdMethod();
    }

    public static void bad_foo(int x){
        int i=0;
        i = i /x;

    }
    public static void bar(){
        startServer();
    }
    public static void startServer(){
        ///....//// Extended Method Call////
    }
    public static void thirdMethod(){

    }

}
