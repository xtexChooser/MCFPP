package top.mcfpp.mni;

import net.querz.nbt.io.SNBTUtil;
import org.jetbrains.annotations.NotNull;
import top.mcfpp.annotations.InsertCommand;
import top.mcfpp.annotations.MNIFunction;
import top.mcfpp.command.Command;
import top.mcfpp.command.Commands;
import top.mcfpp.core.lang.*;
import top.mcfpp.core.lang.bool.BaseBool;
import top.mcfpp.core.lang.bool.ScoreBool;
import top.mcfpp.core.lang.bool.ScoreBoolConcrete;
import top.mcfpp.core.lang.nbt.MCString;
import top.mcfpp.core.lang.nbt.MCStringConcrete;
import top.mcfpp.core.lang.nbt.NBTBasedData;
import top.mcfpp.core.lang.nbt.NBTBasedDataConcrete;
import top.mcfpp.lib.ScoreChatComponent;
import top.mcfpp.model.function.Function;
import top.mcfpp.util.LogProcessor;
import top.mcfpp.util.NBTUtil;
import top.mcfpp.util.ValueWrapper;

import java.io.IOException;
import java.util.UUID;

public class System {

    @MNIFunction(normalParams = {"any a"}, returnType = "type")
    public static void typeOf(@NotNull Var<?> value, ValueWrapper<MCFPPTypeVar> returnValue){
        var re = new MCFPPTypeVar(value.getType(), UUID.randomUUID().toString());
        returnValue.setValue(re);
    }

    @InsertCommand
    @MNIFunction(normalParams = {"text t"})
    public static void print(@NotNull JsonText text){
        if(text instanceof JsonTextConcrete textC){
            Function.Companion.addCommand(new Command("tellraw @a").build(textC.getValue().toCommandPart(), true));
        }else {
            Function.Companion.addCommand(new Command("tellraw @a").build(text.toCommandPart(), true));
        }
    }

    @InsertCommand
    @MNIFunction(normalParams = {"any a"})
    public static void print(@NotNull Var<?> value){
        Function.Companion.addCommand("tellraw @a " + "\"" + value + "\"");
    }

    @InsertCommand
    @MNIFunction(normalParams = {"int i"})
    public static void print(@NotNull MCInt var) {
        if (var instanceof MCIntConcrete varC) {
            //是确定的，直接输出数值
            Function.Companion.addCommand("tellraw @a " + varC.getValue());
        }else {
            if(var.getParent() != null){
                Function.Companion.addCommands(Commands.INSTANCE.selectRun(var.getParent(), "tellraw @a " + new ScoreChatComponent(var).toCommandPart(), true));
            }else {
                Function.Companion.addCommand("tellraw @a " + new ScoreChatComponent(var).toCommandPart());
            }
        }
    }

    //@InsertCommand
    //public static void print(JsonString var){
    //    Function.Companion.addCommand("tellraw @a " + var.getJsonText().toJson());
    //}

    @InsertCommand
    @MNIFunction(normalParams = {"nbt n"})
    public static void print(@NotNull NBTBasedData var){
        if(var instanceof NBTBasedDataConcrete varC){
            Function.Companion.addCommand("tellraw @a \"" + NBTUtil.INSTANCE.toJava(varC.getValue()) + "\"");
        }else {
            //TODO
        }
    }

    @InsertCommand
    @MNIFunction(normalParams = {"string s"})
    public static void print(@NotNull MCString var) {
        if(var instanceof MCStringConcrete varC){
            Function.Companion.addCommand("tellraw @a \"" + varC.getValue().getValue() + "\"");
        }else{
            //TODO
        }
    }

    @InsertCommand
    @MNIFunction(normalParams = {"DataObject o"})
    public static void print(@NotNull DataTemplateObject object) throws IOException {
        if(object instanceof DataTemplateObjectConcrete objectConcrete){
            Function.Companion.addCommand("tellraw @a \"" + SNBTUtil.toSNBT(objectConcrete.getValue()) + "\"");
        }else {
            //TODO
            Function.Companion.addCommand("TODO: tellraw templateData");
        }
    }

    @InsertCommand
    @MNIFunction(normalParams = {"bool b"})
    public static void print(BaseBool bool){
        ScoreBool b;
        if(bool instanceof ScoreBool){
            b = (ScoreBool) bool;
        }else {
            b = bool.toScoreBool();
        }
        if(b instanceof ScoreBoolConcrete bC){
            Function.Companion.addCommand("tellraw @a " + bC.getValue());
        }else {
            if(b.getParent() != null){
                Function.Companion.addCommands(Commands.INSTANCE.selectRun(b.getParent(), "tellraw @a " + new ScoreChatComponent(b.asIntVar()).toCommandPart(), true));
            }else {
                Function.Companion.addCommand("tellraw @a " + new ScoreChatComponent(b.asIntVar()).toCommandPart());
            }
        }
    }

    @MNIFunction
    public static void debug(){
        //噢，在这里断点，这样就可以断点编译了
        int i = 0;
    }

    @MNIFunction(normalParams = {"string s"})
    public static void info(@NotNull MCString var){
        if(var instanceof MCStringConcrete varC){
            LogProcessor.INSTANCE.info(varC.getValue().getValue());
        }else{
            LogProcessor.INSTANCE.info(var.toString());
        }
    }

    @MNIFunction(normalParams = {"string s"})
    public static void warn(@NotNull MCString var){
        if(var instanceof MCStringConcrete varC){
            LogProcessor.INSTANCE.warn(varC.getValue().getValue());
        }else{
            LogProcessor.INSTANCE.warn(var.toString());
        }
    }

    @MNIFunction(normalParams = {"string s"})
    public static void error(@NotNull MCString var){
        if(var instanceof MCStringConcrete varC){
            LogProcessor.INSTANCE.error(varC.getValue().getValue());
        }else{
            LogProcessor.INSTANCE.error(var.toString());
        }
    }
}
