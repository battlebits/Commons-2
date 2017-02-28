package br.com.battlebits.commons.api.item;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import br.com.battlebits.commons.util.string.StringLoreUtils;

public class ItemBuilder {
	private Material material;
	private int amount;
	private short durability;
	private boolean useMeta;
	private boolean glow;
	private String displayName;
	private HashMap<Enchantment, Integer> enchantments;
	private ArrayList<String> lore;
	
	private Color leatherColor;
	private String skullOwner;
	private String skullSkinUrl;
	
	public ItemBuilder() {
		material = Material.STONE;
		amount = 1;
		durability = 0;
		useMeta = false;
		glow = false;
	}

	public ItemBuilder type(Material material) {
		this.material = material;
		return this;
	}

	public ItemBuilder amount(int amount) {
		if (amount > 64) {
			amount = 64;
		} else if (amount == 0) {
			amount = 1;
		}
		this.amount = amount;
		return this;
	}

	public ItemBuilder durability(int durability) {
		if (durability >= 0 && durability <= 15) {
			this.durability = (short) durability;
		}
		return this;
	}

	public ItemBuilder name(String text) {
		if (!useMeta) {
			useMeta = true;
		}
		this.displayName = text.replace("&", "§");
		return this;
	}

	public ItemBuilder enchantment(Enchantment enchantment) {
		return enchantment(enchantment, 1);
	}

	public ItemBuilder enchantment(Enchantment enchantment, Integer level) {
		if (enchantments == null) {
			enchantments = new HashMap<>();
		}
		enchantments.put(enchantment, level);
		return this;
	}

	public ItemBuilder lore(String text) {
		if (!this.useMeta) {
			this.useMeta = true;
		}
		this.lore = new ArrayList<>(StringLoreUtils.getLore(30, text));
		return this;
	}

	public ItemBuilder lore(List<String> text) {
		if (!this.useMeta) {
			this.useMeta = true;
		}
		if (this.lore == null) {
			this.lore = new ArrayList<>();
		}
		for (String str : text) {
			this.lore.add(str.replace("&", "§"));
		}
		return this;
	}

	public ItemBuilder glow() {
		glow = true;
		return this;
	}

	public ItemBuilder setLeatherColor(Color color) {
		this.leatherColor = color;
		return this;
	}
	
	public ItemBuilder setSkullOwner(String nickname) {
		this.skullOwner = nickname;
		return this;
	}
	
	public ItemBuilder setSkullSkinURL(String url) {
		this.skullSkinUrl = url;
		return this;
	}
	
	public ItemStack build() {

		ItemStack stack = new ItemStack(material);
		stack.setAmount(amount);
		stack.setDurability(durability);
		if (enchantments != null && !enchantments.isEmpty()) {
			for (Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
				stack.addUnsafeEnchantment(entry.getKey(), entry.getValue());
			}
		}
		if (useMeta) {
			ItemMeta meta = stack.getItemMeta();
			if (displayName != null) {
				meta.setDisplayName(displayName.replace("&", "§"));
			}
			if (lore != null && !lore.isEmpty()) {
				meta.setLore(lore);
			}
			/* Colored Leather Armor */
			if (leatherColor != null) {
				if (meta instanceof LeatherArmorMeta) {
					((LeatherArmorMeta) meta).setColor(leatherColor);
				}
			}
			/* Skull Heads */
			if (meta instanceof SkullMeta) {
				SkullMeta skullMeta = (SkullMeta) meta;
				if (skullSkinUrl != null) {
					GameProfile profile = new GameProfile(UUID.randomUUID(), null);
					profile.getProperties().put("textures", new Property("textures", Base64.getEncoder().encodeToString(String.format("{textures:{SKIN:{url:\"%s\"}}}", skullSkinUrl).getBytes(StandardCharsets.UTF_8))));
					try {
						Field field = skullMeta.getClass().getDeclaredField("profile");
						field.setAccessible(true);
						field.set(skullMeta, profile);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (skullOwner != null) {
					skullMeta.setDisplayName(skullOwner);					
				}
			}
			stack.setItemMeta(meta);
		}
		if (glow && (enchantments == null || enchantments.isEmpty())) {
			try {
				Constructor<?> caller = MinecraftReflection.getCraftItemStackClass()
						.getDeclaredConstructor(ItemStack.class);
				caller.setAccessible(true);
				ItemStack item = (ItemStack) caller.newInstance(stack);
				NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
				compound.put(NbtFactory.ofList("ench"));
				return item;
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		material = Material.STONE;
		amount = 1;
		durability = 0;
		if (useMeta) {
			useMeta = false;
		}
		if (glow) {
			glow = false;
		}
		if (displayName != null) {
			displayName = null;
		}
		if (enchantments != null) {
			enchantments.clear();
			enchantments = null;
		}
		if (lore != null) {
			lore.clear();
			lore = null;
		}
		return stack;
	}

	public static ItemStack glow(ItemStack stack) {
		try {
			Constructor<?> caller = MinecraftReflection.getCraftItemStackClass()
					.getDeclaredConstructor(ItemStack.class);
			caller.setAccessible(true);
			ItemStack item = (ItemStack) caller.newInstance(stack);
			NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
			compound.put(NbtFactory.ofList("ench"));
			return item;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return stack;
	}
}
