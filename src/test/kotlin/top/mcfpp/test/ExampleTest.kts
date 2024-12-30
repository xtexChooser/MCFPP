package top.mcfpp.test

val n = 3
solveHanoi(n, 1, 3, 2)

fun solveHanoi(n: Int, f: Int, to: Int, aux: Int){
    if (n == 1) {
        println("Move disk 1 from rod $f to rod $to")
        return
    }
    solveHanoi(n - 1, f, aux, to)
    println("Move disk $n from rod $f to rod $to")
    solveHanoi(n - 1, aux, to, f)
}