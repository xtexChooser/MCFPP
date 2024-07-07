package top.mcfpp.test

import top.mcfpp.test.util.MCFPPStringTest
import kotlin.test.Test

class EnumTest {
    @Test
    fun declareTest(){
        val test =
            """
                enum Test{
                    A=1,B=5,C,D
                } 
                
                func main(){
                    Test qwq = A;
                    print(qwq);
                }
            """.trimIndent()
        MCFPPStringTest.readFromString(test)
    }
}