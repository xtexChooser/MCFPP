scoreboard objectives add mcfpp.minecraft:bossbarstyle dummy
scoreboard objectives add mcfpp.minecraft.other:attributemodifiertype dummy
scoreboard objectives add mcfpp_default dummy
scoreboard objectives add mcfpp.minecraft:bossbarstyle dummy
scoreboard objectives add mcfpp.minecraft.entity:gossiptype dummy
scoreboard objectives add mcfpp.minecraft.other:attributemodifiertype dummy
scoreboard objectives add mcfpp.minecraft.entity:pandagene dummy
scoreboard objectives add mcfpp.minecraft.entity:gossiptype dummy
scoreboard objectives add mcfpp.minecraft.entity:frogvariant dummy
scoreboard objectives add mcfpp.minecraft.entity:salmontype dummy
scoreboard objectives add mcfpp.minecraft.item:fireworkshape dummy
scoreboard objectives add mcfpp.minecraft.item:attributeoperation dummy
scoreboard objectives add mcfpp.minecraft.entity:salmontype dummy
scoreboard objectives add mcfpp.minecraft.item:attributeslot dummy
scoreboard objectives add mcfpp.minecraft.entity:foxtype dummy
scoreboard objectives add mcfpp_temp dummy
scoreboard objectives add mcfpp_boolean dummy
scoreboard objectives add mcfpp.minecraft.item:attributeslot dummy
scoreboard objectives add mcfpp.minecraft.item:fireworkshape dummy
scoreboard objectives add mcfpp_init dummy
scoreboard objectives add mcfpp.minecraft:bossbarcolor dummy
scoreboard objectives add mcfpp.minecraft.entity:frogvariant dummy
scoreboard objectives add mcfpp.minecraft.entity:llamavariant dummy
scoreboard objectives add mcfpp.minecraft.entity:pandagene dummy
scoreboard objectives add mcfpp.minecraft.entity:llamavariant dummy
scoreboard objectives add mcfpp.minecraft.entity:armadillostate dummy
scoreboard objectives add mcfpp.minecraft.entity:armadillostate dummy
scoreboard objectives add mcfpp.minecraft:bossbarcolor dummy
scoreboard objectives add mcfpp.minecraft.item:attributeoperation dummy
scoreboard objectives add mcfpp.minecraft.entity:foxtype dummy
execute unless score math mcfpp_init matches 1 run function math:_init
summon item 0 0 0 {Tags:["mcfpp_ptr_marker"],UUID:[I;-2129829775,-249476750,-2133434164,431230200], Age:-32768, NoGravity: true, Item:{id:"stone"}, Invulnerable: true}
summon marker 0 0 0 {Tags:["mcfpp_float_marker"],UUID:[I;1403656652,-1603846101,-1952345304,-866142527]}
