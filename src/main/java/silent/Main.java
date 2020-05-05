package silent;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.plugin.Plugin;
import mindustry.Vars;
import mindustry.Vars.*;
import org.json.JSONObject;

import java.util.HashMap;

import static mindustry.Vars.*;

public class Main extends Plugin {
    //Var
    public static JSONObject adata = new JSONObject();
    public static HashMap<String, HashMap<String, String>> list = new HashMap<>();
    public static Thread cycle;
    public static HashMap<String, Long> dtList = new HashMap<>();
    Boolean enabled = false;

    ///Var
    //on start
    public Main() {
        byteCode.assertCore("silentium");
        adata = byteCode.get("silentium");
        if (adata == null) {
            Log.err("Invalid file - " + System.getProperty("user.home") + "/mind_db/deterioration.cn");
            Log.info("Reset file using command `dt-clear`");
            return;
        }
        enabled = true;
        silent.cycle a = new cycle(Thread.currentThread());
        a.setDaemon(false);
        a.start();
        Events.on(EventType.WorldLoadEvent.class, event -> {
            if (enabled) {
                if (!cycle.isAlive()) {
                    silent.cycle b = new cycle(Thread.currentThread());
                    b.setDaemon(false);
                    b.start();
                }
                netServer.admins.addChatFilter((player, text) -> null);
            }
        });
        Events.on(EventType.PlayerJoin.class, event -> {
            Player player = event.player;
            if (adata.has(player.uuid)) {
                JSONObject data = adata.getJSONObject(player.uuid);
                if (!data.isEmpty()) {
                    HashMap<String, String> empty = new HashMap<>();
                    Main.list.put(player.uuid, empty);
                    for (String keyStr : data.keySet()) {
                        Main.list.get(player.uuid).put(keyStr, "");
                    }
                }
            }
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            Player player = event.player;
            list.remove(player.uuid);
        });

        Events.on(EventType.PlayerChatEvent.class, event -> {
            Player player = event.player;
            if (!event.message.startsWith("/")) {
                Log.info(byteCode.noColors(player.name+": "+event.message));
                for (Player p : playerGroup.all()) {
                    if (list.containsKey(p.uuid) && list.get(p.uuid).containsKey(player.uuid)) {
                        p.sendMessage("[coral][[[][#"+player.color+"]"+player.name+"\u0001[coral]]: [lightgray]<Blocked Message>");
                        continue;
                    } else {
                        if (list.containsKey(player.uuid) && list.get(player.uuid).containsKey(p.uuid)) {
                            continue;
                        }
                        p.sendMessage("[coral][[[][#"+player.color+"]"+player.name+"\u0001[coral]]: [white]"+event.message);
                    }
                }
            }
        });
    }

    public void registerServerCommands(CommandHandler handler) {

    }
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("block", "[player-id]","blocks or unblock a player.", (arg, player) -> {
            if (arg.length == 0) {
                if (Vars.playerGroup.size() > 1) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Online Players:");
                    for (Player p : Vars.playerGroup.all()) {
                        if (p.uuid.equals(player.uuid)) continue;
                        builder.append("\n").append("[accent]#").append(p.id).append("[] : ").append(byteCode.noColors(p.name));
                    }
                    player.sendMessage(builder.toString());
                } else {
                    player.sendMessage("No One available to block");
                }
            } else {
                if (enabled) {
                    if (Strings.canParseInt(arg[0])) {
                        Player p = playerGroup.getByID(Strings.parseInt(arg[0]));
                        if (p == null || p.uuid == null) {
                            player.sendMessage("error");
                            return;
                        }
                        if (p.uuid.equals(player.uuid)) {
                            player.sendMessage("You can't block yourself.");
                            return;
                        }
                        //data stuff
                        byteCode.assertCore("silentium");
                        adata = byteCode.get("silentium");
                        JSONObject data = new JSONObject();
                        if (adata.has(player.uuid)) {
                            data = adata.getJSONObject(player.uuid);
                        } else {
                            byteCode.putJObject("silentium", player.uuid, data);
                        }
                        //ban unban
                        if (data.has(p.uuid)) {
                            list.get(player.uuid).remove(p.uuid);
                            data.remove(p.uuid);
                            adata.put(player.uuid, data);
                            byteCode.save("silentium", adata);
                            player.sendMessage("unBlocked " + p.name);
                        } else {
                            if (!list.containsKey(player.uuid)) {
                                HashMap<String, String> empty = new HashMap<>();
                                Main.list.put(player.uuid, empty);
                            }
                            Main.list.get(player.uuid).put(p.uuid, "");
                            data.put(p.uuid, "");
                            adata.put(player.uuid, data);
                            byteCode.save("silentium", adata);
                            player.sendMessage("Blocked " + p.name);
                        }
                    } else {
                        player.sendMessage("Player id must contain numbers! [lightgray]Do `/block` to see player id");
                    }
                } else {
                    player.sendMessage("Plugin [scarlet]Error[] - Command is disabled");
                }
            }
        });
    }
}