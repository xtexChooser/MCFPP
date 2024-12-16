package top.mcfpp.mni;

import org.jetbrains.annotations.NotNull;
import top.mcfpp.annotations.MNIFunction;
import top.mcfpp.core.lang.JavaVar;
import top.mcfpp.core.lang.JsonTextConcrete;
import top.mcfpp.core.lang.nbt.NBTBasedDataConcrete;
import top.mcfpp.core.lang.Var;
import top.mcfpp.lib.ListChatComponent;
import top.mcfpp.lib.PlainChatComponent;
import top.mcfpp.util.ValueWrapper;

import java.util.UUID;

public class MCAnyData {
    @MNIFunction(caller = "any", returnType = "JavaVar")
    public static void getJavaVar(@NotNull Var<?> caller, ValueWrapper<Var<?>> returnValue){
        var re = new JavaVar(caller, UUID.randomUUID().toString());
        returnValue.setValue(re);
    }

    @MNIFunction(caller = "any", returnType = "text")
    public static void toText(@NotNull Var<?> caller, ValueWrapper<JsonTextConcrete> returnValue){
        var l = new ListChatComponent();
        l.getComponents().add(new PlainChatComponent(caller.toString()));
        returnValue.setValue(new JsonTextConcrete(l, "re"));
    }

    @MNIFunction(caller = "any", returnType = "nbt", isObject = true)
    public static void getDefault(@NotNull Var<?> caller, ValueWrapper<NBTBasedDataConcrete> returnValue){
        var value = caller.getType().defaultValue();
        returnValue.setValue((NBTBasedDataConcrete) returnValue.getValue().assignedBy(new NBTBasedDataConcrete(value, "")));
    }
}
