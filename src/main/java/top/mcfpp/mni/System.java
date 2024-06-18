package top.mcfpp.mni;

import org.jetbrains.annotations.NotNull;
import top.mcfpp.annotations.InsertCommand;
import top.mcfpp.annotations.MNIRegister;
import top.mcfpp.lang.*;
import top.mcfpp.model.function.Function;
import top.mcfpp.util.NBTUtil;
import top.mcfpp.util.ValueWrapper;

import java.util.HashMap;
import java.util.UUID;

public class System {

    @MNIRegister(normalParams = {"any a"})
    public static void typeOf(@NotNull Var<?> value, ValueWrapper<MCFPPTypeVar> returnValue){
        var re = new MCFPPTypeVar(value.getType(), UUID.randomUUID().toString());
        returnValue.setValue(re);
    }


    @InsertCommand
    @MNIRegister(normalParams = {"any a"})
    public static void print(@NotNull Var<?> value){
        Function.Companion.addCommand("tellraw @a " + "\"" + value + "\"");
    }

    @InsertCommand
    @MNIRegister(normalParams = {"int i"})
    public static void print(@NotNull MCInt var) {
        if (var instanceof MCIntConcrete varC) {
            //是确定的，直接输出数值
            Function.Companion.addCommand("tellraw @a " + varC.getValue());
        }else {
            Function.Companion.addCommand("tellraw @a " + new JsonTextNumber(var).toJson());
        }
    }

    //@InsertCommand
    //public static void print(JsonString var){
    //    Function.Companion.addCommand("tellraw @a " + var.getJsonText().toJson());
    //}

    @InsertCommand
    @MNIRegister(normalParams = {"nbt n"})
    public static void print(@NotNull NBTBasedData<?> var){
        if(var instanceof NBTBasedDataConcrete<?> varC){
            Function.Companion.addCommand("tellraw @a " + NBTUtil.INSTANCE.toJava(varC.getValue()));
        }else {
            //TODO
        }
    }

    @InsertCommand
    @MNIRegister(normalParams = {"string s"})
    public static void print(@NotNull MCString var) {
        if(var instanceof MCStringConcrete varC){
            Function.Companion.addCommand("tellraw @a \"" + varC.getValue().getValue() + "\"");
        }else{
            //TODO
        }
    }
}