package fr.xamez.memories.utils;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Easily create itemstacks, without messing your hands. <i>Note that if you do
 * use this in one of your projects, leave this notice.</i> <i>Please do credit
 * me if you do use this in one of your projects.</i>
 * @author NonameSL
 */
public class ItemBuilder {
    private ItemStack is;

    /**
     * Create a new ItemBuilder from scratch.
     * @param m The material to create the ItemBuilder with.
     */
    public ItemBuilder(Material m) {
        this(m, 1);
    }

    /**
     * Create a new ItemBuilder over an existing itemstack.
     * @param is The itemstack to create the ItemBuilder over.
     */
    public ItemBuilder(ItemStack is) {
        this.is = is;
    }

    /**
     * Create a new ItemBuilder from scratch.
     * @param m      The material of the item.
     * @param amount The amount of the item.
     */
    public ItemBuilder(Material m, int amount) {
        is = new ItemStack(m, amount);
    }

    /**
     * Clone the ItemBuilder into a new one.
     * @return The cloned instance.
     */
    @Override
    public ItemBuilder clone() {
        return new ItemBuilder(is);
    }

    /**
     * Set the displayname of the item.
     * @param name The name to change it to.
     */
    public ItemBuilder setName(String name) {
        ItemMeta im = is.getItemMeta();
        assert im != null;
        im.setDisplayName(name);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Set the skull owner for the item. Works on skulls only.
     * @param owner The name of the skull's owner.
     */
    public ItemBuilder setSkullOwner(final OfflinePlayer owner) {
        try {
            SkullMeta im = (SkullMeta) is.getItemMeta();
            assert im != null;
            im.setOwningPlayer(owner);
            is.setItemMeta(im);
        } catch (ClassCastException ignored) {}
        return this;
    }

    /**
     * Sets infinity durability on the item by setting the durability to
     * @param bool Set infinity durability or not
     * Short.MAX_VALUE.
     */
    public ItemBuilder setInfinityDurability(boolean bool) {
        ItemMeta im = is.getItemMeta();
        assert im != null;
        im.setUnbreakable(bool);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Re-sets the lore.
     * @param lore The lore to set it to .
     */
    public ItemBuilder setLore(List<String> lore) {
        ItemMeta im = is.getItemMeta();
        assert im != null;
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Add enchantement
     * @param ench Enchantement to add
     * @param lvl Level of enchantement
     * @return
     */
    public ItemBuilder addEnchantements(Enchantment ench, int lvl){
        ItemMeta im = is.getItemMeta();
        assert im != null;
        im.addEnchant(ench, lvl, true);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Add item flag
     * @param itemFlag ItemFlag to add
     * @return
     */
    public ItemBuilder addItemFlag(ItemFlag itemFlag){
        ItemMeta im = is.getItemMeta();
        assert im != null;
        im.addItemFlags(itemFlag);
        is.setItemMeta(im);
        return this;
    }


    /**
     * Remove a lore line.
     * @param line The lore to remove.
     */
    public ItemBuilder removeLoreLine(String line) {
        ItemMeta im = is.getItemMeta();
        assert im != null;
        List<String> lore = new ArrayList<>(Objects.requireNonNull(im.getLore()));
        if(!lore.contains(line))
            return this;
        lore.remove(line);
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Remove a lore line.
     * @param index The index of the lore line to remove.
     */
    public ItemBuilder removeLoreLine(int index) {
        ItemMeta im = is.getItemMeta();
        assert im != null;
        List<String> lore = new ArrayList<>(Objects.requireNonNull(im.getLore()));
        if(index < 0 || index > lore.size())
            return this;
        lore.remove(index);
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Add a lore line.
     * @param line The lore line to add.
     */
    public ItemBuilder addLoreLine(String line) {
        ItemMeta im = is.getItemMeta();
        List<String> lore = new ArrayList<>();
        assert im != null;
        if(im.hasLore())
            lore = new ArrayList<>(Objects.requireNonNull(im.getLore()));
        lore.add(line);
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Add a lore line.
     * @param line The lore line to add.
     * @param pos  The index of where to put it.
     */
    public ItemBuilder addLoreLine(String line, int pos) {
        ItemMeta im = is.getItemMeta();
        assert im != null;
        List<String> lore = new ArrayList<>(Objects.requireNonNull(im.getLore()));
        lore.set(pos, line);
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    public void clearLoreLines() {
        ItemMeta im = is.getItemMeta();
        assert im != null;
        im.setLore(new ArrayList<>());
        is.setItemMeta(im);
    }

    /**
     * Retrieves the itemstack from the ItemBuilder.
     * @return The itemstack created/modified by the ItemBuilder instance.
     */
    public ItemStack toItemStack() { return is; }

}
