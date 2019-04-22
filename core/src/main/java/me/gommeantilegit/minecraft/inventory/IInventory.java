package me.gommeantilegit.minecraft.inventory;

import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IInventory {

    /**
     * @return the number of slots in the inventory.
     */
    int getSizeInventory();

    /**
     * @return the stack in the given slot. Can be null if no stack is in the slot!
     */
    @Nullable
    ItemStack getStackInSlot(int index);

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     * @param index the index of the slot to decrement
     * @param count the amount to decrement the stack by
     * @return the new item stack that the decrement results in
     */
    @NotNull
    ItemStack decrStackSize(int index, int count);

    /**
     * Removes a stack from the given slot and returns it.
     * @return the stack that was removed
     */
    ItemStack removeStackFromSlot(int index);

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     * @param index the index of the slot to be set to the ItemStack
     * @param stack the stack that should be stored in the specified slot. Can be null, if the slot should be empty.
     */
    void setInventorySlotContents(int index, @Nullable ItemStack stack);

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    boolean isItemValidForSlot(int index, ItemStack stack);

}
