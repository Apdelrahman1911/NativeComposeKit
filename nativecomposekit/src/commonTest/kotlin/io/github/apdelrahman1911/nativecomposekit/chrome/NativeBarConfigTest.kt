package io.github.apdelrahman1911.nativecomposekit.chrome

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Pins the per-screen chrome config contract: value semantics (shells de-duplicate emissions by equality),
 * the do-nothing default, and that a [NativeChromeEntry] carries its config without disturbing entries
 * built the pre-config way (the appended-field ABI pattern).
 */
class NativeBarConfigTest {

    @Test
    fun default_config_changes_nothing() {
        val d = NativeBarConfig.Default
        assertEquals(false, d.hidesTopBar)
        assertEquals(false, d.hidesTabBar)
        assertTrue(d.actions.isEmpty())
        // A freshly constructed no-arg config is the same value as Default.
        assertEquals(NativeBarConfig(), d)
        assertEquals(NativeBarConfig().hashCode(), d.hashCode())
    }

    @Test
    fun compares_by_value_across_every_field() {
        val action = NativeChromeAction("share", "square.and.arrow.up")
        val a = NativeBarConfig(hidesTopBar = true, actions = listOf(action))
        val same = NativeBarConfig(hidesTopBar = true, actions = listOf(NativeChromeAction("share", "square.and.arrow.up")))
        assertEquals(a, same)
        assertEquals(a.hashCode(), same.hashCode())

        assertNotEquals(a, NativeBarConfig(hidesTopBar = false, actions = listOf(action)))
        assertNotEquals(a, NativeBarConfig(hidesTopBar = true)) // actions differ
        assertNotEquals(NativeBarConfig(hidesTabBar = true), NativeBarConfig(hidesTopBar = true))
    }

    @Test
    fun entries_carry_their_config_and_default_it_when_omitted() {
        val immersive = NativeBarConfig(hidesTopBar = true, hidesTabBar = true)
        val reader = NativeChromeEntry("reader/1", "Reader", immersive)
        assertEquals(immersive, reader.bar)

        // Pre-config construction stays valid and equals an explicit-Default entry (appended-field pattern).
        val legacy = NativeChromeEntry("home", "Home")
        assertEquals(NativeBarConfig.Default, legacy.bar)
        assertEquals(NativeChromeEntry("home", "Home", NativeBarConfig.Default), legacy)

        // The config participates in entry equality — a shell may skip re-rendering only on true equality.
        assertNotEquals(reader, NativeChromeEntry("reader/1", "Reader"))
    }

    @Test
    fun state_projects_per_entry_configs_through_the_contract() {
        // A minimal stack-projecting source — proves the config travels the nav-agnostic contract per entry.
        val entries = listOf(
            NativeChromeEntry("home", "Home"),
            NativeChromeEntry("reader/1", "Reader", NativeBarConfig(hidesTopBar = true, hidesTabBar = true)),
        )
        val state = NativeChromeState(
            title = "Reader",
            canGoBack = true,
            selectedTabId = "library",
            tabs = listOf(NativeChromeTab("library", "Library", "books.vertical")),
            actions = emptyList(),
            sheetId = null,
            backStacksByTab = mapOf("library" to entries),
        )
        val projected = state.backStacksByTab.getValue("library")
        assertEquals(NativeBarConfig.Default, projected[0].bar)
        assertTrue(projected[1].bar.hidesTopBar)
        assertTrue(projected[1].bar.hidesTabBar)
    }
}
