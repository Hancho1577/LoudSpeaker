package loudSpeaker;

import java.util.ArrayList;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.Element;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import hFriend.HFriend;
import me.onebone.economyapi.EconomyAPI;

public class LoudSpeaker extends PluginBase implements Listener{
	private static final String PREFIX = "§l§f[ §d! §f] ";
	private static final int FORM_ID = 7189;
	private HFriend friendAPI;
	private EconomyAPI economyApi;
	
	@Override
	public void onEnable() {
		if(getServer().getPluginManager().getPlugin("EconomyAPI") == null) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		this.economyApi = (EconomyAPI) getServer().getPluginManager().getPlugin("EconomyAPI");
		
		Plugin api = getServer().getPluginManager().getPlugin("HFriend");
		if(api != null) {
			this.friendAPI = (HFriend) api;
		}
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equals("확성기")) {
			showForm((Player) sender);
			return true;
		}
		return true;
	}
	
	@EventHandler
	public void onRepond(PlayerFormRespondedEvent ev) {
		if(ev.getWindow() == null) {
			return;
		}
		if(ev.getResponse() == null) {
			return;
		}
		Player player = ev.getPlayer();
		String name = player.getName();
		FormWindow window = ev.getWindow();
		if(window instanceof FormWindowCustom) {
			FormResponseCustom response = (FormResponseCustom) ev.getResponse();
			if(ev.getFormID() == FORM_ID) {
				boolean friendMode; 
				String content;				
				if(economyApi.myMoney(player) < 1000) {
					FormWindowSimple form = new FormWindowSimple("§l§0확성기", PREFIX + "확성기를 사용하려면 최소 §d1000원§f 이상 보유해야합니다.");
					player.showFormWindow(form);
					return;
				}
				
				if(isAPIEnabled()) {
				 friendMode =  response.getToggleResponse(1);
				 content = response.getInputResponse(2);
				} else {
					friendMode =  false;
					 content = response.getInputResponse(1);
				}
				
				if(content == null) {
					return;
				}
				if(content.isEmpty()) {
					return;
				}
				
				economyApi.reduceMoney(player, 1000);
				
				if(friendMode) {
					String message = "§d§l========== §f친구 확성기 §d==========\n\n§b" + name + "§f : " + content + "\n\n§d========== §f친구 확성기 §d==========";
					for(String friend : friendAPI.getOnlineFriends(name)) {
						getServer().getPlayerExact(friend).sendMessage(message);
					}
					player.sendMessage(message);
					getLogger().info(message);
				} else {
					String message = "§d§l========== §f 확성기 §d==========\n\n§b" + name + "§f : " + content + "\n\n§d========== §f확성기 §d==========";
					getServer().broadcastMessage(message);
				}
			}
		}
	}
	
	public boolean isAPIEnabled() {
		if(this.friendAPI == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public void showForm(Player player) {
		ArrayList<Element> formElements = new ArrayList<Element>();
		
		formElements = new ArrayList<Element>();
		formElements.add(new ElementLabel(PREFIX + "§l확성기 사용시 1000원이 차감됩니다."));
		if(isAPIEnabled()) {
			formElements.add(new ElementToggle("§l§f친구에게만 전송 (§d" + friendAPI.getOnlineFriends(player.getName()).size() + "§f명 )"));
		}
		formElements.add(new ElementInput("§l내용"));
		
		FormWindowCustom form = new FormWindowCustom("§0§l확성기", formElements);
		player.showFormWindow(form, FORM_ID);
	}

}
