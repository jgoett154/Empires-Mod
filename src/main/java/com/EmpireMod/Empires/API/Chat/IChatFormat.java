package com.EmpireMod.Empires.API.Chat;

import net.minecraft.util.IChatComponent;

/**
 * Represents an object type that can be converted into a chat message or a part of a chat message
 */
public interface IChatFormat {

    IChatComponent toChatMessage();

}