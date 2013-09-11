package com.martinbrook.TesseractApocalypse;

import org.bukkit.plugin.java.JavaPlugin;

public class TesseractApocalypse extends JavaPlugin {

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new TesseractApocalypseListener(this), this);
	}

}
