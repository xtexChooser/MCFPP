package top.mcfpp.mni;

import org.jetbrains.annotations.NotNull;
import top.mcfpp.annotations.MNIFunction;
import top.mcfpp.core.lang.JavaVar;
import top.mcfpp.core.lang.JsonTextConcrete;
import top.mcfpp.core.lang.Var;
import top.mcfpp.lib.ListChatComponent;
import top.mcfpp.lib.PlainChatComponent;
import top.mcfpp.util.TempPool;
import top.mcfpp.util.ValueWrapper;

import java.util.UUID;

public class MCAnyConcreteData {

    @MNIFunction(normalParams = {"any a"}, returnType = "JavaVar")
    public static void getJavaVar(@NotNull Var<?> value, ValueWrapper<Var<?>> returnValue){
        var re = new JavaVar(value, TempPool.INSTANCE.getVarIdentify());
        returnValue.setValue(re);
    }

    @MNIFunction(caller = "any", returnType = "text")
    public static void toText(@NotNull Var<?> caller, ValueWrapper<JsonTextConcrete> returnValue){
        var l = new ListChatComponent();
        l.getComponents().add(new PlainChatComponent(caller.toString()));
        returnValue.setValue(new JsonTextConcrete(l, "re"));
    }
}
