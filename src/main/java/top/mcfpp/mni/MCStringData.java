package top.mcfpp.mni;

import top.mcfpp.annotations.MNIFunction;
import top.mcfpp.core.lang.JsonTextConcrete;
import top.mcfpp.core.lang.MCInt;
import top.mcfpp.core.lang.nbt.MCString;
import top.mcfpp.lib.ListChatComponent;
import top.mcfpp.lib.NBTChatComponent;
import top.mcfpp.lib.ScoreChatComponent;
import top.mcfpp.util.ValueWrapper;

public class MCStringData {

    @MNIFunction(caller = "string", returnType = "text", override = true)
    public static void toText(MCString caller, ValueWrapper<JsonTextConcrete> returnValue) {
        var l = new ListChatComponent();
        l.getComponents().add(new NBTChatComponent(caller, false, null));
        returnValue.setValue(new JsonTextConcrete(l, "re"));
    }
}
