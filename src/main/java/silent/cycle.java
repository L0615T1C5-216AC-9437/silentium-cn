package silent;

import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.effect.Fire;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class cycle extends Thread {
    private Thread MainT;
    private JSONObject data = new JSONObject();

    public cycle(Thread main) {
        MainT = main;
    }

    public void run() {
        Log.info("silentium started - Waiting 60 Seconds");
        Main.cycle = Thread.currentThread();
        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (Exception e) {}
        Log.info("silentium running");
        while (MainT.isAlive()) {
            //sleep
            try {
                TimeUnit.MINUTES.sleep(5);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ///sleep
            //run
            byteCode.reloadList();
        }
    }
}
