package net.dirtlands.database;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class ItemSerialization {

    private Inventory inventory;
    private String title;

    public ItemSerialization(Inventory inventory, String titleGson) {
        this.inventory = inventory;
        this.title = titleGson;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * A method to serialize an inventory to Base64 string.
     *
     * @param inventoryData to serialize
     * @return Base64 string of the provided inventory
     */
    public static String toBase64(ItemSerialization inventoryData) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(inventoryData.getInventory().getSize());

            // Save every element in the list
            for (int i = 0; i < inventoryData.getInventory().getSize(); i++) {
                ItemStack item = inventoryData.getInventory().getItem(i);
                if (item == null) {
                    dataOutput.writeObject(null);
                    continue;
                }
                dataOutput.writeObject(new SerializedItem(item.serializeAsBytes()));
            }

            dataOutput.writeObject(inventoryData.getTitle().getBytes(StandardCharsets.UTF_8));

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     *
     * A method to get an {@link Inventory} from an encoded, Base64, string.
     *
     * @param data Base64 string of data containing an inventory.
     * @return Inventory created from the Base64 string.
     */
    public static @NotNull ItemSerialization fromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

            // Read the serialized inventory
            for (int i = 0; i < inventory.getSize(); i++) {
                Object item = dataInput.readObject();
                if (item == null) {
                    inventory.setItem(i, null);
                }
                else if (item instanceof ItemStack) {
                    inventory.setItem(i, (ItemStack)item);
                }
                else if (item instanceof SerializedItem serialized) {
                    inventory.setItem(i, ItemStack.deserializeBytes(serialized.bytes));
                }
            }

            dataInput.close();
            return new ItemSerialization(inventory, new String(dataInput.readAllBytes()));
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}

class SerializedItem implements Serializable {

    byte[] bytes;

    public SerializedItem(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
