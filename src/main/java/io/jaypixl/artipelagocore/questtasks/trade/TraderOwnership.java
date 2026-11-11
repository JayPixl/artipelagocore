package io.jaypixl.artipelagocore.questtasks.trade;

/**
 * Who owns a trader, for the purpose of deciding whether a customer trade
 * counts and whether an owner can be credited.
 */
public enum TraderOwnership {

    /** A single player. Customer trades count and the owner can be credited. */
    PLAYER,

    /** A Lightman's Currency team. Customer trades count; nobody is the owner. */
    TEAM,

    /** The server (admin shop, fake owner or no owner). Governed by the task's toggle. */
    SERVER
}
