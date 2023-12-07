package lh.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class Greeter {

    @LHTaskMethod("greet")
    public String greeting(String name) {
        System.out.println("Executing greet task!");
        return "Hello, " + name + "!";
    }
}
