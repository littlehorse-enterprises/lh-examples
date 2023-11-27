package lh.demo.send.email;

public class App {

    /*
     * As per the root README, there are two commands here:
     * - register, which registers the TaskDef
     * - run-worker, which runs the Task Worker.
     */
    public static void main(String[] args) {
        if (args.length != 1 || (!args[0].equals("register") && !args[0].equals("run-worker"))) {
            System.err.println("Please provide either the 'register' or 'run-worker' command");
            System.exit(1);
        }
        System.out.println(new App().getGreeting());
    }
}
