package com.jelly.farmhelper.features;

import akka.actor.MinimalActorRef;
import com.jelly.farmhelper.config.interfaces.MiscConfig;
import com.jelly.farmhelper.gui.DisconnectGUI;
import com.jelly.farmhelper.macros.MacroHandler;
import com.jelly.farmhelper.network.APIHelper;
import com.jelly.farmhelper.utils.Clock;
import com.jelly.farmhelper.utils.LogUtils;
import gg.essential.elementa.state.BasicState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.lwjgl.opengl.Display;

import java.util.LinkedList;

public class BanwaveChecker {
    private Minecraft mc = Minecraft.getMinecraft();
    public static final BasicState<String> staffBan = new BasicState<>("Staff ban : NaN");


    private static final Clock cooldown = new Clock();
    private static final LinkedList<Integer> staffBanLast15Mins = new LinkedList<>();
    public static boolean banwaveOn = false;
    @SubscribeEvent
    public final void tick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END)
            return;
        if(!cooldown.isScheduled() || cooldown.passed()){
            new Thread(() -> {
                try {


                    String s = APIHelper.readJsonFromUrl("https://api.plancke.io/hypixel/v1/punishmentStats", "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
                            .get("record").toString();
                    JSONParser parser = new JSONParser();
                    JSONObject record = (JSONObject) parser.parse(s);

                    staffBanLast15Mins.addLast((Integer.parseInt(record.get("staff_total").toString())));
                    if(staffBanLast15Mins.size() == 17) staffBanLast15Mins.removeFirst();

                    staffBan.set(getBanDisplay());

                    if(getBanTimeDiff() != 0)
                         banwaveOn = getBanDiff() / (getBanTimeDiff() * 1.0f) > MiscConfig.banThreshold / 15.0f;

                    if(MacroHandler.isMacroing && MiscConfig.banwaveDisconnect) {
                        if (banwaveOn && mc.theWorld != null) {
                            LogUtils.webhookLog("Disconnecting due to banwave detected");
                            this.mc.theWorld.sendQuittingDisconnectingPacket();
                        }
                    }

                } catch(Exception e){
                    e.printStackTrace();
                }
            }).start();
            cooldown.schedule(60000);

        }
        if(!banwaveOn && mc.currentScreen instanceof GuiDisconnected && MiscConfig.banwaveDisconnect){
            AutoReconnect.reconnectToHypixel();
        }

    }
    private static int getBanTimeDiff(){
        return staffBanLast15Mins.size() > 1 ? staffBanLast15Mins.size() - 1 : 0;
    }
    private static int getBanDiff(){
        return staffBanLast15Mins.size() > 1 ? Math.abs(staffBanLast15Mins.getLast() - staffBanLast15Mins.getFirst()) : 0;
    }
    public static String getBanDisplay(){
        return getBanTimeDiff() > 0 ? "Staff ban in last " + getBanTimeDiff() + " minutes : " + getBanDiff() : "Staff ban : Collecting data...";
    }

}
