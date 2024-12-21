package top.mcfpp.test

import top.mcfpp.test.util.MCFPPStringTest
import kotlin.test.Test

class NBTTypeTest {

    @Test
    fun listTest(){
        val test =
            """
                func main(){
                    list<int> l = [];
                    l.add(1);
                    l.add(2);
                    print(l.getJavaVar());
                }
            """.trimIndent()
        MCFPPStringTest.readFromString(test, arrayOf("-debug"))
        println()
    }

}