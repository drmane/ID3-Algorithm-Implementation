
import view.MainFrame;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Daniel y Ricardo
 */
public class StartClass {
    
    
    /**
     * Función main
     */
    public static void main(String args[]) {
        
        //Se leen el número de argumentos de la entrada
        
        
        int num = args.length;

        //Dos argumentos de entrada

        if (num == 2) {
               new MainFrame(args[0],args[1]);
        }
        else {
            
            System.err.println("El número de argumentos de entrada es erroneo, cargando nombre de ficheros por defecto " );
               //Nombre de los ficheros por defecto
               new MainFrame("AtributosJuego.txt", "Juego.txt"); 
            
        }
    }
}
