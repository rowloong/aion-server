package admincommands;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.base.BaseLocation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.BaseService;
import com.aionemu.gameserver.services.base.Base;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

public class BaseCommand extends AdminCommand {

	private static final String COMMAND_LIST = "list";
	private static final String COMMAND_CAPTURE = "capture";
	private static final String COMMAND_ASSAULT = "assault";

	public BaseCommand() {
		super("base");
	}

	@Override
	public void execute(Player player, String... params) {

		if (params.length == 0) {
			showHelp(player);
			return;
		}

		if (COMMAND_LIST.equalsIgnoreCase(params[0]))
			handleList(player, params);
		else if (COMMAND_CAPTURE.equals(params[0]))
			capture(player, params);
		else if (COMMAND_ASSAULT.equals(params[0]))
			assault(player, params);
	}

	protected boolean isValidBaseLocationId(Player player, int baseId) {
		if (!BaseService.getInstance().getBaseLocations().keySet().contains(baseId)) {
			PacketSendUtility.sendMessage(player, "Id " + baseId + " is invalid");
			return false;
		}
		return true;
	}

	protected void handleList(Player player, String[] params) {
		if (params.length != 1) {
			showHelp(player);
			return;
		}

		for (BaseLocation base : BaseService.getInstance().getBaseLocations().values()) {
			PacketSendUtility.sendMessage(player, "Base:" + base.getId() + " belongs to " + base.getRace());
		}
	}

	protected void capture(Player player, String[] params) {
		if (params.length < 3 || !NumberUtils.isNumber(params[1])) {
			showHelp(player);
			return;
		}

		int baseId = NumberUtils.toInt(params[1]);
		if (!isValidBaseLocationId(player, baseId))
			return;

		// check if params2 is race
		Race race = null;
		try {
			race = Race.valueOf(params[2].toUpperCase());
		}
		catch (IllegalArgumentException e) {
			// ignore
		}

		// check if can capture
		if (race == null) {
			PacketSendUtility.sendMessage(player, params[2] + " is not valid race");
			showHelp(player);
			return;
		}

		// capture
		Base<?> base = BaseService.getInstance().getActiveBase(baseId);
		if (base != null) {
			BaseService.getInstance().capture(baseId, race);
		}
	}

	protected void assault(Player player, String[] params) {
		if (params.length < 3 || !NumberUtils.isNumber(params[1])) {
			showHelp(player);
			return;
		}

		int baseId = NumberUtils.toInt(params[1]);
		if (!isValidBaseLocationId(player, baseId))
			return;

		// check if params2 is race
		Race race = null;
		try {
			race = Race.valueOf(params[2].toUpperCase());
		}
		catch (IllegalArgumentException e) {
			// ignore
		}

		// check if race is valid
		if (race == null) {
			PacketSendUtility.sendMessage(player, params[2] + " is not valid race");
			showHelp(player);
			return;
		}

		// assault
		Base<?> base = BaseService.getInstance().getActiveBase(baseId);
		if (base != null) {
			if (base.isUnderAssault())
				PacketSendUtility.sendMessage(player, "Assault already started!");
			else
				base.spawnBySpawnHandler(SpawnHandlerType.ATTACKER, race);
		}
	}

	protected void showHelp(Player player) {
		PacketSendUtility.sendMessage(player, "AdminCommand //base Help\n" + "//base list\n"
			+ "//base capture <Id> <Race (ELYOS,ASMODIANS,NPC)>\n" + "//base assault <Id> <delaySec>");
	}

}
