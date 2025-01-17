package top.mcfpp.model

import top.mcfpp.model.field.GlobalField
import top.mcfpp.util.LogProcessor

class UnsolvedNamespace(val identifier: String) {

    fun resolve(): Namespace{
        if(GlobalField.getNamespace(identifier) != null){
            return GlobalField.getNamespace(identifier)!!
        }
        LogProcessor.error("Namespace $identifier not found")
        return Namespace(identifier)
    }

}