package com.takumistudios.morphmod.ability;

public enum AbilitySlot {
	PRIMARY,
	SECONDARY;

	public static AbilitySlot byIndex(int index) {
		AbilitySlot[] values = values();
		return values[Math.floorMod(index, values.length)];
	}
}
