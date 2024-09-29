package top.mcfpp.mni.minecraft;

import org.jetbrains.annotations.NotNull;
import top.mcfpp.annotations.MNIFunction;
import top.mcfpp.command.Command;
import top.mcfpp.command.Commands;
import top.mcfpp.core.lang.*;
import top.mcfpp.core.lang.resource.Advancement;
import top.mcfpp.core.lang.resource.AdvancementConcrete;
import top.mcfpp.core.minecraft.PlayerVar;
import top.mcfpp.mni.hidden.AttributeData;
import top.mcfpp.model.CompoundData;
import top.mcfpp.model.function.Function;
import top.mcfpp.util.ValueWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlayerEntityData extends EntityVarData {

    @NotNull
    public static ArrayList<Var<?>> getMembers() {
        NormalCompoundDataObject attributes = new NormalCompoundDataObject("attribute", Map.of());
        CompoundData attributeData = new CompoundData("attribute", "mcfpp.hidden");
        attributeData.getNativeFromClass(AttributeData.class);
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "armor", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "armor_toughness", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "attack_damage", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "attack_speed", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "burning_time", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "explosion_knockback_resistance", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "fall_damage_multiplier", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "flying_speed", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "follow_range", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "gravity", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "jump_strength", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "knockback_resistance", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "luck", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "max_absorption", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "max_health", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "movement_efficiency", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "movement_speed", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "oxygen_bonus", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "safe_fall_distance", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "scale", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "step_height", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "water_movement_efficiency", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "block_break_speed", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "block_interaction_range", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "entity_interaction_range", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "mining_efficiency", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "sneaking_speed", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "submerged_mining_speed", Map.of()));
        attributes.getData().addMember(new NormalCompoundDataObject(attributeData, "sweeping_damage_ratio", Map.of()));
        //属性
        return new ArrayList<>(List.of(attributes));
    }

    @MNIFunction(normalParams = {"Advancement advancement"}, caller = "Player", returnType = "CommandReturn")
    public static void grant(Advancement advancement, PlayerVar.PlayerEntityVar caller, ValueWrapper<CommandReturn> returnValue) {
        Command[] commands;
        if(advancement instanceof AdvancementConcrete){
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(),
                    new Command("advancement grant @s only " + ((AdvancementConcrete) advancement).getValue())
            );
        }else {
            var c = new Command("advancement grant @s only").buildMacro(advancement, true).buildMacroFunction();
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(), c[c.length - 1]);
            var l = Arrays.asList(c).subList(0, c.length - 1);
            l.addAll(Arrays.asList(commands));
            commands = l.toArray(new Command[0]);
        }
        returnValue.setValue(new CommandReturn(commands[commands.length - 1], "return"));
        Function.Companion.addCommands(commands);
    }

    @MNIFunction(caller = "Player", returnType = "CommandReturn")
    public static void grantAll(PlayerVar.PlayerEntityVar caller, ValueWrapper<CommandReturn> returnValue) {
        var commands = Commands.INSTANCE.runAsEntity(caller.getEntity(),
                new Command("advancement grant @s everything")
        );
        returnValue.setValue(new CommandReturn(commands[commands.length - 1], "return"));
        Function.Companion.addCommands(commands);
    }

    @MNIFunction(normalParams = {"Advancement advancement"}, caller = "Player", returnType = "CommandReturn")
    public static void grantFrom(Advancement advancement, PlayerVar.PlayerEntityVar caller, ValueWrapper<CommandReturn> returnValue) {
        Command[] commands;
        if(advancement instanceof AdvancementConcrete){
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(),
                    new Command("advancement grant @s from " + ((AdvancementConcrete) advancement).getValue())
            );
        }else {
            var c = new Command("return run advancement grant @s from").buildMacro(advancement, true).buildMacroFunction();
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(), c[c.length - 1]);
            var l = Arrays.asList(c).subList(0, c.length - 1);
            l.addAll(Arrays.asList(commands));
            commands = l.toArray(new Command[0]);
        }
        returnValue.setValue(new CommandReturn(commands[commands.length - 1], "return"));
        Function.Companion.addCommands(commands);
    }

    @MNIFunction(normalParams = {"Advancement advancement"}, caller = "Player", returnType = "CommandReturn")
    public static void grantThrough(Advancement advancement, PlayerVar.PlayerEntityVar caller, ValueWrapper<CommandReturn> returnValue) {
        Command[] commands;
        if(advancement instanceof AdvancementConcrete){
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(),
                    new Command("advancement grant @s through " + ((AdvancementConcrete) advancement).getValue())
            );
        }else {
            var c = new Command("return run advancement grant @s through").buildMacro(advancement, true).buildMacroFunction();
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(), c[c.length - 1]);
            var l = Arrays.asList(c).subList(0, c.length - 1);
            l.addAll(Arrays.asList(commands));
            commands = l.toArray(new Command[0]);
        }
        returnValue.setValue(new CommandReturn(commands[commands.length - 1], "return"));
        Function.Companion.addCommands(commands);
    }

    @MNIFunction(normalParams = {"Advancement advancement"}, caller = "Player", returnType = "CommandReturn")
    public static void grantUntil(Advancement advancement, PlayerVar.PlayerEntityVar caller, ValueWrapper<CommandReturn> returnValue) {
        Command[] commands;
        if(advancement instanceof AdvancementConcrete){
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(),
                    new Command("advancement grant @s until " + ((AdvancementConcrete) advancement).getValue())
            );
        }else {
            var c = new Command("return run advancement grant @s until").buildMacro(advancement, true).buildMacroFunction();
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(), c[c.length - 1]);
            var l = Arrays.asList(c).subList(0, c.length - 1);
            l.addAll(Arrays.asList(commands));
            commands = l.toArray(new Command[0]);
        }
        returnValue.setValue(new CommandReturn(commands[commands.length - 1], "return"));
        Function.Companion.addCommands(commands);
    }

    @MNIFunction(normalParams = {"Advancement advancement"}, caller = "Player", returnType = "CommandReturn")
    public static void revoke(Advancement advancement, PlayerVar.PlayerEntityVar caller, ValueWrapper<CommandReturn> returnValue) {
        Command[] commands;
        if(advancement instanceof AdvancementConcrete){
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(),
                    new Command("advancement revoke @s only " + ((AdvancementConcrete) advancement).getValue())
            );
        }else {
            var c = new Command("advancement revoke @s only").buildMacro(advancement, true).buildMacroFunction();
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(), c[c.length - 1]);
            var l = Arrays.asList(c).subList(0, c.length - 1);
            l.addAll(Arrays.asList(commands));
            commands = l.toArray(new Command[0]);
        }
        returnValue.setValue(new CommandReturn(commands[commands.length - 1], "return"));
        Function.Companion.addCommands(commands);
    }

    @MNIFunction(caller = "Player", returnType = "CommandReturn")
    public static void revokeAll(PlayerVar.PlayerEntityVar caller, ValueWrapper<CommandReturn> returnValue) {
        var commands = Commands.INSTANCE.runAsEntity(caller.getEntity(),
                new Command("advancement revoke @s everything")
        );
        returnValue.setValue(new CommandReturn(commands[commands.length - 1], "return"));
        Function.Companion.addCommands(commands);
    }

    @MNIFunction(normalParams = {"Advancement advancement"}, caller = "Player", returnType = "CommandReturn")
    public static void revokeFrom(Advancement advancement, PlayerVar.PlayerEntityVar caller, ValueWrapper<CommandReturn> returnValue) {
        Command[] commands;
        if(advancement instanceof AdvancementConcrete){
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(),
                    new Command("advancement revoke @s from " + ((AdvancementConcrete) advancement).getValue())
            );
        }else {
            var c = new Command("advancement revoke @s from").buildMacro(advancement, true).buildMacroFunction();
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(), c[c.length - 1]);
            var l = Arrays.asList(c).subList(0, c.length - 1);
            l.addAll(Arrays.asList(commands));
            commands = l.toArray(new Command[0]);
        }
        returnValue.setValue(new CommandReturn(commands[commands.length - 1], "return"));
        Function.Companion.addCommands(commands);
    }

    @MNIFunction(normalParams = {"Advancement advancement"}, caller = "Player", returnType = "CommandReturn")
    public static void revokeThrough(Advancement advancement, PlayerVar.PlayerEntityVar caller, ValueWrapper<CommandReturn> returnValue) {
        Command[] commands;
        if(advancement instanceof AdvancementConcrete){
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(),
                    new Command("advancement revoke @s through " + ((AdvancementConcrete) advancement).getValue())
            );
        }else {
            var c = new Command("advancement revoke @s through").buildMacro(advancement, true).buildMacroFunction();
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(), c[c.length - 1]);
            var l = Arrays.asList(c).subList(0, c.length - 1);
            l.addAll(Arrays.asList(commands));
            commands = l.toArray(new Command[0]);
        }
        returnValue.setValue(new CommandReturn(commands[commands.length - 1], "return"));
        Function.Companion.addCommands(commands);
    }

    @MNIFunction(normalParams = {"Advancement advancement"}, caller = "Player", returnType = "CommandReturn")
    public static void revokeUntil(Advancement advancement, PlayerVar.PlayerEntityVar caller, ValueWrapper<CommandReturn> returnValue) {
        Command[] commands;
        if(advancement instanceof AdvancementConcrete){
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(),
                    new Command("advancement revoke @s until " + ((AdvancementConcrete) advancement).getValue())
            );
        }else {
            var c = new Command("advancement revoke @s until").buildMacro(advancement, true).buildMacroFunction();
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(), c[c.length - 1]);
            var l = Arrays.asList(c).subList(0, c.length - 1);
            l.addAll(Arrays.asList(commands));
            commands = l.toArray(new Command[0]);
        }
        returnValue.setValue(new CommandReturn(commands[commands.length - 1], "return"));
        Function.Companion.addCommands(commands);
    }

    @MNIFunction(normalParams = {"string key, float scale"}, caller = "Player", returnType = "int")
    public static void getAttribute(MCString id, MCFloat scale, PlayerVar.PlayerEntityVar caller, ValueWrapper<CommandReturn> returnValue){
        Command[] commands;
        Command command;
        if(id instanceof MCStringConcrete idC){
            command = new Command("attribute @s " + idC.getValue().getValue() + " get");
        }else {
            command = new Command("attribute @s").buildMacro(id, true).build("get", false);
        }
        if(scale instanceof MCFloatConcrete scaleC){
             command.build(scaleC.getValue().toString(), true);
        }else {
            command.buildMacro(scale, true);
        }
        if(command.isMacro()){
            var mcs = command.buildMacroFunction();
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(), mcs[mcs.length - 1]);
            var l = Arrays.asList(mcs).subList(0, mcs.length - 1);
            l.addAll(Arrays.asList(commands));
            commands = l.toArray(new Command[0]);
        }else {
            commands = Commands.INSTANCE.runAsEntity(caller.getEntity(), command);
        }
        returnValue.setValue(new CommandReturn(commands[commands.length - 1], "return"));
        Function.Companion.addCommands(commands);
    }
}
