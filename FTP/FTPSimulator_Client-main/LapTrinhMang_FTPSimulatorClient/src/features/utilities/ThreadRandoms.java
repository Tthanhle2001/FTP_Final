
package features.utilities;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class ThreadRandoms {

    public static int generateCode() {
        return ThreadLocalRandom.current().nextInt(1000, 10000 + 1);
    }
    
    public static String uuid(){
        return UUID.randomUUID().toString();
    }
    
    public static void main(String[] args) {
        System.out.println(ThreadRandoms.uuid());
    }
}
